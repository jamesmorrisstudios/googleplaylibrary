package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementContainer extends BaseRecycleContainer {

    public AchievementContainer(boolean isHeader) {
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
