package it.app.hypertherm.activity;

import it.app.hypertherm.R;
import it.app.hypertherm.TessutoListViewAdapter;
import it.app.hypertherm.Utility;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

public class TessutoActivity extends Activity {

	private TessutoListViewAdapter myAdapter;

	private Utility utility;

	private ListView listaMenuItem;
	private Button button_up, button_down, button_ok, button_home;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tessuto);

		utility = new Utility();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		listaMenuItem = (ListView) findViewById(R.id.listaMenuItem);

		button_up = (Button) findViewById(R.id.button_up);
		button_down = (Button) findViewById(R.id.button_down);
		button_ok = (Button) findViewById(R.id.button_ok);
		button_home = (Button) findViewById(R.id.button_home);

		import_menu_items();

		button_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		button_up.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (listaMenuItem.getCheckedItemPosition() > 0
						&& listaMenuItem.getCheckedItemPosition() <= listaMenuItem
								.getCount() - 1) {
					listaMenuItem.setItemChecked(
							listaMenuItem.getCheckedItemPosition() - 1, true);
				}

				listaMenuItem.setSelection(listaMenuItem
						.getCheckedItemPosition());

			}
		});

		button_down.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (listaMenuItem.getCheckedItemPosition() <= listaMenuItem
						.getCount() - 2) {
					listaMenuItem.setItemChecked(
							listaMenuItem.getCheckedItemPosition() + 1, true);
				}

				listaMenuItem.setSelection(listaMenuItem
						.getCheckedItemPosition());
			}
		});

		button_ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				preferences
						.edit()
						.putString(
								"TRATTAMENTO",
								myAdapter.getItem(
										listaMenuItem.getCheckedItemPosition())
										.getItem()).commit();

				Intent intent = new Intent(TessutoActivity.this,
						PatologiaActivity.class);
				startActivity(intent);
			}
		});

		new carica_configurazione_logo().execute();

		listaMenuItem.setItemChecked(0, true);
	}

	private void import_menu_items() {

		myAdapter = new TessutoListViewAdapter(this,
				utility.get_menu_items("TRATTAMENTI"));
		listaMenuItem.setAdapter(myAdapter);
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
							+ "/Hypertherm/images/logo.jpg");
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
}
