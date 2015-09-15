package it.app.hypertherm.util;

import it.app.hypertherm.Menu_app;
import it.app.hypertherm.R;
import it.app.hypertherm.db.HyperthermDB;
import it.app.hypertherm.db.HyperthermDataSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class Utility {

	private Activity activity;

	private static String TAG = "HYPERTHERM";

	private SharedPreferences preferences;

	private TextView antenna_black_label_up, water_label_up, deltat_label_up,
			time_label_up;

	private Button button_play, button_stop, button_pause, button_bolus_down,
			button_bolus_up, button_home, button_rf_on;

	private int comando;

	// VARIABILI DATA BASE
	private static final String DATABASE_NAME = "Hypertherm.db";
	private static SQLiteDatabase database;
	private HyperthermDataSource datasource;
	private Cursor cur;
	private ContentValues row = new ContentValues();

	// DECIMAL TO HEX
	private static final int sizeOfIntInHalfBytes = 8;
	private static final int numberOfBitsInAHalfByte = 4;
	private static final int halfByte = 0x0F;
	private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public Utility(Activity activity) {

		this.activity = activity;

		datasource = new HyperthermDataSource(activity.getApplicationContext());
		datasource.open();

		database = activity.openOrCreateDatabase(DATABASE_NAME,
				SQLiteDatabase.CREATE_IF_NECESSARY, null);

		preferences = PreferenceManager.getDefaultSharedPreferences(activity);

		antenna_black_label_up = (TextView) activity
				.findViewById(R.id.antenna_black_label_up);
		water_label_up = (TextView) activity.findViewById(R.id.water_label_up);
		deltat_label_up = (TextView) activity
				.findViewById(R.id.deltat_label_up);
		time_label_up = (TextView) activity.findViewById(R.id.time_label_up);

		button_play = (Button) activity.findViewById(R.id.button_play);
		button_pause = (Button) activity.findViewById(R.id.button_pause);
		button_stop = (Button) activity.findViewById(R.id.button_stop);
		button_bolus_down = (Button) activity
				.findViewById(R.id.button_bolus_down);
		button_bolus_up = (Button) activity.findViewById(R.id.button_bolus_up);
		button_home = (Button) activity.findViewById(R.id.button_home);
		button_rf_on = (Button) activity.findViewById(R.id.button_rf_on);

	}

	public Utility() {
	}

	final protected char[] hexArray = "0123456789ABCDEF".toCharArray();

	public String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public int calcola_check_sum(byte[] buf) {

		int check_sum = 0;

		for (int i = 0; i < 128; i += 2) {

			if (i > 2) {

				check_sum += Integer.parseInt(
						bytesToHex(buf).substring(i, i + 2), 16);
			}

		}

		return check_sum;
	}

	public void esegui(int cmd) {

		comando = cmd;

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				switch (comando) {

				case 256: // START
					button_play.setPressed(true);
					button_pause.setPressed(false);
					button_stop.setPressed(false);
					button_home.setEnabled(false);
					button_rf_on.setPressed(true);
					break;

				case 512: // PAUSE
					button_play.setPressed(false);
					button_pause.setPressed(true);
					button_stop.setPressed(false);
					button_rf_on.setPressed(false);
					break;

				case 768: // STOP
					button_play.setPressed(false);
					button_pause.setPressed(false);
					button_stop.setPressed(true);
					button_home.setEnabled(true);
					button_rf_on.setPressed(false);
					break;

				case 1024: // BOOL-UP
					button_bolus_up.setPressed(true);
					button_bolus_down.setPressed(false);
					break;

				case 1536: // BOOL-UP-STOP
					button_bolus_up.setPressed(false);
					button_bolus_down.setPressed(false);
					break;

				case 1280: // BOOL-DOWN
					button_bolus_up.setPressed(false);
					button_bolus_down.setPressed(true);
					break;

				case 2816: // RESET
					button_rf_on.setPressed(false);
					break;

				}

			}
		});
	}

	public int getPmaxRF(float deltat, float twater) {

		return (int) (4.3 * (5.4 * deltat + twater - 37));

	}

	public String[] getStrutturaItems() {

		cur = database.query(true, "STAGE_STRUTTURA",
				new String[] { "STRUTTURA" }, null, null, null, null, null,
				null);

		cur.moveToFirst();

		String[] struttura = new String[cur.getCount()];
		int i = 0;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			struttura[i] = cur.getString(0);
			i += 1;
			cur.moveToNext();
		}
		cur.close();

		return struttura;
	}

	public void setWaterTemperature(final String valore) {

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				appendLog("Imposto il valore di acqua ricevuto dalla scheda..."
						+ valore);
				water_label_up.setText(valore);
				appendLog("Imposto il valore di acqua ricevuto dalla scheda...OK");

			}
		});
	}

	public float getWaterTemperature(String disturbo) {

		float result = 0;

		if (disturbo.equals("DEFAULT")) {

			cur = database.query(HyperthermDB.TABLE_STAGE_DEFAULT,
					new String[] { "TACQUA" }, null, null, null, null, null);

		} else {

			cur = database.query(HyperthermDB.TABLE_DISTURBI,
					new String[] { "TACQUA" }, "MENU_ITEM=?",
					new String[] { disturbo }, null, null, null);

		}

		cur.moveToFirst();

		result = cur.getFloat(0);

		cur.close();

		return result;

	}

	public float getWaterTemperature(String struttura, String profondita) {

		float result = 0;

		cur = database.query("STAGE_STRUTTURA", new String[] { "TACQUA" },
				"STRUTTURA=? AND PROFONDITA=?", new String[] { struttura,
						profondita }, null, null, null);

		cur.moveToFirst();

		result = cur.getFloat(0);

		cur.close();

		return result;

	}

	public void setDeltaT(final String valore) {

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				appendLog("Imposto il valore di DeltaT ricevuto dalla scheda..."
						+ valore);
				deltat_label_up.setText(valore);
				appendLog("Imposto il valore di DeltaT ricevuto dalla scheda...OK");

			}
		});
	}

	public float getDeltaT(String disturbo) {

		float result = 0;

		if (disturbo.equals("DEFAULT")) {

			cur = database.query(HyperthermDB.TABLE_STAGE_DEFAULT,
					new String[] { "DTEMPERATURA" }, null, null, null, null,
					null);

		} else {

			cur = database.query(HyperthermDB.TABLE_DISTURBI,
					new String[] { "DTEMPERATURA" }, "MENU_ITEM=?",
					new String[] { disturbo }, null, null, null);

		}

		cur.moveToFirst();

		result = cur.getFloat(0);

		cur.close();

		return result;

	}

	public float getDeltaT(String struttura, String profondita) {

		float result = 0;

		cur = database.query("STAGE_STRUTTURA",
				new String[] { "DTEMPERATURA" },
				"STRUTTURA=? AND PROFONDITA=?", new String[] { struttura,
						profondita }, null, null, null);

		cur.moveToFirst();

		result = cur.getFloat(0);

		cur.close();

		return result;

	}

	public void setAntenna(final String valore) {

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				appendLog("Imposto il valore di antenna ricevuto dalla scheda..."
						+ valore);
				antenna_black_label_up.setText(valore);
				appendLog("Imposto il valore di antenna ricevuto dalla scheda...OK");

			}
		});
	}

	public int getAntenna(String disturbo) {

		int result = 0;

		if (disturbo.equals("DEFAULT")) {

			cur = database.query(HyperthermDB.TABLE_STAGE_DEFAULT,
					new String[] { "PMAXRF" }, null, null, null, null, null);

		} else {

			cur = database.query(HyperthermDB.TABLE_DISTURBI,
					new String[] { "PMAXRF" }, "MENU_ITEM=?",
					new String[] { disturbo }, null, null, null);

		}

		cur.moveToFirst();

		result = cur.getInt(0);

		cur.close();

		return result;

	}

	public String getProfonditaLabel(String struttura, String profondita) {

		String result = "";

		if (profondita.equals("4")) {

			result = "Dinamico";

		} else {
			cur = database.query("STAGE_STRUTTURA",
					new String[] { "PROFONDITA_LABEL" },
					"STRUTTURA=? AND PROFONDITA=?", new String[] { struttura,
							profondita }, null, null, null);

			cur.moveToFirst();

			result = cur.getString(0);

			cur.close();

		}

		return result;

	}

	public int getAntenna(String struttura, String profondita) {

		int result = 0;

		cur = database.query("STAGE_STRUTTURA", new String[] { "PMAXRF" },
				"STRUTTURA=? AND PROFONDITA=?", new String[] { struttura,
						profondita }, null, null, null);

		cur.moveToFirst();

		result = cur.getInt(0);

		cur.close();

		return result;

	}

	public void SetTime(final String valore) {

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				appendLog("Imposto il valore di tempo ricevuto dalla scheda..."
						+ valore);
				time_label_up.setText(valore);
				appendLog("Imposto il valore di tempo ricevuto dalla scheda...OK");

			}
		});
	}

	public String convertSecondsToMmSs(long seconds) {
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		return String.format("%02d:%02d", m, s);
	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public int getTime(String disturbo) {

		int result = 0;

		if (disturbo.equals("DEFAULT")) {

			cur = database.query(HyperthermDB.TABLE_STAGE_DEFAULT,
					new String[] { "TEMPO" }, null, null, null, null, null);

		} else {

			cur = database.query(HyperthermDB.TABLE_DISTURBI,
					new String[] { "TEMPO" }, "MENU_ITEM=?",
					new String[] { disturbo }, null, null, null);

		}

		cur.moveToFirst();

		result = cur.getInt(0);

		cur.close();

		return result;

	}

	public int getTime(String struttura, String profondita) {

		int result = 0;

		cur = database.query("STAGE_STRUTTURA", new String[] { "TEMPO" },
				"STRUTTURA=? AND PROFONDITA=?", new String[] { struttura,
						profondita }, null, null, null);

		cur.moveToFirst();

		result = cur.getInt(0);

		cur.close();

		return result;

	}

	public String getMenuItemDefault() {

		String result = "";

		cur = database.query(HyperthermDB.TABLE_STAGE_DEFAULT,
				new String[] { HyperthermDB.COLUMN_MENU_ITEM }, null, null,
				null, null, null);

		cur.moveToFirst();

		result = cur.getString(0);

		cur.close();

		return result;

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

	public String get_menu_item_patologia() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_ITEM },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "menu_item_patologia" }, null, null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_menu_item_struttura_profondita() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_ITEM },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "menu_item_struttura_profondita" }, null, null,
				null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_menu_item_manual_selection() {

		cur = database
				.query(HyperthermDB.TABLE_STAGE_STRING,
						new String[] { HyperthermDB.COLUMN_MENU_ITEM },
						HyperthermDB.COLUMN_MENU_ITEM + "=?",
						new String[] { "menu_item_manual_selection" }, null,
						null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_suggerimento_trattamento() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_VALUE },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "suggerimento_trattamento" }, null, null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_menu_item_demo_training() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_ITEM },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "menu_item_demo_training" }, null, null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_menu_item_user_manual() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_ITEM },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "menu_item_user_manual" }, null, null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_label_struttura() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_VALUE },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "label_struttura" }, null, null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_title_tessuto() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_VALUE },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "title_tessuto" }, null, null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_title_patologia() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_VALUE },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "title_patologia" }, null, null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_title_struttura_profondita() {

		cur = database
				.query(HyperthermDB.TABLE_STAGE_STRING,
						new String[] { HyperthermDB.COLUMN_MENU_VALUE },
						HyperthermDB.COLUMN_MENU_ITEM + "=?",
						new String[] { "title_struttura_profondita" }, null,
						null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public String get_label_profondita() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_VALUE },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "label_profondita" }, null, null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
	}

	public int get_time_out_splash() {

		cur = database.query("SETTINGS", new String[] { "TIMEOUT_SPLASH" },
				null, null, null, null, null);

		cur.moveToFirst();

		int timeout = 2000;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			timeout = cur.getInt(0);
			cur.moveToNext();
		}
		cur.close();

		return timeout;
	}

	public ArrayList<Menu_app> get_menu_items(String table_name) {

		cur = database.query(table_name, new String[] { "MENU_ITEM",
				"MENU_CLICCABILE" }, "MENU_ITEM != ?", new String[] { "NULL" },
				null, null, null);

		if (table_name == "PATOLOGIE") {

			cur = database
					.query("trattamenti a inner join disturbi b on (a.COLUMN_ID = b.ID_TRATTAMENTO) inner join  patologie c on (b.ID_PATOLOGIA=c.id)",
							new String[] { "b.MENU_ITEM", "c.MENU_ITEM" },
							"a.MENU_ITEM = ?", new String[] { preferences
									.getString("TRATTAMENTO", "") },
							"b.MENU_ITEM,c.MENU_ITEM", null, "c.MENU_ITEM desc");
		}

		cur.moveToFirst();

		ArrayList<Menu_app> menu_list = new ArrayList<Menu_app>();

		while (cur.getCount() > 0 && !cur.isAfterLast()) {

			if (table_name == "PATOLOGIE") {
				boolean trovato = false;

				for (int i = 0; i < menu_list.size(); i++) {

					if (cur.getString(1).equals(menu_list.get(i).getItem())) {
						trovato = true;
					}
				}

				if (!trovato) {
					if (!cur.getString(1).toString().equals("NULL")) {
						menu_list.add(new Menu_app(cur.getString(1), false));
					}
				}
			}

			menu_list.add(new Menu_app(cur.getString(0), true));
			cur.moveToNext();
		}
		cur.close();

		return menu_list;
	}

	public String getLanguage() {

		cur = database.query("SETTINGS", new String[] { "LANGUAGE" }, null,
				null, null, null, null);

		cur.moveToFirst();

		String language = "Ita";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			language = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return language;
	}

	public void appendLog(String text) {
		File logFile = new File(Environment.getExternalStorageDirectory(),
				"Hypertherm/log/log.txt");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {

			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));

			long yourmilliseconds = System.currentTimeMillis();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
					Locale.ITALY);

			buf.append(sdf.format(new Date(yourmilliseconds)) + ": " + text);
			buf.newLine();
			buf.close();

			Log.d(TAG, sdf.format(new Date(yourmilliseconds)) + ": " + text);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void cancellaStage(String table_name) {

		appendLog("delete Stage Area (" + table_name + ")...");

		database.beginTransaction();
		database.delete(table_name, null, null);
		database.setTransactionSuccessful();
		database.endTransaction();

		appendLog("delete Stage Area (" + table_name + ")...OK");
	}

	public void spostaFile(File file) {
		InputStream inStream = null;
		OutputStream outStream = null;

		try {

			File afile = file;
			File bfile = new File(Environment.getExternalStorageDirectory()
					+ "/Hypertherm/old/" + afile.getName());

			inStream = new FileInputStream(afile);
			outStream = new FileOutputStream(bfile);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();

			// delete the original file
			afile.delete();

			System.out.println("File is copied successful!");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String toBinary(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
		for (int i = 0; i < Byte.SIZE * bytes.length; i++)
			sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0'
					: '1');
		return sb.toString();
	}

	public double getDoubleOperation(String operazione) {

		cur = database.query("SETTINGS", new String[] { operazione.substring(0,
				operazione.length() - 10) }, null, null, null, null, null);

		cur.moveToFirst();

		String risultato = "-1";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			risultato = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return Double.parseDouble(risultato);

	}
}
