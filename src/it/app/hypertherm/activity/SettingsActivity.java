package it.app.hypertherm.activity;

import it.app.hypertherm.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class SettingsActivity extends Activity {

	@Override
	public void onPause() {
		super.onPause();

		Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
		startActivity(intent);

		finish();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		ImageView work_in_progress = (ImageView) findViewById(R.id.work_in_progress);
		work_in_progress.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}

		});
	}

}
