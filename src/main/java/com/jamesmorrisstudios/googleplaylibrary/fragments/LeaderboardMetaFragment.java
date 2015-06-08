package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.LeaderboardMetaAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.LeaderboardMetaContainer;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaFragment extends BaseRecycleListFragment {
    public static final String TAG = "LeaderboardMetaFragment";
    private OnLeaderboardMetaListener leaderboardMetaListener;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bus.register(this);
    }

    public void onDestroy() {
        super.onDestroy();
        Bus.unregister(this);
    }

    /**
     * @param activity Activity to attach to
     */
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            leaderboardMetaListener = (OnLeaderboardMetaListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLeaderboardMetaListener");
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
    protected BaseRecycleAdapter getAdapter(int i, BaseRecycleAdapter.OnItemClickListener onItemClickListener) {
        return new LeaderboardMetaAdapter(i, onItemClickListener);
    }

    @Override
    protected void startDataLoad(boolean b) {
        ArrayList<BaseRecycleContainer> items = new ArrayList<>();

        items.add(new LeaderboardMetaContainer(false));
        items.add(new LeaderboardMetaContainer(false));
        items.add(new LeaderboardMetaContainer(false));

        applyData(items);
    }

    @Override
    protected void itemClick(@NonNull BaseRecycleContainer baseRecycleContainer) {

    }

    @Override
    public void onBack() {
        leaderboardMetaListener.setLeaderboardSpinnerVisibility(false);
    }

    @Subscribe
    public void onGooglePlayEvent(GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case LEADERBOARD_SPINNER_CHANGE:
                Log.v(TAG, "Spinner value changed: Span Is "+leaderboardMetaListener.getLeaderboardSpinnerSpan().toString()
                        +" Collection is "+leaderboardMetaListener.getLeaderboardSpinnerCollection().toString());
                break;
        }
    }

    @Override
    public boolean showToolbarTitle() {
        return false;
    }

    @Override
    protected void afterViewCreated() {
        leaderboardMetaListener.setLeaderboardSpinnerVisibility(true);
    }

    public interface OnLeaderboardMetaListener {

        void setLeaderboardSpinnerVisibility(boolean visible);

        GooglePlay.Span getLeaderboardSpinnerSpan();

        GooglePlay.Collection getLeaderboardSpinnerCollection();
    }

}
