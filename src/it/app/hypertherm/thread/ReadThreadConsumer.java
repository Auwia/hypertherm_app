package it.app.hypertherm.thread;

import it.app.hypertherm.activity.WorkActivity;
import it.app.hypertherm.util.Utility;

import java.util.concurrent.BlockingQueue;

public class ReadThreadConsumer implements Runnable {

	private BlockingQueue<byte[]> queue = null;
	private Utility utility;
	private static int TIME_OUT_READ;

	public ReadThreadConsumer(BlockingQueue<byte[]> queue, Utility utility) {

		this.queue = queue;

		this.utility = utility;

		TIME_OUT_READ = utility.get_time_out_read();
	}

	public void run() {

		utility.appendLog("D", "ENTRO NEL CONSUMER - TIME_OUT_IMPOSTATO="
				+ TIME_OUT_READ);

		while (WorkActivity.COMMUNICATION_READY) {

			try {

				byte[] buf = (byte[]) queue.poll();

				if (buf != null) {

					utility.esegui_buffer(buf);

					Thread.sleep(TIME_OUT_READ);

				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
