package edu.stevens.cs522.chat.oneway.app.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by Xiang on 2015/2/27.
 */
public class ChatProvider extends ContentProvider {

    // Create the constants used to differentiate
    // between the different URI requests.
    public static final String AUTHORITY = "edu.stevens.cs522.chat.oneway.client";
    public static final String PEER_TABLE = "Peers";
    public static final String MESSAGE_TABLE = "Messages";
    public static final Uri PEER_TABLE_URI = new Uri.Builder().scheme("content").authority(AUTHORITY).path(PEER_TABLE).build();
    public static final Uri MESSAGE_TABLE_URI = new Uri.Builder().scheme("content").authority(AUTHORITY).path(MESSAGE_TABLE).build();
    public static final String PEER_TYPE = "vnd.android.cursor.dir/vnd.edu.stevens.cs522.chat.oneway.client.Peers";
    public static final String PEER_ITEM_TYPE = "vnd.android.cursor.item/vnd.edu.stevens.cs522.chat.oneway.client.Peer";
    public static final String MESSAGE_TYPE = "vnd.android.cursor.dir/vnd.edu.stevens.cs522.chat.oneway.client.Messages";
    public static final String MESSAGE_ITEM_TYPE = "vnd.android.cursor.item/vnd.edu.stevens.cs522.chat.oneway.client.Message";
    public static final String PEER_PATH = "Peers";
    public static final String PEER_PATH_ITEM = "Peers/#";
    public static final String MESSAGE_PATH = "Messages";
    public static final String MESSAGE_PATH_ITEM = "Messages/#";
    private static final int ALL_ROWS_PEER = 1;
    private static final int SINGLE_ROW_PEER = 2;
    private static final int ALL_ROWS_MESSAGE = 3;
    private static final int SINGLE_ROW_MESSAGE = 4;
    // Used to dispatch operation based on URI
    private static final UriMatcher uriMatcher;
    public static final String DATABASE_NAME = "D:\\Debug\\Chat client.db";
    public static final int DATABASE_VERSION = 4;
    // The index (key) column name for use in where clauses.
    public static final String _ID = "_id";
    // Name and column index of each column in your database
    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String PORT = "port";
    public static final String DATE = "date";
    public static final String MESSAGE = "message";
    public static final String MESSAGE_ID = "messageID";
    public static final String SENDER_ID = "senderID";
    public static final String SENDER = "sender";
    public static final String CHATROOM = "chatroom";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String PEER_FK = "peer_fk";
    // uriMatcher.addURI(AUTHORITY, CONTENT_PATH, OPCODE)
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PEER_PATH, ALL_ROWS_PEER);
        uriMatcher.addURI(AUTHORITY, PEER_PATH_ITEM, SINGLE_ROW_PEER);
        uriMatcher.addURI(AUTHORITY, MESSAGE_PATH, ALL_ROWS_MESSAGE);
        uriMatcher.addURI(AUTHORITY, MESSAGE_PATH_ITEM, SINGLE_ROW_MESSAGE);
    }
    public
    String getType (Uri _uri){
        switch (uriMatcher.match(_uri)){
            case ALL_ROWS_PEER:
                return "peer";
            case SINGLE_ROW_PEER:
                return "peer";
            case ALL_ROWS_MESSAGE:
                return "message";
            case SINGLE_ROW_MESSAGE:
                return "message";
            default:
                throw new IllegalArgumentException("Unsupported URI:" + _uri);
        }
    }

    private static final String DATABASE_CREATE2 =
            "create table " + MESSAGE_TABLE + " ("
                    + _ID + " integer primary key autoincrement, "
                    + MESSAGE + " text not null, "
                    + DATE + " text not null, "
                    + CHATROOM + " text not null, "
                    + SENDER + " text not null, "
                    + LATITUDE + " text not null, "
                    + LONGITUDE + " text not null, "
                    + ADDRESS + " text not null, "
                    + SENDER_ID + " integer not null, "
                    + MESSAGE_ID + " integer not null);";
    // SQL  Statement to create a new database.
    private static final String DATABASE_CREATE1 =
            "create table " + PEER_TABLE + " ("
                    + _ID + " integer primary key autoincrement, "
                    + NAME + " text not null, "
                    + ADDRESS + " text not null, "
                    + PORT + " text not null);";
    private SQLiteDatabase db;
    //private final Context context = null;

    //A private static inner class called DatabaseHelper
    //You create a subclass implementing onCreate(SQLiteDatabase), onUpgrade(SQLiteDatabase, int, int) and optionally onOpen(SQLiteDatabase),
    // and this class takes care of opening the database if it exists, creating it if it does not, and upgrading it as necessary
    protected static final class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE1);
            _db.execSQL(DATABASE_CREATE2);
        }

        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
            Log.w("CartDbAdapter", "Upgrading from version " + _oldVersion + "to " + _newVersion);
            _db.execSQL("DROP TABLE IF EXISTS " + PEER_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE);
            onCreate(_db);
        }
    }
    private DatabaseHelper dbHelper;
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }
    public ChatProvider open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    };
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        Cursor cursor;
        String _selection;
        db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_ROWS_PEER:
                //query the database
                cursor = db.query(PEER_TABLE, null, null, null, null, null, null);
                return cursor;
            case SINGLE_ROW_PEER:
                //query the database
                cursor = db.query(PEER_TABLE, null, selection, selectionArgs, null, null, null);
                cursor.moveToFirst();
                return cursor;
            case ALL_ROWS_MESSAGE:
                //query the database
                cursor = db.query(MESSAGE_TABLE, null, selection, selectionArgs, sort, null, null);
                return cursor;
            case SINGLE_ROW_MESSAGE:
                cursor = db.query(MESSAGE_TABLE, null, selection, selectionArgs, null, null, null);
                cursor.moveToFirst();
                return cursor;
            default: throw new IllegalArgumentException ("Unsupported URI:" + uri);
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues values){
        long row = 0;
        db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_ROWS_PEER:
                row = db.insert(PEER_TABLE, null, values);
                if(row > 0){
                    Uri instanceUri = Uri.withAppendedPath(PEER_TABLE_URI, String.valueOf(row));
                    ContentResolver cr = getContext().getContentResolver();
                    cr.notifyChange(instanceUri, null);
                    return instanceUri;
                }
            case ALL_ROWS_MESSAGE:
                row = db.insert(MESSAGE_TABLE, null, values);
                if(row > 0){
                    Uri instanceUri = Uri.withAppendedPath(MESSAGE_TABLE_URI, String.valueOf(row));
                    ContentResolver cr = getContext().getContentResolver();
                    cr.notifyChange(instanceUri, null);
                    return instanceUri;
                }
            default: throw new IllegalArgumentException ("Unsupported URI:" + uri);
        }
    }
    @Override
    public int delete(Uri uri, String where, String[] whereArgs){
        db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
            case ALL_ROWS_PEER:
                return db.delete(PEER_TABLE, null, null);
            case SINGLE_ROW_PEER:
                return db.delete(PEER_TABLE, where + "=?", whereArgs);
            case ALL_ROWS_MESSAGE:
                return db.delete(MESSAGE_TABLE, null, null);
            case SINGLE_ROW_MESSAGE:
                return db.delete(MESSAGE_TABLE, where + "=?", whereArgs);
            default: throw new IllegalArgumentException ("Unsupported URI:" + uri);
        }
    }
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs){
        db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)){
            case ALL_ROWS_PEER:
            case SINGLE_ROW_PEER:
            case ALL_ROWS_MESSAGE:
            case SINGLE_ROW_MESSAGE:
            default:
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
    }
}