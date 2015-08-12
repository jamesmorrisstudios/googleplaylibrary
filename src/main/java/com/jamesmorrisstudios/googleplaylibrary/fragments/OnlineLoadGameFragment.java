package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;

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
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 8/3/2015.
 */
public class OnlineLoadGameFragment extends BaseRecycleListFragment {
    public static final String TAG = "OnlineLoadGameFragment";


    @Override
    protected BaseRecycleAdapter getAdapter(int i, @NonNull BaseRecycleAdapter.OnItemClickListener onItemClickListener) {
        return new OnlineLoadGameAdapter(i, onItemClickListener);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Bus.unregister(this);
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
                Utils.toastShort(getString(R.string.failed_load_google_page));
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
