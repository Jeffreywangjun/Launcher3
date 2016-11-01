package com.android.launcher3.freezeapp;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.AppInfo;
import com.android.launcher3.R;

public class FreezeActivity extends BaseActivity implements IFreezeView {
    private static final String TAG = "FreezeActivity";

    private static final String KEY_IN_NORMAL_SCREEN = "mInNormalScreen";
    private static final String TAG_FREEZE_FRAGMENT = "FreezeAppFragment";
    private static final String TAG_NORMAL_FRAGMENT = "NormalAppFragment";

    public static final int LOADER_ID_FREEZED_APP = 1;
    public static final int LOADER_ID_NORMAL_APP = 2;

    private FreezeAppFragment mFreezeFragment;
    private FreezeNormalAppFragment mNormalFragment;

    private FreezePresenter presenter;
    private IFreezeModel model;

    private boolean mInNormalScreen = false;

    private IceAnimationLayout mAnimBgView;
    private ImageView mStaticImgview;
    private TextView mLabelView;

    private Drawable mBlurBgDrawable;

    private ConfirmDialogFragment confirmDialog;

    private int position;
    private String packageName;

    private static final int[] iceBrokenDrawables = {
            R.drawable.ice_broken_0000, R.drawable.ice_broken_0001,
            R.drawable.ice_broken_0002, R.drawable.ice_broken_0003,
            R.drawable.ice_broken_0004, R.drawable.ice_broken_0005,
            R.drawable.ice_broken_0006, R.drawable.ice_broken_0007,
            R.drawable.ice_broken_0008, R.drawable.ice_broken_0009,
            R.drawable.ice_broken_0010, R.drawable.ice_broken_0011,
            R.drawable.ice_broken_0012, R.drawable.ice_broken_0013,
            R.drawable.ice_broken_0014, R.drawable.ice_broken_0015,
            R.drawable.ice_broken_0016, R.drawable.ice_broken_0017,
            R.drawable.ice_broken_0018, R.drawable.ice_broken_0019,
            R.drawable.ice_broken_0020, R.drawable.ice_broken_0021,
            R.drawable.ice_broken_0022, R.drawable.ice_broken_0023,
            R.drawable.ice_broken_0024, R.drawable.ice_broken_0025,
            R.drawable.ice_broken_0026, R.drawable.ice_broken_0027,};

    private static final int[] iceFrozeDrawables = {
            R.drawable.ice_frozen_0000, R.drawable.ice_frozen_0001,
            R.drawable.ice_frozen_0002, R.drawable.ice_frozen_0003,
            R.drawable.ice_frozen_0004, R.drawable.ice_frozen_0005,
            R.drawable.ice_frozen_0006, R.drawable.ice_frozen_0007,
            R.drawable.ice_frozen_0008, R.drawable.ice_frozen_0009,
            R.drawable.ice_frozen_0010, R.drawable.ice_frozen_0011,
            R.drawable.ice_frozen_0012, R.drawable.ice_frozen_0013,
            R.drawable.ice_frozen_0014, R.drawable.ice_frozen_0015,
            R.drawable.ice_frozen_0016, R.drawable.ice_frozen_0017,
            R.drawable.ice_frozen_0018, R.drawable.ice_frozen_0019,
            R.drawable.ice_frozen_0020, R.drawable.ice_frozen_0021,
            R.drawable.ice_frozen_0022, R.drawable.ice_frozen_0023,
            R.drawable.ice_frozen_0024, R.drawable.ice_frozen_0025,
            R.drawable.ice_frozen_0026, R.drawable.ice_frozen_0027,
            R.drawable.ice_frozen_0028, R.drawable.ice_frozen_0029,
            R.drawable.ice_frozen_0030, R.drawable.ice_frozen_0031,
            R.drawable.ice_frozen_0032, R.drawable.ice_frozen_0033,
            R.drawable.ice_frozen_0034, R.drawable.ice_frozen_0035,
            R.drawable.ice_frozen_0036, R.drawable.ice_frozen_0037,
            R.drawable.ice_frozen_0038, R.drawable.ice_frozen_0039,
            R.drawable.ice_frozen_0040,};

    private FreezeNormalAppFragment.OnItemClickListener mOnNormalItemClickListener = new FreezeNormalAppFragment.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View itemView,
                                AppInfo info) {
            getBlurDrawable();
            showConfirmDialog(info, false);
        }
    };

    private FreezeAppFragment.OnItemClickListener mOnFreezeItemClickListener = new FreezeAppFragment.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View itemView,
                                int position, AppInfo info) {
            if (position == 0) {
                toAddApp();
            } else {
                getBlurDrawable();
                FreezeActivity.this.position = position;
                showConfirmDialog(info, true);
            }
        }
    };

    private final LoaderCallbacks<ArrayList<AppInfo>> freezeAppCallbacks = new LoaderCallbacks<ArrayList<AppInfo>>() {
        FreezeAppsLoader loader;

        @Override
        public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
            loader = new FreezeAppsLoader(FreezeActivity.this,
                    FreezeAppsLoader.FLAG_FREEZED_APPS, presenter);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<AppInfo>> loader,
                                   ArrayList<AppInfo> infos) {
            mFreezeFragment.changeData(infos);
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
        }
    };

    private final LoaderCallbacks<ArrayList<AppInfo>> normalAppCallbacks = new LoaderCallbacks<ArrayList<AppInfo>>() {
        FreezeAppsLoader loader;

        @Override
        public Loader<ArrayList<AppInfo>> onCreateLoader(int id, Bundle args) {
            loader = new FreezeAppsLoader(FreezeActivity.this,
                    FreezeAppsLoader.FLAG_NORMAL_APPS, presenter);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<AppInfo>> loader,
                                   ArrayList<AppInfo> infos) {
            mNormalFragment.changeData(infos);
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<AppInfo>> loader) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fp_freeze_activity);
        packageName = getIntent().getStringExtra("packageName");
        Log.i(TAG, "" + packageName);
        mAnimBgView = (IceAnimationLayout) findViewById(R.id.anim_container);
        mStaticImgview = (ImageView) findViewById(R.id.static_img);
        mLabelView = (TextView) findViewById(R.id.label);

        model = new FreezeModelImpl(this);
        presenter = new FreezePresenter(this, model);

        mFreezeFragment = new FreezeAppFragment();
        mNormalFragment = new FreezeNormalAppFragment();

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();
            transaction.add(R.id.container, mFreezeFragment,
                    TAG_FREEZE_FRAGMENT);
            transaction.add(R.id.container, mNormalFragment,
                    TAG_NORMAL_FRAGMENT);
            transaction.commit();
            mInNormalScreen = false;
        } else {
            mInNormalScreen = savedInstanceState
                    .getBoolean(KEY_IN_NORMAL_SCREEN);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof FreezeAppFragment) {
            mFreezeFragment = (FreezeAppFragment) fragment;
            mFreezeFragment.setOnItemClickListener(mOnFreezeItemClickListener);
        } else if (fragment instanceof FreezeNormalAppFragment) {
            mNormalFragment = (FreezeNormalAppFragment) fragment;
            mNormalFragment.setOnItemClickListener(mOnNormalItemClickListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (mInNormalScreen) {
            hideNormalFragment(true);
            mInNormalScreen = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_NORMAL_SCREEN, mInNormalScreen);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        getLoaderManager().initLoader(LOADER_ID_FREEZED_APP,
                null, freezeAppCallbacks);
        getLoaderManager().initLoader(LOADER_ID_NORMAL_APP,
                null, normalAppCallbacks);

        if (mInNormalScreen) {
            showNormalFragment(false);
        } else {
            hideNormalFragment(false);
        }
        AppInfo app = null;
        if (packageName != null) {
            ApplicationInfo appInfo = FreezePackageManagerAdapter.getInstance(FreezeActivity.this).getApplicationInfo(
                    packageName, 0);
            app = new AppInfo();
            app.applicationInfo = appInfo;
            app.title = appInfo.loadLabel(this.getPackageManager());
//            app.iconDrawable = appInfo.loadIcon(this.getPackageManager());
            presenter.addFreezeApp(app);
        }
    }

    private void iceBrokenAnimation() {
        mAnimBgView.shakeChildWithAnim(0);
        mAnimBgView.setAnimRes(iceBrokenDrawables);
        mAnimBgView.startAnim();
    }

    private void iceFreezeAnimation() {
        mAnimBgView.setAnimRes(iceFrozeDrawables);
        mAnimBgView.startAnim();
    }

    private void setAnimationBg() {
        mAnimBgView.setVisibility(View.VISIBLE);
        mAnimBgView.setBackground(mBlurBgDrawable);
    }

    private Drawable getBlurDrawable() {
        Log.d(TAG, "getBlurDrawable");
        View v = mInNormalScreen ? mNormalFragment.getView() : mFreezeFragment
                .getView();
        Bitmap bm = createDraggedChildBitmap(v); // Screenshot
        bm = FastBlur.doBlur(scaleBitmap(bm, 180, 260), 15, false); // Gaussian blur
        // Log.d("sqm", "bm====" + bm);
        mBlurBgDrawable = new BitmapDrawable(getResources(), bm);
        return mBlurBgDrawable;
    }

    // scale bitmap
    private Bitmap scaleBitmap(Bitmap bm, int w, int h) {
        Bitmap b = bm;
        int width = b.getWidth();
        int height = b.getHeight();
        // Log.d("sqm", "width=" + width + ", height=" + height);
        float scaleW = w * 1.0f / width;
        float scaleH = h * 1.0f / height;

        Matrix m = new Matrix();
        m.postScale(scaleW, scaleH);
        Bitmap bb = Bitmap.createBitmap(b, 0, 0, width, height, m, true);
        // Log.d("sqm", "bb:width=" + bb.getWidth() + ", height=" +
        // bb.getHeight());
        return bb;
    }

    // screenshot method
    private Bitmap createDraggedChildBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        final Bitmap cache = view.getDrawingCache();

        Bitmap bitmap = null;
        if (cache != null) {
            try {
                bitmap = cache.copy(Bitmap.Config.ARGB_8888, false);
            } catch (final OutOfMemoryError e) {
                Log.w(TAG, "Failed to copy bitmap from Drawing cache", e);
                bitmap = null;
            }
        }

        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);

        return bitmap;
    }

    private void showConfirmDialog(AppInfo info, boolean isFreezed) {
        // if (confirmDialog == null) {
        confirmDialog = new ConfirmDialogFragment(info, isFreezed);
        // }
        confirmDialog.show(getFragmentManager(),
                ConfirmDialogFragment.FRAGMENT_TAG);
    }

    private void toAddApp() {
        showNormalFragment(true);
        mInNormalScreen = true;
    }

    @Override
    protected void onHomeSelected() {
        if (mInNormalScreen) {
            hideNormalFragment(true);
            mInNormalScreen = false;
        } else {
            finish();
        }
    }

    @SuppressLint("ValidFragment")
    public class ConfirmDialogFragment extends DialogFragment {
        public static final String FRAGMENT_TAG = "confirm_dialog";
        private AppInfo mAppInfo;
        private boolean isFreezed;

        OnClickListener clickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.dialog_ok) {
                    if (isFreezed) {
                        presenter.deleteFreezeApp(mAppInfo);
                    } else {
                        presenter.addFreezeApp(mAppInfo);
                    }
                    dismiss();
                } else if (v.getId() == R.id.dialog_cancel) {
                    dismiss();
                }

            }
        };

        public ConfirmDialogFragment(AppInfo info, boolean isFreez) {
            mAppInfo = info;
            isFreezed = isFreez;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    getActivity(), R.style.DialogTheme);
            final LayoutInflater dialogInflater = LayoutInflater.from(builder
                    .getContext());

            final View view = dialogInflater.inflate(
                    R.layout.confirm_dialog_layout, null, false);
            TextView msgView = (TextView) view.findViewById(R.id.dialog_msg);
            Button okBtn = (Button) view.findViewById(R.id.dialog_ok);
            Button cancelBtn = (Button) view.findViewById(R.id.dialog_cancel);

            okBtn.setOnClickListener(clickListener);
            cancelBtn.setOnClickListener(clickListener);
            String rawMsg = isFreezed ? getString(R.string.app_unfreeze_dialog_message)
                    : getString(R.string.app_freeze_dialog_message);
            msgView.setText(String.format(rawMsg, mAppInfo.title));
            builder.setView(view);

            return builder.create();
        }
    }

    private void hideNormalFragment(boolean animate) {
        if (animate) {
            TranslateAnimation animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, 1,
                    Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, 0);
            animation.setDuration(400);
            animation.setInterpolator(new AccelerateInterpolator());
            mNormalFragment.getView().startAnimation(animation);
        }
        final FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.hide(mNormalFragment);
        transaction.commit();
    }


    private void showNormalFragment(boolean animate) {
        final FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        if (animate) {
            TranslateAnimation animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_PARENT, 1,
                    Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, 0,
                    Animation.RELATIVE_TO_PARENT, 0);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(400);
            mNormalFragment.getView().startAnimation(animation);
        }
        transaction.show(mNormalFragment);
        transaction.commit();
    }

    @Override
    public void addFreezeApp(AppInfo mAppInfo) {
        setAnimationBg();

        Drawable drawable = mAppInfo.applicationInfo.loadIcon(this.getPackageManager());
        mStaticImgview.setImageDrawable(drawable);
        mLabelView.setText(mAppInfo.title);

        iceFreezeAnimation();

        String toast = String
                .format(getString(R.string.toast_app_freeze_success),
                        mAppInfo.title);
        Toast.makeText(FreezeActivity.this, toast,
                Toast.LENGTH_SHORT).show();

        mFreezeFragment.changeData(null);
        mNormalFragment.changeData(null);
    }

    @Override
    public void deleteFreezeApp(AppInfo mAppInfo) {
        setAnimationBg();

        Drawable drawable = mAppInfo.applicationInfo.loadIcon(this.getPackageManager());
        mStaticImgview.setImageDrawable(drawable);
        mLabelView.setText(mAppInfo.title);

        iceBrokenAnimation();

        String toast = String
                .format(getString(R.string.toast_app_unfreeze_success),
                        mAppInfo.title);
        Toast.makeText(FreezeActivity.this, toast,
                Toast.LENGTH_SHORT).show();

        mFreezeFragment.changeData(null);
        mNormalFragment.changeData(null);
    }

}
