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
    private val t = Path()
    private val m = Matrix()
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
    fun draw(c: Canvas, w: Int, h: Int, dx: Int = 0, dy: Int = 0) {
        val ow = 109f
        val oh = 109f
        od = if (w / ow < h / oh) w / ow else h / oh
        r()
        c.save()
        c.translate((w - od * ow) / 2f + dx, (h - od * oh) / 2f + dy)
        m.reset()
        m.setScale(od, od)
        c.save()
        ps.color = Color.argb(0, 0, 0, 0)
        ps.strokeCap = Paint.Cap.BUTT
        ps.strokeJoin = Paint.Join.MITER
        ps.strokeMiter = 4.0f * od
        c.scale(1.0f, 1.0f)
        c.save()
        p.color = Color.argb(0, 0, 0, 0)
        ps.color = Color.parseColor("#000000")
        ps.strokeWidth = 3.0f * od
        ps.strokeCap = Paint.Cap.ROUND
        ps.strokeJoin = Paint.Join.ROUND
        c.save()
        c.save()
        c.save()
        t.reset()
        t.moveTo(49.62f, 13.25f)
        t.cubicTo(49.73f, 14.19f, 50.0f, 15.73f, 49.4f, 17.02f)
        t.cubicTo(45.25f, 25.88f, 34.25f, 42.25f, 12.75f, 54.1f)
        t.transform(m)
        c.drawPath(t, p)
        c.drawPath(t, ps)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.save()
        t.reset()
        t.moveTo(50.54f, 16.55f)
        t.cubicTo(56.67f, 20.9f, 75.53f, 36.77f, 84.52f, 43.88f)
        t.cubicTo(87.74f, 46.42f, 90.12f, 48.0f, 94.25f, 49.25f)
        t.transform(m)
        c.drawPath(t, p)
        c.drawPath(t, ps)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.save()
        c.save()
        c.save()
        t.reset()
        t.moveTo(47.27f, 45.33f)
        t.cubicTo(50.81f, 46.85f, 56.43f, 51.59f, 57.31f, 53.96f)
        t.transform(m)
        c.drawPath(t, p)
        c.drawPath(t, ps)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.save()
        c.save()
        t.reset()
        t.moveTo(29.08f, 65.79f)
        t.cubicTo(30.43f, 66.62f, 33.27f, 67.01f, 34.8f, 66.8f)
        t.cubicTo(37.83f, 66.39f, 70.22f, 61.11f, 73.42f, 60.7f)
        t.cubicTo(76.62f, 60.29f, 77.42f, 62.17f, 75.13f, 64.36f)
        t.cubicTo(69.75f, 69.49f, 53.26f, 84.17f, 51.24f, 85.83f)
        t.transform(m)
        c.drawPath(t, p)
        c.drawPath(t, ps)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.save()
        t.reset()
        t.moveTo(41.59f, 81.4f)
        t.cubicTo(47.75f, 84.62f, 55.25f, 89.87f, 58.71f, 97.0f)
        t.transform(m)
        c.drawPath(t, p)
        c.drawPath(t, ps)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        c.restore()
        r(5, 2, 0, 6)
        p.color = Color.argb(0, 0, 0, 0)
        ps.color = Color.parseColor("#000000")
        ps.strokeWidth = 3.0f * od
        ps.strokeCap = Paint.Cap.ROUND
        ps.strokeJoin = Paint.Join.ROUND
        c.save()
        p.color = Color.parseColor("#808080")
        c.save()
        c.save()
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.save()
        c.drawPath(t, ps)
        c.drawPath(t, p)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.save()
        c.save()
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.save()
        c.drawPath(t, ps)
        c.drawPath(t, p)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.save()
        c.save()
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.save()
        c.drawPath(t, ps)
        c.drawPath(t, p)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.save()
        c.save()
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.save()
        c.drawPath(t, ps)
        c.drawPath(t, p)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.save()
        c.save()
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.save()
        c.drawPath(t, ps)
        c.drawPath(t, p)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4, 3)
        c.restore()
        r(5, 2, 0, 6, 9, 1, 7, 8, 4)
        p.color = Color.parseColor("#808080")
        c.restore()
        r()
        c.restore()
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