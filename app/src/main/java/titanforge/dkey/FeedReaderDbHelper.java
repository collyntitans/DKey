package titanforge.dkey;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import titanforge.dkey.FeedReaderContract.*;

/**
 * Created by collyn on 11/29/16.
 */

public class FeedReaderDbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_256_TYPE = " BLOB(256)";
    private static final String COMMA_SEP = ",";

    // SQL for Table key
    private static final String SQL_CREATE_TABLEKEY =
            "CREATE TABLE IF NOT EXISTS " + TableKey.TABLE_NAME + " (" +
                    TableKey._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    TableKey.COLUMN_NAME_KEYNAME + TEXT_TYPE + COMMA_SEP +
                    TableKey.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    TableKey.COLUMN_NAME_KEY + BLOB_256_TYPE + COMMA_SEP +
                    TableKey.COLUMN_NAME_DESC + TEXT_TYPE + COMMA_SEP +
                    TableKey.COLUMN_NAME_STATUS + TEXT_TYPE + " )";

    private static final String SQL_DELETE_TABLEKEY =
            "DROP TABLE IF EXISTS " + TableKey.TABLE_NAME;

    //SQL for Table Renting
    private static final String SQL_CREATE_TABLERENTING =
            "CREATE TABLE IF NOT EXISTS " + TableRenting.TABLE_NAME + " (" +
                    TableRenting._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    TableRenting.COLUMN_NAME_KEYNAME + TEXT_TYPE + COMMA_SEP +
                    TableRenting.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    TableRenting.COLUMN_NAME_STARTDATE + TEXT_TYPE + COMMA_SEP +
                    TableRenting.COLUMN_NAME_ENDDATE + TEXT_TYPE + COMMA_SEP +
                    TableRenting.COLUMN_NAME_RENTCODE + TEXT_TYPE + COMMA_SEP +
                    TableRenting.COLUMN_NAME_CHECKIN + TEXT_TYPE + COMMA_SEP +
                    TableRenting.COLUMN_NAME_REDEEMDATE + TEXT_TYPE + " )";

    private static final String SQL_DELETE_TABLERENTING =
            "DROP TABLE IF EXISTS " + TableRenting.TABLE_NAME;

    // If change the database schema, must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DKeyRenter.db";

    public FeedReaderDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_TABLEKEY);
        db.execSQL(SQL_CREATE_TABLERENTING);

    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_TABLEKEY);
        db.execSQL(SQL_DELETE_TABLERENTING);

        onCreate(db);

    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        onUpgrade(db, oldVersion, newVersion);

    }
}
