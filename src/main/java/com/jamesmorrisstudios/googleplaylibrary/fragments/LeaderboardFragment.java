package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.UtilsVersion;
import com.jamesmorrisstudios.appbaselibrary.activityHandlers.SnackbarRequest;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardItem;
import com.jamesmorrisstudios.googleplaylibrary.dialogRequests.CompareProfilesRequest;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.LeaderboardAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.LeaderboardContainer;
import com.jamesmorrisstudios.googleplaylibrary.util.UtilsAds;
import com.mopub.nativeads.MoPubRecyclerAdapter;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Leaderboard views fragment
 *
 * Created by James on 6/6/2015.
 */
public final class LeaderboardFragment extends BaseRecycleListFragment {
    public static final String TAG = "LeaderboardFragment";
    private OnLeaderboardListener leaderboardListener;
    private String leaderboardId = null;
    private MoPubRecyclerAdapter myMoPubAdapter;
    private BaseRecycleAdapter adapter;

    /**
     * Creates the custom adapter for leaderboards and if ads are enabled it wraps in in the mopub adapter
     * @param mListener Adapter listener
     * @return Custom recycle adapter
     */
    @NonNull
    @Override
    protected BaseRecycleAdapter getAdapter(@NonNull BaseRecycleAdapter.OnRecycleAdapterEventsListener mListener) {
        adapter = new LeaderboardAdapter(mListener);
        if (!UtilsVersion.isPro()) {
            // Pass the recycler Adapter your original adapter.
            myMoPubAdapter = new MoPubRecyclerAdapter(getActivity(), adapter);
            // Create a view binder that describes your native ad layout.
            ViewBinder myViewBinder = new ViewBinder.Builder(R.layout.list_native_ad)
                    .titleId(R.id.title)
                    .textId(R.id.text)
                    .iconImageId(R.id.icon)
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
     * Show the leaderboard spinners
     */
    @Override
    protected final void afterViewCreated() {
        setEnablePullToRefresh(true);
        hideFab();
    }

    @Override
    public void onStart() {
        super.onStart();
        leaderboardListener.setSpinnerVisibility(true);
    }

    /**
     * Start ads loading if enabled.
     */
    @Override
    public final void onResume() {
        super.onResume();
        String adId = UtilsAds.getMopubNativeAdId();
        if (myMoPubAdapter != null && adId != null && !UtilsVersion.isPro()) {
            myMoPubAdapter.loadAds(adId);
        }
    }

    /**
     * Clear the leaderboard cache
     */
    @Override
    public final void onStop() {
        super.onStop();
        GooglePlayCalls.getInstance().clearLeaderboardsCache();
        leaderboardListener.setSpinnerVisibility(false);
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
     * Save the leaderboard id list
     * @param bundle Bundle
     */
    @Override
    protected final void saveState(@NonNull Bundle bundle) {
        if (leaderboardId != null) {
            bundle.putString("leaderboardId", leaderboardId);
        }
    }

    /**
     * Restore the leaderboard id list
     * @param bundle Bundle
     */
    @Override
    protected final void restoreState(@NonNull Bundle bundle) {
        if (bundle.containsKey("leaderboardId")) {
            leaderboardId = bundle.getString("leaderboardId");
        }
    }

    /**
     * Set the initial data
     * @param startBundle Start bundle
     * @param startScrollY Start scroll Y position
     */
    @Override
    protected final void setStartData(@Nullable Bundle startBundle, int startScrollY) {
        if (startBundle != null && startBundle.containsKey("leaderboardId")) {
            leaderboardId = startBundle.getString("leaderboardId");
        }
    }

    /**
     * Event subscriber for checking if leaderboards are ready
     * @param event Event
     */
    @Subscribe
    public final void onGooglePlayEvent(@NonNull GooglePlay.GooglePlayEvent event) {
        switch (event) {
            case LEADERBOARD_SPINNER_CHANGE:
                startRefresh(false);
                break;
            case LEADERBOARDS_READY:
                applyData();
                break;
            case LEADERBOARDS_FAIL:
                new SnackbarRequest(AppBase.getContext().getString(R.string.loading_failed), SnackbarRequest.SnackBarDuration.SHORT).execute();
                applyData();
                break;
            case LEADERBOARDS_MORE_READY:
                appendData();
                break;
            case LEADERBOARDS_MORE_FAIL:
                new SnackbarRequest(AppBase.getContext().getString(R.string.loading_failed), SnackbarRequest.SnackBarDuration.SHORT).execute();
                break;
        }
    }

    /**
     * Begin loading of data
     * @param forceRefresh True to force a reload of data
     */
    @Override
    protected final void startDataLoad(boolean forceRefresh) {
        GooglePlayCalls.getInstance().loadLeaderboards(forceRefresh, leaderboardId);
    }

    /**
     * Load more data if we have scrolled to the end position.
     */
    @Override
    protected final void startMoreDataLoad() {
        GooglePlayCalls.getInstance().loadLeaderboardsMore();
    }

    /**
     * Apply the data to the list
     */
    private void applyData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if (GooglePlayCalls.getInstance().hasLeaderboards()) {
            ArrayList<LeaderboardItem> items = GooglePlayCalls.getInstance().getLeaderboards();
            for (LeaderboardItem item : items) {
                data.add(new LeaderboardContainer(item));
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
        LeaderboardItem item = (LeaderboardItem) baseRecycleContainer.getItem();
        Bus.postObject(new CompareProfilesRequest(item.player));
    }

    @Override
    protected final void itemMove(int fromPosition, int toPosition) {

    }

    @Override
    protected final boolean allowReording() {
        return false;
    }

    @Override
    protected final boolean includeSearch() {
        return false;
    }







    /**
     * @param context Context to attach to
     */
    @Override
    public final void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            leaderboardListener = (OnLeaderboardListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnLeaderboardListener");
        }
    }

    /**
     * Detach from activity
     */
    @Override
    public final void onDetach() {
        super.onDetach();
        leaderboardListener = null;
    }





    private void appendData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if (GooglePlayCalls.getInstance().hasLeaderboardsMore()) {
            ArrayList<LeaderboardItem> items = GooglePlayCalls.getInstance().getLeaderboardsMore();
            for (LeaderboardItem item : items) {
                data.add(new LeaderboardContainer(item));
            }
        }
        appendData(data);
    }





    @Override
    protected final boolean supportsHeaders() {
        return false;
    }









    @Override
    protected final int getOptionsMenuRes() {
        return 0;
    }

    @Override
    protected final boolean usesOptionsMenu() {
        return false;
    }

    @Override
    public final boolean showToolbarTitle() {
        return false;
    }

    @Override
    protected final void registerBus() {
        Bus.register(this);
    }

    @Override
    protected final void unregisterBus() {
        Bus.unregister(this);
    }





    public interface OnLeaderboardListener {
        void setSpinnerVisibility(boolean visible);
    }

}
