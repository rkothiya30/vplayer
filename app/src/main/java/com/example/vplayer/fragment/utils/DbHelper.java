package com.example.vplayer.fragment.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Recent_Songs";

    public DbHelper( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "create table Recent (path TEXT, name TEXT, artist TEXT, album TEXT, duration TEXT, id TEXT, image TEXT)";

        String query1 = "create table Favourites (path TEXT, name TEXT, artist TEXT, album TEXT, duration TEXT, id TEXT, image TEXT)";

        String query2 = "create table PlaylistName (name TEXT)";

        db.execSQL(query);
        db.execSQL(query2);
        db.execSQL(query1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists Recent");
        db.execSQL("drop table if exists Favourites");
        onCreate(db);
    }

    public boolean insertRecentSongs(String path, String name, String artist, String album, String duration, String id, String image)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("name", name);
        values.put("artist", artist);
        values.put("album", album);
        values.put("duration", duration);
        values.put("id", id);
        values.put("image", image);
        long result = db.insert("Recent", null, values);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getRecentSongs()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from Recent", null);
        return c;
    }

    public Cursor getRecentSongFromId(String id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from Recent where id = ?", new String[]{id});
        return c;
    }

    public boolean addToFavourites(String path, String name, String artist, String album, String duration, String id, String image)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("name", name);
        values.put("artist", artist);
        values.put("album", album);
        values.put("duration", duration);
        values.put("id", id);
        values.put("image", image);
        long result = db.insert("Favourites", null, values);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getFavouritesSongs()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from Favourites", null);
        return c;
    }

    public Cursor getFavouritesFromId(String name)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from Favourites where name = ?", new String[]{name});
        return c;
    }

    public boolean removeFromFavourites(String name)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from Favourites where name = ?", new String[]{name});
        if(c.getCount() > 0)
        {
            long result = db.delete("Favourites", "name = ?", new String[]{name} );
            if(result == -1)
                return false;
            else
                return true;
        }
        else
            return false;
    }

    public void createTable(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "create table " + name + " (path TEXT, name TEXT, artist TEXT, album TEXT, duration TEXT, id TEXT, image TEXT)";
        db.execSQL(query);
    }

    public boolean insetIntoCreatedTable(String playlistName, String path, String name, String artist, String album, String duration, String id, String image)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("name", name);
        values.put("artist", artist);
        values.put("album", album);
        values.put("duration", duration);
        values.put("id", id);
        values.put("image", image);
        long result = db.insert(playlistName, null, values);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getPlaylistSongs(String playlistName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + playlistName + "", null);
        return c;
    }

    public boolean createPlaylist(String name)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        long result = db.insert("PlaylistName", null, values);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllPlaylist()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from PlaylistName", null);
        return c;
    }

    public Cursor getAlreadyAddedSongs(String songName, String playListName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + playListName+ " where name = ?", new String[]{songName});
        return c;
    }

    public boolean removeFromCreatedPlaylist(String playListName, String songName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        long result = db.delete(playListName, "name = ?", new String[]{songName} );
        if(result == -1)
            return false;
        else
            return true;
    }
}
