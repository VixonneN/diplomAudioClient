package ru.veider.audioclient.audioclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import ru.veider.audioclient.audioclient.musicPart.MusicLibraryActivity;
import ru.veider.audioclient.audioclient.storage.AudioLibrary;

public class MainActivity extends AppCompatActivity {

    private Button toBookBtn, toMusicBtn;
    private Intent toBooksIntent, toMusicIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toBookBtn = findViewById(R.id.toBooksBtn);
        toMusicBtn = findViewById(R.id.toMusicBtn);
        btns();
    }

    private void btns(){
     toBookBtn.setOnClickListener(v -> {
        toBooksIntent = new Intent(this, AudioLibrary.class);
        startActivity(toBooksIntent);
     });

     toMusicBtn.setOnClickListener(v -> {
         toMusicIntent = new Intent(this, MusicLibraryActivity.class);
         startActivity(toMusicIntent);
     });
    }
}