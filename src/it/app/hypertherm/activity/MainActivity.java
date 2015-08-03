package it.app.hypertherm.activity;

import it.app.hypertherm.MenuListViewAdapter;
import it.app.hypertherm.R;
import it.app.hypertherm.Utility;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends Activity {

	private MenuListViewAdapter myAdapter;

	private Utility utility;

	private ListView listaMenuItem;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		utility = new Utility(this);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.edit().putBoolean("isMenu", true).commit();
		preferences.edit().putBoolean("isTessuto", false).commit();

		import_menu_items();

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
			startActivity(intent);

			break;
		case 1:
			intent = new Intent(MainActivity.this,
					StrutturaProfonditaActivity.class);
			startActivity(intent);
			break;
		case 2:

			intent = new Intent(MainActivity.this, WorkActivity.class);
			startActivity(intent);

			break;
		case 3:

			intent = new Intent(MainActivity.this, WorkActivity.class);
			startActivity(intent);

			break;
		case 4:
			break;
		}

	}

	private void import_menu_items() {

		myAdapter = new MenuListViewAdapter(this,
				utility.get_menu_items("MENU"));
		listaMenuItem = (ListView) findViewById(R.id.listaMenuItem);
		listaMenuItem.setAdapter(myAdapter);
	}

}
