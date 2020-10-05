/*
 * Copyright 2020 Steven Smith kana-tutor.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kana_tutor.kvgviewer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat

// a custom path for rendering an animated view of a
// character as expressed in a Kvg file.
// See https://c05mic.com/2012/03/23/animating-a-bitmap-using-path-and-pathmeasure-android/
// for the concept behind the animation.
class AnimatorView(context: Context, attrs: AttributeSet) :
        View(context, attrs)
{
    companion object {
        // used for calculating display independent values.
        private var charWidth = 0f
        private var charHeight = 0f

        // used to scale char to view size.
        private val scaleMatrix = Matrix()

        private val charPathMeasure = PathMeasure()

        // contains paths 'rendered' during the animation.
        private val renderedCharPath = Path()

        private var strokePaths: Array<Path>? = null
        private var strokeIdText: Array<PositionedTextInfo>? = null
        private var charPath = Path()

        private var distance = 0f //distance moved

        private var pathLength = 0f // total length of the path.

        // x/y coordinate of current point.
        private val pos = FloatArray(2)
        private var strokePathCounter = 0// path currently being rendered.
        private var startTime = 0L
        private var startNewLine = true

        private val gridPath = Path()

        fun PositionedTextInfo.putText(matrix:Matrix) : PositionedTextInfo {
            val src = floatArrayOf(this.x, this.y)
            val dst = floatArrayOf(0f,0f)
            matrix.mapPoints(dst, src)
            val rv = PositionedTextInfo(dst[0], dst[1], text)
            // Log.d("putText", "$this -> $rv")
            return rv
        }
        fun Canvas.renderText(text : PositionedTextInfo, paint:Paint) {
            drawText(text.text, text.x, text.y, paint)
        }
        private lateinit var gridPaint : Paint
        private fun Canvas.renderGrid() {
            val (left, top, right, bottom) =
                arrayOf(4f, 4f, viewWidth - 4f, viewHeight - 4f)
            drawRect(RectF(left, top, right, bottom), gridPaint)
            val h = viewHeight / 3
            drawLine(left, h, right, h, gridPaint)
            drawLine(left, 2 * h, right, 2 * h, gridPaint)
            val w = viewWidth / 3
            drawLine(w, top, w, bottom, gridPaint)
            drawLine(2 * w, top, 2 * w, bottom, gridPaint)

            Log.d("renderGrid", "$width, $height")
        }
        // speed of animation is determined by number of steps.
        // More steps per frame == faster animation.
        private val speed = intArrayOf(3, 6, 9)
        private var animateSteps = 0
        fun setAnimateSpeed(speedIn: Int) {
            animateSteps = speed[speedIn]
        }
        var viewWidth = 0f
        var viewHeight = 0f
        val positionedText = mutableListOf<PositionedTextInfo>()
        // called from KanaAnimator to select our character.
        fun setRenderCharacter(kp: KvgToAndroidPaths) {
            if (kp.strokePaths == null || kp.strokeIdText == null) {
                Log.d("setRenderChar", "stroke paths or text null")
                return
            }
            // for normalize.
            charWidth = kp.width
            charHeight = kp.height
            strokePaths = kp.strokePaths!!
            strokeIdText = kp.strokeIdText!!

            scaleMatrix.setScale(
                viewWidth/ charWidth, viewHeight/ charHeight, 0f, 0f)
            startNewLine = true
            strokePathCounter = 0
            renderedCharPath.reset()
            for (i in strokePaths!!.indices) {
                strokePaths!![i].transform(scaleMatrix)
            }
            strokePaths!!.map { charPath.addPath(it) }
            strokeIdText!!.map{
                positionedText.add(it.putText(scaleMatrix))
            }
        }
    }
    // convert pix/font point for display independence
    private fun _dp(pixels: Float): Float {
        var dp = pixels * resources.displayMetrics.density
        // dp = 1 pixel if its zero to prevent divide by 0 error.
        if (dp < 1) dp = 1f
        return dp
    }

    private val renderedCharPaint : Paint
    private val cursorPaint : Paint
    private val bgCharPaint : Paint
    private val textPaint : Paint
    private val textBgPaint : Paint
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
        textPaint = Paint()
        with(textPaint) {
            color = ContextCompat.getColor(context, R.color.text_color)
            setTextSize(resources.getDimension(R.dimen.animateNotationTextSize))
            setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        }
        textBgPaint = Paint()
        with(textBgPaint) {
            color = ContextCompat.getColor(context, R.color.text_bg_color)
            maskFilter = BlurMaskFilter(_dp(5f), BlurMaskFilter.Blur.NORMAL)
            setTextSize(resources.getDimension(R.dimen.animateNotationTextSize))
            setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
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
        // Needed to scale width/height to char sizes passed in.
        viewWidth = resources.getDimension(R.dimen.anim_view_height)
        viewHeight = resources.getDimension(R.dimen.anim_view_width)
    }
    // render rate milliseconds. sets our frame rate.  This rate was chosen
    // because my oldest device (android 4.4) could handle it.
    private val renderRate = 75
    //distance each animationStepDistance
    private val animationStepDistance = _dp(3f)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var pause = false
        startTime = System.currentTimeMillis()
        canvas.drawColor(ContextCompat.getColor(context, R.color.animate_bg))
        if (strokePaths == null || strokeIdText == null) {
            Log.d("AnimatorView", "onDraw called with null paths or text")
            canvas.renderGrid()
            return
        }

        // draw the ghost char and grid.
        canvas.drawPath(charPath, bgCharPaint)
        canvas.renderGrid()
        // "faster" render speed means more animateSteps
        // which means the segment drawn will be longer.
        for (i in 0 until animateSteps) {
            if (strokePathCounter < strokePaths!!.size) {
                if (startNewLine) {
                    // Log.d("draw", "stroke $strokePathCounter")
                    distance = 0f
                    pause = true
                    // measure the length of the current path.
                    charPathMeasure.setPath(strokePaths!![strokePathCounter], false)
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
                        // Catch end points as we go.
                    }
                }
                else {
                    // next stroke...
                    strokePathCounter += 1
                    startNewLine = true
                }
                // Animation happens here -- invalidate restarts render if necessary.
                // Using a calculated period measured from the start of the
                // render gives a steady refresh rate.
                var sleepTime = renderRate - System.currentTimeMillis() + startTime
                if (pause)
                    sleepTime += 300
                postInvalidateDelayed(sleepTime)
            }
            canvas.drawPath(renderedCharPath, renderedCharPaint)
            if (strokePathCounter == strokePaths!!.size) {
                for (ti in positionedText) {
                    // Paint a white background first to make text
                    // stand out, then paint the text.
                    canvas.renderText(ti, textBgPaint)
                    canvas.renderText(ti, textBgPaint)
                    canvas.renderText(ti, textPaint)
                }
                canvas.drawCircle(pos[0], pos[1], _dp(3f), textPaint)
            }
            else {
                canvas.drawCircle(pos[0], pos[1], _dp(7f), cursorPaint)
            }
            // Log.d("draw", "${pos[0]},${pos[1]}")
        }
    }
}
