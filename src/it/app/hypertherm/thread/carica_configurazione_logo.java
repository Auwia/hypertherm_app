package it.app.hypertherm.thread;

import it.app.hypertherm.R;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

public class carica_configurazione_logo implements Runnable {

	private Activity activity;

	public carica_configurazione_logo(Activity activity) {
		this.activity = activity;
	}

	public void run() {
		File root = Environment.getExternalStorageDirectory();
		ImageView logo = (ImageView) activity.findViewById(R.id.logo);
		Bitmap bMap = BitmapFactory.decodeFile(root
				+ "/Hypertherm/images/logo.jpg");
		logo.setImageBitmap(bMap);
	}

}
