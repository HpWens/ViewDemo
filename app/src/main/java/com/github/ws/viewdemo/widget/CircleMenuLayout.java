package com.github.ws.viewdemo.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 3/10 0010.
 */
public class CircleMenuLayout extends ViewGroup {
    //圆形半径
    private int mRadius;
    //开始角度
    private double mStartAngle = 0;
    //padding属性  默认值为0
    private float mPadding = 0;
    //滑动时 item偏移量
    private int offsetRotation = 0;
    //最后一次触摸
    private long lastTouchTime;
    //判断触摸点  是否在圆类   默认false
    boolean isRange = false;
    //手指触摸的x,y值
    float x = 0, y = 0;
    // 手指滑动的方向   默认向右
    boolean isLeft = false;
    //适配
    private ListAdapter mAdapter;
    //转动速度 默认速度为0
    private float speed = 0;
    // 每个item 的默认尺寸
    private static final float ITEM_DIMENSION = 1 / 3f;
    //转动快慢
    private static final int ROTATION_DEGREE = 3;
    //distanceFromCenter Item到中心的距离
    private static final float DISTANCE_FROM_CENTER = 2 / 3f;
    //speed attenuation 速度衰减
    private static final int SPEED_ATTENUATION = 1;
    //每次转动的角度
    private static final int ANGLE = 6;
    //消息
    private static final int EMPTY_MESSAGE = 1;
    // MenuItem的点击事件接口
    private OnItemClickListener mOnMenuItemClickListener;
    //线程  处理item的转动
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == EMPTY_MESSAGE) {
                if (speed > 0) {
                    if (isLeft) {
                        //向左转动
                        offsetRotation -= ANGLE;
                    } else {
                        offsetRotation += ANGLE;
                    }
                    //速度衰减
                    speed -= SPEED_ATTENUATION;
                    invalidate();
                    handler.sendEmptyMessageDelayed(EMPTY_MESSAGE, 50);
                }
            }
        }
    };

    /**
     * @param context
     * @param attrs
     */
    public CircleMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPadding(0, 0, 0, 0);
    }

    /**
     * @param context
     */
    public CircleMenuLayout(Context context) {
        super(context);
        setPadding(0, 0, 0, 0);
    }

    public void setAdapter(ListAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    // 构建菜单项
    private void buildMenuItems() {
        // 根据用户设置的参数，初始化menu item
        for (int i = 0; i < mAdapter.getCount(); i++) {
            final View itemView = mAdapter.getView(i, null, this);
            final int position = i;
            if (itemView != null) {
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnMenuItemClickListener != null) {
                            mOnMenuItemClickListener.onItemClickListener(itemView, position);
                        }
                    }
                });
            }
            // 添加view到容器中
            addView(itemView);
        }
    }

    //窗口关联
    @Override
    protected void onAttachedToWindow() {
        if (mAdapter != null) {
            buildMenuItems();
        }
        super.onAttachedToWindow();
    }

    //设置布局的宽高，并策略menu item宽高
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 丈量自身尺寸
        measureMyself(widthMeasureSpec, heightMeasureSpec);
        // 丈量菜单项尺寸
        measureChildViews();
    }

    private void measureMyself(int widthMeasureSpec, int heightMeasureSpec) {
        int resWidth = 0;
        int resHeight = 0;
        // 根据传入的参数，分别获取测量模式和测量值
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 如果宽或者高的测量模式非精确值
        if (widthMode != MeasureSpec.EXACTLY
                || heightMode != MeasureSpec.EXACTLY) {
            // 主要设置为背景图的高度
            resWidth = getSuggestedMinimumWidth();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

            resHeight = getSuggestedMinimumHeight();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
        } else {
            // 如果都设置为精确值，则直接取小值；
            resWidth = resHeight = Math.min(width, height);
        }
        setMeasuredDimension(resWidth, resHeight);
    }

    private void measureChildViews() {
        // 获得半径
        mRadius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
        // menu item数量
        final int count = getChildCount();
        // menu item尺寸
        int childSize = (int) (mRadius * ITEM_DIMENSION);
        // menu item测量模式
        int childMode = MeasureSpec.EXACTLY;
        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            // 计算menu item的尺寸；以及和设置好的模式，去对item进行测量
            int makeMeasureSpec = -1;
            makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize,
                    childMode);
            child.measure(makeMeasureSpec, makeMeasureSpec);
        }
    }

    // 布局menu item的位置
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        refresh();
    }

    //刷新  偏移角度移动item
    public void refresh() {
        final int childCount = getChildCount();
        // 根据menu item的个数，计算item的布局占用的角度
        float angleDelay = 360 / childCount;
        // 遍历所有菜单项设置它们的位置
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int x = (int) Math.round(Math.sin(Math.toRadians(angleDelay * (i + 1) - offsetRotation)) * (mRadius * DISTANCE_FROM_CENTER));
            int y = (int) Math.round(Math.cos(Math.toRadians(angleDelay * (i + 1) - offsetRotation)) * (mRadius * DISTANCE_FROM_CENTER));
            //计算item 距离左边距  上边距 的距离
            if (x <= 0 && y >= 0) {
                x = mRadius - Math.abs(x);
                y = mRadius - y;
            } else if (x <= 0 && y <= 0) {
                y = mRadius + Math.abs(y);
                x = mRadius - Math.abs(x);
            } else if (x >= 0 && y <= 0) {
                y = mRadius + Math.abs(y);
                x = mRadius + x;
            } else if (x >= 0 && y >= 0) {
                x = mRadius + x;
                y = mRadius - Math.abs(y);
            }
            //计算item中心点 距离左边距  上边距 的距离
            x = x - (int) (mRadius * ITEM_DIMENSION) / 2;
            y = y - (int) (mRadius * ITEM_DIMENSION) / 2;
            // 布局child view
            child.layout(x, y,
                    x + (int) (mRadius * ITEM_DIMENSION), y + (int) (mRadius * ITEM_DIMENSION));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //碰撞 手指与大圆的碰撞   计算距离
                x = event.getX();
                y = event.getY();
                //手指与大圆触摸的误差
                int error = 10;
                if ((x - mRadius) * (x - mRadius) + (y - mRadius) * (y - mRadius) < (mRadius + error) * (mRadius + error)) {
                    isRange = true;
                    lastTouchTime = System.currentTimeMillis();
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
                    long timeStamp = System.currentTimeMillis() - lastTouchTime;
                    float distance = (float) Math.sqrt((x1 - x) * (x1 - x) + (y1 - y) * (y1 - y));
                    float speed = distance / timeStamp;
                    if (x1 - x > 0) {
                        isLeft = false;
                    } else {
                        isLeft = true;
                    }
                    //计算速度
                    speed(speed);
                }
                break;
        }
        return true;
    }

    public void speed(float speed) {
        this.speed = speed * ROTATION_DEGREE;
        handler.sendEmptyMessage(EMPTY_MESSAGE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#dddddd"));
        canvas.drawCircle(mRadius, mRadius, mRadius, paint);
        refresh();
    }

    //定义点击接口
    public interface OnItemClickListener {
        public void onItemClickListener(View v, int position);
    }

    // 设置MenuItem的点击事件接口
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnMenuItemClickListener = listener;
    }

    /**
     * 获得默认该layout的尺寸
     *
     * @return
     */
    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }
}
