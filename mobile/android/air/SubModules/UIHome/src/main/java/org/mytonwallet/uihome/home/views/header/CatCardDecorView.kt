package org.mytonwallet.uihome.home.views.header

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import org.mytonwallet.app_air.uicomponents.cat.CatColors
import org.mytonwallet.app_air.uicomponents.extensions.dp
import org.mytonwallet.app_air.walletbasecontext.theme.ThemeManager
import org.mytonwallet.app_air.walletcontext.globalStorage.WGlobalStorage

/**
 * Turns the wallet card into a cat.
 *
 * The [Layer.Back] layer sits behind the card: two ears stick out of its top edge and whiskers
 * out of its sides. The [Layer.Front] layer sits on top of the card: two paws hold it from below.
 * Ears twitch every now and then, and both ears and paws react when the card is touched.
 *
 * Everything is drawn outside of the view bounds, so the parents must not clip children.
 */
@SuppressLint("ViewConstructor")
class CatCardDecorView(context: Context, private val layer: Layer) : View(context) {

    enum class Layer { Back, Front }

    private companion object {
        val TWITCH_DELAY = 6_000L..11_000L
        const val TWITCH_DURATION = 600L
        const val KNEAD_DURATION = 1_600L

        // Ear design box (56 × 46) and paw design box (48 × 30), same art as in the web version
        const val EAR_DESIGN_WIDTH = 56f
        const val PAW_DESIGN_WIDTH = 48f
    }

    private val furPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val skinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val oval = RectF()

    private val earOuter = Path().apply {
        moveTo(2f, 46f)
        lineTo(20f, 5f)
        quadTo(26f, -3f, 32f, 4f)
        lineTo(54f, 46f)
        close()
    }
    private val earInner = Path().apply {
        moveTo(13f, 46f)
        lineTo(24f, 16f)
        quadTo(26.5f, 11f, 29.5f, 16f)
        lineTo(43f, 46f)
        close()
    }
    private val earTufts = Path().apply {
        moveTo(24f, 22f)
        lineTo(22f, 14f)
        moveTo(27f, 20f)
        lineTo(27f, 11f)
        moveTo(30f, 22f)
        lineTo(32f, 14f)
    }
    private val whiskers = Path().apply {
        moveTo(40f, 9f)
        quadTo(20f, 4f, 1f, 1f)
        moveTo(40f, 15f)
        quadTo(20f, 14f, 0f, 15f)
        moveTo(40f, 21f)
        quadTo(20f, 25f, 2f, 29f)
    }
    private val pawOuter = Path().apply {
        moveTo(2f, 30f)
        quadTo(0f, 4f, 24f, 3f)
        quadTo(48f, 4f, 46f, 30f)
        close()
    }
    private val pawLines = Path().apply {
        moveTo(16f, 6f)
        lineTo(16f, 14f)
        moveTo(24f, 4.5f)
        lineTo(24f, 13f)
        moveTo(32f, 6f)
        lineTo(32f, 14f)
    }

    // -1 = idle, otherwise the progress of the current twitch / knead
    private var twitchProgress = -1f
    private var twitchingEar = 0
    private var kneadProgress = -1f
    private var twitchAnimator: ValueAnimator? = null
    private var kneadAnimator: ValueAnimator? = null

    private val twitchRunnable = Runnable {
        twitch(if (Random.nextBoolean()) -1 else 1)
        scheduleTwitch()
    }

    init {
        isClickable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (layer == Layer.Back) scheduleTwitch()
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(twitchRunnable)
        twitchAnimator?.cancel()
        kneadAnimator?.cancel()
        twitchProgress = -1f
        kneadProgress = -1f
        super.onDetachedFromWindow()
    }

    /**
     * The card was touched: an ear twitches and the paws knead for a moment.
     */
    fun onCardTouched() {
        if (layer == Layer.Back) {
            twitch(if (Random.nextBoolean()) -1 else 1)
        } else {
            knead()
        }
    }

    private fun scheduleTwitch() {
        removeCallbacks(twitchRunnable)
        postDelayed(twitchRunnable, Random.nextLong(TWITCH_DELAY.first, TWITCH_DELAY.last))
    }

    private fun canAnimate(): Boolean =
        isAttachedToWindow && isShown && alpha > 0f && WGlobalStorage.getAreAnimationsActive()

    private fun twitch(side: Int) {
        if (!canAnimate() || twitchAnimator?.isRunning == true) return
        twitchingEar = side
        twitchAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = TWITCH_DURATION
            interpolator = LinearInterpolator()
            addUpdateListener {
                twitchProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun knead() {
        if (!canAnimate() || kneadAnimator?.isRunning == true) return
        kneadAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = KNEAD_DURATION
            interpolator = LinearInterpolator()
            addUpdateListener {
                kneadProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        if (layer == Layer.Back) {
            drawWhiskers(canvas)
            drawEar(canvas, -1)
            drawEar(canvas, 1)
        } else {
            drawPaw(canvas, -1)
            drawPaw(canvas, 1)
        }

        if (twitchProgress >= 1f) twitchProgress = -1f
        if (kneadProgress >= 1f) kneadProgress = -1f
    }

    private fun drawEar(canvas: Canvas, side: Int) {
        val earWidth = (width * 0.17f).coerceIn(48f.dp, 80f.dp)
        val scale = earWidth / EAR_DESIGN_WIDTH
        val earHeight = 46f * scale
        val left = if (side < 0) width * 0.09f else width * 0.91f - earWidth
        val top = -earHeight * 0.68f

        var tilt = side * 14f
        if (twitchProgress in 0f..1f && side == twitchingEar) {
            val wave = sin(twitchProgress * PI.toFloat() * 3f) * (1f - twitchProgress)
            tilt += side * 20f * wave
        }

        canvas.save()
        canvas.translate(left, top)
        canvas.rotate(tilt, earWidth / 2f, earHeight)
        canvas.scale(scale, scale)
        furPaint.color = CatColors.fur
        canvas.drawPath(earOuter, furPaint)
        skinPaint.color = CatColors.skin
        canvas.drawPath(earInner, skinPaint)
        strokePaint.color = CatColors.MUZZLE
        strokePaint.strokeWidth = 1.4f
        canvas.drawPath(earTufts, strokePaint)
        canvas.restore()
    }

    private fun drawWhiskers(canvas: Canvas) {
        val whiskerWidth = 30f.dp
        val scale = whiskerWidth / 40f
        val top = height * 0.52f
        strokePaint.color = if (ThemeManager.isDark) 0x73FFECDC else 0x8C966946.toInt()
        strokePaint.strokeWidth = 1.3f.dp / scale

        for (side in intArrayOf(-1, 1)) {
            canvas.save()
            if (side < 0) {
                canvas.translate(-whiskerWidth * 0.55f, top)
                canvas.scale(scale, scale)
            } else {
                canvas.translate(width + whiskerWidth * 0.55f, top)
                canvas.scale(-scale, scale)
            }
            canvas.drawPath(whiskers, strokePaint)
            canvas.restore()
        }
    }

    private fun drawPaw(canvas: Canvas, side: Int) {
        val pawWidth = (width * 0.12f).coerceIn(38f.dp, 56f.dp)
        val scale = pawWidth / PAW_DESIGN_WIDTH
        val pawHeight = 30f * scale
        val left = if (side < 0) width * 0.14f else width * 0.86f - pawWidth
        var top = height - pawHeight * 0.57f

        // Kneading: the paws take turns pressing into the card
        if (kneadProgress in 0f..1f) {
            val phase = kneadProgress * PI.toFloat() * 6f + if (side < 0) 0f else PI.toFloat()
            val strength = 1f - kneadProgress
            top -= (sin(phase) * 0.5f + 0.5f) * 4f.dp * strength
        }

        canvas.save()
        canvas.translate(left, top)
        canvas.rotate(side * 6f, pawWidth / 2f, pawHeight / 2f)
        canvas.scale(scale, scale)
        furPaint.color = CatColors.fur
        canvas.drawPath(pawOuter, furPaint)
        strokePaint.color = CatColors.furDark
        strokePaint.strokeWidth = 1.4f
        canvas.drawPath(pawLines, strokePaint)
        skinPaint.color = CatColors.skin
        drawOval(canvas, 24f, 22f, 7f, 5f)
        drawOval(canvas, 12.5f, 17f, 3f, 3.4f)
        drawOval(canvas, 35.5f, 17f, 3f, 3.4f)
        canvas.restore()
    }

    private fun drawOval(canvas: Canvas, cx: Float, cy: Float, rx: Float, ry: Float) {
        oval.set(cx - rx, cy - ry, cx + rx, cy + ry)
        canvas.drawOval(oval, skinPaint)
    }
}
