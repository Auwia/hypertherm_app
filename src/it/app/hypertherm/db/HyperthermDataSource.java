package it.app.hypertherm.db;

import android.content.Context;
import android.database.SQLException;

public class HyperthermDataSource {

	private HyperthermDB dbHelper;
	
	public HyperthermDataSource(Context context) 
	{
	    dbHelper = new HyperthermDB(context);
	}

	public void open() throws SQLException 
	{
		dbHelper.getWritableDatabase();
	}

	public void close() 
	{
		dbHelper.close();
	}
}
