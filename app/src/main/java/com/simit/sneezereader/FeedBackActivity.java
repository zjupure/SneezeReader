package com.simit.sneezereader;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by liuchun on 2015/12/20.
 */
public class FeedBackActivity extends BaseActivity {
    // Component
    private EditText mTitle;
    private EditText mDescription;
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initView();
    }

    @Override
    protected void initView(){
        // ToolBar
        super.initView();
        setToolBarTitle(R.string.setting_feedback);
        // EditText
        mTitle = (EditText) findViewById(R.id.feedback_title_input);
        mDescription = (EditText) findViewById(R.id.feedback_des_input);
        // Button
        mSubmit = (Button) findViewById(R.id.feedback_submit);
        // listener
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check the content is not empty
                if(mTitle.getText().toString().isEmpty()){
                    Toast.makeText(FeedBackActivity.this, "标题不能为空，请输入", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mDescription.getText().toString().isEmpty()){
                    Toast.makeText(FeedBackActivity.this, "问题描述不能为空，请输入", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 提交到具体的website
                Toast.makeText(FeedBackActivity.this, "反馈提交成功", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
