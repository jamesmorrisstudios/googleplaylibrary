package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.dialogHelper.PlayerDetailsDialogRequest;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.LeaderboardItem;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.LeaderboardAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.LeaderboardContainer;
import com.jamesmorrisstudios.googleplaylibrary.util.AdUsage;
import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.Utils;
import com.mopub.nativeads.MoPubRecyclerAdapter;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardFragment extends BaseRecycleListFragment {
    public static final String TAG = "LeaderboardFragment";
    private OnLeaderboardListener leaderboardListener;

    private String leaderboardId = null;
    private MoPubRecyclerAdapter myMoPubAdapter;
    private BaseRecycleAdapter adapter;

    @Override
    protected BaseRecycleAdapter getAdapter(@NonNull BaseRecycleAdapter.OnRecycleAdapterEventsListener mListener) {
        adapter = new LeaderboardAdapter(mListener);
        if(AdUsage.getAdsEnabled()) {
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

    @Override
    protected final RecyclerView.Adapter getAdapterToSet() {
        if(myMoPubAdapter != null && AdUsage.getAdsEnabled()) {
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
        if(myMoPubAdapter != null && AdUsage.getAdsEnabled()) {
            itemClicker(adapter.getItems().get(myMoPubAdapter.getOriginalPosition(position)).data);
        } else {
            itemClicker(adapter.getItems().get(position).data);
        }
    }

    public void onDestroy() {
        if(myMoPubAdapter != null) {
            myMoPubAdapter.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void saveState(Bundle bundle) {
        if(leaderboardId != null) {
            bundle.putString("leaderboardId", leaderboardId);
        }
    }

    @Override
    protected void restoreState(Bundle bundle) {
        if(bundle != null) {
            if(bundle.containsKey("leaderboardId")) {
                leaderboardId = bundle.getString("leaderboardId");
            }
        }
    }

    /**
     * @param activity Activity to attach to
     */
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            leaderboardListener = (OnLeaderboardListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLeaderboardListener");
        }
    }

    /**
     * Detach from activity
     */
    @Override
    public void onDetach() {
        super.onDetach();
        leaderboardListener = null;
    }

    @Override
    protected void startDataLoad(boolean forceRefresh) {
        Log.v("LeaderboardFragment", "Start data load");
        GooglePlayCalls.getInstance().loadLeaderboards(forceRefresh, leaderboardId);
    }

    @Override
    protected void startMoreDataLoad() {
        Log.v("LeaderboardFragment", "Start more data load");
        GooglePlayCalls.getInstance().loadLeaderboardsMore();
    }

    private void applyData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if(GooglePlayCalls.getInstance().hasLeaderboards()) {
            ArrayList<LeaderboardItem> items = GooglePlayCalls.getInstance().getLeaderboards();
            for(LeaderboardItem item : items) {
                data.add(new LeaderboardContainer(item));
            }
        }
        applyData(data);
    }

    private void appendData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if(GooglePlayCalls.getInstance().hasLeaderboardsMore()) {
            ArrayList<LeaderboardItem> items = GooglePlayCalls.getInstance().getLeaderboardsMore();
            for(LeaderboardItem item : items) {
                data.add(new LeaderboardContainer(item));
            }
        }
        appendData(data);
    }

    @Override
    protected void itemClick(@NonNull BaseRecycleContainer baseRecycleContainer) {

    }

    protected void itemClicker(@NonNull BaseRecycleContainer baseRecycleContainer) {
        Log.v("LeaderboardFragment", "Load player details");
        LeaderboardItem item = (LeaderboardItem)baseRecycleContainer.getItem();
        Bus.postObject(new PlayerDetailsDialogRequest(item.player));
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
        leaderboardListener.setLeaderboardSpinnerVisibility(false);
    }

    @Subscribe
    public void onGooglePlayEvent(GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case LEADERBOARD_SPINNER_CHANGE:
                startRefresh(false);
                break;
            case LEADERBOARDS_READY:
                applyData();
                break;
            case LEADERBOARDS_FAIL:
                Utils.toastShort(getString(R.string.loading_failed));
                applyData();
                break;
            case LEADERBOARDS_MORE_READY:
                appendData();
                break;
            case LEADERBOARDS_MORE_FAIL:
                Utils.toastShort(getString(R.string.loading_failed));
                break;
        }
    }

    @Override
    protected void setStartData(@Nullable Bundle bundle, int i) {
        if(bundle != null && bundle.containsKey("leaderboardId")) {
            leaderboardId = bundle.getString("leaderboardId");
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
    protected void afterViewCreated() {
        setEnablePullToRefresh(true);
        leaderboardListener.setLeaderboardSpinnerVisibility(true);
        if(myMoPubAdapter != null && AdUsage.getAdsEnabled()) {
            myMoPubAdapter.loadAds(AdUsage.getMopubNativeAdId());
        }
    }

    public interface OnLeaderboardListener {
        void setLeaderboardSpinnerVisibility(boolean visible);

    }

}
