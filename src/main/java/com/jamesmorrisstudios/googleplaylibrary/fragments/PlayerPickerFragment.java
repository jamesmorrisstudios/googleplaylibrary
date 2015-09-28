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
    protected BaseRecycleAdapter getAdapter(@NonNull BaseRecycleAdapter.OnItemClickListener onItemClickListener) {
        adapter = new PlayerPickerAdapter(onItemClickListener);
        if(AdUsage.getAdsEnabled()) {
            // Pass the recycler Adapter your original adapter.
            myMoPubAdapter = new MoPubRecyclerAdapter(getActivity(), adapter);
            // Create a view binder that describes your native ad layout.
            myMoPubAdapter.registerViewBinder(new ViewBinder.Builder(R.layout.list_native_ad)
                    .titleId(R.id.title)
                    .textId(R.id.text)
                    .iconImageId(R.id.icon)
                    .daaIconImageId(R.id.native_ad_daa_icon_image)
                    .build());
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

    /**
     * View creation done
     *
     * @param view               This fragments main view
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(myMoPubAdapter != null && AdUsage.getAdsEnabled()) {
            myMoPubAdapter.loadAds(AdUsage.getMopubNativeAdId());
        }
    }

    @Override
    public void itemClicked(@NonNull BaseRecycleContainer item) {
        //Override to prevent use
    }

    @Override
    public void itemClicked(int position) {
        if(myMoPubAdapter != null && AdUsage.getAdsEnabled()) {
            itemClick(adapter.getItems().get(myMoPubAdapter.getOriginalPosition(position)).data);
        } else {
            itemClick(adapter.getItems().get(position).data);
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bus.register(this);
    }

    public void onDestroy() {
        if(myMoPubAdapter != null) {
            myMoPubAdapter.destroy();
        }
        super.onDestroy();
        Bus.unregister(this);
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
