package it.app.tcare;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class Service extends Activity {

	private Button esc;
	private TextView revision, modello_firmware_value, versione_firmware_value;

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
	}

}
