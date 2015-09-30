package it.app.hypertherm.thread;

import java.util.concurrent.BlockingQueue;

public class InviaComandiThread implements Runnable {

	private BlockingQueue<byte[]> queue = null;
	private byte[] buf;

	public InviaComandiThread(BlockingQueue<byte[]> queue, byte[] buf) {

		this.queue = queue;

		this.buf = buf;

	}

	public void run() {

		queue.add(buf);

	}

}
