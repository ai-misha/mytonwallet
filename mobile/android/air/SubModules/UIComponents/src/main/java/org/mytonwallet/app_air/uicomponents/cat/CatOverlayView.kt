package org.mytonwallet.app_air.uicomponents.cat

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.text.TextPaint
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.random.Random
import org.mytonwallet.app_air.uicomponents.extensions.dp
import org.mytonwallet.app_air.walletcontext.globalStorage.WGlobalStorage

/**
 * The cat layer on top of the whole window:
 * - every tap leaves a paw print that fades away;
 * - every now and then a ginger cat peeks out from the right edge of the screen and follows
 *   your finger with its eyes. A tap makes it meow, three quick taps make it rain cats.
 *
 * The view never takes touches except the ones that land on the peeking cat.
 */
@SuppressLint("ViewConstructor")
class CatOverlayView(context: Context) : View(context) {

    private companion object {
        val FIRST_PEEK_DELAY = 8_000L..15_000L
        val NEXT_PEEK_DELAY = 35_000L..80_000L
        const val PEEK_DURATION = 5_000L
        const val HAPPY_DURATION = 1_400L
        const val BLINK_PERIOD = 4_500L
        const val BLINK_DURATION = 180L

        const val PAW_PRINT_LIFETIME = 900L
        const val MAX_PAW_PRINTS = 10

        const val TAPS_FOR_RAIN = 3
        const val TAP_SERIES_TIMEOUT = 900L
        const val RAIN_DROP_COUNT = 28
        const val HEART_LIFETIME = 1_300L

        val RAIN_EMOJIS = listOf("🐱", "😺", "😸", "😻", "🐾", "🐈", "😽", "🐟", "🧶")
        val HEART_EMOJIS = listOf("💕", "✨", "💗")
    }

    private class PawPrint(val x: Float, val y: Float, val rotation: Float, val startTime: Long)

    private class RainDrop(
        val emoji: String,
        val x: Float,
        val drift: Float,
        val spin: Float,
        val size: Float,
        val startTime: Long,
        val duration: Long
    )

    private val pawPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pawPrints = ArrayList<PawPrint>()
    private var isLeftPaw = false

    private val emojiPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val rainDrops = ArrayList<RainDrop>()
    private var heartsStartTime = 0L

    private val catHead = CatHeadDrawable()
    private val catPawPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val catPawRect = RectF()
    private val catHitRect = RectF()

    // 0 = hidden behind the screen edge, 1 = peeking
    private var peekProgress = 0f
    private var peekAnimator: ValueAnimator? = null
    private var isPeeking = false
    private var isTouchingCat = false
    private var happyUntil = 0L
    private var tapCount = 0
    private var lastTapTime = 0L

    private val peekRunnable = Runnable { showCat() }
    private val hideRunnable = Runnable { hideCat() }

    private val areAnimationsActive: Boolean
        get() = WGlobalStorage.getAreAnimationsActive()

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        CatSounds.preload(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        schedulePeek(FIRST_PEEK_DELAY)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(peekRunnable)
        removeCallbacks(hideRunnable)
        peekAnimator?.cancel()
        peekAnimator = null
        pawPrints.clear()
        rainDrops.clear()
        super.onDetachedFromWindow()
    }

    /**
     * Called by the window for every touch event before it reaches the views.
     */
    fun onWindowTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                addPawPrint(event.getX(index), event.getY(index))
                lookAt(event.getX(index), event.getY(index))
            }

            MotionEvent.ACTION_MOVE -> lookAt(event.x, event.y)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouchingCat = peekProgress > 0.5f && catHitRect.contains(event.x, event.y)
                return isTouchingCat
            }

            MotionEvent.ACTION_UP -> {
                if (isTouchingCat && catHitRect.contains(event.x, event.y)) onCatTapped()
                isTouchingCat = false
            }

            MotionEvent.ACTION_CANCEL -> isTouchingCat = false
        }
        return isTouchingCat
    }

    private fun addPawPrint(x: Float, y: Float) {
        if (!areAnimationsActive) return
        if (pawPrints.size >= MAX_PAW_PRINTS) pawPrints.removeAt(0)
        isLeftPaw = !isLeftPaw
        pawPrints.add(
            PawPrint(
                x = x + if (isLeftPaw) (-5f).dp else 5f.dp,
                y = y,
                rotation = Random.nextFloat() * 50f - 25f,
                startTime = SystemClock.uptimeMillis()
            )
        )
        postInvalidateOnAnimation()
    }

    private fun lookAt(x: Float, y: Float) {
        if (peekProgress <= 0f || catHitRect.isEmpty) return
        // The cat is rotated, so its design X axis points up and Y axis points right
        val dx = x - catHitRect.centerX()
        val dy = y - catHitRect.centerY()
        val distance = hypot(dx, dy).coerceAtLeast(1f)
        val strength = min(1f, distance / 120f.dp) * 3f
        catHead.lookX = -dy / distance * strength
        catHead.lookY = dx / distance * strength
        invalidate()
    }

    private fun schedulePeek(delayRange: LongRange) {
        removeCallbacks(peekRunnable)
        postDelayed(peekRunnable, Random.nextLong(delayRange.first, delayRange.last))
    }

    private fun showCat() {
        if (!isAttachedToWindow) return
        if (windowVisibility != VISIBLE) {
            schedulePeek(NEXT_PEEK_DELAY)
            return
        }
        isPeeking = true
        animatePeek(1f)
        removeCallbacks(hideRunnable)
        postDelayed(hideRunnable, PEEK_DURATION)
    }

    private fun hideCat() {
        if (isTouchingCat || SystemClock.uptimeMillis() < happyUntil) {
            postDelayed(hideRunnable, HAPPY_DURATION)
            return
        }
        isPeeking = false
        animatePeek(0f)
        schedulePeek(NEXT_PEEK_DELAY)
    }

    private fun animatePeek(target: Float) {
        peekAnimator?.cancel()
        if (!areAnimationsActive) {
            peekProgress = target
            invalidate()
            return
        }
        peekAnimator = ValueAnimator.ofFloat(peekProgress, target).apply {
            duration = if (target > 0f) 550L else 400L
            interpolator = if (target > 0f) {
                OvershootInterpolator(1.6f)
            } else {
                AccelerateInterpolator()
            }
            addUpdateListener {
                peekProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun onCatTapped() {
        val now = SystemClock.uptimeMillis()
        tapCount = if (now - lastTapTime < TAP_SERIES_TIMEOUT) tapCount + 1 else 1
        lastTapTime = now

        if (tapCount >= TAPS_FOR_RAIN) {
            tapCount = 0
            startRain()
            CatSounds.purr(context)
        } else {
            CatSounds.meow(context, 0.9f + Random.nextFloat() * 0.3f)
        }

        happyUntil = now + HAPPY_DURATION
        heartsStartTime = now
        removeCallbacks(hideRunnable)
        postDelayed(hideRunnable, PEEK_DURATION)
        invalidate()
    }

    private fun startRain() {
        if (!areAnimationsActive || width == 0) return
        val now = SystemClock.uptimeMillis()
        repeat(RAIN_DROP_COUNT) {
            rainDrops.add(
                RainDrop(
                    emoji = RAIN_EMOJIS.random(),
                    x = Random.nextFloat() * width,
                    drift = (Random.nextFloat() - 0.5f) * 160f.dp,
                    spin = (Random.nextFloat() - 0.5f) * 720f,
                    size = 20f.dp + Random.nextFloat() * 24f.dp,
                    startTime = now + Random.nextLong(0L, 900L),
                    duration = 2_000L + Random.nextLong(0L, 1_600L)
                )
            )
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.uptimeMillis()
        var needsNextFrame = false

        if (drawPawPrints(canvas, now)) needsNextFrame = true
        if (peekProgress > 0f && drawPeekingCat(canvas, now)) needsNextFrame = true
        if (drawRain(canvas, now)) needsNextFrame = true

        if (needsNextFrame) postInvalidateOnAnimation()
    }

    private fun drawPawPrints(canvas: Canvas, now: Long): Boolean {
        pawPrints.removeAll { now - it.startTime > PAW_PRINT_LIFETIME }
        if (pawPrints.isEmpty()) return false

        val baseColor = CatColors.pawPrint
        val baseAlpha = baseColor ushr 24
        val size = 22f.dp
        for (print in pawPrints) {
            val progress = (now - print.startTime) / PAW_PRINT_LIFETIME.toFloat()
            val scale = if (progress < 0.15f) 0.4f + progress / 0.15f * 0.7f else 1f
            val opacity = if (progress < 0.3f) 1f else 1f - (progress - 0.3f) / 0.7f
            pawPaint.color = baseColor
            pawPaint.alpha = (baseAlpha * opacity).toInt()
            CatPaw.draw(canvas, pawPaint, print.x, print.y, size * scale, print.rotation)
        }
        return true
    }

    private fun drawPeekingCat(canvas: Canvas, now: Long): Boolean {
        val headWidth = 76f.dp
        val headHeight = headWidth * 0.9f
        val anchorY = height * 0.34f
        val isHappy = now < happyUntil
        val visibleHeight = headHeight * (0.66f * peekProgress + if (isHappy) 0.18f else 0f)

        val blinkPhase = now % BLINK_PERIOD
        catHead.blink = if (blinkPhase > BLINK_PERIOD - BLINK_DURATION) {
            val t = (blinkPhase - (BLINK_PERIOD - BLINK_DURATION)) / BLINK_DURATION.toFloat()
            1f - abs(t * 2f - 1f)
        } else {
            0f
        }
        catHead.isHappy = isHappy

        // Screen-space rect of the visible part, for taps and for the eyes
        catHitRect.set(
            width - visibleHeight,
            anchorY - headWidth / 2f,
            width.toFloat(),
            anchorY + headWidth / 2f
        )

        canvas.save()
        canvas.translate(width.toFloat(), anchorY)
        canvas.rotate(-90f)
        val top = -visibleHeight
        catHead.setBounds(
            (-headWidth / 2f).toInt(),
            top.toInt(),
            (headWidth / 2f).toInt(),
            (top + headHeight).toInt()
        )
        catHead.draw(canvas)
        drawCatPaws(canvas, top + headHeight * 0.66f, headWidth)
        canvas.restore()

        if (isHappy) drawHearts(canvas, now)
        return isPeeking || peekAnimator?.isRunning == true || isHappy || peekProgress > 0f
    }

    private fun drawCatPaws(canvas: Canvas, centerY: Float, headWidth: Float) {
        val pawWidth = headWidth * 0.27f
        val pawHeight = pawWidth * 0.62f
        for (side in intArrayOf(-1, 1)) {
            val cx = side * headWidth * 0.25f
            catPawRect.set(
                cx - pawWidth / 2f,
                centerY - pawHeight / 2f,
                cx + pawWidth / 2f,
                centerY + pawHeight / 2f
            )
            catPawPaint.color = CatColors.fur
            canvas.drawRoundRect(catPawRect, pawHeight / 2f, pawHeight / 2f, catPawPaint)
            catPawPaint.color = CatColors.skin
            CatPaw.draw(canvas, catPawPaint, cx, centerY, pawHeight * 0.9f)
        }
    }

    private fun drawHearts(canvas: Canvas, now: Long) {
        val elapsed = now - heartsStartTime
        emojiPaint.textSize = 18f.dp
        HEART_EMOJIS.forEachIndexed { index, emoji ->
            val progress = ((elapsed - index * 120L) / HEART_LIFETIME.toFloat()).coerceIn(0f, 1f)
            if (progress <= 0f || progress >= 1f) return@forEachIndexed
            val x = catHitRect.left - 14f.dp - progress * 22f.dp
            val y = catHitRect.centerY() + (index - 1) * 20f.dp
            val opacity = if (progress < 0.25f) progress / 0.25f else 1f - progress
            emojiPaint.alpha = (opacity * 255).toInt()
            canvas.drawText(emoji, x, y, emojiPaint)
        }
        emojiPaint.alpha = 255
    }

    private fun drawRain(canvas: Canvas, now: Long): Boolean {
        rainDrops.removeAll { now - it.startTime > it.duration }
        if (rainDrops.isEmpty()) return false

        val fallDistance = height + 96f.dp
        for (drop in rainDrops) {
            val progress = (now - drop.startTime) / drop.duration.toFloat()
            if (progress < 0f) continue
            val eased = progress * progress * (3f - 2f * progress) * 0.5f + progress * 0.5f
            emojiPaint.textSize = drop.size
            val opacity = if (progress > 0.85f) (1f - progress) / 0.15f else 1f
            emojiPaint.alpha = (opacity * 255).toInt()
            canvas.save()
            canvas.translate(drop.x + drop.drift * eased, -48f.dp + fallDistance * eased)
            canvas.rotate(drop.spin * eased)
            canvas.drawText(drop.emoji, 0f, drop.size / 3f, emojiPaint)
            canvas.restore()
        }
        emojiPaint.alpha = 255
        return true
    }
}
