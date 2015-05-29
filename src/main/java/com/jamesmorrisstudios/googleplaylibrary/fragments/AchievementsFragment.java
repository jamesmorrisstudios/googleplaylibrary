package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementContainer;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementsAdapter;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementsFragment extends BaseRecycleListFragment {
    public static final String TAG = "AchievementsFragment";

    @Override
    protected BaseRecycleAdapter getAdapter(int i, BaseRecycleAdapter.OnItemClickListener onItemClickListener) {
        return new AchievementsAdapter(i, onItemClickListener);
    }

    public void onStart() {
        super.onStart();
        Bus.register(this);
    }

    public void onStop() {
        super.onStop();
        Bus.unregister(this);
    }

    /**
     * Event subscriber for checking if achievements are ready
     * @param event Event
     */
    @Subscribe
    public final void onGooglePlayEvent(@NonNull GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case ACHIEVEMENTS_ITEMS_READY:
                applyData();
                break;
            case ACHIEVEMENTS_ITEMS_FAIL:
                Utils.toastShort("Failed to load achievements");
                applyData();
                break;
        }
    }

    private void applyData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if(GooglePlayCalls.getInstance().hasAchievements()) {
            int complete = GooglePlayCalls.getInstance().getNumberCompletedAchievements();
            int total = GooglePlayCalls.getInstance().getNumberAchievements();
            AchievementContainer header = new AchievementContainer(new AchievementHeader("Achievements", complete, total));
            data.add(header);
            ArrayList<AchievementItem> items = GooglePlayCalls.getInstance().getAchievements();
            for(AchievementItem item : items) {
                data.add(new AchievementContainer(item));
            }
        }
        applyData(data);
    }

    @Override
    protected void startDataLoad(boolean forced) {
        GooglePlayCalls.getInstance().loadAchievements(forced);
    }

    @Override
    protected void itemClicked(BaseRecycleItem baseRecycleItem) {

    }

    @Override
    public void onBack() {

    }

    @Override
    protected void afterViewCreated() {
        setEnablePullToRefresh(true);
    }

}
