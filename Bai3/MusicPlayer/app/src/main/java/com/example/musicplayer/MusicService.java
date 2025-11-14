package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private static final String CHANNEL_ID = "MusicPlayerChannel";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PLAY = "com.example.musicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.musicplayer.ACTION_PAUSE";
    public static final String ACTION_NEXT = "com.example.musicplayer.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "com.example.musicplayer.ACTION_PREVIOUS";
    public static final String ACTION_STOP = "com.example.musicplayer.ACTION_STOP";

    private MediaPlayer mediaPlayer;
    private List<Song> playlist = new ArrayList<>();
    private int currentPosition = 0;
    private final IBinder binder = new MusicBinder();
    private MusicServiceCallback callback;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateProgressRunnable;
    private NotificationReceiver receiver;

    public interface MusicServiceCallback {
        void onSongChanged(Song song, int position);
        void onPlaybackStateChanged(boolean isPlaying);
        void onProgressUpdate(int progress, int duration);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> playNext());

        // Register broadcast receiver
        receiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_STOP);

        // ContextCompat tự động handle API level
        ContextCompat.registerReceiver(
                this,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        // Progress update runnable
        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    if (callback != null) {
                        callback.onProgressUpdate(
                                mediaPlayer.getCurrentPosition(),
                                mediaPlayer.getDuration()
                        );
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallback(MusicServiceCallback callback) {
        this.callback = callback;
    }

    public void setPlaylist(List<Song> songs, int position) {
        this.playlist = songs;
        this.currentPosition = position;
        playSong(position);
    }

    public void playSong(int position) {
        if (position < 0 || position >= playlist.size()) return;

        currentPosition = position;
        Song song = playlist.get(position);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            startForeground(NOTIFICATION_ID, createNotification());
            if (callback != null) {
                callback.onSongChanged(song, position);
                callback.onPlaybackStateChanged(true);
            }
            handler.post(updateProgressRunnable);
        } catch (IOException e) {
            e.printStackTrace();
            // Try next song if current fails
            if (position < playlist.size() - 1) {
                playNext();
            }
        }
    }

    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateNotification();
            if (callback != null) {
                callback.onPlaybackStateChanged(true);
            }
            handler.post(updateProgressRunnable);
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification();
            if (callback != null) {
                callback.onPlaybackStateChanged(false);
            }
            handler.removeCallbacks(updateProgressRunnable);
        }
    }

    public void playNext() {
        if (currentPosition < playlist.size() - 1) {
            playSong(currentPosition + 1);
        } else {
            playSong(0); // Loop to first song
        }
    }

    public void playPrevious() {
        if (currentPosition > 0) {
            playSong(currentPosition - 1);
        } else {
            playSong(playlist.size() - 1); // Loop to last song
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public Song getCurrentSong() {
        if (currentPosition >= 0 && currentPosition < playlist.size()) {
            return playlist.get(currentPosition);
        }
        return null;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getPlaybackPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Music playback controls");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Song song = getCurrentSong();
        if (song == null) return null;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Load album art
        Bitmap albumArt = loadAlbumArt(song.getPath());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        if (albumArt != null) {
            builder.setLargeIcon(albumArt);
        }

        // Add actions
        builder.addAction(createAction(R.drawable.ic_previous, "Previous", ACTION_PREVIOUS));
        if (isPlaying()) {
            builder.addAction(createAction(R.drawable.ic_pause, "Pause", ACTION_PAUSE));
        } else {
            builder.addAction(createAction(R.drawable.ic_play, "Play", ACTION_PLAY));
        }
        builder.addAction(createAction(R.drawable.ic_next, "Next", ACTION_NEXT));

        // Media style - hiển thị 3 nút chính trong compact view
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)); // Previous, Play/Pause, Next

        return builder.build();
    }

    private NotificationCompat.Action createAction(int icon, String title, String action) {
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification());
        }
    }

    private Bitmap loadAlbumArt(String path) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateProgressRunnable);
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    // Broadcast Receiver for notification actions
    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case ACTION_PLAY:
                    play();
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREVIOUS:
                    playPrevious();
                    break;
                case ACTION_STOP:
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }
    }
}