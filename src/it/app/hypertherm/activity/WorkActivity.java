package it.app.hypertherm.activity;

import it.app.hypertherm.R;
import it.app.hypertherm.Simulatore;
import it.app.hypertherm.Tracciato;
import it.app.hypertherm.thread.InviaComandiThread;
import it.app.hypertherm.thread.ReadThreadConsumer;
import it.app.hypertherm.thread.ReadThreadProducer;
import it.app.hypertherm.thread.WriteThread;
import it.app.hypertherm.util.CountDownTimer;
import it.app.hypertherm.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dwin.navy.serialportapi.SerialPortOpt;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class WorkActivity extends Activity {

	private SeekBar seek_bar;
	private static Button button_antenna_left, button_antenna_right,
			button_water_left, button_water_right, button_deltat_left,
			button_deltat_right, button_time_left, button_time_right,
			button_home, button_play, button_pause, button_stop,
			button_bolus_up, button_bolus_down, button_power,
			button_temperature_positive, button_temperature_negative,
			button_onda_quadra, button_antenna, button_time, button_water,
			button_deltat;
	private TextView antenna_black_label_down, water_label_down,
			deltat_label_down, time_label_down;
	private static TextView disturbo_label, suggerimenti;

	private static Utility utility;

	private InputStream mInputStream;
	private OutputStream mOutputStream;

	private static BlockingQueue<byte[]> bq_out;
	private BlockingQueue<byte[]> bq_in;

	public static boolean SIMULATORE = false;
	private static boolean PING = false;
	public static boolean COMMUNICATION_READY = true;
	private boolean LONG = false;
	public static boolean AVVIO;

	private int funzionalita;

	public static int WATER = 0;
	public static int DELTAT = 0;
	public static int POWER = 0;
	public static int TIMER = 0;

	private final static int MSK_CMD = 2;
	private final static int MSK_TIME = 4;
	private final static int MSK_DELTAT = 8;
	private final static int MSK_WATER = 16;
	private final static int MSK_POWER = 64;
	private final static int MSK_ALL_4 = 92;
	private final static int MSK_NOTHING = 0;
	private static int CMD = 3;
	private final static int PLAY = 1;
	private final static int PAUSE = 2;
	private final static int STOP = 3;
	// private final static int PLAY_TMP = 112;
	// private final static int PAUSE_TMP = 96;
	// private final static int STOP_TMP = 32;
	private final static int PLAY_TMP = 125;
	private final static int PAUSE_TMP = 109;
	private final static int STOP_TMP = 45;
	private final static int BOLUS_UP = 4;
	private final static int BOLUS_DOWN = 5;
	private final static int BOLUS_STOP = 6;
	private final static int RESET = 11;
	private final static int RF_OFF = 12;
	private final static int RF_ON = 13;

	private static int TIME_OUT_PING, INOUT = STOP_TMP;

	private LineGraphSeries<DataPoint> mSeries380, mSeries383, mSeries386,
			mSeries389, mSeries393, mSeries396, mSeries399, mSeries402,
			mSeries405, mSeries408, mSeries411, mSeries414, mSeries418,
			mSeries421, mSeries424, mSeries427;

	private static SharedPreferences preferences;

	private SerialPortOpt serialPort;

	private ReadThreadProducer mReadThreadProducer;
	private Simulatore sim;
	private ReadThreadConsumer mReadThreadConsumer;
	private WriteThread mWriteThread;
	private WritePing mWritePing;

	private CountDownTimer waitTimerBolusUp = null;
	private CountDownTimer waitTimer = null;
	private CountDownTimer waitTimerGrafico = null;

	private static Timer timerRfOn, timerRfOff;

	private Tracciato tracciato_in = new Tracciato();
	private static Tracciato tracciato_out = new Tracciato();

	public static void inviaComandi(final int comando, final int maschera,
			final int inout) {

		tracciato_out.setComando(comando);
		tracciato_out.setMaschera(maschera);
		tracciato_out.setInOutput(inout);
		tracciato_out.setBuf();
		tracciato_out.setCheckSum(utility.calcola_check_sum(tracciato_out
				.getBuf()));

		InviaComandiThread buf = new InviaComandiThread(bq_out,
				tracciato_out.setBuf());

		new Thread(buf, "Thread Invia Comandi").start();

	}

	private class WritePing extends Thread {

		WritePing() {
		}

		public void run() {

			Timer timer = new Timer();

			timer.scheduleAtFixedRate(new TimerTask() {

				public void run() {

					if (COMMUNICATION_READY) {

						// PING = false;

						if (PING) {
							inviaComandi(CMD, MSK_NOTHING, INOUT);
						}

					} else {

						utility.appendLog("I", "ESCO DALL'INVIO PING!");

						cancel();

					}
				}
			}, 0, TIME_OUT_PING);

		}
	}

	@Override
	public void onPause() {
		super.onPause();

		INOUT = RF_OFF;
		utility.appendLog("I", "Inviato comando: RF_On_Off = OFF");
		inviaComandi(RF_OFF, MSK_CMD, INOUT);

		utility.appendLog("I", "STO USCENDO");

		COMMUNICATION_READY = false;

		PING = false;

		try {
			mInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		finish();

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	private void initSerialPort() {
		serialPort = new SerialPortOpt();
		serialPort.mDevNum = 0;
		serialPort.mDataBits = 8;
		serialPort.mSpeed = 38400;
		serialPort.mStopBits = 1;
		serialPort.mParity = 'n';
		serialPort.openDev(serialPort.mDevNum);
		serialPort.setSpeed(serialPort.mFd, serialPort.mSpeed);
		serialPort.setParity(serialPort.mFd, serialPort.mDataBits,
				serialPort.mStopBits, serialPort.mParity);

		mInputStream = this.serialPort.getInputStream();
		mOutputStream = this.serialPort.getOutputStream();

		mWriteThread = new WriteThread(bq_out, utility, mOutputStream);

		if (SIMULATORE) {

			sim = new Simulatore(bq_in, utility, this);

		} else {

			mReadThreadProducer = new ReadThreadProducer(bq_in, utility,
					mInputStream);

		}

		mReadThreadConsumer = new ReadThreadConsumer(bq_in, utility);

		COMMUNICATION_READY = true;

		new Thread(mWriteThread, "Thread Scrittura").start();

		Thread t;

		new Thread(mReadThreadConsumer, "Thread Lettura Consumer").start();

		if (SIMULATORE) {
			t = new Thread(sim, "Thread lettura simulatore");

		} else {
			t = new Thread(mReadThreadProducer, "Thread Lettura Producer");

		}
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();

		if (AVVIO) {
			INOUT = 0;
			utility.appendLog("I", "Inviato comando: RESET");
			inviaComandi(RESET, MSK_CMD, INOUT);
			AVVIO = false;
		}

		utility.appendLog("I",
				"Attendo dal reset per " + utility.get_time_out_reset() + "ms");
		try {
			Thread.sleep(utility.get_time_out_reset());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		INOUT = RF_ON;
		utility.appendLog("I", "Inviato comando: RF_On_Off = ON");
		inviaComandi(RF_ON, MSK_CMD, INOUT);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PING = true;
		mWritePing = new WritePing();
		mWritePing.setName("Thread_PING");
		mWritePing.start();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_work);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			SIMULATORE = extras.getBoolean("DEMO");
		}

		utility = new Utility(this);

		bq_out = new ArrayBlockingQueue<byte[]>(64);
		bq_in = new ArrayBlockingQueue<byte[]>(64);

		TIME_OUT_PING = utility.get_time_out_ping();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		initSerialPort();

		seek_bar = (SeekBar) findViewById(R.id.seek_bar);
		seek_bar.setMax(10);
		seek_bar.setEnabled(false);
		seek_bar.setProgress(5);

		android.view.ViewGroup.LayoutParams param = seek_bar.getLayoutParams();

		DisplayMetrics display = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(display);
		int width = display.widthPixels;

		param.width = width * 20 / 100;

		def_variable_components();

		def_bottun_click();

		def_value_defaults();

		if (SIMULATORE) {
			disturbo_label.setTextColor(Color.parseColor("#ffa500"));
			disturbo_label.setText("DEMO");
			button_stop.setPressed(true);
		}

	}

	private static void def_value_defaults() {

		button_power.setPressed(true);

		button_water.setEnabled(false);
		button_water.setClickable(false);

		button_deltat.setEnabled(false);
		button_deltat.setClickable(false);

		button_antenna.setEnabled(false);
		button_antenna.setClickable(false);

		button_time.setEnabled(false);
		button_time.setClickable(false);

		tracciato_out
				.setWaterOut((int) (preferences.getFloat("WATER", 35) * 100));
		tracciato_out
				.setDeltaTOut((int) (preferences.getFloat("DELTAT", 1) * 100));
		tracciato_out.setPowerOut(preferences.getInt("ANTENNA", 0) * 100);
		tracciato_out.setTimerOut(preferences.getInt("TIME", 0) * 60);

		disturbo_label.setText(String.valueOf(preferences.getString(
				"MENU_ITEM", "Defect")));

		if (disturbo_label.getText().toString()
				.equals(utility.getMenuItemDefault())) {

			disturbo_label.setTextColor(Color.parseColor("#ffa500"));

		} else {

			disturbo_label.setTextColor(Color.BLACK);

		}

		suggerimenti.setText(utility.get_suggerimento_trattamento());

		button_home.setEnabled(true);
		button_temperature_negative.setPressed(false);
		button_temperature_positive.setPressed(false);

		utility.appendLog("I", "Inviato comando: 4 ALL");
		inviaComandi(0, MSK_ALL_4, INOUT);

	}

	private void def_bottun_click() {

		button_power.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					button_temperature_negative.setPressed(false);
					button_temperature_positive.setPressed(false);

					int pos = seek_bar.getProgress();

					seek_bar.setProgress(5);

					if (button_power.isPressed()) {

						return true;

					} else {

						button_power.setPressed(true);

						if (pos >= 0 && pos < 5) {
							for (int i = 0; i < 5 - pos; i++) {

								funzionalita = button_water_right.getId();
								increment();
								increment();
								increment();

								funzionalita = button_deltat_right.getId();
								increment();

							}
						} else if (pos > 5 && pos <= 10) {
							for (int i = 0; i < pos - 5; i++) {
								funzionalita = button_water_left.getId();
								decrement();
								decrement();
								decrement();

								funzionalita = button_deltat_left.getId();
								decrement();

							}
						}

						inviaComandi(0, MSK_ALL_4, INOUT);

						return false;

					}

				}

				return true;
			}
		});

		button_temperature_negative.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (seek_bar.getProgress() > 0) {

						seek_bar.setProgress(seek_bar.getProgress() - 1);

						funzionalita = button_water_left.getId();
						decrement();
						decrement();
						decrement();

						funzionalita = button_deltat_left.getId();
						decrement();

						if (SIMULATORE) {

							float potenza = utility.getPmaxRF(
									tracciato_out.getPowerOut() / 10,
									tracciato_out.getWaterOut() / 10);

							tracciato_in.setPowerOut((int) (potenza * 10));
						}

						if (seek_bar.getProgress() == 5) {
							button_power.setPressed(true);
							button_temperature_negative.setPressed(false);
							button_temperature_positive.setPressed(false);
						} else {
							button_power.setPressed(false);
							button_temperature_negative.setPressed(true);
							button_temperature_positive.setPressed(false);
						}

						inviaComandi(0, MSK_ALL_4, INOUT);

					}
				}

				return true;
			}
		});

		button_temperature_positive.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (seek_bar.getProgress() < 10) {

						seek_bar.setProgress(seek_bar.getProgress() + 1);

						funzionalita = button_water_right.getId();
						increment();
						increment();
						increment();

						funzionalita = button_deltat_right.getId();
						increment();

						if (SIMULATORE) {

							float potenza = utility.getPmaxRF(
									tracciato_out.getPowerOut() / 10,
									tracciato_out.getWaterOut() / 10);

							tracciato_in.setPowerIn((int) (potenza * 10));
						}

						if (seek_bar.getProgress() == 5) {
							button_power.setPressed(true);
							button_temperature_negative.setPressed(false);
							button_temperature_positive.setPressed(false);
						} else {
							button_power.setPressed(false);
							button_temperature_negative.setPressed(false);
							button_temperature_positive.setPressed(true);
						}

						inviaComandi(0, MSK_ALL_4, INOUT);
					}
				}

				return true;
			}
		});

		button_play.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				suggerimenti.setText("");

				button_time.setPressed(true);

				CMD = 1;

				// runOnUiThread(new Runnable() {
				//
				// @Override
				// public void run() {
				//
				// if (waitTimerGrafico == null) {
				//
				// waitTimerGrafico = new CountDownTimer(
				// Integer.parseInt(time_label_down
				// .getText().subSequence(0, 2)
				// .toString()) * 60 * 1000 + 1,
				// 1000) {
				//
				// public void onTick(long millisUntilFinished) {
				//
				// if (t == 30) {
				// t = 0;
				// }
				//
				// // mSeries1.resetData(generateData(t++));
				//
				// }
				//
				// public void onFinish() {
				// utility.appendLog("I",
				// "CHIUDO IL GRAFICO");
				//
				// // COMANDO STOP SIMULATION
				// utility.esegui(768);
				// CMD = 3;
				//
				// }
				//
				// }.start();
				// }
				//
				// }
				// });

				if (button_onda_quadra.isPressed()) {

					timerRfOn = new Timer("TIMER ON");
					timerRfOn.schedule(new TimerTask() {
						@Override
						public void run() {

							INOUT = RF_OFF;
							utility.appendLog("I",
									"Inviato comando: RF_On_Off = OFF");
							inviaComandi(RF_OFF, MSK_CMD, INOUT);

							timerRfOff = new Timer("TIMER OFF");
							timerRfOff.schedule(new TimerTask() {
								@Override
								public void run() {
									INOUT = RF_ON;
									utility.appendLog("I",
											"Inviato comando: RF_On_Off = ON");
									inviaComandi(RF_ON, MSK_CMD, INOUT);
								}
							}, 60000);

						}
					}, 180000, 240000);

				}

				INOUT = RF_ON;
				utility.appendLog("I", "Inviato comando: RF_On_Off = ON");
				inviaComandi(RF_ON, MSK_CMD, INOUT);

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				INOUT = PLAY_TMP;
				utility.appendLog("I", "Inviato comando: PLAY");
				inviaComandi(PLAY, MSK_CMD, INOUT);

				// runOnUiThread(new Runnable() {
				// @Override
				// public void run() {
				//
				// // disegna_grafico_lib();
				// // disegna_grafico(1);
				//
				// }
				// });

				button_home.setEnabled(false);

			}
		});

		button_pause.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (timerRfOn != null) {
					timerRfOn.cancel();
					timerRfOn = null;
				}

				if (timerRfOff != null) {
					timerRfOff.cancel();
					timerRfOff = null;
				}

				INOUT = PAUSE_TMP;

				utility.appendLog("I", "Inviato comando: PAUSE");
				inviaComandi(PAUSE, MSK_CMD, INOUT);

				CMD = 2;

				utility.reset_piramide();

			}

		});

		button_stop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (timerRfOn != null) {
					timerRfOn.cancel();
					timerRfOn = null;
				}

				if (timerRfOff != null) {
					timerRfOff.cancel();
					timerRfOff = null;
				}

				INOUT = STOP_TMP;

				utility.appendLog("I", "Inviato comando: STOP");
				inviaComandi(STOP, MSK_CMD, INOUT);

				CMD = 3;

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				suggerimenti.setText(utility.get_suggerimento_trattamento());

				button_home.setEnabled(true);
				button_temperature_negative.setPressed(false);
				button_temperature_positive.setPressed(false);

				button_time.setPressed(false);

				utility.reset_piramide();

				def_value_defaults();

			}
		});

		button_bolus_down.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (button_bolus_up.isPressed()) {
						button_bolus_up.setPressed(false);
						if (waitTimerBolusUp != null) {
							waitTimerBolusUp.cancel();
							waitTimerBolusUp = null;
						}

						utility.appendLog("I", "Inviato comando: BOLUS-STOP");
						inviaComandi(BOLUS_STOP, MSK_CMD, INOUT);

						return true;

					} else {

						if (!button_bolus_down.isPressed()) {

							button_bolus_down.setPressed(true);

							utility.appendLog("I",
									"Inviato comando: BOLUS-DOWN");
							inviaComandi(BOLUS_DOWN, MSK_CMD, INOUT);

							waitTimer = new CountDownTimer(30000, 30000) {

								public void onTick(long millisUntilFinished) {

								}

								public void onFinish() {
									button_bolus_down.setPressed(false);
									utility.appendLog("I",
											"Inviato comando: BOLUS-STOP");
									inviaComandi(BOLUS_STOP, MSK_CMD, INOUT);
								}
							}.start();

							return false;

						} else {

							if (waitTimer != null) {
								waitTimer.cancel();
								waitTimer = null;
							}

							utility.appendLog("I",
									"Inviato comando: BOLUS-STOP");
							inviaComandi(BOLUS_STOP, MSK_CMD, INOUT);

							button_bolus_down.setPressed(false);

							return true;
						}
					}
				}

				return true;

			}
		});

		button_bolus_up.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (button_bolus_down.isPressed()) {
						button_bolus_down.setPressed(false);

						if (waitTimer != null) {
							waitTimer.cancel();
							waitTimer = null;
						}

						utility.appendLog("I", "Inviato comando: BOLUS-STOP");
						inviaComandi(BOLUS_STOP, MSK_CMD, INOUT);

						return true;

					} else {

						if (!button_bolus_up.isPressed()) {

							button_bolus_up.setPressed(true);

							utility.appendLog("I", "Inviato comando: BOLUS-UP");
							inviaComandi(BOLUS_UP, MSK_CMD, INOUT);

							waitTimerBolusUp = new CountDownTimer(30000, 30000) {

								public void onTick(long millisUntilFinished) {

								}

								public void onFinish() {
									button_bolus_up.setPressed(false);
									utility.appendLog("I",
											"Inviato comando: BOLUS-STOP");
									inviaComandi(BOLUS_STOP, MSK_CMD, INOUT);

								}
							}.start();

							return false;

						} else {

							if (waitTimerBolusUp != null) {
								waitTimerBolusUp.cancel();
								waitTimerBolusUp = null;
							}

							utility.appendLog("I",
									"Inviato comando: BOLUS-STOP");
							inviaComandi(BOLUS_STOP, MSK_CMD, INOUT);

							button_bolus_up.setPressed(false);

							return true;
						}

					}
				}

				return true;

			}
		});

		button_onda_quadra.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (button_onda_quadra.isPressed()) {

						button_onda_quadra.setPressed(false);

						stoppa_trattamento();

						return true;

					} else {

						button_onda_quadra.setPressed(true);

						utility.appendLog("I", "ONDA QUADRO ON");

						if (button_play.isPressed()) {
							avvia_trattamento();
						}

						return false;
					}
				}
				return true;
			}

		});

		button_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				INOUT = RF_OFF;
				utility.appendLog("I", "Inviato comando: RF_On_Off = OFF");
				inviaComandi(RF_OFF, MSK_CMD, INOUT);

				COMMUNICATION_READY = false;

				Intent intent = new Intent(WorkActivity.this,
						MainActivity.class);
				startActivity(intent);

			}
		});

		button_water_left
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						LONG = true;
						Simulatore.INVIA = false;

						funzionalita = button_water_left.getId();

						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								auto_decrement(button_water_left,
										water_label_down, 35, 10);

							}
						});

						return true;
					}
				});

		button_water_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				funzionalita = button_water_left.getId();

				// int water = tracciato_out.getWaterOut();

				if ((event.getAction() == MotionEvent.ACTION_UP)) {

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					if (WATER >= 3500) {

						inviaComandi(0, MSK_WATER, INOUT);

					}

					LONG = false;
					Simulatore.INVIA = true;
					PING = true;

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					PING = false;

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					WATER -= 10;

					if (WATER >= 3500) {

						tracciato_out.setWaterOut(WATER);

						if (LONG) {
							water_label_down.setText(String.valueOf(Float
									.parseFloat(""
											+ utility.arrotondaPerEccesso(
													WATER, 1))));
						}

					}

					if (!SIMULATORE)
						set_attention();

					attiva_normal();

				}
				return false;
			}
		});

		button_water_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						LONG = true;
						Simulatore.INVIA = false;

						funzionalita = button_water_right.getId();

						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								auto_increment(button_water_right,
										water_label_down, 42, 10);
							}
						});

						return true;
					}
				});

		button_water_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				funzionalita = button_water_right.getId();

				// int water = tracciato_out.getWaterOut();

				if ((event.getAction() == MotionEvent.ACTION_UP)) {

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					if (WATER <= 4200) {
						inviaComandi(0, MSK_WATER, INOUT);
					}

					LONG = false;
					Simulatore.INVIA = true;
					PING = true;

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					PING = false;

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					WATER += 10;

					if (WATER <= 4200) {

						tracciato_out.setWaterOut(WATER);

						if (LONG)
							water_label_down.setText(String.valueOf(Float
									.parseFloat(""
											+ utility.arrotondaPerEccesso(
													WATER, 1))));

					}

					if (!SIMULATORE)
						set_attention();
					attiva_normal();

				}
				return false;
			}
		});

		button_deltat_left
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						LONG = true;
						Simulatore.INVIA = false;

						funzionalita = button_deltat_left.getId();

						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								auto_decrement(button_deltat_left,
										deltat_label_down, -1, 10);

							}
						});

						return true;
					}
				});

		button_deltat_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				funzionalita = button_deltat_left.getId();

				// int deltat = tracciato_out.getDeltaTOut();

				if ((event.getAction() == MotionEvent.ACTION_UP)) {

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					if (DELTAT >= -100) {
						inviaComandi(0, MSK_DELTAT, INOUT);
					}

					LONG = false;
					PING = true;
					Simulatore.INVIA = true;
				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					PING = false;

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					DELTAT -= 10;

					if (DELTAT >= -100) {

						if (DELTAT >= 60000) {
							DELTAT -= 65536;
						}

						if (DELTAT > 0) {

							if (LONG)
								deltat_label_down.setText("+"
										+ utility
												.arrotondaPerEccesso(DELTAT, 1));

						} else {

							if (LONG)
								deltat_label_down.setText(""
										+ utility
												.arrotondaPerEccesso(DELTAT, 1));

						}

						tracciato_out.setDeltaTOut(DELTAT);

					}

					if (!SIMULATORE)
						set_attention();
					attiva_normal();

				}

				return false;
			}
		});

		button_deltat_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						LONG = true;
						Simulatore.INVIA = false;

						funzionalita = button_deltat_right.getId();

						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								auto_increment(button_deltat_right,
										deltat_label_down, 5, 10);

							}
						});

						return true;
					}
				});

		button_deltat_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				funzionalita = button_deltat_right.getId();

				// int deltat = tracciato_out.getDeltaTOut();

				if ((event.getAction() == MotionEvent.ACTION_UP)) {

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					if (DELTAT <= 500) {
						inviaComandi(0, MSK_DELTAT, INOUT);
					}

					LONG = false;
					Simulatore.INVIA = true;
					PING = true;

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					PING = false;

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					DELTAT += 10;

					if (DELTAT <= 500) {

						if (DELTAT >= 60000) {
							DELTAT -= 65536;
						}

						if (DELTAT > 0) {

							if (LONG)
								deltat_label_down.setText("+"
										+ utility
												.arrotondaPerEccesso(DELTAT, 1));

						} else {

							if (LONG)
								deltat_label_down.setText(""
										+ utility
												.arrotondaPerEccesso(DELTAT, 1));

						}

						tracciato_out.setDeltaTOut(DELTAT);

					}

					if (!SIMULATORE)
						set_attention();
					attiva_normal();

				}

				return false;
			}
		});

		button_antenna_left
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						LONG = true;
						Simulatore.INVIA = false;

						funzionalita = button_antenna_left.getId();

						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								auto_decrement(button_antenna_left,
										antenna_black_label_down, 0, 1);

							}
						});

						return true;
					}
				});

		button_antenna_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				funzionalita = button_antenna_left.getId();

				// int antenna = tracciato_out.getPowerOut();

				if ((event.getAction() == MotionEvent.ACTION_UP)) {

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					if (POWER >= 0) {
						inviaComandi(0, MSK_POWER, INOUT);
					}

					PING = true;
					Simulatore.INVIA = true;
					LONG = false;
				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					PING = false;

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					POWER -= 100;

					if (POWER >= 0) {

						if (LONG) {
							antenna_black_label_down
									.setText("" + (POWER / 100));
						}
						tracciato_out.setPowerOut(POWER);

					}

					if (!SIMULATORE)
						set_attention();
					attiva_normal();

				}

				return false;
			}
		});

		button_antenna_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						LONG = true;
						Simulatore.INVIA = false;

						funzionalita = button_antenna_right.getId();

						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								auto_increment(button_antenna_right,
										antenna_black_label_down, 99, 1);

							}
						});

						return true;
					}
				});

		button_antenna_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				funzionalita = button_antenna_right.getId();

				// int antenna= tracciato_out.getPowerOut();

				if ((event.getAction() == MotionEvent.ACTION_UP)) {

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					inviaComandi(0, MSK_POWER, INOUT);

					PING = true;
					Simulatore.INVIA = true;
					LONG = false;

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					PING = false;

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					POWER += 100;

					if (POWER <= 9900) {
						if (LONG)
							antenna_black_label_down
									.setText("" + (POWER / 100));
						tracciato_out.setPowerOut(POWER);

					}

					if (!SIMULATORE)
						set_attention();
					attiva_normal();

				}

				return false;
			}
		});

		button_time_left.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {

				LONG = true;
				Simulatore.INVIA = false;

				funzionalita = button_time_left.getId();

				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						auto_decrement(button_time_left, time_label_down, 0, 1);

					}
				});

				return true;
			}
		});

		button_time_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				funzionalita = button_time_left.getId();

				// int time = tracciato_out.getTimerOut() / 60;

				TIMER /= 60;

				if ((event.getAction() == MotionEvent.ACTION_UP)) {

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					inviaComandi(0, MSK_TIME, INOUT);

					PING = true;
					LONG = false;
					Simulatore.INVIA = true;

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					PING = false;

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					if (TIMER > 1) {

						if (LONG)
							if (TIMER - 1 < 10 && TIMER - 1 > 0) {
								time_label_down.setText("0" + (TIMER - 1));
							} else {
								time_label_down.setText("" + (TIMER - 1));
							}

						if (LONG)
							if (TIMER == 1) {
								time_label_down.setText("00");
							}

						tracciato_out.setTimerOut((TIMER - 1) * 60);
					}

					attiva_normal();

				}

				return false;
			}
		});

		button_time_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						LONG = true;
						Simulatore.INVIA = false;

						funzionalita = button_time_right.getId();

						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								auto_increment(button_time_right,
										time_label_down, 30, 1);

							}
						});

						return true;
					}
				});

		button_time_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				funzionalita = button_time_right.getId();

				// int time = tracciato_out.getTimerOut() / 60;
				TIMER /= 60;

				if ((event.getAction() == MotionEvent.ACTION_UP)) {

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					inviaComandi(0, MSK_TIME, INOUT);

					PING = true;
					LONG = false;
					Simulatore.INVIA = true;

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					PING = false;

					if (waitTimer != null) {
						waitTimer.cancel();
						waitTimer = null;
					}

					if (TIMER > 0 && TIMER < 30) {

						if (LONG)
							if (TIMER + 1 < 10) {
								time_label_down.setText("0" + (TIMER + 1));
							} else {
								time_label_down.setText("" + (TIMER + 1));
							}

						tracciato_out.setTimerOut((TIMER + 1) * 60);

					}

					attiva_normal();

				}

				return false;
			}
		});

	}

	protected void stoppa_trattamento() {
		if (timerRfOn != null) {
			timerRfOn.cancel();
			timerRfOn = null;
		}

		if (timerRfOff != null) {
			timerRfOff.cancel();
			timerRfOff = null;
		}

		utility.appendLog("I", "ONDA QUADRA OFF");

		INOUT = RF_ON;
		utility.appendLog("I", "Inviato comando: RF_On_Off = ON");
		inviaComandi(RF_ON, MSK_CMD, INOUT);

	}

	protected void avvia_trattamento() {

		timerRfOn = new Timer("TIMER ON");
		timerRfOn.schedule(new TimerTask() {
			@Override
			public void run() {

				INOUT = RF_OFF;
				utility.appendLog("I", "Inviato comando: RF_On_Off = OFF");
				inviaComandi(RF_OFF, MSK_CMD, INOUT);

				timerRfOff = new Timer("TIMER OFF");
				timerRfOff.schedule(new TimerTask() {
					@Override
					public void run() {
						INOUT = RF_ON;
						utility.appendLog("I",
								"Inviato comando: RF_On_Off = ON");
						inviaComandi(RF_ON, MSK_CMD, INOUT);
					}
				}, 60000);

			}
		}, 0, 240000);

	}

	protected void attiva_normal() {

		button_temperature_negative.setPressed(false);
		button_temperature_positive.setPressed(false);
		button_power.setPressed(true);
		seek_bar.setProgress(5);

	}

	private float function(double x, double y) {

		int B = 3;
		float Tw = WATER;
		double b = 0.19;
		int Tb = 37;
		float Dt = DELTAT;
		double a = 0.035;
		double A = (B + 1) * Dt + Tw - Tb;
		double h = 0.001522;
		double k = 0.011513;
		int x0 = 0;

		double equation = Tb
				+ ((B * Tw * Math.exp(-b * y) + Tb + A * Math.exp(-a * y))
						/ (B * Math.exp(-b * y) + 1) - Tb)
				* Math.exp(-h * Math.pow(x - x0, 2));

		return new Double(equation).floatValue();

	}

	private double equation(int x) {

		double equation = Math.exp(Math.pow(-x, 2));

		return new Double(equation).floatValue();

	}

	protected void set_attention() {

		if (!disturbo_label.getText().equals(utility.getMenuItemDefault())) {

			disturbo_label.setTextColor(Color.parseColor("#ffa500"));

			waitTimer = new CountDownTimer(5600, 700) {

				private int tot = 1;

				public void onTick(long millisUntilFinished) {

					if (tot % 2 == 0) {
						disturbo_label.setText("");

					} else {
						disturbo_label.setText(utility.getMenuItemDefault());
					}

					tot += 1;

				}

				public void onFinish() {

					disturbo_label.setText(utility.getMenuItemDefault());
					disturbo_label.setTextColor(Color.parseColor("#ffa500"));

					seek_bar.setProgress(5);

					button_power.setPressed(true);

				}
			}.start();
		}

	}

	private void def_variable_components() {
		button_antenna_left = (Button) findViewById(R.id.button_antenna_left);
		button_antenna_right = (Button) findViewById(R.id.button_antenna_right);
		button_water_left = (Button) findViewById(R.id.button_water_left);
		button_water_right = (Button) findViewById(R.id.button_water_right);
		button_deltat_left = (Button) findViewById(R.id.button_deltat_left);
		button_deltat_right = (Button) findViewById(R.id.button_deltat_right);
		button_time_left = (Button) findViewById(R.id.button_time_left);
		button_time_right = (Button) findViewById(R.id.button_time_right);
		button_home = (Button) findViewById(R.id.button_home);
		button_play = (Button) findViewById(R.id.button_play);
		button_pause = (Button) findViewById(R.id.button_pause);
		button_stop = (Button) findViewById(R.id.button_stop);
		button_bolus_down = (Button) findViewById(R.id.button_bolus_down);
		button_bolus_up = (Button) findViewById(R.id.button_bolus_up);
		button_power = (Button) findViewById(R.id.button_power);
		button_temperature_negative = (Button) findViewById(R.id.button_temperature_negative);
		button_temperature_positive = (Button) findViewById(R.id.button_temperature_positive);
		button_onda_quadra = (Button) findViewById(R.id.button_onda_quadra);
		button_antenna = (Button) findViewById(R.id.button_antenna_black);
		button_time = (Button) findViewById(R.id.button_time);
		button_water = (Button) findViewById(R.id.button_water);
		button_deltat = (Button) findViewById(R.id.button_deltat);
		// button_ping = (Button) findViewById(R.id.button_ping);

		antenna_black_label_down = (TextView) findViewById(R.id.antenna_black_label_down);
		water_label_down = (TextView) findViewById(R.id.water_label_down);
		deltat_label_down = (TextView) findViewById(R.id.deltat_label_down);
		time_label_down = (TextView) findViewById(R.id.time_label_down);
		disturbo_label = (TextView) findViewById(R.id.disturbo_label);
		suggerimenti = (TextView) findViewById(R.id.suggerimenti);
		suggerimenti.setSingleLine(false);

	}

	private void disegna_grafico_lib() {

		// GraphView graph = (GraphView) findViewById(R.id.grafico);
		//
		// LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(
		// generateData());
		//
		// // styling grid/labels
		// graph.getGridLabelRenderer().setGridColor(Color.GRAY);
		// graph.getGridLabelRenderer().setHighlightZeroLines(false);
		// graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.GREEN);
		// graph.getGridLabelRenderer().setVerticalLabelsColor(Color.RED);
		// graph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
		// // graph.getGridLabelRenderer().setLabelVerticalWidth(150);
		// // graph.getGridLabelRenderer().setTextSize(40);
		// graph.getGridLabelRenderer().setGridStyle(
		// GridLabelRenderer.GridStyle.BOTH);
		// graph.getGridLabelRenderer().reloadStyles();
		//
		// // styling viewport
		// graph.getViewport().setBackgroundColor(Color.BLACK);
		//
		// // styling series
		// // series.setTitle("Random Curve 1");
		// series.setColor(Color.GREEN);
		// // series.setDrawDataPoints(true);
		// series.setDataPointsRadius(9);
		// series.setThickness(8);
		//
		// graph.addSeries(series);
		//
		// graph.getViewport().setScalable(true);
		//
		// graph.getViewport().setXAxisBoundsManual(true);
		// graph.getViewport().setMinX(-70);
		// graph.getViewport().setMaxX(70);
		//
		// graph.getViewport().setYAxisBoundsManual(true);
		// graph.getViewport().setMinY(37);
		// graph.getViewport().setMaxY(45);

		// GRAFICO_DEF
		// GraphView graph = (GraphView) findViewById(R.id.grafico);
		GraphView graph = null;

		// styling grid/labels
		graph.getGridLabelRenderer().setGridColor(Color.GRAY);
		graph.getGridLabelRenderer().setHighlightZeroLines(false);
		graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.GREEN);
		graph.getGridLabelRenderer().setVerticalLabelsColor(Color.RED);
		graph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
		graph.getGridLabelRenderer().setGridStyle(
				GridLabelRenderer.GridStyle.BOTH);
		graph.getGridLabelRenderer().reloadStyles();

		// styling viewport
		graph.getViewport().setBackgroundColor(Color.BLACK);

		Paint paint = new Paint();

		ArrayList<List<DataPoint>> values = generateData(1);

		mSeries380 = new LineGraphSeries<DataPoint>(values.get(0).toArray(
				new DataPoint[values.get(0).size()]));
		paint.setColor(Color.parseColor("#80007f"));
		mSeries380.setDataPointsRadius(3);
		mSeries380.setThickness(3);

		mSeries383 = new LineGraphSeries<DataPoint>(values.get(1).toArray(
				new DataPoint[values.get(1).size()]));
		paint.setColor(Color.parseColor("#0d0d74"));
		mSeries383.setCustomPaint(paint);
		mSeries383.setDataPointsRadius(3);
		mSeries383.setThickness(3);

		mSeries386 = new LineGraphSeries<DataPoint>(values.get(2).toArray(
				new DataPoint[values.get(2).size()]));
		paint.setColor(Color.parseColor("#0000c4"));
		mSeries386.setCustomPaint(paint);
		mSeries386.setDataPointsRadius(3);
		mSeries386.setThickness(3);

		mSeries389 = new LineGraphSeries<DataPoint>(values.get(3).toArray(
				new DataPoint[values.get(3).size()]));
		paint.setColor(Color.parseColor("#0000f6"));
		mSeries389.setCustomPaint(paint);
		mSeries389.setDataPointsRadius(3);
		mSeries389.setThickness(3);

		mSeries393 = new LineGraphSeries<DataPoint>(values.get(4).toArray(
				new DataPoint[values.get(4).size()]));
		paint.setColor(Color.parseColor("#007a7c"));
		mSeries393.setCustomPaint(paint);
		mSeries393.setDataPointsRadius(3);
		mSeries393.setThickness(3);

		mSeries396 = new LineGraphSeries<DataPoint>(values.get(5).toArray(
				new DataPoint[values.get(5).size()]));
		paint.setColor(Color.parseColor("#007c00"));
		mSeries396.setCustomPaint(paint);
		mSeries396.setDataPointsRadius(3);
		mSeries396.setThickness(3);

		mSeries399 = new LineGraphSeries<DataPoint>(values.get(6).toArray(
				new DataPoint[values.get(6).size()]));
		paint.setColor(Color.parseColor("#00b801"));
		mSeries399.setCustomPaint(paint);
		mSeries399.setDataPointsRadius(3);
		mSeries399.setThickness(3);

		mSeries402 = new LineGraphSeries<DataPoint>(values.get(7).toArray(
				new DataPoint[values.get(7).size()]));
		paint.setColor(Color.parseColor("#03f800"));
		mSeries402.setCustomPaint(paint);
		mSeries402.setDataPointsRadius(3);
		mSeries402.setThickness(3);

		mSeries405 = new LineGraphSeries<DataPoint>(values.get(8).toArray(
				new DataPoint[values.get(8).size()]));
		paint.setColor(Color.parseColor("#fef901"));
		mSeries405.setCustomPaint(paint);
		mSeries405.setDataPointsRadius(3);
		mSeries405.setThickness(3);

		mSeries408 = new LineGraphSeries<DataPoint>(values.get(9).toArray(
				new DataPoint[values.get(9).size()]));
		paint.setColor(Color.parseColor("#bffafd"));
		mSeries408.setCustomPaint(paint);
		mSeries408.setDataPointsRadius(3);
		mSeries408.setThickness(3);

		mSeries411 = new LineGraphSeries<DataPoint>(values.get(10).toArray(
				new DataPoint[values.get(10).size()]));
		paint.setColor(Color.parseColor("#fd0000"));
		mSeries411.setCustomPaint(paint);
		mSeries411.setDataPointsRadius(3);
		mSeries411.setThickness(3);

		mSeries414 = new LineGraphSeries<DataPoint>(values.get(11).toArray(
				new DataPoint[values.get(11).size()]));
		paint.setColor(Color.parseColor("#fbfbbc"));
		mSeries414.setCustomPaint(paint);
		mSeries414.setDataPointsRadius(3);
		mSeries414.setThickness(3);

		mSeries418 = new LineGraphSeries<DataPoint>(values.get(12).toArray(
				new DataPoint[values.get(12).size()]));
		paint.setColor(Color.parseColor("#ffd7bf"));
		mSeries418.setCustomPaint(paint);
		mSeries418.setDataPointsRadius(3);
		mSeries418.setThickness(3);

		mSeries421 = new LineGraphSeries<DataPoint>(values.get(13).toArray(
				new DataPoint[values.get(13).size()]));
		paint.setColor(Color.parseColor("#fcba7f"));
		mSeries421.setCustomPaint(paint);
		mSeries421.setDataPointsRadius(3);
		mSeries421.setThickness(3);

		mSeries424 = new LineGraphSeries<DataPoint>(values.get(14).toArray(
				new DataPoint[values.get(14).size()]));
		paint.setColor(Color.parseColor("#fd7b7f"));
		mSeries424.setCustomPaint(paint);
		mSeries424.setDataPointsRadius(3);
		mSeries424.setThickness(3);

		mSeries427 = new LineGraphSeries<DataPoint>(values.get(15).toArray(
				new DataPoint[values.get(15).size()]));
		paint.setColor(Color.parseColor("#fa0000"));
		mSeries427.setCustomPaint(paint);
		mSeries427.setDataPointsRadius(3);
		mSeries427.setThickness(3);

		graph.addSeries(mSeries380);
		graph.addSeries(mSeries383);
		graph.addSeries(mSeries386);
		graph.addSeries(mSeries389);
		if (mSeries393 != null) {
			graph.addSeries(mSeries393);
		}
		graph.addSeries(mSeries396);
		graph.addSeries(mSeries399);
		graph.addSeries(mSeries402);
		graph.addSeries(mSeries405);
		graph.addSeries(mSeries408);
		graph.addSeries(mSeries411);
		graph.addSeries(mSeries414);
		graph.addSeries(mSeries418);
		graph.addSeries(mSeries421);
		graph.addSeries(mSeries424);
		graph.addSeries(mSeries427);

		graph.getViewport().setXAxisBoundsManual(true);
		// graph.getViewport().setMinX(-70);
		// graph.getViewport().setMaxX(70);
		graph.getViewport().setMinX(-20);
		graph.getViewport().setMaxX(20);

		// graph.getViewport().setYAxisBoundsManual(true);
		// graph.getViewport().setMinY(37);
		// graph.getViewport().setMaxY(45);

	}

	private DataPoint[] generateData() {

		int count = 70;
		double f = 0;
		DataPoint[] values = new DataPoint[2 * count * count];
		int x = 0, i = 0;

		for (x = 0; x < count; x++) {

			for (int y = 0; y < count; y++) {

				f = function(x, y);

				DataPoint v = new DataPoint(x, f);
				DataPoint vn = new DataPoint(-x, f);
				values[i++] = v;
				values[i++] = vn;

			}
		}

		return values;
	}

	protected void disegna_grafico(int z) {

		Paint paint_griglia = new Paint();
		paint_griglia.setColor(Color.parseColor("#327277"));

		int asse_x = 140, asse_y = 70;

		float scala = 20f;

		Bitmap bg = Bitmap
				.createBitmap(asse_x, asse_y, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bg);

		canvas.save();

		canvas.drawColor(Color.BLACK);

		canvas.translate(asse_x / 2, -asse_y / 2 * scala - 39);

		float f = 0;

		// from -x to +x evaluate and plot the function
		for (int x = 0; x < 70; x++) {

			for (int y = 0; y < 70; y++) {

				f = function(x, y);

				Paint paint = new Paint();

				if (f < 38) {
					paint.setColor(Color.parseColor("#fa0000"));
				}
				if (f > 38 && f < 38.3) {
					paint.setColor(Color.parseColor("#fd7b7f"));
				}
				if (f > 38.3 && f < 38.6) {
					paint.setColor(Color.parseColor("#fcba7f"));
				}
				if (f > 38.6 && f < 38.9) {
					paint.setColor(Color.parseColor("#ffd7bf"));
				}
				if (f > 38.9 && f < 39.3) {
					paint.setColor(Color.parseColor("#fbfbbc"));
				}
				if (f > 39.3 && f < 39.6) {
					paint.setColor(Color.parseColor("#fd0000"));
				}
				if (f > 39.6 && f < 39.9) {
					paint.setColor(Color.parseColor("#bffafd"));
				}
				if (f > 39.9 && f < 40.2) {
					paint.setColor(Color.parseColor("#fef901"));
				}
				if (f > 40.2 && f < 40.5) {
					paint.setColor(Color.parseColor("#03f800"));
				}
				if (f > 40.5 && f < 40.8) {
					paint.setColor(Color.parseColor("#00b801"));
				}
				if (f > 40.8 && f < 41.1) {
					paint.setColor(Color.parseColor("#007c00"));
				}
				if (f > 41.1 && f < 41.4) {
					paint.setColor(Color.parseColor("#007a7c"));
				}
				if (f > 41.4 && f < 41.8) {
					paint.setColor(Color.parseColor("#0000f6"));
				}
				if (f > 41.8 && f < 42.1) {
					paint.setColor(Color.parseColor("#0000c4"));
				}
				if (f > 42.1 && f < 42.4) {
					paint.setColor(Color.parseColor("#0d0d74"));
				}
				if (f > 42.7) {
					paint.setColor(Color.parseColor("#80007f"));
				}

				for (int i = (int) -scala; i < scala; i++) {
					canvas.drawPoint((float) x + i, f * scala, paint);
					canvas.drawPoint((float) -x + i, f * scala, paint);
				}

			}
		}

		canvas.restore();

		// canvas.save();
		// int colonna = 20, riga = 5;
		//
		// for (int i = 0; i < asse_x; i += colonna) {
		// canvas.drawLine(i, 0, i, asse_y, paint_griglia);
		// }
		//
		// for (int j = 0; j < asse_y; j += riga) {
		// canvas.drawLine(0, j, asse_x, j, paint_griglia);
		// }
		// canvas.restore();

		// canvas.save();
		// String testo;
		// Paint paintTesto = new Paint();
		// paintTesto.setColor(Color.WHITE);
		// paintTesto.setTextSize(5);
		//
		// for (int i = 0; i <= asse_y; i += 5) {
		// if (i < 10) {
		// testo = " " + i;
		// } else {
		// testo = String.valueOf(i);
		// }
		// canvas.drawText(testo, 10, i, paintTesto);
		// }
		// canvas.restore();

		// LinearLayout ll = (LinearLayout) findViewById(R.id.grafico1);
		LinearLayout ll = null;
		ll.setBackgroundDrawable(new BitmapDrawable(bg));

	}

	private ArrayList<List<DataPoint>> generateData(int t) {

		int count = 20, asse_x = -count;
		double f = 0;

		List<DataPoint> mArray380, mArray383, mArray386, mArray389, mArray393, mArray396, mArray399, mArray402, mArray405, mArray408, mArray411, mArray414, mArray418, mArray421, mArray424, mArray427;
		ArrayList<List<DataPoint>> values = new ArrayList<List<DataPoint>>();

		mArray380 = new ArrayList<DataPoint>();
		mArray383 = new ArrayList<DataPoint>();
		mArray386 = new ArrayList<DataPoint>();
		mArray389 = new ArrayList<DataPoint>();
		mArray393 = new ArrayList<DataPoint>();
		mArray396 = new ArrayList<DataPoint>();
		mArray399 = new ArrayList<DataPoint>();
		mArray402 = new ArrayList<DataPoint>();
		mArray405 = new ArrayList<DataPoint>();
		mArray408 = new ArrayList<DataPoint>();
		mArray411 = new ArrayList<DataPoint>();
		mArray414 = new ArrayList<DataPoint>();
		mArray418 = new ArrayList<DataPoint>();
		mArray421 = new ArrayList<DataPoint>();
		mArray424 = new ArrayList<DataPoint>();
		mArray427 = new ArrayList<DataPoint>();

		for (int x = 0; x < count; x++) {

			asse_x++;

			for (int y = 0; y < count; y++) {

				// f = function(x, y, t);

				f = function(x, y);

				DataPoint v = new DataPoint(asse_x, -f);
				DataPoint vn = new DataPoint(-asse_x, -f);

				if (f < 38) {
					mArray380.add(v);
					mArray380.add(vn);
				}
				if (f > 38 && f < 38.3) {
					mArray383.add(v);
					mArray383.add(vn);
				}
				if (f > 38.3 && f < 38.6) {
					mArray386.add(v);
					mArray386.add(vn);
				}
				if (f > 38.6 && f < 38.9) {
					mArray389.add(v);
					mArray389.add(vn);
				}
				if (f > 38.9 && f < 39.3) {
					mArray393.add(v);
					mArray393.add(vn);
				}
				if (f > 39.3 && f < 39.6) {
					mArray396.add(v);
					mArray396.add(vn);
				}
				if (f > 39.6 && f < 39.9) {
					mArray399.add(v);
					mArray399.add(vn);
				}
				if (f > 39.9 && f < 40.2) {
					mArray402.add(v);
					mArray402.add(vn);
				}
				if (f > 40.2 && f < 40.5) {
					mArray405.add(v);
					mArray405.add(vn);
				}
				if (f > 40.5 && f < 40.8) {
					mArray408.add(v);
					mArray408.add(vn);
				}
				if (f > 40.8 && f < 41.1) {
					mArray411.add(v);
					mArray411.add(vn);
				}
				if (f > 41.1 && f < 41.4) {
					mArray414.add(v);
					mArray414.add(vn);
				}
				if (f > 41.4 && f < 41.8) {
					mArray418.add(v);
					mArray418.add(vn);
				}
				if (f > 41.8 && f < 42.1) {
					mArray421.add(v);
					mArray421.add(vn);
				}
				if (f > 42.1 && f < 42.4) {
					mArray424.add(v);
					mArray424.add(vn);
				}
				if (f > 42.7) {
					mArray427.add(v);
					mArray427.add(vn);
				}

			}
		}

		values.add(mArray380);
		values.add(mArray383);
		values.add(mArray386);
		values.add(mArray389);
		values.add(mArray393);
		values.add(mArray396);
		values.add(mArray399);
		values.add(mArray402);
		values.add(mArray405);
		values.add(mArray408);
		values.add(mArray411);
		values.add(mArray414);
		values.add(mArray418);
		values.add(mArray421);
		values.add(mArray424);
		values.add(mArray427);

		return values;
	}

	private void decrement() {

		if (funzionalita == button_water_left.getId()) {

			if (LONG)
				if (water_label_down.getText().equals("-00.0")) {
					water_label_down.setText("42");
				}

			if (Float.parseFloat(water_label_down.getText().toString()) > 35) {

				WATER -= 10;

				if (LONG)
					water_label_down.setText(""
							+ utility.arrotondaPerEccesso(WATER, 1));

				tracciato_out.setWaterOut(WATER);
				tracciato_out.setBuf();

			}

		}

		if (funzionalita == button_deltat_left.getId()) {

			if (LONG)
				if (deltat_label_down.getText().equals("-00.0")) {
					deltat_label_down.setText("3");
				}

			if (Float.parseFloat(deltat_label_down.getText().toString()) > -1) {

				DELTAT -= 10;

				if (LONG)
					if (DELTAT > 0) {
						deltat_label_down.setText("+"
								+ utility.arrotondaPerEccesso(DELTAT, 1));
					} else {
						deltat_label_down.setText(""
								+ utility.arrotondaPerEccesso(DELTAT, 1));
					}

				tracciato_out.setDeltaTOut(DELTAT);
				tracciato_out.setBuf();

			}

		}

		if (funzionalita == button_antenna_left.getId()) {

			int tot = Integer.parseInt(antenna_black_label_down.getText()
					.toString()) - 1;

			if (LONG)
				if (tot > 0) {
					antenna_black_label_down.setText("" + tot);
				} else {
					antenna_black_label_down.setText(String.valueOf(tot));
				}

			if (tot < 99) {
				if (LONG)
					antenna_black_label_down.setText("" + tot);

				tracciato_out.setPowerOut((int) (tot * 100));
				tracciato_out.setBuf();
			}

		}

		if (funzionalita == button_time_left.getId()) {

			int time = Integer.parseInt(time_label_down.getText().toString()
					.substring(0, 2));

			if (time > 1) {

				if (LONG)
					if (time - 1 < 10 && time - 1 > 0) {
						time_label_down.setText("0" + (time - 1));
					} else {
						time_label_down.setText("" + (time - 1));
					}

				if (LONG)
					if (time == 1) {
						time_label_down.setText("00");
					}

				tracciato_out.setTimerOut((time - 1) * 60);
				tracciato_out.setBuf();
			}

		}

	}

	private void increment() {

		if (funzionalita == button_water_right.getId()) {

			if (LONG)
				if (water_label_down.getText().equals("-00.0")) {
					water_label_down.setText("35");
				}

			if (Float.parseFloat(water_label_down.getText().toString()) < 42) {

				WATER += 10;

				if (LONG)
					water_label_down.setText(""
							+ utility.arrotondaPerEccesso(WATER, 1));

				tracciato_out.setWaterOut(WATER);
				tracciato_out.setBuf();

			}
		}

		if (funzionalita == button_deltat_right.getId()) {

			if (LONG)
				if (deltat_label_down.getText().equals("-00.0")) {
					deltat_label_down.setText("-1");
				}

			if (Float.parseFloat(deltat_label_down.getText().toString()) < 5) {

				DELTAT += 10;

				if (LONG)
					if (DELTAT > 0) {
						deltat_label_down.setText("+"
								+ utility.arrotondaPerEccesso(DELTAT, 1));
					} else {
						deltat_label_down.setText(""
								+ utility.arrotondaPerEccesso(DELTAT, 1));
					}

				tracciato_out.setDeltaTOut(DELTAT);
				tracciato_out.setBuf();

			}

		}

		if (funzionalita == button_antenna_right.getId()) {

			int tot = Integer.parseInt(antenna_black_label_down.getText()
					.toString()) + 1;

			if (LONG)
				if (tot > 0) {
					antenna_black_label_down.setText("" + tot);
				} else {
					antenna_black_label_down.setText(String.valueOf(tot));
				}

			if (tot < 99) {
				if (LONG)
					antenna_black_label_down.setText("" + (tot));

				tracciato_out.setPowerOut((int) (tot * 100));
				tracciato_out.setBuf();
			}

		}

		if (funzionalita == button_time_right.getId()) {

			int time = Integer.parseInt(time_label_down.getText().toString()
					.substring(0, 2));

			if (time < 30) {

				if (LONG)
					if (time + 1 < 10 && time + 1 > 0) {
						time_label_down.setText("0" + (time + 1));
					} else {
						time_label_down.setText("" + (time + 1));
					}

				tracciato_out.setTimerOut((time + 1) * 60);
				tracciato_out.setBuf();
			}
		}
	}

	private int buttonId;

	private void auto_increment(final Button button, final TextView textView,
			final int max, final int step) {

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (waitTimer != null) {
			waitTimer.cancel();
			inviaComandi(0, MSK_ALL_4, INOUT);
			waitTimer = null;
		}

		buttonId = button.getId();

		long count;

		if (buttonId == button_time_right.getId()) {

			count = Math.round(max
					- (Double.valueOf(textView.getText().toString()
							.substring(0, 2))))
					* step * 3 * 1000;

		} else {
			count = Math.round(max
					- (Double.valueOf(textView.getText().toString())))
					* step * 3 * 1000;
		}

		waitTimer = new CountDownTimer(count, 100) {

			public void onTick(long millisUntilFinished) {

				button.setPressed(true);
				funzionalita = button.getId();

				Double misurato;

				if (buttonId == button_time_right.getId()) {
					misurato = Double.valueOf(textView.getText().toString()
							.substring(0, 2));
				} else {
					misurato = Double.valueOf(textView.getText().toString());
				}

				if (max != misurato) {

					increment();

				} else {
					this.cancel();
				}

			}

			public void onFinish() {

			}
		}.start();

	}

	private void auto_decrement(final Button button, final TextView textView,
			final int min, final int step) {

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (waitTimer != null) {
			waitTimer.cancel();
			waitTimer = null;
		}

		buttonId = button.getId();

		long count;

		if (buttonId == button_time_left.getId()) {

			count = Math.round((Double.valueOf(textView.getText().toString()
					.substring(0, 2)))
					- min)
					* step * 3 * 1000 + 10000;

		} else {
			count = Math.round((Double.valueOf(textView.getText().toString()))
					- min)
					* step * 3 * 1000;
		}

		waitTimer = new CountDownTimer(count, 100) {

			public void onTick(long millisUntilFinished) {

				funzionalita = button.getId();

				Double misurato;

				if (buttonId == button_time_left.getId()) {

					misurato = Double.valueOf(textView.getText().toString()
							.substring(0, 2));

				} else {
					misurato = Double.valueOf(textView.getText().toString());
				}

				if (min != misurato) {

					decrement();

				} else {

					this.cancel();
				}

			}

			public void onFinish() {
			}
		}.start();

	}

}
