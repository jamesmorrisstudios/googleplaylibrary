package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

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
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardFragment extends BaseRecycleListFragment {
    public static final String TAG = "LeaderboardFragment";
    private OnLeaderboardListener leaderboardListener;

    private String leaderboardId = null;

    public final void setLeaderboardId(String leaderboardId) {
        this.leaderboardId = leaderboardId;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bus.register(this);
    }

    public void onDestroy() {
        super.onDestroy();
        Bus.unregister(this);
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
    protected BaseRecycleAdapter getAdapter(int i, @NonNull BaseRecycleAdapter.OnItemClickListener onItemClickListener) {
        return new LeaderboardAdapter(i, onItemClickListener);
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
        appendData(data, false);
    }

    @Override
    protected void itemClick(@NonNull BaseRecycleContainer baseRecycleContainer) {
        LeaderboardItem item = (LeaderboardItem)baseRecycleContainer.getItem();
        Bus.postObject(new PlayerDetailsDialogRequest(item.player));
    }

    @Override
    public void onBack() {
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
                Utils.toastShort(getString(R.string.failed_load_google_page));
                applyData();
                break;
            case LEADERBOARDS_MORE_READY:
                appendData();
                break;
            case LEADERBOARDS_MORE_FAIL:
                Utils.toastShort(getString(R.string.failed_load_google_page));
                break;
        }
    }

    @Override
    public boolean showToolbarTitle() {
        return false;
    }

    @Override
    protected void afterViewCreated() {
        setEnablePullToRefresh(true);
        leaderboardListener.setLeaderboardSpinnerVisibility(true);
    }

    public interface OnLeaderboardListener {

        void setLeaderboardSpinnerVisibility(boolean visible);

    }

}
