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
@file:Suppress("UnnecessaryVariable", "unused", "unused", "unused", "unused", "unused")

package com.kana_tutor.kvgviewer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat

// our own personal exception.
class KvgAnimateException(message:String) : Exception (message) {
    /*
    constructor(op:String, found:Int, expected:Int) :
            this(String.format("op:%s expected %d args found %d",
                op, found, expected))
    constructor(renderChar:Char, isPathFile: Boolean, error:String) :
            this(String.format("KvgToAndroidPaths filed for \"%c\" %s file:" +
                    "Exception:%s", renderChar,
                if (isPathFile) "path file " else "svg file ",
                error))
     */
}

// a custom path for rendering an animated view of a
// character as expressed in a Kvg file.
// See https://c05mic.com/2012/03/23/animating-a-bitmap-using-path-and-pathmeasure-android/
// for the concept behind the animation.
class AnimatorView(context: Context, attrs: AttributeSet) :
        View(context, attrs)
{
    // Attributes.
    private val layoutHeight : Float        // android:layout_width
    private val layoutWidth : Float         // android:layout_height
    private val animateStrokeWidth : Float  // app:animate_stroke_width
    private val animateStrokeColor : Int    // app:animate_stroke_color
    private val cursorColor: Int            // app:animate_cursor_color
    private val gridColor: Int              // app:grid_color
    private val backgroundColor: Int        // app:animate_background
    private val annotateTextSize : Float    // app:annotate_text_size
    private val annotateTextColor : Int     // app:annotate_text_color

    // used for calculating display independent values.
    private var charWidth = 0f
    private var charHeight = 0f

    // used to scale char to view size.
    private lateinit var scaleMatrix: Matrix

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

    private val renderedCharPaint : Paint   // the stroked character
    private val ghostCharPaint : Paint      // blured character behind stroked char.
    private val cursorPaint : Paint         // a cursor to highlight the stroke.
    private val textPaint : Paint           // annotation text for stroke order.
    private val textBgPaint : Paint         // blured text behind annotation in
                                            // same color as background to make text
                                            // standout
    private val gridPaint : Paint           // the grid
    private val bgPaint : Paint             // the background.

    // convert pix/font point for display independence
    private fun Float.pxToDp(): Float {
        var dp = this / resources.displayMetrics.density
        // dp = 1 pixel if its zero to prevent divide by 0 error.
        if (dp < 1) dp = 1f
        return dp
    }
    private fun Int.pxToDp() : Float = this.toFloat().pxToDp()
    private fun Float.dpToPx(): Float {
        return this * resources.displayMetrics.density
    }
    private fun Int.dpToPx() : Float = this.toFloat().pxToDp()

    private fun String?.attrDpToPix(default : String = "") : Float {
        if (this != null) {
            val match = "^(\\d+(?:.\\d+)*)(?:dip|dp)$".toRegex().find(this)
            if (match != null) {
                return (match.groupValues[1].toFloat()).dpToPx()
            }
        }
        else if (default.isNotEmpty()) {
            return default.attrDpToPix()
        }
        throw KvgAnimateException(
            this ?: "null" + ": not valid dp value.  " +
            "Please assign AnimatorView layout_width " +
            "and layout_height in dp units.")
    }
    // use if default is a resource id
    private fun String?.attrDpToPix(resId : Int) : Float =
        this.attrDpToPix("@$resId")
    private fun String?.attrSpToPix(default : String = "") : Float {
        if (this != null) {
            val match = "^(\\d+(?:.\\d+)*)sp$".toRegex().find(this)
            if (match != null) {
                return match.groupValues[1].toFloat() *
                        resources.displayMetrics.scaledDensity
            }
        }
        else if (default.isNotEmpty()) {
            return default.attrSpToPix()
        }
        throw KvgAnimateException(
            this ?: "null" + ": not valid sp value.  " +
            "Please assign AnimatorView text_size in sp units." )
    }
    // use if default is a resource id
    private fun String?.attrSpToPix(resId : Int) : Float =
        this.attrSpToPix("@$resId")

    private fun String?.attrToColor(default:String = "") : Int {
        if (this != null) {
            val match = "^([@#])([0-9a-f]+)$"
                .toRegex(RegexOption.IGNORE_CASE)
                .find(this)
            if (match != null) {
                val (op, value) = match.destructured
                val color =
                if (op == "@") {
                    ContextCompat.getColor(context, value.toInt())
                }
                else {
                    // #hex format
                    Color.parseColor(this)
                }
                return color
            }
        }
        else if (default.isNotEmpty()) {
            return default.attrToColor()
        }
        throw KvgAnimateException(
            this ?: "null" + ": not valid color value or resource id.")
    }
    // use if default is a resource id
    private fun String?.attrToColor(resId : Int) : Int =
        this.attrToColor("@$resId")

    init{
        // set values from xml attributes.
        val androidNameSpace = "http://schemas.android.com/apk/res/android"
        layoutHeight = attrs
            .getAttributeValue(androidNameSpace, "layout_height")
            .attrDpToPix()
        layoutWidth = attrs
            .getAttributeValue(androidNameSpace, "layout_width")
            .attrDpToPix()

        val appNameSpace = "http://schemas.android.com/apk/res-auto"
        backgroundColor = attrs.getAttributeValue(appNameSpace, "animate_background")
            .attrToColor("#FFFFF0")

        animateStrokeWidth = attrs.getAttributeValue(appNameSpace, "animate_stroke_width")
            .attrDpToPix("10dp")

        animateStrokeColor = attrs.getAttributeValue(appNameSpace, "animate_stroke_color")
            .attrToColor("#0D47A1")
        cursorColor = attrs.getAttributeValue(appNameSpace, "animate_cursor_color")
            .attrToColor(android.R.color.holo_orange_dark)

        annotateTextSize = attrs.getAttributeValue(appNameSpace, "annotate_text_size")
            .attrSpToPix("20sp")
        annotateTextColor = attrs.getAttributeValue(appNameSpace, "annotate_text_color")
            .attrToColor("#42A5F5")

        gridColor = attrs.getAttributeValue(appNameSpace, "grid_color")
            .attrToColor(android.R.color.holo_blue_dark)

        Log.d("onSizeChanged", "layoutWidth:$layoutWidth, layoutHeight:$layoutHeight")

        // Various paint objects used during render.
        // the character is rendered with this brush.
        renderedCharPaint = Paint()
        with(renderedCharPaint) {
            color = animateStrokeColor
            strokeWidth = animateStrokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        // used to paint a blured version of the character
        // so user can see what's being painted.
        ghostCharPaint = Paint()
        with(ghostCharPaint) {
            val bgStrokeWidth = animateStrokeWidth * 0.5f
            color = animateStrokeColor
            strokeWidth = bgStrokeWidth
            style = Paint.Style.STROKE
            // set android:hardwareAccelerated="false" for activity
            // in AndroidManifest.xml for this to work.
            maskFilter = BlurMaskFilter(bgStrokeWidth, BlurMaskFilter.Blur.NORMAL)
        }

        // we put a cursor on the char in this color
        // to show the movement of the brush.
        cursorPaint = Paint()
        with(cursorPaint) {
            color = cursorColor
            strokeWidth = animateStrokeWidth * 0.1f
            style = Paint.Style.FILL_AND_STROKE
            maskFilter = setMaskFilter(
                BlurMaskFilter(
                    animateStrokeWidth * 0.5f, BlurMaskFilter.Blur.NORMAL)
            )
        }
        textPaint = Paint()
        with(textPaint) {
            color = annotateTextColor
            textSize = annotateTextSize
            setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        }
        textBgPaint = Paint()
        with(textBgPaint) {
            color = backgroundColor
            maskFilter = BlurMaskFilter(3f.dpToPx(), BlurMaskFilter.Blur.SOLID)
            textSize = annotateTextSize
            setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        }

        // used to paint the grid.
        gridPaint = Paint()
        with(gridPaint) {
            color = gridColor
            strokeWidth = 3f.dpToPx()
            style = Paint.Style.STROKE
        }
        bgPaint = Paint()
        with (bgPaint) {
            color = backgroundColor
            style = Paint.Style.FILL
        }
    }
    private fun PositionedTextInfo.putText(matrix:Matrix) : PositionedTextInfo {
        val src = floatArrayOf(this.x, this.y)
        val dst = floatArrayOf(0f,0f)
        matrix.mapPoints(dst, src)
        val rv = PositionedTextInfo(dst[0], dst[1], text)
        // Log.d("putText", "$this -> $rv")
        return rv
    }
    private fun Canvas.renderText(text : PositionedTextInfo, paint:Paint) {
        drawText(text.text, text.x, text.y, paint)
    }

    // speed of animation is determined by number of steps.
    // More steps per frame == faster animation.
    private val speed = intArrayOf(3, 6, 9)
    private var animateSteps = 0
    fun setAnimateSpeed(speedIn: Int) {
        animateSteps = speed[speedIn]
    }
    private val positionedText = mutableListOf<PositionedTextInfo>()
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

        startNewLine = true
        strokePathCounter = 0
        renderedCharPath.reset()

        scaleMatrix = Matrix()
        scaleMatrix.setScale(
            layoutWidth/ charWidth, layoutHeight/ charHeight,
            0f, 0f)
        for (i in strokePaths!!.indices) {
            strokePaths!![i].transform(scaleMatrix)
        }
        strokePaths!!.map { charPath.addPath(it) }
        strokeIdText!!.map{
            positionedText.add(it.putText(scaleMatrix))
        }
    }

    private fun Canvas.renderGrid() {
        val (left, top, right, bottom) =
            arrayOf(4f, 4f, layoutWidth - 4f, layoutHeight - 4f)
        drawRect(RectF(left, top, right, bottom), gridPaint)
        val h = layoutHeight / 3
        drawLine(left, h, right, h, gridPaint)
        drawLine(left, 2 * h, right, 2 * h, gridPaint)
        val w = layoutWidth / 3
        drawLine(w, top, w, bottom, gridPaint)
        drawLine(2 * w, top, 2 * w, bottom, gridPaint)
    }

    // render rate milliseconds. sets our frame rate.  This rate was chosen
    // because my oldest device (android 4.4) could handle it.
    private val renderRate = 75
    //distance each animationStepDistance
    private val animationStepDistance = 3f.dpToPx()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var pause = false
        startTime = System.currentTimeMillis()
        canvas.drawPaint(bgPaint)
        if (strokePaths == null || strokeIdText == null) {
            Log.d("AnimatorView", "onDraw called with null paths or text")
            canvas.renderGrid()
            return
        }

        // draw the ghost char and grid.
        canvas.drawPath(charPath, ghostCharPaint)
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
                        canvas.drawCircle(pos[0], pos[1],
                            0.5f * animateStrokeWidth, cursorPaint)
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
                canvas.drawCircle(pos[0], pos[1],
                    0.5f * animateStrokeWidth, textPaint)
            }
            else {
                canvas.drawCircle(pos[0], pos[1],
                    0.5f * animateStrokeWidth, cursorPaint)
            }
            // Log.d("draw", "${pos[0]},${pos[1]}")
        }
    }
}
