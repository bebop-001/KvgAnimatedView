package com.kana_tutor.kvgviewer;

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

// From https://codecrafted.net/svgtoandroid
// char = 令
// generated by https://codecrafted.net/svgtoandroid

class Render_04ee4 {
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
            t.reset();
            t.moveTo(49.62f,13.25f);
            t.cubicTo(49.73f,14.19f,50.0f,15.73f,49.4f,17.02f);
            t.cubicTo(45.25f,25.88f,34.25f,42.25f,12.75f,54.1f);
            t.transform(m);
            c.drawPath(t, p);
            c.drawPath(t, ps);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.save();
            t.reset();
            t.moveTo(50.54f,16.55f);
            t.cubicTo(56.67f,20.9f,75.53f,36.77f,84.52f,43.88f);
            t.cubicTo(87.74f,46.42f,90.12f,48.0f,94.25f,49.25f);
            t.transform(m);
            c.drawPath(t, p);
            c.drawPath(t, ps);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.save();
            c.save();
            c.save();
            t.reset();
            t.moveTo(47.27f,45.33f);
            t.cubicTo(50.81f,46.85f,56.43f,51.59f,57.31f,53.96f);
            t.transform(m);
            c.drawPath(t, p);
            c.drawPath(t, ps);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.save();
            c.save();
            t.reset();
            t.moveTo(29.08f,65.79f);
            t.cubicTo(30.43f,66.62f,33.27f,67.01f,34.8f,66.8f);
            t.cubicTo(37.83f,66.39f,70.22f,61.11f,73.42f,60.7f);
            t.cubicTo(76.62f,60.29f,77.42f,62.17f,75.13f,64.36f);
            t.cubicTo(69.75f,69.49f,53.26f,84.17f,51.24f,85.83f);
            t.transform(m);
            c.drawPath(t, p);
            c.drawPath(t, ps);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.save();
            t.reset();
            t.moveTo(41.59f,81.4f);
            t.cubicTo(47.75f,84.62f,55.25f,89.87f,58.71f,97.0f);
            t.transform(m);
            c.drawPath(t, p);
            c.drawPath(t, ps);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            c.restore();
            r(5,2,0,6);
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
            r(5,2,0,6,9,1,7,8,4,3);
            c.save();
            c.drawPath(t, ps);
            c.drawPath(t, p);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.save();
            c.save();
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.save();
            c.drawPath(t, ps);
            c.drawPath(t, p);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.save();
            c.save();
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.save();
            c.drawPath(t, ps);
            c.drawPath(t, p);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.save();
            c.save();
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.save();
            c.drawPath(t, ps);
            c.drawPath(t, p);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.save();
            c.save();
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.save();
            c.drawPath(t, ps);
            c.drawPath(t, p);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.restore();
            r(5,2,0,6,9,1,7,8,4,3);
            c.restore();
            r(5,2,0,6,9,1,7,8,4);
            p.setColor(Color.parseColor("#808080"));
            c.restore();
            r();

            c.restore();
        }

        public static Drawable getDrawable(int size){
            return new Render_04ee4Drawable(size);
        }

        public static Drawable getTintedDrawable(int size, int color){
            return new Render_04ee4Drawable(size, color);
        }

        private static class Render_04ee4Drawable extends Drawable {
            private int s = 0;
            private ColorFilter cf = null;

            public Render_04ee4Drawable(int s) {
                this.s = s;
                setBounds(0, 0, s, s);
                invalidateSelf();
            }

            public Render_04ee4Drawable(int s, int c) {
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
                Render_04ee4.cf = cf;
                Render_04ee4.draw(c, b.width(), b.height(), b.left, b.top);
                Render_04ee4.cf = null;
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
                    case 4: ps.setStrokeJoin(Paint.Join.ROUND); break;
                    case 5: ps.setColor(Color.argb(0,0,0,0)); break;
                    case 6: ps.setStrokeMiter(4.0f*od); break;
                    case 7: ps.setStrokeWidth(3.0f*od); break;
                    case 8: ps.setStrokeCap(Paint.Cap.ROUND); break;
                    case 9: p.setColor(Color.argb(0,0,0,0)); break;
                }
            }
        }
    };
    
}
