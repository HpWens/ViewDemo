package com.github.ws.viewdemo;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 3/10 0010.
 */
public class CircleMenuLayout extends ViewGroup {

    private Context context;


    //圆形半径
    private int mRadius;

    // 布局时的开始角度
    private double mStartAngle = 0;

    //数据源
    List<Item> mList = new ArrayList<>();


    // 菜单布局资源id  默认布局
    private int mMenuItemLayoutId = R.layout.circle_menu_item;

    // MenuItem的点击事件接口
    private OnItemClickListener mOnMenuItemClickListener;


    // 该容器内item 的默认尺寸
    private static final float ITEM_DIMENSION = 1 / 3f;

    //转动快慢  测试3比较流畅
    private static final int ROTATION_DEGREE = 3;

    //padding属性  默认值为0
    private float mPadding = 0;

    //每次item转动的角度
    private int rotationAngle = 0;

    private int offsetRotation = 0;

    //最后一次点击时间
    private long lastClickTime;

    //判断触摸点  是否在圆类   默认false
    boolean isRange = false;

    //手指触摸的想x,y值
    float x = 0, y = 0;

    // 手指滑动的方向   默认向右
    boolean isLeft = false;

    //线程  处理item的转动
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    /**
     * @param context
     * @param attrs
     */
    public CircleMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setPadding(0, 0, 0, 0);
    }

    /**
     * @param context
     */
    public CircleMenuLayout(Context context) {
        super(context);
        this.context = context;
        setPadding(0, 0, 0, 0);
    }

    //设置数据源
    public void setCircleMenuItemData(List<Item> mList) {
        this.mList = mList;
        buildMenuItems();
    }

    // 构建菜单项
    private void buildMenuItems() {
        // 根据用户设置的参数，初始化menu item
        if (mList == null) {
            throw new IllegalArgumentException("数据源为空");
        } else {
            for (int i = 0; i < mList.size(); i++) {
                View itemView = inflateMenuView(i);
                // 初始化菜单项
                initMenuItem(itemView, i);
                // 添加view到容器中
                addView(itemView);
            }
        }
    }

    private void initMenuItem(View itemView, int childIndex) {
        ImageView iv = (ImageView) itemView
                .findViewById(R.id.iv_icon);
        TextView tv = (TextView) itemView
                .findViewById(R.id.tv_text);
        iv.setVisibility(View.VISIBLE);
        iv.setImageResource(mList.get(childIndex).imageRes);
        tv.setVisibility(View.VISIBLE);
        tv.setText(mList.get(childIndex).text);
    }

    private View inflateMenuView(final int childIndex) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View itemView = mInflater.inflate(mMenuItemLayoutId, this, false);
        itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMenuItemClickListener != null) {
                    mOnMenuItemClickListener.onItemClickListener(v, childIndex);
                }
            }
        });
        return itemView;
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
        final int childCount = getChildCount();
        // 根据menu item的个数，计算item的布局占用的角度
        float angleDelay = 360 / childCount;
        // 遍历所有菜单项设置它们的位置
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            int x = (int) Math.round(Math.sin(Math.toRadians(angleDelay * (i + 1) - offsetRotation)) * (mRadius * 2 / 3));

            int y = (int) Math.round(Math.cos(Math.toRadians(angleDelay * (i + 1) - offsetRotation)) * (mRadius * 2 / 3));


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

            x = x - (int) (mRadius * ITEM_DIMENSION) / 2;
            y = y -(int) (mRadius * ITEM_DIMENSION) / 2;

            Log.e("------------", "-------------***" + x + "**************" + y + "----" + childCount);

            // 布局child view
            child.layout(x, y,
                    x + (int) (mRadius * ITEM_DIMENSION), y + (int) (mRadius * ITEM_DIMENSION));


        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    // 设置MenuItem的布局文件，必须在setCircleMenuItemData之前调用
    public void setMenuItemLayoutId(int mMenuItemLayoutId) {
        this.mMenuItemLayoutId = mMenuItemLayoutId;
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
