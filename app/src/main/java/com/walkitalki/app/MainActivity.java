package com.walkitalki.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new WalkiTalkiStatusView(this, AppTalkController.createMvpDemoController()));
    }
}
