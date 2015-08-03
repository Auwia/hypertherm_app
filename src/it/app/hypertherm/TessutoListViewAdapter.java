package it.app.hypertherm;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TessutoListViewAdapter extends ArrayAdapter<Menu_app> {
	ArrayList<Menu_app> myMenu;
	int resLayout;
	Context context;

	public View row;

	public TessutoListViewAdapter(Context context, ArrayList<Menu_app> myMenu) {
		super(context, R.layout.list_view_custom_tessuto, myMenu);
		this.myMenu = myMenu;
		resLayout = R.layout.list_view_custom_tessuto;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		row = convertView;
		if (row == null) {
			LayoutInflater ll = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = ll.inflate(resLayout, parent, false);
		}

		Menu_app item = myMenu.get(position);

		if (item != null) {
			TextView myMenuDescription = (TextView) (row != null ? row
					.findViewById(R.id.item) : null);

			if (myMenuDescription != null) {
				myMenuDescription.setText(myMenu.get(position).getItem());
			}

		}

		return row;
	}

	@Override
	public boolean isEnabled(int position) {

		if (myMenu.get(position).getMenuFlaggato()) {

			return true;

		} else {

			return false;

		}
	}
}
