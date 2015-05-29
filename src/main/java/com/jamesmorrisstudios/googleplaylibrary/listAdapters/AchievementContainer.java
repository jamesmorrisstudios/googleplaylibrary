package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementItem;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementContainer extends BaseRecycleContainer {
    private AchievementHeader header;
    private AchievementItem item;

    public AchievementContainer(AchievementHeader header) {
        super(true);
        this.header = header;
    }

    public AchievementContainer(AchievementItem item) {
        super(false);
        this.item = item;
    }

    @Override
    public BaseRecycleItem getHeaderItem() {
        return header;
    }

    @Override
    public BaseRecycleItem getItem() {
        return item;
    }
}
