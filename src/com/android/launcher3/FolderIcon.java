/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.FolderInfo.FolderListener;
import com.android.launcher3.freezeapp.FreezeActivity;
import com.android.launcher3.util.Thunk;

import java.util.ArrayList;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends FrameLayout implements FolderListener {
    @Thunk
    Launcher mLauncher;
    @Thunk
    Folder mFolder;
    private FolderInfo mInfo;
    @Thunk
    static boolean sStaticValuesDirty = true;

    private CheckLongPressHelper mLongPressHelper;
    private StylusEventHelper mStylusEventHelper;

    // The number of icons to display in the

    // Modify BUG_ID:NONE zhaopenglin 20150601(start)
    //private static final int NUM_ITEMS_IN_PREVIEW = 3;
    public static int NUM_ITEMS_IN_PREVIEW;
    private static final int NUM_COLUMN_IN_PREVIEW = 2;
    // Modify BUG_ID:NONE zhaopenglin 20150601(end)

    private static final int CONSUMPTION_ANIMATION_DURATION = 100;
    private static final int DROP_IN_ANIMATION_DURATION = 400;
    private static final int INITIAL_ITEM_ANIMATION_DURATION = 350;
    private static final int FINAL_ITEM_ANIMATION_DURATION = 200;

    // The degree to which the inner ring grows when accepting drop
    private static final float INNER_RING_GROWTH_FACTOR = 0.15f;

    // The degree to which the outer ring is scaled in its natural state
    private static final float OUTER_RING_GROWTH_FACTOR = 0.3f;

    // The amount of vertical spread between items in the stack [0...1]
    private static final float PERSPECTIVE_SHIFT_FACTOR = 0.18f;

    // Flag as to whether or not to draw an outer ring. Currently none is designed.
    public static final boolean HAS_OUTER_RING = true;

    // Flag whether the folder should open itself when an item is dragged over is enabled.
    public static final boolean SPRING_LOADING_ENABLED = true;

    // The degree to which the item in the back of the stack is scaled [0...1]
    // (0 means it's not scaled at all, 1 means it's scaled to nothing)
    private static final float PERSPECTIVE_SCALE_FACTOR = 0.35f;

    // Delay when drag enters until the folder opens, in miliseconds.
    private static final int ON_OPEN_DELAY = 800;

    public static Drawable sSharedFolderLeaveBehind = null;

    @Thunk
    ImageView mPreviewBackground;
    @Thunk
    BubbleTextView mFolderName;

    FolderRingAnimator mFolderRingAnimator = null;

    // These variables are all associated with the drawing of the preview; they are stored
    // as member variables for shared usage and to avoid computation on each frame
    private int mIntrinsicIconSize;
    private float mBaselineIconScale;
    private int mBaselineIconSize;
    private int mAvailableSpaceInPreview;
    private int mTotalWidth = -1;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;
    private float mMaxPerspectiveShift;
    boolean mAnimating = false;
    private Rect mOldBounds = new Rect();
    private static boolean is_rect_folder;
    private float mSlop;

    private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    @Thunk
    PreviewItemDrawingParams mAnimParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    @Thunk
    ArrayList<ShortcutInfo> mHiddenItems = new ArrayList<ShortcutInfo>();

    private Alarm mOpenAlarm = new Alarm();
    @Thunk
    ItemInfo mDragInfo;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolderIcon(Context context) {
        super(context);
        init();
    }

    private void init() {
        //Add by zhaopenglin for change the folder icon  20150719 start
        is_rect_folder = true;
        if (is_rect_folder) {
            NUM_ITEMS_IN_PREVIEW = 4;
        } else {
            NUM_ITEMS_IN_PREVIEW = 3;
        }
        //Add by zhaopenglin for change the folder icon  20150719 end
        mLongPressHelper = new CheckLongPressHelper(this);
        mStylusEventHelper = new StylusEventHelper(this);
        setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());
    }

    public boolean isDropEnabled() {
        final ViewGroup cellLayoutChildren = (ViewGroup) getParent();
        final ViewGroup cellLayout = (ViewGroup) cellLayoutChildren.getParent();
        final Workspace workspace = (Workspace) cellLayout.getParent();
        return !workspace.workspaceInModalState();
    }

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
                              FolderInfo folderInfo, IconCache iconCache) {
        return fromXml(resId, launcher, group, folderInfo, iconCache, false);//delete Freeze 7
    }

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
                              FolderInfo folderInfo, IconCache iconCache, boolean fromAllApp) {
        Log.i("Launcher:FolderIcon", "fromXml " + folderInfo.title);
        @SuppressWarnings("all") // suppress dead code warning
        final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }

        DeviceProfile grid = launcher.getDeviceProfile();

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
        icon.setClipToPadding(false);
        icon.mFolderName = (BubbleTextView) icon.findViewById(R.id.folder_icon_name);
        icon.mFolderName.setText(folderInfo.title);
        icon.mFolderName.setCompoundDrawablePadding(0);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) icon.mFolderName.getLayoutParams();
        lp.topMargin = grid.iconSizePx + grid.iconDrawablePaddingPx;

        // Offset the preview background to center this view accordingly
        icon.mPreviewBackground = (ImageView) icon.findViewById(R.id.preview_background);
        lp = (FrameLayout.LayoutParams) icon.mPreviewBackground.getLayoutParams();
        lp.topMargin = grid.folderBackgroundOffset;
        lp.width = grid.folderIconSizePx;
        lp.height = grid.folderIconSizePx;

        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        Folder folder = Folder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        folder.bind(folderInfo);//delete Freeze 6
        icon.mFolder = folder;

        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);

        icon.setOnFocusChangeListener(launcher.mFocusHandler);
        Log.i("Launcher:FolderIcon", "icon = " + icon.getFolderInfo().title);
        return icon;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    public static class FolderRingAnimator {
        public int mCellX;
        public int mCellY;
        @Thunk
        CellLayout mCellLayout;
        public float mOuterRingSize;
        public float mInnerRingSize;
        public FolderIcon mFolderIcon = null;
        public static Drawable sSharedOuterRingDrawable = null;
        public static Drawable sSharedInnerRingDrawable = null;
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;

        public static int sAvailableIconSpace = -1;//Add BUG_ID:NONE zhaopenglin 20150601
        private ValueAnimator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;

        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            Log.i("Launcher:FolderIcon", "FolderRingAnimator");
            mFolderIcon = folderIcon;
            Resources res = launcher.getResources();

            // We need to reload the static values when configuration changes in case they are
            // different in another configuration
            if (sStaticValuesDirty) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    throw new RuntimeException("FolderRingAnimator loading drawables on non-UI thread "
                            + Thread.currentThread());
                }

                DeviceProfile grid = launcher.getDeviceProfile();
                sPreviewSize = grid.folderIconSizePx;
                sPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);
                //Modify BUG_ID:none zhaopenglin 20150601(start)
                sAvailableIconSpace = res.getDimensionPixelSize(R.dimen.folder_available_icon_space);
                //if(true){
                //    sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer_rect);
                //    sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_rect);
                //    sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_rest_rect);
                //}else{
                sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer);
                sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner_nolip);
                sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_rest);
                sStaticValuesDirty = false;
            }
        }

        public void animateToAcceptState() {
            Log.i("Launcher:FolderIcon", "animateToAcceptState");
            if (mNeutralAnimator != null) {
                mNeutralAnimator.cancel();
            }
            mAcceptAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mAcceptAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + percent * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
                    }
                }
            });
            mAcceptAnimator.start();
        }

        public void animateToNaturalState() {
            Log.i("Launcher:FolderIcon", "animateToNaturalState");
            if (mAcceptAnimator != null) {
                mAcceptAnimator.cancel();
            }
            mNeutralAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mNeutralAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + (1 - percent) * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCellLayout != null) {
                        mCellLayout.hideFolderAccept(FolderRingAnimator.this);
                    }
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                    }
                }
            });
            mNeutralAnimator.start();
        }

        // Location is expressed in window coordinates
        public void getCell(int[] loc) {
            loc[0] = mCellX;
            loc[1] = mCellY;
        }

        // Location is expressed in window coordinates
        public void setCell(int x, int y) {
            mCellX = x;
            mCellY = y;
        }

        public void setCellLayout(CellLayout layout) {
            Log.i("Launcher:FolderIcon", "setCellLayout");
            mCellLayout = layout;
        }

        public float getOuterRingSize() {
            return mOuterRingSize;
        }

        public float getInnerRingSize() {
            return mInnerRingSize;
        }
    }

    public Folder getFolder() {
        return mFolder;
    }

    FolderInfo getFolderInfo() {
        return mInfo;
    }

    private boolean willAcceptItem(ItemInfo item) {
        Log.i("Launcher:FolderIcon", "willAcceptItem");
        final int itemType = item.itemType;
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) &&
                !mFolder.isFull() && item != mInfo && !mInfo.opened);
    }

    public boolean acceptDrop(Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        boolean b = !mFolder.isDestroyed() && willAcceptItem(item);
        Log.i("Launcher:FolderIcon", "acceptDrop " + b);
        return b;
    }

    public void addItem(ShortcutInfo item) {
        Log.i("Launcher:FolderIcon", "addItem");
        mInfo.add(item);
    }

    public void onDragEnter(Object dragInfo) {
        Log.i("Launcher:FolderIcon", "onDragEnter");
        if (mFolder.isDestroyed() || !willAcceptItem((ItemInfo) dragInfo)) return;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) getLayoutParams();
        CellLayout layout = (CellLayout) getParent().getParent();
        mFolderRingAnimator.setCell(lp.cellX, lp.cellY);
        mFolderRingAnimator.setCellLayout(layout);
        mFolderRingAnimator.animateToAcceptState();
        layout.showFolderAccept(mFolderRingAnimator);
        mOpenAlarm.setOnAlarmListener(mOnOpenListener);
        if (SPRING_LOADING_ENABLED &&
                ((dragInfo instanceof AppInfo) || (dragInfo instanceof ShortcutInfo))) {
            // TODO: we currently don't support spring-loading for PendingAddShortcutInfos even
            // though widget-style shortcuts can be added to folders. The issue is that we need
            // to deal with configuration activities which are currently handled in
            // Workspace#onDropExternal.
            mOpenAlarm.setAlarm(ON_OPEN_DELAY);
        }
        mDragInfo = (ItemInfo) dragInfo;
    }

    public void onDragOver(Object dragInfo) {
    }

    OnAlarmListener mOnOpenListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            //taoqi return open folder
            Log.i("Launcher:FolderIcon", "mOnOpenListener " + getFolderInfo().title);
            if (getFolderInfo().title.equals("Freeze")) {
                return;
            }
            //taoqi end
            ShortcutInfo item;
            if (mDragInfo instanceof AppInfo) {
                // Came from all apps -- make a copy.
                item = ((AppInfo) mDragInfo).makeShortcut();
                item.spanX = 1;
                item.spanY = 1;
            }
            ///M: Added to filter out the PendingAddItemInfo instance.@{
            else if (mDragInfo instanceof PendingAddItemInfo) {
                return;
            }
            ///M: @}
            else {
                item = (ShortcutInfo) mDragInfo;
            }
            mFolder.beginExternalDrag(item);
            mLauncher.openFolder(FolderIcon.this);
        }
    };

    public void performCreateAnimation(final ShortcutInfo destInfo, final View destView,
                                       final ShortcutInfo srcInfo, final DragView srcView, Rect dstRect,
                                       float scaleRelativeToDragLayer, Runnable postAnimationRunnable) {

        Log.i("Launcher:FolderIcon", "performCreateAnimation");
        // These correspond two the drawable and view that the icon was dropped _onto_
        Drawable animateDrawable = getTopDrawable((TextView) destView);
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(),
                destView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, INITIAL_ITEM_ANIMATION_DURATION, false, null);
        addItem(destInfo);

        // This will animate the dragView (srcView) into the new folder
        onDrop(srcInfo, srcView, dstRect, scaleRelativeToDragLayer, 1, postAnimationRunnable, null);
    }

    public void performDestroyAnimation(final View finalView, Runnable onCompleteRunnable) {
        Log.i("Launcher:FolderIcon", "performDestroyAnimation");
        Drawable animateDrawable = getTopDrawable((TextView) finalView);
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(),
                finalView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, FINAL_ITEM_ANIMATION_DURATION, true,
                onCompleteRunnable);
    }

    public void onDragExit(Object dragInfo) {
        Log.i("Launcher:FolderIcon", "onDragExit");
        onDragExit();
    }

    public void onDragExit() {
        Log.i("Launcher:FolderIcon", "onDragExit()");
        mFolderRingAnimator.animateToNaturalState();
        mOpenAlarm.cancelAlarm();
    }

    private void onDrop(final ShortcutInfo item, DragView animateView, Rect finalRect,
                        float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable,
                        DragObject d) {
        Log.i("Launcher:FolderIcon", "onDrop()");
        item.cellX = -1;
        item.cellY = -1;

        // Typically, the animateView corresponds to the DragView; however, if this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
        // will not have a view to animate
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
            Rect from = new Rect();
            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = finalRect;
            if (to == null) {
                to = new Rect();
                Workspace workspace = mLauncher.getWorkspace();
                // Set cellLayout and this to it's final state to compute final animation locations
                workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
                float scaleX = getScaleX();
                float scaleY = getScaleY();
                setScaleX(1.0f);
                setScaleY(1.0f);
                scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(this, to);
                // Finished computing final animation locations, restore current state
                setScaleX(scaleX);
                setScaleY(scaleY);
                workspace.resetTransitionTransform((CellLayout) getParent().getParent());
            }

            int[] center = new int[2];
            float scale = getLocalCenterForIndex(index, center);
            center[0] = (int) Math.round(scaleRelativeToDragLayer * center[0]);
            center[1] = (int) Math.round(scaleRelativeToDragLayer * center[1]);

            to.offset(center[0] - animateView.getMeasuredWidth() / 2,
                    center[1] - animateView.getMeasuredHeight() / 2);

            float finalAlpha = index < NUM_ITEMS_IN_PREVIEW ? 0.5f : 0f;

            float finalScale = scale * scaleRelativeToDragLayer;
            dragLayer.animateView(animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, DROP_IN_ANIMATION_DURATION,
                    new DecelerateInterpolator(2), new AccelerateInterpolator(2),
                    postAnimationRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null);
            addItem(item);
            mHiddenItems.add(item);
            mFolder.hideItem(item);
            postDelayed(new Runnable() {
                public void run() {
                    mHiddenItems.remove(item);
                    mFolder.showItem(item);
                    invalidate();
                }
            }, DROP_IN_ANIMATION_DURATION);
        } else {
            addItem(item);
        }
    }

    public void onDrop(DragObject d) {
        Log.i("Launcher:FolderIcon", "onDrop(DragObject d)");
        ShortcutInfo item;
        boolean isDraggingAdd = mInfo.title.equals("Freeze");
        if (d.dragInfo instanceof AppInfo) {
            // Came from all apps -- make a copy
            item = ((AppInfo) d.dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }
        mFolder.notifyDrop();
        onDrop(item, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
        //A: taoqi 20160925(start) start FreezeActivity when dragging to "Freeze" folder
        String strIntent = item.getIntent().toString();
        int start = strIntent.indexOf("cmp=com");
        int end = strIntent.indexOf("/.");
        String packageName = strIntent.substring(start + 4, end);
        Log.i("Launcher:FolderIcon", "packageName = " + packageName);
        if (isDraggingAdd) {
            Intent intent = new Intent(getContext(), FreezeActivity.class);
            intent.putExtra("packageName", packageName);
            mLauncher.startActivity(intent);
        }
        //A: taoqi 20160925(end)
    }

    private void computePreviewDrawingParams(int drawableSize, int totalSize) {
        if (mIntrinsicIconSize != drawableSize || mTotalWidth != totalSize) {
            DeviceProfile grid = mLauncher.getDeviceProfile();

            mIntrinsicIconSize = drawableSize;
            mTotalWidth = totalSize;

            final int previewSize = mPreviewBackground.getLayoutParams().height;
            final int previewPadding = FolderRingAnimator.sPreviewPadding;

            mAvailableSpaceInPreview = (previewSize - 2 * previewPadding);
            // cos(45) = 0.707  + ~= 0.1) = 0.8f
            int adjustedAvailableSpace = (int) ((mAvailableSpaceInPreview / 2) * (1 + 0.8f));

            int unscaledHeight = (int) (mIntrinsicIconSize * (1 + PERSPECTIVE_SHIFT_FACTOR));

            mBaselineIconScale = (1.0f * adjustedAvailableSpace / unscaledHeight);

            mBaselineIconSize = (int) (mIntrinsicIconSize * mBaselineIconScale);
            mMaxPerspectiveShift = mBaselineIconSize * PERSPECTIVE_SHIFT_FACTOR;

            mPreviewOffsetX = (mTotalWidth - mAvailableSpaceInPreview) / 2;
            mPreviewOffsetY = previewPadding + grid.folderBackgroundOffset;
        }
    }

    private void computePreviewDrawingParams(Drawable d) {
        computePreviewDrawingParams(d.getIntrinsicWidth(), getMeasuredWidth());
    }

    class PreviewItemDrawingParams {
        PreviewItemDrawingParams(float transX, float transY, float scale, int overlayAlpha) {
            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
            this.overlayAlpha = overlayAlpha;
        }

        float transX;
        float transY;
        float scale;
        int overlayAlpha;
        Drawable drawable;
    }

    private float getLocalCenterForIndex(int index, int[] center) {
        Log.i("Launcher:FolderIcon", "getLocalCenterForIndex");
        mParams = computePreviewItemDrawingParams(Math.min(NUM_ITEMS_IN_PREVIEW, index), mParams);

        mParams.transX += mPreviewOffsetX;
        mParams.transY += mPreviewOffsetY;
        float offsetX = mParams.transX + (mParams.scale * mIntrinsicIconSize) / 2;
        float offsetY = mParams.transY + (mParams.scale * mIntrinsicIconSize) / 2;

        center[0] = (int) Math.round(offsetX);
        center[1] = (int) Math.round(offsetY);
        return mParams.scale;
    }

    private PreviewItemDrawingParams computePreviewItemDrawingParams(int index,
                                                                     PreviewItemDrawingParams params) {
        //Modify BUG_ID:none zhaopenglin 20150601(start)

        float transY;
        float transX;
        float totalScale;
        final int overlayAlpha;
        if (!is_rect_folder) {
            index = NUM_ITEMS_IN_PREVIEW - index - 1;
            float r = (index * 1.0f) / (NUM_ITEMS_IN_PREVIEW - 1);
            float scale = (1 - PERSPECTIVE_SCALE_FACTOR * (1 - r));

            float offset = (1 - r) * mMaxPerspectiveShift;
            float scaledSize = scale * mBaselineIconSize;
            float scaleOffsetCorrection = (1 - scale) * mBaselineIconSize;

            // We want to imagine our coordinates from the bottom left, growing up and to the
            // right. This is natural for the x-axis, but for the y-axis, we have to invert things.
            transY = mAvailableSpaceInPreview - (offset + scaledSize + scaleOffsetCorrection) + getPaddingTop() - 3;
            transX = (mAvailableSpaceInPreview - scaledSize) / 2;
            //transX = offset + scaleOffsetCorrection;
            totalScale = mBaselineIconScale * scale;
            overlayAlpha = (int) (80 * (1 - r));
        } else {
            float mAvailableSpaceInFolderIcon = FolderRingAnimator.sAvailableIconSpace;
            float itemPadding = 2;
            float scaledSize = (mAvailableSpaceInFolderIcon - itemPadding * NUM_COLUMN_IN_PREVIEW) / NUM_COLUMN_IN_PREVIEW;
            float scale = scaledSize / mIntrinsicIconSize;
            float leftMargin = (mAvailableSpaceInPreview - mAvailableSpaceInFolderIcon) / 2;
            Resources res = getResources();
            int iconTopToFolderHeight = res.getDimensionPixelSize(R.dimen.folder_available_icon_height);
            float topMarginY = (mAvailableSpaceInPreview - mAvailableSpaceInFolderIcon) / 2 + iconTopToFolderHeight;
            int column = index % NUM_COLUMN_IN_PREVIEW;
            int row = index / NUM_COLUMN_IN_PREVIEW;
            transX = leftMargin + scaledSize * column + itemPadding * column + 4;
            transY = topMarginY + scaledSize * row + itemPadding * row;
            totalScale = scale;
            overlayAlpha = 0;
        }
        //Modify BUG_ID:none zhaopenglin 20150601(end)

        if (params == null) {
            params = new PreviewItemDrawingParams(transX, transY, totalScale, overlayAlpha);
        } else {
            params.transX = transX;
            params.transY = transY;
            params.scale = totalScale;
            params.overlayAlpha = overlayAlpha;
        }
        return params;
    }

    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
        canvas.save();
        canvas.translate(params.transX + mPreviewOffsetX, params.transY + mPreviewOffsetY);
        canvas.scale(params.scale, params.scale);
        Drawable d = params.drawable;

        if (d != null) {
            mOldBounds.set(d.getBounds());
            d.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
            if (d instanceof FastBitmapDrawable) {
                FastBitmapDrawable fd = (FastBitmapDrawable) d;
                int oldBrightness = fd.getBrightness();
                fd.setBrightness(params.overlayAlpha);
                d.draw(canvas);
                fd.setBrightness(oldBrightness);
            } else {
                d.setColorFilter(Color.argb(params.overlayAlpha, 255, 255, 255),
                        PorterDuff.Mode.SRC_ATOP);
                d.draw(canvas);
                d.clearColorFilter();
            }
            d.setBounds(mOldBounds);
        }
        canvas.restore();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mFolder == null) return;
        if (mFolder.getItemCount() == 0 && !mAnimating) return;

        ArrayList<View> items = mFolder.getItemsInReadingOrder();
        Drawable d;
        TextView v;

        // Update our drawing parameters if necessary
        if (mAnimating) {
            computePreviewDrawingParams(mAnimParams.drawable);
        } else {
            try {
                v = (TextView) items.get(0);
                d = getTopDrawable(v);
                computePreviewDrawingParams(d);
            } catch (Exception e) {
                Log.e("Launcher:FolderIcon", "e = " + e);
            }
        }

        int nItemsInPreview = Math.min(items.size(), NUM_ITEMS_IN_PREVIEW);
        if (!mAnimating) {
            for (int i = nItemsInPreview - 1; i >= 0; i--) {
                v = (TextView) items.get(i);
                if (!mHiddenItems.contains(v.getTag())) {
                    d = getTopDrawable(v);
                    mParams = computePreviewItemDrawingParams(i, mParams);
                    mParams.drawable = d;
                    drawPreviewItem(canvas, mParams);
                }
            }
        } else {
            drawPreviewItem(canvas, mAnimParams);
        }
        /**M: Draw unread event number.@{**/
        MTKUnreadLoader.drawUnreadEventIfNeed(canvas, this);
        /**@}**/
    }

    private Drawable getTopDrawable(TextView v) {
        Drawable d = v.getCompoundDrawables()[1];
        return (d instanceof PreloadIconDrawable) ? ((PreloadIconDrawable) d).mIcon : d;
    }

    private void animateFirstItem(final Drawable d, int duration, final boolean reverse,
                                  final Runnable onCompleteRunnable) {
        Log.i("Launcher:FolderIcon", "animateFirstItem");
        final PreviewItemDrawingParams finalParams = computePreviewItemDrawingParams(0, null);

        float iconSize = mLauncher.getDeviceProfile().iconSizePx;
        final float scale0 = iconSize / d.getIntrinsicWidth();
        final float transX0 = (mAvailableSpaceInPreview - iconSize) / 2;
        final float transY0 = (mAvailableSpaceInPreview - iconSize) / 2 + getPaddingTop();
        mAnimParams.drawable = d;

        ValueAnimator va = LauncherAnimUtils.ofFloat(this, 0f, 1.0f);
        va.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (Float) animation.getAnimatedValue();
                if (reverse) {
                    progress = 1 - progress;
                    mPreviewBackground.setAlpha(progress);
                }

                mAnimParams.transX = transX0 + progress * (finalParams.transX - transX0);
                mAnimParams.transY = transY0 + progress * (finalParams.transY - transY0);
                mAnimParams.scale = scale0 + progress * (finalParams.scale - scale0);
                Log.i("Launcher:FolderIcon", "addUpdateListener1 " + mInfo.title);
                invalidate();
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
            }
        });
        va.setDuration(duration);
        va.start();
    }

    public void setTextVisible(boolean visible) {
        Log.i("Launcher:FolderIcon", "setTextVisible");
        if (visible) {
            mFolderName.setVisibility(VISIBLE);
        } else {
            mFolderName.setVisibility(INVISIBLE);
        }
    }

    public boolean getTextVisible() {
        return mFolderName.getVisibility() == VISIBLE;
    }

    public void onItemsChanged() {
        Log.i("Launcher:FolderIcon", "onItemsChanged");
        invalidate();
        requestLayout();
    }

    public void onAdd(ShortcutInfo item) {
        /**
         * M: added for unread feature, when add a item to a folder, we need to update
         * the unread num of the folder.@{
         */
        Log.i("Launcher:FolderIcon", "onAdd");
        final ComponentName componentName = item.intent.getComponent();
        updateFolderUnreadNum(componentName, item.unreadNum);
        /**@}**/

        invalidate();
        requestLayout();
    }

    public void onRemove(ShortcutInfo item) {

        /**M: added for Unread feature, when remove a item from a folder, we need to update
         *  the unread num of the folder.@{
         */
        Log.i("Launcher:FolderIcon", "onRemove");
        final ComponentName componentName = item.intent.getComponent();
        updateFolderUnreadNum(componentName, item.unreadNum);
        /**@}**/

        invalidate();
        requestLayout();
    }

    public void onTitleChanged(CharSequence title) {
        Log.i("Launcher:FolderIcon", "onTitleChanged");
        mFolderName.setText(title);
        setContentDescription(String.format(getContext().getString(R.string.folder_name_format),
                title));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("Launcher:FolderIcon", "onTouchEvent");
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        // Check for a stylus button press, if it occurs cancel any long press checks.
        if (mStylusEventHelper.checkAndPerformStylusEvent(event)) {
            mLongPressHelper.cancelLongPress();
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!Utilities.pointInView(this, event.getX(), event.getY(), mSlop)) {
                    mLongPressHelper.cancelLongPress();
                }
                break;
        }
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        Log.i("Launcher:FolderIcon", "onAttachedToWindow");
        super.onAttachedToWindow();
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        Log.i("Launcher:FolderIcon", "cancelLongPress");
        mLongPressHelper.cancelLongPress();
    }

    /**M: Added for unread message feature.@{**/

    /**
     * M: Update the unread message number of the shortcut with the given value.
     *
     * @param unreadNum the number of the unread message.
     */
    public void setFolderUnreadNum(int unreadNum) {

        Log.i("Launcher:FolderIcon", "setFolderUnreadNum");
        if (unreadNum <= 0) {
            mInfo.unreadNum = 0;
        } else {
            mInfo.unreadNum = unreadNum;
        }
    }

    /**
     * M: Update unread number of the folder, the number is the total unread number
     * of all shortcuts in folder, duplicate shortcut will be only count once.
     */
    public void updateFolderUnreadNum() {
        final ArrayList<ShortcutInfo> contents = mInfo.contents;
        final int contentsCount = contents.size();
        int unreadNumTotal = 0;
        final ArrayList<ComponentName> components = new ArrayList<ComponentName>();
        ShortcutInfo shortcutInfo = null;
        ComponentName componentName = null;
        int unreadNum = 0;
        for (int i = 0; i < contentsCount; i++) {
            shortcutInfo = contents.get(i);
            componentName = shortcutInfo.intent.getComponent();
            //unreadNum = MTKUnreadLoader.getUnreadNumberOfComponent(componentName);
            if (unreadNum > 0) {
                shortcutInfo.unreadNum = unreadNum;
                int j = 0;
                for (j = 0; j < components.size(); j++) {
                    if (componentName != null && componentName.equals(components.get(j))) {
                        break;
                    }
                }
                if (j >= components.size()) {
                    components.add(componentName);
                    unreadNumTotal += unreadNum;
                }
            }
        }
        setFolderUnreadNum(unreadNumTotal);
    }

    /**
     * M: Update the unread message of the shortcut with the given information.
     *
     * @param unreadNum the number of the unread message.
     */
    public void updateFolderUnreadNum(ComponentName component, int unreadNum) {
        Log.i("Launcher:FolderIcon", "updateFolderUnreadNum");
        final ArrayList<ShortcutInfo> contents = mInfo.contents;
        final int contentsCount = contents.size();
        int unreadNumTotal = 0;
        ShortcutInfo appInfo = null;
        ComponentName name = null;
        final ArrayList<ComponentName> components = new ArrayList<ComponentName>();
        for (int i = 0; i < contentsCount; i++) {
            appInfo = contents.get(i);
            name = appInfo.intent.getComponent();
            if (name != null && name.equals(component)) {
                appInfo.unreadNum = unreadNum;
            }
            if (appInfo.unreadNum > 0) {
                int j = 0;
                for (j = 0; j < components.size(); j++) {
                    if (name != null && name.equals(components.get(j))) {
                        break;
                    }
                }
                if (j >= components.size()) {
                    components.add(name);
                    unreadNumTotal += appInfo.unreadNum;
                }
            }
        }
        setFolderUnreadNum(unreadNumTotal);
    }
    /**@**/
}
