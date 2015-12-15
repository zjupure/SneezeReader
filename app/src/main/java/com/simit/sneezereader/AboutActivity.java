package com.simit.sneezereader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by liuchun on 2015/12/15.
 */
public class AboutActivity extends AppCompatActivity {
    // Component
    private Toolbar mToolBar;
    private TextView mAboutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        // ToolBar
        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        mToolBar.setTitle(R.string.app_about);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAboutText = (TextView) findViewById(R.id.app_about);
        try{
            InputStream is = getAssets().open("about.html");
            BufferedInputStream bis = new BufferedInputStream(is);
            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[1024];
            int len = -1;
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
