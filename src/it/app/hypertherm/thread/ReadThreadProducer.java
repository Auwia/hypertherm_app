package it.app.hypertherm.thread;

import it.app.hypertherm.Tracciato;
import it.app.hypertherm.activity.WorkActivity;
import it.app.hypertherm.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

public class ReadThreadProducer implements Runnable {

	private BlockingQueue<byte[]> queue = null;
	private static int TIME_OUT_READ;
	private InputStream mInputStream;
	private Utility utility;
	private byte[] buf = new byte[64];

	public ReadThreadProducer(BlockingQueue<byte[]> queue, Utility utility,
			InputStream mInputStream) {

		this.queue = queue;

		this.utility = utility;

		this.mInputStream = mInputStream;

		TIME_OUT_READ = utility.get_time_out_read();

	}

	public void run() {

		utility.appendLog("D", "ENTRO NEL PRODUCER");

		while (WorkActivity.COMMUNICATION_READY) {

			try {

				final byte[] buffer = new byte[Tracciato.PACKET_SIZE];
				int total = 0;
				int read = 0;
				int somma = 0;
				while (total < Tracciato.PACKET_SIZE
						&& (read = mInputStream.read(buffer, total,
								Tracciato.PACKET_SIZE - total)) >= 0) {
					total += read;
					somma += read;
				}

				if (read != -1) {

					if (read == 64 || somma == 64) {

						queue.add(buffer);

					} else {
						Log.d("MAX", "SOMMA=" + somma + " - READ=" + read);
					}

				}

				Thread.sleep(TIME_OUT_READ);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}
