package org.xbase.android.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;


public class ActivityMainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


    }

    @OnClick(R.id.button)
    void onButtonClick() {
        //TODO implement
    }

    @OnLongClick(R.id.button)
    boolean onButtonLongClick() {
        //TODO implement
        return true;
    }
}
