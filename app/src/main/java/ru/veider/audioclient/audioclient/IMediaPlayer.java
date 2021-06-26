package ru.veider.audioclient.audioclient;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.ImageButton;

public interface IMediaPlayer {
    String createTimeLabel(int time);
    void showToast(String message);

    void bookCover();
}
