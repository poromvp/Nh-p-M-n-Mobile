package com.example.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MusicService.MusicServiceCallback {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private RecyclerView recyclerView;
    private MusicAdapter adapter;
    private List<Song> songs = new ArrayList<>();
    private TextView tvSongCount;
    private View emptyState;

    // Mini player views
    private View miniPlayer;
    private ImageView ivMiniAlbumArt;
    private TextView tvMiniTitle, tvMiniArtist;
    private ImageButton btnMiniPlayPause, btnMiniNext, btnMiniPrevious;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalTime;

    private MusicService musicService;
    private boolean isBound = false;
    private boolean isMiniPlayerVisible = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setCallback(MainActivity.this);
            isBound = true;

            // Update UI if music is already playing
            if (musicService.isPlaying()) {
                Song currentSong = musicService.getCurrentSong();
                if (currentSong != null) {
                    updateMiniPlayer(currentSong, musicService.getCurrentPosition());
                    updatePlayPauseButton(true);
                    showMiniPlayer();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupMiniPlayer();

        // Check permissions
        if (checkPermissions()) {
            loadSongs();
        } else {
            requestPermissions();
        }

        // Handle intent from file manager
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                Song song = getSongFromUri(uri);
                if (song != null) {
                    songs.clear();
                    songs.add(song);
                    adapter.notifyDataSetChanged();
                    startMusicService(0);
                    Toast.makeText(this, "Playing: " + song.getTitle(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Song getSongFromUri(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                String title = titleIndex >= 0 ? cursor.getString(titleIndex) : "Unknown";
                String artist = artistIndex >= 0 ? cursor.getString(artistIndex) : "Unknown Artist";
                long duration = durationIndex >= 0 ? cursor.getLong(durationIndex) : 0;
                String path = dataIndex >= 0 ? cursor.getString(dataIndex) : uri.getPath();

                cursor.close();
                return new Song(title, artist, path, duration, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvSongCount = findViewById(R.id.tvSongCount);
        emptyState = findViewById(R.id.emptyState);
        miniPlayer = findViewById(R.id.miniPlayer);
        ivMiniAlbumArt = findViewById(R.id.ivMiniAlbumArt);
        tvMiniTitle = findViewById(R.id.tvMiniTitle);
        tvMiniArtist = findViewById(R.id.tvMiniArtist);
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause);
        btnMiniNext = findViewById(R.id.btnMiniNext);
        btnMiniPrevious = findViewById(R.id.btnMiniPrevious);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
    }

    private void setupRecyclerView() {
        adapter = new MusicAdapter(songs, (song, position) -> {
            startMusicService(position);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupMiniPlayer() {
        miniPlayer.setVisibility(View.GONE);

        btnMiniPlayPause.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                } else {
                    musicService.play();
                }
            }
        });

        btnMiniNext.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.playNext();
            }
        });

        btnMiniPrevious.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.playPrevious();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null) {
                    musicService.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    PERMISSION_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access music files.", Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        }
    }

    private void loadSongs() {
        songs.clear();

        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = null;    //MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
        };

        Cursor cursor = resolver.query(uri, projection, selection, null,
                MediaStore.Audio.Media.TITLE + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                    File file = new File(path);
                    if (file.exists() && duration > 0) {
                        songs.add(new Song(title, artist, path, duration, null));
                        Log.d("MusicScan", "Found: " + title + " | " + path + " | duration: " + duration);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
        updateSongCount();

        if (songs.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }

    }

    private void updateSongCount() {
        tvSongCount.setText(songs.size() + " songs found");
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void startMusicService(int position) {
        Intent intent = new Intent(this, MusicService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        new android.os.Handler().postDelayed(() -> {
            if (isBound && musicService != null) {
                musicService.setPlaylist(songs, position);
                adapter.setCurrentPlayingPosition(position);
            }
        }, 100);
    }

    @Override
    public void onSongChanged(Song song, int position) {
        runOnUiThread(() -> {
            updateMiniPlayer(song, position);
            adapter.setCurrentPlayingPosition(position);
            if (!isMiniPlayerVisible) {
                showMiniPlayer();
            }
        });
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        runOnUiThread(() -> updatePlayPauseButton(isPlaying));
    }

    @Override
    public void onProgressUpdate(int progress, int duration) {
        runOnUiThread(() -> {
            seekBar.setMax(duration);
            seekBar.setProgress(progress);
            tvCurrentTime.setText(formatTime(progress));
            tvTotalTime.setText(formatTime(duration));
        });
    }

    private void updateMiniPlayer(Song song, int position) {
        if (song == null) return;

        tvMiniTitle.setText(song.getTitle());
        tvMiniArtist.setText(song.getArtist());

        // Load album art asynchronously
        new Thread(() -> {
            Bitmap albumArt = loadAlbumArt(song.getPath());
            runOnUiThread(() -> {
                if (albumArt != null) {
                    ivMiniAlbumArt.setImageBitmap(albumArt);
                } else {
                    ivMiniAlbumArt.setImageResource(R.drawable.ic_music);
                }
            });
        }).start();
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

    private void showMiniPlayer() {
        if (isMiniPlayerVisible) return;
        isMiniPlayerVisible = true;

        miniPlayer.setVisibility(View.VISIBLE);
        TranslateAnimation animation = new TranslateAnimation(0, 0, miniPlayer.getHeight(), 0);
        animation.setDuration(300);
        miniPlayer.startAnimation(animation);
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            btnMiniPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            btnMiniPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rebind to service if it's running
        rescanMusicFolder();
        if (!isBound) {
            Intent intent = new Intent(this, MusicService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void rescanMusicFolder() {
        File musicDir = new File(Environment.getExternalStorageDirectory(), "Music");
        if (musicDir.exists()) {
            File[] files = musicDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    MediaScannerConnection.scanFile(this,
                            new String[]{file.getAbsolutePath()},
                            null,
                            (path, uri) -> {
                                Log.d("MusicScan", "Scanned: " + path);
                                runOnUiThread(this::loadSongs); // Gọi lại loadSongs sau khi quét
                            });
                }
            }
        }
    }



}