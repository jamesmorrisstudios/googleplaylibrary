package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.app.Activity;
import android.content.Context;
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
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardMetaItem;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.LeaderboardMetaAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.LeaderboardMetaContainer;
import com.jamesmorrisstudios.googleplaylibrary.util.UtilsAds;
import com.mopub.nativeads.MoPubRecyclerAdapter;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaFragment extends BaseRecycleListFragment {
    public static final String TAG = "LeaderboardMetaFragment";
    private OnLeaderboardMetaListener leaderboardMetaListener;
    private String[] leaderboardIds = null;
    private MoPubRecyclerAdapter myMoPubAdapter;
    private BaseRecycleAdapter adapter;

    @NonNull
    @Override
    protected BaseRecycleAdapter getAdapter(@NonNull BaseRecycleAdapter.OnRecycleAdapterEventsListener mListener) {
        adapter = new LeaderboardMetaAdapter(mListener);
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

    @NonNull
    @Override
    protected final RecyclerView.Adapter getAdapterToSet() {
        if (myMoPubAdapter != null && !UtilsVersion.isPro()) {
            return myMoPubAdapter;
        }
        return adapter;
    }

    @Override
    protected boolean includeSearch() {
        return false;
    }

    @Override
    public void itemClicked(int position) {
        if (myMoPubAdapter != null && !UtilsVersion.isPro()) {
            itemClicker(adapter.getItems().get(myMoPubAdapter.getOriginalPosition(position)).data);
        } else {
            itemClicker(adapter.getItems().get(position).data);
        }
    }

    public void onDestroy() {
        if (myMoPubAdapter != null) {
            myMoPubAdapter.destroy();
        }
        super.onDestroy();
    }

    /**
     * @param context Activity to attach to
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            leaderboardMetaListener = (OnLeaderboardMetaListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnLeaderboardMetaListener");
        }
    }

    /**
     * Detach from activity
     */
    @Override
    public void onDetach() {
        super.onDetach();
        leaderboardMetaListener = null;
    }

    @Override
    protected void saveState(@NonNull Bundle bundle) {
        if (leaderboardIds != null) {
            bundle.putStringArray("leaderboardIds", leaderboardIds);
        }
    }

    @Override
    protected void restoreState(@NonNull Bundle bundle) {
        if (bundle.containsKey("leaderboardIds")) {
            leaderboardIds = bundle.getStringArray("leaderboardIds");
        }
    }

    @Override
    protected void startDataLoad(boolean forceRefresh) {
        if (leaderboardIds != null) {
            GooglePlayCalls.getInstance().loadLeaderboardsMeta(forceRefresh, leaderboardIds);
        }
    }

    @Override
    protected void startMoreDataLoad() {

    }

    private void applyData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if (GooglePlayCalls.getInstance().hasLeaderboardsMeta()) {
            ArrayList<LeaderboardMetaItem> items = GooglePlayCalls.getInstance().getLeaderboardsMeta();
            for (LeaderboardMetaItem item : items) {
                data.add(new LeaderboardMetaContainer(item));
            }
        }
        applyData(data);
    }

    @Override
    protected void itemClick(@NonNull BaseRecycleContainer baseRecycleContainer) {

    }

    protected void itemClicker(@NonNull BaseRecycleContainer baseRecycleContainer) {
        LeaderboardMetaItem item = (LeaderboardMetaItem) baseRecycleContainer.getItem();
        leaderboardMetaListener.loadLeaderboardFragment(item.leaderboardId);
    }

    @Override
    protected void itemMove(int i, int i1) {

    }

    @Override
    protected boolean supportsHeaders() {
        return false;
    }

    @Override
    protected boolean allowReording() {
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        leaderboardMetaListener.setSpinnerVisibility(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        leaderboardMetaListener.setSpinnerVisibility(true);
    }

    @Subscribe
    public void onGooglePlayEvent(@NonNull GooglePlay.GooglePlayEvent event) {
        switch (event) {
            case LEADERBOARD_SPINNER_CHANGE:
                startRefresh(false);
                break;
            case LEADERBOARDS_META_READY:
                applyData();
                break;
            case LEADERBOARDS_META_FAIL:
                new SnackbarRequest(AppBase.getContext().getString(R.string.loading_failed), SnackbarRequest.SnackBarDuration.SHORT).execute();
                applyData();
                break;
        }
    }

    @Override
    protected void setStartData(@Nullable Bundle bundle, int i) {
        if (bundle != null && bundle.containsKey("leaderboardIds")) {
            leaderboardIds = bundle.getStringArray("leaderboardIds");
        }
    }

    @Override
    protected int getOptionsMenuRes() {
        return 0;
    }

    @Override
    protected boolean usesOptionsMenu() {
        return false;
    }

    @Override
    public boolean showToolbarTitle() {
        return false;
    }

    @Override
    protected void registerBus() {
        Bus.register(this);
    }

    @Override
    protected void unregisterBus() {
        Bus.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        String adId = UtilsAds.getMopubNativeAdIdFull();
        if (myMoPubAdapter != null && adId != null && !UtilsVersion.isPro()) {
            myMoPubAdapter.loadAds(adId);
        }
    }

    @Override
    protected void afterViewCreated() {
        setEnablePullToRefresh(true);
        hideFab();
    }

    public interface OnLeaderboardMetaListener {

        boolean loadLeaderboardFragment(String leaderboardId);

        void setSpinnerVisibility(boolean visible);

    }

}
