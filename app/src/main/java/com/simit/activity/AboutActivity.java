package com.simit.activity;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by liuchun on 2015/12/15.
 */
public class AboutActivity extends BaseActivity {
    // Component
    private TextView mAboutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initView() {
        super.initView();
        setToolBarTitle(R.string.app_about);
        //
        mAboutText = (TextView) findViewById(R.id.app_about);
        try{
            InputStream is = getAssets().open("about.html");
            BufferedInputStream bis = new BufferedInputStream(is);
            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[1024];
            int len;
            while((len = bis.read(buffer, 0, 1024)) != -1){
                sb.append(new String(buffer, 0, len));
            }

            String text = sb.toString();
            mAboutText.setText(Html.fromHtml(text));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
