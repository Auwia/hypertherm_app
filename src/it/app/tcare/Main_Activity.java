package it.app.tcare;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Main_Activity extends Activity {

	private TextView label_start, label_pause, label_stop, title, title2,
			percentage, percentuale_simbolo, duty, time, zero, dieci, venti,
			trenta, quaranta, cinquanta, sessanta, settanta, ottanta, novanta,
			cento;
	private Button play, stop, pause, cap, res, body, face, menu, energy,
			frequency, continuos;
	private SeekBar seek_bar_percentage;

	public static Utility utility;

	private static SharedPreferences preferences;

	public static Activity activity;

	private static final int REQUEST_CODE_TEST = 0;

	public static boolean start_in_progress = false;
	public static boolean start_lettura = true;
	private boolean bConfiged = false;

	public String act_string;

	private PowerManager pm;
	private static PowerManager.WakeLock wl;

	// VARIABILI DATA BASE
	private static final String DATABASE_NAME = "TCaReDB.db";
	private static SQLiteDatabase database;
	private TCaReDataSource datasource;
	private Cursor cur;

	public static final Handler aggiorna_tempo_lavoro_db = new Handler() {

		public void handleMessage(Message msg) {

			database.execSQL("update WORK_TIME set WORK_FROM=WORK_FROM+1;");
		}
	};

	public static final Handler handler_reset_work_time_db = new Handler() {

		public void handleMessage(Message msg) {

			database.execSQL("update WORK_TIME set WORK_FROM=0;");
		}
	};

	public static final Handler handler_save_settings_db = new Handler() {

		public void handleMessage(Message msg) {

			if (preferences.getBoolean("isSmart", false)) {
				Log.d("TCARE", "update SETTINGS set SMART=1, PHYSIO=0;");
				database.execSQL("update SETTINGS set SMART=1, PHYSIO=0;");
			}

			if (preferences.getBoolean("isPhysio", false)) {
				Log.d("TCARE", "update SETTINGS set SMART=0, PHYSIO=1;");
				database.execSQL("update SETTINGS set SMART=0, PHYSIO=1;");
			}

			String query = "update SETTINGS set LANGUAGE='"
					+ preferences.getString("language", "en") + "';";
			Log.d("TCARE", query);
			database.execSQL(query);
		}
	};

	public static final Handler handler_save_serial_number_db = new Handler() {

		public void handleMessage(Message msg) {

			Log.d("TCARE",
					"update SETTINGS set SERIAL_NUMBER='"
							+ preferences.getString("serial_number",
									"SN DEFAULT") + "';");
			database.execSQL("update SETTINGS set SERIAL_NUMBER='"
					+ preferences.getString("serial_number", "SN DEFAULT")
					+ "';");
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d("TCARE", "Chiamo onActivityResult, la lettura e': "
				+ start_lettura);

		if (preferences.getBoolean("isSmart", false)) {
			cap.setVisibility(View.INVISIBLE);
			res.setVisibility(View.INVISIBLE);
		}

		if (preferences.getBoolean("isPhysio", false)) {
			cap.setVisibility(View.VISIBLE);
			res.setVisibility(View.VISIBLE);
		}

		if (preferences.getBoolean("exit", false))
			finish();

		// utility.ResumeAccessory(bConfiged);

		if (requestCode == REQUEST_CODE_TEST) {
			if (resultCode == Activity.RESULT_OK) {
				if (data.hasExtra("comandi_da_eseguire")) {

					Bundle b = data.getExtras();
					String[] array = b.getStringArray("comandi_da_eseguire");

					for (int i = 0; i < array.length; i++) {
						if (array[i].length() > 1) {
							utility.MandaDati(Integer.valueOf(array[i]));
						} else {
							utility.writeData(array[i]);
						}
					}
				}
			}
		}

		utility.writeData("a");
	}

	@Override
	protected void onPause() {
		super.onPause();

		datasource.close();
		Log.d("TCARE", "Chiamo onPause, la lettura e': " + start_lettura);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		start_lettura = false;
		FT311UARTInterface.READ_ENABLE = false;

		Log.d("TCARE", "Chiamo onDestroy, la lettura e': " + start_lettura);

		utility.DestroyAccessory(true);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		wl.release();

		System.exit(0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);

		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "TCARE");

		wl.acquire();

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		preferences.edit().putBoolean("isMenu", false).commit();

		database = openOrCreateDatabase(DATABASE_NAME,
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
		datasource = new TCaReDataSource(getApplicationContext());
		datasource.open();

		cur = database.query("SETTINGS", new String[] { "SMART", "PHYSIO",
				"SERIAL_NUMBER", "LANGUAGE" }, null, null, null, null, null);

		cur.moveToFirst();
		Boolean smart = null, physio = null;
		String serial_number = null, language = null;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			smart = cur.getInt(0) > 0;
			physio = cur.getInt(1) > 0;
			serial_number = cur.getString(2);
			language = cur.getString(3);
			cur.moveToNext();
		}
		cur.close();

		preferences.edit().putBoolean("isSmart", smart).commit();
		preferences.edit().putBoolean("isPhysio", physio).commit();
		preferences.edit().putString("serial_number", serial_number).commit();
		preferences.edit().putString("language", language).commit();

		Resources resource = getResources();
		DisplayMetrics dm = resource.getDisplayMetrics();
		android.content.res.Configuration conf = resource.getConfiguration();
		conf.locale = new Locale(preferences.getString("language", "en"));
		resource.updateConfiguration(conf, dm);
		setContentView(R.layout.main_activity_layout);

		activity = this;

		utility = new Utility(this);

		title = (TextView) findViewById(R.id.title);
		title2 = (TextView) findViewById(R.id.title2);

		percentuale_simbolo = (TextView) findViewById(R.id.percentuale_simbolo);

		duty = (TextView) findViewById(R.id.duty);
		time = (TextView) findViewById(R.id.time);

		continuos = (Button) findViewById(R.id.button_continuos);
		continuos.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (preferences.getBoolean("isPlaying", false))
					utility.writeData("P");

				if (preferences.getBoolean("isContinuos", false)) {
					if (preferences.getInt("hz", 1) == 0) {
						utility.writeData("1");
					} else {
						utility.writeData(String.valueOf(preferences.getInt(
								"hz", 1)));
					}
				} else if (preferences.getBoolean("isPulsed", false)) {
					utility.writeData("0");
				} else {
					utility.writeData("1");
				}

			}
		});

		frequency = (Button) findViewById(R.id.frequency);
		frequency.setTag(R.drawable.button_457);
		frequency.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (preferences.getBoolean("isPlaying", false))
					utility.writeData("P");

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
		label_start.setTextSize(16);

		label_stop = (TextView) findViewById(R.id.label_stop);
		label_stop.setTextSize(16);

		label_pause = (TextView) findViewById(R.id.label_pause);
		label_pause.setTextSize(16);

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

					if (preferences.getBoolean("isPlaying", false))
						utility.writeData("P");

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

					if (preferences.getBoolean("isPlaying", false))
						utility.writeData("P");

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

					if (preferences.getBoolean("isPlaying", false))
						utility.writeData("P");

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

					if (preferences.getBoolean("isPlaying", false))
						utility.writeData("P");

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
		// label_start.setTextSize(width * moltiplicativo / 100 / 2);
		// label_stop.setTextSize(width * moltiplicativo / 100 / 2);
		// label_pause.setTextSize(width * moltiplicativo / 100 / 2);

		menu = (Button) findViewById(R.id.menu);
		menu.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (!preferences.getBoolean("isMenu", false)) {

						utility.writeData("a");

						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}

						preferences.edit().putBoolean("isMenu", true).commit();
						Intent intent = new Intent(Main_Activity.this,
								Menu.class);
						startActivityForResult(intent, REQUEST_CODE_TEST);

						cur = database.query("WORK_TIME",
								new String[] { "WORK_FROM" }, null, null, null,
								null, null);

						cur.moveToFirst();

						int work_from = 0;

						while (cur.getCount() > 0 && !cur.isAfterLast()) {
							work_from = cur.getInt(0);
							cur.moveToNext();
						}

						cur.close();

						cur = database.query("PASSWORD",
								new String[] { "PWD" }, null, null, null, null,
								null);

						cur.moveToFirst();

						String password = null;

						while (cur.getCount() > 0 && !cur.isAfterLast()) {
							password = cur.getString(0);
							cur.moveToNext();
						}

						cur.close();

						preferences.edit().putInt("work_time", work_from)
								.commit();
						preferences.edit().putString("password", password)
								.commit();

					}
					return true;
				}

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

		// energy.setWidth((int) (blocco2_dim * moltiplicativo));
		// energy.setHeight((int) (blocco2_dim * moltiplicativo / 0.40));

		if (preferences.getBoolean("isSmart", false)) {
			cap.setVisibility(View.INVISIBLE);
			res.setVisibility(View.INVISIBLE);
		}

		if (preferences.getBoolean("isPhysio", false)) {
			cap.setVisibility(View.VISIBLE);
			res.setVisibility(View.VISIBLE);
		}

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}

		utility.config(this);

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}

		act_string = getIntent().getAction();
		if (-1 != act_string.indexOf("android.intent.action.MAIN")) {
			restorePreference();
		} else if (-1 != act_string
				.indexOf("android.hardware.usb.action.USB_ACCESSORY_ATTACHED")) {
			cleanPreference();
		}

		if (false == bConfiged) {
			bConfiged = true;
			utility.SetConfig();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}

			savePreference();
		}

		utility.ResumeAccessory(bConfiged);
		// utility.SetConfig();
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
		}

		utility.writeData("@");
		utility.writeData("^");
		utility.writeData("a");
		utility.writeData("?");

	}

	protected void cleanPreference() {
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove("configed");
		editor.commit();
	}

	protected void savePreference() {
		if (true == bConfiged) {
			preferences.edit().putString("configed", "TRUE").commit();
		} else {
			preferences.edit().putString("configed", "FALSE").commit();
		}

		preferences.edit().putBoolean("exit", false).commit();

	}

	protected void restorePreference() {
		String key_name = preferences.getString("configed", "");
		if (true == key_name.contains("TRUE")) {
			bConfiged = true;
		} else {
			bConfiged = false;
		}
	}

	// @Override
	public void onHomePressed() {
		onBackPressed();
	}

	public void onBackPressed() {
		super.onBackPressed();

		start_lettura = false;
		Main_Activity.start_lettura = false;

		Log.d("TCARE", "Chiamo onBackPressed, la lettura e': " + start_lettura);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		utility.DestroyAccessory(true);

		System.exit(0);
	}

	@Override
	protected void onResume() {
		super.onResume();

		datasource.open();

		if (2 == utility.ResumeAccessory(bConfiged)) {
			cleanPreference();
			restorePreference();
		}

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
		}

	}

	@Override
	protected void onStop() {

		super.onStop();
	}

}
