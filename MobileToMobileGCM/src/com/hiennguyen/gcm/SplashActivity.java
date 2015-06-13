package com.hiennguyen.gcm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

	private final int SPLASH_DISPLAY_LENGTH = 3000;
	SessionManager sm;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_splash);
		sm=new SessionManager(getApplicationContext());
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				/* Create an Intent that will start the Menu-Activity. */
				Intent intent = null;
				if (sm.isRememberLogin()) {
					intent = new Intent(SplashActivity.this,HomeActivity.class);
				}else {
					intent = new Intent(SplashActivity.this,LogRegActivity.class);
				}
				startActivity(intent);
				finish();
			}
		}, SPLASH_DISPLAY_LENGTH);
	}
}
