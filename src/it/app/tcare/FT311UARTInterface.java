//User must modify the below package with their package name
package it.app.tcare;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.State;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

/****************************** FT311 GPIO interface class ******************************************/
public class FT311UARTInterface extends Activity {

	private static final int BaudRate = 9600;
	private static final byte dataBits = 1;
	private static final byte stopBits = 8;
	private static final byte parity = 0;
	private static final byte flowControl = 0;

	private static final String ACTION_USB_PERMISSION = "it.app.tcare.USB_PERMISSION";
	public UsbManager usbmanager;
	public UsbAccessory usbaccessory;
	public PendingIntent mPermissionIntent;
	public ParcelFileDescriptor filedescriptor = null;
	public FileInputStream inputstream = null;
	public FileOutputStream outputstream = null;
	public boolean mPermissionRequestPending = false;

	public static read_thread readThread;
	public static write_thread writeThread;

	private byte[] usbdata, writeusbdata;
	private byte status;

	private int readcount, exit;

	public boolean datareceived = false, accessory_attached = false;
	public static boolean READ_ENABLE = false;

	public Activity global_context;

	public static String ManufacturerString = "mManufacturer=FTDI";
	public static String ModelString1 = "mModel=FTDIUARTDemo";
	public static String ModelString2 = "mModel=Android Accessory FT312D";
	public static String VersionString = "mVersion=1.0";

	private Utility utility;
	private StringBuffer readSB = new StringBuffer();

	public SharedPreferences preferences;

	/* constructor */
	public FT311UARTInterface(Activity context,
			SharedPreferences sharePrefSettings) throws InterruptedException {
		super();
		global_context = context;

		preferences = sharePrefSettings;

		utility = new Utility(global_context);

		usbdata = new byte[1024];
		writeusbdata = new byte[256];

		/*********************** USB handling ******************************************/

		usbmanager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		context.registerReceiver(mUsbReceiver, filter);

		inputstream = null;
		outputstream = null;

	}

	public void SetConfig() {

		/* prepare the baud rate buffer */
		writeusbdata[0] = (byte) BaudRate;
		writeusbdata[1] = (byte) (BaudRate >> 8);
		writeusbdata[2] = (byte) (BaudRate >> 16);
		writeusbdata[3] = (byte) (BaudRate >> 24);

		/* data bits */
		writeusbdata[4] = dataBits;
		/* stop bits */
		writeusbdata[5] = stopBits;
		/* parity */
		writeusbdata[6] = parity;
		/* flow control */
		writeusbdata[7] = flowControl;

		/* send the UART configuration packet */
		SendPacket((int) 8);
	}

	/* write data */
	public byte SendData(int numBytes, byte[] buffer) {
		status = 0x00; /* success by default */
		/*
		 * if num bytes are more than maximum limit
		 */
		if (numBytes < 1) {
			/* return the status with the error in the command */
			Log.e("TCARE", "SendData: numero di byte nullo o negativo");
			return status;
		}

		/* check for maximum limit */
		if (numBytes > 256) {
			numBytes = 256;
			Log.e("TCARE", "SendData: numero di byte superiore a 256byte");
		}

		/* prepare the packet to be sent */
		for (int count = 0; count < numBytes; count++) {
			writeusbdata[count] = buffer[count];

		}

		if (numBytes != 64) {
			SendPacket(numBytes);
		} else {
			byte temp = writeusbdata[63];
			SendPacket(63);
			writeusbdata[0] = temp;
			SendPacket(1);
		}

		return status;
	}

	/* method to send on USB */
	private void SendPacket(int numBytes) {

		try {
			if (outputstream != null) {
				outputstream.write(writeusbdata, 0, numBytes);
			} else {
				READ_ENABLE = false;
				Log.i("TCARE", "SendPacket: stream di scrittura chiuso");
			}
		} catch (IOException e) {

			Log.d("TCARE", "SendPacket: HO PERSO LA SCHEDA");
			READ_ENABLE = false;

		}
	}

	/* resume accessory */
	public int ResumeAccessory(boolean bConfiged) {
		// Intent intent = getIntent();
		if (inputstream != null && outputstream != null) {
			return 1;
		}

		UsbAccessory[] accessories = usbmanager.getAccessoryList();
		if (accessories != null) {
			// Toast.makeText(global_context, "Accessory Attached",
			// Toast.LENGTH_SHORT).show();
		} else {
			// return 2 for accessory detached case
			// Log.e(">>@@","ResumeAccessory RETURN 2 (accessories == null)");
			accessory_attached = false;
			return 2;
		}

		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (-1 == accessory.toString().indexOf(ManufacturerString)) {
				Toast.makeText(global_context, "Manufacturer is not matched!",
						Toast.LENGTH_SHORT).show();
				return 1;
			}

			if (-1 == accessory.toString().indexOf(ModelString1)
					&& -1 == accessory.toString().indexOf(ModelString2)) {
				Toast.makeText(global_context, "Model is not matched!",
						Toast.LENGTH_SHORT).show();
				return 1;
			}

			if (-1 == accessory.toString().indexOf(VersionString)) {
				Toast.makeText(global_context, "Version is not matched!",
						Toast.LENGTH_SHORT).show();
				return 1;
			}

			// Toast.makeText(global_context,
			// "Manufacturer, Model & Version are matched!",
			// Toast.LENGTH_SHORT).show();
			accessory_attached = true;

			if (usbmanager.hasPermission(accessory)) {
				OpenAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						Toast.makeText(global_context,
								"Request USB Permission", Toast.LENGTH_SHORT)
								.show();
						usbmanager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		}

		return 0;
	}

	/* destroy accessory */
	public void DestroyAccessory(boolean bConfiged) {

		if (true == bConfiged) {
			READ_ENABLE = false;
			writeusbdata[0] = 0; // send dummy data for instream.read going
			SendPacket(1);
		} else {
			SetConfig();

			try {
				Thread.sleep(10);
			} catch (Exception e) {
				// CloseAccessory();
			}

			READ_ENABLE = false;
			writeusbdata[0] = 0; // send dummy data for instream.read going
			SendPacket(1);
			if (true == accessory_attached) {
				saveDefaultPreference();
			}

		}

		try {
			Thread.sleep(10);
		} catch (Exception e) {
			// CloseAccessory();
		}

		CloseAccessory();
	}

	/********************* helper routines *************************************************/

	public void OpenAccessory(UsbAccessory accessory) {
		filedescriptor = usbmanager.openAccessory(accessory);
		if (filedescriptor != null) {
			usbaccessory = accessory;

			FileDescriptor fd = filedescriptor.getFileDescriptor();

			inputstream = new FileInputStream(fd);
			outputstream = new FileOutputStream(fd);
			/* check if any of them are null */
			if (inputstream == null || outputstream == null) {
				Log.e("TCARE", "I/O nullo ");
				return;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
			}

			if (READ_ENABLE == false) {
				READ_ENABLE = true;

				SetConfig();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				readThread = new read_thread(inputstream);
				if (!readThread.isAlive()
						&& readThread.getState() != State.RUNNABLE) {
					readThread.setName("Thread_Lettura");
					readThread.start();
				}

				writeThread = new write_thread();
				if (!writeThread.isAlive()
						&& writeThread.getState() != State.RUNNABLE) {
					writeThread.setName("Thread_Scrittura");
					writeThread.start();

				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				writeData("@");
				writeData("^");
				writeData("a");
				writeData("?");
			}

			Log.d("TCARE", "OpenAccessory: stream di lettura avviato");
			Log.d("TCARE", "OpenAccessory: accessorio aperto");

		} else {
			Log.e("TCARE", "OpenAccessory: nessun accessorio trovato");
		}
	}

	private void CloseAccessory() {
		try {
			if (filedescriptor != null)
				filedescriptor.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (inputstream != null)
				inputstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (outputstream != null)
				outputstream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		filedescriptor = null;
		inputstream = null;
		outputstream = null;

	}

	protected void saveDetachPreference() {
		if (preferences != null) {
			preferences.edit().putString("configed", "FALSE").commit();
		}
	}

	protected void saveDefaultPreference() {
		if (preferences != null) {
			preferences.edit().putString("configed", "TRUE").commit();
		}
	}

	/*********** USB broadcast receiver *******************************************/
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = (UsbAccessory) intent
							.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						Toast.makeText(global_context, "Allow USB Permission",
								Toast.LENGTH_SHORT).show();
						OpenAccessory(accessory);
					} else {
						Toast.makeText(global_context, "Deny USB Permission",
								Toast.LENGTH_SHORT).show();
						Log.d("TCARE", "permission denied for accessory "
								+ accessory);

					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				READ_ENABLE = false;
				saveDetachPreference();
				DestroyAccessory(true);
			} else {
				Log.d("TCARE", "....");
			}
		}

	};

	/* usb input data handler */
	private class read_thread extends Thread {
		FileInputStream instream;

		read_thread(FileInputStream stream) {
			instream = stream;
			this.setPriority(Thread.MAX_PRIORITY);
		}

		public void run() {

			Log.d("TCARE", "ENTRO NEL LEGGO");

			while (READ_ENABLE) {

				// Log.d("TCARE", "SONO NEL LEGGO");

				try {
					if (instream != null) {

						readcount = instream.read(usbdata, 0, 1024);

						if (readcount > 0) {
							for (int count = 0; count < readcount; count++) {

								if (usbdata[count] == (byte) '\r') {
									if (!readSB.toString().contains("W")) {
										Log.d("TCARE", "COMANDO_RICEVUTO="
												+ readSB.toString());
									}
									// Log.d("TCARE",
									// "COMANDO_RICEVUTO="
									// + readSB.toString());

									// Log.d("TCARE", "EXIT=" + exit);
									exit = 0;

									utility.esegui(readSB.toString().trim());
									readSB.delete(0, readSB.length());

								} else {
									readSB.append((char) usbdata[count]);
									// Log.d("TCARE",
									// "COMANDO_WIP="
									// + readSB.toString());
								}
							}
						} else {
							Log.d("TCARE", "BUFFER NULLO");
							READ_ENABLE = false;
						}
					} else {
						Log.d("TCARE", "read_thread: Inputstream NULL");
						READ_ENABLE = false;
					}
				} catch (IOException e) {

					Log.d("TCARE", "read_thread: HO PERSO LA SCHEDA");
					READ_ENABLE = false;

				}

				// Log.d("TCARE", "EXIT=" + exit);
				if (exit > preferences.getInt("timeout", 5)) {
					Log.d("TCARE", "AZZERO LA SCHEDA");
					READ_ENABLE = false;
				}

			}

			Log.d("TCARE", "ESCO DAL LEGGO");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	private class write_thread extends Thread {

		write_thread() {
		}

		public void run() {

			Log.d("TCARE", "ENTRO NELLO SCRIVO");

			while (READ_ENABLE) {

				// Log.d("TCARE", "SONO NELLO SCRIVO");

				writeData("W");

				exit += 1;

				Message aggiorna_tempo_lavoro_db = Main_Activity.aggiorna_tempo_lavoro_db
						.obtainMessage();
				Main_Activity.aggiorna_tempo_lavoro_db
						.sendMessage(aggiorna_tempo_lavoro_db);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				if (exit > preferences.getInt("timeout", 5)) {
					READ_ENABLE = false;
				}
			}

			READ_ENABLE = false;
			Log.d("TCARE", "ESCO DALLO SCRIVO");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	public void writeData(String commandString) {

		int numBytes = commandString.length();
		byte[] writeBuffer = new byte[64];

		for (int i = 0; i < numBytes; i++) {
			writeBuffer[i] = (byte) commandString.charAt(i);
			if (!String.valueOf(commandString.charAt(i)).equals("W"))
				Log.d("TCARE", "writeData: scrivo: " + commandString.charAt(i)
						+ " tradotto: " + (byte) commandString.charAt(i));
		}

		SendData(numBytes, writeBuffer);

	}

	public void MandaDati(int max) {
		try {
			if (outputstream != null) {
				outputstream.write(max);
				outputstream.flush();

				Log.d("TCARE", "MandaDati: scrittura eseguita= " + max);
			} else {
				Log.i("TCARE", "MandaDati: stream di scrittura chiuso");
				READ_ENABLE = false;
			}
		} catch (IOException e) {

			READ_ENABLE = false;

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}

			android.os.Process.killProcess(android.os.Process.myPid());

		}
	}

}