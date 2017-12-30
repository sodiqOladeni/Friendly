package com.example.android.friendly.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.friendly.data.FriendlyContract.FriendlyEntry;

/**
 * Created by ricHVision on 11/20/2017.
 */

public class FriendlyDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    private static final String DATABASE_NAME = "friendly.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    public FriendlyDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_FRIENDLY_TABLE =  "CREATE TABLE " + FriendlyEntry.TABLE_NAME + " ("
                + FriendlyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FriendlyEntry.COLUMN_FRIEND_NAME + " TEXT NOT NULL, "
                + FriendlyEntry.COLUMN_FRIEND_EMAIL + " TEXT, "
                + FriendlyEntry.COLUMN_FRIEND_PHONE + " NUMERIC, "
                + FriendlyEntry.COLUMN_FRIEND_FB + " TEXT, "
                + FriendlyEntry.COLUMN_FRIEND_REL + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_FRIENDLY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
       // The database is still at version 1, so there's nothing to do be done here.
    }
}
