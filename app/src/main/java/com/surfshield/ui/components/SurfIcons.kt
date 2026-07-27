package com.surfshield.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * A small hand-built icon set.
 *
 * Every glyph is assembled from SVG path data at runtime, which keeps
 * material-icons-extended (a few thousand unused vectors) out of the build.
 * Paths are baked white and tinted by [androidx.compose.material3.Icon], so a
 * single definition works against every palette.
 */
object SurfIcons {

    val Shield = icon(
        stroke = listOf(
            "M12 3.2 L19 5.8 V11.6 C19 15.9 16.1 19.4 12 20.8 C7.9 19.4 5 15.9 5 11.6 V5.8 Z"
        )
    )

    val ShieldCheck = icon(
        stroke = listOf(
            "M12 3.2 L19 5.8 V11.6 C19 15.9 16.1 19.4 12 20.8 C7.9 19.4 5 15.9 5 11.6 V5.8 Z",
            "M8.6 11.8 L11.2 14.4 L15.6 9.6",
        )
    )

    val Palette = icon(
        stroke = listOf(
            "M12 3.6 C7.4 3.6 3.6 7.2 3.6 11.9 C3.6 16.6 7.4 20.4 12 20.4 " +
                "C13.5 20.4 14.3 19.5 14.3 18.6 C14.3 17.8 13.8 17.2 13.8 16.4 " +
                "C13.8 15.4 14.6 14.7 15.7 14.7 H17.4 C19.2 14.7 20.4 13.3 20.4 11.4 " +
                "C20.4 7 16.6 3.6 12 3.6 Z"
        ),
        fill = listOf(
            "M7.6 10.4 A1.15 1.15 0 1 1 7.6 12.7 A1.15 1.15 0 1 1 7.6 10.4 Z",
            "M11 7.3 A1.15 1.15 0 1 1 11 9.6 A1.15 1.15 0 1 1 11 7.3 Z",
            "M15.4 8.4 A1.15 1.15 0 1 1 15.4 10.7 A1.15 1.15 0 1 1 15.4 8.4 Z",
        ),
    )

    val Globe = icon(
        stroke = listOf(
            "M12 3.5 A8.5 8.5 0 1 1 12 20.5 A8.5 8.5 0 1 1 12 3.5 Z",
            "M3.5 12 H20.5",
            "M12 3.5 C14.4 6 15.4 9 15.4 12 C15.4 15 14.4 18 12 20.5 " +
                "C9.6 18 8.6 15 8.6 12 C8.6 9 9.6 6 12 3.5 Z",
        )
    )

    val Grid = icon(
        fill = listOf(
            "M4.4 4.4 H9.6 V9.6 H4.4 Z",
            "M14.4 4.4 H19.6 V9.6 H14.4 Z",
            "M4.4 14.4 H9.6 V19.6 H4.4 Z",
            "M14.4 14.4 H19.6 V19.6 H14.4 Z",
        )
    )

    val Bolt = icon(fill = listOf("M13.2 2.5 L5.5 13.2 H10.8 L9.8 21.5 L18.5 10.2 H13 Z"))

    val Layers = icon(
        stroke = listOf(
            "M4.2 12 L12 16.2 L19.8 12",
            "M4.2 15.6 L12 19.8 L19.8 15.6",
        ),
        fill = listOf("M12 3.4 L20.4 7.8 L12 12.2 L3.6 7.8 Z"),
    )

    val Sliders = icon(
        stroke = listOf("M4 7.6 H20", "M4 16.4 H20"),
        fill = listOf(
            "M9 6.2 A1.4 1.4 0 1 1 9 9 A1.4 1.4 0 1 1 9 6.2 Z",
            "M15 15 A1.4 1.4 0 1 1 15 17.8 A1.4 1.4 0 1 1 15 15 Z",
        ),
    )

    val Gauge = icon(
        stroke = listOf("M4.2 17.2 A8.6 8.6 0 1 1 19.8 17.2", "M12 16 L16.4 10.2"),
        fill = listOf("M12 14.6 A1.4 1.4 0 1 1 12 17.4 A1.4 1.4 0 1 1 12 14.6 Z"),
    )

    val Moon = icon(
        fill = listOf(
            "M20.4 15.2 A8.8 8.8 0 1 1 10.6 3.2 A7 7 0 0 0 20.4 15.2 Z"
        )
    )

    val Sun = icon(
        stroke = listOf(
            "M12 7.6 A4.4 4.4 0 1 1 12 16.4 A4.4 4.4 0 1 1 12 7.6 Z",
            "M12 2.6 V4.4", "M12 19.6 V21.4", "M2.6 12 H4.4", "M19.6 12 H21.4",
            "M5.7 5.7 L7 7", "M17 17 L18.3 18.3", "M18.3 5.7 L17 7", "M7 17 L5.7 18.3",
        )
    )

    val Contrast = icon(
        stroke = listOf("M12 3.5 A8.5 8.5 0 1 1 12 20.5 A8.5 8.5 0 1 1 12 3.5 Z"),
        fill = listOf("M12 3.5 A8.5 8.5 0 0 1 12 20.5 Z"),
    )

    val Leaf = icon(
        stroke = listOf("M5.4 19.8 C9 16.2 12.8 12.8 17 9.6"),
        fill = listOf("M20 4 C20 13.2 14 19.4 5.4 19.8 C5.4 10.6 11.4 4.4 20 4 Z"),
    )

    val Sparkle = icon(
        fill = listOf(
            "M11 2.6 L12.6 8.4 L18.4 10 L12.6 11.6 L11 17.4 L9.4 11.6 L3.6 10 L9.4 8.4 Z",
            "M18 15.2 L18.8 17.8 L21.4 18.6 L18.8 19.4 L18 22 L17.2 19.4 " +
                "L14.6 18.6 L17.2 17.8 Z",
        )
    )

    val Lock = icon(
        stroke = listOf(
            "M7.6 10.8 V8.6 A4.4 4.4 0 0 1 16.4 8.6 V10.8",
            "M6.2 10.8 H17.8 V19.4 H6.2 Z",
        )
    )

    val Route = icon(
        stroke = listOf(
            "M7 19.6 V11.4 A4.4 4.4 0 0 1 11.4 7 H17.6",
            "M15.2 4.6 L18 7 L15.2 9.4",
        ),
        fill = listOf("M7 17.6 A2 2 0 1 1 7 21.6 A2 2 0 1 1 7 17.6 Z"),
    )

    val Split = icon(
        stroke = listOf(
            "M4.4 12 H9",
            "M9 12 C12.4 12 13 7.4 16 7.4 H19.6",
            "M9 12 C12.4 12 13 16.6 16 16.6 H19.6",
            "M17.4 5.2 L19.8 7.4 L17.4 9.6",
            "M17.4 14.4 L19.8 16.6 L17.4 18.8",
        )
    )

    val Home = icon(
        stroke = listOf(
            "M4.2 11.2 L12 4.4 L19.8 11.2 V19.6 H14.4 V14.6 H9.6 V19.6 H4.2 Z"
        )
    )

    val Servers = icon(
        stroke = listOf("M4.4 5 H19.6 V10 H4.4 Z", "M4.4 14 H19.6 V19 H4.4 Z"),
        fill = listOf(
            "M7.4 6.4 A1.1 1.1 0 1 1 7.4 8.6 A1.1 1.1 0 1 1 7.4 6.4 Z",
            "M7.4 15.4 A1.1 1.1 0 1 1 7.4 17.6 A1.1 1.1 0 1 1 7.4 15.4 Z",
        ),
    )

    val Eye = icon(
        stroke = listOf(
            "M2.6 12 C5 8 8.4 6 12 6 C15.6 6 19 8 21.4 12 " +
                "C19 16 15.6 18 12 18 C8.4 18 5 16 2.6 12 Z",
            "M12 9.6 A2.4 2.4 0 1 1 12 14.4 A2.4 2.4 0 1 1 12 9.6 Z",
        )
    )

    val Bell = icon(
        stroke = listOf(
            "M8 10.6 A4 4 0 0 1 16 10.6 C16 14.2 17.4 16.6 18.4 17.8 H5.6 " +
                "C6.6 16.6 8 14.2 8 10.6 Z",
            "M10.2 17.8 A1.9 1.9 0 0 0 13.8 17.8",
        )
    )

    val Terminal = icon(
        stroke = listOf(
            "M3.6 5.4 H20.4 V18.6 H3.6 Z",
            "M7 10 L9.6 12.4 L7 14.8",
            "M12.4 14.8 H16.4",
        )
    )

    val Check = icon(stroke = listOf("M5.2 12.8 L9.6 17.2 L18.8 7.6"), width = 2.2f)

    val ChevronRight = icon(stroke = listOf("M9.6 5.6 L16 12 L9.6 18.4"), width = 2.1f)

    val ArrowLeft = icon(
        stroke = listOf("M20 12 H5.2", "M11 5.8 L4.8 12 L11 18.2"),
        width = 2.1f,
    )

    val Refresh = icon(
        stroke = listOf(
            "M19.4 12 A7.4 7.4 0 1 1 12 4.6 C14.8 4.6 17.2 6.2 18.4 8.4",
            "M18.8 4 V8.8 H14",
        )
    )
}

// --------------------------------------------------------------------- builder

private fun icon(
    stroke: List<String> = emptyList(),
    fill: List<String> = emptyList(),
    width: Float = 1.9f,
): ImageVector {
    val builder = ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    fill.forEach { path ->
        builder.addPath(pathData = addPathNodes(path), fill = SolidColor(Color.White))
    }
    stroke.forEach { path ->
        builder.addPath(
            pathData = addPathNodes(path),
            stroke = SolidColor(Color.White),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    return builder.build()
}
