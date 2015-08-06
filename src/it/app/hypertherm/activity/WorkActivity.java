package it.app.hypertherm.activity;

import it.app.hypertherm.R;
import it.app.hypertherm.Utility;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class WorkActivity extends Activity {

	private SeekBar seek_bar;
	private Button button_antenna_left, button_antenna_right,
			button_water_left, button_water_right, button_deltat_left,
			button_deltat_right, button_time_left, button_time_right,
			button_home;
	private TextView antenna_black_label_down, water_label_down,
			deltat_label_down, time_label_down, disturbo_label;

	private boolean mAutoIncrement = false;
	private boolean mAutoDecrement = false;

	private Handler repeatUpdateHandler = new Handler();

	public double mValue;

	private Utility utility;

	private int funzionalita;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_work);

		utility = new Utility(this);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

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

		antenna_black_label_down = (TextView) findViewById(R.id.antenna_black_label_down);
		water_label_down = (TextView) findViewById(R.id.water_label_down);
		deltat_label_down = (TextView) findViewById(R.id.deltat_label_down);
		time_label_down = (TextView) findViewById(R.id.time_label_down);
		disturbo_label = (TextView) findViewById(R.id.disturbo_label);

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
