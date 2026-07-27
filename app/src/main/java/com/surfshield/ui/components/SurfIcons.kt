package com.surfshield.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object SurfIcons {
    val Shield: ImageVector
        get() = ImageVector.Builder(
            name = "Shield",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.White),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 1f)
            lineTo(3f, 5f)
            verticalLineToRelative(6f)
            curveToRelative(0f, 5.55f, 3.84f, 10.74f, 9f, 12f)
            curveToRelative(5.16f, -1.26f, 9f, -6.45f, 9f, -12f)
            verticalLineTo(5f)
            lineTo(12f, 1f)
            close()
        }.build()

    val Palette: ImageVector
        get() = ImageVector.Builder(
            name = "Palette",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 2f)
            curveTo(6.49f, 2f, 2f, 6.49f, 2f, 12f)
            curveToRelative(0f, 5.51f, 4.49f, 10f, 10f, 10f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            curveToRelative(0f, -0.49f, -0.18f, -0.96f, -0.51f, -1.33f)
            curveToRelative(-0.3f, -0.34f, -0.49f, -0.8f, -0.49f, -1.32f)
            curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
            horizontalLineToRelative(2.5f)
            curveToRelative(3.59f, 0f, 6.5f, -2.91f, 6.5f, -6.5f)
            curveTo(22f, 5.75f, 17.51f, 2f, 12f, 2f)
            close()
            moveTo(6.5f, 13f)
            curveTo(5.67f, 13f, 5f, 12.33f, 5f, 11.5f)
            curveTo(5f, 10.67f, 5.67f, 10f, 6.5f, 10f)
            reflectiveCurveTo(8f, 10.67f, 8f, 11.5f)
            curveTo(8f, 12.33f, 7.33f, 13f, 6.5f, 13f)
            close()
            moveTo(9.5f, 9f)
            curveTo(8.67f, 9f, 8f, 8.33f, 8f, 7.5f)
            reflectiveCurveTo(8.67f, 6f, 9.5f, 6f)
            reflectiveCurveTo(11f, 6.67f, 11f, 7.5f)
            curveTo(11f, 8.33f, 10.33f, 9f, 9.5f, 9f)
            close()
            moveTo(14.5f, 9f)
            curveTo(13.67f, 9f, 13f, 8.33f, 13f, 7.5f)
            reflectiveCurveTo(13.67f, 6f, 14.5f, 6f)
            reflectiveCurveTo(16f, 6.67f, 16f, 7.5f)
            curveTo(16f, 8.33f, 15.33f, 9f, 14.5f, 9f)
            close()
            moveTo(17.5f, 13f)
            curveTo(16.67f, 13f, 16f, 12.33f, 16f, 11.5f)
            reflectiveCurveTo(16.67f, 10f, 17.5f, 10f)
            reflectiveCurveTo(19f, 10.67f, 19f, 11.5f)
            curveTo(19f, 12.33f, 18.33f, 13f, 17.5f, 13f)
            close()
        }.build()

    val Globe: ImageVector
        get() = ImageVector.Builder(
            name = "Globe",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            reflectiveCurveToRelative(4.48f, 10f, 10f, 10f)
            reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
            reflectiveCurveTo(17.52f, 2f, 12f, 2f)
            close()
            moveTo(19.93f, 11f)
            horizontalLineToRelative(-4f)
            curveToRelative(-0.08f, -2.69f, -0.66f, -4.89f, -1.39f, -6.06f)
            curveTo(17.2f, 6.13f, 19.24f, 8.28f, 19.93f, 11f)
            close()
            moveTo(12f, 4.03f)
            curveTo(13.12f, 5.82f, 13.84f, 8.24f, 13.97f, 11f)
            horizontalLineTo(10.03f)
            curveTo(10.16f, 8.24f, 10.88f, 5.82f, 12f, 4.03f)
            close()
            moveTo(4.07f, 13f)
            horizontalLineToRelative(4f)
            curveTo(8.16f, 15.76f, 8.88f, 18.18f, 10.03f, 19.97f)
            curveTo(8.88f, 18.18f, 8.16f, 15.76f, 8.07f, 13f)
            close()
            moveTo(4.07f, 11f)
            curveTo(4.76f, 8.28f, 6.8f, 6.13f, 9.46f, 4.94f)
            curveTo(8.73f, 6.11f, 8.15f, 8.31f, 8.07f, 11f)
            horizontalLineTo(4.07f)
            close()
            moveTo(12f, 19.97f)
            curveTo(10.88f, 18.18f, 10.16f, 15.76f, 10.03f, 13f)
            horizontalLineToRelative(3.94f)
            curveTo(13.84f, 15.76f, 13.12f, 18.18f, 12f, 19.97f)
            close()
            moveTo(15.93f, 13f)
            horizontalLineToRelative(4f)
            curveTo(19.24f, 15.72f, 17.2f, 17.87f, 14.54f, 19.06f)
            curveTo(15.27f, 17.89f, 15.85f, 15.69f, 15.93f, 13f)
            close()
        }.build()

    val Lightning: ImageVector
        get() = ImageVector.Builder(
            name = "Lightning",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(7f, 2f)
            verticalLineToRelative(11f)
            horizontalLineToRelative(3f)
            verticalLineToRelative(9f)
            lineTo(17f, 10f)
            horizontalLineToRelative(-4f)
            lineTo(16f, 2f)
            horizontalLineTo(7f)
            close()
        }.build()

    val Tunnel: ImageVector
        get() = ImageVector.Builder(
            name = "Tunnel",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 2f)
            lineTo(2f, 22f)
            horizontalLineToRelative(20f)
            lineTo(12f, 2f)
            close()
            moveTo(12f, 6f)
            lineTo(18f, 18f)
            horizontalLineTo(6f)
            lineTo(12f, 6f)
            close()
        }.build()

    val Apps: ImageVector
        get() = ImageVector.Builder(
            name = "Apps",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(4f, 8f)
            horizontalLineTo(8f)
            verticalLineTo(4f)
            horizontalLineTo(4f)
            verticalLineTo(8f)
            close()
            moveTo(10f, 20f)
            horizontalLineTo(14f)
            verticalLineTo(16f)
            horizontalLineTo(10f)
            verticalLineTo(20f)
            close()
            moveTo(4f, 20f)
            horizontalLineTo(8f)
            verticalLineTo(16f)
            horizontalLineTo(4f)
            verticalLineTo(20f)
            close()
            moveTo(4f, 14f)
            horizontalLineTo(8f)
            verticalLineTo(10f)
            horizontalLineTo(4f)
            verticalLineTo(14f)
            close()
            moveTo(10f, 14f)
            horizontalLineTo(14f)
            verticalLineTo(10f)
            horizontalLineTo(10f)
            verticalLineTo(14f)
            close()
            moveTo(16f, 4f)
            verticalLineTo(8f)
            horizontalLineTo(20f)
            verticalLineTo(4f)
            horizontalLineTo(16f)
            close()
            moveTo(10f, 8f)
            horizontalLineTo(14f)
            verticalLineTo(4f)
            horizontalLineTo(10f)
            verticalLineTo(8f)
            close()
            moveTo(16f, 14f)
            horizontalLineTo(20f)
            verticalLineTo(10f)
            horizontalLineTo(16f)
            verticalLineTo(14f)
            close()
            moveTo(16f, 20f)
            horizontalLineTo(20f)
            verticalLineTo(16f)
            horizontalLineTo(16f)
            verticalLineTo(20f)
            close()
        }.build()

    val Gear: ImageVector
        get() = Icons.Default.Settings

    val Lock: ImageVector
        get() = Icons.Outlined.Lock
}
