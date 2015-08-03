package it.app.hypertherm;

import it.app.hypertherm.activity.Splash_Screen_Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Service_boot extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onDestroy() {
	}

	@Override
	public void onStart(Intent intent, int startid) {
		Intent intents = new Intent(getBaseContext(),
				Splash_Screen_Activity.class);
		intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intents);
	}
}