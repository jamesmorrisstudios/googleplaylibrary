package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.UtilsVersion;
import com.jamesmorrisstudios.appbaselibrary.activityHandlers.SnackbarRequest;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.dialogRequests.AchievementOverlayDialogRequest;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementHeader;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementContainer;
import com.jamesmorrisstudios.googleplaylibrary.util.UtilsAds;
import com.mopub.nativeads.MoPubRecyclerAdapter;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Achievement views fragment
 *
 * Created by James on 5/27/2015.
 */
public final class AchievementFragment extends BaseRecycleListFragment {
    public static final String TAG = "AchievementsFragment";
    private String[] achievementIds = null;
    private MoPubRecyclerAdapter myMoPubAdapter;
    private BaseRecycleAdapter adapter;

    /**
     * Creates the custom adapter for achievements and if ads are enabled it wraps in in the mopub adapter
     * @param mListener Adapter listener
     * @return Custom recycle adapter
     */
    @NonNull
    @Override
    protected final BaseRecycleAdapter getAdapter(@NonNull BaseRecycleAdapter.OnRecycleAdapterEventsListener mListener) {
        adapter = new AchievementAdapter(mListener);
        if (!UtilsVersion.isPro()) {
            // Pass the recycler Adapter your original adapter.
            myMoPubAdapter = new MoPubRecyclerAdapter(getActivity(), adapter);
            // Create a view binder that describes your native ad layout.
            ViewBinder myViewBinder = new ViewBinder.Builder(R.layout.list_native_ad_full)
                    .titleId(R.id.title)
                    .textId(R.id.text)
                    .mainImageId(R.id.image)
                    .iconImageId(R.id.icon)
                    .callToActionId(R.id.call_to_action)
                    .build();

            MoPubStaticNativeAdRenderer myRenderer = new MoPubStaticNativeAdRenderer(myViewBinder);
            myMoPubAdapter.registerAdRenderer(myRenderer);
        }
        return adapter;
    }

    /**
     * @return The set adapter. Mopub adapter if present.
     */
    @NonNull
    @Override
    protected final RecyclerView.Adapter getAdapterToSet() {
        if (myMoPubAdapter != null && !UtilsVersion.isPro()) {
            return myMoPubAdapter;
        }
        return adapter;
    }

    /**
     * Enable pull to refresh
     */
    @Override
    protected final void afterViewCreated() {
        setEnablePullToRefresh(true);
        hideFab();
    }

    /**
     * Start ads loading if enabled.
     */
    @Override
    public final void onResume() {
        super.onResume();
        String adId = UtilsAds.getMopubNativeAdIdFull();
        if (myMoPubAdapter != null && adId != null && !UtilsVersion.isPro()) {
            myMoPubAdapter.loadAds(adId);
        }
    }

    /**
     * Clear the achievement cache
     */
    @Override
    public final void onStop() {
        super.onStop();
        GooglePlayCalls.getInstance().clearAchievementsCache();
    }

    /**
     * Destroy the mopub adapter
     */
    @Override
    public final void onDestroy() {
        if (myMoPubAdapter != null) {
            myMoPubAdapter.destroy();
        }
        super.onDestroy();
    }

    /**
     * Save the achievement id list
     * @param bundle Bundle
     */
    @Override
    protected final void saveState(@NonNull Bundle bundle) {
        if (achievementIds != null) {
            bundle.putStringArray("achievementIds", achievementIds);
        }
    }

    /**
     * Restore the achievement id list
     * @param bundle Bundle
     */
    @Override
    protected final void restoreState(@NonNull Bundle bundle) {
        if (bundle.containsKey("achievementIds")) {
            achievementIds = bundle.getStringArray("achievementIds");
        }
    }

    /**
     * Set the initial data
     * @param startBundle Start bundle
     * @param startScrollY Start scroll Y position
     */
    @Override
    protected final void setStartData(@Nullable Bundle startBundle, int startScrollY) {
        if (startBundle != null && startBundle.containsKey("achievementIds")) {
            achievementIds = startBundle.getStringArray("achievementIds");
        }
    }

    /**
     * Event subscriber for checking if achievements are ready
     * @param event Event
     */
    @Subscribe
    public final void onGooglePlayEvent(@NonNull GooglePlay.GooglePlayEvent event) {
        switch (event) {
            case ACHIEVEMENTS_ITEMS_READY:
                applyData();
                break;
            case ACHIEVEMENTS_ITEMS_FAIL:
                new SnackbarRequest(AppBase.getContext().getString(R.string.loading_failed), SnackbarRequest.SnackBarDuration.SHORT).execute();
                applyData();
                break;
        }
    }

    /**
     * Begin loading of data
     * @param forceRefresh True to force a reload of data
     */
    @Override
    protected final void startDataLoad(boolean forceRefresh) {
        if (achievementIds != null) {
            GooglePlayCalls.getInstance().loadAchievements(forceRefresh, achievementIds);
        }
    }

    /**
     * Load more data if we have scrolled to the end position.
     */
    @Override
    protected final void startMoreDataLoad() {

    }

    /**
     * Apply the data to the list
     */
    private void applyData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if (GooglePlayCalls.getInstance().hasAchievements()) {
            int complete = GooglePlayCalls.getInstance().getNumberCompletedAchievements();
            int total = GooglePlayCalls.getInstance().getNumberAchievements();
            AchievementContainer header = new AchievementContainer(new AchievementHeader(getString(R.string.achievements), complete, total));
            data.add(header);
            ArrayList<AchievementItem> items = GooglePlayCalls.getInstance().getAchievements();
            for (AchievementItem item : items) {
                data.add(new AchievementContainer(item));
            }
        }
        applyData(data);
    }

    /**
     * Translates the item clicked position to the actual position around the ads.
     * @param position Clicked item position.
     */
    @Override
    public final void itemClicked(int position) {
        if (myMoPubAdapter != null && !UtilsVersion.isPro()) {
            itemClickTranslated(adapter.getItems().get(myMoPubAdapter.getOriginalPosition(position)).data);
        } else {
            itemClickTranslated(adapter.getItems().get(position).data);
        }
    }

    /**
     * Item click
     * @param baseRecycleContainer Item clicked container
     */
    @Override
    protected final void itemClick(@NonNull BaseRecycleContainer baseRecycleContainer) {

    }

    /**
     * Item click after it was translated around ads
     * @param baseRecycleContainer Clicked item
     */
    protected final void itemClickTranslated(@NonNull BaseRecycleContainer baseRecycleContainer) {
        AchievementContainer item = (AchievementContainer) baseRecycleContainer;
        Bus.postObject(new AchievementOverlayDialogRequest(item));
    }

    /**
     *
     * @return
     */
    @Override
    protected final boolean allowReording() {
        return false;
    }

    /**
     * Item was moved
     * @param fromPosition
     * @param toPosition
     */
    @Override
    protected final void itemMove(int fromPosition, int toPosition) {

    }

    /**
     * @return false
     */
    @Override
    protected final boolean includeSearch() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    protected final boolean supportsHeaders() {
        return true;
    }



    /**
     *
     * @return
     */
    @Override
    protected final boolean usesOptionsMenu() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    protected final int getOptionsMenuRes() {
        return 0;
    }

    /**
     *
     * @return
     */
    @Override
    public final boolean showToolbarTitle() {
        return true;
    }

    /**
     * Register the bus listener
     */
    @Override
    protected final void registerBus() {
        Bus.register(this);
    }

    /**
     * Unregister the bus listener
     */
    @Override
    protected final void unregisterBus() {
        Bus.unregister(this);
    }

}
