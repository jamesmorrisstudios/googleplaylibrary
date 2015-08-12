package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.PlayerHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.PlayerItem;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.PlayerPickerAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.PlayerPickerContainer;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerPickerFragment extends BaseRecycleListFragment {
    public static final String TAG = "PlayerPickerFragment";

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bus.register(this);
    }

    public void onDestroy() {
        super.onDestroy();
        Bus.unregister(this);
    }

    @Override
    protected BaseRecycleAdapter getAdapter(int i, @NonNull BaseRecycleAdapter.OnItemClickListener onItemClickListener) {
        return new PlayerPickerAdapter(i ,onItemClickListener);
    }

    @Subscribe
    public void onGooglePlayEvent(GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case PLAYERS_ACTIVE_READY:
                applyDataActive();
                break;
            case PLAYERS_ACTIVE_FAIL:
                Utils.toastShort(getString(R.string.failed_load_google_page));
                applyDataActive();
                break;
            case PLAYERS_ALL_READY:
                applyDataAll();
                break;
            case PLAYERS_ALL_FAIL:
                Utils.toastShort(getString(R.string.failed_load_google_page));
                break;
            case PLAYERS_ALL_MORE_READY:
                applyDataAllMore();
                break;
            case PLAYERS_ALL_MORE_FAIL:
                Utils.toastShort(getString(R.string.failed_load_google_page));
                break;
        }
    }

    private void applyDataActive() {
        Log.v("PlayerPicker", "Apply Data Active");
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if(GooglePlayCalls.getInstance().hasPlayersActive()) {
            ArrayList<PlayerItem> items = GooglePlayCalls.getInstance().getPlayersActive();
            data.add(new PlayerPickerContainer(new PlayerHeader("Active Players")));
            for(PlayerItem item : items) {
                data.add(new PlayerPickerContainer(item));
            }
        }
        applyData(data);
        GooglePlayCalls.getInstance().loadPlayersAll(false);
    }

    private void applyDataAll() {
        Log.v("PlayerPicker", "Apply Data All");
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if(GooglePlayCalls.getInstance().hasPlayersAll()) {
            ArrayList<PlayerItem> items = GooglePlayCalls.getInstance().getPlayersAll();
            data.add(new PlayerPickerContainer(new PlayerHeader("Players in your Circles")));
            for(PlayerItem item : items) {
                data.add(new PlayerPickerContainer(item));
            }
        }
        appendData(data, true);
    }

    private void applyDataAllMore() {
        Log.v("PlayerPicker", "Apply Data All");
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if(GooglePlayCalls.getInstance().hasPlayersAllMore()) {
            ArrayList<PlayerItem> items = GooglePlayCalls.getInstance().getPlayersAllMore();
            for(PlayerItem item : items) {
                data.add(new PlayerPickerContainer(item));
            }
        }
        appendData(data, false);
    }

    @Override
    protected void startDataLoad(boolean forceRefresh) {
        Log.v("PlayerPicker", "Start Data Load");
        GooglePlayCalls.getInstance().loadPlayersActive(forceRefresh);
    }

    @Override
    protected void startMoreDataLoad() {
        GooglePlayCalls.getInstance().loadPlayersAllMore();
    }

    @Override
    protected void itemClick(@NonNull BaseRecycleContainer baseRecycleContainer) {

    }

    @Override
    public void onBack() {

    }

    @Override
    public boolean showToolbarTitle() {
        return true;
    }

    @Override
    protected void saveState(Bundle bundle) {

    }

    @Override
    protected void restoreState(Bundle bundle) {

    }

    @Override
    protected void afterViewCreated() {
        setEnablePullToRefresh(true);
    }
}
