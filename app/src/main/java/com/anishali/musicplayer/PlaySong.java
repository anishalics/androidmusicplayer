package com.anishali.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PlaySong extends AppCompatActivity {
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUpdateSeek();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    TextView textView;
    ImageView pause, previous, next;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    SeekBar seekBar;
    SeekBar volumeSeekBar;
    Thread updateSeek;
    boolean paused = false;
    AudioManager audioManager;
    ImageView albumArtImageView;
    ImageView singing;
    boolean isSinging = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView = findViewById(R.id.textView);
        previous = findViewById(R.id.previous);
        pause = findViewById(R.id.pause);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);
        albumArtImageView = findViewById(R.id.imageView);
        singing = findViewById(R.id.singing);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songs = (ArrayList) bundle.getParcelableArrayList("songList");
        textContent = intent.getStringExtra("currentSong");
        textView.setText(textContent);
        textView.setSelected(true);

        position = intent.getIntExtra("position", 0);

        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration());

        setArtistName(uri);

        startUpdateSeek();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) mediaPlayer.seekTo(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Get the maximum volume level
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // Get the current volume level
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        // Set the seekbar maximum value to the maximum volume level
        volumeSeekBar.setMax(maxVolume);

        // Set the current volume level as the initial progress of the seekbar
        volumeSeekBar.setProgress(currentVolume);

        // Set a listener to track volume changes
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the volume level when the seekbar is changed by the user
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                isSinging = true;
                singing.setImageResource(R.drawable.volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Empty implementation required by the interface
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Empty implementation required by the interface
            }
        });

        setImageView(uri);

        singing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSinging)
                {
                    isSinging = false;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    singing.setImageResource(R.drawable.mute);
                }
                else
                {
                    isSinging = true;
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeSeekBar.getProgress(), 0);
                    singing.setImageResource(R.drawable.volume);
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    stopUpdateSeek();
                    paused = true;
                    mediaPlayer.pause();
                    pause.setImageResource(R.drawable.play_circled);
                } else {
                    mediaPlayer.start();
                    paused = false;
                    pause.setImageResource(R.drawable.pause_circled);
                    startUpdateSeek();
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopUpdateSeek();

                if (position == 0) position = songs.size() - 1;
                else position = position - 1;

                textContent = songs.get(position).getName();
                textView.setText(textContent.replace(".mp3", ""));
                seekBar.setProgress(0);
                mediaPlayer.stop();

                try {
                    mediaPlayer.reset();
                    Uri uri = Uri.parse(songs.get(position).toString());
                    setImageView(uri);
                    setArtistName(uri);
                    mediaPlayer.setDataSource(getApplicationContext(), uri);
                    mediaPlayer.prepare();

                    if (!paused)
                        mediaPlayer.start();

                    seekBar.setMax(mediaPlayer.getDuration());
                    startUpdateSeek();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopUpdateSeek();
                playNextSong();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopUpdateSeek();
                playNextSong();
            }
        });
    }

    private void playNextSong() {
        position = (position + 1) % songs.size();
        textContent = songs.get(position).getName();
        textView.setText(textContent.replace(".mp3", ""));
        seekBar.setProgress(0);
        mediaPlayer.stop();

        try {
            mediaPlayer.reset();
            Uri uri = Uri.parse(songs.get(position).toString());
            setImageView(uri);
            setArtistName(uri);
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();

            if (!paused)
                mediaPlayer.start();

            seekBar.setMax(mediaPlayer.getDuration());
            startUpdateSeek();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startUpdateSeek() {
        updateSeek = new Thread() {
            @Override
            public void run() {
                try {
                    while (mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        sleep(400);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        updateSeek.start();
    }

    private void stopUpdateSeek() {
        if (updateSeek != null && updateSeek.isAlive()) {
            updateSeek.interrupt();
            updateSeek = null;
        }
    }

    private void setImageView(Uri uri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, uri);

            // Extract album art as a byte array from the metadata
            byte[] albumArtByteArray = retriever.getEmbeddedPicture();

            if (albumArtByteArray != null) {
                // Convert the byte array to a Bitmap and set it to the ImageView
                Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArtByteArray, 0, albumArtByteArray.length);
                albumArtImageView.setImageBitmap(albumArtBitmap);
            } else {
                // Set a default image or handle the case where no album art is available
                albumArtImageView.setImageResource(R.drawable.logo);
            }

            retriever.release();
        } catch (Exception e) {
            albumArtImageView.setImageResource(R.drawable.logo);
            e.printStackTrace();
        }
    }

    public void setArtistName(Uri uri)
    {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, uri);

            // Extract the artist name from the metadata
            String artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            if (artistName != null && !artistName.isEmpty()) {
                // Set the artist name to the TextView
                Objects.requireNonNull(getSupportActionBar()).setTitle(artistName);
            } else {
                // Handle the case where artist name is not available
                Objects.requireNonNull(getSupportActionBar()).setTitle("Unknown Artist");
            }

            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}