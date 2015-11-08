package it.app.hypertherm;

import it.app.hypertherm.db.HyperthermDB;
import it.app.hypertherm.db.HyperthermDataSource;
import it.app.hypertherm.util.Utility;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class Caricamento {

	// VARIABILI DATA BASE
	private static final String DATABASE_NAME = "Hypertherm.db";
	private static SQLiteDatabase database;
	private HyperthermDataSource datasource;
	private ContentValues row = new ContentValues();

	private Utility utility = new Utility();

	public Caricamento(Activity activity) {

		datasource = new HyperthermDataSource(activity.getApplicationContext());
		datasource.open();

		database = activity.openOrCreateDatabase(DATABASE_NAME,
				SQLiteDatabase.CREATE_IF_NECESSARY, null);

		import_file();

	}

	private void import_file() {

		try { // ParaPatologie

			File file = new File(Environment.getExternalStorageDirectory()
					+ "/Hypertherm/conf/ParaPatologie" + utility.getLanguage()
					+ ".txt");

			if (file.exists()) {

				utility.appendLog("D",
						"upload dati macchina (ParaPatologie)...");

				utility.cancellaStage(HyperthermDB.TABLE_STAGE_PATOLOGIE);
				utility.cancellaStage(HyperthermDB.DISTURBI_MENU);
				utility.cancellaStage(HyperthermDB.PATOLOGIE_MENU);
				utility.cancellaStage(HyperthermDB.TRATTAMENTI_MENU);

				FileInputStream fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String strLine = null;

				while ((strLine = br.readLine()) != null) {
					utility.appendLog("D", strLine);
					caricaStage(HyperthermDB.TABLE_STAGE_PATOLOGIE,
							strLine.split("\\|"));
				}

				in.close();

				utility.appendLog("D",
						"upload dati macchina (ParaPatologie)...OK");

				utility.appendLog("D", "move file (ParaPatologie)...");
				utility.spostaFile(file);
				utility.appendLog("D", "move file (ParaPatologie)...OK");

				caricaTrattamenti();
				caricaPatologia();
				caricaDisturbi();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {// ParaStruttura

			File file = new File(Environment.getExternalStorageDirectory()
					+ "/Hypertherm/conf/ParaStruttura" + utility.getLanguage()
					+ ".txt");

			if (file.exists()) {

				utility.appendLog("D",
						"upload dati macchina (ParaStruttura)...");

				utility.cancellaStage(HyperthermDB.TABLE_STAGE_STRUTTURA);

				FileInputStream fstream = new FileInputStream(file);

				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String strLine = null;

				while ((strLine = br.readLine()) != null) {
					utility.appendLog("D", strLine);
					caricaStage(HyperthermDB.TABLE_STAGE_STRUTTURA,
							strLine.split("\\|"));
				}

				in.close();

				utility.appendLog("D",
						"upload dati macchina (ParaStruttura)...OK");

				utility.appendLog("D", "move file (ParaStruttura)...");
				utility.spostaFile(file);
				utility.appendLog("D", "move file (ParaStruttura)...OK");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try { // ParaDefault

			File file = new File(Environment.getExternalStorageDirectory()
					+ "/Hypertherm/conf/ParaDefault" + utility.getLanguage()
					+ ".txt");

			if (file.exists()) {

				utility.appendLog("D", "upload dati macchina (ParaDefault)...");

				utility.cancellaStage(HyperthermDB.TABLE_STAGE_DEFAULT);

				FileInputStream fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String strLine = null;

				while ((strLine = br.readLine()) != null) {
					utility.appendLog("D", strLine);
					caricaStage(HyperthermDB.TABLE_STAGE_DEFAULT,
							strLine.split("\\|"));
				}

				in.close();

				utility.appendLog("D",
						"upload dati macchina (ParaDefault)...OK");

				utility.appendLog("D", "move file (ParaDefault)...");
				utility.spostaFile(file);
				utility.appendLog("D", "move file (ParaDefault)...OK");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try { // ParaString

			File file = new File(Environment.getExternalStorageDirectory()
					+ "/Hypertherm/conf/ParaString" + utility.getLanguage()
					+ ".txt");

			if (file.exists()) {

				utility.appendLog("D", "upload dati macchina (ParaString)...");

				utility.cancellaStage(HyperthermDB.TABLE_STAGE_STRING);

				FileInputStream fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String strLine = null;

				while ((strLine = br.readLine()) != null) {
					utility.appendLog("D", strLine);
					caricaStage(HyperthermDB.TABLE_STAGE_STRING,
							strLine.split("\\="));
				}

				in.close();

				utility.appendLog("D", "update Table Menu...");

				database.beginTransaction();

				database.delete(HyperthermDB.TABLE_MENU, null, null);

				database.setTransactionSuccessful();
				database.endTransaction();

				database.beginTransaction();

				database.execSQL("insert into menu select VALUE, 1 from "
						+ HyperthermDB.TABLE_STAGE_STRING
						+ " where menu_item in ('menu_item_patologia', 'menu_item_struttura_profondita', 'menu_item_manual_selection', 'menu_item_demo_training' ,'menu_item_user_manual');");

				database.setTransactionSuccessful();
				database.endTransaction();

				utility.appendLog("D", "update Table Menu...OK");

				utility.appendLog("D", "upload dati macchina (ParaString)...OK");

				utility.appendLog("D", "move file (ParaString)...");
				utility.spostaFile(file);
				utility.appendLog("D", "move file (ParaString)...OK");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try { // ParaConfig

			File file = new File(Environment.getExternalStorageDirectory()
					+ "/Hypertherm/conf/ParaConfig" + utility.getLanguage()
					+ ".txt");

			if (file.exists()) {

				utility.appendLog("D", "upload dati macchina (ParaConfig)...");

				FileInputStream fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);

				Properties properties = new Properties();
				properties.load(fstream);
				String[] array_properties = new String[properties.size()];

				array_properties[0] = properties
						.getProperty("time_out_splash_screen");
				array_properties[1] = properties.getProperty("time_out_ping");
				array_properties[2] = properties.getProperty("time_out_read");
				array_properties[3] = properties.getProperty("time_out_write");
				array_properties[4] = properties.getProperty("time_out_reset");
				array_properties[5] = properties
						.getProperty("time_out_simulatore");

				caricaStage(HyperthermDB.TABLE_SETTINGS, array_properties);

				in.close();

				utility.appendLog("D", "upload dati macchina (ParaConfig)...OK");

				utility.appendLog("D", "move file (ParaConfig)...");
				utility.spostaFile(file);
				utility.appendLog("D", "move file (ParaConfig)...OK");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void caricaDisturbi() {

		utility.appendLog("D", "upload Disturbi...");

		database.beginTransaction();

		database.execSQL("insert into disturbi select null, a.disturbi, c.COLUMN_ID id_trattamento, b.ID id_patologia, a.TEMPO, a.PMAXRF, a.TACQUA, a.DTEMPERATURA from STAGE_PATALOGIE a inner join patologie b on (a.PATOLOGIE=b.menu_item) inner join trattamenti c on (c.MENU_ITEM=a.trattamenti);");

		database.setTransactionSuccessful();
		database.endTransaction();

		utility.appendLog("D", "upload Disturbi...OK");
	}

	public void caricaPatologia() {

		utility.appendLog("D", "upload Patologie...");

		database.beginTransaction();

		database.execSQL("insert into patologie select distinct null, patologie, 0 from STAGE_PATALOGIE;");

		database.setTransactionSuccessful();
		database.endTransaction();

		utility.appendLog("D", "upload Patologie...OK");
	}

	public void caricaTrattamenti() {

		utility.appendLog("D", "upload Trattamenti...");

		database.beginTransaction();

		database.execSQL("insert into trattamenti select distinct null, trattamenti, 1 from STAGE_PATALOGIE;");

		database.setTransactionSuccessful();
		database.endTransaction();

		utility.appendLog("D", "upload Trattamenti...OK");
	}

	public void caricaStage(String table_name, String[] array_items) {

		utility.appendLog("D", "upload Stage Area...");

		database.beginTransaction();

		row.clear();

		if (table_name.equals(HyperthermDB.TABLE_STAGE_PATOLOGIE)) {

			row.put(HyperthermDB.TRATTAMENTI_MENU, array_items[0]);
			row.put(HyperthermDB.PATOLOGIE_MENU, array_items[1]);
			row.put(HyperthermDB.DISTURBI_MENU, array_items[2]);
			row.put(HyperthermDB.RIFERIMENTI_MENU, array_items[3]);
			row.put(HyperthermDB.TEMPO_MENU, array_items[4]);
			row.put(HyperthermDB.PMAXRF_MENU, array_items[5]);
			row.put(HyperthermDB.TACQUA_MENU, array_items[6]);
			row.put(HyperthermDB.DTEMPERATURA_MENU, array_items[7]);

			database.insert(HyperthermDB.TABLE_STAGE_PATOLOGIE, null, row);

		} else if (table_name.equals(HyperthermDB.TABLE_STAGE_STRUTTURA)) {

			row.put(HyperthermDB.STRUTTURA_MENU, array_items[0]);
			row.put(HyperthermDB.PROFONDITA_MENU, array_items[1]);
			row.put(HyperthermDB.PROFONDITA_LABEL_MENU, array_items[2]);
			row.put(HyperthermDB.RIFERIMENTI_MENU, array_items[3]);
			row.put(HyperthermDB.TEMPO_MENU, array_items[4]);
			row.put(HyperthermDB.PMAXRF_MENU, array_items[5]);
			row.put(HyperthermDB.TACQUA_MENU, array_items[6]);
			row.put(HyperthermDB.DTEMPERATURA_MENU, array_items[7]);

			database.insert(HyperthermDB.TABLE_STAGE_STRUTTURA, null, row);

		} else if (table_name.equals(HyperthermDB.TABLE_STAGE_DEFAULT)) {

			row.put(HyperthermDB.COLUMN_MENU_ITEM, array_items[0]);
			row.put(HyperthermDB.TEMPO_MENU, array_items[1]);
			row.put(HyperthermDB.PMAXRF_MENU, array_items[2]);
			row.put(HyperthermDB.TACQUA_MENU, array_items[3]);
			row.put(HyperthermDB.DTEMPERATURA_MENU, array_items[4]);

			database.insert(HyperthermDB.TABLE_STAGE_DEFAULT, null, row);

		} else if (table_name.equals(HyperthermDB.TABLE_STAGE_STRING)) {

			for (int i = 0; i < array_items.length - 1; i += 2) {

				row.put(HyperthermDB.COLUMN_MENU_ITEM, array_items[i].trim());
				row.put(HyperthermDB.COLUMN_MENU_VALUE,
						array_items[i + 1].trim());

				database.insert(HyperthermDB.TABLE_STAGE_STRING, null, row);

				row.clear();

			}

		} else if (table_name.equals(HyperthermDB.TABLE_SETTINGS)) {

			row.put(HyperthermDB.COLUMN_TIMEOUT_SPLASH, array_items[0]);
			row.put(HyperthermDB.COLUMN_TIMEOUT_PING, array_items[1]);
			row.put(HyperthermDB.COLUMN_TIMEOUT_READ, array_items[2]);
			row.put(HyperthermDB.COLUMN_TIMEOUT_WRITE, array_items[3]);

			database.update(HyperthermDB.TABLE_SETTINGS, row, null, null);

		}

		database.setTransactionSuccessful();
		database.endTransaction();

		utility.appendLog("D", "upload Stage Area...OK");

	}
}
