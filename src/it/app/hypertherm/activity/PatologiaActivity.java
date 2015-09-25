package it.app.hypertherm.activity;

import it.app.hypertherm.PatologiaListViewAdapter;
import it.app.hypertherm.R;
import it.app.hypertherm.util.Utility;
import it.app.hypertherm.util.carica_configurazione_logo;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class PatologiaActivity extends Activity {

	private PatologiaListViewAdapter myAdapter;

	private Utility utility;

	private ListView listaMenuItem;
	private Button button_up, button_down, button_ok, button_home;
	private TextView tessuto;

	private SharedPreferences preferences;

	@Override
	public void onPause() {
		super.onPause();

		finish();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patologia);

		utility = new Utility(this);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		listaMenuItem = (ListView) findViewById(R.id.listaMenuItem);

		button_up = (Button) findViewById(R.id.button_up);
		button_down = (Button) findViewById(R.id.button_down);
		button_ok = (Button) findViewById(R.id.button_ok);
		button_home = (Button) findViewById(R.id.button_home);

		tessuto = (TextView) findViewById(R.id.tessuto);
		tessuto.setText(utility.get_title_patologia());

		import_menu_items();

		button_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(PatologiaActivity.this,
						MainActivity.class);
				startActivity(intent);
			}
		});

		button_up.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				int position = listaMenuItem.getCheckedItemPosition();

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

							listaMenuItem.setSelection(listaMenuItem
									.getCheckedItemPosition() - 2);

							return;

						} else {
							return;
						}
					} else {

						if (position - 1 >= 0) {

							listaMenuItem.setItemChecked(
									listaMenuItem.getCheckedItemPosition() - 1,
									true);

							listaMenuItem.setSelection(listaMenuItem
									.getCheckedItemPosition() - 1);

							return;

						} else {
							return;
						}
					}
				} else {
					return;
				}

			}
		});

		button_down.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				int position = listaMenuItem.getCheckedItemPosition();

				if (position == -1) {
					// position = 0;
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

							listaMenuItem.setSelection(listaMenuItem
									.getCheckedItemPosition());

							return;

						} else {
							return;
						}
					} else {
						if (position + 1 <= listaMenuItem.getCount() - 1) {

							listaMenuItem.setItemChecked(
									listaMenuItem.getCheckedItemPosition() + 1,
									true);

							listaMenuItem.setSelection(listaMenuItem
									.getCheckedItemPosition());

							return;

						} else {
							return;
						}
					}
				} else {
					return;
				}
			}
		});

		button_ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				preferences
						.edit()
						.putFloat(
								"WATER",
								utility.getWaterTemperature(myAdapter.getItem(
										listaMenuItem.getCheckedItemPosition())
										.getItem())).commit();
				preferences
						.edit()
						.putFloat(
								"DELTAT",
								utility.getDeltaT(myAdapter.getItem(
										listaMenuItem.getCheckedItemPosition())
										.getItem())).commit();
				preferences
						.edit()
						.putInt("ANTENNA",
								utility.getAntenna(myAdapter.getItem(
										listaMenuItem.getCheckedItemPosition())
										.getItem())).commit();
				preferences
						.edit()
						.putInt("TIME",
								utility.getTime(myAdapter.getItem(
										listaMenuItem.getCheckedItemPosition())
										.getItem())).commit();

				preferences
						.edit()
						.putString(
								"MENU_ITEM",
								myAdapter.getItem(
										listaMenuItem.getCheckedItemPosition())
										.getItem()).commit();

				Intent intent = new Intent(PatologiaActivity.this,
						WorkActivity.class);
				startActivity(intent);
			}
		});

		new carica_configurazione_logo(this).execute();

		listaMenuItem.setItemChecked(myAdapter.getItem(0).getMenuFlaggato() ? 0
				: 1, true);

	}

	private void import_menu_items() {

		myAdapter = new PatologiaListViewAdapter(this,
				utility.get_menu_items("PATOLOGIE"));
		listaMenuItem.setAdapter(myAdapter);
	}

	@Override
	public void onStop() {
		super.onStop();

		finish();

	}

}
