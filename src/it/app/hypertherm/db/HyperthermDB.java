package it.app.hypertherm.db;

import it.app.hypertherm.util.Utility;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HyperthermDB extends SQLiteOpenHelper {

	private Utility utility;

	private static final String DATABASE_NAME = "Hypertherm.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_WORK_TIME = "WORK_TIME";
	public static final String TABLE_STAGE_STRING = "STAGE_STRING";
	public static final String TABLE_STAGE_PATOLOGIE = "STAGE_PATALOGIE";
	public static final String TABLE_STAGE_STRUTTURA = "STAGE_STRUTTURA";
	public static final String TABLE_STAGE_DEFAULT = "STAGE_DEFAULT";
	public static final String TABLE_DISTURBI = "DISTURBI";
	public static final String TABLE_PASSWORD = "PASSWORD";
	public static final String TABLE_SETTINGS = "SETTINGS";
	public static final String TABLE_MENU = "MENU";

	public static final String STRUTTURA_MENU = "STRUTTURA";
	public static final String TRATTAMENTI_MENU = "TRATTAMENTI";
	public static final String PROFONDITA_MENU = "PROFONDITA";
	public static final String PROFONDITA_LABEL_MENU = "PROFONDITA_LABEL";
	public static final String PATOLOGIE_MENU = "PATOLOGIE";
	public static final String DISTURBI_MENU = "DISTURBI";
	public static final String RIFERIMENTI_MENU = "RIFERIMENTI";
	public static final String TEMPO_MENU = "TEMPO";
	public static final String PMAXRF_MENU = "PMAXRF";
	public static final String TACQUA_MENU = "TACQUA";
	public static final String DTEMPERATURA_MENU = "DTEMPERATURA";

	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_WORK_FROM = "WORK_FROM";
	public static final String COLUMN_PASSWORD = "PWD";
	public static final String COLUMN_SMART = "SMART";
	public static final String COLUMN_PHYSIO = "PHYSIO";
	public static final String COLUMN_SERIAL_NUMBER = "SERIAL_NUMBER";
	public static final String COLUMN_LANGUAGE = "LANGUAGE";
	public static final String COLUMN_MENU_ITEM = "MENU_ITEM";
	public static final String COLUMN_MENU_VALUE = "VALUE";
	public static final String COLUMN_MENU_CLICCABILE = "MENU_CLICCABILE";
	public static final String COLUMN_MENU_ID_TRATTAMENTO = "ID_TRATTAMENTO";
	public static final String COLUMN_MENU_ID_PATOLOGIA = "ID_PATOLOGIA";
	public static final String COLUMN_TIMEOUT = "TIMEOUT";
	public static final String COLUMN_TIMEOUT_SPLASH = "TIMEOUT_SPLASH";
	public static final String COLUMN_TIMEOUT_PING = "TIMEOUT_PING";
	public static final String COLUMN_TIMEOUT_READ = "TIMEOUT_READ";
	public static final String COLUMN_TIMEOUT_WRITE = "TIMEOUT_WRITE";
	public static final String COLUMN_TIMEOUT_RESET = "TIMEOUT_RESET";
	public static final String COLUMN_TIMEOUT_SIMULATORE = "TIMEOUT_SIMULATORE";

	private static final String CREATE_TABLE_STAGE_STRING = "create table "
			+ TABLE_STAGE_STRING + "(" + COLUMN_MENU_ITEM + " varchar(50), "
			+ COLUMN_MENU_VALUE + " varchar(270));";

	private static final String CREATE_TABLE_STAGE_DEFAULT = "create table "
			+ TABLE_STAGE_DEFAULT + "(" + COLUMN_MENU_ITEM + " varchar(50), "
			+ TEMPO_MENU + " integer, " + PMAXRF_MENU + " integer, "
			+ TACQUA_MENU + " FLOAT, " + DTEMPERATURA_MENU + " FLOAT" + ");";

	private static final String CREATE_TABLE_STAGE_STRUTTURA = "create table "
			+ TABLE_STAGE_STRUTTURA + "(" + STRUTTURA_MENU + " varchar(50), "
			+ PROFONDITA_MENU + " varchar(1), " + PROFONDITA_LABEL_MENU
			+ " varchar(50), " + RIFERIMENTI_MENU + " varchar(50), "
			+ TEMPO_MENU + " integer, " + PMAXRF_MENU + " integer, "
			+ TACQUA_MENU + " FLOAT, " + DTEMPERATURA_MENU + " FLOAT" + ");";

	private static final String CREATE_TABLE_STAGE_PATALOGIE = "create table "
			+ TABLE_STAGE_PATOLOGIE + "(" + TRATTAMENTI_MENU + " varchar(50), "
			+ PATOLOGIE_MENU + " varchar(50), " + DISTURBI_MENU
			+ " varchar(50), " + RIFERIMENTI_MENU + " varchar(50), "
			+ TEMPO_MENU + " integer, " + PMAXRF_MENU + " integer, "
			+ TACQUA_MENU + " FLOAT, " + DTEMPERATURA_MENU + " FLOAT" + ");";

	private static final String CREATE_TABLE_DISTURBI = "create table "
			+ DISTURBI_MENU + "(COLUMN_ID"
			+ " integer primary key autoincrement NOT NULL, "
			+ COLUMN_MENU_ITEM + " varchar(50), " + COLUMN_MENU_ID_TRATTAMENTO
			+ " integer, " + COLUMN_MENU_ID_PATOLOGIA + " integer, "
			+ TEMPO_MENU + " integer, " + PMAXRF_MENU + " integer, "
			+ TACQUA_MENU + " FLOAT, " + DTEMPERATURA_MENU + " FLOAT );";

	private static final String CREATE_TABLE_TRATTAMENTI = "create table "
			+ TRATTAMENTI_MENU + "(COLUMN_ID"
			+ " integer primary key autoincrement NOT NULL, "
			+ COLUMN_MENU_ITEM + " varchar(50), " + COLUMN_MENU_CLICCABILE
			+ " bit);";

	private static final String CREATE_TABLE_PATOLOGIE = "create table "
			+ PATOLOGIE_MENU + "(" + COLUMN_ID
			+ " integer primary key autoincrement NOT NULL, "
			+ COLUMN_MENU_ITEM + " varchar(50), " + COLUMN_MENU_CLICCABILE
			+ " bit);";

	private static final String CREATE_TABLE_MENU = "create table "
			+ TABLE_MENU + "(" + COLUMN_MENU_ITEM + " varchar(50), "
			+ COLUMN_MENU_CLICCABILE + " bit);";

	private static final String CREATE_TABLE_TABLE_WORK_TIME = "create table "
			+ TABLE_WORK_TIME + "(" + COLUMN_WORK_FROM
			+ " integer primary key NOT NULL DEFAULT 1 " + " );";

	// private static final String CREATE_TABLE_TABLE_PASSWORD = "create table "
	// + TABLE_PASSWORD + "(" + COLUMN_PASSWORD + " VARCHAR(20));";

	private static final String CREATE_TABLE_TABLE_SETTINGS = "create table "
			+ TABLE_SETTINGS + "(" + COLUMN_SERIAL_NUMBER + " VARCHAR(20), "
			+ COLUMN_LANGUAGE + " varchar(3), " + COLUMN_TIMEOUT + " integer, "
			+ COLUMN_TIMEOUT_PING + " integer, " + COLUMN_TIMEOUT_READ
			+ " integer, " + COLUMN_TIMEOUT_WRITE + " integer, "
			+ COLUMN_TIMEOUT_RESET + " integer, " + COLUMN_TIMEOUT_SIMULATORE
			+ " integer, " + COLUMN_TIMEOUT_SPLASH + " integer);";

	public HyperthermDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase database) {

		utility = new Utility();

		database.execSQL(CREATE_TABLE_STAGE_PATALOGIE);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_STAGE_PATALOGIE);
		database.execSQL(CREATE_TABLE_STAGE_STRUTTURA);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_STAGE_STRUTTURA);
		database.execSQL(CREATE_TABLE_STAGE_DEFAULT);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_STAGE_DEFAULT);
		database.execSQL(CREATE_TABLE_STAGE_STRING);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_STAGE_STRING);
		database.execSQL(CREATE_TABLE_DISTURBI);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_DISTURBI);
		database.execSQL(CREATE_TABLE_TABLE_WORK_TIME);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_TABLE_WORK_TIME);
		// database.execSQL(CREATE_TABLE_TABLE_PASSWORD);
		// utility.appendLog("D","Creo tabella..." +
		// CREATE_TABLE_TABLE_PASSWORD);
		database.execSQL(CREATE_TABLE_MENU);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_MENU);
		database.execSQL(CREATE_TABLE_TABLE_SETTINGS);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_TABLE_SETTINGS);
		database.execSQL(CREATE_TABLE_TRATTAMENTI);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_TRATTAMENTI);
		database.execSQL(CREATE_TABLE_PATOLOGIE);
		utility.appendLog("D", "Creo tabella..." + CREATE_TABLE_PATOLOGIE);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ContentValues row = new ContentValues();
		row.put(COLUMN_WORK_FROM, 1);
		database.beginTransaction();
		database.insert(TABLE_WORK_TIME, null, row);
		row.clear();
		row.put(COLUMN_SERIAL_NUMBER, "SN ");
		row.put(COLUMN_LANGUAGE, "Ita");
		row.put(COLUMN_TIMEOUT, 3);
		row.put(COLUMN_TIMEOUT_SPLASH, 2500);
		row.put(COLUMN_TIMEOUT_PING, 100);
		row.put(COLUMN_TIMEOUT_READ, 500);
		row.put(COLUMN_TIMEOUT_WRITE, 500);
		row.put(COLUMN_TIMEOUT_RESET, 1500);
		row.put(COLUMN_TIMEOUT_SIMULATORE, 500);
		database.insert(TABLE_SETTINGS, null, row);
		row.clear();

		// byte[] salt = new byte[16];
		// KeySpec spec = new PBEKeySpec("240776".toCharArray(), salt, 65536,
		// 128);
		// SecretKeyFactory f;
		// byte[] hash = null;
		//
		// try {
		// f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		// hash = f.generateSecret(spec).getEncoded();
		// } catch (NoSuchAlgorithmException e) {
		// e.printStackTrace();
		// } catch (InvalidKeySpecException e) {
		// e.printStackTrace();
		// }
		//
		// row.put("PWD", new BigInteger(1, hash).toString(16));
		// database.insert(TABLE_PASSWORD, null, row);

		database.setTransactionSuccessful();
		database.endTransaction();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
