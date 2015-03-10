package it.app.tcare;

import java.sql.Timestamp;
import java.util.Calendar;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Main_Activity extends Activity {

	private static final int BaudRate = 9600;

	private TextView label_start, label_pause, label_stop, title, title2,
			percentage, percentuale_simbolo, duty, time, zero, dieci, venti,
			trenta, quaranta, cinquanta, sessanta, settanta, ottanta, novanta,
			cento, frequency_label, revision;
	private Button play, stop, pause, cap, res, body, face, menu, button1,
			energy, frequency, continuos;
	private SeekBar seek_bar_percentage;

	public FT311UARTInterface uartInterface;

	private int[] actualNumBytes;
	private char[] readBufferToChar;
	private byte[] writeBuffer, readBuffer;

	private StringBuffer readSB = new StringBuffer();

	private boolean active = false;

	private Utility utility;

	@Override
	protected void onResume() {
		// Ideally should implement onResume() and onPause()
		// to take appropriate action when the activity looses focus
		super.onResume();
		uartInterface.ResumeAccessory();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		uartInterface.DestroyAccessory(true);
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);

		utility = new Utility(this);

		title = (TextView) findViewById(R.id.title);
		title2 = (TextView) findViewById(R.id.title2);

		percentuale_simbolo = (TextView) findViewById(R.id.percentuale_simbolo);

		duty = (TextView) findViewById(R.id.duty);
		time = (TextView) findViewById(R.id.time);

		frequency = (Button) findViewById(R.id.frequency);
		frequency.setTag(R.drawable.button_457);
		frequency.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				switch ((Integer) frequency.getTag()) {
				case R.drawable.button_457:
					writeData("s");
					break;
				case R.drawable.button_571:
					writeData("m");
					break;
				case R.drawable.button_714:
					writeData("q");
					break;
				case R.drawable.button_145:
					writeData("c");
					break;

				}
			}

		});

		continuos = (Button) findViewById(R.id.button_continuos);
		continuos.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (continuos.isPressed())
						writeData("0");
					else
						writeData("1");
					return true;
				}

				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
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

		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			System.out.println("ERRORE STRANO QUI!");
			e.printStackTrace();
		}
		revision = (TextView) findViewById(R.id.revision);
		revision.setText(pInfo.versionName);

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

						uartInterface.MandaDati(Integer.parseInt(percentage
								.getText().toString()) + 150);

					}
				});

		writeBuffer = new byte[64];
		readBuffer = new byte[4096];
		readBufferToChar = new char[4096];
		actualNumBytes = new int[1];

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
					writeData("S");
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
					writeData("T");
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
					writeData("P");
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
					writeData("C");
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
					writeData("R");
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
					writeData("B");
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
					writeData("F");
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

		int blocco2_dim = (int) (width * 50 / 100 / 5);
		play.setWidth(blocco2_dim);
		stop.setWidth(blocco2_dim);
		pause.setWidth(blocco2_dim);
		play.setHeight(blocco2_dim);
		stop.setHeight(blocco2_dim);
		pause.setHeight(blocco2_dim);
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
					Log.d("TCARE",
							"Dimensioni pulsantone: " + frequency.getHeight()
									+ " - " + frequency.getWidth());
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

		Log.d("TCARE", "Dimensioni blocco2_dim: " + blocco2_dim);

		percentage.setTextSize(height / 2 * 20 / 100 / density);
		percentuale_simbolo.setTextSize(height / 2 * 20 / 100 / density);
		duty.setTextSize(height / 2 * 20 / 100 / density / 2);
		time.setTextSize(height / 2 * 20 / 100 / density);

		android.view.ViewGroup.LayoutParams param = seek_bar_percentage
				.getLayoutParams();
		param.width = width * 70 / 100;

		// int padding = (int) (width * 70 / 100 / (14 + density));
		//
		// dieci.setPadding(padding, 0, 0, 0);
		// venti.setPadding(padding, 0, 0, 0);
		// trenta.setPadding(padding, 0, 0, 0);
		// quaranta.setPadding(padding, 0, 0, 0);
		// cinquanta.setPadding(padding, 0, 0, 0);
		// sessanta.setPadding(padding, 0, 0, 0);
		// settanta.setPadding(padding, 0, 0, 0);
		// ottanta.setPadding(padding, 0, 0, 0);
		// novanta.setPadding(padding, 0, 0, 0);
		// cento.setPadding(padding, 0, 0, 0);

		energy.setWidth((int) (blocco2_dim * moltiplicativo));
		energy.setHeight((int) (blocco2_dim * moltiplicativo / 0.40));

		frequency.setHeight((int) (blocco2_dim + 50 * density));
		frequency.setWidth((int) (blocco2_dim + 50 * density));

		continuos.setHeight(blocco2_dim-10);
		continuos.setWidth(blocco2_dim-10);

		Log.d("TCARE", "Dimensioni pulsantone: " + frequency.getHeight()
				+ " - " + frequency.getWidth());

		try {
			uartInterface = new FT311UARTInterface(this, null);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("TCARE", e.getMessage());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void writeData(String commandString) {

		int numBytes = commandString.length();
		writeBuffer = new byte[64];

		for (int i = 0; i < numBytes; i++) {
			writeBuffer[i] = (byte) commandString.charAt(i);
			Log.d("TCARE", "writeData: scrivo: " + commandString.charAt(i)
					+ " tradotto: " + (byte) commandString.charAt(i));
		}

		if (uartInterface != null)
			uartInterface.SendData(numBytes, writeBuffer);
		else
			Log.e("TCARE", "Interfaccia non avviata!!!");
		Calendar calendar = Calendar.getInstance();
		Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime()
				.getTime());
		Log.d("TCARE", currentTimestamp + ": writeData: scritto(" + numBytes
				+ "): " + writeBuffer.toString());

	}

	@Override
	public void onStart() {
		super.onStart();
		active = true;

	}
}
