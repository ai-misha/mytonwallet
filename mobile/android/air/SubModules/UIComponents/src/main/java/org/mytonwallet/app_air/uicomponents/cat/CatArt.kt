package org.mytonwallet.app_air.uicomponents.cat

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import org.mytonwallet.app_air.walletbasecontext.theme.ThemeManager

/**
 * Colors of every cat in the app: a ginger tabby that gets a bit darker at night.
 */
object CatColors {
    val fur: Int
        get() = if (ThemeManager.isDark) 0xFFE8893E.toInt() else 0xFFF59A4B.toInt()

    val furDark: Int
        get() = if (ThemeManager.isDark) 0xFFB9611F.toInt() else 0xFFD9772A.toInt()

    val stripe: Int
        get() = if (ThemeManager.isDark) 0xFFC96F28.toInt() else 0xFFE07F30.toInt()

    val skin: Int
        get() = if (ThemeManager.isDark) 0xFFE89AA6.toInt() else 0xFFFFB8C3.toInt()

    val nose: Int
        get() = if (ThemeManager.isDark) 0xFFF07F94.toInt() else 0xFFFF8FA3.toInt()

    val eye: Int
        get() = if (ThemeManager.isDark) 0xFF1A120C.toInt() else 0xFF3A2A1E.toInt()

    // Cats' eyes glow in the dark
    val iris: Int
        get() = if (ThemeManager.isDark) 0xFFC9F25A.toInt() else 0xFFF2B33D.toInt()

    val pawPrint: Int
        get() = if (ThemeManager.isDark) 0x66FFA25C else 0x73F28A3A

    const val MUZZLE = 0xFFFFF3E6.toInt()
    const val WHISKER = 0x8C463223.toInt()

    // Semi-transparent pink, so the inner ear works on any button color
    const val INNER_EAR_ON_TINT = 0xBFFFB8C3.toInt()
}

/**
 * The shape of a cat ear: a rounded triangle standing on the bottom edge of its box.
 */
object CatEar {
    fun outer(path: Path, width: Float, height: Float) {
        path.rewind()
        path.moveTo(0f, height)
        path.lineTo(width * 0.37f, height * 0.122f)
        path.quadTo(width * 0.5f, -height * 0.1f, width * 0.63f, height * 0.122f)
        path.lineTo(width, height)
        path.close()
    }

    fun inner(path: Path, width: Float, height: Float) {
        path.rewind()
        path.moveTo(width * 0.25f, height)
        path.lineTo(width * 0.445f, height * 0.41f)
        path.quadTo(width * 0.5f, height * 0.3f, width * 0.555f, height * 0.41f)
        path.lineTo(width * 0.75f, height)
        path.close()
    }
}

/**
 * A paw print: one main pad and four toe beans, the same shape as in the web version of the theme.
 */
object CatPaw {
    private const val UNIT = 24f

    private val oval = RectF()

    fun draw(
        canvas: Canvas,
        paint: Paint,
        centerX: Float,
        centerY: Float,
        size: Float,
        rotation: Float = 0f
    ) {
        val scale = size / UNIT
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.rotate(rotation)
        canvas.scale(scale, scale)
        canvas.translate(-UNIT / 2f, -UNIT / 2f)
        drawOval(canvas, paint, 12f, 16.2f, 5.4f, 4.5f, 0f)
        drawOval(canvas, paint, 4.9f, 10.6f, 2.2f, 2.9f, -22f)
        drawOval(canvas, paint, 9.3f, 5.9f, 2.35f, 3.05f, -6f)
        drawOval(canvas, paint, 14.7f, 5.9f, 2.35f, 3.05f, 6f)
        drawOval(canvas, paint, 19.1f, 10.6f, 2.2f, 2.9f, 22f)
        canvas.restore()
    }

    private fun drawOval(
        canvas: Canvas,
        paint: Paint,
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        rotation: Float
    ) {
        canvas.save()
        canvas.rotate(rotation, cx, cy)
        oval.set(cx - rx, cy - ry, cx + rx, cy + ry)
        canvas.drawOval(oval, paint)
        canvas.restore()
    }
}
