package it.app.hypertherm;

import it.app.hypertherm.db.HyperthermDataSource;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Caricamento {

	private Context activity;

	// VARIABILI DATA BASE
	private static final String DATABASE_NAME = "Hypertherm.db";
	private static SQLiteDatabase database;
	private HyperthermDataSource datasource;
	private Cursor cur;

	public Caricamento(Context activity, String[] array_items) {
		this.activity = activity;

		datasource = new HyperthermDataSource(activity.getApplicationContext());
		datasource.open();

		database = activity.openOrCreateDatabase(DATABASE_NAME,
				SQLiteDatabase.CREATE_IF_NECESSARY, null);

		ContentValues row = new ContentValues();

		database.beginTransaction();

		for (int i = 0; i < array_items.length; i++) {

			if (i % 9 == 1) {

				row.clear();

				row.put("TRATTAMENTI", array_items[i]);
				row.put("PATOLOGIE", array_items[i + 1]);
				row.put("DISTURBI", array_items[i + 2]);
				row.put("MENU_CLICCABILE", array_items[i + 3]);
				row.put("RIFERIMENTI", array_items[i + 4]);
				row.put("TEMPO", array_items[i + 5]);
				row.put("PMAXRF", array_items[i + 6]);
				row.put("TACQUA", array_items[i + 7]);
				row.put("DTEMPERATURA", array_items[i + 8]);

				database.insert("STAGE", null, row);

			}

		}

		database.setTransactionSuccessful();
		database.endTransaction();

	}

	public void caricaTrattamenti() {

		database.beginTransaction();

		database.delete("TRATTAMENTI", null, null);

		database.rawQuery(
				"insert into trattamenti select distinct trattamenti from stage",
				null);

		database.setTransactionSuccessful();
		database.endTransaction();

	}
}
