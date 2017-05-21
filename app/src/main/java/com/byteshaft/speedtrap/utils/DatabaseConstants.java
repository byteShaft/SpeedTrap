package com.byteshaft.speedtrap.utils;

/**
 * Created by fi8er1 on 23/11/2016.
 */
public class DatabaseConstants {

    public static final String DATABASE_NAME = "Database.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "TrapsDatabase";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String LOCATION = "location";

    private static final String OPENING_BRACE = "(";
    private static final String CLOSING_BRACE = ")";

    public static final String TABLE_CREATE = "CREATE TABLE "
            + TABLE_NAME
            + OPENING_BRACE
            + ID + " TEXT,"
            + TYPE + " TEXT,"
            + LOCATION + " TEXT"
            + CLOSING_BRACE;
}