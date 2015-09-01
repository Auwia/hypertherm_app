package it.app.hypertherm;

import java.util.Arrays;

import android.os.SystemClock;

public class PC_TO_CY {
	int CheckSum; // (uint16) Da implementare, per ora posta a 0x5A fissa
	byte Ver; // (uint8) Versione della struttura (per estensione e verifica di
				// compatibilità)
	byte TimeStamp; // (uint8) Per la verifica di controllo di ricezione del
					// pacchetto inviato
	public long Msk; // (uint32) Maschera dei dati validi (i soli da processare)
	long In_Output; // (uint32) Stato delle 32 uscite disponibili
	public int Cmd; // (uint16) Comando eventualmente inviato (0xFFFF <=>
					// nessuno)
	int iTime; // (uint16) Tempo impostato del trattamento (1..30 minuti in
				// secondi)
	int iD_temp; // (int16) Delta termico impostato (-100..+500 centesimi di °C)
	int iH2o_temp; // (int16) Temperatura dell'H2o impostata (3500..4200
					// centesimi di °C)
	int iColdHp_temp; // (int16) Temperatura impostata del manipolo freddo
						// (centesimi di °C)
	int Gain_D_temp; // (int16) Gain Delta Temp (adimensionale)
	int Offset_D_temp; // (int16) Offset Delta Temp (centesimi di grado)
	int Gain_H2o_temp; // (int16) Gain tH2o Boiler Temp (adimensionale)
	int Offset_H2o_temp; // (int16) Offset tH2o Boiler Temp (centesimi di grado)
	int Gain_Cold_temp; // (int16) Gain Cold Temp (adimensionale)
	int Offset_Cold_temp; // (int16) Offset Cold Temp (centesimi di grado)
	int Gain_Boil_temp; // (int16) Gain Boiler Temp (adimensionale)
	int Offset_Boil_temp; // (int16) Offset Boiler Temp (centesimi di grado)
	int iPower; // (int16) Potenza impostata del trattamento (0..10000 centesimi
				// di Watt)
	byte[] Buf; // (uint8) Buffer a disposizione per trasmettere dati da Pc a Cy
	public byte[] PSoCData; // (uint8) Buffer dei dati scambiati

	public static final byte HOSTUSB_VER = 14;
	public static final int PACKET_SIZE = 64;
	private static final int PC_CY_BUFCNT = 24;

	public static final int PCCMD_ECO_DATA = 0x8000, PCCMD_WR_DATA = 0x0101,
			PCCMD_BOOTLOAD = 0xEFE0, PCCMD_STOP = 0x0200, PCCMD_PAUSE = 0x0201,
			PCCMD_PLAY = 0x0202, PCCMD_INIT = 0x0220;

	private static final int CYOUT_BolusUp = 0x00000002,
			CYOUT_BolusDown = 0x00000004;

	private static final int PCMSK_OUTPUT = 0x1;

	public PC_TO_CY() {
		CheckSum = 0x5A;
		Ver = HOSTUSB_VER;
		Msk = In_Output = 0;
		TimeStamp = 0;
		Cmd = iTime = iD_temp = iH2o_temp = iColdHp_temp = Gain_D_temp = Offset_D_temp = Gain_H2o_temp = Offset_H2o_temp = Gain_Cold_temp = Offset_Cold_temp = Gain_Boil_temp = Offset_Boil_temp = iPower = 0;
		Buf = new byte[PC_CY_BUFCNT];
		PSoCData = new byte[PACKET_SIZE];
	}

	/*
	 * Restituisce un intero XXY ottenuto dalla lettura di una stringa con il
	 * formato XX.Y
	 */
	private int getStrToInt_x10(String str) {
		try {
			Float f = Float.parseFloat(str) * 10;
			int retCode = f.intValue();
			return retCode;
		} catch (Exception e) {
			return 0;
		}
	}

	/*
	 * Restituisce un intero XXY ottenuto dalla lettura di una stringa con il
	 * formato XX.Y
	 */
	private int getStrToInt_x100(String str) {
		try {
			Float f = Float.parseFloat(str) * 100;
			int retCode = f.intValue();
			return retCode;
		} catch (Exception e) {
			return 0;
		}
	}

	// Assegna i parametri del trattamento, dati gli Id di risorsa delle
	// TextView che li contengono
	public void setTreatParms(String TvTime, String TvPower, String TvH2otemp,
			String TvDtemp) {

		iTime = Integer.parseInt(TvTime);

		// Conversione della potenza
		iPower = getStrToInt_x100(TvPower);

		// Conversione della temperatura acqua
		iH2o_temp = getStrToInt_x100(TvH2otemp);

		// Conversione del delta termico
		iD_temp = getStrToInt_x100(TvDtemp);
	}

	// Assegna i parametri del trattamento default
	public void setDefaultTreatParms() {
		iTime = 18;
		iPower = 78;
		iH2o_temp = 39;
		iD_temp = 1;
	}

	// Azzera il buffer dei dati da trasmettere
	private void clrPSoCData() {
		Arrays.fill(PSoCData, (byte) 0);
	}

	// --------------------------------------------------------------------
	// Assegna un unsigned a 16 bit alla posizione pos del buffer PSoCData
	private void uint16_To_Buf(int val, int pos) {
		PSoCData[pos++] = (byte) (val & 0xFF);
		PSoCData[pos] = (byte) ((val & 0xFF00) >> 8);
	}

	// --------------------------------------------------------------------
	// Assegna un unsigned a 32 bit alla posizione pos del buffer PSoCData
	private void uint32_To_Buf(long val, int pos) {
		PSoCData[pos++] = (byte) (val & 0xFF);
		PSoCData[pos++] = (byte) ((val & 0xFF00) >> 8);
		PSoCData[pos++] = (byte) ((val & 0xFF0000) >> 16);
		PSoCData[pos] = (byte) ((val & 0xFF000000) >> 24);
	}

	// ------------------------------------
	// Assegna tutta la struttura PSoCData
	public void setPSoCData() {

		TimeStamp = (byte) ((SystemClock.elapsedRealtime()) & 0x00FF);

		Cmd |= PCCMD_ECO_DATA; // Sollecita la ritrasmissione indietro
								// del pacchetto
		uint16_To_Buf(CheckSum & 0xFFFF, 0); // Assegna la checksum del
												// pacchetto
		PSoCData[2] = Ver; // Versione della struttura (per estensione e
							// verifica di compatibilità)

		PSoCData[3] = TimeStamp;

		uint32_To_Buf(Msk & 0xFFFFFFFF, 4); // Assegna la maschera dei dati
											// validi
		// (i soli da processare)
		uint32_To_Buf(In_Output & 0xFFFFFFFF, 8); // Stato delle 32 uscite
													// disponibili
		uint16_To_Buf(Cmd & 0xFFFF, 12); // Comando eventualmente inviato
											// (0xFFFF <=> nessuno)
		uint16_To_Buf(iTime & 0xFFFF, 14); // Assegna il Tempo impostato del
											// trattamento (1..30 minuti)
		uint16_To_Buf(iD_temp & 0xFFFF, 16); // Assegna il Delta termico
												// impostato (-10..+50
												// decimi di °C)
		uint16_To_Buf(iH2o_temp & 0xFFFF, 18); // Temperatura dell'H2o impostata
												// (350..420 decimi di °C)
		uint16_To_Buf(iColdHp_temp & 0xFFFF, 20); // Temperatura impostata del
													// manipolo freddo
		uint16_To_Buf(iPower & 0xFFFF, 22);

		uint16_To_Buf(Gain_D_temp & 0xFFFF, 24); // Temperatura impostata del
													// manipolo freddo
		uint16_To_Buf(Offset_D_temp & 0xFFFF, 26); // Temperatura impostata del
													// manipolo freddo
		uint16_To_Buf(Gain_H2o_temp & 0xFFFF, 28); // Temperatura impostata del
													// manipolo freddo
		uint16_To_Buf(Offset_H2o_temp & 0xFFFF, 30); // Temperatura impostata
														// del manipolo freddo
		uint16_To_Buf(Gain_Cold_temp & 0xFFFF, 32); // Temperatura impostata del
													// manipolo freddo
		uint16_To_Buf(Offset_Cold_temp & 0xFFFF, 34); // Temperatura impostata
														// del manipolo freddo
		uint16_To_Buf(Gain_Boil_temp & 0xFFFF, 36); // Temperatura impostata del
													// manipolo freddo
		uint16_To_Buf(Offset_Boil_temp & 0xFFFF, 38); // Temperatura impostata
														// del manipolo freddo

		// Trasferisce il contenuto del buffer
		{
			byte k, h;
			for (k = PC_CY_BUFCNT - 1, h = PACKET_SIZE - 1; k >= 0;)
				PSoCData[h--] = Buf[k--];
		}

	}

	// Setta BolusUp
	public void setBolusUp() {
		In_Output |= CYOUT_BolusUp;
		Msk |= PCMSK_OUTPUT;
	}

	// Cancella BolusUp
	public void clrBolusUp() {
		In_Output &= ~CYOUT_BolusUp;
	}

	// Testa se BolusUp e' attivo
	public boolean isBolusUp() {
		if ((In_Output & CYOUT_BolusUp) != 0)
			return true;
		else
			return false;
	}

	// Setta BolusDown
	public void setBolusDown() {
		In_Output |= CYOUT_BolusDown;
		Msk |= PCMSK_OUTPUT;
	}

	// Cancella BolusDown
	public void clrBolusDown() {
		In_Output &= ~CYOUT_BolusDown;
	}

	// Testa se BolusDown e' attivo
	public boolean isBolusDown() {
		if ((In_Output & CYOUT_BolusDown) != 0)
			return true;
		else
			return false;
	}

	// Cancella entrambi i Bolus
	public void clrBothBolus() {
		In_Output &= ~(CYOUT_BolusUp | CYOUT_BolusDown);
	}

	// Testa se uno dei due Bolus e' attivo
	public boolean isAnyBolus() {
		if ((In_Output & (CYOUT_BolusUp | CYOUT_BolusDown)) != 0)
			return true;
		else
			return false;
	}
}