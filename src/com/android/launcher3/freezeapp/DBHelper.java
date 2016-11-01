package com.android.launcher3.freezeapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static final String TAG = "RgkFp.DBHelper";
	
	private static final String DATABASE_NAME = "fingerprint.db";
	private static final int DATABASE_VERSION = 6;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + FreezeProvider.TABLE_FREEZE_APPS + "("
				+ FreezeProvider.FREEZE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + FreezeProvider.FREEZE_PACKAGE_NAME
				+ " TEXT,intent TEXT," + FreezeProvider.FREEZE_UID + " INTEGER);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + FreezeProvider.TABLE_FREEZE_APPS);
		onCreate(db);
	}

}
