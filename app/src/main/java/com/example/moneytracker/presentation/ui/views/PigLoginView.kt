package com.example.moneytracker.presentation.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
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

    private val stateAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 460L
        interpolator = OvershootInterpolator(1.5f)
        addUpdateListener {
            transition = it.animatedValue as Float
            invalidate()
        }
    }

    private val idleAnimator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
        duration = 1800L
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            bob = it.animatedValue as Float
            invalidate()
        }
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
        val cy = h * 0.54f + sin(bob) * 4f
        val scale = minOf(w / 180f, h / 150f)

        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(scale, scale)

        drawCoins(canvas)
        drawPig(canvas)
        drawFace(canvas)

        canvas.restore()
    }

    private fun drawCoins(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(249, 188, 80)
        canvas.drawCircle(-68f, 44f, 13f, paint)
        canvas.drawCircle(66f, 42f, 11f, paint)
        paint.color = Color.rgb(255, 224, 135)
        canvas.drawCircle(-68f, 44f, 6f, paint)
        canvas.drawCircle(66f, 42f, 5f, paint)
    }

    private fun drawPig(canvas: Canvas) {
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(255, 179, 198)
        path.reset()
        path.moveTo(-54f, -30f)
        path.lineTo(-76f, -64f)
        path.lineTo(-35f, -51f)
        path.close()
        canvas.drawPath(path, paint)

        path.reset()
        path.moveTo(54f, -30f)
        path.lineTo(76f, -64f)
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
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(45, 38, 51)
        canvas.drawCircle(-27f, -13f, 6f, paint)
        canvas.drawCircle(27f, -13f, 6f, paint)

        paint.color = Color.WHITE
        canvas.drawCircle(-29f, -15f, 2f, paint)
        canvas.drawCircle(25f, -15f, 2f, paint)

        paint.color = Color.rgb(231, 91, 130)
        paint.alpha = 90
        canvas.drawCircle(-48f, 5f, 8f, paint)
        canvas.drawCircle(48f, 5f, 8f, paint)
        paint.alpha = 255
    }

    private fun drawSunglasses(canvas: Canvas, peeking: Boolean) {        // Nếu đang peeking, nhấc kính lên cao (peekLift), nếu không thì hạ xuống 0
        // Sử dụng transition để tạo chuyển động trượt mượt mà
        val peekLift = if (peeking) 18f * transition else 18f * (1f - transition)
        val tilt = if (peeking) -8f * transition else -8f * (1f - transition)

        // Vẽ mắt bên dưới kính
        if (peeking || transition < 1f) {
            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(45, 38, 51)
            // Mắt hơi mờ đi một chút khi có kính che
            paint.alpha = (transition * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(-27f, -13f, 6f, paint)
            canvas.drawCircle(27f, -13f, 6f, paint)
            paint.alpha = 255
        }

        canvas.save()
        // Xoay kính một chút tạo cảm giác nhấc kính bằng tay/tai
        canvas.rotate(tilt, 0f, -15f)
        // Di chuyển kính lên theo trục Y
        canvas.translate(0f, -peekLift)

        // Vẽ gọng và tròng kính
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(22, 24, 28)
        glassesRect.set(-52f, -30f, -7f, 2f)
        canvas.drawRoundRect(glassesRect, 10f, 10f, paint)
        glassesRect.set(7f, -30f, 52f, 2f)
        canvas.drawRoundRect(glassesRect, 10f, 10f, paint)

        // Vẽ cầu nối và càng kính
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawLine(-7f, -15f, 7f, -15f, paint)

        // Hiệu ứng bóng đổ/phản chiếu trên mắt kính để trông sang trọng hơn
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.alpha = 30 // Rất mờ
        canvas.drawCircle(-35f, -20f, 8f, paint)
        canvas.drawCircle(25f, -20f, 8f, paint)
        paint.alpha = 255

        canvas.restore()
    }
}
