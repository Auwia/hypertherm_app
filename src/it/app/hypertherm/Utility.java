package it.app.hypertherm;

import it.app.hypertherm.db.HyperthermDB;
import it.app.hypertherm.db.HyperthermDataSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class Utility {

	private static String TAG = "HYPERTHERM";

	private SharedPreferences preferences;

	// VARIABILI DATA BASE
	private static final String DATABASE_NAME = "Hypertherm.db";
	private static SQLiteDatabase database;
	private HyperthermDataSource datasource;
	private Cursor cur;
	private ContentValues row = new ContentValues();

	public Utility(Activity activity) {

		datasource = new HyperthermDataSource(activity.getApplicationContext());
		datasource.open();

		database = activity.openOrCreateDatabase(DATABASE_NAME,
				SQLiteDatabase.CREATE_IF_NECESSARY, null);

		preferences = PreferenceManager.getDefaultSharedPreferences(activity);

	}

	public Utility() {
	}

	public double getPmaxRF(double deltat, double twater) {

		return Math.round(4.3 * (5.4 * deltat + twater - 37));

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

	public float getWaterTemperature(String disturbo) {

		float result = 0;

		if (disturbo.equals("DEFAULT")) {

			try {

				File root = Environment.getExternalStorageDirectory();
				FileInputStream fstream = new FileInputStream(root
						+ "/Hypertherm/conf/ParaDefault" + getLanguage()
						+ ".txt");

				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String strLine = br.readLine().replace('\n', ' ');

				appendLog(strLine);

				result = Float.parseFloat(strLine.split("\\|")[3]);

				in.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {

			cur = database.query("DISTURBI", new String[] { "TACQUA" },
					"MENU_ITEM=?", new String[] { disturbo }, null, null, null);

			cur.moveToFirst();

			result = cur.getFloat(0);

			cur.close();
		}

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

	public float getDeltaT(String disturbo) {

		float result = 0;

		if (disturbo.equals("DEFAULT")) {

			try {

				File root = Environment.getExternalStorageDirectory();
				FileInputStream fstream = new FileInputStream(root
						+ "/Hypertherm/conf/ParaDefault" + getLanguage()
						+ ".txt");

				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String strLine = br.readLine().replace('\n', ' ');

				appendLog(strLine);

				result = Float.parseFloat(strLine.split("\\|")[4]);

				in.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {

			cur = database.query("DISTURBI", new String[] { "DTEMPERATURA" },
					"MENU_ITEM=?", new String[] { disturbo }, null, null, null);

			cur.moveToFirst();

			result = cur.getFloat(0);

			cur.close();
		}

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

	public int getAntenna(String disturbo) {

		int result = 0;

		if (disturbo.equals("DEFAULT")) {

			try {

				File root = Environment.getExternalStorageDirectory();
				FileInputStream fstream = new FileInputStream(root
						+ "/Hypertherm/conf/ParaDefault" + getLanguage()
						+ ".txt");

				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String strLine = br.readLine().replace('\n', ' ');

				appendLog(strLine);

				result = Integer.parseInt(strLine.split("\\|")[2]);

				in.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {

			cur = database.query("DISTURBI", new String[] { "PMAXRF" },
					"MENU_ITEM=?", new String[] { disturbo }, null, null, null);

			cur.moveToFirst();

			result = cur.getInt(0);

			cur.close();
		}

		return result;

	}

	public String getProfonditaLabel(String struttura, String profondita) {

		String result = "";

		cur = database.query("STAGE_STRUTTURA",
				new String[] { "PROFONDITA_LABEL" },
				"STRUTTURA=? AND PROFONDITA=?", new String[] { struttura,
						profondita }, null, null, null);

		cur.moveToFirst();

		result = cur.getString(0);

		cur.close();

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

	public int getTime(String disturbo) {

		int result = 0;

		if (disturbo.equals("DEFAULT")) {

			try {

				File root = Environment.getExternalStorageDirectory();
				FileInputStream fstream = new FileInputStream(root
						+ "/Hypertherm/conf/ParaDefault" + getLanguage()
						+ ".txt");

				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String strLine = br.readLine().replace('\n', ' ');

				appendLog(strLine);

				result = Integer.parseInt(strLine.split("\\|")[1]);

				in.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {

			cur = database.query("DISTURBI", new String[] { "TEMPO" },
					"MENU_ITEM=?", new String[] { disturbo }, null, null, null);

			cur.moveToFirst();

			result = cur.getInt(0);

			cur.close();

		}

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

		cur = database.query("STAGE_DEFAULT", new String[] { "MENU_ITEM" },
				null, null, null, null, null);

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
}
