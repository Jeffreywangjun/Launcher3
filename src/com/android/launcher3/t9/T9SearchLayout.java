package com.android.launcher3.t9;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.t9.util.T9View;


/**
 * t9 搜索器
 */
public class T9SearchLayout extends RelativeLayout implements T9View.T9ViewListener{

    TextView search_result_prompt_text_view,search_emmpy;
    GridView t9_search_grid_view;
    T9View mT9View = null;

    private AppInfoAdapter mAppInfoAdapter;
    public T9SearchLayout(Context context) {
        this(context,null);
    }

    public T9SearchLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public T9SearchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        search_result_prompt_text_view= (TextView) findViewById(R.id.search_result_prompt_text_view);
        search_emmpy= (TextView) findViewById(R.id.search_emmpy);
        t9_search_grid_view= (GridView) findViewById(R.id.t9_search_grid_view);

        mT9View = (T9View) findViewById(R.id.mt9view);
        mT9View.setTextInput(search_result_prompt_text_view);
        mT9View.setT9ViewListener(this);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mAppInfoAdapter = new AppInfoAdapter(getContext(),
                        R.layout.app_info_grid_item, AppInfoHelper.getInstance()
                        .getT9SearchAppInfos());

                t9_search_grid_view.setAdapter(mAppInfoAdapter);
            }
        }, 20);
        //Add by zhaopenglin for t9 20160920 start
        t9_search_grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setComponent(AppInfoHelper.getInstance()
                        .getT9SearchAppInfos().get(position).componentName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });
        //Add by zhaopenglin for t9 20160920 end
    }

    private void updateSearch(String search) {
        String curCharacter;
        if (null == search) {
            curCharacter = search;
        } else {
            curCharacter = search.trim();
        }

        if (TextUtils.isEmpty(curCharacter)) {
            AppInfoHelper.getInstance().t9Search(null);
        } else {
            AppInfoHelper.getInstance().t9Search(curCharacter);
        }
    }

    public void refreshView() {
        refreshT9SearchGv();
    }

    private void refreshT9SearchGv() {
        if (null == t9_search_grid_view) {
            return;
        }

        BaseAdapter baseAdapter = (BaseAdapter) t9_search_grid_view.getAdapter();
        if (null != baseAdapter) {
            baseAdapter.notifyDataSetChanged();
            Log.i("zhao11t9","baseAdapter.getCount():"+baseAdapter.getCount());
            if (baseAdapter.getCount() > 0) {
                ViewUtil.showView(t9_search_grid_view);
                ViewUtil.hideView(search_emmpy);
            } else {
                ViewUtil.hideView(t9_search_grid_view);
                ViewUtil.showView(search_emmpy);
            }
        }
    }

    @Override
    public void AddDialCharacter(String addCharacter) {

    }

    @Override
    public void DeleteDialCharacter(String deleteCharacter) {

    }

    @Override
    public void DialInputTextChanged(String curCharacter) {
        updateSearch(curCharacter);
        refreshView();

    }
}
