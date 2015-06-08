package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaContainer extends BaseRecycleContainer {

    public LeaderboardMetaContainer(boolean isHeader) {
        super(isHeader);
    }

    @Override
    public BaseRecycleItem getHeaderItem() {
        return null;
    }

    @Override
    public BaseRecycleItem getItem() {
        return null;
    }

}
