package org.mytonwallet.app_air.uicomponents.cat

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import kotlin.math.min
import org.mytonwallet.app_air.uicomponents.extensions.dp

/**
 * A ginger cat head, drawn in a 100 × 90 design box that is fitted into the bounds.
 *
 * [lookX] / [lookY] move the pupils (design units, ±3), [blink] closes the open eyes (0…1),
 * [isSleeping] and [isHappy] replace them with closed eyes, a happy cat also shows its tongue.
 */
class CatHeadDrawable : Drawable() {

    companion object {
        private const val DESIGN_WIDTH = 100f
        private const val DESIGN_HEIGHT = 90f
        private const val MAX_LOOK = 3f
    }

    var lookX = 0f
        set(value) {
            field = value.coerceIn(-MAX_LOOK, MAX_LOOK)
            invalidateSelf()
        }

    var lookY = 0f
        set(value) {
            field = value.coerceIn(-MAX_LOOK, MAX_LOOK)
            invalidateSelf()
        }

    var blink = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidateSelf()
        }

    var isSleeping = false
        set(value) {
            field = value
            invalidateSelf()
        }

    var isHappy = false
        set(value) {
            field = value
            invalidateSelf()
        }

    private var drawableAlpha = 255

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val oval = RectF()

    private val leftEar = Path().apply {
        moveTo(13f, 44f)
        lineTo(19f, 5f)
        quadTo(21f, -1f, 26f, 3f)
        lineTo(47f, 25f)
        close()
    }
    private val leftInnerEar = Path().apply {
        moveTo(20f, 35f)
        lineTo(23f, 13f)
        lineTo(38f, 26f)
        close()
    }
    private val rightEar = Path().apply {
        moveTo(87f, 44f)
        lineTo(81f, 5f)
        quadTo(79f, -1f, 74f, 3f)
        lineTo(53f, 25f)
        close()
    }
    private val rightInnerEar = Path().apply {
        moveTo(80f, 35f)
        lineTo(77f, 13f)
        lineTo(62f, 26f)
        close()
    }
    private val stripes = Path().apply {
        moveTo(50f, 24f)
        lineTo(50f, 34f)
        moveTo(41f, 25f)
        lineTo(43.5f, 33f)
        moveTo(59f, 25f)
        lineTo(56.5f, 33f)
        moveTo(11f, 52f)
        lineTo(20f, 54f)
        moveTo(11f, 60f)
        lineTo(19f, 60f)
        moveTo(89f, 52f)
        lineTo(80f, 54f)
        moveTo(89f, 60f)
        lineTo(81f, 60f)
    }
    private val closedEyes = Path().apply {
        moveTo(27f, 53f)
        quadTo(34f, 59f, 41f, 53f)
        moveTo(59f, 53f)
        quadTo(66f, 59f, 73f, 53f)
    }
    private val nose = Path().apply {
        moveTo(45.5f, 63.5f)
        quadTo(50f, 61.5f, 54.5f, 63.5f)
        lineTo(50f, 68.5f)
        close()
    }
    private val mouth = Path().apply {
        moveTo(50f, 68.5f)
        quadTo(50f, 74f, 44f, 73.5f)
        moveTo(50f, 68.5f)
        quadTo(50f, 74f, 56f, 73.5f)
    }
    private val tongue = Path().apply {
        moveTo(47.5f, 73f)
        quadTo(50f, 79f, 52.5f, 73f)
        close()
    }
    private val whiskers = Path().apply {
        moveTo(32f, 67f)
        lineTo(4f, 61f)
        moveTo(32f, 70.5f)
        lineTo(3f, 72f)
        moveTo(33f, 74f)
        lineTo(7f, 82f)
        moveTo(68f, 67f)
        lineTo(96f, 61f)
        moveTo(68f, 70.5f)
        lineTo(97f, 72f)
        moveTo(67f, 74f)
        lineTo(93f, 82f)
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        if (bounds.isEmpty) return

        val scale = min(bounds.width() / DESIGN_WIDTH, bounds.height() / DESIGN_HEIGHT)
        val left = bounds.left + (bounds.width() - DESIGN_WIDTH * scale) / 2f
        val top = bounds.top + (bounds.height() - DESIGN_HEIGHT * scale) / 2f

        canvas.save()
        canvas.translate(left, top)
        canvas.scale(scale, scale)

        fill(canvas, leftEar, CatColors.fur)
        fill(canvas, leftInnerEar, CatColors.skin)
        fill(canvas, rightEar, CatColors.fur)
        fill(canvas, rightInnerEar, CatColors.skin)

        fillOval(canvas, 50f, 55f, 40f, 33f, CatColors.fur)
        fillOval(canvas, 50f, 70f, 17f, 12f, CatColors.MUZZLE, 0.9f)
        stroke(canvas, stripes, CatColors.stripe, 3.2f)
        fillOval(canvas, 24f, 67f, 6f, 3.5f, CatColors.nose, 0.35f)
        fillOval(canvas, 76f, 67f, 6f, 3.5f, CatColors.nose, 0.35f)

        if (isSleeping || isHappy) {
            stroke(canvas, closedEyes, CatColors.eye, 2.6f)
        } else {
            drawOpenEyes(canvas)
        }

        fill(canvas, nose, CatColors.nose)
        stroke(canvas, mouth, CatColors.eye, 1.8f)
        if (isHappy) {
            fill(canvas, tongue, CatColors.nose)
        }
        stroke(canvas, whiskers, CatColors.WHISKER, 1.2f)

        canvas.restore()
    }

    private fun drawOpenEyes(canvas: Canvas) {
        canvas.save()
        canvas.scale(1f, 1f - blink * 0.9f, 50f, 52f)
        for (eyeX in floatArrayOf(34f, 66f)) {
            fillOval(canvas, eyeX, 52f, 7.5f, 8.5f, CatColors.iris)
            fillOval(canvas, eyeX + lookX, 52f + lookY, 2.6f, 6.4f, CatColors.eye)
            fillOval(canvas, eyeX + 2.5f + lookX, 48.5f + lookY, 1.8f, 1.8f, 0xFFFFFFFF.toInt())
        }
        canvas.restore()
    }

    private fun fill(canvas: Canvas, path: Path, color: Int) {
        fillPaint.color = color
        fillPaint.alpha = drawableAlpha
        canvas.drawPath(path, fillPaint)
    }

    private fun fillOval(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        color: Int,
        opacity: Float = 1f
    ) {
        fillPaint.color = color
        fillPaint.alpha = (drawableAlpha * opacity * (color ushr 24) / 255f).toInt()
        oval.set(cx - rx, cy - ry, cx + rx, cy + ry)
        canvas.drawOval(oval, fillPaint)
    }

    private fun stroke(canvas: Canvas, path: Path, color: Int, width: Float) {
        strokePaint.color = color
        strokePaint.alpha = (drawableAlpha * (color ushr 24) / 255f).toInt()
        strokePaint.strokeWidth = width
        canvas.drawPath(path, strokePaint)
    }

    override fun setAlpha(alpha: Int) {
        drawableAlpha = alpha
        invalidateSelf()
    }

    override fun getAlpha(): Int = drawableAlpha

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
        strokePaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = 50.dp

    override fun getIntrinsicHeight(): Int = 45.dp
}
