package com.android.launcher3.t9;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.AppInfo;
import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.R;

import java.util.List;

public class AppInfoAdapter extends ArrayAdapter<AppInfo> {
    private Context mContext;
    private int mTextViewResourceId;

    public AppInfoAdapter(Context context, int textViewResourceId,
                          List<AppInfo> appInfos) {
        super(context, textViewResourceId, appInfos);
        mContext = context;
        mTextViewResourceId = textViewResourceId;

    }

    @SuppressLint("NewApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        AppInfo appInfo = getItem(position);
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(mTextViewResourceId,
                    null);
            viewHolder = new ViewHolder();
            viewHolder.mIconIv = (ImageView) convertView
                    .findViewById(R.id.icon_image_view);
            viewHolder.mLabelTv = (TextView) convertView
                    .findViewById(R.id.label_text_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mIconIv.setBackground(new FastBitmapDrawable(appInfo.getIconBitmap()));
        switch (appInfo.getSearchByType()) {
            case SearchByLabel:
                ViewUtil.showTextHighlight(viewHolder.mLabelTv, appInfo.getTitle(),
                        appInfo.getMatchKeywords().toString());

                break;
            case SearchByNull:
                ViewUtil.showTextNormal(viewHolder.mLabelTv, appInfo.getTitle());
                break;
            default:
                break;
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView mIconIv;
        TextView mLabelTv;
    }

}
