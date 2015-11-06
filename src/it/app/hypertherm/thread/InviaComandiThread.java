package it.app.hypertherm.thread;

import it.app.hypertherm.util.Utility;

import java.util.concurrent.BlockingQueue;

public class InviaComandiThread implements Runnable {

	private BlockingQueue<byte[]> queue = null;
	private byte[] buf;
	private Utility utility = new Utility();

	public InviaComandiThread(BlockingQueue<byte[]> queue, byte[] buf) {

		this.queue = queue;

		this.buf = buf;

	}

	public void run() {

		try {
			queue.add(buf);
		} catch (Exception e) {
			utility.appendLog("E", "La scheda è scollegata...");
		}

	}

}
