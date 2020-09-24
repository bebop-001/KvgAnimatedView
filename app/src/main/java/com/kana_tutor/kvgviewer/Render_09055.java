package com.kana_tutor.kvgviewer;


// TODO Include your package name here

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.ColorFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

public class Render_09055 {
    private static final Paint  p  = new Paint();
    private static final Paint  ps = new Paint();
    private static final Path   t  = new Path();
    private static final Matrix m  = new Matrix();
    private static float od;
    protected static ColorFilter cf = null;

    /**
     *  IMPORTANT: Due to the static usage of this class this
     *  method sets the tint color statically. So it is highly
     *  recommended to call the clearColorTint method when you
     *  have finished drawing.
     *
     *  Sets the color to use when drawing the SVG. This replaces
     *  all parts of the drawable which are not completely
     *  transparent with this color.
     */
    public static void setColorTint(int color){
        cf = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public static void clearColorTint(int color){
        cf = null;
    }

    public static void draw(Canvas c, int w, int h){
        draw(c, w, h, 0, 0);
    }

    public static void draw(Canvas c, int w, int h, int dx, int dy){
        float ow = 109f;
        float oh = 109f;

        od = (w / ow < h / oh) ? w / ow : h / oh;

        r();
        c.save();
        c.translate((w - od * ow) / 2f + dx, (h - od * oh) / 2f + dy);

        m.reset();
        m.setScale(od, od);

        c.save();
        ps.setColor(Color.argb(0,0,0,0));
        ps.setStrokeCap(Paint.Cap.BUTT);
        ps.setStrokeJoin(Paint.Join.MITER);
        ps.setStrokeMiter(4.0f*od);
        c.scale(1.0f,1.0f);
        c.save();
        p.setColor(Color.argb(0,0,0,0));
        ps.setColor(Color.parseColor("#000000"));
        ps.setStrokeWidth(3.0f*od);
        ps.setStrokeCap(Paint.Cap.ROUND);
        ps.setStrokeJoin(Paint.Join.ROUND);
        c.save();
        c.save();
        c.save();
        c.save();
        t.reset();
        t.moveTo(61.46f,10.0f);
        t.cubicTo(62.25f,11.0f,62.25f,12.21f,61.79f,13.68f);
        t.cubicTo(60.62f,17.37f,57.98f,27.07f,56.75f,30.15f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(47.25f,20.28f);
        t.cubicTo(48.75f,20.63f,50.25f,20.75f,51.44f,20.62f);
        t.cubicTo(59.77f,19.72f,70.75f,18.66f,74.88f,18.29f);
        t.cubicTo(77.34f,18.07f,79.13f,19.26f,78.54f,21.54f);
        t.cubicTo(77.92f,23.94f,77.13f,26.26f,76.34f,28.92f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(40.22f,32.27f);
        t.cubicTo(41.62f,32.86f,43.91f,32.67f,45.39f,32.52f);
        t.cubicTo(52.25f,31.82f,72.39f,29.92f,84.26f,29.18f);
        t.cubicTo(86.15f,29.06f,88.44f,29.04f,90.28f,29.51f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        c.save();
        c.save();
        t.reset();
        t.moveTo(48.1f,39.57f);
        t.cubicTo(48.74f,40.16f,49.13f,40.89f,49.27f,41.75f);
        t.cubicTo(49.97f,44.1f,50.31f,46.2f,50.91f,49.77f);
        t.cubicTo(51.06f,50.64f,51.22f,51.59f,51.4f,52.66f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(49.77f,40.45f);
        t.cubicTo(56.85f,39.59f,70.1f,38.06f,75.53f,37.47f);
        t.cubicTo(78.16f,37.18f,79.6f,38.26f,78.51f,40.92f);
        t.cubicTo(77.57f,43.22f,76.91f,46.05f,76.36f,47.96f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(52.39f,51.14f);
        t.cubicTo(58.01f,50.86f,67.54f,49.65f,74.52f,49.04f);
        t.cubicTo(75.99f,48.91f,77.32f,48.82f,78.42f,48.78f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(47.23f,59.95f);
        t.cubicTo(48.38f,60.5f,50.5f,60.65f,51.32f,60.59f);
        t.cubicTo(59.91f,60.05f,71.75f,59.0f,80.86f,58.12f);
        t.cubicTo(82.64f,57.95f,84.48f,57.94f,86.24f,58.25f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(49.74f,61.3f);
        t.cubicTo(50.12f,62.12f,50.25f,62.75f,50.25f,63.75f);
        t.cubicTo(50.25f,67.21f,50.37f,71.13f,50.48f,72.77f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(40.23f,74.01f);
        t.cubicTo(42.68f,74.62f,45.39f,74.24f,47.87f,74.08f);
        t.cubicTo(57.42f,73.44f,74.28f,72.07f,85.75f,71.53f);
        t.cubicTo(88.43f,71.4f,91.08f,71.23f,93.75f,71.63f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(67.75f,51.96f);
        t.cubicTo(68.64f,52.86f,68.99f,54.12f,68.99f,55.64f);
        t.cubicTo(68.99f,56.25f,69.0f,70.61f,68.91f,80.0f);
        t.cubicTo(68.88f,83.53f,68.87f,85.12f,68.87f,86.5f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        c.save();
        t.reset();
        t.moveTo(20.96f,19.25f);
        t.cubicTo(24.59f,20.99f,30.34f,26.42f,31.25f,29.13f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(13.0f,50.5f);
        t.cubicTo(15.25f,51.5f,16.75f,51.0f,17.75f,50.75f);
        t.cubicTo(18.75f,50.5f,27.01f,47.28f,28.5f,46.75f);
        t.cubicTo(30.62f,46.0f,31.86f,47.66f,31.0f,49.25f);
        t.cubicTo(22.75f,64.5f,23.5f,59.25f,31.25f,69.0f);
        t.cubicTo(32.29f,70.31f,32.25f,71.5f,30.75f,72.75f);
        t.cubicTo(29.25f,74.0f,21.0f,81.0f,19.5f,81.5f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.save();
        t.reset();
        t.moveTo(15.25f,83.0f);
        t.cubicTo(18.25f,82.5f,24.71f,81.72f,29.25f,82.5f);
        t.cubicTo(34.37f,83.38f,58.82f,90.46f,63.25f,91.75f);
        t.cubicTo(75.25f,95.25f,83.13f,96.25f,89.75f,96.5f);
        t.transform(m);
        c.drawPath(t, p);
        c.drawPath(t, ps);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        c.restore();
        r(9,2,0,4);
        p.setColor(Color.argb(0,0,0,0));
        ps.setColor(Color.parseColor("#000000"));
        ps.setStrokeWidth(3.0f*od);
        ps.setStrokeCap(Paint.Cap.ROUND);
        ps.setStrokeJoin(Paint.Join.ROUND);
        c.save();
        p.setColor(Color.parseColor("#808080"));
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.save();
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.save();
        c.drawPath(t, ps);
        c.drawPath(t, p);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6,3);
        c.restore();
        r(9,2,0,4,5,1,7,8,6);
        p.setColor(Color.parseColor("#808080"));
        c.restore();
        r();

        c.restore();
    }

    public static Drawable getDrawable(int size){
        return new Render_09055Drawable(size);
    }

    public static Drawable getTintedDrawable(int size, int color){
        return new Render_09055Drawable(size, color);
    }

    private static class Render_09055Drawable extends Drawable {
        private int s = 0;
        private ColorFilter cf = null;

        public Render_09055Drawable(int s) {
            this.s = s;
            setBounds(0, 0, s, s);
            invalidateSelf();
        }

        public Render_09055Drawable(int s, int c) {
            this(s);
            cf = new PorterDuffColorFilter(c, PorterDuff.Mode.SRC_IN);
        }

        @Override
        public int getIntrinsicHeight() {
            return s;
        }

        @Override
        public int getIntrinsicWidth() {
            return s;
        }

        @Override
        public void draw(Canvas c) {
            Rect b = getBounds();
            Render_09055.cf = cf;
            Render_09055.draw(c, b.width(), b.height(), b.left, b.top);
            Render_09055.cf = null;
        }

        @Override
        public void setAlpha(int i) {}

        @Override
        public void setColorFilter(ColorFilter c) { cf = c; invalidateSelf(); }

        @Override
        public int getOpacity() {
            return 0;
        }
    }

    private static void r(Integer... o){
        p.reset();
        ps.reset();
        if(cf != null){
            p.setColorFilter(cf);
            ps.setColorFilter(cf);
        }
        p.setAntiAlias(true);
        ps.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        ps.setStyle(Paint.Style.STROKE);
        for(Integer i : o){
            switch (i){
                case 0: ps.setStrokeJoin(Paint.Join.MITER); break;
                case 1: ps.setColor(Color.parseColor("#000000")); break;
                case 2: ps.setStrokeCap(Paint.Cap.BUTT); break;
                case 3: p.setColor(Color.parseColor("#808080")); break;
                case 4: ps.setStrokeMiter(4.0f*od); break;
                case 5: p.setColor(Color.argb(0,0,0,0)); break;
                case 6: ps.setStrokeJoin(Paint.Join.ROUND); break;
                case 7: ps.setStrokeWidth(3.0f*od); break;
                case 8: ps.setStrokeCap(Paint.Cap.ROUND); break;
                case 9: ps.setColor(Color.argb(0,0,0,0)); break;
            }
        }
    }
};
