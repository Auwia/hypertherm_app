package it.app.hypertherm;

import it.app.hypertherm.util.Utility;

import java.util.Arrays;

public class Tracciato {

	// UTILIZZATI
	private int check_sum;
	private int maschera;
	private int comando;
	private int water_in;
	private int water_out;
	private int deltat_in;
	private int deltat_out;
	private int time_in;
	private int time_out;
	private int power_in;
	private int power_out;
	private int Dir_power;
	private int In_Output;

	// NON UTILIZZATI
	private byte Ver;
	private byte TimeStamp;
	private int iColdHp_temp;
	private int Gain_deltat_out;
	private int Offset_deltat_out;
	private int Gain_water_out;
	private int Offset_water_out;
	private int Gain_Cold_temp;
	private int Offset_Cold_temp;
	private int Gain_Boil_temp;
	private int Offset_Boil_temp;
	private int Boil_temp;
	private int ColdHp_temp;
	private int req_power;
	private int pwmRes;
	private int pwmPomp;
	private int pwmFan;

	public static final int PACKET_SIZE = 64;
	private byte[] buf;

	private Utility utility = new Utility();

	public Tracciato() {

		buf = new byte[PACKET_SIZE];

		clearTracciato();

		Ver = 0;
		In_Output = 0;
		TimeStamp = 0;
		comando = time_in = deltat_in = water_in = iColdHp_temp = Gain_deltat_out = Offset_deltat_out = Gain_water_out = Offset_water_out = Gain_Cold_temp = Offset_Cold_temp = Gain_Boil_temp = Offset_Boil_temp = power_in = power_out = Dir_power = maschera = Boil_temp = ColdHp_temp = req_power = pwmFan = pwmPomp = pwmRes = 0;

	}

	public byte[] setBuf() {

		uint16_To_Buf(check_sum & 0xFFFF, 0);
		uint8_To_Buf(Ver, 2);
		uint8_To_Buf(TimeStamp, 3);
		uint32_To_Buf(maschera & 0xFFFFFFFF, 4);
		uint32_To_Buf(In_Output & 0xFFFFFFFF, 8);
		uint16_To_Buf(comando & 0xFFFF, 13);
		uint16_To_Buf(time_out & 0xFFFF, 14);
		uint16_To_Buf(deltat_out & 0xFFFF, 16);
		uint16_To_Buf(water_out & 0xFFFF, 18);
		uint16_To_Buf(iColdHp_temp & 0xFFFF, 20);
		uint16_To_Buf(power_out & 0xFFFF, 22);
		uint16_To_Buf(Gain_deltat_out & 0xFFFF, 24);
		uint16_To_Buf(Offset_deltat_out & 0xFFFF, 26);
		uint16_To_Buf(Gain_water_out & 0xFFFF, 28);
		uint16_To_Buf(Offset_water_out & 0xFFFF, 30);
		uint16_To_Buf(Gain_Cold_temp & 0xFFFF, 32);
		uint16_To_Buf(Offset_Cold_temp & 0xFFFF, 34);
		uint16_To_Buf(Gain_Boil_temp & 0xFFFF, 36);
		uint16_To_Buf(Offset_Boil_temp & 0xFFFF, 38);
		uint16_To_Buf(req_power & 0xFFFF, 40);
		uint16_To_Buf(Dir_power & 0xFFFF, 42);
		uint16_To_Buf(power_in & 0xFFFF, 44);
		uint16_To_Buf(deltat_in & 0xFFFF, 46);
		uint16_To_Buf(water_in & 0xFFFF, 48);
		uint16_To_Buf(ColdHp_temp & 0xFFFF, 50);
		uint16_To_Buf(Boil_temp & 0xFFFF, 52);
		uint16_To_Buf(time_in & 0xFFFF, 54);
		uint8_To_Buf(pwmRes, 56);
		uint8_To_Buf(pwmPomp, 57);
		uint8_To_Buf(pwmFan, 58);
		uint8_To_Buf(11, 59);
		uint8_To_Buf(12, 60);
		uint8_To_Buf(13, 61);
		uint8_To_Buf(14, 62);
		uint8_To_Buf(15, 63);

		return buf;
	}

	public void setTreatParms(int TvTime, int TvPower, int TvH2otemp,
			int TvDtemp) {

		time_out = TvTime;
		power_out = TvPower;
		water_out = TvH2otemp;
		deltat_out = TvDtemp;
	}

	public void setDefaultTreatParms() {

		time_in = utility.getTime("DEFAULT");
		power_in = utility.getAntenna("DEFAULT");
		water_in = (int) utility.getWaterTemperature("DEFAULT") * 10;
		deltat_in = utility.getTime("DEFAULT");
	}

	public void setCheckSum(int check_sum) {
		this.check_sum = check_sum;
	}

	public int getCheckSum() {
		return check_sum;
	}

	public void setComando(int comando) {
		this.comando = comando;
	}

	public int getComando() {
		return comando;
	}

	public void setMaschera(int maschera) {
		this.maschera = maschera;
	}

	public void setInOutput(int In_Output) {
		this.In_Output = In_Output;
	}

	public int getMaschera() {
		return maschera;
	}

	public int getInOutput() {
		return In_Output;
	}

	public void setDeltaTIn(int deltat_in) {
		this.deltat_in = deltat_in;
	}

	public int getDeltaTIn() {
		return deltat_in;
	}

	public void setDeltaTOut(int deltat_out) {
		this.deltat_out = deltat_out;
	}

	public int getDeltaTOut() {
		return deltat_out;
	}

	public void setWaterIn(int water_in) {
		this.water_in = water_in;
	}

	public int getWaterIn() {
		return water_in;
	}

	public void setWaterOut(int water_out) {
		this.water_out = water_out;
	}

	public int getWaterOut() {
		return water_out;
	}

	public void setTimerIn(int time_in) {
		this.time_in = time_in;
	}

	public int getTimerIn() {
		return time_in;
	}

	public void setTimerOut(int time_out) {
		this.time_out = time_out;
	}

	public int getTimerOut() {
		return time_out;
	}

	public void setPowerIn(int power_in) {
		this.power_in = power_in;
	}

	public int getPowerIn() {
		return power_in;
	}

	public void setPowerOut(int power_out) {
		this.power_out = power_out;
	}

	public int getPowerOut() {
		return power_out;
	}

	public void setDirPower(int Dir_power) {
		this.Dir_power = Dir_power;
	}

	public int getDirPower() {
		return Dir_power;
	}

	public void clearTracciato() {
		Arrays.fill(buf, (byte) 0);
	}

	private void uint8_To_Buf(int val, int pos) {
		buf[pos] = (byte) (val & 0xFF);
	}

	private void uint16_To_Buf(int val, int pos) {
		buf[pos++] = (byte) (val & 0xFF);
		buf[pos] = (byte) ((val & 0xFF00) >> 8);
	}

	private void uint32_To_Buf(long val, int pos) {
		buf[pos++] = (byte) (val & 0xFF);
		buf[pos++] = (byte) ((val & 0xFF00) >> 8);
		buf[pos++] = (byte) ((val & 0xFF0000) >> 16);
		buf[pos] = (byte) ((val & 0xFF000000) >> 24);
	}

	public byte[] getBuf() {
		return buf;
	}

}