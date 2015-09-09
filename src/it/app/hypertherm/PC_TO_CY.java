package it.app.hypertherm;

import it.app.hypertherm.util.Utility;

import java.util.Arrays;

public class PC_TO_CY {
	public int CheckSum;
	public int Dir_power;
	public int H2o_temp;
	public int D_temp;
	byte Ver;
	byte TimeStamp;
	public long Msk;
	long In_Output;
	public int Cmd;
	public int iTime;
	public int iD_temp;
	public int iH2o_temp;
	int iColdHp_temp;
	public int iPower;
	int Gain_D_temp;
	int Offset_D_temp;
	int Gain_H2o_temp;
	int Offset_H2o_temp;
	int Gain_Cold_temp;
	int Offset_Cold_temp;
	int Gain_Boil_temp;
	int Offset_Boil_temp;

	public int runningTime;
	byte[] Buf;
	public byte[] PSoCData;

	public static final byte HOSTUSB_VER = 0;
	public static final int PACKET_SIZE = 64;
	private static final int PC_CY_BUFCNT = 24;

	public static final int PCCMD_ECO_DATA = 0x8000, PCCMD_WR_DATA = 0x0101,
			PCCMD_BOOTLOAD = 0xEFE0, PCCMD_STOP = 0x0200, PCCMD_PAUSE = 0x0201,
			PCCMD_PLAY = 0x0202, PCCMD_INIT = 0x0220;

	private static final int CYOUT_BolusUp = 0x00000002,
			CYOUT_BolusDown = 0x00000004;

	private static final int PCMSK_OUTPUT = 0x1;

	private Utility utility = new Utility();

	public PC_TO_CY() {
		Ver = HOSTUSB_VER;
		Msk = In_Output = 0;
		TimeStamp = 0;
		Cmd = iTime = iD_temp = iH2o_temp = iColdHp_temp = Gain_D_temp = Offset_D_temp = Gain_H2o_temp = Offset_H2o_temp = Gain_Cold_temp = Offset_Cold_temp = Gain_Boil_temp = Offset_Boil_temp = iPower = 0;
		Buf = new byte[PC_CY_BUFCNT];
		PSoCData = new byte[PACKET_SIZE];
	}

	private int getStrToInt_x10(String str) {
		try {
			Float f = Float.parseFloat(str) * 10;
			int retCode = f.intValue();
			return retCode;
		} catch (Exception e) {
			return 0;
		}
	}

	private int getStrToInt_x100(String str) {
		try {
			Float f = Float.parseFloat(str) * 100;
			int retCode = f.intValue();
			return retCode;
		} catch (Exception e) {
			return 0;
		}
	}

	public void setTreatParms(String TvTime, String TvPower, String TvH2otemp,
			String TvDtemp) {

		runningTime = Integer.parseInt(TvTime);

		Dir_power = getStrToInt_x100(TvPower);

		H2o_temp = getStrToInt_x100(TvH2otemp);

		D_temp = getStrToInt_x100(TvDtemp);
	}

	public void setDefaultTreatParms() {
		iTime = utility.getTime("DEFAULT");
		iPower = utility.getAntenna("DEFAULT");
		iH2o_temp = (int) utility.getWaterTemperature("DEFAULT") * 10;
		iD_temp = utility.getTime("DEFAULT");
	}

	public void setCheckSum(int val) {
		CheckSum = val;
	}

	private void clrPSoCData() {
		Arrays.fill(PSoCData, (byte) 0);
	}

	private void uint16_To_Buf(int val, int pos) {
		PSoCData[pos++] = (byte) (val & 0xFF);
		PSoCData[pos] = (byte) ((val & 0xFF00) >> 8);
	}

	private void uint32_To_Buf(long val, int pos) {
		PSoCData[pos++] = (byte) (val & 0xFF);
		PSoCData[pos++] = (byte) ((val & 0xFF00) >> 8);
		PSoCData[pos++] = (byte) ((val & 0xFF0000) >> 16);
		PSoCData[pos] = (byte) ((val & 0xFF000000) >> 24);
	}

	public void setPSoCData() {

		TimeStamp = (byte) ((0) & 0x00FF);

		uint16_To_Buf(CheckSum & 0xFFFF, 0);
		PSoCData[2] = Ver;
		PSoCData[3] = TimeStamp;

		// uint32_To_Buf(Msk & 0xFFFFFFFF, 4);
		uint32_To_Buf(In_Output & 0xFFFFFFFF, 8);
		uint16_To_Buf(Cmd & 0xFFFF, 13);
		uint16_To_Buf(iTime & 0xFFFF, 14);
		uint16_To_Buf(iD_temp & 0xFFFF, 16);
		uint16_To_Buf(iH2o_temp & 0xFFFF, 18);
		uint16_To_Buf(iColdHp_temp & 0xFFFF, 20);
		uint16_To_Buf(iPower & 0xFFFF, 22);

		uint16_To_Buf(Gain_D_temp & 0xFFFF, 24);
		uint16_To_Buf(Offset_D_temp & 0xFFFF, 26);
		uint16_To_Buf(Gain_H2o_temp & 0xFFFF, 28);
		uint16_To_Buf(Offset_H2o_temp & 0xFFFF, 30);
		uint16_To_Buf(Gain_Cold_temp & 0xFFFF, 32);
		uint16_To_Buf(Offset_Cold_temp & 0xFFFF, 34);
		uint16_To_Buf(Gain_Boil_temp & 0xFFFF, 36);
		uint16_To_Buf(Offset_Boil_temp & 0xFFFF, 38);
		uint16_To_Buf(Dir_power & 0xFFFF, 42);
		uint16_To_Buf(D_temp & 0xFFFF, 46	);
		uint16_To_Buf(H2o_temp & 0xFFFF, 48);
		uint16_To_Buf(runningTime & 0xFFFF, 54);

		{
			byte k, h;
			for (k = PC_CY_BUFCNT - 1, h = PACKET_SIZE - 1; k >= 0;)
				PSoCData[h--] = Buf[k--];
		}

	}

	public void setBolusUp() {
		In_Output |= CYOUT_BolusUp;
		Msk |= PCMSK_OUTPUT;
	}

	public void clrBolusUp() {
		In_Output &= ~CYOUT_BolusUp;
	}

	public boolean isBolusUp() {
		if ((In_Output & CYOUT_BolusUp) != 0)
			return true;
		else
			return false;
	}

	public void setBolusDown() {
		In_Output |= CYOUT_BolusDown;
		Msk |= PCMSK_OUTPUT;
	}

	public void clrBolusDown() {
		In_Output &= ~CYOUT_BolusDown;
	}

	public boolean isBolusDown() {
		if ((In_Output & CYOUT_BolusDown) != 0)
			return true;
		else
			return false;
	}

	public void clrBothBolus() {
		In_Output &= ~(CYOUT_BolusUp | CYOUT_BolusDown);
	}

	public boolean isAnyBolus() {
		if ((In_Output & (CYOUT_BolusUp | CYOUT_BolusDown)) != 0)
			return true;
		else
			return false;
	}
}