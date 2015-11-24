package it.app.hypertherm.thread;

import it.app.hypertherm.R;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

public class carica_immagini implements Runnable {

	private Activity activity;

	private int numero_immagine = 0;

	public carica_immagini(Activity activity, int numero_immagine) {
		this.activity = activity;

		this.numero_immagine = numero_immagine;
	}

	public void run() {
		File root = Environment.getExternalStorageDirectory();
		ImageView immagine_suggerimento = (ImageView) activity
				.findViewById(R.id.immagine_suggerimento);
		immagine_suggerimento.setVisibility(View.VISIBLE);
		Bitmap bMap = BitmapFactory.decodeFile(root
				+ "/Hypertherm/images/immagine_" + numero_immagine + ".jpg");
		immagine_suggerimento.setImageBitmap(bMap);
	}

}
