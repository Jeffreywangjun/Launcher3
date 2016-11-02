package com.android.launcher3.music;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.launcher3.R;
public class PlayMusicService extends Service implements Runnable {

	private List<Music> musiclist;
	public static int last_song = 0;
	private int progress = 0;
	public static MediaPlayer mPlayer;
	public static boolean playflag = false;
	private MusicPreference service;
	private Tool toTime;
	private Bitmap bitmap = null;
	private Close close;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		musiclist = MusicList.getMusicData(getApplicationContext());
		service = new MusicPreference(this);
		toTime = new Tool();
		register();
		new Thread(this).start();
		super.onCreate();
	}

	private void register() {
		close = new Close();
		IntentFilter filter22 = new IntentFilter("com.sleep.close");
		this.registerReceiver(close, filter22);
	}

	@Override
	public void onDestroy() {
		if (mPlayer != null) {
			try {
				String time = toTime.toTime((int) mPlayer.getCurrentPosition());
				service.saveProgress(time,
						Integer.valueOf((int) mPlayer.getCurrentPosition()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			Music mz = musiclist.get(last_song);
			String musicna = mz.getName();
			service.savename(musicna, Integer.valueOf(last_song));
		}
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
		playflag = false;
		this.unregisterReceiver(close);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int mode = intent.getIntExtra("mode", 0);
		Log.d("onStartCommand", String.valueOf(mode));
		if (0 == mode) {
			if (playflag) {// ��ͣ
				playflag = false;
				if (null != mPlayer) {
					mPlayer.pause();
					widgetstop();
				}
				try {
					Music m = musiclist.get(last_song);
					String Name = m.getName();
					service.saveMusic(Name, Integer.valueOf(last_song));
					service.saveProgress(
							toTime.toTime(mPlayer.getCurrentPosition()),
							Integer.valueOf(mPlayer.getCurrentPosition()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {// ����
				if (mPlayer != null) { // ��������
					mPlayer.start();
					playflag = true;
				} else if (mPlayer == null) { // ��ȡ���Ž��Ȳ���
					Map<String, String> progressMap = service.getProgress();
					progress = Integer.valueOf(progressMap.get("progress"));
					Map<String, String> params = service.getMusic();
					last_song = (Integer.valueOf(params.get("currentId")));

					if (last_song >= musiclist.size() - 1) {
						last_song = (musiclist.size() - 1);
					}
					Log.d("progress", "" + String.valueOf(progress));
					Log.e("last_song", "last_song" + last_song);
try {
	playMusic(last_song);



					mPlayer.seekTo(progress);}
					catch (Exception e){


					}
				}
				playflag = true;
				try {
				widgetplay();}
				catch (Exception e){
					Toast.makeText(this,"本地没歌曲！！！",Toast.LENGTH_LONG).show();
				}
			}
		} else if (1 == mode) {
			last_song = (last_song + 1);
			if (last_song > musiclist.size() - 1) {
				last_song = 0;
			}
			playMusic(last_song);
			widgetUpdate();
		} else if (2 == mode) {
			last_song = (last_song - 1);
			if (last_song < 0) {
				last_song = (musiclist.size() - 1);
			}
			playMusic(last_song);
			widgetUpdate();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void playMusic(int song) {
		if (null != mPlayer) {
			try {
				mPlayer.release();
			}
			catch (ArrayIndexOutOfBoundsException e){
				

			}

			mPlayer = null;
		}

	Music m = musiclist.get(song);

		Intent intent = new Intent("com.example.musicserviceplayer");
		sendBroadcast(intent);
		if(song==-1){
			stopService(intent);
			Toast.makeText(this,"stop",Toast.LENGTH_LONG).show();
		}
		String url = m.getUrl();
		Uri myUri = Uri.parse(url);
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mPlayer.setDataSource(getApplicationContext(), myUri);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mPlayer.setOnCompletionListener(new OnCompletionListener());
		mPlayer.setOnErrorListener(new OnErrorListener());

	}

	private class OnCompletionListener implements
			android.media.MediaPlayer.OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer arg0) {
			last_song = (last_song + 1);
			if (last_song > musiclist.size() - 1) {
				last_song = (0);
			}
			playMusic(last_song);
		}
	}

	private final class OnErrorListener implements
			android.media.MediaPlayer.OnErrorListener {
		@Override
		public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
			if (null != mPlayer) {
				mPlayer.release();
				mPlayer = null;
			}
			mPlayer = new MediaPlayer();
			Music m = musiclist.get(last_song);
			String url = m.getUrl();
			Uri myUri = Uri.parse(url);
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mPlayer.setDataSource(getApplicationContext(), myUri);
				mPlayer.prepare();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	public class Close extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (mPlayer != null) {
					String time = toTime.toTime(mPlayer.getCurrentPosition());
					service.saveProgress(time,
							Integer.valueOf(mPlayer.getCurrentPosition()));
					widgetstop();
					mPlayer.release();
					mPlayer = null;
				}
				Music mz = musiclist.get(last_song);
				String musicna = mz.getName();
				service.savename(musicna, Integer.valueOf(last_song));

				String Name = mz.getName();
				service.saveMusic(Name, Integer.valueOf(last_song));
			} catch (Exception e) {
				e.printStackTrace();
			}
			playflag = false;
		}
	}

	private void widgetplay() {
		RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
				R.layout.music_appwidget);

		Music m = musiclist.get(last_song);

		Map<String, String> paramsui = service.getPreferencesback();
		Uri uri = Uri.parse(paramsui.get("background"));
		ContentResolver contentResolver = this.getContentResolver();
		if (String.valueOf(uri).length() > 4) {
			try {

				bitmap = BitmapFactory.decodeStream(contentResolver
						.openInputStream(uri));
				int h = bitmap.getHeight();
				remoteViews.setImageViewBitmap(R.id.widget_logo,
						RoundCorner.toRoundCorner(bitmap, h / 34));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		String sbr = m.getName().substring(0, m.getName().length() - 4);
		remoteViews.setTextViewText(R.id.widgettitlemain, sbr);
		if (m.getSinger().equals("unknown")) {
			remoteViews.setTextViewText(R.id.widgetsinger, "");
		} else {
			remoteViews.setTextViewText(R.id.widgetsinger, m.getSinger());
		}
		remoteViews.setImageViewResource(R.id.widget_startBtn,
				R.drawable.widget_pause);
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getBaseContext());
		ComponentName componentName = new ComponentName(getBaseContext(),
				MusicAppWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
	}

	public void widgetstop() {
		RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
				R.layout.music_appwidget);
		remoteViews.setImageViewResource(R.id.widget_startBtn,
				R.drawable.widget_play);
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getBaseContext());
		ComponentName componentName = new ComponentName(getBaseContext(),
				MusicAppWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
	}

	public void widgetUpdate() {
		RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
				R.layout.music_appwidget);
		Music m = musiclist.get(last_song);
		Map<String, String> paramsui = service.getPreferencesback();
		Uri uri = Uri.parse(paramsui.get("background"));
		ContentResolver contentResolver = this.getContentResolver();
		if (String.valueOf(uri).length() > 4) {
			try {

				bitmap = BitmapFactory.decodeStream(contentResolver
						.openInputStream(uri));
				int h = bitmap.getHeight();
				remoteViews.setImageViewBitmap(R.id.widget_logo,
						RoundCorner.toRoundCorner(bitmap, h / 34));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		String sbr = m.getName().substring(0, m.getName().length() - 4);
		remoteViews.setTextViewText(R.id.widgettitlemain, sbr);
		if (m.getSinger().equals("unknown")) {
			remoteViews.setTextViewText(R.id.widgetsinger, "");
		} else {
			remoteViews.setTextViewText(R.id.widgetsinger, m.getSinger());
		}
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(getBaseContext());
		ComponentName componentName = new ComponentName(getBaseContext(),
				MusicAppWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
	}

	@Override
	public void run() {
		while (1 > 0) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long current = 0;
			long total = 0;
			int degree = 0;
			try {
				current = mPlayer.getCurrentPosition();
				total = mPlayer.getDuration();
				degree = (int) (current * 360 / total);
			} catch (Exception e) {
				e.printStackTrace();
			}
			RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
					R.layout.music_appwidget);
			if (degree >= 360) {
				degree = 360;
			}
			try {
				Xuanzhuan xuanzhuan = new Xuanzhuan(PlayMusicService.this);
				remoteViews.setImageViewBitmap(R.id.thumb2,
						xuanzhuan.rotate(degree));
				remoteViews.setImageViewBitmap(
						R.id.icon_panel_progress_barleft,
						xuanzhuan.rotate2(degree));
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			if (degree >= 180) {
				remoteViews.setViewVisibility(R.id.leftyuanquan, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.icon_panel_progress_bg2,
						View.INVISIBLE);
			}
			if (degree < 180) {
				remoteViews
						.setViewVisibility(R.id.leftyuanquan, View.INVISIBLE);
				remoteViews.setViewVisibility(R.id.icon_panel_progress_bg2,
						View.VISIBLE);
			}
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(getBaseContext());
			ComponentName componentName = new ComponentName(getBaseContext(),
					MusicAppWidgetProvider.class);
			appWidgetManager.updateAppWidget(componentName, remoteViews);
		}

	}

}
