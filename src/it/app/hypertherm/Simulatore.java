package it.app.hypertherm;

import it.app.hypertherm.activity.WorkActivity;
import it.app.hypertherm.util.CountDownTimer;
import it.app.hypertherm.util.Utility;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;

public class Simulatore implements Runnable {

	private static Utility utility;
	private static Tracciato tracciato;
	private static BlockingQueue<byte[]> queue;
	private static boolean START = false;
	public static boolean INVIA = false;
	private static int TIME, POTENZA_IN, POTENZA_OUT, POTENZA_DIR, DELTAT,
			WATER, CMD;
	private static CountDownTimer waitTimer = null;
	private static Activity activity;

	public Simulatore(BlockingQueue<byte[]> queue, Utility utility,
			Activity activity) {

		this.utility = utility;

		this.queue = queue;

		this.activity = activity;

		tracciato = new Tracciato();

		TIME = 1800;

	}

	public void run() {

		while (WorkActivity.COMMUNICATION_READY) {

			if (INVIA) {

				if (START) {

					if (TIME == 0) {
						tracciato.setComando(3);
						START = false;
					}

				}

				tracciato.setTimerIn(TIME);
				tracciato.setPowerOut(POTENZA_OUT);
				tracciato.setPowerIn(POTENZA_IN);
				tracciato.setDirPower(POTENZA_DIR);
				tracciato.setDeltaTIn(DELTAT);
				tracciato.setWaterIn(WATER);
				tracciato.setComando(CMD);

				tracciato.setCheckSum(utility.calcola_check_sum(tracciato
						.setBuf()));

				queue.add(tracciato.setBuf());

				INVIA = false;

			}

		}

	}

	public static void simulate(byte[] buffer) {

		int CheckSum = ((int) buffer[0]) & 0xFF;
		CheckSum |= (((int) buffer[1]) & 0xFF) << 8;
		int Ver = ((int) buffer[2]) & 0xFF;
		int TimStmp = ((int) buffer[3]) & 0xFF;

		byte[] msk = new byte[4];
		msk[0] = buffer[4];
		msk[1] = buffer[5];
		msk[2] = buffer[6];
		msk[3] = buffer[7];
		String msk_binary = utility.toBinary(msk);
		int[] msk1 = new int[4];
		msk1[0] = buffer[4] & 0xFF;
		msk1[1] = buffer[5] & 0xFF;
		msk1[2] = buffer[6] & 0xFF;
		msk1[3] = buffer[7] & 0xFF;

		byte[] In_Output_buffer = new byte[4];
		In_Output_buffer[0] = buffer[8];
		In_Output_buffer[1] = buffer[9];
		In_Output_buffer[2] = buffer[10];
		In_Output_buffer[3] = buffer[11];

		String In_Output = utility.toBinary(In_Output_buffer);

		int Cmd = ((int) buffer[12]) & 0xFF;
		Cmd |= (((int) buffer[13]) & 0xFF) << 8;
		int iTime = ((int) buffer[14]) & 0xFF;
		iTime |= (((int) buffer[15]) & 0xFF) << 8;
		int iD_temp = ((int) buffer[16]) & 0xFF;
		iD_temp |= (((int) buffer[17]) & 0xFF) << 8;
		int iH2o_temp = ((int) buffer[18]) & 0xFF;
		iH2o_temp |= (((int) buffer[19]) & 0xFF) << 8;
		int iColdHp_buffer = ((int) buffer[20]) & 0xFF;
		iColdHp_buffer |= (((int) buffer[21]) & 0xFF) << 8;
		int iPower = ((int) buffer[22]) & 0xFF;
		iPower |= (((int) buffer[23]) & 0xFF) << 8;

		int Gain_D_buffer = ((int) buffer[24]) & 0xFF;
		Gain_D_buffer |= (((int) buffer[25]) & 0xFF) << 8;
		int Offset_D_buffer = ((int) buffer[26]) & 0xFF;
		Offset_D_buffer |= (((int) buffer[27]) & 0xFF) << 8;
		int Gain_H2o_buffer = ((int) buffer[28]) & 0xFF;
		Gain_H2o_buffer |= (((int) buffer[29]) & 0xFF) << 8;
		int Offset_H2o_buffer = ((int) buffer[30]) & 0xFF;
		Offset_H2o_buffer |= (((int) buffer[31]) & 0xFF) << 8;
		int Gain_Cold_buffer = ((int) buffer[32]) & 0xFF;
		Gain_Cold_buffer |= (((int) buffer[33]) & 0xFF) << 8;
		int Offset_Cold_buffer = ((int) buffer[34]) & 0xFF;
		Offset_Cold_buffer |= (((int) buffer[35]) & 0xFF) << 8;
		int Gain_Boil_buffer = ((int) buffer[36]) & 0xFF;
		Gain_Boil_buffer |= (((int) buffer[37]) & 0xFF) << 8;
		int Offset_Boil_buffer = ((int) buffer[38]) & 0xFF;
		Offset_Boil_buffer |= (((int) buffer[39]) & 0xFF) << 8;
		int Req_power = ((int) buffer[40]) & 0xFF;
		Req_power |= (((int) buffer[41]) & 0xFF) << 8;

		int Dir_power = ((int) buffer[42]) & 0xFF;
		Dir_power |= (((int) buffer[43]) & 0xFF) << 8;
		int Ref_power = ((int) buffer[44]) & 0xFF;
		Ref_power |= (((int) buffer[45]) & 0xFF) << 8;
		int D_buffer = ((int) buffer[46]) & 0xFF;
		D_buffer |= (((int) buffer[47]) & 0xFF) << 8;
		int H2o_buffer = ((int) buffer[48]) & 0xFF;
		H2o_buffer |= (((int) buffer[49]) & 0xFF) << 8;

		int ColdHp_buffer = ((int) buffer[50]) & 0xFF;
		ColdHp_buffer |= (((int) buffer[51]) & 0xFF) << 8;
		int Boil_buffer = ((int) buffer[52]) & 0xFF;
		Boil_buffer |= (((int) buffer[53]) & 0xFF) << 8;

		int runningTime = ((int) buffer[54]) & 0xFF;
		runningTime |= (((int) buffer[55]) & 0xFF) << 8;

		int pwmRes = ((int) buffer[56]) & 0xFF;
		int pwmPomp = ((int) buffer[57]) & 0xFF;
		int pwmFan = ((int) buffer[58]) & 0xFF;

		int[] last = new int[5];
		last[0] = buffer[59] & 0xFF;
		last[1] = buffer[60] & 0xFF;
		last[2] = buffer[61] & 0xFF;
		last[3] = buffer[62] & 0xFF;
		last[4] = buffer[63] & 0xFF;

		if (utility.calcola_check_sum(buffer) == CheckSum) {

			POTENZA_IN = simulatePotenza(iPower);
			POTENZA_OUT = iPower;
			POTENZA_DIR = simulatePotenza(iPower);
			DELTAT = simulateDeltaT(iD_temp);
			WATER = simulateWater(iH2o_temp);

			if (CMD == 3) {
				TIME = iTime;
				POTENZA_IN = simulatePotenza(0);
				POTENZA_DIR = simulatePotenza(0);

			}

			if (CMD == 2) {
				POTENZA_IN = simulatePotenza(0);
				POTENZA_DIR = simulatePotenza(0);
			}

			switch (Cmd) {

			case 256: // START

				if (!START) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							waitTimer = new CountDownTimer(TIME * 1000, 1000) {

								public void onTick(long millisUntilFinished) {

									TIME -= 1;

								}

								public void onFinish() {

								}
							}.start();
						}
					});
				}

				START = true;

				CMD = 1;

				break;

			case 512: // PAUSE
				START = false;
				if (waitTimer != null) {
					waitTimer.cancel();
				}
				CMD = 2;
				break;

			case 768: // STOP
				START = false;
				TIME = iTime;
				if (waitTimer != null) {
					waitTimer.cancel();
				}
				CMD = 3;
				break;

			case 1024: // BOOL-UP
				CMD = 4;
				break;

			case 1536: // BOOL-UP-STOP
				CMD = 6;
				break;

			case 1280: // BOOL-DOWN
				CMD = 5;
				break;

			case 1100: // RESET
				CMD = 11;
				break;

			}

		}

	}

	private static int simulateWater(int iH2o_temp) {

		Random ran = new Random();

		int max = 50;
		int min = 0;

		return iH2o_temp - (ran.nextInt((max - min) + 1) + min);

	}

	private static int simulateDeltaT(int iD_temp) {

		Random ran = new Random();

		int max = 10;
		int min = -10;

		return iD_temp - (ran.nextInt((max - min) + 1) + min);

	}

	private static int simulatePotenza(int iPower) {

		Random ran = new Random();

		int max = 100;
		int min = 1;

		int risultato = iPower - (ran.nextInt((max - min) + 1) + min);

		if (risultato < 0) {
			risultato = 0;
		}

		return risultato;
	}

}
