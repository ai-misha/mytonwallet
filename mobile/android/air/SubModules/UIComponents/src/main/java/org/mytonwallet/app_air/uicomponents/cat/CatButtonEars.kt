package org.mytonwallet.app_air.uicomponents.cat

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.max
import org.mytonwallet.app_air.uicomponents.AnimationConstants
import org.mytonwallet.app_air.uicomponents.extensions.dp

/**
 * Cat ears sticking out of the top edge of a pill-shaped button.
 *
 * The ears take the button's background color, so they always match it, and a button without
 * a background stays earless. They go flat for a moment while the button is pressed,
 * like a startled kitty, and spring back on release.
 */
class CatButtonEars(private val view: View) {

    private companion object {
        const val BASE_TILT = 14f
        const val FLAT_TILT = 40f
        const val VISIBLE_PART = 0.62f
        const val MIN_BUTTON_WIDTH = 72
    }

    private val outerPath = Path()
    private val innerPath = Path()
    private val furPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = CatColors.INNER_EAR_ON_TINT
    }

    private var earWidth = 0f
    private var earHeight = 0f

    // 0 = upright, 1 = flattened
    private var flatness = 0f
    private var isPressed = false
    private var animator: ValueAnimator? = null

    /**
     * Ears are drawn above the button, outside of its bounds, so its parent must not clip it.
     */
    fun onAttachedToWindow() {
        (view.parent as? ViewGroup)?.clipChildren = false
    }

    fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        flatness = 0f
        isPressed = false
    }

    fun setPressed(pressed: Boolean) {
        if (isPressed == pressed) return
        isPressed = pressed
        animator?.cancel()
        animator = ValueAnimator.ofFloat(flatness, if (pressed) 1f else 0f).apply {
            duration = if (pressed) {
                AnimationConstants.SUPER_QUICK_ANIMATION
            } else {
                AnimationConstants.QUICK_ANIMATION
            }
            interpolator = if (pressed) DecelerateInterpolator() else OvershootInterpolator(3f)
            addUpdateListener {
                flatness = it.animatedValue as Float
                view.invalidate()
            }
            start()
        }
    }

    fun draw(canvas: Canvas, backgroundColor: Int) {
        if (Color.alpha(backgroundColor) == 0) return

        val width = view.width.toFloat()
        val height = view.height.toFloat()
        if (width < MIN_BUTTON_WIDTH.dp || height <= 0f) return

        val scale = (height / 50f.dp).coerceIn(0.6f, 1.2f)
        val newEarWidth = 21f.dp * scale
        val newEarHeight = 18f.dp * scale
        if (newEarWidth != earWidth || newEarHeight != earHeight) {
            earWidth = newEarWidth
            earHeight = newEarHeight
            CatEar.outer(outerPath, earWidth, earHeight)
            CatEar.inner(innerPath, earWidth, earHeight)
        }

        // Keep the ears on the flat part of the pill, away from the rounded ends
        val inset = max(height * 0.42f, width * 0.08f)
        val tilt = BASE_TILT + (FLAT_TILT - BASE_TILT) * flatness
        val squash = 1f - 0.3f * flatness
        furPaint.color = backgroundColor
        innerPaint.alpha = Color.alpha(backgroundColor) * 191 / 255

        drawEar(canvas, inset, -tilt, squash)
        drawEar(canvas, width - inset - earWidth, tilt, squash)
    }

    private fun drawEar(canvas: Canvas, left: Float, tilt: Float, squash: Float) {
        val top = -earHeight * VISIBLE_PART
        canvas.save()
        canvas.translate(left, top)
        canvas.rotate(tilt, earWidth / 2f, earHeight)
        canvas.scale(1f, squash, earWidth / 2f, earHeight)
        canvas.drawPath(outerPath, furPaint)
        // The inner ear ends at the top edge of the button
        canvas.clipRect(0f, -earHeight, earWidth, earHeight * VISIBLE_PART)
        canvas.drawPath(innerPath, innerPaint)
        canvas.restore()
    }
}
