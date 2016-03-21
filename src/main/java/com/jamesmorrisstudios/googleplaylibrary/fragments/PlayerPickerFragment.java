package com.jamesmorrisstudios.googleplaylibrary.fragments;

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
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.PlayerHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.PlayerItem;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.PlayerPickerAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.PlayerPickerContainer;
import com.jamesmorrisstudios.googleplaylibrary.util.AdUsage;
import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.Utils;
import com.mopub.nativeads.MoPubRecyclerAdapter;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerPickerFragment extends BaseRecycleListFragment {
    public static final String TAG = "PlayerPickerFragment";
    private MoPubRecyclerAdapter myMoPubAdapter;
    private BaseRecycleAdapter adapter;

    @Override
    protected BaseRecycleAdapter getAdapter(@NonNull BaseRecycleAdapter.OnRecycleAdapterEventsListener mListener) {
        adapter = new PlayerPickerAdapter(mListener);
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
            itemClick(adapter.getItems().get(myMoPubAdapter.getOriginalPosition(position)).data);
        } else {
            itemClick(adapter.getItems().get(position).data);
        }
    }

    public void onDestroy() {
        if(myMoPubAdapter != null) {
            myMoPubAdapter.destroy();
        }
        super.onDestroy();
    }

    @Subscribe
    public void onGooglePlayEvent(GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case PLAYERS_ACTIVE_READY:
                applyDataActive();
                break;
            case PLAYERS_ACTIVE_FAIL:
                Utils.toastShort(getString(R.string.loading_failed));
                applyDataActive();
                break;
            case PLAYERS_ALL_READY:
                applyDataAll();
                break;
            case PLAYERS_ALL_FAIL:
                Utils.toastShort(getString(R.string.loading_failed));
                break;
            case PLAYERS_ALL_MORE_READY:
                applyDataAllMore();
                break;
            case PLAYERS_ALL_MORE_FAIL:
                Utils.toastShort(getString(R.string.loading_failed));
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
        appendData(data);
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
        appendData(data);
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
    protected void setStartData(@Nullable Bundle bundle, int i) {

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
        return true;
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
    protected void saveState(Bundle bundle) {

    }

    @Override
    protected void restoreState(Bundle bundle) {

    }

    @Override
    protected void afterViewCreated() {
        setEnablePullToRefresh(true);
        if(myMoPubAdapter != null && AdUsage.getAdsEnabled()) {
            myMoPubAdapter.loadAds(AdUsage.getMopubNativeAdId());
        }
    }

}
