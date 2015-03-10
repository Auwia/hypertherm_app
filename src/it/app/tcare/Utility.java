package it.app.tcare;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Utility {

	private Activity activity;
	private SeekBar seek_bar_percentage;
	private TextView time, label_start, label_pause, label_stop, percentage;
	private Button play, stop, pause, cap, res, body, face, reset, energy,
			set_value, menu, continuos, frequency;

	public Utility(Activity activity) {
		this.activity = activity;

		seek_bar_percentage = (SeekBar) activity
				.findViewById(R.id.seek_bar_percentage);

		label_start = (TextView) activity.findViewById(R.id.label_start);
		label_stop = (TextView) activity.findViewById(R.id.label_stop);
		label_pause = (TextView) activity.findViewById(R.id.label_pause);
		time = (TextView) activity.findViewById(R.id.time);

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

	}

	public void esegui(final String command) {

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (command != null) {
					String[] comandi = command.split(" ");
					if (comandi != null && comandi.length == 2) {

						if (comandi[1].equals("J")) {
							energy.setText(String.valueOf(Integer.parseInt(
									comandi[0], 16)));
						}

						if (comandi[1].equals("(")) {

							String minuti, secondi;

							if (Integer.parseInt(comandi[0].substring(0, 2), 16) < 10)
								minuti = "0"
										+ Integer.parseInt(
												comandi[0].substring(0, 2), 16);
							else
								minuti = ""
										+ Integer.parseInt(
												comandi[0].substring(0, 2), 16);

							if (Integer.parseInt(comandi[0].substring(2, 4), 16) < 10)
								secondi = "0"
										+ Integer.parseInt(
												comandi[0].substring(2, 4), 16);
							else
								secondi = ""
										+ Integer.parseInt(
												comandi[0].substring(2, 4), 16);

							time.setText(minuti + "'" + secondi + "''");

						}

						if (comandi[1].equals("<") || comandi[1].equals(">")) {
							seek_bar_percentage.setProgress(Integer.parseInt(
									comandi[0], 16));
						}

						if (comandi[1].equals("0")) {
							if (comandi[0].equals("00")) {
								continuos.setPressed(true);
							} else {
								continuos.setPressed(false);
								Log.e("TCARE",
										"Not permitted, operation failed");
							}
						}

						if (comandi[1].equals("0") || comandi[1].equals("1")
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
							}
							if (comandi[0].equals("00")) {
								continuos.setPressed(false);
							}

						}

						if (comandi[1].equals("q") || comandi[1].equals("c")
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

						if (comandi[1].equals("F") || comandi[1].equals("B")
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

						if (comandi[1].equals("S") || comandi[1].equals("T")
								|| comandi[1].equals("P")) {
							if (comandi[0].equals("00")) {
								stop.setPressed(true);
								stop.setTextColor(Color.parseColor("#015c5f"));
								play.setPressed(false);
								pause.setPressed(false);
								label_stop.setTextColor(Color
										.parseColor("#78d0d2"));
								label_start.setTextColor(Color.WHITE);
								label_pause.setTextColor(Color.WHITE);
							}
							if (comandi[0].equals("01")) {
								play.setPressed(true);
								play.setTextColor(Color.parseColor("#015c5f"));
								pause.setPressed(false);
								stop.setPressed(false);
								label_start.setTextColor(Color
										.parseColor("#78d0d2"));
								label_stop.setTextColor(Color.WHITE);
								label_pause.setTextColor(Color.WHITE);
							}
							if (comandi[0].equals("02")) {
								pause.setPressed(true);
								pause.setTextColor(Color.parseColor("#015c5f"));
								play.setPressed(false);
								stop.setPressed(false);
								label_pause.setTextColor(Color
										.parseColor("#78d0d2"));
								label_start.setTextColor(Color.WHITE);
								label_stop.setTextColor(Color.WHITE);
							}
						}
					}
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
}
