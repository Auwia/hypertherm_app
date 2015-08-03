package it.app.hypertherm;

import it.app.hypertherm.db.HyperthermDataSource;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	private Activity activity;

	private ContentValues row = new ContentValues();

	private static String TAG = "HYPERTHERM";

	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	// VARIABILI DATA BASE
	private static final String DATABASE_NAME = "Hypertherm.db";
	private static SQLiteDatabase database;
	private HyperthermDataSource datasource;
	private Cursor cur;

	public Utility(Activity activity) {

		this.activity = activity;

		datasource = new HyperthermDataSource(activity.getApplicationContext());
		datasource.open();

		database = activity.openOrCreateDatabase(DATABASE_NAME,
				SQLiteDatabase.CREATE_IF_NECESSARY, null);

		preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		editor = preferences.edit();

	}

	public Utility() {
	}

	public void caricaDisturbi() {

		appendLog("upload Disturbi...");

		database.beginTransaction();

		database.delete("DISTURBI", null, null);

		database.execSQL("insert into disturbi select null, a.disturbi, c.COLUMN_ID id_trattamento, b.ID id_patologia from stage a inner join patologie b on (a.PATOLOGIE=b.menu_item) inner join trattamenti c on (c.MENU_ITEM=a.trattamenti);");

		database.setTransactionSuccessful();
		database.endTransaction();

		appendLog("upload Disturbi...OK");
	}

	public void caricaPatologia() {

		appendLog("upload Patologie...");

		database.beginTransaction();

		database.delete("PATOLOGIE", null, null);

		database.execSQL("insert into patologie select distinct null, patologie, 0 from stage;");

		database.setTransactionSuccessful();
		database.endTransaction();

		appendLog("upload Patologie...OK");
	}

	public void caricaTrattamenti() {

		appendLog("upload Trattamenti...");

		database.beginTransaction();

		database.delete("TRATTAMENTI", null, null);

		database.execSQL("insert into trattamenti select distinct null, trattamenti, 1 from stage;");

		database.setTransactionSuccessful();
		database.endTransaction();

		appendLog("upload Trattamenti...OK");
	}

	public void cancellaStage() {
		appendLog("delete Stage Area...");

		database.beginTransaction();
		database.delete("STAGE", null, null);
		database.setTransactionSuccessful();
		database.endTransaction();

		appendLog("delete Stage Area...OK");
	}

	public void caricaStage(String[] array_items) {

		appendLog("upload Stage Area...");

		database.beginTransaction();

		row.clear();

		row.put("TRATTAMENTI", array_items[0]);
		row.put("PATOLOGIE", array_items[1]);
		row.put("DISTURBI", array_items[2]);
		row.put("MENU_CLICCABILE", array_items[3]);
		row.put("RIFERIMENTI", array_items[4]);
		row.put("TEMPO", array_items[5]);
		row.put("PMAXRF", array_items[6]);
		row.put("TACQUA", array_items[7]);
		row.put("DTEMPERATURA", array_items[8]);

		database.insert("STAGE", null, row);

		database.setTransactionSuccessful();
		database.endTransaction();

		appendLog("upload Stage Area...OK");

	}

	public void poweroff() {

		try {

			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());

			appendLog("shoutdown...");

			os.writeBytes("reboot -p" + "\n");
			os.writeBytes("exit\n");
			os.flush();

			appendLog("shoutdown...OK");

		} catch (IOException e) {
			e.printStackTrace();
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

	public static byte[] stringToBytesASCII(String str) {

		byte[] b = new byte[str.length()];
		for (int i = 0; i < b.length; i++) {

			b[i] = (byte) str.charAt(i);
		}
		return b;
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

	public void set_menu_items(String[] array_items) {

		return;
	}

	public ArrayList<Menu_app> get_menu_items(String table_name) {

		cur = database.query(table_name, new String[] { "MENU_ITEM",
				"MENU_CLICCABILE" }, null, null, null, null, null);

		cur.moveToFirst();

		ArrayList<Menu_app> menu_list = new ArrayList<Menu_app>();

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			menu_list.add(new Menu_app(cur.getString(0), cur.getInt(1) > 0));
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
}
