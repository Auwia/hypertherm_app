package it.app.hypertherm.activity;

import it.app.hypertherm.PatologiaListViewAdapter;
import it.app.hypertherm.R;
import it.app.hypertherm.thread.carica_configurazione_logo;
import it.app.hypertherm.util.Utility;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PatologiaActivity extends Activity {

	private PatologiaListViewAdapter myAdapter;

	private Utility utility;

	private ListView listaMenuItem;
	private Button button_up, button_down, button_ok, button_home;
	private TextView tessuto;

	private SharedPreferences preferences;

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;

		if (listView.getCount() >= 11) {

			for (int i = 0; i < 12; i++) {
				View listItem = listAdapter.getView(i, null, listView);
				listItem.measure(0, 0);
				if (listItem.getMeasuredWidth() > 700) {
					i++;
					totalHeight += listItem.getMeasuredHeight() * 85 / 100;
				}
				totalHeight += listItem.getMeasuredHeight();
			}

			ViewGroup.LayoutParams params = listView.getLayoutParams();
			params.height = totalHeight
					+ (listView.getDividerHeight() * (listAdapter.getCount() - 1))
					+ 10;
			listView.setLayoutParams(params);
			listView.requestLayout();
		}

	}

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
				if (WorkActivity.SIMULATORE) {
					intent.putExtra("DEMO", true);
				} else {

					intent.putExtra("DEMO", false);
				}
				startActivity(intent);
			}
		});

	runOnUiThread(new carica_configurazione_logo(this));

		listaMenuItem.setItemChecked(myAdapter.getItem(0).getMenuFlaggato() ? 0
				: 1, true);

		setListViewHeightBasedOnChildren(listaMenuItem);

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
