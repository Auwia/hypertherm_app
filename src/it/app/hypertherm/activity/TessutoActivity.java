package it.app.hypertherm.activity;

import it.app.hypertherm.R;
import it.app.hypertherm.TessutoListViewAdapter;
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

public class TessutoActivity extends Activity {

	private TessutoListViewAdapter myAdapter;

	private Utility utility;

	private ListView listaMenuItem;
	private Button button_up, button_down, button_ok, button_home;
	private TextView tessuto_label;

	private SharedPreferences preferences;

	@Override
	public void onPause() {
		super.onPause();

		finish();

	}

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

		tessuto_label = (TextView) findViewById(R.id.tessuto);
		tessuto_label.setText(utility.get_title_tessuto());

		import_menu_items();

		button_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				Intent intent = new Intent(TessutoActivity.this,
						MainActivity.class);
				startActivity(intent);

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

		new carica_configurazione_logo(this).execute();

		listaMenuItem.setItemChecked(0, true);
	}

	private void import_menu_items() {

		myAdapter = new TessutoListViewAdapter(this,
				utility.get_menu_items("TRATTAMENTI"));
		listaMenuItem.setAdapter(myAdapter);
	}

}
