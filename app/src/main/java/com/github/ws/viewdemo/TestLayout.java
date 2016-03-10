package com.github.ws.viewdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created by Administrator on 3/10 0010.
 */
public class TestLayout  extends ViewGroup {

    public TestLayout(Context context) {
        super(context);
    }

    public TestLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

        Log.e("---------","---------"+i+"----"+i1+"-----"+i2+"----"+i3);


    }
}
