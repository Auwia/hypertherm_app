package it.app.hypertherm.activity;

import it.app.hypertherm.Caricamento;
import it.app.hypertherm.R;
import it.app.hypertherm.util.Utility;
import it.app.hypertherm.util.carica_configurazione_logo;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Splash_Screen_Activity extends Activity {

	private Utility utility;

	private boolean settings_click = false;

	private Button settings;

	@Override
	public void onPause() {
		super.onPause();

		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		utility = new Utility(this);
		utility.appendLog("D", "splash screen start...");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		settings = (Button) findViewById(R.id.settings);

		new carica_configurazione_logo(this).execute();

		new Caricamento(this);

		settings.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				settings_click = true;

				Intent intent = new Intent(Splash_Screen_Activity.this,
						SettingsActivity.class);
				startActivity(intent);
			}
		});

		utility.appendLog("D", "splash screen start...OK");

		new conteggio_time_out().execute();

	}

	private class conteggio_time_out extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			utility.appendLog("D", "loading main...");
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

			utility.appendLog("D", "loading main...OK");
		}

	}

}
