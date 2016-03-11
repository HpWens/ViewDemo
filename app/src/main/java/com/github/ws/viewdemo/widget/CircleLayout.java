package com.github.ws.viewdemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by Administrator on 3/9 0009.
 */
public class CircleLayout extends ViewGroup {

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (speed > 0) {
                    if (isLeft) {
                        degree -= 6;
                    } else {
                        degree += 6;
                    }
                    speed -= 1;
                    invalidate();
                    handler.sendEmptyMessageDelayed(1, 50);
                }
            }
        }
    };

    public CircleLayout(Context context) {
        super(context);
    }

    public CircleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        // Log.e("------------", "-------onLayout--------" + i + "----" + i1 + "----" + i2 + "----------" + i3);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Log.e("------------", "-------onMeasure--------" + widthMeasureSpec + "----" + heightMeasureSpec);
    }

    private int radius = 200;

    private int degree = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Log.e("------------", "-------onDraw--------");

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#f00000"));
        paint.setAntiAlias(true);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);

        paint.setColor(Color.parseColor("#ffffff"));

        for (int i = 0; i < 12; i++) {
            int x = (int) Math.round(Math.sin(Math.toRadians(30 * (i + 1) - degree)) * radius);

            int y = (int) Math.round(Math.cos(Math.toRadians(30 * (i + 1) - degree)) * radius);

            canvas.drawLine(getWidth() / 2, getHeight() / 2, getWidth() / 2 + x, getHeight() / 2 - y, paint);

            paint.setColor(Color.parseColor("#dddddd"));

            canvas.drawCircle(getWidth() / 2 + x, getHeight() / 2 - y, 20, paint);
        }

    }

    //  速度  时间  关系      非常感谢  我的同事 小贾   刚开始我还想用重力加速度   根本没必要哈

    //存时间
    private long lastClickTime;

    boolean isRange = false;

    float x = 0, y = 0;

    boolean isLeft = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // radius += 50;
                //碰撞   计算距离
                x = event.getX();
                y = event.getY();
                if ((x - getWidth() / 2) * (x - getWidth() / 2) + (y - getHeight() / 2) * (y - getHeight() / 2) < (radius + 10) * (radius + 10)) {
                    isRange = true;
                    lastClickTime = System.currentTimeMillis();
                } else {
                    isRange = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                float x1 = event.getX();
                float y1 = event.getY();
                if (isRange) {
                    long timeStamp = System.currentTimeMillis() - lastClickTime;
                    float distance = (float) Math.sqrt((x1 - x) * (x1 - x) + (y1 - y) * (y1 - y));
                    float speed = distance / timeStamp;
                    // Log.e("------------", "-------speed--------***" + speed + "****" + distance);
                    if (x1 - x > 0) {
                        isLeft = false;
                    } else {
                        isLeft = true;
                    }
                    // invalidate();
                    speed(speed);
                }
                break;
        }
        return true;
    }

    private float speed = 0;

    public void speed(float speed) {
        this.speed = speed * 6;
        handler.sendEmptyMessage(1);
    }

}
