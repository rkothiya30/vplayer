package com.example.vplayer.fragment.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.vplayer.R;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;


public class VideoPlayerUtils {

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static final String makeShortTimeString(final Context context, long secs) {
        int totalSeconds = (int) (secs);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public static String formateSize(long size) {
        if (size <= 0)
            return "0";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) ( Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getDateFormate(String time) {
        String dateString = "";
        if (time != null && !TextUtils.isEmpty(time)) {
            String longV = time;
            long millisecond = Long.parseLong(longV);
            dateString = DateFormat.format("dd/MM/yyyy hh:mm a", new Date(millisecond)).toString();
        }
        return dateString;
    }

    public static String getDateTimeFormate(String time) {
        String dateString = "";
        if (time != null && !TextUtils.isEmpty(time)) {
            String longV = time;
            long millisecond = Long.parseLong(longV);
            dateString = DateFormat.format("dd/MM/yyyy hh:mm ss a", new Date(millisecond)).toString();
        }
        return dateString;
    }

    public static String getFilenameFromPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String getParentPath(String path) {
        if (path.endsWith(getFilenameFromPath(path))) {
            return path.substring(0, path.length() - getFilenameFromPath(path).length());
        }
        return "";
    }

    public static String getFilenameExtension(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }

    public static String getMimeTypeFromFilePath(String filePath) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                getFilenameExtension(filePath));
        return (mimeType == null) ? null : mimeType;
    }

    public static void shareVideo(long id, Context context) {
        //if (PermissionManager.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            if (id == 0) {
                return;
            }
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            Uri trackUri = Uri.parse(uri.toString() + "/" + id);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, trackUri);
            intent.setType("video/*");
            context.startActivity(Intent.createChooser(intent, "Share Now"));
        } else {
            Log.d("ListenerUtil", "Permission failed");
        }
    }

    /*public static void showDeleteDialog(final Context context, final String name, final long[] list) {
        DeleteDialog.getInstance((Activity) context, name, list)
                .show(((AppCompatActivity) context)
                        .getSupportFragmentManager(), "");
    }*/

    public static void deleteTracks(final Context context, final long[] list) {
        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.DATA};
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            selection.append(list[i]);
            if (i < list.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (c != null) {
            // Step 1: Remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 2: Remove files from card
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final String name = c.getString(1);
                final File f = new File(name);
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Log.e("VideoUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (final SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

        Toast.makeText(context, "Video delete successfully", Toast.LENGTH_SHORT).show();
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
    }

    public static void renameVideo(final Context context, long videoId, String newName, String oldPath) {

        String PArentDir = new File(oldPath).getParent();
        File From = new File(PArentDir, VideoPlayerUtils.getFilenameFromPath(oldPath));
        File to = new File(PArentDir, newName);

        if (From.exists()) {
            if (From.renameTo(to)) {
                ContentValues values = new ContentValues();
                context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA + "='" + From.getAbsolutePath() + "'", null);
                values.put(MediaStore.Video.Media.DATA, to.getPath());
                values.put(MediaStore.Video.Media.DATE_TAKEN, to.lastModified());
                context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            }
        }
    }

    public static int getVideoFrameRate(String path) {
        MediaExtractor extractor = new MediaExtractor();
        int frameRate = 0; //may be default
        try {
            //Adjust data source as per the requirement if file, URI, etc.
            extractor.setDataSource(path);
            int numTracks = extractor.getTrackCount();
            for (int i = 0; i < numTracks; ++i) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Release stuff
            extractor.release();
        }
        return frameRate;
    }

    public static void hideVideo(Context context, String oldPath) {
        String path = getParentPath(oldPath);
        String fileName = getFilenameFromPath(oldPath);

        File file = new File(path + "." + context.getString(R.string.app_name));
        try {
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            /*Timber.d("hideVideo ------> " + e);*/
        }


        String newPath = file.getAbsolutePath() + "/" + fileName;

        boolean hide = new File(oldPath).renameTo(new File(newPath));

        if (hide) {
            context.getContentResolver()
                    .delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Video.Media.DATA + "='"
                                    + new File(oldPath).getPath()
                                    + "'", null);
        }
        context.getContentResolver().notifyChange(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null);

//        RxBus.getInstance().post(new MediaUpdateEvent());
    }

    public static String getHideVideoPath(Context context, String oldPath) {
        String path = getParentPath(oldPath);
        String fileName = getFilenameFromPath(oldPath);

        File file = new File(path + "." + context.getString(R.string.app_name));
        if (!file.exists()) {
            file.mkdir();
        }

        return file.getAbsolutePath() + "/" + fileName;
    }

    /*public static void unHideVideo(Context context, String oldPath) {
        String path = getParentPath(oldPath);
        String fileName = getFilenameFromPath(oldPath);

        path = path.replace("." + context.getString(R.string.app_name), "");

        String newPath = path + fileName;

        new File(oldPath).renameTo(new File(newPath));
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        RxBus.getInstance().post(new MediaUpdateEvent());
    }*/

    public static String getUnhideVideoPath(Context context, String oldPath) {
        String path = getParentPath(oldPath);
        String fileName = getFilenameFromPath(oldPath);

        path = path.replace("." + context.getString(R.string.app_name), "");

        String newPath = path + fileName;
        return newPath;
    }

   /* public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }*/
}
