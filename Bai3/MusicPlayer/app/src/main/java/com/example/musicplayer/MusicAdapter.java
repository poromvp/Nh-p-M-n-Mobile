package com.example.musicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private List<Song> songs;
    private OnSongClickListener listener;
    private int currentPlayingPosition = -1;

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
    }

    public MusicAdapter(List<Song> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song, position);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setCurrentPlayingPosition(int position) {
        int oldPosition = currentPlayingPosition;
        currentPlayingPosition = position;
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
        if (currentPlayingPosition != -1) {
            notifyItemChanged(currentPlayingPosition);
        }
    }

    class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist, tvDuration;
        ImageView ivAlbumArt, ivPlaying;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            ivAlbumArt = itemView.findViewById(R.id.ivAlbumArt);
            ivPlaying = itemView.findViewById(R.id.ivPlaying);
        }

        public void bind(Song song, int position) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            tvDuration.setText(song.getFormattedDuration());

            // Load album art
            loadAlbumArt(song.getPath());

            // Highlight currently playing song
            if (position == currentPlayingPosition) {
                ivPlaying.setVisibility(View.VISIBLE);
                itemView.setBackgroundColor(0x1A4CAF50); // Light green tint
            } else {
                ivPlaying.setVisibility(View.GONE);
                itemView.setBackgroundColor(0x00000000); // Transparent
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSongClick(song, position);
                }
            });
        }

        private void loadAlbumArt(String path) {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(path);
                byte[] art = retriever.getEmbeddedPicture();
                if (art != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                    ivAlbumArt.setImageBitmap(bitmap);
                } else {
                    ivAlbumArt.setImageResource(R.drawable.ic_music);
                }
                retriever.release();
            } catch (Exception e) {
                ivAlbumArt.setImageResource(R.drawable.ic_music);
            }
        }
    }
}