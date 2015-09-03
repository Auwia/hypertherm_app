package it.app.hypertherm.activity;

import it.app.hypertherm.R;
import it.app.hypertherm.util.Utility;
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
			button_articolare, button_superficiale, button_intermedio,
			button_profondo, button_dinamico, button_ok;

	private TextView struttura_label, profondita_label, tessuto_label;

	private String struttura, profondita;

	private Utility utility;

	private SharedPreferences preferences;

	@Override
	public void onPause() {
		super.onPause();

		finish();

	}

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
		button_superficiale = (Button) findViewById(R.id.button_superficiale);
		button_intermedio = (Button) findViewById(R.id.button_intermedio);
		button_profondo = (Button) findViewById(R.id.button_profondo);
		button_dinamico = (Button) findViewById(R.id.button_dinamico);
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

		button_superficiale.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_intermedio.isPressed()
							|| button_profondo.isPressed()
							|| button_dinamico.isPressed()) {

						if (button_superficiale.isPressed()) {
							button_superficiale.setPressed(false);
							button_intermedio.setPressed(true);
							button_profondo.setPressed(true);
							button_dinamico.setPressed(true);
						} else {
							button_superficiale.setPressed(true);
							button_intermedio.setPressed(false);
							button_profondo.setPressed(false);
							button_dinamico.setPressed(false);
							profondita = "1";
						}
					}
				}
				return true;
			}
		});

		button_intermedio.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_superficiale.isPressed()
							|| button_profondo.isPressed()
							|| button_dinamico.isPressed()) {

						if (button_intermedio.isPressed()) {
							button_intermedio.setPressed(false);
							button_superficiale.setPressed(true);
							button_profondo.setPressed(true);
							button_dinamico.setPressed(true);
						} else {
							button_intermedio.setPressed(true);
							button_superficiale.setPressed(false);
							button_profondo.setPressed(false);
							button_dinamico.setPressed(false);
							profondita = "2";
						}
					}
				}
				return true;
			}
		});

		button_profondo.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_superficiale.isPressed()
							|| button_intermedio.isPressed()
							|| button_dinamico.isPressed()) {

						if (button_profondo.isPressed()) {
							button_profondo.setPressed(false);
							button_superficiale.setPressed(true);
							button_intermedio.setPressed(true);
							button_dinamico.setPressed(true);
						} else {
							button_profondo.setPressed(true);
							button_superficiale.setPressed(false);
							button_intermedio.setPressed(false);
							button_dinamico.setPressed(false);
							profondita = "3";
						}
					}
				}
				return true;
			}
		});

		button_dinamico.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_superficiale.isPressed()
							|| button_intermedio.isPressed()
							|| button_profondo.isPressed()) {

						if (button_dinamico.isPressed()) {
							button_dinamico.setPressed(false);
							button_superficiale.setPressed(true);
							button_intermedio.setPressed(true);
							button_profondo.setPressed(true);
						} else {
							button_dinamico.setPressed(true);
							button_superficiale.setPressed(false);
							button_intermedio.setPressed(false);
							button_profondo.setPressed(false);
							profondita = "4";
						}
					}
				}
				return true;
			}
		});

		button_ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				preferences.edit().putString("PROFONDITA", profondita).commit();
				preferences.edit().putString("STRUTTURA", struttura).commit();

				preferences
						.edit()
						.putString(
								"MENU_ITEM",
								struttura
										+ " - "
										+ utility.getProfonditaLabel(struttura,
												profondita)).commit();

				if (profondita.equals("4")) {

					preferences
							.edit()
							.putFloat("WATER",
									utility.getWaterTemperature(struttura, "1"))
							.commit();
					preferences
							.edit()
							.putFloat("DELTAT",
									utility.getDeltaT(struttura, "1")).commit();
					preferences
							.edit()
							.putInt("ANTENNA",
									utility.getAntenna(struttura, "1"))
							.commit();
					preferences.edit()
							.putInt("TIME", utility.getTime(struttura, "1"))
							.commit();

				} else {

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
					preferences
							.edit()
							.putInt("TIME",
									utility.getTime(struttura, profondita))
							.commit();

				}

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
		button_intermedio.setPressed(true);

	}
}
