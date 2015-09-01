package it.app.hypertherm.activity;

import it.app.hypertherm.PC_TO_CY;
import it.app.hypertherm.R;
import it.app.hypertherm.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
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
			button_temperature_positive, button_temperature_negative;
	private TextView antenna_black_label_down, water_label_down,
			deltat_label_down, time_label_down, disturbo_label, suggerimenti;

	private boolean mAutoIncrement = false;
	private boolean mAutoDecrement = false;

	private Handler repeatUpdateHandler = new Handler();

	public double mValue;

	private Utility utility;

	private int funzionalita;

	private SharedPreferences preferences;

	private SerialPortOpt serialPort;

	private InputStream mInputStream;
	private ReadThread mReadThread;

	private byte[] writeusbdata = new byte[256];
	private StringBuffer readSB = new StringBuffer();

	public static write_thread writeThread;

	private CountDownTimer waitTimerBolusUp = null;
	private CountDownTimer waitTimerBolusDown = null;

	private void inviaComandi(String comando) {

		PC_TO_CY pctocy = new PC_TO_CY();
		pctocy.setPSoCData();

		pctocy.PSoCData[2] = 0;
		pctocy.PSoCData[3] = 0;
		pctocy.PSoCData[4] = 64;
		pctocy.PSoCData[12] = 0;
		pctocy.PSoCData[13] = (byte) Integer.parseInt(comando);

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

			boolean READ_ENABLE = true;
			int exit = 0;

			while (READ_ENABLE) {

				// utility.appendLog("SONO NELLO SCRIVO");

				// inviaComandi("W");

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

			while (!isInterrupted()) {

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
							// int Msk = ((int) buf[4]) & 0xFF;
							// Msk |= (((int) buf[5]) & 0xFF) << 8;
							// Msk |= (((int) buf[6]) & 0xFF) << 16;
							// Msk |= (((int) buf[7]) & 0xFF) << 24;

							byte[] msk = new byte[4];

							msk[0] = buf[4];
							msk[1] = buf[5];
							msk[2] = buf[6];
							msk[3] = buf[7];

							String msk_binary = utility.toBinary(msk);

							int In_Output = ((int) buf[8]) & 0xFF;
							In_Output |= (((int) buf[9]) & 0xFF) << 8;
							In_Output |= (((int) buf[10]) & 0xFF) << 16;
							In_Output |= (((int) buf[11]) & 0xFF) << 24;
							int Cmd = ((int) buf[12]) & 0xFF;
							Cmd |= (((int) buf[13]) & 0xFF) << 8;
							int iTime = ((int) buf[14]) & 0xFF;
							iTime |= (((int) buf[15]) & 0xFF) << 8;
							int iD_temp = ((int) buf[16]) & 0xFF;
							iD_temp |= (((int) buf[17]) & 0xFF) << 8;
							int iH2o_temp = ((int) buf[18]) & 0xFF;
							iH2o_temp |= (((int) buf[19]) & 0xFF) << 8;
							int iColdHp_temp = ((int) buf[20]) & 0xFF;
							iColdHp_temp |= (((int) buf[21]) & 0xFF) << 8;
							int iPower = ((int) buf[22]) & 0xFF;
							iPower |= (((int) buf[23]) & 0xFF) << 8;
							int Gain_D_temp = ((int) buf[24]) & 0xFF;
							Gain_D_temp |= (((int) buf[25]) & 0xFF) << 8;
							int Offset_D_temp = ((int) buf[26]) & 0xFF;
							Offset_D_temp |= (((int) buf[27]) & 0xFF) << 8;
							int Gain_H2o_temp = ((int) buf[28]) & 0xFF;
							Gain_H2o_temp |= (((int) buf[29]) & 0xFF) << 8;
							int Offset_H2o_temp = ((int) buf[30]) & 0xFF;
							Offset_H2o_temp |= (((int) buf[31]) & 0xFF) << 8;
							int Gain_Cold_temp = ((int) buf[32]) & 0xFF;
							Gain_Cold_temp |= (((int) buf[33]) & 0xFF) << 8;
							int Offset_Cold_temp = ((int) buf[34]) & 0xFF;
							Offset_Cold_temp |= (((int) buf[35]) & 0xFF) << 8;
							int Gain_Boil_temp = ((int) buf[36]) & 0xFF;
							Gain_Boil_temp |= (((int) buf[37]) & 0xFF) << 8;
							int Offset_Boil_temp = ((int) buf[38]) & 0xFF;
							Offset_Boil_temp |= (((int) buf[39]) & 0xFF) << 8;
							int Req_power = ((int) buf[40]) & 0xFF;
							Req_power |= (((int) buf[41]) & 0xFF) << 8;
							int Dir_power = ((int) buf[42]) & 0xFF;
							Dir_power |= (((int) buf[43]) & 0xFF) << 8;
							int Ref_power = ((int) buf[44]) & 0xFF;
							Ref_power |= (((int) buf[45]) & 0xFF) << 8;
							int D_temp = ((int) buf[46]) & 0xFF;
							D_temp |= (((int) buf[47]) & 0xFF) << 8;
							int H2o_temp = ((int) buf[48]) & 0xFF;
							H2o_temp |= (((int) buf[49]) & 0xFF) << 8;
							int ColdHp_temp = ((int) buf[50]) & 0xFF;
							ColdHp_temp |= (((int) buf[51]) & 0xFF) << 8;
							int Boil_temp = ((int) buf[52]) & 0xFF;
							Boil_temp |= (((int) buf[53]) & 0xFF) << 8;
							int runningTime = ((int) buf[54]) & 0xFF;
							runningTime |= (((int) buf[55]) & 0xFF) << 8;
							int pwmRes = ((int) buf[56]) & 0xFF;
							int pwmPomp = ((int) buf[57]) & 0xFF;
							int pwmFan = ((int) buf[58]) & 0xFF;
							int[] Buf = new int[5];
							Buf[0] = ((int) buf[59]) & 0xFF;
							Buf[1] = ((int) buf[60]) & 0xFF;
							Buf[2] = ((int) buf[61]) & 0xFF;
							Buf[3] = ((int) buf[62]) & 0xFF;
							Buf[4] = ((int) buf[63]) & 0xFF;

							utility.appendLog("COMANDO_RICEVUTO:" + "CheckSum="
									+ CheckSum + " Ver=" + Ver + " TimStmp="
									+ TimStmp + " Msk=" + msk_binary
									+ " In_Output=" + In_Output + " Cmd=" + Cmd
									+ " iTime=" + iTime + " iD_temp=" + iD_temp
									+ " iH2o_temp=" + iH2o_temp
									+ " iColdHp_temp=" + iColdHp_temp
									+ " iPower=" + iPower + " Gain_D_temp="
									+ Gain_D_temp + " Gain_D_temp="
									+ Offset_D_temp + " Gain_H2o_temp="
									+ Gain_H2o_temp + " Offset_H2o_temp="
									+ Offset_H2o_temp + " Gain_Cold_temp="
									+ Gain_Cold_temp + " Offset_Cold_temp="
									+ Offset_Cold_temp + " Gain_Boil_temp="
									+ Gain_Boil_temp + " Offset_Boil_temp="
									+ Offset_Boil_temp + " Req_power="
									+ Req_power + " Dir_power=" + Dir_power
									+ " Ref_power=" + Ref_power + " D_temp="
									+ D_temp + " H2o_temp=" + H2o_temp
									+ " ColdHp_temp=" + ColdHp_temp
									+ " Boil_temp=" + Boil_temp
									+ " runningTime=" + runningTime
									+ " pwmRes=" + pwmRes + " pwmPomp="
									+ pwmPomp + " pwmFan=" + pwmFan
									+ " Buf[0]=" + Buf[0] + " Buf[1]=" + Buf[1]
									+ " Buf[2]=" + Buf[2] + " Buf[3]=" + Buf[3]
									+ " Buf[4]=" + Buf[4]);

							int cmd = msk_binary.indexOf("1");

							switch (cmd) {
							case 1: // COMANDO
								utility.esegui(Cmd);
								break;

							case 2: // TEMPO
								utility.SetTime(iTime / 60 + ":00");
								break;

							case 3: // DELTAT
								utility.setDeltaT(due_cifre(iD_temp).replace(
										",", "."));
								break;

							case 4: // WATER
								utility.setWaterTemperature(due_cifre(iH2o_temp)
										.replace(",", "."));
								break;

							case 6: // ANTENNA
								utility.setAntenna(due_cifre(iPower).replace(
										",", "."));
								break;
							}

							utility.esegui(Cmd);

							utility.SetTime(iTime / 60 + ":00");

							utility.setDeltaT(due_cifre(iD_temp).replace(",",
									"."));

							utility.setWaterTemperature(due_cifre(iH2o_temp)
									.replace(",", "."));

							utility.setAntenna(due_cifre(iPower).replace(",",
									"."));
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
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	private String due_cifre(int number) {
		double numer_job = (double) number / 100;
		DecimalFormat df = new DecimalFormat("0.0#");
		return df.format(numer_job);
	}

	private String una_cifra(int number) {
		number /= 10;
		DecimalFormat df = new DecimalFormat("0.0#");
		return df.format(number);
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
		seek_bar.setMax(100);

		android.view.ViewGroup.LayoutParams param = seek_bar.getLayoutParams();

		DisplayMetrics display = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(display);
		int width = display.widthPixels;

		param.width = width * 20 / 100;

		def_variable_components();

		def_bottun_click();

		def_value_defaults();

		suggerimenti.setText(utility.get_suggerimento_trattamento());

		// writeThread = new write_thread();
		// if (!writeThread.isAlive() && writeThread.getState() !=
		// State.RUNNABLE) {
		// writeThread.setName("Thread_Scrittura");
		// writeThread.start();
		//
		// }
		//
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// }
		//
		// inviaComandi("@");
		// inviaComandi("^");
		// inviaComandi("a");
		// inviaComandi("?");

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

	}

	private void def_bottun_click() {

		button_power.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					seek_bar.setProgress(50);

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

						if (!button_power.isPressed()) {

						}

					}
				});

		button_temperature_positive
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						if (!button_power.isPressed()) {

						}

					}
				});
		button_play.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				utility.appendLog("Inviato comando: PLAY");
				inviaComandi("1");
			}
		});

		button_pause.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				utility.appendLog("Inviato comando: PAUSE");
				inviaComandi("2");
			}
		});

		button_stop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				utility.appendLog("Inviato comando: STOP");
				inviaComandi("3");
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
						inviaComandi("6");

						return true;

					} else {

						if (!button_bolus_down.isPressed()) {

							button_bolus_down.setPressed(true);
							utility.appendLog("Setto pressed true");

							utility.appendLog("Inviato comando: BOLUS-DOWN");
							inviaComandi("5");

							waitTimerBolusDown = new CountDownTimer(30000,
									30000) {

								public void onTick(long millisUntilFinished) {

								}

								public void onFinish() {
									button_bolus_down.setPressed(false);
									utility.appendLog("Setto pressed false");
								}
							}.start();

							return false;

						} else {

							if (waitTimerBolusDown != null) {
								waitTimerBolusDown.cancel();
								waitTimerBolusDown = null;
							}

							utility.appendLog("Inviato comando: BOLUS-STOP");
							inviaComandi("6");

							utility.appendLog("Setto pressed false");
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

						if (waitTimerBolusDown != null) {
							waitTimerBolusDown.cancel();
							waitTimerBolusDown = null;
						}

						utility.appendLog("Inviato comando: BOLUS-STOP");
						inviaComandi("6");

						return true;

					} else {

						if (!button_bolus_up.isPressed()) {

							button_bolus_up.setPressed(true);
							utility.appendLog("Setto pressed true");

							utility.appendLog("Inviato comando: BOLUS-UP");
							inviaComandi("4");

							waitTimerBolusUp = new CountDownTimer(30000, 30000) {

								public void onTick(long millisUntilFinished) {

								}

								public void onFinish() {
									button_bolus_up.setPressed(false);
									utility.appendLog("Setto pressed false");
								}
							}.start();

							return false;

						} else {

							if (waitTimerBolusUp != null) {
								waitTimerBolusUp.cancel();
								waitTimerBolusUp = null;
							}

							utility.appendLog("Inviato comando: BOLUS-STOP");
							inviaComandi("6");

							utility.appendLog("Setto pressed false");
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

				if (Integer.parseInt(antenna_black_label_down.getText()
						.toString()) > 0) {
					antenna_black_label_down.setText(String.valueOf(Integer
							.parseInt(antenna_black_label_down.getText()
									.toString()) - 1));
				}

				disturbo_label.setText(utility.getMenuItemDefault());

				utility.appendLog("Inviato comando: ANTENNA-LEFT");
				invia_trattamenti();

			}
		});

		button_antenna_right.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (antenna_black_label_down.getText().equals("-00.0")) {
					antenna_black_label_down.setText("0");
				}

				if (Integer.parseInt(antenna_black_label_down.getText()
						.toString()) < 99) {
					antenna_black_label_down.setText(String.valueOf(Integer
							.parseInt(antenna_black_label_down.getText()
									.toString()) + 1));
				}

				disturbo_label.setText(utility.getMenuItemDefault());

				utility.appendLog("Inviato comando: ANTENNA-RIGHT");
				invia_trattamenti();

			}
		});

		button_water_left.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (water_label_down.getText().equals("-00.0")) {
					water_label_down.setText("42");
				}

				if (Float.parseFloat(water_label_down.getText().toString()) > 35) {

					float tot = (Float.parseFloat(water_label_down.getText()
							.toString()) * 10 - 1) / 10;

					water_label_down.setText(String.valueOf(tot));

				}

				disturbo_label.setText(utility.getMenuItemDefault());

				utility.appendLog("Inviato comando: WATER-LEFT");
				invia_trattamenti();

			}
		});

		button_water_right.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (water_label_down.getText().equals("-00.0")) {
					water_label_down.setText("35");
				}

				if (Float.parseFloat(water_label_down.getText().toString()) < 42) {

					float tot = (Float.parseFloat(water_label_down.getText()
							.toString()) * 10 + 1) / 10;

					water_label_down.setText(String.valueOf(tot));
				}

				disturbo_label.setText(utility.getMenuItemDefault());

				utility.appendLog("Inviato comando: WATER-RIGHT");
				invia_trattamenti();

			}
		});

		button_deltat_left.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

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

				disturbo_label.setText(utility.getMenuItemDefault());

				utility.appendLog("Inviato comando: DELTAt-LEFT");
				invia_trattamenti();

			}
		});

		button_deltat_right.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

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

				disturbo_label.setText(utility.getMenuItemDefault());

				utility.appendLog("Inviato comando: DELTAT-RIGHT");
				invia_trattamenti();

			}
		});

		button_time_left.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

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

				disturbo_label.setText(utility.getMenuItemDefault());

				utility.appendLog("Inviato comando: TIMER-LEFT");
				invia_trattamenti();

			}
		});

		button_time_right.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

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

				disturbo_label.setText(utility.getMenuItemDefault());

				utility.appendLog("Inviato comando: TIMER-RIGHT");
				invia_trattamenti();

			}
		});

		// AUTOMATICI

		button_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(WorkActivity.this,
						MainActivity.class);
				startActivity(intent);

			}
		});

		button_water_left
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {
						funzionalita = button_water_left.getId();
						mValue = 1;
						mAutoDecrement = true;
						repeatUpdateHandler.post(new RptUpdater());
						return false;
					}
				});

		button_water_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)
						&& mAutoDecrement) {
					funzionalita = button_water_left.getId();
					mValue = 1;
					mAutoDecrement = false;
				}
				return false;
			}
		});

		button_water_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {
						funzionalita = button_water_right.getId();
						mValue = 1;
						mAutoIncrement = true;
						repeatUpdateHandler.post(new RptUpdater());
						return false;
					}
				});

		button_water_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)
						&& mAutoIncrement) {
					funzionalita = button_water_right.getId();
					mValue = 1;
					mAutoIncrement = false;
				}
				return false;
			}
		});

		button_deltat_left
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {
						funzionalita = button_deltat_left.getId();
						mValue = 1;
						mAutoDecrement = true;
						repeatUpdateHandler.post(new RptUpdater());
						return false;
					}
				});

		button_deltat_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)
						&& mAutoDecrement) {
					funzionalita = button_deltat_left.getId();
					mValue = 1;
					mAutoDecrement = false;
				}
				return false;
			}
		});

		button_deltat_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {
						funzionalita = button_deltat_right.getId();
						mValue = 1;
						mAutoIncrement = true;
						repeatUpdateHandler.post(new RptUpdater());
						return false;
					}
				});

		button_deltat_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)
						&& mAutoIncrement) {
					funzionalita = button_deltat_right.getId();
					mValue = 1;
					mAutoIncrement = false;
				}
				return false;
			}
		});

		button_antenna_left
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {
						funzionalita = button_antenna_left.getId();
						mValue = 1;
						mAutoDecrement = true;
						repeatUpdateHandler.post(new RptUpdater());
						return false;
					}
				});

		button_antenna_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)
						&& mAutoDecrement) {
					funzionalita = button_antenna_left.getId();
					mValue = 1;
					mAutoDecrement = false;
				}
				return false;
			}
		});

		button_antenna_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {
						funzionalita = button_antenna_right.getId();
						mValue = 1;
						mAutoIncrement = true;
						repeatUpdateHandler.post(new RptUpdater());
						return false;
					}
				});

		button_antenna_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)
						&& mAutoIncrement) {
					funzionalita = button_antenna_right.getId();
					mValue = 1;
					mAutoIncrement = false;
				}
				return false;
			}
		});

		button_time_left.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				funzionalita = button_time_left.getId();
				mValue = 1;
				mAutoDecrement = true;
				repeatUpdateHandler.post(new RptUpdater());
				return false;
			}
		});

		button_time_left.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)
						&& mAutoDecrement) {
					funzionalita = button_time_left.getId();
					mValue = 1;
					mAutoDecrement = false;
				}
				return false;
			}
		});

		button_time_right
				.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View arg0) {
						funzionalita = button_time_right.getId();
						mValue = 1;
						mAutoIncrement = true;
						repeatUpdateHandler.post(new RptUpdater());
						return false;
					}
				});

		button_time_right.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if ((event.getAction() == MotionEvent.ACTION_UP || event
						.getAction() == MotionEvent.ACTION_CANCEL)
						&& mAutoIncrement) {
					funzionalita = button_time_right.getId();
					mValue = 1;
					mAutoIncrement = false;
				}
				return false;
			}
		});

	}

	protected void invia_trattamenti() {

		PC_TO_CY pctocy = new PC_TO_CY();

		utility.appendLog("Setto trattamento: TIMER:"
				+ Integer
						.parseInt(time_label_down
								.getText()
								.toString()
								.substring(
										0,
										time_label_down.getText().toString()
												.length() - 3)) + " - ANTENNA:"
				+ antenna_black_label_down.getText().toString() + " WATER:"
				+ water_label_down.getText().toString() + " - DELTAT:"
				+ deltat_label_down.getText().toString());

		pctocy.setTreatParms(
				String.valueOf(Integer
						.parseInt(time_label_down
								.getText()
								.toString()
								.substring(
										0,
										time_label_down.getText().toString()
												.length() - 3)) * 60),
				antenna_black_label_down.getText().toString(), water_label_down
						.getText().toString(), deltat_label_down.getText()
						.toString());
		pctocy.setPSoCData();

		SendData(PC_TO_CY.PACKET_SIZE, pctocy.PSoCData);

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

		antenna_black_label_down = (TextView) findViewById(R.id.antenna_black_label_down);
		water_label_down = (TextView) findViewById(R.id.water_label_down);
		deltat_label_down = (TextView) findViewById(R.id.deltat_label_down);
		time_label_down = (TextView) findViewById(R.id.time_label_down);
		disturbo_label = (TextView) findViewById(R.id.disturbo_label);
		suggerimenti = (TextView) findViewById(R.id.suggerimenti);

	}

	private void decrement() {

		if (funzionalita == button_water_left.getId()) {

			if (water_label_down.getText().equals("-00.0")) {
				water_label_down.setText("42");
			}

			if (Float.parseFloat(water_label_down.getText().toString()) > 35) {

				double tot = (Float.parseFloat(water_label_down.getText()
						.toString()) * 10 - mValue) / 10;

				water_label_down.setText(String.valueOf(tot));

				// mValue = mValue + 0.5;

			}
		}

		if (funzionalita == button_deltat_left.getId()) {

			if (deltat_label_down.getText().equals("-00.0")) {
				deltat_label_down.setText("3");
			}

			if (Float.parseFloat(deltat_label_down.getText().toString()) > -1) {

				double tot = (Float.parseFloat(deltat_label_down.getText()
						.toString()) * 10 - mValue) / 10;

				if (tot > 0) {
					deltat_label_down.setText("+" + tot);
				} else {
					deltat_label_down.setText(String.valueOf(tot));
				}

				// mValue++;

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

				// mValue++;

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

				double tot = (Float.parseFloat(water_label_down.getText()
						.toString()) * 10 + mValue) / 10;

				water_label_down.setText(String.valueOf(tot));

				// mValue++;
			}
		}

		if (funzionalita == button_deltat_right.getId()) {

			if (deltat_label_down.getText().equals("-00.0")) {
				deltat_label_down.setText("-1");
			}

			if (Float.parseFloat(deltat_label_down.getText().toString()) < 3) {

				double tot = (Float.parseFloat(deltat_label_down.getText()
						.toString()) * 10 + mValue) / 10;

				if (tot > 0) {
					deltat_label_down.setText("+" + tot);
				} else {
					deltat_label_down.setText(String.valueOf(tot));
				}

				// mValue++;

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

				// mValue++;
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

	class RptUpdater implements Runnable {
		public void run() {
			if (mAutoIncrement) {
				increment();
				repeatUpdateHandler.postDelayed(new RptUpdater(), 300);
			} else if (mAutoDecrement) {
				decrement();
				repeatUpdateHandler.postDelayed(new RptUpdater(), 300);
			}
		}

	}

}
