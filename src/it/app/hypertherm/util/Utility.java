package it.app.hypertherm.util;

import it.app.hypertherm.Menu_app;
import it.app.hypertherm.R;
import it.app.hypertherm.Tracciato;
import it.app.hypertherm.activity.WorkActivity;
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Utility {

	private Activity activity;

	private static String TAG = "HYPERTHERM";

	private SharedPreferences preferences;

	private TextView antenna_black_label_up, antenna_black_label_down,
			water_label_down, deltat_label_down, time_label_down,
			water_label_up, deltat_label_up, time_label_up, suggerimenti,
			disturbo_label;

	private Button button_play, button_stop, button_pause, button_bolus_down,
			button_bolus_up, button_home, button_power, button_antenna,
			button_time;

	private LinearLayout zero, dieci, venti, trenta, quaranta, cinquanta,
			sessanta, settanta, ottanta, novanta;

	private ImageView grafico;

	private int Ref_power, Dir_power, iPower, iD_temp, iH2o_temp, iTime,
			comando;

	private byte[] In_Output_temp;

	private final static int ARANCIONE = Color.parseColor("#ffa500");
	// private final static int VERDE = Color.parseColor("#ccff00");
	private final static int VERDE = Color.GREEN;

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

		antenna_black_label_up = (TextView) activity
				.findViewById(R.id.antenna_black_label_up);
		water_label_up = (TextView) activity.findViewById(R.id.water_label_up);
		deltat_label_up = (TextView) activity
				.findViewById(R.id.deltat_label_up);
		time_label_up = (TextView) activity.findViewById(R.id.time_label_up);
		water_label_down = (TextView) activity
				.findViewById(R.id.water_label_down);
		deltat_label_down = (TextView) activity
				.findViewById(R.id.deltat_label_down);
		antenna_black_label_down = (TextView) activity
				.findViewById(R.id.antenna_black_label_down);
		time_label_down = (TextView) activity
				.findViewById(R.id.time_label_down);
		suggerimenti = (TextView) activity.findViewById(R.id.suggerimenti);
		disturbo_label = (TextView) activity.findViewById(R.id.disturbo_label);

		button_play = (Button) activity.findViewById(R.id.button_play);
		button_pause = (Button) activity.findViewById(R.id.button_pause);
		button_stop = (Button) activity.findViewById(R.id.button_stop);
		button_bolus_down = (Button) activity
				.findViewById(R.id.button_bolus_down);
		button_bolus_up = (Button) activity.findViewById(R.id.button_bolus_up);
		button_home = (Button) activity.findViewById(R.id.button_home);
		button_power = (Button) activity.findViewById(R.id.button_power);
		button_antenna = (Button) activity
				.findViewById(R.id.button_antenna_black);
		button_time = (Button) activity.findViewById(R.id.button_time);

		zero = (LinearLayout) activity.findViewById(R.id.zero);
		dieci = (LinearLayout) activity.findViewById(R.id.dieci);
		venti = (LinearLayout) activity.findViewById(R.id.venti);
		trenta = (LinearLayout) activity.findViewById(R.id.trenta);
		quaranta = (LinearLayout) activity.findViewById(R.id.quaranta);
		cinquanta = (LinearLayout) activity.findViewById(R.id.cinquanta);
		sessanta = (LinearLayout) activity.findViewById(R.id.sessanta);
		settanta = (LinearLayout) activity.findViewById(R.id.settanta);
		ottanta = (LinearLayout) activity.findViewById(R.id.ottanta);
		novanta = (LinearLayout) activity.findViewById(R.id.novanta);

		// grafico = (ImageView) activity.findViewById(R.id.grafico);

	}

	public Utility() {
	}

	public String[] bytesToHex3(byte[] array) {

		String[] risultato = new String[array.length];

		for (int k = 0; k < array.length; k++) {
			risultato[k] = UnicodeFormatter.byteToHex(array[k]).toUpperCase(
					Locale.getDefault());
		}

		return risultato;
	}

	public String bytesToString(byte[] array) {

		String risultato = "";

		String[] c = bytesToHex3(array);

		for (int i = 0; i < Tracciato.PACKET_SIZE; i++) {

			risultato += c[i] + " ";

		}

		return risultato;
	}

	public int calcola_check_sum(byte[] buffer) {

		int[] risultato = bytearray2intarray(buffer);

		int somma = 0;

		if (risultato.length == 64) {

			for (int i = 0; i < risultato.length; i++) {
				if (i > 1) {
					somma += risultato[i];
				}
			}
		} else {
			appendLog("E", "TRACCIATO ERRATO PER CALCOLO CHECK SUM");
		}
		return somma;
	}

	public int stampa_check_sum(byte[] buffer) {

		int[] risultato = bytearray2intarray(buffer);

		int somma = 0;

		for (int i = 0; i < risultato.length; i++) {
			if (i > 1) {
				appendLog("D", "buf[" + i + "] = " + risultato[i]);
				somma += risultato[i];
			}
		}
		return somma;
	}

	public int[] bytearray2intarray(byte[] barray) {
		int[] iarray = new int[barray.length];
		int i = 0;
		for (byte b : barray)
			iarray[i++] = b & 0xff;
		return iarray;
	}

	final protected char[] hexArray = "0123456789ABCDEF".toCharArray();

	public String bytesToHex(byte[] bytes) {

		char[] hexChars = null;

		if (bytes != null) {

			hexChars = new char[bytes.length * 2];
			for (int j = 0; j < bytes.length; j++) {
				int v = bytes[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
			}
		}

		return new String(hexChars);
	}

	public char[] bytesToHex2(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 8];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return hexChars;
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
					button_time.setPressed(true);

					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {

							setColoriPiramide();

						}
					});

					suggerimenti.setText("");

					break;

				case 512: // PAUSE
					button_play.setPressed(false);
					button_pause.setPressed(true);
					button_stop.setPressed(false);

					reset_piramide();

					break;

				case 768: // STOP
					button_play.setPressed(false);
					button_pause.setPressed(false);
					button_stop.setPressed(true);
					button_home.setEnabled(true);
					button_time.setPressed(false);

					reset_piramide();

					suggerimenti.setText(get_suggerimento_trattamento());

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

				case 1100: // RESET
					break;

				}

			}
		});
	}

	public boolean esegui_buffer(byte[] temp) {

		if (temp.length > 0) {

			int CheckSum = ((int) temp[0]) & 0xFF;
			CheckSum |= (((int) temp[1]) & 0xFF) << 8;
			int Ver = ((int) temp[2]) & 0xFF;
			int TimStmp = ((int) temp[3]) & 0xFF;

			byte[] msk = new byte[4];
			msk[0] = temp[4];
			msk[1] = temp[5];
			msk[2] = temp[6];
			msk[3] = temp[7];
			String msk_binary = toBinary(msk);
			int[] msk1 = new int[4];
			msk1[0] = temp[4] & 0xFF;
			msk1[1] = temp[5] & 0xFF;
			msk1[2] = temp[6] & 0xFF;
			msk1[3] = temp[7] & 0xFF;

			In_Output_temp = new byte[4];
			In_Output_temp[0] = temp[8];
			In_Output_temp[1] = temp[9];
			In_Output_temp[2] = temp[10];
			In_Output_temp[3] = temp[11];

			String In_Output = toBinary(In_Output_temp);

			int Cmd = ((int) temp[12]) & 0xFF;
			Cmd |= (((int) temp[13]) & 0xFF) << 8;
			iTime = ((int) temp[14]) & 0xFF;
			iTime |= (((int) temp[15]) & 0xFF) << 8;
			iD_temp = ((int) temp[16]) & 0xFF;
			iD_temp |= (((int) temp[17]) & 0xFF) << 8;
			iH2o_temp = ((int) temp[18]) & 0xFF;
			iH2o_temp |= (((int) temp[19]) & 0xFF) << 8;
			int iColdHp_temp = ((int) temp[20]) & 0xFF;
			iColdHp_temp |= (((int) temp[21]) & 0xFF) << 8;
			iPower = ((int) temp[22]) & 0xFF;
			iPower |= (((int) temp[23]) & 0xFF) << 8;

			int Gain_D_temp = ((int) temp[24]) & 0xFF;
			Gain_D_temp |= (((int) temp[25]) & 0xFF) << 8;
			int Offset_D_temp = ((int) temp[26]) & 0xFF;
			Offset_D_temp |= (((int) temp[27]) & 0xFF) << 8;
			int Gain_H2o_temp = ((int) temp[28]) & 0xFF;
			Gain_H2o_temp |= (((int) temp[29]) & 0xFF) << 8;
			int Offset_H2o_temp = ((int) temp[30]) & 0xFF;
			Offset_H2o_temp |= (((int) temp[31]) & 0xFF) << 8;
			int Gain_Cold_temp = ((int) temp[32]) & 0xFF;
			Gain_Cold_temp |= (((int) temp[33]) & 0xFF) << 8;
			int Offset_Cold_temp = ((int) temp[34]) & 0xFF;
			Offset_Cold_temp |= (((int) temp[35]) & 0xFF) << 8;
			int Gain_Boil_temp = ((int) temp[36]) & 0xFF;
			Gain_Boil_temp |= (((int) temp[37]) & 0xFF) << 8;
			int Offset_Boil_temp = ((int) temp[38]) & 0xFF;
			Offset_Boil_temp |= (((int) temp[39]) & 0xFF) << 8;
			int Req_power = ((int) temp[40]) & 0xFF;
			Req_power |= (((int) temp[41]) & 0xFF) << 8;

			Dir_power = ((int) temp[42]) & 0xFF;
			Dir_power |= (((int) temp[43]) & 0xFF) << 8;
			Ref_power = ((int) temp[44]) & 0xFF;
			Ref_power |= (((int) temp[45]) & 0xFF) << 8;
			int D_temp = ((int) temp[46]) & 0xFF;
			D_temp |= (((int) temp[47]) & 0xFF) << 8;
			int H2o_temp = ((int) temp[48]) & 0xFF;
			H2o_temp |= (((int) temp[49]) & 0xFF) << 8;

			int ColdHp_temp = ((int) temp[50]) & 0xFF;
			ColdHp_temp |= (((int) temp[51]) & 0xFF) << 8;
			int Boil_temp = ((int) temp[52]) & 0xFF;
			Boil_temp |= (((int) temp[53]) & 0xFF) << 8;

			int runningTime = ((int) temp[54]) & 0xFF;
			runningTime |= (((int) temp[55]) & 0xFF) << 8;

			int pwmRes = ((int) temp[56]) & 0xFF;
			int pwmPomp = ((int) temp[57]) & 0xFF;
			int pwmFan = ((int) temp[58]) & 0xFF;

			int[] last = new int[5];
			last[0] = temp[59] & 0xFF;
			last[1] = temp[60] & 0xFF;
			last[2] = temp[61] & 0xFF;
			last[3] = temp[62] & 0xFF;
			last[4] = temp[63] & 0xFF;

			// appendLog("I", "COMANDO_RICEVUTO:" + "CheckSum=" +
			// CheckSum
			// + " Ver=" + Ver + " TimStmp=" + TimStmp + " Msk="
			// + msk_binary + " Cmd=" + Cmd + " iTime=" + iTime
			// + " iD_temp=" + iD_temp + " iH2o_temp=" + iH2o_temp
			// + " iPower=" + iPower + " Dir_power=" + Dir_power
			// + " Ref_power=" + Ref_power + " D_temp=" + D_temp
			// + " H2o_temp=" + H2o_temp + " runningTime=" + runningTime
			// + " runningTime=" + runningTime);

			if (calcola_check_sum(temp) == CheckSum) {

				stampa_tracciato(temp, "D", "in");

				if (WorkActivity.SIMULATORE) {

					esegui(Cmd);

				} else {
					if (In_Output.substring(1, 2).equals("0")
							&& In_Output.substring(3, 4).equals("0")) {
						// STOP
						esegui(768);
					}

					if (In_Output.substring(1, 2).equals("1")
							&& In_Output.substring(3, 4).equals("1")) {
						// PLAY
						esegui(256);
					}

					if (In_Output.substring(1, 2).equals("1")
							&& In_Output.substring(3, 4).equals("0")) {
						// PAUSE
						esegui(512);
					}
				}

				if (button_play.isPressed() || button_pause.isPressed()) {
					SetTime(convertSecondsToMmSs(runningTime));
				} else {
					SetTime(convertSecondsToMmSs(0));
				}

				int d_temp = 0;
				if (D_temp >= 60000) {
					d_temp = (D_temp - 65536);
				} else {
					d_temp = D_temp;
				}
				if (d_temp > 0) {

					setDeltaT("+" + arrotondaPerEccesso(d_temp, 1));

				} else {

					setDeltaT("" + arrotondaPerEccesso(d_temp, 1));

				}

				int h2o_temp = 0;
				if (H2o_temp >= 60000) {
					h2o_temp = (H2o_temp - 65536);
				} else {
					h2o_temp = H2o_temp;
				}
				setWaterTemperature(String.valueOf(Float.parseFloat(""
						+ arrotondaPerEccesso(h2o_temp, 1))));

				setAntenna(""
						+ (int) Float.parseFloat(""
								+ arrotondaPerEccesso(Dir_power, 0)));

				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {

						if (button_play.isPressed()
								&& toBinary(In_Output_temp).substring(2, 3)
										.equals("1")) {
							button_antenna.setPressed(true);
						} else {
							button_antenna.setPressed(false);
						}

						water_label_down.setText(String.valueOf(Float
								.parseFloat(""
										+ arrotondaPerEccesso(iH2o_temp, 1))));
						WorkActivity.WATER = iH2o_temp;

						int id_temp = 0;
						if (iD_temp >= 60000) {
							id_temp = (iD_temp - 65536);
						} else {
							id_temp = iD_temp;
						}
						if (id_temp > 0) {

							deltat_label_down.setText("+".concat(String
									.valueOf(Float.parseFloat(""
											+ arrotondaPerEccesso(id_temp, 1)))));

						} else {
							deltat_label_down.setText(String.valueOf(Float
									.parseFloat(""
											+ arrotondaPerEccesso(id_temp, 1))));

						}
						WorkActivity.DELTAT = id_temp;

						antenna_black_label_down.setText(""
								+ (int) Float.parseFloat(""
										+ arrotondaPerEccesso(iPower, 0)));
						WorkActivity.POWER = iPower;

						time_label_down.setText(convertSecondsToMm(iTime));

						WorkActivity.TIMER = iTime;

						if (button_power.isPressed()) {
							WorkActivity.WATER_IMP = iH2o_temp;
							WorkActivity.DELTAT_IMP = id_temp;
						}

					}
				});

				return true;

			} else {
				appendLog("E", "Tracciato non conforme al checksum atteso="
						+ CheckSum + " checksum ricevuto="
						+ calcola_check_sum(temp));

				stampa_tracciato(temp, "E", "in");

				// stampa_check_sum(temp);

				return false;
			}

		} else {
			appendLog("E", "Comando CORROTTO!!!");
		}
		return false;
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

				// appendLog("Imposto il valore di acqua ricevuto dalla scheda..."
				// + valore);

				water_label_up.setText(valore);

				// appendLog("Imposto il valore di acqua ricevuto dalla scheda...OK");

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

				// appendLog("Imposto il valore di DeltaT ricevuto dalla scheda..."
				// + valore);

				deltat_label_up.setText(valore);

				// appendLog("Imposto il valore di DeltaT ricevuto dalla scheda...OK");

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

				// appendLog("Imposto il valore di antenna ricevuto dalla scheda..."
				// + valore);

				antenna_black_label_up.setText(valore);

				// appendLog("Imposto il valore di antenna ricevuto dalla scheda...OK");

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

				// appendLog("Imposto il valore di tempo ricevuto dalla scheda..."
				// + valore);

				time_label_up.setText(valore);

				// appendLog("Imposto il valore di tempo ricevuto dalla scheda...OK");

			}
		});
	}

	public String convertSecondsToMmSs(long seconds) {
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		return String.format("%02d:%02d", m, s);
	}

	public String convertSecondsToMm(long seconds) {
		long m = (seconds / 60) % 60;
		return String.format("%02d", m);
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

		String tmp = result
				.replace("\\n", System.getProperty("line.separator"));

		return tmp;
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

	public String get_menu_item_demo_training_2() {

		cur = database.query(HyperthermDB.TABLE_STAGE_STRING,
				new String[] { HyperthermDB.COLUMN_MENU_VALUE },
				HyperthermDB.COLUMN_MENU_ITEM + "=?",
				new String[] { "menu_item_demo_training_2" }, null, null, null);

		cur.moveToFirst();

		String result = "";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			result = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return result;
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
							"b.MENU_ITEM,c.MENU_ITEM", null, "b.column_id");
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

		cur = database.query(HyperthermDB.TABLE_SETTINGS,
				new String[] { "LANGUAGE" }, null, null, null, null, null);

		cur.moveToFirst();

		String language = "Ita";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			language = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return language;
	}

	public void appendLog(String severity, String text) {
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

			if (severity.equals("D")) {
				Log.d(TAG, sdf.format(new Date(yourmilliseconds)) + ": " + text);
			}

			if (severity.equals("E")) {
				Log.e(TAG, sdf.format(new Date(yourmilliseconds)) + ": " + text);
			}

			if (severity.equals("W")) {
				Log.w(TAG, sdf.format(new Date(yourmilliseconds)) + ": " + text);
			}

			if (severity.equals("I")) {
				Log.i(TAG, sdf.format(new Date(yourmilliseconds)) + ": " + text);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void cancellaStage(String table_name) {

		appendLog("D", "delete Stage Area (" + table_name + ")...");

		database.beginTransaction();
		database.delete(table_name, null, null);
		database.setTransactionSuccessful();
		database.endTransaction();

		appendLog("D", "delete Stage Area (" + table_name + ")...OK");
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

		cur = database
				.query(HyperthermDB.TABLE_SETTINGS,
						new String[] { operazione.substring(0,
								operazione.length() - 10) }, null, null, null,
						null, null);

		cur.moveToFirst();

		String risultato = "-1";

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			risultato = cur.getString(0);
			cur.moveToNext();
		}
		cur.close();

		return Double.parseDouble(risultato);

	}

	public double arrotondaPerEccesso(int value, int numCifreDecimali) {

		String app = String.valueOf(value);

		if (app.length() > 2) {
			app = app.substring(0, app.length() - 2) + "."
					+ app.substring(app.length() - 2, app.length());
		}

		if (app.length() == 1) {
			app = "0.0" + value;
		}

		if (app.length() == 2) {

			if (app.subSequence(0, 1).equals("-")) {
				app = "-0.0" + app.substring(1, 2);
			} else {
				app = "0." + value;
			}
		}

		double flo1 = Double.parseDouble(app);

		double temp = Math.pow(10, numCifreDecimali);

		if (numCifreDecimali == 0) {

			temp = 1;

			return (long) (Math.round(flo1 * temp) / temp);
		}

		return (double) (Math.round(flo1 * temp) / temp);

	}

	public void stampa_tracciato(byte[] buf, String severity, String inout) {

		if (inout.equals("out")) {
			appendLog(severity, "---> TX (" + calcola_check_sum(buf) + ") --->"
					+ bytesToString(buf));
		}

		if (inout.equals("in")) {
			appendLog(severity, "<--- RX (" + calcola_check_sum(buf) + ") <---"
					+ bytesToString(buf));
		}

	}

	// GESTIONE TIMEOUT
	public int get_time_out_splash() {

		cur = database.query(HyperthermDB.TABLE_SETTINGS,
				new String[] { HyperthermDB.COLUMN_TIMEOUT_SPLASH }, null,
				null, null, null, null);

		cur.moveToFirst();

		int timeout = 2000;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			timeout = cur.getInt(0);
			cur.moveToNext();
		}
		cur.close();

		return timeout;
	}

	public int get_time_out_ping() {

		cur = database.query(HyperthermDB.TABLE_SETTINGS,
				new String[] { HyperthermDB.COLUMN_TIMEOUT_PING }, null, null,
				null, null, null);

		cur.moveToFirst();

		int timeout = 1000;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			timeout = cur.getInt(0);
			cur.moveToNext();
		}
		cur.close();

		return timeout;
	}

	public int get_time_out_write() {

		cur = database.query(HyperthermDB.TABLE_SETTINGS,
				new String[] { HyperthermDB.COLUMN_TIMEOUT_WRITE }, null, null,
				null, null, null);

		cur.moveToFirst();

		int timeout = 500;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			timeout = cur.getInt(0);
			cur.moveToNext();
		}
		cur.close();

		return timeout;
	}

	public int get_time_out_simulatore() {

		cur = database.query(HyperthermDB.TABLE_SETTINGS,
				new String[] { HyperthermDB.COLUMN_TIMEOUT_SIMULATORE }, null,
				null, null, null, null);

		cur.moveToFirst();

		int timeout = 500;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			timeout = cur.getInt(0);
			cur.moveToNext();
		}
		cur.close();

		return timeout;
	}

	public int get_time_out_reset() {

		cur = database.query(HyperthermDB.TABLE_SETTINGS,
				new String[] { HyperthermDB.COLUMN_TIMEOUT_RESET }, null, null,
				null, null, null);

		cur.moveToFirst();

		int timeout = 1500;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			timeout = cur.getInt(0);
			cur.moveToNext();
		}
		cur.close();

		return timeout;
	}

	public int get_time_out_read() {

		cur = database.query(HyperthermDB.TABLE_SETTINGS,
				new String[] { HyperthermDB.COLUMN_TIMEOUT_READ }, null, null,
				null, null, null);

		cur.moveToFirst();

		int timeout = 500;

		while (cur.getCount() > 0 && !cur.isAfterLast()) {
			timeout = cur.getInt(0);
			cur.moveToNext();
		}
		cur.close();

		return timeout;
	}

	// FINE GESTIONE TIMEOUT
	protected void setColoriPiramide() {

		if (iPower != 0) {

			int BARRA_VERDE = 100 * Dir_power / iPower;

			int BARRA_ARANCIONE = 100 * Ref_power / iPower;

			appendLog("D", "VERDE=" + BARRA_VERDE + "% - ARANCIONE="
					+ BARRA_ARANCIONE + "%");

			reset_piramide();

			if (BARRA_VERDE > 0 && BARRA_VERDE <= 4) {
			}

			if (BARRA_VERDE > 4 && BARRA_VERDE <= 10) {
				zero.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 10 && BARRA_VERDE <= 14) {
				zero.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 14 && BARRA_VERDE <= 20) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 20 && BARRA_VERDE <= 24) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 24 && BARRA_VERDE <= 30) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 30 && BARRA_VERDE <= 34) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 34 && BARRA_VERDE <= 40) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 40 && BARRA_VERDE <= 44) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 44 && BARRA_VERDE <= 50) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 50 && BARRA_VERDE <= 54) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 54 && BARRA_VERDE <= 60) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
				cinquanta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 60 && BARRA_VERDE <= 64) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
				cinquanta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 64 && BARRA_VERDE <= 70) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
				cinquanta.setBackgroundColor(VERDE);
				sessanta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 70 && BARRA_VERDE <= 74) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
				cinquanta.setBackgroundColor(VERDE);
				sessanta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 74 && BARRA_VERDE <= 80) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
				cinquanta.setBackgroundColor(VERDE);
				sessanta.setBackgroundColor(VERDE);
				settanta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 80 && BARRA_VERDE <= 84) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
				cinquanta.setBackgroundColor(VERDE);
				sessanta.setBackgroundColor(VERDE);
				settanta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 84 && BARRA_VERDE <= 90) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
				cinquanta.setBackgroundColor(VERDE);
				sessanta.setBackgroundColor(VERDE);
				settanta.setBackgroundColor(VERDE);
				ottanta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 90 && BARRA_VERDE <= 94) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
				cinquanta.setBackgroundColor(VERDE);
				sessanta.setBackgroundColor(VERDE);
				settanta.setBackgroundColor(VERDE);
				ottanta.setBackgroundColor(VERDE);
			}

			if (BARRA_VERDE > 94) {
				zero.setBackgroundColor(VERDE);
				dieci.setBackgroundColor(VERDE);
				venti.setBackgroundColor(VERDE);
				trenta.setBackgroundColor(VERDE);
				quaranta.setBackgroundColor(VERDE);
				cinquanta.setBackgroundColor(VERDE);
				sessanta.setBackgroundColor(VERDE);
				settanta.setBackgroundColor(VERDE);
				ottanta.setBackgroundColor(VERDE);
				novanta.setBackgroundColor(VERDE);
			}

			if (BARRA_ARANCIONE > 0 && BARRA_ARANCIONE <= 4) {
			}

			if (BARRA_ARANCIONE > 4 && BARRA_ARANCIONE <= 10) {
				zero.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 10 && BARRA_ARANCIONE <= 14) {
				zero.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 14 && BARRA_ARANCIONE <= 20) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 20 && BARRA_ARANCIONE <= 24) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 24 && BARRA_ARANCIONE <= 30) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 30 && BARRA_ARANCIONE <= 34) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 34 && BARRA_ARANCIONE <= 40) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 40 && BARRA_ARANCIONE <= 44) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 44 && BARRA_ARANCIONE <= 50) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 50 && BARRA_ARANCIONE <= 54) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 54 && BARRA_ARANCIONE <= 60) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
				cinquanta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 60 && BARRA_ARANCIONE <= 64) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
				cinquanta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 64 && BARRA_ARANCIONE <= 70) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
				cinquanta.setBackgroundColor(ARANCIONE);
				sessanta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 70 && BARRA_ARANCIONE <= 74) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
				cinquanta.setBackgroundColor(ARANCIONE);
				sessanta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 74 && BARRA_ARANCIONE <= 80) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
				cinquanta.setBackgroundColor(ARANCIONE);
				sessanta.setBackgroundColor(ARANCIONE);
				settanta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 80 && BARRA_ARANCIONE <= 84) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
				cinquanta.setBackgroundColor(ARANCIONE);
				sessanta.setBackgroundColor(ARANCIONE);
				settanta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 84 && BARRA_ARANCIONE <= 90) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
				cinquanta.setBackgroundColor(ARANCIONE);
				sessanta.setBackgroundColor(ARANCIONE);
				settanta.setBackgroundColor(ARANCIONE);
				ottanta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 90 && BARRA_ARANCIONE <= 94) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
				cinquanta.setBackgroundColor(ARANCIONE);
				sessanta.setBackgroundColor(ARANCIONE);
				settanta.setBackgroundColor(ARANCIONE);
				ottanta.setBackgroundColor(ARANCIONE);
			}

			if (BARRA_ARANCIONE > 94) {
				zero.setBackgroundColor(ARANCIONE);
				dieci.setBackgroundColor(ARANCIONE);
				venti.setBackgroundColor(ARANCIONE);
				trenta.setBackgroundColor(ARANCIONE);
				quaranta.setBackgroundColor(ARANCIONE);
				cinquanta.setBackgroundColor(ARANCIONE);
				sessanta.setBackgroundColor(ARANCIONE);
				settanta.setBackgroundColor(ARANCIONE);
				ottanta.setBackgroundColor(ARANCIONE);
				novanta.setBackgroundColor(ARANCIONE);
			}

		}
	}

	public void reset_piramide() {

		zero.setBackgroundColor(Color.TRANSPARENT);
		dieci.setBackgroundColor(Color.TRANSPARENT);
		venti.setBackgroundColor(Color.TRANSPARENT);
		trenta.setBackgroundColor(Color.TRANSPARENT);
		quaranta.setBackgroundColor(Color.TRANSPARENT);
		cinquanta.setBackgroundColor(Color.TRANSPARENT);
		sessanta.setBackgroundColor(Color.TRANSPARENT);
		settanta.setBackgroundColor(Color.TRANSPARENT);
		ottanta.setBackgroundColor(Color.TRANSPARENT);
		novanta.setBackgroundColor(Color.TRANSPARENT);

		zero.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));
		dieci.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));
		venti.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));
		trenta.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));
		quaranta.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));
		cinquanta.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));
		sessanta.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));
		settanta.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));
		ottanta.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));
		novanta.setBackground(activity.getResources().getDrawable(
				R.drawable.cell_shape_bottom_white));

	}
}
