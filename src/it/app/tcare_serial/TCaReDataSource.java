package it.app.tcare_serial;

import android.content.Context;
import android.database.SQLException;

public class TCaReDataSource {

	private TCaReDB dbHelper;
	
	public TCaReDataSource(Context context) 
	{
	    dbHelper = new TCaReDB(context);
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
