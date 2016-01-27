package com.houlijiang.common.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.houlijiang.common.R;
import com.houlijiang.common.image.ImageOptions;
import com.houlijiang.common.image.activity.ImageBrowserActivity;
import com.houlijiang.common.ui.BaseActivity;

/**
 * Created by houlijiang on 16/1/26.
 */
public class TestImageBrowserActivity extends BaseActivity {

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_image_browser);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.test_image_browser_btn_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] urls =
                    new String[] { "http://pic85.nipic.com/file/20151212/22257486_115828234000_2.jpg",
                        "http://pic84.nipic.com/file/20151129/17961491_210752100000_2.jpg",
                        "http://pic84.nipic.com/file/20151209/22287948_101044394123_2.jpg" };
                ImageOptions option = new ImageOptions();
                option.setImageSize(new ImageOptions.ImageSize(500, 500));
                option.setImageResForEmptyUri(R.drawable.ic_common_list_empty);
                option.setImageResForEmptyUri(R.drawable.ic_common_list_fail);
                ImageBrowserActivity.launch(TestImageBrowserActivity.this, urls, 0, option);
            }
        });
    }
}
