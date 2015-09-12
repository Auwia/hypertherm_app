package it.app.hypertherm.activity;

import it.app.hypertherm.PC_TO_CY;
import it.app.hypertherm.R;
import it.app.hypertherm.util.CountDownTimer;
import it.app.hypertherm.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;

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

public class WorkActivity extends Activity {

	private SeekBar seek_bar;
	private Button button_antenna_left, button_antenna_right,
			button_water_left, button_water_right, button_deltat_left,
			button_deltat_right, button_time_left, button_time_right,
			button_home, button_play, button_pause, button_stop,
			button_bolus_up, button_bolus_down, button_power,
			button_temperature_positive, button_temperature_negative,
			button_rf_on;
	private TextView antenna_black_label_down, water_label_down,
			deltat_label_down, time_label_down, disturbo_label, suggerimenti;
	private LinearLayout zero, dieci, venti, trenta, quaranta, cinquanta,
			sessanta, settanta, ottanta, novanta;

	private Handler repeatUpdateHandler = new Handler();

	private Utility utility;

	private int funzionalita, Ref_power, Dir_power, iTime, iD_temp, iH2o_temp,
			iPower;

	private final static int ROSSO = Color.parseColor("#ccff00");
	private final static int VERDE = Color.parseColor("#0000cc");
	private final static int MSK_CMD = 2;
	private final static int MSK_TIME = 4;
	private final static int MSK_DELTAT = 8;
	private final static int MSK_WATER = 16;
	private final static int MSK_POWER = 64;
	private final static int MSK_ALL_4 = 92;
	private final static int PLAY = 1;
	private final static int PAUSE = 2;
	private final static int STOP = 3;
	private final static int BOLUS_UP = 4;
	private final static int BOLUS_DOWN = 5;
	private final static int BOLUS_STOP = 6;
	private final static int RESET = 11;

	private boolean READ_ENABLE = true;

	private SharedPreferences preferences;

	private SerialPortOpt serialPort;

	private InputStream mInputStream;
	private ReadThread mReadThread;

	private byte[] writeusbdata = new byte[256];
	private StringBuffer readSB = new StringBuffer();

	public static write_thread writeThread;

	private CountDownTimer waitTimerBolusUp = null;
	private CountDownTimer waitTimer = null;

	private PC_TO_CY pctocy = new PC_TO_CY();

	public void inviaComandi(int comando, int maschera) {

		int timer = Integer
				.parseInt(time_label_down
						.getText()
						.toString()
						.substring(
								0,
								time_label_down.getText().toString().length() - 3)) * 60;

		int power = (int) (Integer.parseInt(antenna_black_label_down.getText()
				.toString()) * 100);
		int water = (int) (Float.parseFloat(water_label_down.getText()
				.toString()) * 100);
		int deltat = (int) (Float.parseFloat(deltat_label_down.getText()
				.toString()) * 100);

		for (int i = 0; i < 64; i++) { // Reset buffer
			pctocy.PSoCData[i] = (byte) 0;
		}

		pctocy.PSoCData[4] = (byte) maschera; // maschera 1
		pctocy.PSoCData[13] = (byte) comando; // Cmd
		pctocy.PSoCData[14] = (byte) (timer & 0xFF); // Tempo 1
		pctocy.PSoCData[15] = (byte) ((timer & 0xFF00) >> 8); // Tempo 2
		pctocy.PSoCData[16] = (byte) (deltat & 0xFF); // Tempo 1
		pctocy.PSoCData[17] = (byte) ((deltat & 0xFF00) >> 8); // Tempo 2
		pctocy.PSoCData[18] = (byte) (water & 0xFF); // Tempo 1
		pctocy.PSoCData[19] = (byte) ((water & 0xFF00) >> 8); // Tempo 2
		pctocy.PSoCData[22] = (byte) (power & 0xFF); // Tempo 1
		pctocy.PSoCData[23] = (byte) ((power & 0xFF00) >> 8); // Tempo 2
		pctocy.PSoCData[0] = (byte) (utility.calcola_check_sum(pctocy.PSoCData) & 0xFF);
		pctocy.PSoCData[1] = (byte) ((utility
				.calcola_check_sum(pctocy.PSoCData) & 0xFF00) >> 8);

		SendData(PC_TO_CY.PACKET_SIZE, pctocy.PSoCData);
	}

	public byte SendData(int numBytes, byte[] buffer) {
		byte status = 0x00; /* success by default */

		/*
		 * if num bytes are more than maximum limit
		 */
		if (numBytes < 1) {
			/* return the status with the error in the command */
			utility.appendLog("SendData: numero di byte nullo o negativo");
			return status;
		}

		/* check for maximum limit */
		if (numBytes > 256) {
			numBytes = 256;
			utility.appendLog("SendData: numero di byte superiore a 256byte");
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

			utility.appendLog("SendPacket: HO PERSO LA SCHEDA");

			e.printStackTrace();

		}
	}

	private class write_thread extends Thread {

		write_thread() {
		}

		public void run() {

			utility.appendLog("ENTRO NELLO SCRIVO");

			int exit = 0;

			while (READ_ENABLE) {

				// inviaComandi(0, MSK_WATER);
				exit += 1;

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

			}

			// READ_ENABLE = false;
			utility.appendLog("ESCO DALLO SCRIVO");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

		}
	}

	private class ReadThread extends Thread {

		private ReadThread(InputStream stream) {
			mInputStream = stream;
			this.setPriority(Thread.MAX_PRIORITY);
		}

		public void run() {
			super.run();

			utility.appendLog("ENTRO NEL LEGGO");

			byte[] buf = new byte[64];

			while (READ_ENABLE) {

				if (mInputStream == null)
					return;

				InputStream input_stream = serialPort.getInputStream();

				int readcount;
				try {

					Thread.sleep(1000);

					readcount = input_stream.read(buf, 0, 64);

					if (readcount > 0) {

						utility.appendLog("LETTO BUFFER NON NULLO:" + readcount
								+ buf.toString());

						if (readSB.toString().length() != 8) {

							int CheckSum = ((int) buf[0]) & 0xFF;
							CheckSum |= (((int) buf[1]) & 0xFF) << 8;
							int Ver = ((int) buf[2]) & 0xFF;
							int TimStmp = ((int) buf[3]) & 0xFF;

							byte[] msk = new byte[4];
							msk[0] = buf[4];
							msk[1] = buf[5];
							msk[2] = buf[6];
							msk[3] = buf[7];
							String msk_binary = utility.toBinary(msk);

							int Cmd = ((int) buf[12]) & 0xFF;
							Cmd |= (((int) buf[13]) & 0xFF) << 8;
							iTime = ((int) buf[14]) & 0xFF;
							iTime |= (((int) buf[15]) & 0xFF) << 8;
							iD_temp = ((int) buf[16]) & 0xFF;
							iD_temp |= (((int) buf[17]) & 0xFF) << 8;
							iH2o_temp = ((int) buf[18]) & 0xFF;
							iH2o_temp |= (((int) buf[19]) & 0xFF) << 8;
							iPower = ((int) buf[22]) & 0xFF;
							iPower |= (((int) buf[23]) & 0xFF) << 8;
							Dir_power = ((int) buf[42]) & 0xFF;
							Dir_power |= (((int) buf[43]) & 0xFF) << 8;
							Ref_power = ((int) buf[44]) & 0xFF;
							Ref_power |= (((int) buf[45]) & 0xFF) << 8;
							int D_temp = ((int) buf[46]) & 0xFF;
							D_temp |= (((int) buf[47]) & 0xFF) << 8;
							int H2o_temp = ((int) buf[48]) & 0xFF;
							H2o_temp |= (((int) buf[49]) & 0xFF) << 8;
							int runningTime = ((int) buf[54]) & 0xFF;
							runningTime |= (((int) buf[55]) & 0xFF) << 8;

							if (utility.calcola_check_sum(buf) == CheckSum
									|| utility.calcola_check_sum(buf) + 1 == CheckSum) {

								utility.appendLog("COMANDO_RICEVUTO:"
										+ "CheckSum="
										+ CheckSum
										+ " Ver="
										+ Ver
										+ " TimStmp="
										+ TimStmp
										+ " Msk="
										+ msk_binary
										+ " Cmd="
										+ Cmd
										+ " iTime="
										+ iTime
										+ " iD_temp="
										+ iD_temp
										+ " iH2o_temp="
										+ iH2o_temp
										+ " iPower="
										+ iPower
										+ " Dir_power="
										+ Dir_power
										+ " Ref_power="
										+ Ref_power
										+ " D_temp="
										+ D_temp
										+ " H2o_temp="
										+ H2o_temp
										+ " runningTime="
										+ runningTime
										+ " runningTime="
										+ runningTime);

								runOnUiThread(new Runnable() {
									@Override
									public void run() {

										setColoriPiramide(Ref_power / 100);

									}
								});

								utility.esegui(Cmd);

								utility.SetTime(runningTime / 60 + ":00");

								int d_temp = 0;
								if (D_temp >= 60000) {
									d_temp = (D_temp - 65536);
								} else {
									d_temp = D_temp;
								}

								String a;

								if (d_temp < 0) {
									a = String.valueOf(d_temp).substring(0, 1)
											+ "0."
											+ String.valueOf(d_temp).substring(
													1, 2);
								} else {
									a = String.valueOf(d_temp).substring(0, 1)
											+ "."
											+ String.valueOf(d_temp).substring(
													1, 2);
								}

								utility.setDeltaT(a);

								utility.setWaterTemperature(String.valueOf(
										H2o_temp).substring(0, 2)
										+ "."
										+ String.valueOf(H2o_temp).substring(2,
												3));

								utility.setAntenna("" + (int) (Dir_power / 100));

								runOnUiThread(new Runnable() {
									@Override
									public void run() {

										// float id_temp = iD_temp / 100;
										//
										// water_label_down.setText(String
										// .valueOf(iH2o_temp).substring(
										// 0, 2)
										// + "."
										// + String.valueOf(iH2o_temp)
										// .substring(2, 3));
										//
										// deltat_label_down.setText(String
										// .valueOf(id_temp));
										//
										// antenna_black_label_down.setText(""
										// + ((int) (iPower / 100)));
										//
										// time_label_down.setText(iTime / 60
										// + ":00");

									}
								});

							} else {
								utility.appendLog("Tracciato non conforme al checksum atteso="
										+ CheckSum
										+ " checksum ricevuto="
										+ utility.calcola_check_sum(buf));
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			utility.appendLog("ESCO DAL LEGGO");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

		}

		private void setColoriPiramide(int Ref_power) {

			int MAX = (int) Float.parseFloat(antenna_black_label_down.getText()
					.toString());

			if (Ref_power < MAX / 10) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(VERDE);

				venti.setBackgroundColor(VERDE);

				trenta.setBackgroundColor(VERDE);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power + Dir_power < MAX / 10) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(ROSSO);

				sessanta.setBackgroundColor(ROSSO);

				settanta.setBackgroundColor(ROSSO);

				ottanta.setBackgroundColor(ROSSO);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power < MAX / 10 * 2 && Ref_power > MAX / 10) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(VERDE);

				trenta.setBackgroundColor(VERDE);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power + Dir_power < MAX / 10 * 2
					&& Ref_power + Dir_power > MAX / 10) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(ROSSO);

				sessanta.setBackgroundColor(ROSSO);

				settanta.setBackgroundColor(ROSSO);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power < MAX / 10 * 3 && Ref_power > MAX / 10 * 2) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(VERDE);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power + Dir_power < MAX / 10 * 3
					&& Ref_power + Dir_power > MAX / 10 * 2) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(VERDE);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power < MAX / 10 * 4 && Ref_power > MAX / 10 * 3) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power + Dir_power < MAX / 10 * 4
					&& Ref_power + Dir_power > MAX / 10 * 3) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(ROSSO);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power < MAX / 10 * 5 && Ref_power > MAX / 10 * 4) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(ROSSO);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power + Dir_power < MAX / 10 * 5
					&& Ref_power + Dir_power > MAX / 10 * 4) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power < MAX / 10 * 6 && Ref_power > MAX / 10 * 5) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(ROSSO);

				sessanta.setBackgroundColor(ROSSO);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power + Dir_power < MAX / 10 * 6
					&& Ref_power + Dir_power > MAX / 10 * 5) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power < MAX / 10 * 7 && Ref_power > MAX / 10 * 6) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(ROSSO);

				sessanta.setBackgroundColor(ROSSO);

				settanta.setBackgroundColor(ROSSO);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power + Dir_power < MAX / 70
					&& Ref_power + Dir_power > MAX / 60) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(VERDE);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power < MAX / 80 && Ref_power > MAX / 70) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(ROSSO);

				sessanta.setBackgroundColor(ROSSO);

				settanta.setBackgroundColor(ROSSO);

				ottanta.setBackgroundColor(ROSSO);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power + Dir_power < MAX / 80
					&& Ref_power + Dir_power > MAX / 70) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(VERDE);

				trenta.setBackgroundColor(VERDE);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(ROSSO);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power < MAX / 90 && Ref_power > MAX / 80) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(ROSSO);

				sessanta.setBackgroundColor(ROSSO);

				settanta.setBackgroundColor(ROSSO);

				ottanta.setBackgroundColor(ROSSO);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power + Dir_power < MAX / 90
					&& Ref_power + Dir_power > MAX / 80) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(VERDE);

				venti.setBackgroundColor(VERDE);

				trenta.setBackgroundColor(VERDE);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

			if (Ref_power < MAX / 100 && Ref_power > MAX / 90) {

				zero.setBackgroundColor(ROSSO);

				dieci.setBackgroundColor(ROSSO);

				venti.setBackgroundColor(ROSSO);

				trenta.setBackgroundColor(ROSSO);

				quaranta.setBackgroundColor(ROSSO);

				cinquanta.setBackgroundColor(ROSSO);

				sessanta.setBackgroundColor(ROSSO);

				settanta.setBackgroundColor(ROSSO);

				ottanta.setBackgroundColor(ROSSO);

				novanta.setBackgroundColor(ROSSO);

			}

			if (Ref_power + Dir_power < MAX / 100
					&& Ref_power + Dir_power > MAX / 90) {

				zero.setBackgroundColor(VERDE);

				dieci.setBackgroundColor(VERDE);

				venti.setBackgroundColor(VERDE);

				trenta.setBackgroundColor(VERDE);

				quaranta.setBackgroundColor(VERDE);

				cinquanta.setBackgroundColor(VERDE);

				sessanta.setBackgroundColor(VERDE);

				settanta.setBackgroundColor(VERDE);

				ottanta.setBackgroundColor(VERDE);

				novanta.setBackgroundColor(VERDE);

			}

		}
	}

	@Override
	public void onPause() {
		super.onPause();

		utility.appendLog("STO USCENDO");

		READ_ENABLE = false;

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
		mReadThread = new ReadThread(mInputStream);
		mReadThread.start();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_work);

		utility = new Utility(this);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		initSerialPort();

		seek_bar = (SeekBar) findViewById(R.id.seek_bar);
		seek_bar.setMax(10);
		seek_bar.setProgress(5);

		android.view.ViewGroup.LayoutParams param = seek_bar.getLayoutParams();

		DisplayMetrics display = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(display);
		int width = display.widthPixels;

		param.width = width * 20 / 100;

		def_variable_components();

		def_bottun_click();

		def_value_defaults();

		writeThread = new write_thread();
		if (!writeThread.isAlive() && writeThread.getState() != State.RUNNABLE) {
			writeThread.setName("Thread_Scrittura");
			writeThread.start();

		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		inviaComandi(0, MSK_ALL_4);

	}

	private void def_value_defaults() {

		water_label_down.setText(String.valueOf(preferences.getFloat("WATER",
				35)));

		if (preferences.getFloat("DELTAT", 1) >= 0) {
			deltat_label_down.setText("+"
					+ String.valueOf(preferences.getFloat("DELTAT", 1)));
		} else {
			deltat_label_down.setText("-"
					+ String.valueOf(preferences.getFloat("DELTAT", 1)));
		}

		antenna_black_label_down.setText(String.valueOf(preferences.getInt(
				"ANTENNA", 0)));
		time_label_down.setText(String.valueOf(preferences.getInt("TIME", 0))
				+ ":00");

		disturbo_label.setText(String.valueOf(preferences.getString(
				"MENU_ITEM", "Defect")));

		if (disturbo_label.getText().toString()
				.equals(utility.getMenuItemDefault())) {

			disturbo_label.setTextColor(Color.parseColor("#ffa500"));

			button_power.setPressed(true);

		} else {

			disturbo_label.setTextColor(Color.BLACK);

		}

		suggerimenti.setText(utility.get_suggerimento_trattamento());

		button_home.setEnabled(true);

	}

	private void def_bottun_click() {

		button_power.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					seek_bar.setProgress(5);

					if (button_power.isPressed()) {

						button_power.setPressed(false);

						button_temperature_negative.setEnabled(true);
						button_temperature_positive.setEnabled(true);

						return true;

					} else {

						button_power.setPressed(true);

						button_temperature_negative.setEnabled(false);
						button_temperature_positive.setEnabled(false);

						return false;

					}

				}

				return true;
			}
		});

		button_temperature_negative
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						azzera();

						if (!button_power.isPressed()
								&& seek_bar.getProgress() < 10) {

							button_rf_on.setPressed(true);

							seek_bar.setProgress(seek_bar.getProgress() + 1);

							waitTimer = new CountDownTimer(1000, 1000) {

								public void onTick(long millisUntilFinished) {

								}

								public void onFinish() {

									// water_label_down.setText(""
									// + (Float.parseFloat(water_label_down
									// .getText().toString()) - 0.3f));

									funzionalita = button_water_left.getId();
									decrement();
									decrement();
									decrement();

									// deltat_label_down.setText(""
									// + (Float.parseFloat(deltat_label_down
									// .getText().toString()) - 0.1f));

									funzionalita = button_deltat_left.getId();
									decrement();

									if (!disturbo_label
											.getText()
											.toString()
											.equals(utility
													.getMenuItemDefault())) {

										antenna_black_label_down.setText(""
												+ utility.getPmaxRF(
														Float.parseFloat(deltat_label_down
																.getText()
																.toString()),
														Float.parseFloat(water_label_down
																.getText()
																.toString())));
									}

									inviaComandi(0, MSK_ALL_4);

									button_rf_on.setPressed(false);

								}
							}.start();

						}

					}
				});

		button_temperature_positive
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						azzera();

						if (!button_power.isPressed()
								&& seek_bar.getProgress() > 0) {

							button_rf_on.setPressed(true);

							seek_bar.setProgress(seek_bar.getProgress() - 1);

							waitTimer = new CountDownTimer(1000, 1000) {

								public void onTick(long millisUntilFinished) {

								}

								public void onFinish() {

									funzionalita = button_water_right.getId();
									increment();
									increment();
									increment();

									funzionalita = button_deltat_right.getId();
									increment();

									if (!disturbo_label
											.getText()
											.toString()
											.equals(utility
													.getMenuItemDefault())) {

										antenna_black_label_down.setText(""
												+ utility.getPmaxRF(
														Float.parseFloat(deltat_label_down
																.getText()
																.toString()),
														Float.parseFloat(water_label_down
																.getText()
																.toString())));
									}

									inviaComandi(0, MSK_ALL_4);

									button_rf_on.setPressed(false);

								}
							}.start();
						}

					}
				});

		button_play.setOnClickListener(new View.OnClickListener() {

			private int i = 0;

			public void onClick(View v) {

				azzera();

				suggerimenti.setText("");

				// waitTimer = new CountDownTimer(30000, 1000) {
				//
				// public void onTick(long millisUntilFinished) {
				//
				// disegna_grafico(i++);
				//
				// }
				//
				// public void onFinish() {
				//
				// }
				// }.start();

				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						// disegna_grafico(100);

						button_rf_on.setPressed(true);

					}
				});

				if (preferences.getString("PROFONDITA", "1").equals("4")) {

					inviaComandi(PLAY, MSK_CMD);

					utility.appendLog("Lancio programma DINAMICO");

					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							utility.appendLog("Attendo 5 minuti");

							waitTimer = new CountDownTimer(30000, 30000) {

								public void onTick(long millisUntilFinished) {

								}

								public void onFinish() {
									utility.appendLog("Setto il valore della temperatura dell'acqua al livello intermedio");

									water_label_down.setText(String.valueOf(utility
											.getWaterTemperature(preferences
													.getString("STRUTTURA",
															"Mix"), "2")));

									inviaComandi(0, MSK_WATER);

									utility.appendLog("Attendo 1 minuto");

									waitTimer = new CountDownTimer(6000, 6000) {

										public void onTick(
												long millisUntilFinished) {

										}

										public void onFinish() {

											utility.appendLog("Setto i 3 parametri al livello intermedio");

											deltat_label_down.setText(String.valueOf(utility.getDeltaT(
													preferences.getString(
															"STRUTTURA", "Mix"),
													"2")));

											antenna_black_label_down.setText(String.valueOf(utility.getAntenna(
													preferences.getString(
															"STRUTTURA", "Mix"),
													"2")));

											time_label_down.setText(String.valueOf(utility.getTime(
													preferences.getString(
															"STRUTTURA", "Mix"),
													"2")));

											inviaComandi(0, MSK_POWER);

											utility.appendLog("Attendo 6 minuti");

											waitTimer = new CountDownTimer(
													36000, 36000) {

												public void onTick(
														long millisUntilFinished) {

												}

												public void onFinish() {
													utility.appendLog("Setto il valore della temperatura dell'acqua al livello profondo");

													water_label_down.setText(String.valueOf(utility
															.getWaterTemperature(
																	preferences
																			.getString(
																					"STRUTTURA",
																					"Mix"),
																	"3")));

													inviaComandi(0, MSK_POWER);

													utility.appendLog("Attendo 1 minuto");

													waitTimer = new CountDownTimer(
															6000, 6000) {

														public void onTick(
																long millisUntilFinished) {

														}

														public void onFinish() {
															utility.appendLog("Setto i 3 parametri al livello profondo");

															deltat_label_down
																	.setText(String
																			.valueOf(utility
																					.getDeltaT(
																							preferences
																									.getString(
																											"STRUTTURA",
																											"Mix"),
																							"3")));

															antenna_black_label_down
																	.setText(String
																			.valueOf(utility
																					.getAntenna(
																							preferences
																									.getString(
																											"STRUTTURA",
																											"Mix"),
																							"3")));

															time_label_down
																	.setText(String
																			.valueOf(utility
																					.getTime(
																							preferences
																									.getString(
																											"STRUTTURA",
																											"Mix"),
																							"3")));

															inviaComandi(0,
																	MSK_POWER);

															utility.appendLog("Attendo 7 minuti");

															waitTimer = new CountDownTimer(
																	42000,
																	42000) {

																public void onTick(
																		long millisUntilFinished) {

																}

																public void onFinish() {

																}
															}.start();

														}
													}.start();

												}
											}.start();

										}
									}.start();

								}
							}.start();

						}
					});

				} else {

					utility.appendLog("Inviato comando: PLAY");
					inviaComandi(PLAY, MSK_CMD);

					button_home.setEnabled(false);

				}
			}
		});

		button_pause.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				utility.appendLog("Inviato comando: PAUSE");
				button_rf_on.setPressed(false);
				azzera();
				inviaComandi(PAUSE, MSK_CMD);
			}
		});

		button_stop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				utility.appendLog("Inviato comando: STOP");
				button_rf_on.setPressed(false);
				azzera();
				inviaComandi(STOP, MSK_CMD);
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

						utility.appendLog("Inviato comando: BOLUS-STOP");
						inviaComandi(BOLUS_STOP, MSK_CMD);

						return true;

					} else {

						if (!button_bolus_down.isPressed()) {

							button_bolus_down.setPressed(true);

							utility.appendLog("Inviato comando: BOLUS-DOWN");
							inviaComandi(BOLUS_DOWN, MSK_CMD);

							waitTimer = new CountDownTimer(30000, 30000) {

								public void onTick(long millisUntilFinished) {

								}

								public void onFinish() {
									button_bolus_down.setPressed(false);
								}
							}.start();

							return false;

						} else {

							if (waitTimer != null) {
								waitTimer.cancel();
								waitTimer = null;
							}

							utility.appendLog("Inviato comando: BOLUS-STOP");
							inviaComandi(BOLUS_STOP, MSK_CMD);

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

						utility.appendLog("Inviato comando: BOLUS-STOP");
						inviaComandi(BOLUS_STOP, MSK_CMD);

						return true;

					} else {

						if (!button_bolus_up.isPressed()) {

							button_bolus_up.setPressed(true);

							utility.appendLog("Inviato comando: BOLUS-UP");
							inviaComandi(BOLUS_UP, MSK_CMD);

							waitTimerBolusUp = new CountDownTimer(30000, 30000) {

								public void onTick(long millisUntilFinished) {

								}

								public void onFinish() {
									button_bolus_up.setPressed(false);

								}
							}.start();

							return false;

						} else {

							if (waitTimerBolusUp != null) {
								waitTimerBolusUp.cancel();
								waitTimerBolusUp = null;
							}

							utility.appendLog("Inviato comando: BOLUS-STOP");
							inviaComandi(BOLUS_STOP, MSK_CMD);

							button_bolus_up.setPressed(false);

							return true;
						}

					}
				}

				return true;

			}
		});

		button_antenna_left.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (antenna_black_label_down.getText().equals("-00.0")) {
					antenna_black_label_down.setText("0");
				}

				azzera();

				if (Float.parseFloat(antenna_black_label_down.getText()
						.toString()) > 0) {
					antenna_black_label_down.setText(String.valueOf(Integer
							.parseInt(antenna_black_label_down.getText()
									.toString()) - 1));
				}

				set_attention();

				utility.appendLog("Inviato comando: ANTENNA-LEFT");

				inviaComandi(0, MSK_POWER);

			}
		});

		button_antenna_right.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (antenna_black_label_down.getText().equals("-00.0")) {
					antenna_black_label_down.setText("0");
				}

				azzera();

				if (Integer.parseInt(antenna_black_label_down.getText()
						.toString()) < 99) {
					antenna_black_label_down.setText(String.valueOf(Integer
							.parseInt(antenna_black_label_down.getText()
									.toString()) + 1));
				}

				set_attention();

				utility.appendLog("Inviato comando: ANTENNA-RIGHT");
				inviaComandi(0, MSK_POWER);

			}
		});

		button_water_left.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (water_label_down.getText().equals("-00.0")) {
					water_label_down.setText("42");
				}

				azzera();

				if (Float.parseFloat(water_label_down.getText().toString()) > 35) {

					float tot = (Float.parseFloat(water_label_down.getText()
							.toString()) * 10 - 1) / 10;

					water_label_down.setText(String.valueOf(tot));

				}

				set_attention();

				utility.appendLog("Inviato comando: WATER-LEFT");
				inviaComandi(0, MSK_WATER);

			}
		});

		button_water_right.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (water_label_down.getText().equals("-00.0")) {
					water_label_down.setText("35");
				}

				azzera();

				if (Float.parseFloat(water_label_down.getText().toString()) < 42) {

					float tot = (Float.parseFloat(water_label_down.getText()
							.toString()) * 10 + 1) / 10;

					water_label_down.setText(String.valueOf(tot));
				}

				set_attention();

				utility.appendLog("Inviato comando: WATER-RIGHT");
				inviaComandi(0, MSK_WATER);

			}
		});

		button_deltat_left.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (deltat_label_down.getText().equals("-00.0")) {
					deltat_label_down.setText("+3");
				}

				azzera();

				if (Float.parseFloat(deltat_label_down.getText().toString()) > -1) {

					float tot = (Float.parseFloat(deltat_label_down.getText()
							.toString()) * 10 - 1) / 10;

					if (tot > 0) {
						deltat_label_down.setText("+" + tot);
					} else {
						deltat_label_down.setText(String.valueOf(tot));
					}

				}

				set_attention();

				utility.appendLog("Inviato comando: DELTAt-LEFT");
				inviaComandi(0, MSK_DELTAT);

			}
		});

		button_deltat_right.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (deltat_label_down.getText().equals("-00.0")) {
					deltat_label_down.setText("-1");
				}

				azzera();

				if (Float.parseFloat(deltat_label_down.getText().toString()) < 3) {

					float tot = (Float.parseFloat(deltat_label_down.getText()
							.toString()) * 10 + 1) / 10;

					if (tot > 0) {
						deltat_label_down.setText("+" + tot);
					} else {
						deltat_label_down.setText(String.valueOf(tot));
					}

				}

				set_attention();

				utility.appendLog("Inviato comando: DELTAT-RIGHT");
				inviaComandi(0, MSK_DELTAT);

			}
		});

		button_time_left.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				azzera();

				int time = Integer
						.parseInt(time_label_down
								.getText()
								.toString()
								.substring(
										0,
										time_label_down.getText().toString()
												.length() - 3));

				if (time > 0) {

					time_label_down.setText("" + (time - 1) + ":00");

				}

				utility.appendLog("Inviato comando: TIMER-LEFT");
				inviaComandi(0, MSK_TIME);

			}
		});

		button_time_right.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				azzera();

				int time = Integer
						.parseInt(time_label_down
								.getText()
								.toString()
								.substring(
										0,
										time_label_down.getText().toString()
												.length() - 3));

				if (time < 30) {

					time_label_down.setText("" + (time + 1) + ":00");

				}

				utility.appendLog("Inviato comando: TIMER-RIGHT");
				inviaComandi(0, MSK_TIME);

			}
		});

		// AUTOMATICI

		button_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				azzera();

				Intent intent = new Intent(WorkActivity.this,
						MainActivity.class);
				startActivity(intent);

			}
		});

		button_water_left
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						azzera();

						funzionalita = button_water_left.getId();

						auto_decrement(button_water_left, water_label_down, 35,
								10);

						return true;
					}
				});

		button_water_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)) {
					funzionalita = button_water_left.getId();

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (waitTimer != null) {
						waitTimer.cancel();
						inviaComandi(0, MSK_ALL_4);
						waitTimer = null;
					}

				}
				return false;
			}
		});

		button_water_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						azzera();

						funzionalita = button_water_right.getId();

						auto_increment(button_water_right, water_label_down,
								42, 10);

						return true;
					}
				});

		button_water_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)) {
					funzionalita = button_water_right.getId();

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (waitTimer != null) {
						waitTimer.cancel();
						inviaComandi(0, MSK_ALL_4);
						waitTimer = null;
					}

				}
				return false;
			}
		});

		button_deltat_left
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						azzera();

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
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)) {
					funzionalita = button_deltat_left.getId();

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (waitTimer != null) {
						waitTimer.cancel();
						inviaComandi(0, MSK_ALL_4);
						waitTimer = null;
					}

				}

				return false;
			}
		});

		button_deltat_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						azzera();

						funzionalita = button_deltat_right.getId();

						auto_increment(button_deltat_right, deltat_label_down,
								3, 10);

						return true;
					}
				});

		button_deltat_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)) {
					funzionalita = button_deltat_right.getId();

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (waitTimer != null) {
						waitTimer.cancel();
						inviaComandi(0, MSK_ALL_4);
						waitTimer = null;
					}

				}

				return false;
			}
		});

		button_antenna_left
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						azzera();

						funzionalita = button_antenna_left.getId();

						auto_decrement(button_antenna_left,
								antenna_black_label_down, 0, 1);
						return true;
					}
				});

		button_antenna_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)) {
					funzionalita = button_antenna_left.getId();

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (waitTimer != null) {
						waitTimer.cancel();
						inviaComandi(0, MSK_ALL_4);
						waitTimer = null;
					}

				}

				return false;
			}
		});

		button_antenna_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						azzera();

						funzionalita = button_antenna_right.getId();
						auto_increment(button_antenna_right,
								antenna_black_label_down, 99, 1);

						return true;
					}
				});

		button_time_left.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {

				azzera();

				funzionalita = button_time_left.getId();

				auto_decrement(button_time_left, time_label_down, 0, 1);

				return true;
			}
		});

		button_time_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)) {
					funzionalita = button_time_left.getId();
				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (waitTimer != null) {
						waitTimer.cancel();
						inviaComandi(0, MSK_ALL_4);
						waitTimer = null;
					}

				}

				return false;
			}
		});

		button_time_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {

						azzera();

						funzionalita = button_time_right.getId();
						auto_increment(button_time_right, time_label_down, 30,
								1);

						return true;
					}
				});

		button_time_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)) {
					funzionalita = button_time_right.getId();

				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (waitTimer != null) {
						waitTimer.cancel();
						inviaComandi(0, MSK_ALL_4);
						waitTimer = null;
					}

				}

				return false;
			}
		});

	}

	protected void azzera() {

		if (waitTimer != null) {
			waitTimer.cancel();
			inviaComandi(0, MSK_ALL_4);
			waitTimer = null;
		}

		button_antenna_left.setPressed(false);
		button_antenna_right.setPressed(false);
		button_water_left.setPressed(false);
		button_water_right.setPressed(false);
		button_deltat_left.setPressed(false);
		button_deltat_right.setPressed(false);
		button_time_left.setPressed(false);
		button_time_right.setPressed(false);

	}

	protected void disegna_grafico_bkp() {
		Paint paintRed = new Paint();
		paintRed.setColor(Color.RED);

		Paint paintBlu = new Paint();
		paintBlu.setColor(Color.BLUE);

		Bitmap bg = Bitmap.createBitmap(480, 800, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bg);

		MeasuredAxis measured = new MeasuredAxis(canvas.getWidth(),
				canvas.getHeight());

		synchronized (canvas) {
			int x = measured.getXAxisNegLimit();
			// from -x to +x evaluate and plot the function
			while (x++ < measured.getXAxisLimit()) {
				canvas.drawLine(measured.getXMeasured(x), measured
						.getYMeasured(function(x, 0, 0)),

				measured.getYMeasured(function(x + MeasuredAxis.Y_GAP, 0, 0)),
						measured.getXMeasured(x + MeasuredAxis.X_GAP),

						paintRed);
			}
			canvas.save();
			canvas.translate(0, 0);
			canvas.scale(canvas.getWidth(), canvas.getHeight());
			canvas.restore();

		}

		LinearLayout ll = (LinearLayout) findViewById(R.id.grafico);
		ll.setBackgroundDrawable(new BitmapDrawable(bg));

	}

	protected void disegna_grafico(int z) {
		Paint paintRed = new Paint();
		paintRed.setColor(Color.RED);

		Paint paintBlu = new Paint();
		paintBlu.setColor(Color.BLUE);

		Bitmap bg = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bg);

		MeasuredAxis measured = new MeasuredAxis(canvas.getWidth(),
				canvas.getHeight());

		synchronized (canvas) {
			int x = measured.getXAxisNegLimit();
			int y = measured.getXAxisNegLimit();

			// from -x to +x evaluate and plot the function
			for (double x1 = 370; x1 <= 420; x1 += 0.1) {

				for (int y1 = 0; y1 < measured.getYAxisLimit(); y1++) {

					// canvas.drawLine(
					// measured.getXMeasured(x1),
					// measured.getYMeasured(function(x1, y, 100)),
					// measured.getXMeasured(x1 + MeasuredAxis.X_GAP),
					// measured.getYMeasured(function(x1
					// + MeasuredAxis.Y_GAP, y1, 100)), paintRed);

					canvas.drawPoint(function(x1, y1, 100), (float) x1,
							paintBlu);

					// canvas.drawLine(
					// measured.getXMeasured(x1),
					// measured.getYMeasured(function(x1, y1, 100)),
					// measured.getXMeasured(x1 + MeasuredAxis.X_GAP),
					// measured.getYMeasured(function(x1
					// + MeasuredAxis.Y_GAP, y1, 100)), paintRed);

					utility.appendLog("x=" + x1 + " y=" + function(x1, y1, 100));
				}
			}
			canvas.save();
			canvas.translate(0, 0);
			canvas.scale(canvas.getWidth(), canvas.getHeight());
			canvas.restore();

		}

		LinearLayout ll = (LinearLayout) findViewById(R.id.grafico);
		ll.setBackgroundDrawable(new BitmapDrawable(bg));

	}

	private float function(double x, double y, double z) {

		int B = 3, n = 0;
		float W, T, D;
		float Tw = W = pctocy.iH2o_temp;
		double b = 0.19;
		float Tb = T = 37;
		float Dt = D = pctocy.iD_temp;
		double a = 0.035;
		int X = pctocy.runningTime;
		double A = (B + 1) * D + W - T;
		double h = 0.011522;
		double k = 0.011513;
		int x0 = n = 70;

		double equation = T
				+ ((B * W * Math.exp(-b * y) + T + A * Math.exp(-a * y))
						/ (B * Math.exp(-b * y) + 1) - T)
				* Math.exp(-h * Math.pow(x - n, 2) * (1 - Math.exp(-k * z)));

		// return new Double((B * Tw * Math.exp(-b * x) + ((B + 1) * Dt + Tw -
		// Tb)
		// * Math.exp(-a * x))
		// / (B * Math.exp(-b * x) + 1)).floatValue();

		return new Double(equation).floatValue();

	}

	class MeasuredAxis {
		public static final int X_GAP = 1;
		public static final int Y_GAP = 1;

		int _realHeight;
		int _realWidth;

		public MeasuredAxis(int width, int height) {
			_realWidth = width;
			_realHeight = height;
		}

		public int getXAxisNegLimit() {
			return (_realWidth / 2) * (-1);
		}

		public int getXAxisLimit() {
			return (_realWidth / 2);
		}

		public int getYAxisLimit() {
			return (_realHeight / 2);
		}

		public float getXMeasured(int x) {
			return ((_realWidth / 2) + x);
		}

		public float getYMeasured(float y) {
			return ((_realHeight / 2) - y);
		}
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
		button_rf_on = (Button) findViewById(R.id.button_rf_on);

		antenna_black_label_down = (TextView) findViewById(R.id.antenna_black_label_down);
		water_label_down = (TextView) findViewById(R.id.water_label_down);
		deltat_label_down = (TextView) findViewById(R.id.deltat_label_down);
		time_label_down = (TextView) findViewById(R.id.time_label_down);
		disturbo_label = (TextView) findViewById(R.id.disturbo_label);
		suggerimenti = (TextView) findViewById(R.id.suggerimenti);

		zero = (LinearLayout) findViewById(R.id.zero);
		dieci = (LinearLayout) findViewById(R.id.dieci);
		venti = (LinearLayout) findViewById(R.id.venti);
		trenta = (LinearLayout) findViewById(R.id.trenta);
		quaranta = (LinearLayout) findViewById(R.id.quaranta);
		cinquanta = (LinearLayout) findViewById(R.id.cinquanta);
		sessanta = (LinearLayout) findViewById(R.id.sessanta);
		settanta = (LinearLayout) findViewById(R.id.settanta);
		ottanta = (LinearLayout) findViewById(R.id.ottanta);
		novanta = (LinearLayout) findViewById(R.id.novanta);

	}

	private void decrement() {

		if (funzionalita == button_water_left.getId()) {

			if (water_label_down.getText().equals("-00.0")) {
				water_label_down.setText("42");
			}

			if (Float.parseFloat(water_label_down.getText().toString()) > 35) {

				float tot = (Float.parseFloat(water_label_down.getText()
						.toString()) * 10 - 1) / 10;

				water_label_down.setText(String.valueOf(tot));

			}
		}

		if (funzionalita == button_deltat_left.getId()) {

			if (deltat_label_down.getText().equals("-00.0")) {
				deltat_label_down.setText("3");
			}

			if (Float.parseFloat(deltat_label_down.getText().toString()) > -1) {

				float tot = (Float.parseFloat(deltat_label_down.getText()
						.toString()) * 10 - 1) / 10;

				if (tot > 0) {
					deltat_label_down.setText("+" + tot);
				} else {
					deltat_label_down.setText(String.valueOf(tot));
				}

			}

		}

		if (funzionalita == button_antenna_left.getId()) {

			if (antenna_black_label_down.getText().equals("-00.0")) {
				antenna_black_label_down.setText("0");
			}

			if (Integer.parseInt(antenna_black_label_down.getText().toString()) > 0) {
				antenna_black_label_down
						.setText(String.valueOf(Integer
								.parseInt(antenna_black_label_down.getText()
										.toString()) - 1));

			}

		}

		if (funzionalita == button_time_left.getId()) {
			int time = Integer.parseInt(time_label_down
					.getText()
					.toString()
					.substring(0,
							time_label_down.getText().toString().length() - 3));

			if (time > 0) {

				time_label_down.setText("" + (time - 1) + ":00");

			}

		}

	}

	private void increment() {

		if (funzionalita == button_water_right.getId()) {
			if (water_label_down.getText().equals("-00.0")) {
				water_label_down.setText("35");
			}

			if (Float.parseFloat(water_label_down.getText().toString()) < 42) {

				float tot = (Float.parseFloat(water_label_down.getText()
						.toString()) * 10 + 1) / 10;

				water_label_down.setText(String.valueOf(tot));

			}
		}

		if (funzionalita == button_deltat_right.getId()) {

			if (deltat_label_down.getText().equals("-00.0")) {
				deltat_label_down.setText("-1");
			}

			if (Float.parseFloat(deltat_label_down.getText().toString()) < 3) {

				float tot = (Float.parseFloat(deltat_label_down.getText()
						.toString()) * 10 + 1) / 10;

				if (tot > 0) {
					deltat_label_down.setText("+" + tot);
				} else {
					deltat_label_down.setText(String.valueOf(tot));
				}

			}

		}

		if (funzionalita == button_antenna_right.getId()) {

			if (antenna_black_label_down.getText().equals("-00.0")) {
				antenna_black_label_down.setText("0");
			}

			if (Integer.parseInt(antenna_black_label_down.getText().toString()) < 99) {
				antenna_black_label_down
						.setText(String.valueOf(Integer
								.parseInt(antenna_black_label_down.getText()
										.toString()) + 1));

			}

		}

		if (funzionalita == button_time_right.getId()) {

			int time = Integer.parseInt(time_label_down
					.getText()
					.toString()
					.substring(0,
							time_label_down.getText().toString().length() - 3));

			if (time < 30) {

				time_label_down.setText("" + (time + 1) + ":00");

			}
		}
	}

	private int buttonId;

	private void auto_increment(final Button button, final TextView textView,
			final int max, final int step) {

		if (waitTimer != null) {
			waitTimer.cancel();
			inviaComandi(0, MSK_ALL_4);
			waitTimer = null;
		}

		buttonId = button.getId();

		long count;

		if (buttonId == button_time_right.getId()) {

			count = Math.round(max
					- (Double.valueOf(textView.getText().toString()
							.substring(0, textView.length() - 3))))
					* step * 3 * 1000;

		} else {
			count = Math.round(max
					- (Double.valueOf(textView.getText().toString())))
					* step * 3 * 1000;
		}

		waitTimer = new CountDownTimer(count, 500) {

			public void onTick(long millisUntilFinished) {

				button.setPressed(true);
				funzionalita = button.getId();

				Double misurato;

				if (buttonId == button_time_right.getId()) {
					misurato = Double.valueOf(textView.getText().toString()
							.substring(0, textView.length() - 3));
				} else {
					misurato = Double.valueOf(textView.getText().toString());
				}

				if (max != misurato) {

					increment();

				} else {
					button.setPressed(false);
					inviaParametri(button);
					this.cancel();
				}

			}

			public void onFinish() {
				button.setPressed(false);

				inviaParametri(button);

			}
		}.start();

	}

	private void auto_decrement(final Button button, final TextView textView,
			final int min, final int step) {

		if (waitTimer != null) {
			waitTimer.cancel();
			inviaComandi(0, MSK_ALL_4);
			waitTimer = null;
		}

		buttonId = button.getId();

		long count;

		if (buttonId == button_time_left.getId()) {

			count = Math.round((Double.valueOf(textView.getText().toString()
					.substring(0, textView.length() - 3)))
					- min)
					* step * 3 * 1000;

		} else {
			count = Math.round((Double.valueOf(textView.getText().toString()))
					- min)
					* step * 3 * 1000;
		}

		waitTimer = new CountDownTimer(count, 500) {

			public void onTick(long millisUntilFinished) {

				button.setPressed(true);
				funzionalita = button.getId();

				Double misurato;

				if (buttonId == button_time_left.getId()) {
					misurato = Double.valueOf(textView.getText().toString()
							.substring(0, textView.length() - 3));
				} else {
					misurato = Double.valueOf(textView.getText().toString());
				}

				if (min != misurato) {

					decrement();

				} else {
					button.setPressed(false);

					inviaParametri(button);

					this.cancel();
				}

			}

			public void onFinish() {

				button.setPressed(false);

				inviaParametri(button);
			}
		}.start();

	}

	protected void inviaParametri(Button button) {

		int WATER_LEFT = button_water_left.getId(), WATER_RIGHT = button_water_right
				.getId();
		int DELTAT_LEFT = button_water_left.getId(), DELTAT_RIGHT = button_water_right
				.getId();
		int ANTENNA_LEFT = button_antenna_left.getId(), ANTENNA_RIGHT = button_antenna_right
				.getId();
		int TIME_LEFT = button_time_left.getId(), TIME_RIGHT = button_time_right
				.getId();

		if (button.getId() == WATER_LEFT || button.getId() == WATER_RIGHT) {
			inviaComandi(0, MSK_WATER);
		}
		if (button.getId() == DELTAT_LEFT || button.getId() == DELTAT_RIGHT) {
			inviaComandi(0, MSK_DELTAT);
		}
		if (button.getId() == ANTENNA_LEFT || button.getId() == ANTENNA_RIGHT) {
			inviaComandi(0, MSK_POWER);
		}
		if (button.getId() == TIME_LEFT || button.getId() == TIME_RIGHT) {
			inviaComandi(0, MSK_TIME);
		}

	}
}
