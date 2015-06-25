package it.app.tcare_serial;

import it.app.tcare.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;

public class Utility {

	private Activity activity;
	private SeekBar seek_bar_percentage;
	private TextView time, label_start, label_pause, label_stop,
			label_continuos, label_energy, jaule_label;
	private Button play, stop, pause, cap, res, body, face, energy, menu,
			continuos, frequency, joule;
	private TableRow pannello_energia;

	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	public void poweroff() {

		try {

			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());

			Log.d("TCARE", "SPENGO IL CELL");
			os.writeBytes("reboot -p" + "\n");
			os.writeBytes("exit\n");
			os.flush();

			Log.d("TCARE", "FATTO! :)");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Utility(Activity activity) {

		this.activity = activity;

		seek_bar_percentage = (SeekBar) activity
				.findViewById(R.id.seek_bar_percentage);

		jaule_label = (TextView) activity.findViewById(R.id.jaule_label);
		label_energy = (TextView) activity.findViewById(R.id.label_energy);
		label_start = (TextView) activity.findViewById(R.id.label_start);
		label_stop = (TextView) activity.findViewById(R.id.label_stop);
		label_pause = (TextView) activity.findViewById(R.id.label_pause);
		time = (TextView) activity.findViewById(R.id.time);
		label_continuos = (TextView) activity
				.findViewById(R.id.label_continuos);

		play = (Button) activity.findViewById(R.id.button_play);
		stop = (Button) activity.findViewById(R.id.button_stop);
		pause = (Button) activity.findViewById(R.id.button_pause);

		cap = (Button) activity.findViewById(R.id.cap);
		res = (Button) activity.findViewById(R.id.res);
		body = (Button) activity.findViewById(R.id.body);
		face = (Button) activity.findViewById(R.id.face);

		continuos = (Button) activity.findViewById(R.id.button_continuos);

		frequency = (Button) activity.findViewById(R.id.frequency);
		energy = (Button) activity.findViewById(R.id.energy);

		menu = (Button) activity.findViewById(R.id.menu);

		pannello_energia = (TableRow) activity
				.findViewById(R.id.pannello_energia);

		joule = (Button) activity.findViewById(R.id.joule);

		preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		editor = preferences.edit();

	}

	public void esegui(final String command) {

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				try {

					if (command != null) {
						String[] comandi = command.split(" ");

						if (comandi != null && comandi.length == 3) {

							if (comandi[2].equals("?")) {

								editor.putString("versione_firmware",
										comandi[0] + " " + comandi[1]).commit();
							}
						}

						if (comandi != null && comandi.length == 2) {

							if (comandi[1].equals("W")) {

								if (comandi[0].length() == 7) {

									joule.setText(String.valueOf(Integer
											.parseInt(
													comandi[0].substring(0, 2),
													16) * 1000));

									jaule_label.setText(String.valueOf(Integer
											.parseInt(
													comandi[0].substring(0, 2),
													16) * 1000));

									jaule_label.setText(R.string.setValue);

									editor.putInt(
											"energy",
											Integer.parseInt(
													comandi[0].substring(0, 2),
													16) * 1000).commit();

									seek_bar_percentage.setProgress(Integer
											.parseInt(comandi[0].toString()
													.substring(2, 4), 16));

									String fs = new BigInteger(comandi[0]
											.toString().substring(4, 6), 16)
											.toString(2);

									if (fs.length() == 7)
										fs = "0" + fs;

									if (fs != null && fs.length() == 8) {

										if (fs.substring(0, 2).equals("00")) {
											frequency
													.setTag(R.drawable.button_145);
											frequency
													.setBackgroundResource(R.drawable.button_145);
										}
										if (fs.substring(0, 2).equals("01")) {
											frequency
													.setTag(R.drawable.button_457);
											frequency
													.setBackgroundResource(R.drawable.button_457);
										}
										if (fs.substring(0, 2).equals("10")) {
											frequency
													.setTag(R.drawable.button_571);
											frequency
													.setBackgroundResource(R.drawable.button_571);
										}
										if (fs.substring(0, 2).equals("11")) {
											frequency
													.setTag(R.drawable.button_714);
											frequency
													.setBackgroundResource(R.drawable.button_714);
										}

										if (fs.substring(2, 4).equals("00")) {
											res.setPressed(true);
											cap.setPressed(false);
											body.setPressed(false);
											face.setPressed(false);
										}
										if (fs.substring(2, 4).equals("01")) {
											cap.setPressed(true);
											res.setPressed(false);
											body.setPressed(false);
											face.setPressed(false);
										}
										if (fs.substring(2, 4).equals("10")) {
											face.setPressed(true);
											cap.setPressed(false);
											res.setPressed(false);
											body.setPressed(false);
										}
										if (fs.substring(2, 4).equals("11")) {
											body.setPressed(true);
											cap.setPressed(false);
											res.setPressed(false);
											face.setPressed(false);
										}

										if (fs.substring(4, 6).equals("00")) {

											editor.putBoolean("isPlaying",
													false).commit();

											stop.setPressed(true);
											play.setPressed(false);
											pause.setPressed(false);
											label_stop.setTextColor(Color
													.parseColor("#78d0d2"));
											label_start
													.setTextColor(Color.WHITE);
											label_pause
													.setTextColor(Color.WHITE);

											menu.setEnabled(true);

										}

										if (fs.substring(4, 6).equals("01")) {

											editor.putBoolean("isPlaying", true)
													.commit();

											play.setPressed(true);
											pause.setPressed(false);
											stop.setPressed(false);
											label_stop
													.setTextColor(Color.WHITE);
											label_pause
													.setTextColor(Color.WHITE);

											menu.setEnabled(false);

										}

										if (fs.substring(4, 6).equals("10")) {

											editor.putBoolean("isPlaying",
													false).commit();

											pause.setPressed(true);
											play.setPressed(false);
											stop.setPressed(false);
											label_pause.setTextColor(Color
													.parseColor("#78d0d2"));
											label_start
													.setTextColor(Color.WHITE);
											label_stop
													.setTextColor(Color.WHITE);

											menu.setEnabled(false);
										}

										if (fs.substring(7, 8).equals("0")) {
											pannello_energia
													.setVisibility(View.GONE);
											editor.putBoolean("isTime", true)
													.commit();
											editor.putBoolean("isEnergy", false)
													.commit();
										}

										if (fs.substring(7, 8).equals("1")) {
											pannello_energia
													.setVisibility(View.VISIBLE);
											editor.putBoolean("isEnergy", true)
													.commit();
											editor.putBoolean("isTime", false)
													.commit();
										}

									}

									label_continuos.setText(" "
											+ Integer.parseInt(
													comandi[0].toString()
															.substring(6, 7),
													16) + " Hz");
								}
							}
						}

						if (comandi != null && comandi.length == 10) {

							if (comandi[9].equals("a")) {

								String minuti, secondi;

								editor.putInt(
										"timer_progress",
										Integer.parseInt(
												comandi[0].substring(0, 2), 16) * 2)
										.commit();

								if (Integer.parseInt(
										comandi[0].substring(0, 2), 16) < 10)
									minuti = "0"
											+ Integer.parseInt(
													comandi[0].substring(0, 2),
													16);
								else
									minuti = ""
											+ Integer.parseInt(
													comandi[0].substring(0, 2),
													16);

								if (Integer.parseInt(
										comandi[0].substring(2, 4), 16) < 10)
									secondi = "0"
											+ Integer.parseInt(
													comandi[0].substring(2, 4),
													16);
								else
									secondi = ""
											+ Integer.parseInt(
													comandi[0].substring(2, 4),
													16);

								time.setText(minuti + "'" + secondi + "''");

								editor.putString("timer",
										minuti + "'" + secondi + "''").commit();

								joule.setText(String.valueOf(Integer.parseInt(
										comandi[1], 16) * 1000));

								editor.putInt("energy",
										Integer.parseInt(comandi[1], 16) * 1000)
										.commit();

								if (comandi[3].equals("00")) {
									frequency.setTag(R.drawable.button_145);
									frequency
											.setBackgroundResource(R.drawable.button_145);
								}
								if (comandi[3].equals("01")) {
									frequency.setTag(R.drawable.button_457);
									frequency
											.setBackgroundResource(R.drawable.button_457);
								}
								if (comandi[3].equals("02")) {
									frequency.setTag(R.drawable.button_571);
									frequency
											.setBackgroundResource(R.drawable.button_571);
								}
								if (comandi[3].equals("03")) {
									frequency.setTag(R.drawable.button_714);
									frequency
											.setBackgroundResource(R.drawable.button_714);
								}

								if (comandi[4].equals("00")) {
									res.setPressed(true);
									cap.setPressed(false);
									body.setPressed(false);
									face.setPressed(false);
								}
								if (comandi[4].equals("01")) {
									cap.setPressed(true);
									res.setPressed(false);
									body.setPressed(false);
									face.setPressed(false);
								}
								if (comandi[4].equals("02")) {
									face.setPressed(true);
									cap.setPressed(false);
									res.setPressed(false);
									body.setPressed(false);
								}
								if (comandi[4].equals("03")) {
									body.setPressed(true);
									cap.setPressed(false);
									res.setPressed(false);
									face.setPressed(false);
								}

								if (comandi[5].equals("01")
										|| comandi[5].equals("02")
										|| comandi[5].equals("03")
										|| comandi[5].equals("04")
										|| comandi[5].equals("05")) {
									continuos
											.setBackgroundResource(R.drawable.pulsed_normal);
									label_continuos.setText(" "
											+ Integer.parseInt(comandi[5])
											+ " Hz");
									label_continuos.setVisibility(View.VISIBLE);
									editor.putBoolean("isPulsed", true)
											.commit();
									editor.putBoolean("isContinuos", false)
											.commit();
									editor.putInt("hz",
											Integer.parseInt(comandi[5]))
											.commit();
								}

								if (comandi[5].equals("00")) {
									continuos
											.setBackgroundResource(R.drawable.continuos_normal);
									label_continuos
											.setVisibility(View.INVISIBLE);
									editor.putBoolean("isContinuos", true)
											.commit();
									editor.putBoolean("isPulsed", false)
											.commit();
									editor.putInt("hz", 0).commit();
								}

								if (comandi[6].equals("00")) {
									pannello_energia.setVisibility(View.GONE);
									editor.putBoolean("isTime", true).commit();
									editor.putBoolean("isEnergy", false)
											.commit();
								}

								if (comandi[6].equals("01")) {
									pannello_energia
											.setVisibility(View.VISIBLE);
									editor.putBoolean("isEnergy", true)
											.commit();
									editor.putBoolean("isTime", false).commit();
								}

								if (comandi[7].equals("00")) {

									editor.putBoolean("isPlaying", false)
											.commit();

									stop.setPressed(true);
									stop.setTextColor(Color
											.parseColor("#015c5f"));
									play.setPressed(false);
									pause.setPressed(false);
									label_stop.setTextColor(Color
											.parseColor("#78d0d2"));
									label_start.setTextColor(Color.WHITE);
									label_pause.setTextColor(Color.WHITE);

									menu.setEnabled(true);
								}

								if (comandi[7].equals("01")) {

									editor.putBoolean("isPlaying", true)
											.commit();

									play.setPressed(true);
									pause.setPressed(false);
									stop.setPressed(false);
									label_start.setTextColor(Color
											.parseColor("#78d0d2"));
									label_stop.setTextColor(Color.WHITE);
									label_pause.setTextColor(Color.WHITE);

									menu.setEnabled(false);
								}

								if (comandi[7].equals("02")) {

									editor.putBoolean("isPlaying", false)
											.commit();

									pause.setPressed(true);
									pause.setTextColor(Color
											.parseColor("#015c5f"));
									play.setPressed(false);
									stop.setPressed(false);
									label_pause.setTextColor(Color
											.parseColor("#78d0d2"));
									label_start.setTextColor(Color.WHITE);
									label_stop.setTextColor(Color.WHITE);

									menu.setEnabled(false);

								}

							}

						}

						if (comandi != null && comandi.length == 2) {

							if (comandi[1].equals("J")) {

								energy.setText(String.valueOf(Integer.parseInt(
										comandi[0], 16)));

								label_energy.setText(String.valueOf(Integer
										.parseInt(comandi[0], 16)));

								label_energy.setText(R.string.energy);

							}

							if (comandi[1].equals("(")
									|| comandi[1].equals(")")) {

								String minuti, secondi;

								if (Integer.parseInt(
										comandi[0].substring(0, 2), 16) < 10)
									minuti = "0"
											+ Integer.parseInt(
													comandi[0].substring(0, 2),
													16);
								else
									minuti = ""
											+ Integer.parseInt(
													comandi[0].substring(0, 2),
													16);

								if (Integer.parseInt(
										comandi[0].substring(2, 4), 16) < 10)
									secondi = "0"
											+ Integer.parseInt(
													comandi[0].substring(2, 4),
													16);
								else
									secondi = ""
											+ Integer.parseInt(
													comandi[0].substring(2, 4),
													16);

								time.setText(minuti + "'" + secondi + "''");

							}

							if (comandi[1].equals("<")
									|| comandi[1].equals(">")) {
								seek_bar_percentage.setProgress(Integer
										.parseInt(comandi[0], 16));
							}

							if (comandi[1].equals("0")
									|| comandi[1].equals("1")
									|| comandi[1].equals("2")
									|| comandi[1].equals("3")
									|| comandi[1].equals("4")
									|| comandi[1].equals("5")) {
								if (comandi[0].equals("01")
										|| comandi[0].equals("02")
										|| comandi[0].equals("03")
										|| comandi[0].equals("04")
										|| comandi[0].equals("05")) {
									continuos.setPressed(true);
									continuos
											.setBackgroundResource(R.drawable.pulsed_normal);
									label_continuos.setText(" "
											+ Integer.parseInt(comandi[0])
											+ " Hz");
									label_continuos.setVisibility(View.VISIBLE);

									editor.putBoolean("isPulsed", true)
											.commit();
									editor.putBoolean("isContinuos", false)
											.commit();

								}

								if (comandi[0].equals("00")) {
									continuos
											.setBackgroundResource(R.drawable.continuos_normal);
									label_continuos
											.setVisibility(View.INVISIBLE);
									continuos.setPressed(false);

									editor.putBoolean("isPulsed", false)
											.commit();
									editor.putBoolean("isContinuos", true)
											.commit();
								}

								editor.putInt("hz",
										Integer.parseInt(comandi[1])).commit();

							}

							if (comandi[1].equals("q")
									|| comandi[1].equals("c")
									|| comandi[1].equals("s")
									|| comandi[1].equals("m")) {

								if (comandi[0].equals("00")) {
									frequency.setTag(R.drawable.button_145);
									frequency
											.setBackgroundResource(R.drawable.button_145);
								}

								if (comandi[0].equals("01")) {
									frequency.setTag(R.drawable.button_457);
									frequency
											.setBackgroundResource(R.drawable.button_457);
								}

								if (comandi[0].equals("02")) {
									frequency.setTag(R.drawable.button_571);
									frequency
											.setBackgroundResource(R.drawable.button_571);
								}

								if (comandi[0].equals("03")) {
									frequency.setTag(R.drawable.button_714);
									frequency
											.setBackgroundResource(R.drawable.button_714);
								}
							}

							if (comandi[1].equals("F")
									|| comandi[1].equals("B")
									|| comandi[1].equals("R")
									|| comandi[1].equals("C")) {

								if (comandi[0].equals("00")) {
									res.setPressed(true);
									cap.setPressed(false);
									body.setPressed(false);
									face.setPressed(false);
								}

								if (comandi[0].equals("01")) {
									cap.setPressed(true);
									res.setPressed(false);
									body.setPressed(false);
									face.setPressed(false);
								}

								if (comandi[0].equals("02")) {
									face.setPressed(true);
									cap.setPressed(false);
									res.setPressed(false);
									body.setPressed(false);
								}

								if (comandi[0].equals("03")) {
									body.setPressed(true);
									cap.setPressed(false);
									res.setPressed(false);
									face.setPressed(false);
								}
							}

							if (!(preferences.getBoolean("isMenu", false))) {

								if (comandi[1].equals("T")
										&& !(preferences.getBoolean("isMenu",
												false))) {

									if (comandi[0].equals("00")) {

										editor.putBoolean("isPlaying", false)
												.commit();

										stop.setPressed(true);
										play.setPressed(false);
										pause.setPressed(false);
										label_stop.setTextColor(Color
												.parseColor("#78d0d2"));
										label_start.setTextColor(Color.WHITE);
										label_pause.setTextColor(Color.WHITE);

										menu.setEnabled(true);

									}
								}
							}

							if (!(preferences.getBoolean("isMenu", false))) {
								if (comandi[1].equals("S")) {

									if (comandi[0].equals("01")) {

										editor.putBoolean("isPlaying", true)
												.commit();

										play.setPressed(true);
										pause.setPressed(false);
										stop.setPressed(false);
										label_start.setTextColor(Color
												.parseColor("#78d0d2"));
										label_stop.setTextColor(Color.WHITE);
										label_pause.setTextColor(Color.WHITE);

										menu.setEnabled(false);

									}
								}
							}

							if (!(preferences.getBoolean("isMenu", false))) {
								if (comandi[1].equals("P")
										&& !(preferences.getBoolean("isMenu",
												false))) {
									if (comandi[0].equals("02")) {

										editor.putBoolean("isPlaying", false)
												.commit();

										pause.setPressed(true);
										play.setPressed(false);
										stop.setPressed(false);
										label_pause.setTextColor(Color
												.parseColor("#78d0d2"));
										label_start.setTextColor(Color.WHITE);
										label_stop.setTextColor(Color.WHITE);

										menu.setEnabled(false);

									}
								}
							}
						}
					}
				} catch (Exception e) {
					Log.d("TCARE", "Comando errato: " + command);
				}
			}
		});
	}

	public boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}

	public static byte[] stringToBytesASCII(String str) {

		byte[] b = new byte[str.length()];
		for (int i = 0; i < b.length; i++) {

			b[i] = (byte) str.charAt(i);
		}
		return b;
	}

}
