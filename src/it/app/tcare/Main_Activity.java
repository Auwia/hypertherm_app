package it.app.tcare;

import java.sql.Timestamp;
import java.util.Calendar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Main_Activity extends Activity {

	private static final int BaudRate = 9600;

	private SeekBar seek_bar_percentage;
	private TextView time, revision, label_start, label_pause, label_stop,
			percentage;

	private Button play, stop, pause, cap, res, body, face, reset, energy,
			menu, continuos;
	private ImageButton frequency;

	public FT311UARTInterface uartInterface;

	private int[] actualNumBytes;
	private char[] readBufferToChar;
	private byte[] writeBuffer, readBuffer;

	private StringBuffer readSB = new StringBuffer();

	private boolean active = false;

	private Utility utility;

	ProgressDialog barProgressDialog;
	Handler updateBarHandler;

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

		frequency = (ImageButton) findViewById(R.id.frequency);
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

		menu = (Button) findViewById(R.id.menu);
		menu.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					return true;
				}

				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				if (menu.isPressed())
					menu.setPressed(false);
				else
					menu.setPressed(true);

				return true;
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

		updateBarHandler = new Handler();

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

		reset = (Button) findViewById(R.id.button_reset);
		reset.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					for (int i = 0; i < 20000; i += 100) {
						uartInterface.SetConfig(i, (byte) 8, (byte) 1,
								(byte) 0, (byte) 0);
						Log.d("TCARE", "SetConfig: " + i);
						writeData("S");
					}
					return true;
				}

				// don't handle event unless its ACTION_UP so "doSomething()"
				// only runs once.
				if (event.getAction() != MotionEvent.ACTION_UP)
					return false;
				;

				return true;
			}
		});

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

	public void launchBarDialog(View view) {

		barProgressDialog = new ProgressDialog(Main_Activity.this);
		barProgressDialog.setTitle("Loading driver...");
		barProgressDialog.setMessage("work in progress ...");
		barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		barProgressDialog.setProgress(0);
		barProgressDialog.setMax(20);
		barProgressDialog.show();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (barProgressDialog.getProgress() <= barProgressDialog
							.getMax()) {
						Thread.sleep(200);
						updateBarHandler.post(new Runnable() {
							public void run() {
								barProgressDialog.incrementProgressBy(2);
							}
						});
						if (barProgressDialog.getProgress() == barProgressDialog
								.getMax()) {
							barProgressDialog.dismiss();
						}
					}
				} catch (Exception e) {
					System.out.println("ERRORE STRANO QUI!");
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onStart() {
		super.onStart();
		active = true;

	}

	private void avvia_driver() {
		try {
			uartInterface = new FT311UARTInterface(this, null);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("TCARE", e.getMessage());
		}
		Log.d("TCARE", "Pre-Progress...");
		launchBarDialog(null);
		Log.d("TCARE", "Preparo la configurazione...");
		uartInterface.ResumeAccessory();
		uartInterface.SetConfig(BaudRate, (byte) 8, (byte) 1, (byte) 0,
				(byte) 0);

		Log.d("TCARE", "Fine configurazione...");
	}
}
