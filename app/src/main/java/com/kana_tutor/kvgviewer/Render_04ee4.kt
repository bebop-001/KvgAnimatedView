package com.kana_tutor.kvgviewer

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable

// From https://codecrafted.net/svgtoandroid
// char = 令
// generated by https://codecrafted.net/svgtoandroid
internal object Render_04ee4 {
    private val p = Paint()
    private val ps = Paint()
    private val path = Path()
    private val matrix = Matrix()
    private var od = 0f
    internal var cf: ColorFilter? = null

    /**
     * IMPORTANT: Due to the static usage of this class this
     * method sets the tint color statically. So it is highly
     * recommended to call the clearColorTint method when you
     * have finished drawing.
     *
     * Sets the color to use when drawing the SVG. This replaces
     * all parts of the drawable which are not completely
     * transparent with this color.
     */
    fun setColorTint(color: Int) {
        cf = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    fun clearColorTint(color: Int) {
        cf = null
    }

    @JvmOverloads
    fun draw(canvas: Canvas, w: Int, h: Int, dx: Int = 0, dy: Int = 0) {
        val winWidth = 109f
        val winHeight = 109f
        od = if (w / winWidth < h / winHeight) w / winWidth else h / winHeight
        r()
        canvas.save()
        canvas.translate((w - od * winWidth) / 2f + dx, (h - od * winHeight) / 2f + dy)
        matrix.reset()
        matrix.setScale(od, od)
        canvas.save()
        ps.color = Color.argb(0, 0, 0, 0)
        ps.strokeCap = Paint.Cap.BUTT
        ps.strokeJoin = Paint.Join.MITER
        ps.strokeMiter = 4.0f * od
        canvas.scale(1.0f, 1.0f)
        canvas.save()
        p.color = Color.argb(0, 0, 0, 0)
        ps.color = Color.parseColor("#000000")
        ps.strokeWidth = 3.0f * od
        ps.strokeCap = Paint.Cap.ROUND
        ps.strokeJoin = Paint.Join.ROUND
        canvas.save()
        canvas.save()
        canvas.save()
        path.reset()
        path.moveTo(49.62f, 13.25f)
        path.cubicTo(49.73f, 14.19f, 50.0f, 15.73f, 49.4f, 17.02f)
        path.cubicTo(45.25f, 25.88f, 34.25f, 42.25f, 12.75f, 54.1f)
        path.transform(matrix)
        canvas.drawPath(path, p)
        canvas.drawPath(path, ps)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.save()
        path.reset()
        path.moveTo(50.54f, 16.55f)
        path.cubicTo(56.67f, 20.9f, 75.53f, 36.77f, 84.52f, 43.88f)
        path.cubicTo(87.74f, 46.42f, 90.12f, 48.0f, 94.25f, 49.25f)
        path.transform(matrix)
        canvas.drawPath(path, p)
        canvas.drawPath(path, ps)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.save()
        canvas.save()
        canvas.save()
        path.reset()
        path.moveTo(47.27f, 45.33f)
        path.cubicTo(50.81f, 46.85f, 56.43f, 51.59f, 57.31f, 53.96f)
        path.transform(matrix)
        canvas.drawPath(path, p)
        canvas.drawPath(path, ps)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.save()
        canvas.save()
        path.reset()
        path.moveTo(29.08f, 65.79f)
        path.cubicTo(30.43f, 66.62f, 33.27f, 67.01f, 34.8f, 66.8f)
        path.cubicTo(37.83f, 66.39f, 70.22f, 61.11f, 73.42f, 60.7f)
        path.cubicTo(76.62f, 60.29f, 77.42f, 62.17f, 75.13f, 64.36f)
        path.cubicTo(69.75f, 69.49f, 53.26f, 84.17f, 51.24f, 85.83f)
        path.transform(matrix)
        canvas.drawPath(path, p)
        canvas.drawPath(path, ps)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.save()
        path.reset()
        path.moveTo(41.59f, 81.4f)
        path.cubicTo(47.75f, 84.62f, 55.25f, 89.87f, 58.71f, 97.0f)
        path.transform(matrix)
        canvas.drawPath(path, p)
        canvas.drawPath(path, ps)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        canvas.restore()
        r(5, 2, 0, 6)
        p.color = Color.argb(0, 0, 0, 0)
        ps.color = Color.parseColor("#000000")
        ps.strokeWidth = 3.0f * od
        ps.strokeCap = Paint.Cap.ROUND
        ps.strokeJoin = Paint.Join.ROUND
        canvas.save()
        p.color = Color.parseColor("#808080")
        canvas.save()
        canvas.save()
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.save()
        canvas.drawPath(path, ps)
        canvas.drawPath(path, p)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.save()
        canvas.save()
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.save()
        canvas.drawPath(path, ps)
        canvas.drawPath(path, p)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.save()
        canvas.save()
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.save()
        canvas.drawPath(path, ps)
        canvas.drawPath(path, p)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.save()
        canvas.save()
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.save()
        canvas.drawPath(path, ps)
        canvas.drawPath(path, p)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.save()
        canvas.save()
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.save()
        canvas.drawPath(path, ps)
        canvas.drawPath(path, p)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        canvas.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        p.color = Color.parseColor("#808080")
        canvas.restore()
        r()
        canvas.restore()
    }

    fun getDrawable(size: Int): Drawable {
        return Render_04ee4Drawable(size)
    }

    fun getTintedDrawable(size: Int, color: Int): Drawable {
        return Render_04ee4Drawable(size, color)
    }

    private fun r(vararg o: Int) {
        p.reset()
        ps.reset()
        if (cf != null) {
            p.colorFilter = cf
            ps.colorFilter = cf
        }
        p.isAntiAlias = true
        ps.isAntiAlias = true
        p.style = Paint.Style.FILL
        ps.style = Paint.Style.STROKE
        for (i in o) {
            when (i) {
                0 -> ps.strokeJoin = Paint.Join.MITER
                1 -> ps.color = Color.parseColor("#000000")
                2 -> ps.strokeCap = Paint.Cap.BUTT
                3 -> p.color = Color.parseColor("#808080")
                4 -> ps.strokeJoin = Paint.Join.ROUND
                5 -> ps.color = Color.argb(0, 0, 0, 0)
                6 -> ps.strokeMiter = 4.0f * od
                7 -> ps.strokeWidth = 3.0f * od
                8 -> ps.strokeCap = Paint.Cap.ROUND
                9 -> p.color = Color.argb(0, 0, 0, 0)
            }
        }
    }

    private class Render_04ee4Drawable(private val s: Int) : Drawable() {
        private var cf: ColorFilter? = null

        constructor(s: Int, c: Int) : this(s) {
            cf = PorterDuffColorFilter(c, PorterDuff.Mode.SRC_IN)
        }

        override fun getIntrinsicHeight(): Int {
            return s
        }

        override fun getIntrinsicWidth(): Int {
            return s
        }

        override fun draw(c: Canvas) {
            val b = bounds
            Render_04ee4.cf = cf
            draw(c, b.width(), b.height(), b.left, b.top)
            Render_04ee4.cf = null
        }

        override fun setAlpha(i: Int) {}
        override fun setColorFilter(c: ColorFilter?) {
            cf = c
            invalidateSelf()
        }

        @SuppressLint("WrongConstant")
        override fun getOpacity(): Int {
            return 0
        }

        init {
            setBounds(0, 0, s, s)
            invalidateSelf()
        }
    }
}