package it.app.hypertherm.activity;

import it.app.hypertherm.MenuListViewAdapter;
import it.app.hypertherm.R;
import it.app.hypertherm.db.HyperthermDB;
import it.app.hypertherm.thread.carica_configurazione_logo;
import it.app.hypertherm.util.Utility;
import net.sf.andpdf.pdfviewer.PdfViewerActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {

	private MenuListViewAdapter myAdapter;

	private Utility utility;

	private ListView listaMenuItem;

	private SharedPreferences preferences;

	private boolean exit = false;

	@Override
	public void onPause() {
		super.onPause();

		if (!exit) {
			finish();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		utility = new Utility(this);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.edit().putBoolean("isMenu", true).commit();
		preferences.edit().putBoolean("isTessuto", false).commit();

		import_menu_items();

		runOnUiThread(new carica_configurazione_logo(this));

		listaMenuItem.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {

				for (int i = 0; i < listaMenuItem.getChildCount(); i++) {

					if (position == i) {
						listaMenuItem.getChildAt(i).setBackgroundColor(
								Color.WHITE);
						listaMenuItem.getChildAt(i).setPressed(true);

					} else {
						listaMenuItem.getChildAt(i).setPressed(false);
						if (i % 2 == 1) {
							listaMenuItem.getChildAt(i).setBackgroundColor(
									Color.parseColor("#39bdce"));
						} else {
							listaMenuItem.getChildAt(i).setBackgroundColor(
									Color.parseColor("#00aac0"));
						}
					}
				}

				load_menu_item(position);
			}
		});

		listaMenuItem.setDivider(null);

		WorkActivity.SIMULATORE = false;

	}

	@Override
	public void onResume() {
		super.onResume();

		for (int i = 0; i < listaMenuItem.getChildCount(); i++) {

			if (i % 2 == 1) {
				listaMenuItem.getChildAt(i).setBackgroundColor(
						Color.parseColor("#39bdce"));
			} else {
				listaMenuItem.getChildAt(i).setBackgroundColor(
						Color.parseColor("#00aac0"));
			}

		}

	}

	protected void load_menu_item(int position) {

		Intent intent;

		switch (position) {
		case 0:
			intent = new Intent(MainActivity.this, TessutoActivity.class);

			if (WorkActivity.SIMULATORE) {
				intent.putExtra("DEMO", true);
			} else {

				intent.putExtra("DEMO", false);
			}
			startActivity(intent);

			break;
		case 1:
			intent = new Intent(MainActivity.this,
					StrutturaProfonditaActivity.class);
			if (WorkActivity.SIMULATORE) {
				intent.putExtra("DEMO", true);
			} else {

				intent.putExtra("DEMO", false);
			}
			startActivity(intent);
			break;
		case 2:

			preferences.edit().putInt("ANTENNA", utility.getAntenna("DEFAULT"))
					.commit();

			preferences.edit()
					.putFloat("WATER", utility.getWaterTemperature("DEFAULT"))
					.commit();

			preferences.edit().putFloat("DELTAT", utility.getDeltaT("DEFAULT"))
					.commit();

			preferences.edit().putInt("TIME", utility.getTime("DEFAULT"))
					.commit();

			preferences.edit()
					.putString("MENU_ITEM", utility.getMenuItemDefault())
					.commit();

			intent = new Intent(MainActivity.this, WorkActivity.class);
			if (WorkActivity.SIMULATORE) {
				intent.putExtra("DEMO", true);
			} else {

				intent.putExtra("DEMO", false);
			}
			startActivity(intent);

			break;
		case 3:

			if (WorkActivity.SIMULATORE) {

				WorkActivity.SIMULATORE = false;

				import_menu_items();

			} else {

				WorkActivity.SIMULATORE = true;

				myAdapter.getItem(position).setItem(
						utility.get_menu_item_demo_training_2());
				listaMenuItem.setAdapter(myAdapter);
			}

			break;
		case 4:

			exit = true;

			intent = new Intent(MainActivity.this, OpenPdfActivity.class);
			intent.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME,
					Environment.getExternalStorageDirectory()
							+ "/Hypertherm/pdf/user_manual.pdf");
			startActivity(intent);

			break;
		}

	}

	private void import_menu_items() {

		myAdapter = new MenuListViewAdapter(this,
				utility.get_menu_items(HyperthermDB.TABLE_MENU));
		listaMenuItem = (ListView) findViewById(R.id.listaMenuItem);
		listaMenuItem.setAdapter(myAdapter);
	}

}
