package it.app.hypertherm.activity;

import it.app.hypertherm.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class StrutturaProfonditaActivity extends Activity {

	private Button button_home, button_muscolare, button_mix,
			button_articolare, button_uno, button_due, button_tre,
			button_quattro, button_ok;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_struttura_profondita);

		button_home = (Button) findViewById(R.id.button_home);
		button_muscolare = (Button) findViewById(R.id.button_muscolare);
		button_mix = (Button) findViewById(R.id.button_mix);
		button_articolare = (Button) findViewById(R.id.button_articolare);
		button_uno = (Button) findViewById(R.id.button_uno);
		button_due = (Button) findViewById(R.id.button_due);
		button_tre = (Button) findViewById(R.id.button_tre);
		button_quattro = (Button) findViewById(R.id.button_quattro);
		button_ok = (Button) findViewById(R.id.button_ok);

		button_muscolare.setPressed(true);
		button_uno.setPressed(true);

		button_home.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		button_muscolare.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_mix.isPressed() || button_articolare.isPressed()) {

						if (button_muscolare.isPressed()) {
							button_muscolare.setPressed(false);
							button_mix.setPressed(true);
							button_articolare.setPressed(true);
						} else {
							button_muscolare.setPressed(true);
							button_mix.setPressed(false);
							button_articolare.setPressed(false);
						}
					}
				}
				return true;
			}
		});

		button_mix.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_muscolare.isPressed()
							|| button_articolare.isPressed()) {

						if (button_mix.isPressed()) {
							button_mix.setPressed(false);
							button_muscolare.setPressed(true);
							button_articolare.setPressed(true);
						} else {
							button_mix.setPressed(true);
							button_muscolare.setPressed(false);
							button_articolare.setPressed(false);
						}
					}
				}

				return true;
			}
		});

		button_articolare.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (button_muscolare.isPressed() || button_mix.isPressed()) {

					if (event.getAction() == MotionEvent.ACTION_UP) {
						if (button_articolare.isPressed()) {
							button_articolare.setPressed(false);
							button_mix.setPressed(true);
							button_muscolare.setPressed(true);
						} else {
							button_articolare.setPressed(true);
							button_mix.setPressed(false);
							button_muscolare.setPressed(false);
						}
					}
				}

				return true;
			}
		});

		button_uno.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_due.isPressed() || button_tre.isPressed()
							|| button_quattro.isPressed()) {

						if (button_uno.isPressed()) {
							button_uno.setPressed(false);
							button_due.setPressed(true);
							button_tre.setPressed(true);
							button_quattro.setPressed(true);
						} else {
							button_uno.setPressed(true);
							button_due.setPressed(false);
							button_tre.setPressed(false);
							button_quattro.setPressed(false);
						}
					}
				}
				return true;
			}
		});

		button_due.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_uno.isPressed() || button_tre.isPressed()
							|| button_quattro.isPressed()) {

						if (button_due.isPressed()) {
							button_due.setPressed(false);
							button_uno.setPressed(true);
							button_tre.setPressed(true);
							button_quattro.setPressed(true);
						} else {
							button_due.setPressed(true);
							button_uno.setPressed(false);
							button_tre.setPressed(false);
							button_quattro.setPressed(false);
						}
					}
				}
				return true;
			}
		});

		button_tre.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_uno.isPressed() || button_due.isPressed()
							|| button_quattro.isPressed()) {

						if (button_tre.isPressed()) {
							button_tre.setPressed(false);
							button_uno.setPressed(true);
							button_due.setPressed(true);
							button_quattro.setPressed(true);
						} else {
							button_tre.setPressed(true);
							button_uno.setPressed(false);
							button_due.setPressed(false);
							button_quattro.setPressed(false);
						}
					}
				}
				return true;
			}
		});

		button_quattro.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {

					if (button_uno.isPressed() || button_due.isPressed()
							|| button_tre.isPressed()) {

						if (button_quattro.isPressed()) {
							button_quattro.setPressed(false);
							button_uno.setPressed(true);
							button_due.setPressed(true);
							button_tre.setPressed(true);
						} else {
							button_quattro.setPressed(true);
							button_uno.setPressed(false);
							button_due.setPressed(false);
							button_tre.setPressed(false);
						}
					}
				}
				return true;
			}
		});

		button_ok.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				load_menu_item(0);
			}
		});

	}

	protected void load_menu_item(int position) {

		Intent intent;

		intent = new Intent(StrutturaProfonditaActivity.this,
				WorkActivity.class);
		startActivity(intent);

		switch (position) {
		case 0:

			break;
		case 1:
			break;
		case 2:

			// intent = new Intent(MainActivity.this, WorkActivity.class);
			// startActivity(intent);

			break;
		case 3:
			break;
		case 4:
			break;
		}

	}

}
