package com.github.ws.viewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CircleMenuLayout circleMenuLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleMenuLayout = (CircleMenuLayout) findViewById(R.id.cm);

        List<Item> mList = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            Item item = new Item();
            item.imageRes = R.mipmap.cz;
            item.text = "石头";
            mList.add(item);
        }

        circleMenuLayout.setCircleMenuItemData(mList);


    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }
}
