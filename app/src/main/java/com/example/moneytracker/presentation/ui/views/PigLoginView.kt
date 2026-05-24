package com.example.moneytracker.presentation.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PigLoginView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class PigState {
        NORMAL,
        COVERED,
        PEEKING
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val bodyRect = RectF()
    private val snoutRect = RectF()
    private val glassesRect = RectF()
    private var state = PigState.NORMAL
    private var transition = 0f
    private var bob = 0f
    private var tapPop = 0f

    private val stateAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 460L
        interpolator = OvershootInterpolator(1.5f)
        addUpdateListener {
            transition = it.animatedValue as Float
            invalidate()
        }
    }

    private val idleAnimator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
        duration = 2200L
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            bob = it.animatedValue as Float
            invalidate()
        }
    }

    private val tapAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 520L
        interpolator = OvershootInterpolator(2.4f)
        addUpdateListener {
            tapPop = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        isClickable = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!idleAnimator.isStarted) {
            idleAnimator.start()
        }
    }

    override fun onDetachedFromWindow() {
        idleAnimator.cancel()
        stateAnimator.cancel()
        super.onDetachedFromWindow()
    }

    override fun performClick(): Boolean {
        super.performClick()
        tapAnimator.cancel()
        tapAnimator.start()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            performClick()
        }
        return true
    }

    fun setPigState(newState: PigState) {
        if (state == newState) return
        state = newState
        stateAnimator.cancel()
        stateAnimator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h * 0.54f + sin(bob) * 4f - sin(tapPop * PI).toFloat() * 8f
        val popScale = 1f + sin(tapPop * PI).toFloat() * 0.08f
        val scale = minOf(w / 180f, h / 150f) * popScale

        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(scale, scale)

        drawCoins(canvas)
        drawPig(canvas)
        drawFace(canvas)

        canvas.restore()
    }

    private fun drawCoins(canvas: Canvas) {
        val coinPulse = sin(bob * 1.6f) * 2f
        val leftCoinY = 44f + cos(bob) * 4f
        val rightCoinY = 42f + sin(bob + 0.8f) * 4f

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(249, 188, 80)
        canvas.drawCircle(-68f, leftCoinY, 13f + coinPulse, paint)
        canvas.drawCircle(66f, rightCoinY, 11f - coinPulse * 0.4f, paint)
        paint.color = Color.rgb(255, 224, 135)
        canvas.drawCircle(-68f, leftCoinY, 6f, paint)
        canvas.drawCircle(66f, rightCoinY, 5f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.alpha = (90 + 70 * sin(bob * 2f).coerceAtLeast(0f)).toInt()
        paint.color = Color.rgb(255, 214, 102)
        val sparkleX = -48f + cos(bob * 1.5f) * 10f
        val sparkleY = 20f + sin(bob * 1.5f) * 8f
        canvas.drawLine(sparkleX - 5f, sparkleY, sparkleX + 5f, sparkleY, paint)
        canvas.drawLine(sparkleX, sparkleY - 5f, sparkleX, sparkleY + 5f, paint)
        paint.alpha = 255
    }

    private fun drawPig(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        val earWiggle = sin(bob * 1.3f) * 4f + sin(tapPop * PI).toFloat() * 7f

        paint.color = Color.rgb(255, 179, 198)
        path.reset()
        path.moveTo(-54f, -30f)
        path.lineTo(-76f - earWiggle, -64f + earWiggle)
        path.lineTo(-35f, -51f)
        path.close()
        canvas.drawPath(path, paint)

        path.reset()
        path.moveTo(54f, -30f)
        path.lineTo(76f + earWiggle, -64f + earWiggle)
        path.lineTo(35f, -51f)
        path.close()
        canvas.drawPath(path, paint)

        paint.color = Color.rgb(255, 196, 211)
        bodyRect.set(-76f, -58f, 76f, 78f)
        canvas.drawOval(bodyRect, paint)

        paint.color = Color.rgb(255, 156, 184)
        bodyRect.set(-66f, -47f, 66f, 66f)
        paint.alpha = 72
        canvas.drawOval(bodyRect, paint)
        paint.alpha = 255

        snoutRect.set(-35f, 4f, 35f, 48f)
        paint.color = Color.rgb(255, 151, 178)
        canvas.drawRoundRect(snoutRect, 22f, 22f, paint)

        paint.color = Color.rgb(121, 64, 82)
        canvas.drawCircle(-13f, 25f, 4.5f, paint)
        canvas.drawCircle(13f, 25f, 4.5f, paint)
    }

    private fun drawFace(canvas: Canvas) {
        when (state) {
            PigState.NORMAL -> drawNormalEyes(canvas)
            PigState.COVERED -> drawSunglasses(canvas, false)
            PigState.PEEKING -> drawSunglasses(canvas, true)
        }
    }

    private fun drawNormalEyes(canvas: Canvas) {
        val blink = blinkProgress()
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(45, 38, 51)
        if (blink > 0.85f) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            canvas.drawLine(-33f, -13f, -21f, -13f, paint)
            canvas.drawLine(21f, -13f, 33f, -13f, paint)
            paint.style = Paint.Style.FILL
        } else {
            val eyeScaleY = 1f - blink * 0.78f
            canvas.save()
            canvas.scale(1f, eyeScaleY, -27f, -13f)
            canvas.drawCircle(-27f, -13f, 6f, paint)
            canvas.restore()

            canvas.save()
            canvas.scale(1f, eyeScaleY, 27f, -13f)
            canvas.drawCircle(27f, -13f, 6f, paint)
            canvas.restore()
        }

        paint.color = Color.WHITE
        if (blink < 0.55f) {
            canvas.drawCircle(-29f, -15f, 2f, paint)
            canvas.drawCircle(25f, -15f, 2f, paint)
        }

        paint.color = Color.rgb(231, 91, 130)
        paint.alpha = 90
        canvas.drawCircle(-48f, 5f, 8f, paint)
        canvas.drawCircle(48f, 5f, 8f, paint)
        paint.alpha = 255
    }

    private fun blinkProgress(): Float {
        val phase = ((bob / (Math.PI * 2).toFloat()) % 1f).let { if (it < 0f) it + 1f else it }
        val blinkWindow = 0.08f
        return when {
            phase < blinkWindow -> sin((phase / blinkWindow) * PI).toFloat().coerceIn(0f, 1f)
            phase > 0.58f && phase < 0.58f + blinkWindow ->
                sin(((phase - 0.58f) / blinkWindow) * PI).toFloat().coerceIn(0f, 1f)
            else -> 0f
        }
    }

    private fun drawSunglasses(canvas: Canvas, peeking: Boolean) {        // Náº¿u Ä‘ang peeking, nháº¥c kĂ­nh lĂªn cao (peekLift), náº¿u khĂ´ng thĂ¬ háº¡ xuá»‘ng 0
        // Sá»­ dá»¥ng transition Ä‘á»ƒ táº¡o chuyá»ƒn Ä‘á»™ng trÆ°á»£t mÆ°á»£t mĂ 
        val peekLift = if (peeking) 18f * transition else 18f * (1f - transition)
        val tilt = if (peeking) -8f * transition else -8f * (1f - transition)

        // Váº½ máº¯t bĂªn dÆ°á»›i kĂ­nh
        if (peeking || transition < 1f) {
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(45, 38, 51)
            // Máº¯t hÆ¡i má» Ä‘i má»™t chĂºt khi cĂ³ kĂ­nh che
            paint.alpha = (transition * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(-27f, -13f, 6f, paint)
            canvas.drawCircle(27f, -13f, 6f, paint)
            paint.alpha = 255
        }

        canvas.save()
        // Xoay kĂ­nh má»™t chĂºt táº¡o cáº£m giĂ¡c nháº¥c kĂ­nh báº±ng tay/tai
        canvas.rotate(tilt, 0f, -15f)
        // Di chuyá»ƒn kĂ­nh lĂªn theo trá»¥c Y
        canvas.translate(0f, -peekLift)

        // Váº½ gá»ng vĂ  trĂ²ng kĂ­nh
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(22, 24, 28)
        glassesRect.set(-52f, -30f, -7f, 2f)
        canvas.drawRoundRect(glassesRect, 10f, 10f, paint)
        glassesRect.set(7f, -30f, 52f, 2f)
        canvas.drawRoundRect(glassesRect, 10f, 10f, paint)

        // Váº½ cáº§u ná»‘i vĂ  cĂ ng kĂ­nh
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawLine(-7f, -15f, 7f, -15f, paint)

        // Hiá»‡u á»©ng bĂ³ng Ä‘á»•/pháº£n chiáº¿u trĂªn máº¯t kĂ­nh Ä‘á»ƒ trĂ´ng sang trá»ng hÆ¡n
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.alpha = 30 // Ráº¥t má»
        canvas.drawCircle(-35f, -20f, 8f, paint)
        canvas.drawCircle(25f, -20f, 8f, paint)
        paint.alpha = 255

        canvas.restore()
    }
}
