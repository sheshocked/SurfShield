package com.surfshield.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Measures how reachable each candidate endpoint is, so Smart Connect can rank
 * servers instead of making the user guess.
 *
 * Why TCP and not the real UDP port: a WireGuard server never answers an
 * unauthenticated datagram, so sending UDP to 51820 tells us nothing - we would
 * only ever see a timeout. Instead we time a TCP handshake to a port the same
 * host also listens on. That measures the path (routing, peering, and whether
 * the IP is blackholed by the ISP) which is exactly what we want to rank.
 *
 * This is a heuristic. Definitive proof is a completed WireGuard handshake, so
 * the connect flow still verifies the winner and falls through to the next
 * candidate if the tunnel does not come up.
 */
object EndpointProber {

    /** Ports commercial VPN endpoints commonly also serve, in order of likelihood. */
    private val PROBE_PORTS = intArrayOf(443, 80, 1443)

    const val UNREACHABLE = Int.MAX_VALUE

    data class Result(val endpoint: String, val host: String, val rttMs: Int) {
        val reachable: Boolean get() = rttMs != UNREACHABLE
    }

    /**
     * Probes every endpoint in parallel and returns them sorted fastest first.
     * Unreachable endpoints are kept at the tail so the UI can still show them.
     */
    suspend fun rank(
        endpoints: List<String>,
        timeoutMs: Int = 1_500,
        attempts: Int = 2,
    ): List<Result> = coroutineScope {
        endpoints.distinct()
            .map { endpoint -> async(Dispatchers.IO) { probe(endpoint, timeoutMs, attempts) } }
            .awaitAll()
            .sortedBy { it.rttMs }
    }

    suspend fun probe(
        endpoint: String,
        timeoutMs: Int = 1_500,
        attempts: Int = 2,
    ): Result {
        val host = endpoint.substringBeforeLast(':', endpoint)
        val best = withTimeoutOrNull((timeoutMs.toLong() + 250) * attempts) {
            var best = UNREACHABLE
            repeat(attempts) {
                val rtt = singleShot(host, timeoutMs)
                if (rtt < best) best = rtt
            }
            best
        } ?: UNREACHABLE
        return Result(endpoint = endpoint, host = host, rttMs = best)
    }

    private fun singleShot(host: String, timeoutMs: Int): Int {
        for (port in PROBE_PORTS) {
            val started = System.nanoTime()
            try {
                Socket().use { socket ->
                    socket.tcpNoDelay = true
                    socket.connect(InetSocketAddress(host, port), timeoutMs)
                }
                return ((System.nanoTime() - started) / 1_000_000).toInt().coerceAtLeast(1)
            } catch (_: Exception) {
                // try the next port
            }
        }
        return UNREACHABLE
    }
}
