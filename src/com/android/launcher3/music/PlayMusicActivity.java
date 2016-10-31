package com.android.launcher3.music;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.launcher3.R;

public class PlayMusicActivity extends Activity {

    private ImageButton imageBtnRewind;
    private ImageButton imageBtnForward;
    private ImageButton imageBtnPlay;
    private TextView startTime;
    private TextView endTime;
    private TextView currentMusic;
    private TextView allMusic;
    private TextView musicName;
    private TextView musicSinger;
    private SeekBar seekBar;
    private Intent intent;
    private List<Music> musiclist;
    private int id = 0;
    private int progress = 0;
    private MusicPreference service;
    private Tool toTime;
    private MusicPlayer receiver;
    private Close close;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_main);

        initView();

        register();

        buttonclick();
        service = new MusicPreference(this);
        intent = new Intent(PlayMusicActivity.this, PlayMusicService.class);
        musiclist = MusicList.getMusicData(this);
        toTime = new Tool();
        allMusic.setText(String.valueOf(musiclist.size()));
    }

    private void buttonclick() {
        imageBtnRewind.setOnClickListener(new MyListener());
        imageBtnPlay.setOnClickListener(new MyListener());
        imageBtnForward.setOnClickListener(new MyListener());
    }

    private void initView() {
        imageBtnRewind = (ImageButton) this.findViewById(R.id.music_rewind);
        imageBtnPlay = (ImageButton) this.findViewById(R.id.music_play);
        imageBtnForward = (ImageButton) this.findViewById(R.id.music_foward);
        startTime = (TextView) this.findViewById(R.id.music_start_time);
        endTime = (TextView) this.findViewById(R.id.music_end_time);
        currentMusic = (TextView) this.findViewById(R.id.currentmusicId);
        allMusic = (TextView) this.findViewById(R.id.allmusic);
        musicName = (TextView) this.findViewById(R.id.music_name);
        musicSinger = (TextView) this.findViewById(R.id.music_singer);
        seekBar = (SeekBar) this.findViewById(R.id.music_seekBar);
    }

    @Override
    protected void onStart() {

        Map<String, String> params = service.getMusic();
        Map<String, String> params3 = service.getProgress();
        if (PlayMusicService.mPlayer == null) {
            id = Integer.valueOf(params.get("currentId"));
            progress = Integer.valueOf(params3.get("progress"));
            String time = params3.get("time");
            try {
                Music m = musiclist.get(id);
                seekBar.setProgress((int) (progress * 1000 / m.getTime()));
                startTime.setText(time);
                if (musiclist.size() > 0) {
                    currentMusic.setText(String.valueOf(id + 1));
                } else {
                    currentMusic.setText("0");
                }
                musicName.setText(m.getTitle());
                if (m.getSinger().equals("δ֪������")) {
                    musicSinger.setText("music");
                } else {
                    musicSinger.setText(m.getSinger());
                }
                endTime.setText(toTime.toTime((int) m.getTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (PlayMusicService.playflag) {
            Music m = musiclist.get(PlayMusicService.last_song);
            endTime.setText(toTime.toTime((int) m.getTime()));
            musicName.setText(m.getTitle());
            if (m.getSinger().equals("δ֪������")) {
                musicSinger.setText("music");
            } else {
                musicSinger.setText(m.getSinger());
            }
            if (musiclist.size() > 0)
                currentMusic.setText(String
                        .valueOf(PlayMusicService.last_song + 1));
            else
                currentMusic.setText("0");
            imageBtnPlay.setBackgroundResource(R.drawable.pause1);
        } else {
            Music m = musiclist.get(PlayMusicService.last_song);
            seekBar.setProgress((int) (PlayMusicService.mPlayer
                    .getCurrentPosition() * 1000 / PlayMusicService.mPlayer
                    .getDuration()));
            startTime.setText(toTime.toTime(PlayMusicService.mPlayer
                    .getCurrentPosition()));
            if (musiclist.size() > 0)
                currentMusic
                        .setText(String.valueOf(PlayMusicService.last_song));
            else
                currentMusic.setText("0");
            musicName.setText(m.getTitle());
            if (m.getSinger().equals("unknown")) {
                musicSinger.setText("music");
            } else {
                musicSinger.setText(m.getSinger());
            }
            endTime.setText(toTime.toTime((int) PlayMusicService.mPlayer
                    .getDuration()));
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable, 100);
        super.onResume();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(runnable, 100);
            if (PlayMusicService.mPlayer != null) {
                seekBar.setProgress((int) (PlayMusicService.mPlayer
                        .getCurrentPosition() * 1000 / PlayMusicService.mPlayer
                        .getDuration()));
                seekBar.invalidate();
                startTime.setText(toTime.toTime((int) PlayMusicService.mPlayer
                        .getCurrentPosition()));
            }
        }
    };

    private void register() {
        receiver = new MusicPlayer();
        IntentFilter filter = new IntentFilter(
                "com.example.musicserviceplayer");
        this.registerReceiver(receiver, filter);

        close = new Close();
        IntentFilter filter2 = new IntentFilter("com.sleep.close");
        this.registerReceiver(close, filter2);
    }

    // prepare��ɺ�
    public class MusicPlayer extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Music m = musiclist.get(PlayMusicService.last_song);
            endTime.setText(toTime.toTime((int) m.getTime()));
            musicName.setText(m.getTitle());
            if (m.getSinger().equals("δ֪������")) {
                musicSinger.setText("music");
            } else {
                musicSinger.setText(m.getSinger());
            }
            if (musiclist.size() > 0)
                currentMusic.setText(String
                        .valueOf(PlayMusicService.last_song + 1));
            else
                currentMusic.setText("0");
            imageBtnPlay.setBackgroundResource(R.drawable.pause1);
        }
    }

    // ����������
    private class MyListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == imageBtnRewind) { // ��һ��
                intent.putExtra("mode", 1);
                startService(intent);
            } else if (v == imageBtnPlay) {
                if (PlayMusicService.mPlayer != null
                        && PlayMusicService.playflag) {
                    imageBtnPlay.setBackgroundResource(R.drawable.play1);
                } else {
                    imageBtnPlay.setBackgroundResource(R.drawable.pause1);
                }
                intent.putExtra("mode", 0);
                startService(intent);
            } else if (v == imageBtnForward) {
                intent.putExtra("mode", 2);
                startService(intent);
            }
        }
    }


    @Override
    protected void onDestroy() {
        this.unregisterReceiver(receiver);
        this.unregisterReceiver(close);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            dialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                PlayMusicActivity.this);
        builder.setMessage("退出后继续播放音乐吗？");

        builder.setTitle("提示");
        builder.setPositiveButton("听啦",
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setNegativeButton("不听啦",
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent("com.sleep.close");
                        sendBroadcast(intent);
                        finish();
                    }
                });
        builder.create().show();
    }

    public class Close extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }
}
