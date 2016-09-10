package com.simit.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by liuchun on 2015/12/20.
 */
public class FeedBackActivity extends BaseActivity {
    private static final String FEEDBACK_URL = "http://gracesite.applinzi.com/feedback";
    // Component
    private EditText mTitle;
    private EditText mDescription;
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_feedback;
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
                String title = mTitle.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(FeedBackActivity.this, "标题不能为空，请输入", Toast.LENGTH_SHORT).show();
                    return;
                }

                String description = mDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(FeedBackActivity.this, "问题描述不能为空，请输入", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 提交到具体的website
                sendFeedBack(title, description);
            }
        });
    }

    /**
     * 发送反馈信息
     * @param title
     * @param description
     */
    private void sendFeedBack(String title, String description){

        /**TODO empty implement**/
    }
}
