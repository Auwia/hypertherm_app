package it.app.hypertherm.util;

import it.app.hypertherm.R;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

public class carica_configurazione_logo extends AsyncTask<Void, Void, Void> {

	private Activity activity;

	private Utility utility;

	public carica_configurazione_logo(Activity activity) {
		this.activity = activity;

		utility = new Utility(activity);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		utility.appendLog("D", "upload logo...");
	}

	@Override
	protected Void doInBackground(Void... params) {

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				File root = Environment.getExternalStorageDirectory();
				ImageView logo = (ImageView) activity.findViewById(R.id.logo);
				Bitmap bMap = BitmapFactory.decodeFile(root
						+ "/Hypertherm/images/logo.jpg");
				logo.setImageBitmap(bMap);

			}
		});

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		utility.appendLog("D", "upload logo...OK");

	}

}
