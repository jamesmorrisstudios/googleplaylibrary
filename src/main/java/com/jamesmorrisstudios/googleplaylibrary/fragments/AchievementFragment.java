package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.dialogHelper.AchievementOverlayDialogRequest;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementContainer;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementFragment extends BaseRecycleListFragment {
    public static final String TAG = "AchievementsFragment";

    private String[] achievementIds = null;

    public final void setAchievementIds(@NonNull String[] achievementIds) {
        this.achievementIds = achievementIds;
    }

    @Override
    protected BaseRecycleAdapter getAdapter(int i, @NonNull BaseRecycleAdapter.OnItemClickListener onItemClickListener) {
        return new AchievementAdapter(i, onItemClickListener);
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

    @Override
    protected void saveState(Bundle bundle) {
        if(achievementIds != null) {
            bundle.putStringArray("achievementIds", achievementIds);
        }
    }

    @Override
    protected void restoreState(Bundle bundle) {
        if(bundle != null) {
            if(bundle.containsKey("achievementIds")) {
                achievementIds = bundle.getStringArray("achievementIds");
            }
        }
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
                Utils.toastShort(getString(R.string.failed_load_google_page));
                applyData();
                break;
        }
    }

    private void applyData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if(GooglePlayCalls.getInstance().hasAchievements()) {
            int complete = GooglePlayCalls.getInstance().getNumberCompletedAchievements();
            int total = GooglePlayCalls.getInstance().getNumberAchievements();
            AchievementContainer header = new AchievementContainer(new AchievementHeader(getString(R.string.achievements), complete, total));
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
        if(achievementIds != null) {
            GooglePlayCalls.getInstance().loadAchievements(forced, achievementIds);
        }
    }

    @Override
    protected void startMoreDataLoad() {

    }


    @Override
    protected void itemClick(@NonNull BaseRecycleContainer baseRecycleContainer) {
        Log.v("AchievementsFragment", "Item clicked");
        AchievementContainer item = (AchievementContainer)baseRecycleContainer;
        Bus.postObject(new AchievementOverlayDialogRequest(item));
    }

    @Override
    public void onBack() {
        GooglePlayCalls.getInstance().clearAchievementsCache();
    }

    @Override
    public boolean showToolbarTitle() {
        return true;
    }

    @Override
    protected void afterViewCreated() {
        setEnablePullToRefresh(true);
    }

}
