package com.android.launcher3.music;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.android.launcher3.R;
public class MusicAppWidgetProvider extends AppWidgetProvider {

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.music_appwidget);
			
			//play
			Intent intentPlay = new Intent(context, PlayMusicService.class);
			intentPlay.putExtra("mode", 0);
			PendingIntent pendingPlay = PendingIntent.getService(
					context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.widget_startBtn,
					pendingPlay);
			//next
			Intent intentNext = new Intent(context, PlayMusicService.class);
			intentNext.putExtra("mode", 1);
			PendingIntent pendingNext = PendingIntent.getService(
					context, 1, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.widget_frontBtn,
					pendingNext);
			
			//pre
			Intent intentPre = new Intent(context, PlayMusicService.class);
			intentPre.putExtra("mode", 2);
			PendingIntent pendingPre = PendingIntent.getService(
					context, 2, intentPre, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.widget_nextBtn,
					pendingPre);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
}
