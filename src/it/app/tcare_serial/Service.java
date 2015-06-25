package it.app.tcare_serial;

import it.app.tcare.R;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class Service extends Activity {

	private Button esc, ok, zero, uno, due, tre, quattro, cinque, sei, sette,
			otto, nove, password, del, serial_number_value, reset, english,
			russian, chinese, exit;
	private TextView revision, versione_firmware_value, work_time_value;
	private RadioButton radio_button_smart, radio_button_physio;
	private LinearLayout english_rectangular, russian_rectangular,
			chinese_rectangular;

	private String work_time;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_service);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		revision = (TextView) findViewById(R.id.revision);
		work_time_value = (TextView) findViewById(R.id.work_time_value);
		versione_firmware_value = (TextView) findViewById(R.id.versione_firmware_value);

		exit = (Button) findViewById(R.id.exit);
		esc = (Button) findViewById(R.id.esc);
		serial_number_value = (Button) findViewById(R.id.serial_number_value);
		del = (Button) findViewById(R.id.del);
		ok = (Button) findViewById(R.id.ok);
		zero = (Button) findViewById(R.id.zero);
		uno = (Button) findViewById(R.id.uno);
		due = (Button) findViewById(R.id.due);
		tre = (Button) findViewById(R.id.tre);
		quattro = (Button) findViewById(R.id.quattro);
		cinque = (Button) findViewById(R.id.cinque);
		sei = (Button) findViewById(R.id.sei);
		sette = (Button) findViewById(R.id.sette);
		otto = (Button) findViewById(R.id.otto);
		nove = (Button) findViewById(R.id.nove);
		password = (Button) findViewById(R.id.password);
		reset = (Button) findViewById(R.id.reset);
		english = (Button) findViewById(R.id.english);
		russian = (Button) findViewById(R.id.russian);
		chinese = (Button) findViewById(R.id.chinese);

		english_rectangular = (LinearLayout) findViewById(R.id.english_rectangular);
		russian_rectangular = (LinearLayout) findViewById(R.id.russian_rectangular);
		chinese_rectangular = (LinearLayout) findViewById(R.id.chinese_rectangular);

		radio_button_smart = (RadioButton) findViewById(R.id.radio_button_smart);
		radio_button_physio = (RadioButton) findViewById(R.id.radio_button_physio);

		if (preferences.getBoolean("isSmart", false)) {
			radio_button_smart.setChecked(true);
			radio_button_physio.setChecked(false);
		}

		if (preferences.getBoolean("isPhysio", false)) {
			radio_button_smart.setChecked(false);
			radio_button_physio.setChecked(true);
		}

		serial_number_value.setText(preferences.getString("serial_number",
				"SN DEFAULT"));

		esc.setText(getResources().getString(R.string.esc));
		esc.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				preferences.edit().putBoolean("exit", false).commit();
				finish();
			}
		});

		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e("TCARE", "ERRORE STRANO QUI! " + e.getMessage());
		}
		revision.setText(pInfo.versionName);

		versione_firmware_value.setText(preferences.getString(
				"versione_firmware", "1.0"));

		long seconds = preferences.getInt("work_time", 0);
		work_time_value.setText(String.format("%d:%02d:%02d",
				(seconds / (60 * 60)) % 24, (seconds / 60) % 60, seconds % 60));
		work_time = work_time_value.getText().toString();

		if (preferences.getString("language", "en").equals("en")) {
			english_rectangular.setTag(R.drawable.cell_shape);
			russian_rectangular.setTag(0);
			chinese_rectangular.setTag(0);
			english_rectangular.setBackgroundResource(R.drawable.cell_shape);
		} else if (preferences.getString("language", "en").equals("ru")) {
			russian_rectangular.setTag(R.drawable.cell_shape);
			english_rectangular.setTag(0);
			chinese_rectangular.setTag(0);

			russian_rectangular.setBackgroundResource(R.drawable.cell_shape);
		} else if (preferences.getString("language", "zh").equals("ru")) {
			chinese_rectangular.setTag(R.drawable.cell_shape);
			russian_rectangular.setTag(0);
			english_rectangular.setTag(0);

			chinese_rectangular.setBackgroundResource(R.drawable.cell_shape);
		}

		del.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				if (serial_number_value.isPressed()
						&& serial_number_value.length() > 0) {
					String text = serial_number_value.getText().toString();
					serial_number_value.setText(text.substring(0,
							text.length() - 1));
				}

				if (password.isPressed() && password.length() > 0) {
					String text = password.getText().toString();
					password.setText(text.substring(0, text.length() - 1));
				}
			}
		});

		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (password.length() > 0) {
					if (cifra(password.getText().toString()).equals(
							preferences.getString("password", ""))) {

						preferences
								.edit()
								.putString(
										"serial_number",
										serial_number_value.getText()
												.toString()).commit();
						preferences
								.edit()
								.putBoolean("isSmart",
										radio_button_smart.isChecked())
								.commit();
						preferences
								.edit()
								.putBoolean("isPhysio",
										radio_button_physio.isChecked())
								.commit();

						Message handler_save_serial_number_db = Main_Activity.handler_save_serial_number_db
								.obtainMessage();
						Main_Activity.handler_save_serial_number_db
								.sendMessage(handler_save_serial_number_db);

						if (reset.isPressed()) {
							preferences.edit().putInt("work_time", 0).commit();
							Message handler_reset_work_time_db = Main_Activity.handler_reset_work_time_db
									.obtainMessage();
							Main_Activity.handler_reset_work_time_db
									.sendMessage(handler_reset_work_time_db);
						}

						if ((Integer) english_rectangular.getTag() == R.drawable.cell_shape) {
							preferences.edit().putString("language", "en")
									.commit();
						}

						if ((Integer) russian_rectangular.getTag() == R.drawable.cell_shape) {
							preferences.edit().putString("language", "ru")
									.commit();
						}

						if ((Integer) chinese_rectangular.getTag() == R.drawable.cell_shape) {
							preferences.edit().putString("language", "zh")
									.commit();
						}

						Message handler_save_settings_db = Main_Activity.handler_save_settings_db
								.obtainMessage();
						Main_Activity.handler_save_settings_db
								.sendMessage(handler_save_settings_db);

						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.ok_password),
								Toast.LENGTH_LONG).show();

						preferences.edit().putBoolean("exit", false).commit();

						finish();
					} else {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.wrong_password),
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.insert_password),
							Toast.LENGTH_LONG).show();
					password.setPressed(true);
					serial_number_value.setPressed(false);
				}
			}

		});

		zero.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("0");

				if (password.isPressed())
					password.append("0");
			}
		});

		uno.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("1");

				if (password.isPressed())
					password.append("1");
			}
		});

		due.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("2");

				if (password.isPressed())
					password.append("2");
			}
		});

		tre.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("3");

				if (password.isPressed())
					password.append("3");
			}
		});

		quattro.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("4");

				if (password.isPressed())
					password.append("4");
			}
		});

		cinque.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("5");

				if (password.isPressed())
					password.append("5");
			}
		});

		sei.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("6");

				if (password.isPressed())
					password.append("6");
			}
		});

		sette.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("7");

				if (password.isPressed())
					password.append("7");
			}
		});

		otto.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("8");

				if (password.isPressed())
					password.append("8");
			}
		});

		nove.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (serial_number_value.isPressed())
					serial_number_value.append("9");

				if (password.isPressed())
					password.append("9");
			}
		});

		serial_number_value.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (serial_number_value.isPressed()) {
						serial_number_value.setPressed(false);
						password.setPressed(true);
					} else {
						serial_number_value.setPressed(true);
						password.setPressed(false);
					}

					return true;
				}

				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
			}
		});

		password.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (!password.isPressed()) {
						serial_number_value.setPressed(false);
						password.setPressed(true);
					} else {
						serial_number_value.setPressed(true);
						password.setPressed(false);
					}

					return true;
				}

				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
			}
		});

		reset.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// show interest in events resulting from ACTION_DOWN
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (reset.isPressed()) {
						reset.setPressed(false);
						work_time_value.setText(work_time);
					} else {
						reset.setPressed(true);
						work_time_value.setText("RESET");
					}

					return true;
				}

				if (event.getAction() != MotionEvent.ACTION_UP) {

					return false;
				}

				return true;
			}
		});

		english.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				english_rectangular
						.setBackgroundResource(R.drawable.cell_shape);
				russian_rectangular.setBackgroundResource(0);
				chinese_rectangular.setBackgroundResource(0);

				english_rectangular.setTag(R.drawable.cell_shape);
				russian_rectangular.setTag(0);
				chinese_rectangular.setTag(0);

			}
		});

		russian.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				english_rectangular.setBackgroundResource(0);
				russian_rectangular
						.setBackgroundResource(R.drawable.cell_shape);
				chinese_rectangular.setBackgroundResource(0);

				english_rectangular.setTag(0);
				russian_rectangular.setTag(R.drawable.cell_shape);
				chinese_rectangular.setTag(0);

			}
		});

		chinese.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				english_rectangular.setBackgroundResource(0);
				russian_rectangular.setBackgroundResource(0);
				chinese_rectangular
						.setBackgroundResource(R.drawable.cell_shape);

				english_rectangular.setTag(0);
				russian_rectangular.setTag(0);
				chinese_rectangular.setTag(R.drawable.cell_shape);

			}
		});

		exit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				preferences.edit().putBoolean("exit", true).commit();

				finish();

			}
		});

	}

	private String cifra(String string) {
		byte[] salt = new byte[16];
		KeySpec spec = new PBEKeySpec(string.toCharArray(), salt, 65536, 128);
		SecretKeyFactory f;
		byte[] hash = null;

		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hash = f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return new BigInteger(1, hash).toString(16);

	}
}
