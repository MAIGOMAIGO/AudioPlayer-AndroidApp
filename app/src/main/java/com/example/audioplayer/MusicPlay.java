package com.example.audioplayer;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MusicPlay extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private Button playBtn;
    private SeekBar positionBar;
    private SeekBar volumeBar;
    private TextView elapsedTimeLabel;
    private TextView remainingTimeLabel;
    private TextView textView;
    private ImageView RepeatBtn;
    MediaPlayer mp = new MediaPlayer();
    private int totalTime;
    AudioManager audioManager;
    int index=0;
    long[] music;
    Uri contentUri;
    String NowMusicTitle;
    String[] TitleList;
    Intent intent;
    int flg;
    Boolean Loop;
    Boolean AllLoop;
    TextView LoopNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        playBtn = findViewById(R.id.playBtn);
        elapsedTimeLabel = findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = findViewById(R.id.remainingTimeLabel);
        textView = findViewById(R.id.selected_title);
        RepeatBtn = findViewById(R.id.repeatBtn);
        LoopNow = findViewById(R.id.LoopNow);

        //インテントの取得
        intent = getIntent();

        //AudioFocusを扱うためのManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //音楽リスト
        Bundle extras = getIntent().getExtras();
        music = extras.getLongArray("Music");
        TitleList = extras.getStringArray("TITLE");
        index = getIntent().getIntExtra("index",0);

        // Media Playerの初期化
        flg = 0;
        Loop = false;
        AllLoop = false;
        mp.setVolume(0.5f, 0.5f);
        MusicSet(index);

    }

    public void MusicSet(int index){
        mp.setLooping(Loop);
        //音楽をセッティングする
        contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,music[index]);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(getApplicationContext(), contentUri);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this,
                    "例外が発生、SETする曲がないです", Toast.LENGTH_SHORT);
            toast.show();
        }
        NowMusicTitle =TitleList[index];
        textView.setText(NowMusicTitle);
        mp.seekTo(0);
        totalTime = mp.getDuration();
        mp.setOnCompletionListener(this);
        //オーディオフォーカスを要求
        if(audioManager.requestAudioFocus(afChangeListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //取得出来たら再生
            mp.start();
            playBtn.setBackgroundResource(R.drawable.stop);
        }

        // 再生位置
        positionBar = findViewById(R.id.positionBar);
        positionBar.setMax(totalTime);
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mp.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        // 音量調節
        volumeBar = findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        mp.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
        // Thread (positionBar・経過時間ラベル・残り時間ラベルを更新する)
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }

    //定期的に回す
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int currentPosition = msg.what;
            // 再生位置を更新
            positionBar.setProgress(currentPosition);

            // 経過時間ラベル更新
            String elapsedTime = createTimeLabel(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);

            // 全体時間ラベル
            remainingTimeLabel.setText(createTimeLabel(totalTime));

            return true;
        }
    });

    //時間ラベルの表示 msを計算
    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    public void playBtnClick(View view) {
        if (!mp.isPlaying()) {
            // 停止中
            //オーディオフォーカスを要求
            if(audioManager.requestAudioFocus(afChangeListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                //取得出来たら再生
                mp.start();
                playBtn.setBackgroundResource(R.drawable.stop);
            }

        } else {
            // 再生中
            mp.pause();
            //オーディオフォーカスを解放
            audioManager.abandonAudioFocus(afChangeListener);
            playBtn.setBackgroundResource(R.drawable.play);
        }
    }

    public void nextSkipClick(View view){
        index++;
        if(index >= music.length)
            index=0;
        mp.stop();
        mp.reset();
        MusicSet(index);
    }

    public void backSkipClick(View view){
        index--;
        if (index < 0)
            index = music.length-1;
        mp.stop();
        mp.reset();
        MusicSet(index);
    }

    public void RepeatBtnClick(View view){
        if(flg == 0){
            //onlyLoop
            Loop = true;
            AllLoop = false;
            flg = 1;
            mp.setLooping(Loop);
            RepeatBtn.setBackgroundResource(R.drawable.onlyrepeat);
            LoopNow.setText("OnlyRepeat");
        }else if(flg == 1){
            //AllLoop
            Loop = false;
            AllLoop = true;
            flg = 2;
            mp.setLooping(Loop);
            RepeatBtn.setBackgroundResource(R.drawable.repeat);
            LoopNow.setText("Repeat");
        }else{
            //Loop無し
            Loop = false;
            AllLoop = false;
            flg = 0;
            mp.setLooping(Loop);
            LoopNow.setText("NoLoop");
            RepeatBtn.setBackgroundResource(R.drawable.image);
        }
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            //フォーカスを完全に失ったら
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                //止める
                mp.pause();
                audioManager.abandonAudioFocus(afChangeListener);
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {//一時的なフォーカスロスト

                //止める
                mp.pause();
                audioManager.abandonAudioFocus(afChangeListener);
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {//通知音とかによるフォーカスロスト（ボリュームを下げて再生し続けるべき）
                //本来なら音量を一時的に下げるべきだが何もしない
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {//フォーカスを再度得た場合
                //再生
                mp.start();
            }
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        index++;
        if(index >= music.length) {
            if(AllLoop) {
                index = 0;
                mp.stop();
                mp.reset();
                MusicSet(index);
            }else{
                mp.stop();
                mp.reset();
                finish();
            }
        }else{
            mp.stop();
            mp.reset();
            MusicSet(index);
        }
    }

    @Override
    public void onBackPressed(){
        intent.setClass(MusicPlay.this,MainActivity.class);
        startActivity(intent);;
    }

}