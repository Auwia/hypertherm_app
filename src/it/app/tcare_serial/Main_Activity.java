package it.app.tcare_serial;

import it.app.tcare.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.util.LinkedList;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dwin.navy.serialportapi.SerialPortOpt;

public class Main_Activity extends Activity {

	public static Activity activity;

	private TextView label_start, label_pause, label_stop, title, title2,
			percentage, percentuale_simbolo, duty, time, zero, dieci, venti,
			trenta, quaranta, cinquanta, sessanta, settanta, ottanta, novanta,
			cento;
	private Button play, stop, pause, cap, res, body, face, menu, energy,
			frequency, continuos;
	private SeekBar seek_bar_percentage;

	public static Utility utility;

	private static SharedPreferences preferences;

	private static final int REQUEST_CODE_TEST = 0;

	private boolean bConfiged = false;

	public String act_string;

	// VARIABILI DATA BASE
	private static final String DATABASE_NAME = "TCaReDB.db";
	private static SQLiteDatabase database;
	private TCaReDataSource datasource;
	private Cursor cur;

	private SerialPortOpt serialPort;
	private LinkedList<byte[]> byteLinkedList = new LinkedList();

	private InputStream mInputStream;
	private ReadThread mReadThread;

	private byte[] writeusbdata = new byte[256];

	public static write_thread writeThread;

	private void initSerialPort() {
		serialPort = new SerialPortOpt();
		serialPort.mDevNum = 0;
		serialPort.mDataBits = 8;
		serialPort.mSpeed = 115200;
		serialPort.mStopBits = 1;
		serialPort.mParity = 'n';
		serialPort.openDev(serialPort.mDevNum);
		serialPort.setSpeed(serialPort.mFd, serialPort.mSpeed);
		serialPort.setParity(serialPort.mFd, serialPort.mDataBits,
				serialPort.mStopBits, serialPort.mParity);

		mInputStream = this.serialPort.getInputStream();
		mReadThread = new ReadThread();
		mReadThread.start();

	}

	protected void onDataReceived() {
		runOnUiThread(new Runnable() {
			public void run() {
				byte[] arrayOfByte;
				int i;

				arrayOfByte = (byte[]) Main_Activity.this.byteLinkedList.poll();
				i = arrayOfByte.length;
				Log.i("TCARE", new String(arrayOfByte, 0, i));
				Toast.makeText(getApplicationContext(),
						new String(arrayOfByte, 0, i), Toast.LENGTH_LONG)
						.show();

			}
		});
	}

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

		Log.d("TCARE", "MAIN ACTIVITY: SONO IN onActivityResult");

		if (preferences.getBoolean("isSmart", false)) {
			cap.setVisibility(View.INVISIBLE);
			res.setVisibility(View.INVISIBLE);
			title2.setText(getResources().getString(R.string.title2_smart));
		}

		if (preferences.getBoolean("isPhysio", false)) {
			cap.setVisibility(View.VISIBLE);
			res.setVisibility(View.VISIBLE);
			title2.setText(getResources().getString(R.string.title2_physio));
		}

		if (requestCode == REQUEST_CODE_TEST) {
			if (resultCode == Activity.RESULT_OK) {
				if (data.hasExtra("comandi_da_eseguire")) {

					Bundle b = data.getExtras();
					String[] array = b.getStringArray("comandi_da_eseguire");

					for (int i = 0; i < array.length; i++) {
						if (array[i].length() > 1) {
							serialPort.writeBytes(Utility
									.stringToBytesASCII(array[i]));
						} else {
							serialPort.writeBytes(Utility
									.stringToBytesASCII(array[i]));
						}
					}
				}
			}
		}

		inviaComandi("a");
	}

	@Override
	protected void onPause() {
		super.onPause();

		datasource.close();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (preferences.getBoolean("exit", false)) {

			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);

		Log.d("TCARE", "SONO IN ONCREATE");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		Log.d("TCARE",
				"EXIT (SONO IN ONCREATE)? "
						+ preferences.getBoolean("exit", false));

		preferences.edit().putBoolean("isMenu", false).commit();

		database = openOrCreateDatabase(DATABASE_NAME,
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
		datasource = new TCaReDataSource(getApplicationContext());
		datasource.open();

		cur = database.query("SETTINGS", new String[] { "SMART", "PHYSIO",
				"SERIAL_NUMBER", "LANGUAGE", "TIMEOUT" }, null, null, null,
				null, null);

		cur.moveToFirst();
		Boolean smart = null, physio = null;
		String serial_number = null, language = null;
		int timeout = 5;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			smart = cur.getInt(0) > 0;
			physio = cur.getInt(1) > 0;
			serial_number = cur.getString(2);
			language = cur.getString(3);
			timeout = cur.getInt(4);
			cur.moveToNext();
		}
		cur.close();

		preferences.edit().putBoolean("isSmart", smart).commit();
		preferences.edit().putBoolean("isPhysio", physio).commit();
		preferences.edit().putString("serial_number", serial_number).commit();
		preferences.edit().putString("language", language).commit();
		preferences.edit().putInt("timeout", timeout).commit();

		Resources resource = getResources();
		DisplayMetrics dm = resource.getDisplayMetrics();
		android.content.res.Configuration conf = resource.getConfiguration();
		conf.locale = new Locale(preferences.getString("language", "en"));
		resource.updateConfiguration(conf, dm);

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
					inviaComandi("P");

				if (preferences.getBoolean("isContinuos", false)) {
					if (preferences.getInt("hz", 1) == 0) {
						inviaComandi("1");
					} else {
						inviaComandiNumerici(preferences.getInt("hz", 1));
					}
				} else if (preferences.getBoolean("isPulsed", false)) {
					inviaComandi("0");
				} else {
					inviaComandi("1");
				}

			}
		});

		frequency = (Button) findViewById(R.id.frequency);
		frequency.setTag(R.drawable.button_457);
		frequency.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (preferences.getBoolean("isPlaying", false))
					inviaComandi("P");

				switch ((Integer) frequency.getTag()) {
				case R.drawable.button_457:
					inviaComandi("s");
					break;
				case R.drawable.button_571:
					inviaComandi("m");
					break;
				case R.drawable.button_714:
					inviaComandi("q");
					break;
				case R.drawable.button_145:
					inviaComandi("c");
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

						inviaComandiNumerici(Integer.parseInt(percentage
								.getText().toString()) + 150);

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
		play.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				inviaComandi("S");
			}
		});

		stop = (Button) findViewById(R.id.button_stop);
		stop.setPressed(true);
		stop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				inviaComandi("T");
			}
		});
		label_stop.setTextColor(Color.parseColor("#78d0d2"));

		pause = (Button) findViewById(R.id.button_pause);
		pause.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				inviaComandi("P");
			}
		});

		cap = (Button) findViewById(R.id.cap);
		cap.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (preferences.getBoolean("isPlaying", false))
					inviaComandi("P");

				inviaComandi("C");
			}
		});

		res = (Button) findViewById(R.id.res);
		res.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (preferences.getBoolean("isPlaying", false))
					inviaComandi("P");

				inviaComandi("R");
			}
		});

		body = (Button) findViewById(R.id.body);
		body.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (preferences.getBoolean("isPlaying", false))
					inviaComandi("P");

				inviaComandi("B");
			}
		});

		face = (Button) findViewById(R.id.face);
		face.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (preferences.getBoolean("isPlaying", false))
					inviaComandi("P");

				inviaComandi("F");
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

		DisplayMetrics display = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(display);

		int width, height;

		width = display.widthPixels;
		height = display.heightPixels;

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

		final int blocco2_dim = (int) (width * 50 / 100 / 5);
		label_start.setWidth(blocco2_dim);
		label_stop.setWidth(blocco2_dim);
		label_pause.setWidth(blocco2_dim);
		// label_start.setTextSize(width * moltiplicativo / 100 / 2);
		// label_stop.setTextSize(width * moltiplicativo / 100 / 2);
		// label_pause.setTextSize(width * moltiplicativo / 100 / 2);

		menu = (Button) findViewById(R.id.menu);
		menu.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				inviaComandi("a");

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}

				preferences.edit().putBoolean("isMenu", true).commit();
				Intent intent = new Intent(Main_Activity.this, Menu.class);
				startActivityForResult(intent, REQUEST_CODE_TEST);

				cur = database.query("WORK_TIME", new String[] { "WORK_FROM" },
						null, null, null, null, null);

				cur.moveToFirst();

				int work_from = 0;

				while (cur.getCount() > 0 && !cur.isAfterLast()) {
					work_from = cur.getInt(0);
					cur.moveToNext();
				}

				cur.close();

				cur = database.query("PASSWORD", new String[] { "PWD" }, null,
						null, null, null, null);

				cur.moveToFirst();

				String password = null;

				while (cur.getCount() > 0 && !cur.isAfterLast()) {
					password = cur.getString(0);
					cur.moveToNext();
				}

				cur.close();

				preferences.edit().putInt("work_time", work_from).commit();
				preferences.edit().putString("password", password).commit();

			}
		});

		percentage.setTextSize(height / 2 * 20 / 100 / density);
		percentuale_simbolo.setTextSize(height / 2 * 20 / 100 / density);
		duty.setTextSize(height / 2 * 20 / 100 / density / 2);
		time.setTextSize(height / 2 * 20 / 100 / density);

		android.view.ViewGroup.LayoutParams param = seek_bar_percentage
				.getLayoutParams();
		param.width = width * 70 / 100;

		int padding = (int) (width * 70 / 100 / 11);

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
			title2.setText(getResources().getString(R.string.title2_smart));
		}

		if (preferences.getBoolean("isPhysio", false)) {
			cap.setVisibility(View.VISIBLE);
			res.setVisibility(View.VISIBLE);
			title2.setText(getResources().getString(R.string.title2_physio));
		}

		initSerialPort();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		writeThread = new write_thread();
		if (!writeThread.isAlive() && writeThread.getState() != State.RUNNABLE) {
			writeThread.setName("Thread_Scrittura");
			writeThread.start();

		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		String commandString = "@";
		int numBytes = commandString.length();
		byte[] writeBuffer = new byte[64];

		for (int i = 0; i < numBytes; i++) {
			writeBuffer[i] = (byte) commandString.charAt(i);
		}

		SendData(numBytes, writeBuffer);

		inviaComandi("@");
		inviaComandi("^");
		inviaComandi("a");
		inviaComandi("?");

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

	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d("TCARE", "SONO IN ONRESUME");

		datasource.open();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

	}

	@Override
	protected void onStop() {

		super.onStop();
		Log.d("TCARE", "SONO IN ONSTOP");
	}

	private class write_thread extends Thread {

		write_thread() {
		}

		public void run() {

			Log.d("TCARE", "ENTRO NELLO SCRIVO");

			boolean READ_ENABLE = true;
			int exit = 0;

			while (READ_ENABLE) {

				// Log.d("TCARE", "SONO NELLO SCRIVO");

				inviaComandi("W");

				exit += 1;

				Message aggiorna_tempo_lavoro_db = Main_Activity.aggiorna_tempo_lavoro_db
						.obtainMessage();
				Main_Activity.aggiorna_tempo_lavoro_db
						.sendMessage(aggiorna_tempo_lavoro_db);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				if (exit > preferences.getInt("timeout", 5)) {
					READ_ENABLE = false;
				}
			}

			READ_ENABLE = false;
			Log.d("TCARE", "ESCO DALLO SCRIVO");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			utility.poweroff();
		}
	}

	private class ReadThread extends Thread {
		byte[] buf = new byte[512];

		private ReadThread() {
		}

		public void run() {
			super.run();

			Log.d("TCARE", "ENTRO NEL LEGGO");

			Toast.makeText(getApplicationContext(), "ENTRO NEL LEGGO",
					Toast.LENGTH_LONG).show();

			for (;;) {
	
				while (mInputStream == null) {
					return;
				}
				int i = serialPort.readBytes(this.buf);
				if (i > 0) {
					byte[] arrayOfByte = new byte[i];
					System.arraycopy(this.buf, 0, arrayOfByte, 0, i);
					byteLinkedList.offer(arrayOfByte);
					onDataReceived();
				}
			}
		}
	}

	private void inviaComandi(String comando) {
		int numBytes = comando.length();
		byte[] writeBuffer = new byte[64];

		for (int i = 0; i < numBytes; i++) {
			writeBuffer[i] = (byte) comando.charAt(i);
		}

		SendData(numBytes, writeBuffer);
	}

	public byte SendData(int numBytes, byte[] buffer) {
		byte status = 0x00; /* success by default */

		/*
		 * if num bytes are more than maximum limit
		 */
		if (numBytes < 1) {
			/* return the status with the error in the command */
			Log.e("TCARE", "SendData: numero di byte nullo o negativo");
			return status;
		}

		/* check for maximum limit */
		if (numBytes > 256) {
			numBytes = 256;
			Log.e("TCARE", "SendData: numero di byte superiore a 256byte");
		}

		/* prepare the packet to be sent */
		for (int count = 0; count < numBytes; count++) {
			writeusbdata[count] = buffer[count];

		}

		if (numBytes != 64) {
			SendPacket(numBytes);
		} else {
			byte temp = writeusbdata[63];
			SendPacket(63);
			writeusbdata[0] = temp;
			SendPacket(1);
		}

		return status;
	}

	private void SendPacket(int numBytes) {

		try {
			serialPort.getOutputStream().write(writeusbdata, 0, numBytes);

		} catch (IOException e) {

			Log.d("TCARE", "SendPacket: HO PERSO LA SCHEDA");

		}
	}

	private void inviaComandiNumerici(int comando) {
		try {
			serialPort.getOutputStream().write(comando);
			serialPort.getOutputStream().flush();

			Log.d("TCARE", "MandaDati: scrittura eseguita= " + comando);

		} catch (IOException e) {

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}

			utility.poweroff();

		}
	}
}