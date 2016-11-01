package com.android.launcher3.freezeapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class FreezeProvider extends ContentProvider {
	private static final String TAG = "RgkFp.FPProvider";

	public static final String AUTHORITIES = "com.rgk.fingerprint";
	public static final String TABLE_FREEZE_APPS = "freeze";

	public static final int FREEZE = 9;
	public static final int FREEZE_ITEM = 10;
	
	public static final String FREEZE_ID = "_id";
	public static final String FREEZE_PACKAGE_NAME = "package_name";
	public static final String FREEZE_UID = "uid";

	private DBHelper dbHelper;
	private static UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITIES, TABLE_FREEZE_APPS, FREEZE);
		uriMatcher.addURI(AUTHORITIES, TABLE_FREEZE_APPS + "/#", FREEZE_ITEM);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case FREEZE:
			count = db.delete(TABLE_FREEZE_APPS, selection, selectionArgs);
			return count;
		case FREEZE_ITEM: {
			long id = ContentUris.parseId(uri);
			String where = "_id=" + id;
			count = db.delete(TABLE_FREEZE_APPS, where, selectionArgs);
			return count;
		}
		default:
			break;
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case FREEZE:
			return AUTHORITIES + "/" + TABLE_FREEZE_APPS;
		case FREEZE_ITEM:
			return AUTHORITIES + "/" + TABLE_FREEZE_APPS;
		default:
			throw new IllegalArgumentException("Unknow Uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.i(TAG, "insert: " + values + "\nuri: " + uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case FREEZE: {
			long rowId = db.insert(TABLE_FREEZE_APPS, null, values);
			if (rowId < 0) {
				throw new SQLiteException("Unable to insert " + values
						+ " for " + uri);
			}
			Uri insertUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(insertUri, null);
			return insertUri;
		}
		default:
			throw new IllegalArgumentException("Unknow Uri: " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		Log.d(TAG, "Provider > onCreate");
		dbHelper = new DBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "query");
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case FREEZE:
			Log.d(TAG, "FREEZE");
			return database.query(TABLE_FREEZE_APPS, projection, selection,
					selectionArgs, null, null, sortOrder);

		case FREEZE_ITEM: {
			Log.d(TAG, "FREEZE_ITEM");
			long id = ContentUris.parseId(uri);
			String where = "_id=" + id;
			if (selection != null && !"".equals(selection)) {
				where = selection + " and " + where;
			}

			return database.query(TABLE_FREEZE_APPS, projection, where,
					selectionArgs, null, null, sortOrder);
		}
		default:
			throw new IllegalArgumentException("Unknow Uri: " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case FREEZE:
			count = db.update(TABLE_FREEZE_APPS, values, selection, selectionArgs);
			return count;
		case FREEZE_ITEM: {
			long id = ContentUris.parseId(uri);
			String where = "_id=" + id;
			if (selection != null && !"".equals(selection)) {
				where = selection + " and " + where;
			}
			count = db.update(TABLE_FREEZE_APPS, values, where, selectionArgs);
			return count;
		}
		default:
			throw new IllegalArgumentException("Unknow Uri: " + uri);
		}
	}
}
