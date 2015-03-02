package it.app.tcare;

import android.app.Activity;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Utility {

	private Activity activity;
	private SeekBar seek_bar_percentage;
	private TextView monitor;

	public Utility(Activity activity) {
		this.activity = activity;

		seek_bar_percentage = (SeekBar) activity
				.findViewById(R.id.seek_bar_percentage);

		monitor = (TextView) activity.findViewById(R.id.monitor);

	}

	public void scriviMonitor(String command) {
		monitor.setText(command);
	}

	public void esegui(String command) {
		if (command != null) {
			String[] comandi = command.split(" ");
			if (comandi != null && comandi.length == 2) {
				if (comandi[1].equals("<") || comandi[1].equals(">")) {
					seek_bar_percentage.setProgress(Integer.parseInt(
							comandi[0], 16));
				}
			}
		}
	}

	public boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
		}
		return false;
	}

	public void setC_FREQ440(String returnCode) {
		Button frequency = (Button) activity.findViewById(R.id.frequency);
		if (returnCode == "00")
			frequency.setText("440 KHz");
		else
			frequency.setText("ERROR");
	}

	public void setC_FREQ500(String returnCode) {
		Button frequency = (Button) activity.findViewById(R.id.frequency);
		if (returnCode == "01")
			frequency.setText("500 KHz");
		else
			frequency.setText("ERROR");
	}

	public void setC_FREQ720(String returnCode) {
		Button frequency = (Button) activity.findViewById(R.id.frequency);
		if (returnCode == "02")
			frequency.setText("720 KHz");
		else
			frequency.setText("ERROR");
	}

	public void setC_FREQ1000(String returnCode) {
		Button frequency = (Button) activity.findViewById(R.id.frequency);
		if (returnCode == "03")
			frequency.setText("1000 KHz");
		else
			frequency.setText("ERROR");
	}

	public void setC_START(String returnCode) {

		if (returnCode == "01") {

		}
	}

	public void setC_STOP(String returnCode) {

		if (returnCode == "00") {

		}
	}

	public void setC_PAUSE(String returnCode) {

		if (returnCode == "02") {

		}
	}
}
