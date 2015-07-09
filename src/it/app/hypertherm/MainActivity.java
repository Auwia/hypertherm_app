package it.app.hypertherm;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends Activity {

	private MenuListViewAdapter myAdapter;

	private Utility utility;

	private ListView listaMenuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		utility = new Utility(this);

		import_menu_items();

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				File root = Environment.getExternalStorageDirectory();
				ImageView logo = (ImageView) findViewById(R.id.logo);
				Bitmap bMap = BitmapFactory.decodeFile(root
						+ "/TCaRe/images/logo.jpg");
				logo.setImageBitmap(bMap);

			}
		});

		listaMenuItem.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long arg3) {

				for (int i = 0; i < listaMenuItem.getChildCount(); i++) {

					if (position == i) {
						listaMenuItem.getChildAt(i).setBackgroundColor(
								Color.WHITE);
						listaMenuItem.getChildAt(i).setPressed(true);

					} else {
						listaMenuItem.getChildAt(i).setPressed(false);
						if (i % 2 == 1) {
							listaMenuItem.getChildAt(i).setBackgroundColor(
									Color.parseColor("#39bdce"));
						} else {
							listaMenuItem.getChildAt(i).setBackgroundColor(
									Color.parseColor("#00aac0"));
						}
					}
				}
			}
		});
	}

	private void import_menu_items() {
		myAdapter = new MenuListViewAdapter(this, utility.get_menu_items());
		listaMenuItem = (ListView) findViewById(R.id.listaMenuItem);
		listaMenuItem.setAdapter(myAdapter);
	}

}
