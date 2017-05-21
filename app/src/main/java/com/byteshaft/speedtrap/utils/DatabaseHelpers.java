package com.byteshaft.speedtrap.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by fi8er1 on 23/11/2016.
 */

public class DatabaseHelpers extends SQLiteOpenHelper {

    private ArrayList<OnDatabaseChangedListener> mListeners = new ArrayList<>();

    public DatabaseHelpers(Context context) {
        super(context, DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseConstants.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + DatabaseConstants.TABLE_NAME);
        onCreate(db);
    }

    public void createNewEntry(String id, String type, String location) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.ID, id);
        values.put(DatabaseConstants.TYPE, type);
        values.put(DatabaseConstants.LOCATION, location);
        db.insert(DatabaseConstants.TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<HashMap> getAllRecords() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseConstants.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<HashMap> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.ID)));
            hashMap.put("type", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.TYPE)));
            hashMap.put("location", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.LOCATION)));
            list.add(hashMap);
        }
        db.close();
        cursor.close();
        return list;
    }

    public void deleteEntry(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM "
                + DatabaseConstants.TABLE_NAME
                + " WHERE "
                + DatabaseConstants.ID
                + "="
                + "'" + id + "'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        db.close();
        cursor.close();
    }

    public void clearTable() {
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM " + DatabaseConstants.TABLE_NAME;
        db.execSQL(query);
        db.close();
    }

    public boolean isEmpty() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseConstants.TABLE_NAME, null);
        boolean isEmpty;
        isEmpty = !cursor.moveToNext();
        cursor.close();
        return isEmpty;
    }

    public boolean entryExists(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT * FROM " + DatabaseConstants.TABLE_NAME + " WHERE " + DatabaseConstants.ID + " =?";
        Cursor cursor = db.rawQuery(selectString, new String[] {id});
        boolean entryExists = false;
        if(cursor.moveToFirst()){
            entryExists = true;
            while(cursor.moveToNext()){
                entryExists = true;
            }
        }
        cursor.close();
        db.close();
        return entryExists;
    }

    public void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mListeners.add(listener);
    }

    private void dispatchEventOnNewEntryCreated() {
        for (OnDatabaseChangedListener listener : mListeners) {
            listener.onNewEntryCreated();
        }
    }

    interface OnDatabaseChangedListener {
        void onNewEntryCreated();
    }
}
