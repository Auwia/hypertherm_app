package it.app.tcare;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Menu extends Activity {

	private Button energy, button_energy, continuos, button_time, pulsed,
			confirm, back, exit, service;
	private SeekBar seek_bar_frequency, seek_bar_energy;
	private TextView uno, due, tre, quattro, cinque, label_energy, revision;
	private LinearLayout simbolo_frequenza;
	private RelativeLayout barra_orizzontale;

	private String[] comando_da_inviare;

	private SharedPreferences preferences;

	private static final int REQUEST_CODE_TEST = 0;

	@Override
	public void finish() {

		if (comando_da_inviare != null) {
			Bundle b = new Bundle();
			b.putStringArray("comandi_da_eseguire", comando_da_inviare);
			Intent i = new Intent();
			i.putExtras(b);
			setResult(RESULT_OK, i);
		}

		preferences.edit().putBoolean("isMenu", false).commit();

		super.finish();
	}

	@Override
	protected void onResume() {
		// Ideally should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (preferences.contains("isEnergy")) {

			button_energy.setPressed(preferences.getBoolean("isEnergy", false));
			button_time.setPressed(preferences.getBoolean("isTime", false));
			pulsed.setPressed(preferences.getBoolean("isPulsed", false));
			continuos.setPressed(preferences.getBoolean("isContinuos", false));

			if (preferences.getBoolean("isEnergy", false)) {
				seek_bar_energy.setMax(40);
				seek_bar_energy
						.setProgress(preferences.getInt("energy", 10) / 5000);
				label_energy.setText(getResources()
						.getString(R.string.setValue));
				energy.setText(String.valueOf(preferences.getInt("energy", 10)));
			}

			if (preferences.getBoolean("isTime", false)) {
				seek_bar_energy.setMax(100);
				seek_bar_energy.setProgress(preferences.getInt(
						"timer_progress", 2));
				label_energy.setText(getResources().getString(
						R.string.label_time));
				energy.setText(preferences.getString("timer", getResources()
						.getString(R.string.time)));
			}

			if (preferences.getBoolean("isPulsed", false)) {
				barra_orizzontale.setVisibility(View.VISIBLE);
				seek_bar_frequency.setProgress(preferences.getInt("hz", 1) - 1);
				simbolo_frequenza.setVisibility(View.VISIBLE);
			}

			if (preferences.getBoolean("isContinuos", false)) {
				barra_orizzontale.setVisibility(View.INVISIBLE);
				simbolo_frequenza.setVisibility(View.INVISIBLE);
			}

		} else {
			button_time.setPressed(preferences.getBoolean("isTime", true));
			continuos.setPressed(preferences.getBoolean("isContinuos", true));
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_menu);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		preferences.edit().putBoolean("exit", false).commit();

		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e("TCARE", "ERRORE STRANO QUI! " + e.getMessage());
		}
		revision = (TextView) findViewById(R.id.revision);
		revision.setText(pInfo.versionName);

		button_energy = (Button) findViewById(R.id.button_energy);
		continuos = (Button) findViewById(R.id.button_continuos);
		pulsed = (Button) findViewById(R.id.button_pulsed);
		button_time = (Button) findViewById(R.id.button_time);
		energy = (Button) findViewById(R.id.energy);
		confirm = (Button) findViewById(R.id.button_confirm);
		back = (Button) findViewById(R.id.button_back);
		exit = (Button) findViewById(R.id.exit);
		service = (Button) findViewById(R.id.service);

		simbolo_frequenza = (LinearLayout) findViewById(R.id.simbolo_frequenza);
		barra_orizzontale = (RelativeLayout) findViewById(R.id.barra_orizzontale);

		seek_bar_energy = (SeekBar) findViewById(R.id.seek_bar_energy);
		seek_bar_frequency = (SeekBar) findViewById(R.id.seek_bar_frequency);

		label_energy = (TextView) findViewById(R.id.label_energy);
		uno = (TextView) findViewById(R.id.uno);
		due = (TextView) findViewById(R.id.due);
		tre = (TextView) findViewById(R.id.tre);
		quattro = (TextView) findViewById(R.id.quattro);
		cinque = (TextView) findViewById(R.id.cinque);

		seek_bar_frequency.setMax(4);

		service.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				preferences.edit().putBoolean("isService", true).commit();
				Intent intent = new Intent(Menu.this, Service.class);
				startActivityForResult(intent, REQUEST_CODE_TEST);
			}
		});

		exit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				preferences.edit().putBoolean("exit", true).commit();
				finish();
			}
		});

		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		confirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				List<String> list = new ArrayList<String>();

				if (button_energy.isPressed()) {
					list.add("M");
					list.add("j");
					list.add(String.valueOf(seek_bar_energy.getProgress() + 150));
				}

				if (button_time.isPressed()) {
					list.add("M");
					list.add("t");
					list.add(String.valueOf(seek_bar_energy.getProgress() + 150));
				}

				if (pulsed.isPressed()) {
					list.add("L");
					list.add(String.valueOf(seek_bar_frequency.getProgress() + 1));
				}

				if (continuos.isPressed()) {
					list.add("L");
					list.add("0");
				}

				comando_da_inviare = new String[list.size()];

				for (int i = 0; i < list.size(); i++) {
					comando_da_inviare[i] = list.get(i);
				}

				finish();
			}
		});

		seek_bar_energy
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {

						if (button_energy.isPressed()) {
							energy.setText(Integer.toString(progress * 5000));

						}

						if (button_time.isPressed()) {

							if (progress > 1) {

								if (((progress * 30) % 60) == 0)

									if (progress < 20)
										energy.setText("0"
												+ Integer
														.toString(progress / 2)
												+ "'00''");
									else
										energy.setText(Integer
												.toString(progress / 2)
												+ "'00''");
								else if (progress < 20)
									energy.setText("0"
											+ Integer
													.toString((progress - 1) / 2)
											+ "'30''");
								else
									energy.setText(Integer
											.toString((progress - 1) / 2)
											+ "'30''");

							} else {
								seek_bar_energy.setProgress(1);
							}
						}

					}

					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					public void onStopTrackingTouch(SeekBar seekBar) {

					}
				});

		Display display = getWindowManager().getDefaultDisplay();
		int width, height;
		width = display.getWidth();
		height = display.getHeight();
		android.view.ViewGroup.LayoutParams param = seek_bar_frequency
				.getLayoutParams();
		param.width = width * 35 / 100;

		uno.setWidth(width * 35 / 100 / 5);
		due.setWidth(width * 35 / 100 / 5);
		tre.setWidth(width * 35 / 100 / 5);
		quattro.setWidth(width * 35 / 100 / 5);
		cinque.setWidth(width * 35 / 100 / 5);

		param = seek_bar_energy.getLayoutParams();
		param.width = width * 60 / 100;

		pulsed.setPressed(false);
		pulsed.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					continuos.setPressed(false);
					pulsed.setPressed(true);
					simbolo_frequenza.setVisibility(View.VISIBLE);
					barra_orizzontale.setVisibility(View.VISIBLE);
					return true;
				}

				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
			}
		});

		continuos.setPressed(true);
		continuos.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					pulsed.setPressed(false);
					continuos.setPressed(true);
					simbolo_frequenza.setVisibility(View.INVISIBLE);
					barra_orizzontale.setVisibility(View.INVISIBLE);
					return true;
				}

				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
			}
		});

		button_time.setPressed(true);
		seek_bar_energy.setMax(100);
		seek_bar_energy.setProgress(2);
		button_time.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					seek_bar_energy.setProgress(2);
					seek_bar_energy.setMax(100);
					button_energy.setPressed(false);
					button_time.setPressed(true);
					label_energy.setText(getResources().getString(
							R.string.label_time));
					energy.setText(getResources().getString(R.string.time));
					return true;
				}

				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
			}
		});

		button_energy.setPressed(false);
		button_energy.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					seek_bar_energy.setMax(40);
					seek_bar_energy.setProgress(1);
					button_energy.setPressed(true);
					button_time.setPressed(false);
					label_energy.setText(getResources().getString(
							R.string.setValue));
					energy.setText(String.valueOf(seek_bar_energy.getProgress() * 5000));
					return true;
				}

				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
			}
		});

	}
}
