package it.app.tcare;

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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Service extends Activity {

	private Button esc, ok, zero, uno, due, tre, quattro, cinque, sei, sette,
			otto, nove, password, del;
	private TextView revision, modello_firmware_value, versione_firmware_value,
			work_time_value;

	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_service);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();

		esc = (Button) findViewById(R.id.esc);
		esc.setText(getResources().getString(R.string.esc));
		esc.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				finish();
			}
		});

		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e("TCARE", "ERRORE STRANO QUI! " + e.getMessage());
		}
		revision = (TextView) findViewById(R.id.revision);
		revision.setText(pInfo.versionName);

		modello_firmware_value = (TextView) findViewById(R.id.modello_firmware_value);
		modello_firmware_value.setText(preferences.getString(
				"modello_firmware", "Base"));

		versione_firmware_value = (TextView) findViewById(R.id.versione_firmware_value);
		versione_firmware_value.setText(preferences.getString(
				"versione_firmware", "1.0"));

		work_time_value = (TextView) findViewById(R.id.work_time_value);
		long seconds = preferences.getInt("work_time", 0);
		work_time_value.setText(String.format("%d:%02d:%02d",
				(seconds / (60 * 60)) % 24, (seconds / 60) % 60, seconds % 60));

		del = (Button) findViewById(R.id.del);
		del.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				String text = password.getText().toString();
				password.setText(text.substring(0, text.length() - 1));
			}
		});

		ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				byte[] salt = new byte[16];
				KeySpec spec = new PBEKeySpec(password.getText().toString()
						.toCharArray(), salt, 65536, 128);
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

				if (new BigInteger(1, hash).toString(16).equals(
						preferences.getString("password", ""))) {
					Toast.makeText(getApplicationContext(), "OK",
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "FAILED",
							Toast.LENGTH_LONG).show();
				}
			}
		});

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

		zero.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("0");
			}
		});

		uno.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("1");
			}
		});

		due.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("2");
			}
		});

		tre.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("3");
			}
		});

		quattro.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("4");
			}
		});

		cinque.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("5");
			}
		});

		sei.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("6");
			}
		});

		sette.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("7");
			}
		});

		otto.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("8");
			}
		});

		nove.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				password.append("9");
			}
		});

	}
}
