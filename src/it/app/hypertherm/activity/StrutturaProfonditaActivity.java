package it.app.hypertherm.activity;

import it.app.hypertherm.R;
import it.app.hypertherm.Utility;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class StrutturaProfonditaActivity extends Activity {

	private Button button_home, button_muscolare, button_mix,
			button_articolare, button_uno, button_due, button_tre,
			button_quattro, button_ok;

	private TextView struttura_label, profondita_label, tessuto_label;

	private String struttura, profondita;

	private Utility utility;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_struttura_profondita);

		utility = new Utility();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		button_home = (Button) findViewById(R.id.button_home);
		button_muscolare = (Button) findViewById(R.id.button_muscolare);
		button_mix = (Button) findViewById(R.id.button_mix);
		button_articolare = (Button) findViewById(R.id.button_articolare);
		button_uno = (Button) findViewById(R.id.button_uno);
		button_due = (Button) findViewById(R.id.button_due);
		button_tre = (Button) findViewById(R.id.button_tre);
		button_quattro = (Button) findViewById(R.id.button_quattro);
		button_ok = (Button) findViewById(R.id.button_ok);

		String[] struttura_array = utility.getStrutturaItems();

		button_muscolare.setText(struttura_array[0]);
		button_mix.setText(struttura_array[1]);
		button_articolare.setText(struttura_array[2]);

		struttura_label = (TextView) findViewById(R.id.struttura);
		profondita_label = (TextView) findViewById(R.id.profondita);
		tessuto_label = (TextView) findViewById(R.id.tessuto);

		struttura_label.setText(utility.get_label_struttura());
		profondita_label.setText(utility.get_label_profondita());
		tessuto_label.setText(utility.get_title_struttura_profondita());

		button_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		button_muscolare.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_mix.isPressed() || button_articolare.isPressed()) {

						if (button_muscolare.isPressed()) {
							button_muscolare.setPressed(false);
							button_mix.setPressed(true);
							button_articolare.setPressed(true);
						} else {
							button_muscolare.setPressed(true);
							button_mix.setPressed(false);
							button_articolare.setPressed(false);
							utility.appendLog("CIAO:"
									+ button_muscolare.getText().toString());
							struttura = button_muscolare.getText().toString();
						}
					}
				}
				return true;
			}
		});

		button_mix.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_muscolare.isPressed()
							|| button_articolare.isPressed()) {

						if (button_mix.isPressed()) {
							button_mix.setPressed(false);
							button_muscolare.setPressed(true);
							button_articolare.setPressed(true);
						} else {
							button_mix.setPressed(true);
							button_muscolare.setPressed(false);
							button_articolare.setPressed(false);
							struttura = button_mix.getText().toString();
						}
					}
				}

				return true;
			}
		});

		button_articolare.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (button_muscolare.isPressed() || button_mix.isPressed()) {

					if (event.getAction() == MotionEvent.ACTION_UP) {
						if (button_articolare.isPressed()) {
							button_articolare.setPressed(false);
							button_mix.setPressed(true);
							button_muscolare.setPressed(true);
						} else {
							button_articolare.setPressed(true);
							button_mix.setPressed(false);
							button_muscolare.setPressed(false);
							struttura = button_articolare.getText().toString();
						}
					}
				}

				return true;
			}
		});

		button_uno.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_due.isPressed() || button_tre.isPressed()
							|| button_quattro.isPressed()) {

						if (button_uno.isPressed()) {
							button_uno.setPressed(false);
							button_due.setPressed(true);
							button_tre.setPressed(true);
							button_quattro.setPressed(true);
						} else {
							button_uno.setPressed(true);
							button_due.setPressed(false);
							button_tre.setPressed(false);
							button_quattro.setPressed(false);
							profondita = "1";
						}
					}
				}
				return true;
			}
		});

		button_due.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_uno.isPressed() || button_tre.isPressed()
							|| button_quattro.isPressed()) {

						if (button_due.isPressed()) {
							button_due.setPressed(false);
							button_uno.setPressed(true);
							button_tre.setPressed(true);
							button_quattro.setPressed(true);
						} else {
							button_due.setPressed(true);
							button_uno.setPressed(false);
							button_tre.setPressed(false);
							button_quattro.setPressed(false);
							profondita = "2";
						}
					}
				}
				return true;
			}
		});

		button_tre.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_uno.isPressed() || button_due.isPressed()
							|| button_quattro.isPressed()) {

						if (button_tre.isPressed()) {
							button_tre.setPressed(false);
							button_uno.setPressed(true);
							button_due.setPressed(true);
							button_quattro.setPressed(true);
						} else {
							button_tre.setPressed(true);
							button_uno.setPressed(false);
							button_due.setPressed(false);
							button_quattro.setPressed(false);
							profondita = "2";
						}
					}
				}
				return true;
			}
		});

		button_quattro.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_uno.isPressed() || button_due.isPressed()
							|| button_tre.isPressed()) {

						if (button_quattro.isPressed()) {
							button_quattro.setPressed(false);
							button_uno.setPressed(true);
							button_due.setPressed(true);
							button_tre.setPressed(true);
						} else {
							button_quattro.setPressed(true);
							button_uno.setPressed(false);
							button_due.setPressed(false);
							button_tre.setPressed(false);
							profondita = "4";
						}
					}
				}
				return true;
			}
		});

		button_ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				preferences
						.edit()
						.putFloat(
								"WATER",
								utility.getWaterTemperature(struttura,
										profondita)).commit();
				preferences
						.edit()
						.putFloat("DELTAT",
								utility.getDeltaT(struttura, profondita))
						.commit();
				preferences
						.edit()
						.putInt("ANTENNA",
								utility.getAntenna(struttura, profondita))
						.commit();
				preferences.edit()
						.putInt("TIME", utility.getTime(struttura, profondita))
						.commit();

				preferences
						.edit()
						.putString(
								"MENU_ITEM",
								struttura
										+ " - "
										+ utility.getProfonditaLabel(struttura,
												profondita)).commit();

				Intent intent = new Intent(StrutturaProfonditaActivity.this,
						WorkActivity.class);
				startActivity(intent);
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();

		struttura = button_mix.getText().toString();
		profondita = "1";

		button_mix.setPressed(true);
		button_uno.setPressed(true);

	}
}
