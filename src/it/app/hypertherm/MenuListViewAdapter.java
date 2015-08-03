package it.app.hypertherm;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuListViewAdapter extends ArrayAdapter<Menu_app> {

	private ArrayList<Menu_app> myMenu;
	private int resLayout, mySelectedItem;
	private Activity activity;
	private View row;
	private LayoutInflater inflater;

	private SharedPreferences preferences;

	public MenuListViewAdapter(Activity activity, ArrayList<Menu_app> myMenu) {
		super(activity, R.layout.list_view_custom_menu, myMenu);

		this.myMenu = myMenu;

		preferences = PreferenceManager.getDefaultSharedPreferences(activity);

		resLayout = R.layout.list_view_custom_menu;

		this.activity = activity;

		this.inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	public static class ViewHolder {
		TextView item;
		LinearLayout row_item;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		row = convertView;

		if (row == null) {

			holder = new ViewHolder();

			row = inflater.inflate(resLayout, parent, false);

			holder.item = (TextView) row.findViewById(R.id.item);
			holder.row_item = (LinearLayout) row.findViewById(R.id.menu_item);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		holder.item.setText(myMenu.get(position).getItem());

		final LayoutParams params = holder.row_item.getLayoutParams();

		int tot = myMenu.size();

		WindowManager wm = (WindowManager) activity
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		final LinearLayout row_lista_item = (LinearLayout) activity
				.findViewById(R.id.row_lista_item);

		if (params != null) {
			params.height = row_lista_item.getHeight() / tot - 1;
			params.width = size.x;
		}

		if (position % 2 == 1) {
			row.setBackgroundColor(Color.parseColor("#39bdce"));
		} else {
			row.setBackgroundColor(Color.parseColor("#00aac0"));
		}

		row.setLayoutParams(params);

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
