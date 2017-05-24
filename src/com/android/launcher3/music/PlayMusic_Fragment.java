package com.android.launcher3.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.app.Fragment;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.launcher3.R;

import java.util.List;
import java.util.Map;

/**
 * Created by huachun.li on 2016/10/31.
 */
public class PlayMusic_Fragment extends Fragment {
    private View customer_view = null;
    ImageButton imageBtnRewind = null;
    ImageButton imageBtnPlay = null;
    ImageButton imageBtnForward = null;
    TextView startTime = null;
    TextView endTime = null;
    TextView currentMusic = null;
    TextView allMusic = null;
    TextView musicName = null;
    TextView musicSinger = null;
    SeekBar seekBar;
    private Intent intent;
    private MusicPlayer receiver;
    private List<Music> musiclist;
    private MusicPreference service;
    private Tool toTime;
    Close close;
    private int id = 0;
    private int progress = 0;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        customer_view = inflater.inflate(R.layout.music_main, container, false);
        initView();
        buttonclick();

        return customer_view;
    }

    private void initView() {
        imageBtnRewind = (ImageButton) customer_view.findViewById(R.id.music_rewind);
        imageBtnPlay = (ImageButton) customer_view.findViewById(R.id.music_play);
        imageBtnForward = (ImageButton) customer_view.findViewById(R.id.music_foward);
        startTime = (TextView) customer_view.findViewById(R.id.music_start_time);
        endTime = (TextView) customer_view.findViewById(R.id.music_end_time);
        currentMusic = (TextView) customer_view.findViewById(R.id.currentmusicId);
        allMusic = (TextView) customer_view.findViewById(R.id.allmusic);
        musicName = (TextView) customer_view.findViewById(R.id.music_name);
        musicSinger = (TextView) customer_view.findViewById(R.id.music_singer);
        seekBar = (SeekBar) customer_view.findViewById(R.id.music_seekBar);
    }

    private void buttonclick() {
        imageBtnRewind.setOnClickListener(new MyListener());
        imageBtnPlay.setOnClickListener(new MyListener());
        imageBtnForward.setOnClickListener(new MyListener());
    }

    private void register() {
        receiver = new MusicPlayer();
        IntentFilter filter = new IntentFilter(
                "com.example.musicserviceplayer");
        getActivity().registerReceiver(receiver, filter);

        close = new Close();
        IntentFilter filter2 = new IntentFilter("com.sleep.close");
        getActivity().registerReceiver(close, filter2);
    }

    @Override
    public void onStart() {
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
    public void onResume() {
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
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        register();
        service = new MusicPreference(getActivity());
        intent = new Intent(getActivity(), PlayMusicService.class);
        musiclist = MusicList.getMusicData(getActivity());
        toTime = new Tool();
        allMusic.setText(String.valueOf(musiclist.size()));
    }

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

    public class Close extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getActivity().finish();
        }
    }

    private class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == imageBtnRewind) { // ��һ��
                intent.putExtra("mode", 1);
                getActivity().startService(intent);
            } else if (v == imageBtnPlay) {
                if (PlayMusicService.mPlayer != null
                        && PlayMusicService.playflag) {
                    imageBtnPlay.setBackgroundResource(R.drawable.play1);
                } else {
                    imageBtnPlay.setBackgroundResource(R.drawable.pause1);
                }
                intent.putExtra("mode", 0);
                getActivity().startService(intent);
            } else if (v == imageBtnForward) {
                intent.putExtra("mode", 2);
                getActivity().startService(intent);
            }
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(receiver);
       getActivity().unregisterReceiver(close);
        super.onDestroy();
    }

}
