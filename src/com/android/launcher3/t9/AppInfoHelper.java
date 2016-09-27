package com.android.launcher3.t9;

import android.text.TextUtils;
import android.util.Log;

import com.android.launcher3.AppInfo;
import com.pinyinsearch.model.PinyinSearchUnit;
import com.pinyinsearch.util.PinyinUtil;
import com.pinyinsearch.util.T9Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class AppInfoHelper {
    private static final String TAG="zhao11t9";
    private static AppInfoHelper mInstance;

    private List<AppInfo> mBaseAllAppInfos;

    private List<AppInfo> mT9SearchAppInfos;

    private StringBuffer mFirstNoT9SearchResultInput=null;

    public static AppInfoHelper getInstance(){
        if(null==mInstance){
            mInstance=new AppInfoHelper();
        }
        
        return mInstance;
    } 
    
    private AppInfoHelper(){
        initAppInfoHelper();
        return;
    }
    
    private void initAppInfoHelper(){
        clearAppInfoData();
        return;
    }


    public void setBaseAllAppInfos(List<AppInfo> baseAllAppInfos) {
        mBaseAllAppInfos = baseAllAppInfos;
        for(int i = 0 ; i< mBaseAllAppInfos.size();i++){
            AppInfo appInfo = mBaseAllAppInfos.get(i);
            appInfo.getLabelPinyinSearchUnit().setBaseData(appInfo.getTitle());
            PinyinUtil.parse(appInfo.getLabelPinyinSearchUnit());
        }
    }

    public List<AppInfo> getT9SearchAppInfos() {
        return mT9SearchAppInfos;
    }

    public void t9Search(String keyword){
        List<AppInfo> baseAppInfos=getBaseAppInfo();
        Log.i(TAG, "baseAppInfos["+baseAppInfos.size()+"]");
        if(null != mT9SearchAppInfos){
            mT9SearchAppInfos.clear();
        }else{
            mT9SearchAppInfos = new ArrayList<AppInfo>();
        }

        if(TextUtils.isEmpty(keyword)){
            for(AppInfo ai:baseAppInfos){
                ai.setSearchByType(AppInfo.SearchByType.SearchByNull);
                ai.clearMatchKeywords();
                ai.setMatchStartIndex(-1);
                ai.setMatchLength(0);
            }
            
            mT9SearchAppInfos.addAll(baseAppInfos);

            mFirstNoT9SearchResultInput.delete(0, mFirstNoT9SearchResultInput.length());
            Log.i(TAG, "null==search,mFirstNoT9SearchResultInput.length()="
                    + mFirstNoT9SearchResultInput.length()+","+
                    mFirstNoT9SearchResultInput);
            return;
        }

        if (mFirstNoT9SearchResultInput.length() > 0) {
            if (keyword.contains(mFirstNoT9SearchResultInput.toString())) {
                Log.i(TAG,
                        "no need  to search,null!=search,mFirstNoT9SearchResultInput.length()="
                                + mFirstNoT9SearchResultInput.length() + "["
                                + mFirstNoT9SearchResultInput.toString() + "]"
                                + ";searchlen=" + keyword.length() + "["
                                + keyword + "]");
                return;
            } else {
                Log.i(TAG,
                        "delete  mFirstNoT9SearchResultInput, null!=search,mFirstNoT9SearchResultInput.length()="
                                + mFirstNoT9SearchResultInput.length()
                                + "["
                                + mFirstNoT9SearchResultInput.toString()
                                + "]"
                                + ";searchlen="
                                + keyword.length()
                                + "["
                                + keyword + "]");
                mFirstNoT9SearchResultInput.delete(0,mFirstNoT9SearchResultInput.length());
            }
        }

        mT9SearchAppInfos.clear();
        int baseAppInfosCount=baseAppInfos.size();
        for(int i=0; i<baseAppInfosCount; i++){
            PinyinSearchUnit labelPinyinSearchUnit=baseAppInfos.get(i).getLabelPinyinSearchUnit();
            Log.i(TAG,"labelPinyinSearchUnit:"+labelPinyinSearchUnit.getPinyinUnits()+",keyword:"+keyword);
            boolean match= T9Util.match(labelPinyinSearchUnit, keyword);
            
            if (true == match) {// search by LabelPinyinUnits;
                AppInfo appInfo = baseAppInfos.get(i);
                appInfo.setSearchByType(AppInfo.SearchByType.SearchByLabel);
                appInfo.setMatchKeywords(labelPinyinSearchUnit.getMatchKeyword().toString());
                appInfo.setMatchStartIndex(appInfo.getTitle().indexOf(appInfo.getMatchKeywords().toString()));
                appInfo.setMatchLength(appInfo.getMatchKeywords().length());
                mT9SearchAppInfos.add(appInfo);

                continue;
            }
        }
        
        if (mT9SearchAppInfos.size() <= 0) {
            if (mFirstNoT9SearchResultInput.length() <= 0) {
                mFirstNoT9SearchResultInput.append(keyword);
                Log.i(TAG,
                        "no search result,null!=search,mFirstNoT9SearchResultInput.length()="
                                + mFirstNoT9SearchResultInput.length() + "["
                                + mFirstNoT9SearchResultInput.toString() + "]"
                                + ";searchlen=" + keyword.length() + "["
                                + keyword + "]");
            }
        }else{
            Collections.sort(mT9SearchAppInfos, AppInfo.mSearchComparator);
        }
        return;
    }

    private void clearAppInfoData(){
        
        if(null==mBaseAllAppInfos){
            mBaseAllAppInfos=new ArrayList<AppInfo>();
        }
        //mBaseAllAppInfos.clear();

        if(null==mT9SearchAppInfos){
            mT9SearchAppInfos=new ArrayList<AppInfo>();
        }
        mT9SearchAppInfos.clear();

        if(null==mFirstNoT9SearchResultInput){
            mFirstNoT9SearchResultInput=new StringBuffer();
        }else{
            mFirstNoT9SearchResultInput.delete(0, mFirstNoT9SearchResultInput.length());
        }
        
        return;
    }

    private List<AppInfo> getBaseAppInfo(){
        return mBaseAllAppInfos;
    }
}
