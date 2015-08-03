package it.app.hypertherm.activity;

import it.app.hypertherm.PatologiaListViewAdapter;
import it.app.hypertherm.R;
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

public class PatologiaActivity extends Activity {

	private PatologiaListViewAdapter myAdapter;

	private Utility utility;

	private ListView listaMenuItem;
	private Button button_up, button_down, button_ok, button_home;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patologia);

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

				int position = listaMenuItem.getCheckedItemPosition();
				utility.appendLog("CIAO: " + position);

				if (position == -1) {
					position = 0;
				} else if (position >= listaMenuItem.getCount()) {
					position = listaMenuItem.getCount() - 1;
				}

				if (listaMenuItem.getCheckedItemPosition() > 0) {

					if (!myAdapter.getItem(
							listaMenuItem.getCheckedItemPosition() - 1)
							.getMenuFlaggato()) {

						if (position - 2 >= 0) {
							listaMenuItem.setItemChecked(
									listaMenuItem.getCheckedItemPosition() - 2,
									true);
						} else {
							return;
						}
					} else {

						if (position - 1 >= 0) {
							listaMenuItem.setItemChecked(
									listaMenuItem.getCheckedItemPosition() - 1,
									true);
						} else {
							return;
						}
					}
				} else {
					return;
				}

				listaMenuItem.setSelection(listaMenuItem
						.getCheckedItemPosition() - 5);

			}
		});

		button_down.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				int position = listaMenuItem.getCheckedItemPosition();
				utility.appendLog("CIAO: " + position);

				if (position == -1) {
					position = 0;
				} else if (position >= listaMenuItem.getCount()) {
					position = listaMenuItem.getCount() - 1;
				}

				if (listaMenuItem.getCheckedItemPosition() < listaMenuItem
						.getCount() - 1) {
					if (!myAdapter.getItem(
							listaMenuItem.getCheckedItemPosition() + 1)
							.getMenuFlaggato()) {

						if (position + 2 <= listaMenuItem.getCount() - 1) {
							listaMenuItem.setItemChecked(
									listaMenuItem.getCheckedItemPosition() + 2,
									true);
						} else {
							return;
						}
					} else {
						if (position + 1 <= listaMenuItem.getCount() - 1) {
							listaMenuItem.setItemChecked(
									listaMenuItem.getCheckedItemPosition() + 1,
									true);
						} else {
							return;
						}
					}
				} else {
					return;
				}

				listaMenuItem.setSelection(listaMenuItem
						.getCheckedItemPosition());

			}
		});

		button_ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				load_menu_item(listaMenuItem.getCheckedItemPosition());
			}
		});

		new carica_configurazione_logo().execute();

	}

	protected void load_menu_item(int position) {

		Intent intent;

		intent = new Intent(PatologiaActivity.this, WorkActivity.class);
		startActivity(intent);

		switch (position) {
		case 0:

			break;
		case 1:
			break;
		case 2:

			// intent = new Intent(MainActivity.this, WorkActivity.class);
			// startActivity(intent);

			break;
		case 3:
			break;
		case 4:
			break;
		}

	}

	private void import_menu_items() {

		myAdapter = new PatologiaListViewAdapter(this,
				utility.get_menu_items("PATOLOGIE"));
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
