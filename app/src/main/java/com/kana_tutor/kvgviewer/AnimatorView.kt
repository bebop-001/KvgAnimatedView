package com.kana_tutor.kvgviewer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// a custom path for rendering an animated view of a
// character as expressed in a Kvg file.
class AnimatorView(context: Context, attrs: AttributeSet) :
        View(context, attrs)
{
    companion object {
        // used for calculating display independent values.
        private const val charWidth = 100
        private const val charHeight = 100

        // used to translate from the 100x100 pix of the
        // char as read in.
        private val scaleMatrix = Matrix()

        private val charPathMeasure = PathMeasure()

        // contains paths 'rendered' during the animation.
        private val renderedCharPath = Path()

        private lateinit var strokePaths: Array<Path>
        private lateinit var charPath: Path

        private var distance = 0f //distance moved

        private var pathLength = 0f // total length of the path.

        // x/y coordinate of current point.
        private val pos = FloatArray(2)
        private var strokePathCounter = 0// path currently being rendered.
        private var startTime = 0L
        private var startNewLine = true

        private val gridPath = Path()

        private fun Path.renderGrid() {
            reset()
            moveTo(0.5f, 0.5f)
            lineTo(0.5f, charWidth - 0.5f)
            lineTo(charHeight - 0.5f, charWidth - 0.5f)
            lineTo(charHeight - 0.5f, 0.5f)
            lineTo(0.5f, 0.5f)
            var h = 0f; var w = 0f
            while (h < charHeight) {
                moveTo(0.5f, h + 0.5f)
                lineTo(charWidth - 0.5f, h + 0.5f)
                h += (charHeight / 3f)
            }
            while (w < charWidth) {
                moveTo(w + 0.5f, 0.5f)
                lineTo(w + 0.5f, charHeight - 0.5f)
                w += (charWidth / 3f)
            }
        }
        // speed of animation is determined by number of steps.
        // More steps per frame == faster animation.
        private val speed = intArrayOf(3, 6, 9)
        private var animateSteps = 0
        @JvmStatic fun setAnimateSpeed(speedIn: Int) {
            animateSteps = speed[speedIn]
        }
        // called from KanaAnimator to select our character.
        @JvmStatic fun setRenderCharacter(renderChar: String, context: Context) {
            val kp = KvgToAndroidPaths(context, renderChar)
            strokePaths = kp.strokePaths
            charPath = kp.charPath
            startNewLine = true
            strokePathCounter = 0
            renderedCharPath.reset()
            gridPath.renderGrid()
            gridPath.transform(scaleMatrix)
            for (i in strokePaths.indices) {
                strokePaths[i].transform(scaleMatrix)
            }
            charPath.transform(scaleMatrix)
        }
    }
    // convert pix/font point for display independence
    private fun _dp(pixels: Float): Float {
        var dp = pixels * resources.displayMetrics.density
        // dp = 1 pixel if its zero to prevent divide by 0 error.
        if (dp < 1) dp = 1f
        return dp
    }

    val renderedCharPaint : Paint
    val cursorPaint : Paint
    val bgCharPaint : Paint
    val gridPaint : Paint
    // one time initialization at start of first object.
    init{
        // Various paint objects used during render.
        // the character is rendered with this brush.
        renderedCharPaint = Paint()
        with(renderedCharPaint) {
            color = ContextCompat.getColor(context, R.color.rendered_char)
            strokeWidth = _dp(10f)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        // we put a cursor on the char in this color
        // to show the movement of the brush.
        cursorPaint = Paint()
        with(cursorPaint) {
            color = ContextCompat.getColor(context, R.color.pointer_color)
            strokeWidth = _dp(1.0f)
            style = Paint.Style.FILL_AND_STROKE
            maskFilter = setMaskFilter(
                BlurMaskFilter(_dp(5f), BlurMaskFilter.Blur.NORMAL)
            )
        }
        // used to paint a blured version of the character
        // so user can see what's being painted.
        bgCharPaint = Paint()
        with(bgCharPaint) {
            color = ContextCompat.getColor(context, R.color.rendered_char_bg)
            strokeWidth = _dp(6f)
            style = Paint.Style.STROKE
            // set android:hardwareAccelerated="false" for activity
            // in AndroidManifest.xml for this to work.
            maskFilter = BlurMaskFilter(_dp(5f), BlurMaskFilter.Blur.NORMAL)
        }
        // used to paint the grid.
        gridPaint = Paint()
        with(gridPaint) {
            color = ContextCompat.getColor(context, R.color.grid_color)
            strokeWidth = _dp(3f)
            style = Paint.Style.STROKE
        }
        // Scale paths from 100 pix up to width from dimensions.
        val sHeight: Float = resources.getDimension(R.dimen.anim_view_height) / 100f
        val sWidth: Float = resources.getDimension(R.dimen.anim_view_width) / 100f
        scaleMatrix.setScale(sWidth, sHeight, 0f, 0f)
    }
    // render rate milliseconds. sets our frame rate.  This rate was chosen
    // because my oldest device (android 4.4) could handle it.
    val renderRate = 75
    //distance each animationStepDistance
    val animationStepDistance = _dp(3f)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var pause = false
        startTime = System.currentTimeMillis()
        // draw the ghost char and grid.
        canvas.drawPath(charPath, bgCharPaint)
        canvas.drawPath(gridPath, gridPaint)
        // "faster" render speed means more animateSteps
        // which means the segment drawn will be longer.
        for (i in 0 until animateSteps) {
            if (strokePathCounter < strokePaths.size) {
                if (startNewLine) {
                    distance = 0f
                    pause = true
                    // measure the length of the current path.
                    charPathMeasure.setPath(strokePaths[strokePathCounter], false)
                    pathLength = charPathMeasure.length
                }
                if (distance < pathLength + animationStepDistance) {
                    // render the end point.
                    if (distance > pathLength)
                        distance = pathLength
                    // getPosTan pins the distance along the Path and
                    // computes the position and the tangent.  This sets
                    // the position for the move-to segment.
                    charPathMeasure.getPosTan(distance, pos, null)
                    distance += animationStepDistance
                    if (startNewLine) {
                        startNewLine = false
                        renderedCharPath.moveTo(pos[0], pos[1])
                    }
                    else {
                        renderedCharPath.lineTo(pos[0], pos[1])
                        canvas.drawCircle(pos[0], pos[1], _dp(3f), cursorPaint)
                    }
                }
                else {
                    // next stroke...
                    strokePathCounter += 1
                    startNewLine = true
                }
                // Animation happens here -- this restarts render if necessary
                // and invalidate of the view is what triggers the render.
                // Using a calculated period measured from the start of the
                // render gives a steady refresh rate.
                CoroutineScope(Dispatchers.Main).launch {
                    var sleepTime = renderRate - System.currentTimeMillis() + startTime
                    if (pause)
                        sleepTime += 300
                    delay(sleepTime)
                    invalidate()
                }
                // new line.  pause render.
                if (pause) break
            }
            canvas.drawPath(renderedCharPath, renderedCharPaint)
            canvas.drawCircle(pos[0], pos[1], _dp(7f), cursorPaint)
        }
    }
}