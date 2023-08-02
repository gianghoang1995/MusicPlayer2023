package com.musicplayer.mp3player.defaultmusicplayer.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R;
import com.musicplayer.mp3player.defaultmusicplayer.database.playlist.FavoriteSqliteHelperDB;
import com.musicplayer.mp3player.defaultmusicplayer.model.AlbumItem;
import com.musicplayer.mp3player.defaultmusicplayer.model.ArtistItem;
import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem;
import com.musicplayer.mp3player.defaultmusicplayer.model.PlaylistITem;
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.list.ListSongActivity;
import com.musicplayer.mp3player.defaultmusicplayer.ui.activity.plashscreen.PlashScreenActivity;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ShortcutManagerUtils {

    public static boolean createShortcutPlaylist(Context context, PlaylistITem playlist) {
        @SuppressLint("UseCompatLoadingForDrawables")
        Bitmap icon;

        String title = playlist.getName();
        int id = playlist.getId();
        String playlistID = String.valueOf(playlist.getFavorite_id());

        String label = "";
        if (title.equals(FavoriteSqliteHelperDB.DEFAULT_FAVORITE)) {
            label = context.getString(R.string.favorite_song);
            icon = AppUtils.INSTANCE.getBitmapFromVectorDrawable(context, R.drawable.ic_playlist_favorite);
        } else if (title.equals(FavoriteSqliteHelperDB.TABLE_RECENT_ADDED)) {
            label = context.getString(R.string.favorite_recent_added);
            icon = AppUtils.INSTANCE.getBitmapFromVectorDrawable(context, R.drawable.ic_playlist_recent_added);
        } else if (title.equals(FavoriteSqliteHelperDB.TABLE_LAST_PLAYING)) {
            label = context.getString(R.string.favorite_last_playing);
            icon = AppUtils.INSTANCE.getBitmapFromVectorDrawable(context, R.drawable.ic_playlist_last_playing);
        } else if (title.equals(FavoriteSqliteHelperDB.TABLE_MOST_PLAYING)) {
            label = context.getString(R.string.favorite_most_playing);
            icon = AppUtils.INSTANCE.getBitmapFromVectorDrawable(context, R.drawable.ic_playlist_most_play);
        } else {
            label = title;
            icon = AppUtils.INSTANCE.getBitmapFromVectorDrawable(context, R.drawable.ic_playlist_most_play);
        }
        Intent shortcutIntent = new Intent();
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(context.getPackageName(), ListSongActivity.class.getCanonicalName());
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        shortcutIntent.putExtra(AppConstants.SHORTCUT_PLAYLIST_ID, id);
        shortcutIntent.putExtra(AppConstants.SHORTCUT_PLAYLIST_NAME, title);
        shortcutIntent.putExtra(AppConstants.SHORTCUT_PLAYLIST_FAVORITE_ID, playlistID);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Icon withBitmap = Icon.createWithBitmap(icon);
            ShortcutInfo likeShortcut = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                likeShortcut = new ShortcutInfo.Builder(context, playlistID)
                        .setShortLabel(label)
                        .setLongLabel(label)
                        .setIcon(withBitmap)
                        .setIntent(shortcutIntent)
                        .build();
                createShortcutAboveN(context, likeShortcut);
            }
            // crate app shortcuts.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return createDeskShortcutAboveO(context, likeShortcut);
            }
        } else {
            Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
            intent.putExtra("duplicate", false);
            Parcelable parcelable = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_song);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, parcelable);
            context.sendBroadcast(intent);
        }
        return true;
    }

    public static void delShortcut(Context cx, PlaylistITem playlist) {
        Intent shortcutIntent = new Intent();
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(cx.getPackageName(), ListSongActivity.class.getCanonicalName());
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        shortcutIntent.putExtra(AppConstants.SHORTCUT_PLAYLIST_ID, playlist.getId());
        shortcutIntent.putExtra(AppConstants.SHORTCUT_PLAYLIST_NAME, playlist.getName());
        shortcutIntent.putExtra(AppConstants.SHORTCUT_PLAYLIST_FAVORITE_ID, playlist.getFavorite_id());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            removeDeskShortcutAboveO(cx, playlist.getFavorite_id());
        } else {
            Intent intent = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, playlist.getName());
            cx.sendBroadcast(intent);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static boolean createShortcutArtist(Context context, ArtistItem artist) {
        @SuppressLint("UseCompatLoadingForDrawables")
        Bitmap icon = null;

        String title = artist.getName();
        String artistID = String.valueOf(artist.getId());
        Intent shortcutIntent = new Intent(context.getApplicationContext(), PlashScreenActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.putExtra(AppConstants.SHORTCUT_ARTIST_ID, artistID);
        shortcutIntent.putExtra(AppConstants.SHORTCUT_ARTIST_NAME, title);
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        Uri uri = ArtworkUtils.INSTANCE.uri(Long.parseLong(artistID));
        icon = uriToBitmap(context, uri);
        if (icon == null) {
            icon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_artist_png);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Icon withBitmap = Icon.createWithBitmap(icon);
            ShortcutInfo likeShortcut = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                likeShortcut = new ShortcutInfo.Builder(context, artistID)
                        .setShortLabel(title)
                        .setLongLabel(title)
                        .setIcon(withBitmap)
                        .setIntent(shortcutIntent)
                        .build();
                createShortcutAboveN(context, likeShortcut);
            }
            // crate app shortcuts.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return createDeskShortcutAboveO(context, likeShortcut);
            }
        } else {
            Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            intent.putExtra("duplicate", false);
            Parcelable parcelable = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_artist_png);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, parcelable);
            context.sendBroadcast(intent);
        }
        return true;
    }

    public static boolean createShortcutAlbum(Context context, AlbumItem album) {
        /**val name: String?, val count: Int, var path: String?, val parentId: Int*/
        @SuppressLint("UseCompatLoadingForDrawables")
        Bitmap icon = null;

        String title = album.getAlbumName();
        String albumID = String.valueOf(album.getId());
        Intent shortcutIntent = new Intent(context.getApplicationContext(), PlashScreenActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.putExtra(AppConstants.SHORTCUT_ALBUM_ID, albumID);
        shortcutIntent.putExtra(AppConstants.SHORTCUT_ALBUM_NAME, title);
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        Uri uri = ArtworkUtils.INSTANCE.uri(Long.parseLong(albumID));
        icon = uriToBitmap(context, uri);
        if (icon == null) {
            icon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_album_png);
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Icon withBitmap = Icon.createWithBitmap(icon);
            ShortcutInfo likeShortcut = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                likeShortcut = new ShortcutInfo.Builder(context, albumID)
                        .setShortLabel(title)
                        .setLongLabel(title)
                        .setIcon(withBitmap)
                        .setIntent(shortcutIntent)
                        .build();
                createShortcutAboveN(context, likeShortcut);
            }
            // crate app shortcuts.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return createDeskShortcutAboveO(context, likeShortcut);
            }
        } else {
            Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            intent.putExtra("duplicate", false);
            Parcelable parcelable = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_album_png);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, parcelable);
            context.sendBroadcast(intent);
        }

        return true;
    }

    public static boolean createShortcutFolder(Context context, FolderItem folder, int resID) {
        /**val name: String?, val count: Int, var path: String?, val parentId: Int*/
        String title = folder.getName();
        String idShortcut = folder.getPath();

        Intent shortcutIntent = new Intent(context.getApplicationContext(), PlashScreenActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.putExtra(AppConstants.SHORTCUT_FOLDER_PATH, folder.getPath());
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_folder_query);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Icon withBitmap = Icon.createWithBitmap(icon);
            ShortcutInfo likeShortcut = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                likeShortcut = new ShortcutInfo.Builder(context, idShortcut)
                        .setShortLabel(title)
                        .setLongLabel(title)
                        .setIcon(withBitmap)
                        .setIntent(shortcutIntent)
                        .build();
                createShortcutAboveN(context, likeShortcut);
            }
            // crate app shortcuts.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return createDeskShortcutAboveO(context, likeShortcut);
            }
        } else {
            Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            intent.putExtra("duplicate", false);
            Parcelable parcelable = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_folder_query);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, parcelable);
            context.sendBroadcast(intent);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private static boolean createShortcutAboveN(Context context, ShortcutInfo likeShortcut) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return false;
        }
        try {
            int max = shortcutManager.getMaxShortcutCountPerActivity();
            List<ShortcutInfo> dynamicShortcuts = shortcutManager.getDynamicShortcuts();
            if (dynamicShortcuts.size() >= max) {
                Collections.sort(dynamicShortcuts, (o1, o2) -> {
                    long r = o1.getLastChangedTimestamp() - o2.getLastChangedTimestamp();
                    return r == 0 ? 0 : (r > 0 ? 1 : -1);
                });

                ShortcutInfo remove = dynamicShortcuts.remove(0);// remove old.
                shortcutManager.removeDynamicShortcuts(Collections.singletonList(remove.getId()));
            }

            shortcutManager.addDynamicShortcuts(Collections.singletonList(likeShortcut));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private static boolean removeShortcutAboveN(Context context, ShortcutInfo likeShortcut) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return false;
        }
        try {
            int max = shortcutManager.getMaxShortcutCountPerActivity();
            List<ShortcutInfo> dynamicShortcuts = shortcutManager.getDynamicShortcuts();
            if (dynamicShortcuts.size() >= max) {
                Collections.sort(dynamicShortcuts, (o1, o2) -> {
                    long r = o1.getLastChangedTimestamp() - o2.getLastChangedTimestamp();
                    return r == 0 ? 0 : (r > 0 ? 1 : -1);
                });

                ShortcutInfo remove = dynamicShortcuts.remove(0);// remove old.
                shortcutManager.removeDynamicShortcuts(Collections.singletonList(remove.getId()));
            }
            shortcutManager.removeDynamicShortcuts(Collections.singletonList(likeShortcut.getId()));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private static boolean createDeskShortcutAboveO(Context context, ShortcutInfo info) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return false;
        }
        if (shortcutManager.isRequestPinShortcutSupported()) {
            // 当添加快捷方式的确认弹框弹出来时，将被回调
            // PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0,
            // new Intent(context, MyReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

            List<ShortcutInfo> pinnedShortcuts = shortcutManager.getPinnedShortcuts();
            boolean exists = false;
            for (ShortcutInfo pinnedShortcut : pinnedShortcuts) {
                if (TextUtils.equals(pinnedShortcut.getId(), info.getId())) {
                    // already exist.
                    exists = true;
                    Toast.makeText(context, R.string.create_shortcut_already_exist, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            if (!exists) {
                shortcutManager.requestPinShortcut(info, null);
            }
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void removeDeskShortcutAboveO(Context context, String id) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        shortcutManager.removeDynamicShortcuts(Collections.singletonList(id));
        shortcutManager.disableShortcuts(Collections.singletonList(id));
    }

    public static Bitmap drawableToBitmap(Context context, Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        return getCroppedBitmap(context, bitmap);
    }

    private static Bitmap uriToBitmap(Context context, Uri selectedFileUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return getCroppedBitmap(context, image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getCroppedBitmap(Context context, Bitmap bitmap) {
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        roundedBitmapDrawable.setCornerRadius(32.0f);
        roundedBitmapDrawable.setAntiAlias(true);
        int width = roundedBitmapDrawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = roundedBitmapDrawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        roundedBitmapDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        roundedBitmapDrawable.draw(canvas);
        return output;
    }
}
