package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.OnlineLoadHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.OnlineSaveItem;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.OnlineLoadGameAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.OnlineLoadGameContainer;
import com.jamesmorrisstudios.googleplaylibrary.util.AdUsage;
import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.Utils;
import com.mopub.nativeads.MoPubRecyclerAdapter;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 8/3/2015.
 */
public class OnlineLoadGameFragment extends BaseRecycleListFragment {
    public static final String TAG = "OnlineLoadGameFragment";

    private MoPubRecyclerAdapter myMoPubAdapter;
    private BaseRecycleAdapter adapter;

    @Override
    protected BaseRecycleAdapter getAdapter(@NonNull BaseRecycleAdapter.OnRecycleAdapterEventsListener mListener) {
        adapter = new OnlineLoadGameAdapter(mListener);
        if(AdUsage.getAdsEnabled()) {
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

    @Override
    public void onDestroy() {
        if(myMoPubAdapter != null) {
            myMoPubAdapter.destroy();
        }
        super.onDestroy();
    }

    /**
     * Event subscriber for checking if achievements are ready
     * @param event Event
     */
    @Subscribe
    public final void onGooglePlayEvent(@NonNull GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case ONLINE_SAVE_ITEM_LOAD_READY:
                applyData();
                break;
            case ONLINE_SAVE_ITEM_LOAD_FAIL:
                Utils.toastShort(getString(R.string.loading_failed));
                applyData();
                break;
        }
    }

    private void applyData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        ArrayList<OnlineSaveItem> items;
        if(GooglePlayCalls.getInstance().hasOnlineSaveItems(GooglePlay.SaveType.INVITATION)) {
            data.add(new OnlineLoadGameContainer(new OnlineLoadHeader(getString(R.string.invitations))));
            items = GooglePlayCalls.getInstance().getOnlineSaveItems(GooglePlay.SaveType.INVITATION);
            for(OnlineSaveItem item : items) {
                data.add(new OnlineLoadGameContainer(item));
            }
        }
        if(GooglePlayCalls.getInstance().hasOnlineSaveItems(GooglePlay.SaveType.YOUR_TURN)) {
            data.add(new OnlineLoadGameContainer(new OnlineLoadHeader(getString(R.string.your_turn))));
            items = GooglePlayCalls.getInstance().getOnlineSaveItems(GooglePlay.SaveType.YOUR_TURN);
            for(OnlineSaveItem item : items) {
                data.add(new OnlineLoadGameContainer(item));
            }
        }
        if(GooglePlayCalls.getInstance().hasOnlineSaveItems(GooglePlay.SaveType.THEIR_TURN)) {
            data.add(new OnlineLoadGameContainer(new OnlineLoadHeader(getString(R.string.their_turn))));
            items = GooglePlayCalls.getInstance().getOnlineSaveItems(GooglePlay.SaveType.THEIR_TURN);
            for(OnlineSaveItem item : items) {
                data.add(new OnlineLoadGameContainer(item));
            }
        }
        if(GooglePlayCalls.getInstance().hasOnlineSaveItems(GooglePlay.SaveType.COMPLETE)) {
            data.add(new OnlineLoadGameContainer(new OnlineLoadHeader(getString(R.string.completed))));
            items = GooglePlayCalls.getInstance().getOnlineSaveItems(GooglePlay.SaveType.COMPLETE);
            for(OnlineSaveItem item : items) {
                data.add(new OnlineLoadGameContainer(item));
            }
        }
        applyData(data);



        //data.add(new OnlineLoadGameContainer(new OnlineLoadHeader(getString(R.string.invitations))));
       // data.add(new OnlineLoadGameContainer(new OnlineSaveItem(true)));
        //data.add(new OnlineLoadGameContainer(new OnlineLoadHeader(getString(R.string.your_turn))));
        //data.add(new OnlineLoadGameContainer(new OnlineSaveItem(false)));
        //data.add(new OnlineLoadGameContainer(new OnlineLoadHeader(getString(R.string.their_turn))));
        //data.add(new OnlineLoadGameContainer(new OnlineSaveItem(true)));
        //data.add(new OnlineLoadGameContainer(new OnlineLoadHeader(getString(R.string.completed))));
        //data.add(new OnlineLoadGameContainer(new OnlineSaveItem(false)));

    }

    @Override
    protected void startDataLoad(boolean forced) {
        GooglePlayCalls.getInstance().loadOnlineSaves(forced);
    }

    @Override
    protected void startMoreDataLoad() {

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
            myMoPubAdapter.loadAds(AdUsage.getMopubNativeAdIdFull());
        }
    }
}
