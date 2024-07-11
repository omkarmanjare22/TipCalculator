package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "secureApp.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    private static final String TABLE_CERTIFICATES = "certificates";
    private static final String COLUMN_CERTIFICATE_ID = "id";
    private static final String COLUMN_CERTIFICATE_NAME = "name";
    private static final String COLUMN_CERTIFICATE_DESIGNATION = "designation";
    private static final String COLUMN_CERTIFICATE_DATE = "date";

    private static final String TABLE_ID_CARDS = "id_cards";
    private static final String COLUMN_ID_CARD_ID = "id";
    private static final String COLUMN_ID_CARD_NAME = "name";
    private static final String COLUMN_ID_CARD_DESIGNATION = "designation";
    private static final String COLUMN_ID_CARD_NUMBER = "number";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_CERTIFICATES_TABLE = "CREATE TABLE " + TABLE_CERTIFICATES + "("
                + COLUMN_CERTIFICATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CERTIFICATE_NAME + " TEXT,"
                + COLUMN_CERTIFICATE_DESIGNATION + " TEXT,"
                + COLUMN_CERTIFICATE_DATE + " TEXT" + ")";
        db.execSQL(CREATE_CERTIFICATES_TABLE);

        String CREATE_ID_CARDS_TABLE = "CREATE TABLE " + TABLE_ID_CARDS + "("
                + COLUMN_ID_CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ID_CARD_NAME + " TEXT,"
                + COLUMN_ID_CARD_DESIGNATION + " TEXT,"
                + COLUMN_ID_CARD_NUMBER + " TEXT" + ")";
        db.execSQL(CREATE_ID_CARDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CERTIFICATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ID_CARDS);
        onCreate(db);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        boolean result = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return result;
    }

    public boolean addCertificate(String name, String designation, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CERTIFICATE_NAME, name);
        values.put(COLUMN_CERTIFICATE_DESIGNATION, designation);
        values.put(COLUMN_CERTIFICATE_DATE, date);

        long result = db.insert(TABLE_CERTIFICATES, null, values);
        db.close();
        return result != -1;
    }

    public boolean addIDCard(String name, String designation, String number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID_CARD_NAME, name);
        values.put(COLUMN_ID_CARD_DESIGNATION, designation);
        values.put(COLUMN_ID_CARD_NUMBER, number);

        long result = db.insert(TABLE_ID_CARDS, null, values);
        db.close();
        return result != -1;
    }
}
