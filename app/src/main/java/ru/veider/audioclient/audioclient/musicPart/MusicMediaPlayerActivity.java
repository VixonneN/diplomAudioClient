package ru.veider.audioclient.audioclient.musicPart;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.veider.audioclient.audioclient.IMediaPlayer;
import ru.veider.audioclient.audioclient.R;
import ru.veider.audioclient.audioclient.data.Api;
import ru.veider.audioclient.audioclient.data.Film;
import ru.veider.audioclient.audioclient.data.SearchResponse;
import ru.veider.audioclient.audioclient.musicPart.MusicLibraryActivity;
import ru.veider.audioclient.audioclient.recycler.MediaModel;
import ru.veider.audioclient.audioclient.service.PlayerService;
import ru.veider.audioclient.audioclient.storage.AudioLibrary;

public class MusicMediaPlayerActivity extends AppCompatActivity implements IMediaPlayer {

    private static final String EXTRA_POS = "position";

    private SeekBar mPositionBar;
    private TextView mCurrentTime, mFullTime, bookName;
    private MediaPlayer mediaPlayer;
    private ImageButton mPlayButton, mPauseButton, mNextButton, mBackButton;
    private ImageView mCoverView;
    private MediaModel mediaModel;

    private MusicPlayerService.MusicPlayerServiceBinder playerServiceBinder;
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback callback;
    private ServiceConnection serviceConnection;

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_media_player);

        mCoverView = findViewById(R.id.music_Book);
        mCurrentTime = findViewById(R.id.music_Current_time);
        mPlayButton = findViewById(R.id.music_playButton);
        mPauseButton = findViewById(R.id.music_pauseButton);
        mNextButton = findViewById(R.id.music_nextButton);
        mBackButton = findViewById(R.id.music_backButton);
        mFullTime = findViewById(R.id.music_Full_time);
        bookName = findViewById(R.id.music_Name);
        mPositionBar = findViewById(R.id.music_seekBar);

        Intent intent = getIntent();
        position = intent.getIntExtra(EXTRA_POS, 0);
        mediaModel = MusicLibraryActivity.repository.get(position);
        mediaPlayer = MediaPlayer.create(this, Uri.parse(mediaModel.getUri()));

        int totalTime = mediaPlayer.getDuration();
        mPositionBar.setMax(totalTime);

        startServicePlay();
    }


    private void startServicePlay(){
        callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if (state == null)
                    return;
                boolean playing = state.getState() == PlaybackStateCompat.STATE_PLAYING;
                mPlayButton.setEnabled(!playing);
            }
        };

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                playerServiceBinder = (MusicPlayerService.MusicPlayerServiceBinder) service;
                try {
                    mediaController = new MediaControllerCompat(MusicMediaPlayerActivity.this, playerServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(callback);
                    callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                }
                catch (RemoteException e) {
                    mediaController = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                playerServiceBinder = null;
                if (mediaController != null) {
                    mediaController.unregisterCallback(callback);
                    mediaController = null;
                }
            }
        };

        bindService(new Intent(this, MusicPlayerService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bookName.setText(mediaModel.getName());

        buttonsAndSeekBar();
    }

    private void buttonsAndSeekBar() {
        //play
        mPlayButton.setOnClickListener(v -> {
            if (mediaController != null)
                mediaController.getTransportControls().play();

            mediaPlayer.start();
            mediaPlayer.setVolume(0.0f, 0.0f);
            mPauseButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.INVISIBLE);
        });

        //pause
        mPauseButton.setOnClickListener(v -> {
            if (mediaController != null)
                mediaController.getTransportControls().pause();

            mediaPlayer.pause();
            mPauseButton.setVisibility(View.INVISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);
        });

        //next
        mNextButton.setOnClickListener(v ->{
            if (mediaController != null)
                mediaController.getTransportControls().skipToNext();
            mediaModel = AudioLibrary.repository.get(position + 1);

            mediaPlayer = MediaPlayer.create(this, Uri.parse(mediaModel.getUri()));
            mediaPlayer.start();
            mediaPlayer.setVolume(0.0f, 0.0f);
        });

        //previous
        mBackButton.setOnClickListener(v -> {
            if (mediaController != null)
                mediaController.getTransportControls().skipToPrevious();

            mediaModel = AudioLibrary.repository.get(position - 1);

            mediaPlayer = MediaPlayer.create(this, Uri.parse(mediaModel.getUri()));
            mediaPlayer.start();
            mediaPlayer.setVolume(0.0f, 0.0f);

        });

        //seek bar
        mPositionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        mPlayButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(mPositionBar.getProgress());
            }
        });

        //update seek bar
        new Thread(() -> {
            while (mediaPlayer != null){
                try {
                    Message message = new Message();
                    message.what = mediaPlayer.getCurrentPosition();//временами баг
                    handler.sendMessage(message);

                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    showToast("InterruptedException" + e);
                }
            }
        }).start();

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            //Update mPositionBar
            mPositionBar.setProgress(currentPosition);

            //Update Labels
            String elapsedTime = createTimeLabel(currentPosition);
            mCurrentTime.setText(elapsedTime);

            int intFullTime = mediaPlayer.getDuration();
            String fullTime = createTimeLabel(intFullTime);
            mFullTime.setText(fullTime);
        }
    };

    //вытягивание обложки из Google Play books

    //Вывод тостов
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void bookCover() {

    }

    //Время
    public String createTimeLabel(int time) {
        String timeLabel;
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) {
            timeLabel += "0";
        }
        timeLabel += sec;
        return timeLabel;
    }

    public static void start(Context context, int position) {
        context.startActivity(
                new Intent(context, MusicMediaPlayerActivity.class).putExtra(EXTRA_POS, position));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
//        handler.removeCallbacks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerServiceBinder = null;
        if (mediaController != null) {
            mediaController.unregisterCallback(callback);
            mediaController = null;
        }
        unbindService(serviceConnection);
    }
}