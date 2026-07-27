# SurfShield

An Android WireGuard client built for censored networks. One transport only:
WireGuard, carrying AmneziaWG obfuscation parameters. No protocol picker, no
half-implemented alternatives.

## Design decisions

### Endpoints are literal IP addresses, never hostnames

Every profile connects to `IP:port` taken straight from the bundled config. A
hostname would be handed to the system resolver at connect time, which on a
filtered network is both the slowest step of the handshake and the easiest one
to poison. The domain is kept in the profile for reference only and is never
resolved.

### AmneziaWG parameters, split by compatibility

This is the detail that breaks most attempts to bolt AmneziaWG onto a commercial
VPN provider. The parameters are not interchangeable:

| Parameter | Effect | Works against a stock WireGuard server? |
|---|---|---|
| `Jc`, `Jmin`, `Jmax` | Sends junk datagrams around an otherwise untouched handshake | **Yes.** The server cannot parse them and drops them, then proceeds normally. |
| `S1`, `S2` | Prepends junk *inside* the handshake init/response | **No.** |
| `H1`–`H4` | Replaces the four WireGuard message-type headers | **No.** |

So `Jc/Jmin/Jmax` can be used freely against Surfshark. `S1/S2/H1–H4` require
AmneziaWG on the server, and sending them to a stock peer produces a tunnel that
builds an interface and then never completes a handshake — a silent failure that
looks identical to "connected but no internet".

`AwgConfigBuilder` enforces this: parameters are coerced to vanilla-safe values
unless the target profile is explicitly marked as an AmneziaWG server. Only the
personal server in the bundled list is marked as such.

### Auto-tuning per network

There is no single obfuscation setting that is right for every ISP, so the client
searches instead of guessing:

1. `EndpointProber` measures every candidate IP and ranks them by RTT.
2. For each candidate, the obfuscation ladder is walked cheapest-first:
   plain → light junk → balanced → aggressive → full AmneziaWG.
3. A handshake must actually complete. Received bytes are the success signal;
   the interface coming up is not.
4. The winning profile is stored against a coarse network fingerprint (Wi-Fi vs
   cellular plus SIM operator), so the app converges on the right settings for
   each user's own connection and reuses them next time.

MTU is part of each profile, descending from 1420 to 1280, because on a tunnelled
link MTU problems present as large pages stalling while small requests succeed.

## Settings

- **Connection** — Smart Connect, probe on launch, reconnect on network change,
  connect on boot, connect on untrusted Wi-Fi, kill switch
- **Obfuscation** — auto / plain / light / balanced / aggressive / full AWG /
  custom, with manual `Jc`, `Jmin`, `Jmax`, `S1`, `S2` sliders and a warning when
  the selection requires an AmneziaWG server; plus a reset for learned profiles
- **Routing** — split tunnelling (off / exclude selected apps / include only
  selected apps), bypass local network, route Iranian address space directly,
  IPv6 toggle
- **DNS** — from server config, Cloudflare, Google, Quad9, AdGuard, Shecan, or
  custom resolvers
- **Tuning** — MTU override, persistent keepalive override
- **Appearance** — dark / AMOLED theme, language, animations, haptics,
  speed in notification
- **Advanced** — verbose logging, reset everything

## Building

AmneziaWG for Android publishes no Maven artifact. Its `tunnel` module compiles
`libwg-go.so`, `libwg.so` and `libwg-quick.so` through CMake and takes its
package name from a Gradle property, so it has to be vendored:

```bash
git submodule add https://github.com/amnezia-vpn/amneziawg-android \
    third_party/amneziawg-android
git submodule update --init --recursive
./gradlew :app:assembleDebug
```

Building the native libraries additionally requires the Android NDK and a Go
toolchain. `settings.gradle.kts` includes the module conditionally, so a clone
without submodules still configures and tells you what is missing rather than
failing on an unresolvable dependency.

## Outstanding security work

These are tracked deliberately, not overlooked:

1. **The bundled WireGuard private keys are committed to a public repository.**
   All Surfshark entries share one key. Anyone can clone this repo and use them,
   and the provider can revoke them at any time. They must be regenerated, and
   purging them requires rewriting history — deleting the files is not enough.
2. **`app/src/main/assets` still contains 29 leftover `.conf` files.** They are
   redundant now that `locations.json` is the single source of truth, and each
   one contains a private key. They should be deleted.
3. **The release build is signed with the debug key.** A real keystore is needed
   before distributing anything.
4. **Which IP actually works from inside Iran cannot be determined from here.**
   The prober measures reachability from the device it runs on, so the candidate
   ranking is only meaningful once the app runs on an Iranian connection.
