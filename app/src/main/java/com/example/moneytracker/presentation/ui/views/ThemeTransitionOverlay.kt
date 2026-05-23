package com.example.moneytracker.presentation.ui.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import kotlin.math.hypot
import kotlin.math.cos
import kotlin.math.sin

class ThemeTransitionOverlay(
    context: Context,
    private val originX: Float,
    private val originY: Float,
    private val darkMode: Boolean,
    private val startsCovered: Boolean = false
) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val clipPath = Path()
    private val bodyRect = RectF()
    private val snoutRect = RectF()
    private var progress = if (startsCovered) 1f else 0f
    private var mascotPop = if (startsCovered) 1f else 0f
    private var sparklePhase = if (startsCovered) 1f else 0f
    private var callbackFired = false

    private val overlayColor = if (darkMode) Color.rgb(11, 16, 32) else Color.rgb(250, 248, 255)
    private val accentColor = if (darkMode) Color.rgb(52, 211, 153) else Color.rgb(0, 109, 54)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val maxRadius = hypot(
            maxOf(originX, width - originX),
            maxOf(originY, height - originY)
        )
        paint.style = Paint.Style.FILL
        paint.color = overlayColor
        canvas.drawCircle(originX, originY, maxRadius * progress, paint)

        drawSkyScene(canvas, maxRadius)
        drawCoinBurst(canvas, maxRadius)
        drawSparkles(canvas, maxRadius)
        drawMascot(canvas)
    }

    fun playCover(onCovered: () -> Unit) {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 860L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                mascotPop = popValue(progress)
                sparklePhase = progress
                if (!callbackFired && progress >= 0.96f) {
                    callbackFired = true
                    onCovered()
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!callbackFired) {
                        callbackFired = true
                        onCovered()
                    }
                }
            })
            start()
        }
    }

    fun playReveal(onDone: () -> Unit = {}) {
        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 520L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                mascotPop = progress
                sparklePhase = progress
                alpha = (0.35f + progress * 0.65f).coerceIn(0f, 1f)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    (parent as? ViewGroup)?.removeView(this@ThemeTransitionOverlay)
                    onDone()
                }
            })
            start()
        }
    }

    private fun popValue(value: Float): Float {
        val overshoot = sin(value * Math.PI).toFloat() * 0.18f
        return (value + overshoot).coerceIn(0f, 1.12f)
    }

    private fun drawSparkles(canvas: Canvas, maxRadius: Float) {
        val visible = sin(progress * Math.PI).toFloat().coerceAtLeast(0f)
        if (visible <= 0.02f) return

        paint.style = Paint.Style.FILL
        paint.color = if (darkMode) Color.rgb(250, 204, 21) else Color.rgb(52, 211, 153)
        paint.alpha = (visible * 170).toInt().coerceIn(0, 170)

        for (i in 0 until 10) {
            val angle = i * 36f + sparklePhase * 80f
            val radius = maxRadius * (0.12f + i * 0.035f) * progress
            val x = originX + cos(Math.toRadians(angle.toDouble())).toFloat() * radius
            val y = originY + sin(Math.toRadians(angle.toDouble())).toFloat() * radius
            canvas.save()
            canvas.rotate(angle, x, y)
            canvas.drawRoundRect(x - 2.5f, y - 10f, x + 2.5f, y + 10f, 3f, 3f, paint)
            canvas.drawRoundRect(x - 10f, y - 2.5f, x + 10f, y + 2.5f, 3f, 3f, paint)
            canvas.restore()
        }
        paint.alpha = 255
    }

    private fun drawCoinBurst(canvas: Canvas, maxRadius: Float) {
        val visible = sin(progress * Math.PI).toFloat().coerceAtLeast(0f)
        if (visible <= 0.02f) return

        for (i in 0 until 8) {
            val angle = i * 45f - 12f + progress * 100f
            val distance = maxRadius * (0.08f + 0.16f * progress) + i * 4f
            val x = originX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val y = originY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance
            val coinScale = 0.72f + visible * 0.42f

            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(angle + progress * 180f)
            canvas.scale(coinScale, coinScale)

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(249, 188, 80)
            paint.alpha = (visible * 220).toInt().coerceIn(0, 220)
            canvas.drawCircle(0f, 0f, 12f, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.2f
            paint.color = Color.rgb(255, 240, 170)
            paint.alpha = (visible * 210).toInt().coerceIn(0, 210)
            canvas.drawCircle(0f, 0f, 7f, paint)
            canvas.drawLine(0f, -5f, 0f, 5f, paint)

            canvas.restore()
        }
        paint.alpha = 255
    }

    private fun drawSkyScene(canvas: Canvas, maxRadius: Float) {
        if (progress <= 0.02f) return

        clipPath.reset()
        clipPath.addCircle(originX, originY, maxRadius * progress, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(clipPath)
        if (darkMode) {
            drawNightSky(canvas)
        } else {
            drawDaySky(canvas)
        }
        canvas.restore()
    }

    private fun drawNightSky(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        for (i in 0 until 28) {
            val x = ((i * 83 + 37) % width.coerceAtLeast(1)).toFloat()
            val y = ((i * 47 + 29) % height.coerceAtLeast(1)).toFloat()
            val pulse = 0.55f + 0.45f * sin(sparklePhase * Math.PI * 2f + i).toFloat()
            paint.color = if (i % 5 == 0) Color.rgb(250, 204, 21) else Color.WHITE
            paint.alpha = (90 + pulse * 130).toInt().coerceIn(70, 220)
            canvas.drawCircle(x, y, if (i % 4 == 0) 3.2f else 2f, paint)
        }
        paint.alpha = 255
    }

    private fun drawDaySky(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.alpha = 210
        drawCloud(canvas, width * 0.2f, height * 0.16f, 1f)
        paint.alpha = 175
        drawCloud(canvas, width * 0.64f, height * 0.32f, 0.82f)
        paint.alpha = 255
    }

    private fun drawCloud(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        canvas.drawCircle(cx - 32f * scale, cy + 8f * scale, 20f * scale, paint)
        canvas.drawCircle(cx - 8f * scale, cy - 6f * scale, 26f * scale, paint)
        canvas.drawCircle(cx + 24f * scale, cy + 5f * scale, 22f * scale, paint)
        canvas.drawRoundRect(
            cx - 54f * scale,
            cy + 6f * scale,
            cx + 52f * scale,
            cy + 30f * scale,
            18f * scale,
            18f * scale,
            paint
        )
    }

    private fun drawMascot(canvas: Canvas) {
        val size = minOf(width, height).coerceAtLeast(1) * 0.23f
        val scale = (size / 180f) * mascotPop.coerceAtLeast(0.01f)
        val bounce = sin(progress * Math.PI * 3f).toFloat() * 12f * (1f - progress)
        val cx = originX
        val cy = (originY - 88f + bounce).coerceIn(size * 0.65f, height - size * 0.7f)

        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(scale, scale)

        drawCoins(canvas)
        drawPig(canvas)
        drawThemeFace(canvas)

        canvas.restore()
    }

    private fun drawCoins(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(249, 188, 80)
        canvas.drawCircle(-76f, 42f, 13f, paint)
        canvas.drawCircle(72f, 38f, 11f, paint)
        paint.color = Color.rgb(255, 224, 135)
        canvas.drawCircle(-76f, 42f, 6f, paint)
        canvas.drawCircle(72f, 38f, 5f, paint)
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

        paint.color = Color.rgb(255, 151, 178)
        snoutRect.set(-35f, 4f, 35f, 48f)
        canvas.drawRoundRect(snoutRect, 22f, 22f, paint)

        paint.color = Color.rgb(121, 64, 82)
        canvas.drawCircle(-13f, 25f, 4.5f, paint)
        canvas.drawCircle(13f, 25f, 4.5f, paint)
    }

    private fun drawThemeFace(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(45, 38, 51)
        canvas.drawCircle(-27f, -13f, 6f, paint)
        canvas.drawCircle(27f, -13f, 6f, paint)

        paint.color = accentColor
        if (darkMode) {
            path.reset()
            path.addCircle(-2f, -67f, 16f, Path.Direction.CW)
            path.addCircle(7f, -73f, 16f, Path.Direction.CCW)
            canvas.drawPath(path, paint)
        } else {
            canvas.drawCircle(0f, -67f, 13f, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            for (i in 0 until 8) {
                canvas.save()
                canvas.rotate(i * 45f, 0f, -67f)
                canvas.drawLine(0f, -90f, 0f, -101f, paint)
                canvas.restore()
            }
        }

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(231, 91, 130)
        paint.alpha = 88
        canvas.drawCircle(-48f, 5f, 8f, paint)
        canvas.drawCircle(48f, 5f, 8f, paint)
        paint.alpha = 255
    }
}
