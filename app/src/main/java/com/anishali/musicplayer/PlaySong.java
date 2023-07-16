package com.anishali.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity{
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        updateSeek.interrupt();
    }

    TextView textView;
    ImageView pause, previous, next;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    SeekBar seekBar;
    Thread updateSeek;
    boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView = findViewById(R.id.textView);
        previous = findViewById(R.id.previous);
        pause = findViewById(R.id.pause);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);

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

        updateSeek = new Thread()
        {
            @Override
            public void run() {
                int currentPosition = 0;

                try {
                    while(currentPosition < mediaPlayer.getDuration())
                    {
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        sleep(400);
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        updateSeek.start();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {
                    paused = true;
                    mediaPlayer.pause();
                    pause.setImageResource(R.drawable.play);
                }
                else
                {
                    mediaPlayer.start();
                    paused = false;
                    pause.setImageResource(R.drawable.pause);
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0) position = songs.size()-1;
                else position = position - 1;

                textContent = songs.get(position).getName();
                textView.setText(textContent.replace(".mp3", ""));

                seekBar.setProgress(0);
                mediaPlayer.stop();

                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                if(!paused) mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == songs.size()-1) position = 0;
                else position = position + 1;

                textContent = songs.get(position).getName();
                textView.setText(textContent.replace(".mp3", ""));
                seekBar.setProgress(0);
                mediaPlayer.stop();

                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                if(!paused) mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(position == songs.size()-1) position = 0;
                else position = position + 1;

                textContent = songs.get(position).getName();
                textView.setText(textContent.replace(".mp3", ""));
                seekBar.setProgress(0);
                mp.stop();

                Uri uri = Uri.parse(songs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(), uri);
                if(!paused) mp.start();
                seekBar.setMax(mp.getDuration());
            }
        });
    }
}