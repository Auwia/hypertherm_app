package it.app.tcare;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableRow;
import android.widget.TextView;

public class Main_Activity extends Activity {

	private TextView label_start, label_pause, label_stop, title, title2,
			percentage, percentuale_simbolo, duty, time, zero, dieci, venti,
			trenta, quaranta, cinquanta, sessanta, settanta, ottanta, novanta,
			cento, label_continuos;
	private Button play, stop, pause, cap, res, body, face, menu, energy,
			frequency, continuos, jaule;
	private SeekBar seek_bar_percentage;

	private Utility utility;

	private TableRow pannello_energia;

	private SharedPreferences preferences;

	public static Activity activity;

	@Override
	protected void onResume() {
		// Ideally should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();

		utility.ResumeAccessory();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (preferences.contains("isEnergy")) {

			if (preferences.getBoolean("isEnergy", false)) {
				pannello_energia.setVisibility(View.VISIBLE);
				if (jaule != null) {
					jaule.setPressed(true);
					jaule.setText(String.valueOf(preferences.getInt("energy",
							10) * 5000));
					utility.writeData("M");
					utility.writeData("j");
					utility.MandaDati(preferences.getInt("energy", 1) + 150);
				}
			}

			if (preferences.getBoolean("isTime", false)) {
				pannello_energia.setVisibility(View.GONE);
				time.setText(preferences.getString("timer", getResources()
						.getString(R.string.time)));
				utility.writeData("M");
				utility.writeData("t");
				utility.MandaDati(preferences.getInt("timer_progress", 1) + 150);

			}

			if (preferences.getBoolean("isPulsed", false)) {
				continuos.setBackgroundResource(R.drawable.pulsed_normal);
				label_continuos.setVisibility(View.VISIBLE);
				label_continuos.setText(" "
						+ String.valueOf(preferences.getInt("hz", 1)) + " hz");
			}

			if (preferences.getBoolean("isContinuos", false)) {
				continuos.setBackgroundResource(R.drawable.continuos_normal);
				label_continuos.setVisibility(View.GONE);
			}

			stop.setPressed(true);
		}

		utility.writeData("L");

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
		utility.DestroyAccessory(true);

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			android.os.Process.killProcess(android.os.Process.myPid());
		}

		super.onDestroy();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);

		activity = this;

		pannello_energia = (TableRow) findViewById(R.id.pannello_energia);

		label_continuos = (TextView) findViewById(R.id.label_continuos);

		utility = new Utility(this);

		title = (TextView) findViewById(R.id.title);
		title2 = (TextView) findViewById(R.id.title2);

		percentuale_simbolo = (TextView) findViewById(R.id.percentuale_simbolo);

		duty = (TextView) findViewById(R.id.duty);
		time = (TextView) findViewById(R.id.time);

		continuos = (Button) findViewById(R.id.button_continuos);
		continuos.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (label_continuos.getVisibility() == View.VISIBLE) {
					utility.writeData("0");
				} else {
					utility.writeData(label_continuos.getText()
							.subSequence(1, 2).toString());
				}
			}
		});

		frequency = (Button) findViewById(R.id.frequency);
		frequency.setTag(R.drawable.button_457);
		frequency.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				switch ((Integer) frequency.getTag()) {
				case R.drawable.button_457:
					utility.writeData("s");
					break;
				case R.drawable.button_571:
					utility.writeData("m");
					break;
				case R.drawable.button_714:
					utility.writeData("q");
					break;
				case R.drawable.button_145:
					utility.writeData("c");
					break;

				}
			}

		});

		energy = (Button) findViewById(R.id.energy);
		energy.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					return true;
				}

				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
			}
		});

		percentage = (TextView) findViewById(R.id.percentage);
		percentage.setText("0");

		seek_bar_percentage = (SeekBar) findViewById(R.id.seek_bar_percentage);
		seek_bar_percentage.setMax(100);
		seek_bar_percentage
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {

						percentage.setText(Integer.toString(progress));

					}

					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					public void onStopTrackingTouch(SeekBar seekBar) {

						utility.MandaDati(Integer.parseInt(percentage.getText()
								.toString()) + 150);

					}
				});

		label_start = (TextView) findViewById(R.id.label_start);
		label_start.setTextSize(18);

		label_stop = (TextView) findViewById(R.id.label_stop);
		label_stop.setTextSize(18);

		label_pause = (TextView) findViewById(R.id.label_pause);
		label_pause.setTextSize(18);

		time = (TextView) findViewById(R.id.time);

		cap = (Button) findViewById(R.id.cap);
		cap.setPressed(true);

		play = (Button) findViewById(R.id.button_play);
		play.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					utility.writeData("S");
					return true;
				}

				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP) {
					return false;
				}

				return true;
			}
		});

		stop = (Button) findViewById(R.id.button_stop);
		stop.setPressed(true);
		label_stop.setTextColor(Color.parseColor("#78d0d2"));
		stop.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					utility.writeData("T");
					return true;
				}
				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP) {
					return false;
				}

				return true;
			}
		});

		pause = (Button) findViewById(R.id.button_pause);
		pause.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					utility.writeData("P");
					return true;
				}
				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP) {
					return false;
				}

				return true;
			}
		});

		cap = (Button) findViewById(R.id.cap);
		cap.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					utility.writeData("C");
					return true;
				}
				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP)
					return false;

				return true;
			}
		});

		res = (Button) findViewById(R.id.res);
		res.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					utility.writeData("R");
					return true;
				}
				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP)
					return false;

				return true;
			}
		});

		body = (Button) findViewById(R.id.body);
		body.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					utility.writeData("B");
					return true;
				}
				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP)
					return false;

				return true;
			}
		});

		face = (Button) findViewById(R.id.face);
		face.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					utility.writeData("F");
					return true;
				}
				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP)
					return false;

				return true;
			}
		});

		zero = (TextView) findViewById(R.id.zero);
		dieci = (TextView) findViewById(R.id.dieci);
		venti = (TextView) findViewById(R.id.venti);
		trenta = (TextView) findViewById(R.id.trenta);
		quaranta = (TextView) findViewById(R.id.quaranta);
		cinquanta = (TextView) findViewById(R.id.cinquanta);
		sessanta = (TextView) findViewById(R.id.sessanta);
		settanta = (TextView) findViewById(R.id.settanta);
		ottanta = (TextView) findViewById(R.id.ottanta);
		novanta = (TextView) findViewById(R.id.novanta);
		cento = (TextView) findViewById(R.id.cento);

		Display display = getWindowManager().getDefaultDisplay();

		int width, height;

		width = display.getWidth();
		height = display.getHeight();

		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);

		float density = getResources().getDisplayMetrics().density;

		int moltiplicativo = 0;
		if (density == 1)
			moltiplicativo = 5;

		if (density == 3)
			moltiplicativo = 2;

		if (density == 1.5)
			moltiplicativo = 4;

		title.setTextSize(width * moltiplicativo / 100);
		title2.setTextSize(width * moltiplicativo / 100);

		int blocco1_dim = (int) (width * 30 / 100 / 2.2);
		face.setWidth(blocco1_dim);
		body.setWidth(blocco1_dim);
		res.setWidth(blocco1_dim);
		cap.setWidth(blocco1_dim);
		// per mantenere le proporzioni: altezza = 35% larghezza
		face.setHeight(blocco1_dim * 35 / 100);
		body.setHeight(blocco1_dim * 35 / 100);
		res.setHeight(blocco1_dim * 35 / 100);
		cap.setHeight(blocco1_dim * 35 / 100);

		final int blocco2_dim = (int) (width * 50 / 100 / 5);
		label_start.setWidth(blocco2_dim);
		label_stop.setWidth(blocco2_dim);
		label_pause.setWidth(blocco2_dim);
		label_start.setTextSize(width * moltiplicativo / 100 / 2);
		label_stop.setTextSize(width * moltiplicativo / 100 / 2);
		label_pause.setTextSize(width * moltiplicativo / 100 / 2);

		menu = (Button) findViewById(R.id.menu);
		menu.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					utility.DestroyAccessory(true);
					Intent intent = new Intent(Main_Activity.this, Menu.class);
					startActivity(intent);
					return true;
				}

				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
			}
		});

		menu.setWidth(blocco2_dim);
		menu.setHeight(blocco2_dim);

		percentage.setTextSize(height / 2 * 20 / 100 / density);
		percentuale_simbolo.setTextSize(height / 2 * 20 / 100 / density);
		duty.setTextSize(height / 2 * 20 / 100 / density / 2);
		time.setTextSize(height / 2 * 20 / 100 / density);

		android.view.ViewGroup.LayoutParams param = seek_bar_percentage
				.getLayoutParams();
		param.width = width * 70 / 100;

		int padding = (int) (width * 70 / 100 / (11));

		zero.setWidth(padding);
		dieci.setWidth(padding);
		venti.setWidth(padding);
		trenta.setWidth(padding);
		quaranta.setWidth(padding);
		cinquanta.setWidth(padding);
		sessanta.setWidth(padding);
		settanta.setWidth(padding);
		ottanta.setWidth(padding);
		novanta.setWidth(padding);
		cento.setWidth(padding);

		energy.setWidth((int) (blocco2_dim * moltiplicativo));
		energy.setHeight((int) (blocco2_dim * moltiplicativo / 0.40));

		jaule = (Button) findViewById(R.id.jaule);
		jaule.setPressed(true);

		utility.config(this);
		utility.writeData("@^");
	}

	@Override
	public void onStart() {
		super.onStart();

	}
}
