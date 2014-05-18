package com.xzw.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	/** 数据库名称 **/

	public static final String DATABASE_NAME = "pzc.db";

	/** 数据库版本号 **/
	private static final int DATABASE_VERSION = 1;

	/** 数据库SQL语句 添加一个表 **/
	private static final String NAME_TABLE_CREATE = "create table test("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT,"
			+ "hp INTEGER DEFAULT 100," + "mp INTEGER DEFAULT 100,"
			+ "number INTEGER);";

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
