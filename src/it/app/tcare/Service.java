package it.app.tcare;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class Service extends Activity {

	private Button esc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_service);

		esc = (Button) findViewById(R.id.esc);
		esc.setText(getResources().getString(R.string.esc));
	}

}
