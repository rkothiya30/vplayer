package com.example.vplayer.fragment.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.example.vplayer.R;
import com.example.vplayer.dialog.DeleteDialog;
import com.example.vplayer.dialog.RenameDialog;
import com.example.vplayer.fragment.event.DeleteEvent;
import com.example.vplayer.fragment.event.RenameEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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

    public static boolean renameFile(File file, String newName, Context context) {
        DocumentFile targetDocument = getDocumentFile(file, false, context);
        boolean renamed = targetDocument.renameTo(newName);

        Log.e("renameFileUtils", "ParentFile: " + targetDocument.getParentFile() + " Name: " + targetDocument.getName());

        return renamed;
    }

    public static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context) {
        String baseFolder = getExtSdCardFolder(file, context);
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            Log.e("StorageUtils","fullPath: "+fullPath);
            if (!baseFolder.equals(fullPath))
                relativePath = fullPath.substring(baseFolder.length() + 1);
            else originalDirectory = true;
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
            //continue
        }

//        String as = "";
        String as = PreferencesUtility.getSDCardTreeUri(context);

        Uri treeUri = null;
        if (!TextUtils.isEmpty(as)) treeUri = Uri.parse(as);
        if (treeUri == null) {
            return null;
        }

        Log.e("StorageUtils","treeUri: "+treeUri + " as: " +as);
        Log.e("StorageUtils","relativePath: "+relativePath);
        Log.e("StorageUtils","baseFolder: "+baseFolder);

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) return document;
        String[] parts = relativePath.split("\\/");

        Log.e("StorageUtils","parts: "+parts.toString());
        if (document != null) {
            for (int i = 0; i < parts.length; i++) {
                if (parts[i] != null) {
                    if (document != null) {
                        DocumentFile nextDocument = document.findFile(parts[i]);

                        if (nextDocument == null) {
                            if ((i < parts.length - 1) || isDirectory) {
                                nextDocument = document.createDirectory(parts[i]);
                            } else {
                                nextDocument = document.createFile("image", parts[i]);
                            }
                        }
                        document = nextDocument;
                    }
                }
            }
        }
        return document;
    }

    public static String getExtSdCardFolder(final File file, Context context) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : ContextCompat.getExternalFilesDirs(context, "external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("FileUtils", "Unexpected external manager dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }

    public static String getParentPath(String path) {
        if (path.endsWith(getFilenameFromPath(path))) {
            return path.substring(0, path.length() - getFilenameFromPath(path).length());
        }
        return "";
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
    public static void shareAudio(String id, Context context) {
        //if (PermissionManager.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            if (id.isEmpty()) {
                return;
            }
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Uri trackUri = Uri.parse(uri.toString() + "/" + id);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, trackUri);
            intent.setType("audio/*");
            context.startActivity(Intent.createChooser(intent, "Share Now"));
        } else {
            Log.d("ListenerUtil", "Permission failed");
        }
    }

    public static void share(Context context,List<String> paths) {

        if (paths == null || paths.size() == 0) {
            return;
        }
        ArrayList<Uri> uris = new ArrayList<>();
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        for (String path : paths) {
            File file = new File(path);
            uris.add(Uri.fromFile(file));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(intent, "Share Now"));
    }

    public static void showDeleteDialog(final Context context, final String name, final long[] list) {
        DeleteDialog.getInstance((Activity) context, name, list)
                .show(((AppCompatActivity) context)
                        .getSupportFragmentManager(), "");
    }

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
        RxBus.getInstance().post(new DeleteEvent(list[0]));
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
    }

    public static String getFilenameExtension(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
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

    public static String getExternalStoragePath(Context mContext, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removable == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
   /* public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }*/
}
