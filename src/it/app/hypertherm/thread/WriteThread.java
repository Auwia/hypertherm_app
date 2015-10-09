package it.app.hypertherm.thread;

import it.app.hypertherm.Simulatore;
import it.app.hypertherm.Tracciato;
import it.app.hypertherm.activity.WorkActivity;
import it.app.hypertherm.util.Utility;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public class WriteThread implements Runnable {

	private static int TIME_OUT_WRITE;
	private Utility utility;
	private OutputStream mWriteThread;
	private BlockingQueue<byte[]> queue = null;

	public WriteThread(BlockingQueue<byte[]> queue, Utility utility,
			OutputStream mWriteThread) {

		this.utility = utility;

		this.queue = queue;

		this.mWriteThread = mWriteThread;

		TIME_OUT_WRITE = utility.get_time_out_write();

	}

	public void run() {

		utility.appendLog("D", "ENTRO NELLO SCRIVO - TIME_OUT_IMPOSTATO="
				+ TIME_OUT_WRITE);

		while (WorkActivity.COMMUNICATION_READY) {

			byte[] buf = (byte[]) queue.poll();

			if (buf != null) {

				utility.stampa_tracciato(buf, "D", "out");

				if (WorkActivity.SIMULATORE) {

					Simulatore.simulate(buf);

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Simulatore.INVIA = true;

				} else {

					try {

						mWriteThread.write(buf, 0, Tracciato.PACKET_SIZE);

						mWriteThread.flush();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {

					Thread.sleep(TIME_OUT_WRITE);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

		utility.appendLog("D", "ESCO DALLO SCRIVO");

	}
}
