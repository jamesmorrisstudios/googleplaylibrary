package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementHeader extends BaseRecycleItem {
    public final String title;
    public final int numberComplete, numberTotal;

    public AchievementHeader(String title, int numberComplete, int numberTotal) {
        this.title = title;
        this.numberComplete = numberComplete;
        this.numberTotal = numberTotal;
    }

}
