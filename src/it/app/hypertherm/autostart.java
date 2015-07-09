package it.app.hypertherm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class autostart extends BroadcastReceiver {

	public void onReceive(Context arg0, Intent arg1) {
		Intent intent = new Intent(arg0, Service_boot.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		arg0.startService(intent);

	}
}
