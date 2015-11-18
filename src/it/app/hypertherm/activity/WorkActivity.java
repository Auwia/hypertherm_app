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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.dwin.navy.serialportapi.SerialPortOpt;

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

	private LinearLayout ll;

	private static Utility utility;

	private Float[][] array_colori;

	private InputStream mInputStream;
	private OutputStream mOutputStream;

	private static BlockingQueue<byte[]> bq_out;
	private BlockingQueue<byte[]> bq_in;

	public static boolean SIMULATORE = false;
	public static boolean PING = false;
	public static boolean COMMUNICATION_READY = true;
	private boolean LONG = false;
	public static boolean AVVIO;

	private int funzionalita;

	public static int WATER, WATER_IMP = 0;
	public static int DELTAT, DELTAT_IMP = 0;
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
	private static int MSK = MSK_NOTHING;
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

	private static SharedPreferences preferences;

	private SerialPortOpt serialPort;

	private ReadThreadProducer mReadThreadProducer;
	private Simulatore sim;
	private ReadThreadConsumer mReadThreadConsumer;
	private WriteThread mWriteThread;
	private WritePing mWritePing;

	private CountDownTimer waitTimerBolusUp = null;
	private CountDownTimer waitTimer = null;
	private CountDownTimer waitTimerFlash = null;
	// private CountDownTimer waitTimerGrafico = null;

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
							inviaComandi(CMD, MSK, INOUT);
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
		seek_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				if (progress >= 0 && progress < 5) {
					tracciato_out.setWaterOut(WATER_IMP - 30 * (5 - progress));
					tracciato_out
							.setDeltaTOut(DELTAT_IMP - 10 * (5 - progress));

					utility.appendLog("I", "Aggiornata temperatura acqua:"
							+ (int) (WATER_IMP - 30 * (5 - progress)));
					utility.appendLog("I", "Aggiornato DELTAT:"
							+ (int) (DELTAT_IMP - 10 * (5 - progress)));

				} else if (progress >= 5 && progress <= 10) {
					tracciato_out.setWaterOut(WATER_IMP + 30 * (progress - 5));
					tracciato_out
							.setDeltaTOut(DELTAT_IMP + 10 * (progress - 5));

					utility.appendLog("I", "Aggiornata temperatura acqua:"
							+ (int) (WATER_IMP + 30 * (progress - 5)));
					utility.appendLog("I", "Aggiornato DELTAT:"
							+ (int) (DELTAT_IMP + 10 * (progress - 5)));
				}

				utility.appendLog("I", "Inviato comando: 4 ALL");
				inviaComandi(0, MSK_ALL_4, INOUT);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		android.view.ViewGroup.LayoutParams param = seek_bar.getLayoutParams();

		DisplayMetrics display = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(display);
		int width = display.widthPixels;

		param.width = width * 20 / 100;

		def_variable_components();

		def_bottun_click();

		def_value_defaults();

		disturbo_label.setText(preferences.getString("MENU_ITEM", "Defect"));

		if (disturbo_label.getText().toString()
				.contains(utility.getMenuItemDefault())) {

			disturbo_label.setTextColor(Color.parseColor("#ffa500"));

		} else {
			disturbo_label.setTextColor(Color.BLACK);
		}

	}

	public static final Handler aggiorna_def_value_defaults = new Handler() {

		public void handleMessage(Message msg) {

			def_value_defaults();
		}
	};

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

		tracciato_out.setWaterOut(WATER_IMP = (int) (preferences.getFloat(
				"WATER", 35) * 100));
		tracciato_out.setDeltaTOut(DELTAT_IMP = (int) (preferences.getFloat(
				"DELTAT", 1) * 100));
		tracciato_out.setPowerOut(preferences.getInt("ANTENNA", 0) * 100);
		tracciato_out.setTimerOut(preferences.getInt("TIME", 0) * 60);

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

					seek_bar.setProgress(5);

					button_power.setPressed(true);

					tracciato_out.setWaterOut(WATER_IMP);
					tracciato_out.setDeltaTOut(DELTAT_IMP);

					utility.appendLog("I", "Aggiornata temperatura acqua:"
							+ WATER_IMP);
					utility.appendLog("I", "Aggiornato DELTAT:" + DELTAT_IMP);
					utility.appendLog("I", "Inviato comando: 4 ALL");
					inviaComandi(0, MSK_ALL_4, INOUT);

					return false;

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

					}
				}

				return true;
			}
		});

		button_play.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				suggerimenti.setText("");

				CMD = 1;

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
				MSK = MSK_CMD;
				utility.appendLog("I", "Inviato comando: PLAY");
				inviaComandi(PLAY, MSK_CMD, INOUT);

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						disegna_grafico();

					}
				});

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

				if (waitTimerFlash != null) {
					waitTimerFlash.cancel();
					waitTimerFlash = null;
				}

				INOUT = STOP_TMP;

				if (button_play.isPressed()) {
					utility.appendLog("I", "Inviato comando: STOP");
					inviaComandi(STOP, MSK_CMD, INOUT);
				} else {
					disturbo_label.setText(String.valueOf(preferences
							.getString("MENU_ITEM", "Defect")));

					if (disturbo_label.getText().toString()
							.contains(utility.getMenuItemDefault())) {

						disturbo_label.setTextColor(Color.parseColor("#ffa500"));

					} else {
						disturbo_label.setTextColor(Color.BLACK);
					}

				}

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

						utility.appendLog("I", "Inviato comando: WATER-LEFT");
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
						utility.appendLog("I", "Inviato comando: WATER-RIGHT");
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
						utility.appendLog("I", "Inviato comando: DELTAT-LEFT");
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
						utility.appendLog("I", "Inviato comando: DELTAT-RIGHT");
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
						utility.appendLog("I", "Inviato comando: POWER-LEFT");
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

					utility.appendLog("I", "Inviato comando: POWER-RIGHT");
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

					utility.appendLog("I", "Inviato comando: TIME-LEFT");
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

					utility.appendLog("I", "Inviato comando: TIME-RIGHT");
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

	private float function_y(double y) {

		int B = 3;
		float Tw = 41;
		double b = 0.19;
		int Tb = 37;
		float Dt = 1.2f;
		double a = 0.035;
		double A = (B + 1) * Dt + Tw - Tb;

		double equation = ((B * Tw * Math.exp(-b * y) + Tb + A
				* Math.exp(-a * y))
				/ (B * Math.exp(-b * y) + 1) - Tb);

		return new Double(equation).floatValue();

	}

	private float function_x(double x) {

		double h = 0.001522;
		int x0 = 0;

		double equation = Math.exp(-h * Math.pow(x - x0, 2));

		return new Double(equation).floatValue();

	}

	protected void set_attention() {

		if (!disturbo_label.getText().toString()
				.contains(utility.getMenuItemDefault())) {

			disturbo_label.setTextColor(Color.parseColor("#ffa500"));

			waitTimerFlash = new CountDownTimer(5600, 700) {

				private int tot = 1;

				public void onTick(long millisUntilFinished) {

					if (tot % 2 == 0) {
						disturbo_label.setText("");

					} else {

						if (SIMULATORE) {
							disturbo_label.setText("DEMO - "
									+ utility.getMenuItemDefault());
						} else {
							disturbo_label
									.setText(utility.getMenuItemDefault());
						}

					}

					tot += 1;

				}

				public void onFinish() {

					if (SIMULATORE) {
						disturbo_label.setText("DEMO - "
								+ utility.getMenuItemDefault());
					} else {
						disturbo_label.setText(utility.getMenuItemDefault());
					}
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

		ll = (LinearLayout) findViewById(R.id.grafico1);

		array_colori = utility.getArrayColori();

	}

	protected void disegna_grafico() {

		Paint paint_griglia = new Paint();
		paint_griglia.setColor(Color.parseColor("#327277"));

		int asse_x = 140, asse_y = 70;

		Bitmap bg = Bitmap
				.createBitmap(asse_x, asse_y, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bg);
		canvas.save();
		canvas.drawColor(Color.BLACK);
		canvas.translate(asse_x / 2, 0);

		float f = 0;

		// from -x to +x evaluate and plot the function

		for (int y = 0; y < asse_y; y++) {

			double componente_y = function_y(y);

			for (int x = 0; x < asse_x / 2; x++) {

				Paint paint = new Paint();
				int Tb = 37;
				f = (float) (Tb + componente_y * function_x(x));

				int[] index = utility.find2DIndex(array_colori, f);

				if (index != null) {
					paint.setColor(Color.rgb(index[0], index[1], index[2]));

					// utility.appendLog("D", "Temp " + f + "°C RED=" + index[0]
					// + " GREEN=" + index[1] + " BLUE=" + index[2]);
					canvas.drawPoint((float) x, y, paint);
					canvas.drawPoint((float) -x, y, paint);
				}

			}
		}

		canvas.restore();

		ll.setBackgroundDrawable(new BitmapDrawable(bg));

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
