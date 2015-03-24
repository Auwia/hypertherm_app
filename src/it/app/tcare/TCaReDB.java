package it.app.tcare;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TCaReDB extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "TCaReDB.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_WORK_TIME = "WORK_TIME";
	public static final String TABLE_PASSWORD = "PASSWORD";

	public static final String COLUMN_WORK_FROM = "WORK_FROM";
	public static final String COLUMN_PASSWORD = "PWD";

	private static final String CREATE_TABLE_TABLE_WORK_TIME = "create table "
			+ TABLE_WORK_TIME + "(" + COLUMN_WORK_FROM
			+ " integer primary key NOT NULL DEFAULT 1 " + " );";

	private static final String CREATE_TABLE_TABLE_PASSWORD = "create table "
			+ TABLE_PASSWORD + "(" + COLUMN_PASSWORD + " VARCHAR(20));";

	public TCaReDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_TABLE_WORK_TIME);
		database.execSQL(CREATE_TABLE_TABLE_PASSWORD);

		ContentValues row = new ContentValues();
		row.put("WORK_FROM", 1);
		database.beginTransaction();
		database.insert(TABLE_WORK_TIME, null, row);
		row.clear();
		row.put("PWD", "29eac7c860aeaaf89a77364cd1a05f24");
		database.insert(TABLE_PASSWORD, null, row);
		database.setTransactionSuccessful();
		database.endTransaction();
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
	}

	@Override
	public void onDowngrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
	}
}
