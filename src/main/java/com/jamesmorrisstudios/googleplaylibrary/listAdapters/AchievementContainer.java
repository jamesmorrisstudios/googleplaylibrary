package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementHeader;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementItem;

/**
 * Achievement data container.
 *
 * Created by James on 5/27/2015.
 */
public class AchievementContainer extends BaseRecycleContainer {
    private AchievementHeader header;
    private AchievementItem item;

    /**
     * Constructor for header
     * @param header Header data
     */
    public AchievementContainer(AchievementHeader header) {
        super(true);
        this.header = header;
    }

    /**
     * Constructor for item
     * @param item Item data
     */
    public AchievementContainer(AchievementItem item) {
        super(false);
        this.item = item;
    }

    /**
     * If using the header this cannot be null
     * @return The header data
     */
    @NonNull
    @Override
    public BaseRecycleItem getHeader() {
        return header;
    }

    /**
     * If using the item this cannot be null
     * @return The item data
     */
    @NonNull
    @Override
    public BaseRecycleItem getItem() {
        return item;
    }

}
