package it.app.tcare_serial;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TCaReDB extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "TCaReDB.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_WORK_TIME = "WORK_TIME";
	public static final String TABLE_PASSWORD = "PASSWORD";
	public static final String TABLE_SETTINGS = "SETTINGS";

	public static final String COLUMN_WORK_FROM = "WORK_FROM";
	public static final String COLUMN_PASSWORD = "PWD";
	public static final String COLUMN_SMART = "SMART";
	public static final String COLUMN_PHYSIO = "PHYSIO";
	public static final String COLUMN_SERIAL_NUMBER = "SERIAL_NUMBER";
	public static final String COLUMN_LANGUAGE = "LANGUAGE";
	public static final String COLUMN_TIMEOUT = "TIMEOUT";

	private static final String CREATE_TABLE_TABLE_WORK_TIME = "create table "
			+ TABLE_WORK_TIME + "(" + COLUMN_WORK_FROM
			+ " integer primary key NOT NULL DEFAULT 1 " + " );";

	private static final String CREATE_TABLE_TABLE_PASSWORD = "create table "
			+ TABLE_PASSWORD + "(" + COLUMN_PASSWORD + " VARCHAR(20));";

	private static final String CREATE_TABLE_TABLE_SETTINGS = "create table "
			+ TABLE_SETTINGS + "(" + COLUMN_SMART + " bit, " + COLUMN_PHYSIO
			+ " bit, " + COLUMN_SERIAL_NUMBER + " VARCHAR(20), "
			+ COLUMN_LANGUAGE + " varchar(2), " + COLUMN_TIMEOUT + " integer);";

	public TCaReDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase database) {

		database.execSQL(CREATE_TABLE_TABLE_WORK_TIME);
		database.execSQL(CREATE_TABLE_TABLE_PASSWORD);

		Log.d("TCARE", "CREO TABELLA: " + CREATE_TABLE_TABLE_SETTINGS);

		database.execSQL(CREATE_TABLE_TABLE_SETTINGS);

		ContentValues row = new ContentValues();
		row.put(COLUMN_WORK_FROM, 1);
		database.beginTransaction();
		database.insert(TABLE_WORK_TIME, null, row);
		row.clear();
		row.put(COLUMN_SMART, 1);
		row.put(COLUMN_PHYSIO, 0);
		row.put(COLUMN_SERIAL_NUMBER, "SN ");
		row.put(COLUMN_LANGUAGE, "en");
		row.put(COLUMN_TIMEOUT, 3);
		database.insert(TABLE_SETTINGS, null, row);
		row.clear();

		byte[] salt = new byte[16];
		KeySpec spec = new PBEKeySpec("240776".toCharArray(), salt, 65536, 128);
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

		row.put("PWD", new BigInteger(1, hash).toString(16));
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
