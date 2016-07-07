package org.xbase.android.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import org.xbase.android.navigation.bottom.BottomNavigationActivity;
import org.xbase.android.utils.App;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;


public class ActivityMainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.init(getApplication());
        ButterKnife.bind(this);
        startActivity(new Intent(this, BottomNavigationActivity.class));

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
