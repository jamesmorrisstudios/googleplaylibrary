package com.jamesmorrisstudios.googleplaylibrary.fragments;

import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementsAdapter;

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

    @Override
    protected void startDataLoad(boolean b) {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();


        applyData(data);
    }

    @Override
    protected void itemClicked(BaseRecycleItem baseRecycleItem) {

    }

    @Override
    public void onBack() {

    }

    @Override
    protected void afterViewCreated() {

    }
}
