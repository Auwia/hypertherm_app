package it.app.hypertherm;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Splash_Screen_Activity extends Activity {

	private Utility utility;

	private boolean settings_click = false;

	private Button settings;

	@Override
	public void onPause() {
		super.onPause();
		utility.appendLog("sono in PAUSA...");
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		utility = new Utility(this);
		utility.appendLog("splash screen start...");

		settings = (Button) findViewById(R.id.settings);

		new import_configuration_thread().execute();

		new carica_configurazione_logo().execute();

		settings.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				settings_click = true;

				Intent intent = new Intent(Splash_Screen_Activity.this,
						Settings.class);
				startActivity(intent);
			}
		});

		utility.appendLog("splash screen start...OK");

		new conteggio_time_out().execute();

	}

	private class carica_configurazione_logo extends
			AsyncTask<Void, Void, Void> {
		// ProgressDialog pdLoading = new ProgressDialog(
		// Splash_Screen_Activity.this);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			utility.appendLog("upload logo...");
		}

		@Override
		protected Void doInBackground(Void... params) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					File root = Environment.getExternalStorageDirectory();
					ImageView logo = (ImageView) findViewById(R.id.logo);
					Bitmap bMap = BitmapFactory.decodeFile(root
							+ "/TCaRe/images/logo.jpg");
					logo.setImageBitmap(bMap);

				}
			});

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			utility.appendLog("upload logo...OK");

		}

	}

	private class import_configuration_thread extends
			AsyncTask<Void, Void, Void> {
		// ProgressDialog pdLoading = new ProgressDialog(
		// Splash_Screen_Activity.this);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			utility.appendLog("import file configuration...");
		}

		@Override
		protected Void doInBackground(Void... params) {

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			utility.appendLog("import file configuration...OK");
		}

	}

	private class conteggio_time_out extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			utility.appendLog("loading main...");
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				Thread.sleep(utility.get_time_out_splash());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {

				if (!settings_click) {

					Intent intent = new Intent(Splash_Screen_Activity.this,
							MainActivity.class);
					startActivity(intent);
				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			utility.appendLog("loading main...OK");
		}

	}

}
