package org.mytonwallet.app_air.uicomponents.cat

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import kotlin.math.min
import org.mytonwallet.app_air.uicomponents.extensions.dp

/**
 * Switch thumb shaped as a cat face with little ears on top:
 * asleep while the switch is off, wide awake when it is on.
 */
class CatSwitchThumbDrawable(
    private val backgroundColor: Int,
    private val strokeColor: Int,
    private val isAwake: Boolean
) : Drawable() {

    private var drawableAlpha = 255

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val earPath = Path()
    private val facePath = Path()

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        if (bounds.isEmpty) return

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val stroke = 2f.dp
        val radius = min(bounds.width(), bounds.height()) / 2f - stroke / 2f
        val unit = radius / 9f

        // Ears ride on top of the head, their bases hidden behind its outline
        val earWidth = 7f * unit
        val earHeight = 6f * unit
        CatEar.outer(earPath, earWidth, earHeight)
        setColor(fillPaint, strokeColor)
        for (side in intArrayOf(-1, 1)) {
            canvas.save()
            canvas.translate(
                cx + side * 4.6f * unit - earWidth / 2f,
                cy - radius - earHeight * 0.55f
            )
            canvas.rotate(side * 18f, earWidth / 2f, earHeight)
            canvas.drawPath(earPath, fillPaint)
            canvas.restore()
        }

        setColor(fillPaint, backgroundColor)
        canvas.drawCircle(cx, cy, radius, fillPaint)
        setColor(strokePaint, strokeColor)
        strokePaint.strokeWidth = stroke
        canvas.drawCircle(cx, cy, radius, strokePaint)

        strokePaint.strokeWidth = 1.1f * unit
        val eyeY = cy - 0.8f * unit
        if (isAwake) {
            setColor(fillPaint, strokeColor)
            canvas.drawCircle(cx - 3.2f * unit, eyeY, 1.15f * unit, fillPaint)
            canvas.drawCircle(cx + 3.2f * unit, eyeY, 1.15f * unit, fillPaint)
        } else {
            facePath.rewind()
            facePath.moveTo(cx - 4.6f * unit, eyeY)
            facePath.quadTo(cx - 3.2f * unit, eyeY + 1.6f * unit, cx - 1.8f * unit, eyeY)
            facePath.moveTo(cx + 1.8f * unit, eyeY)
            facePath.quadTo(cx + 3.2f * unit, eyeY + 1.6f * unit, cx + 4.6f * unit, eyeY)
            canvas.drawPath(facePath, strokePaint)
        }

        facePath.rewind()
        facePath.moveTo(cx - 1.1f * unit, cy + 1.6f * unit)
        facePath.lineTo(cx + 1.1f * unit, cy + 1.6f * unit)
        facePath.lineTo(cx, cy + 2.7f * unit)
        facePath.close()
        setColor(fillPaint, CatColors.nose)
        canvas.drawPath(facePath, fillPaint)
    }

    private fun setColor(paint: Paint, color: Int) {
        paint.color = color
        paint.alpha = (color ushr 24) * drawableAlpha / 255
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

    override fun getIntrinsicWidth(): Int = 20.dp

    override fun getIntrinsicHeight(): Int = 20.dp
}
