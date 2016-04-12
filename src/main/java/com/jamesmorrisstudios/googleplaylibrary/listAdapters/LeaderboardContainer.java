package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardHeader;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardItem;

/**
 * Leaderboard data container.
 *
 * Created by James on 6/6/2015.
 */
public class LeaderboardContainer extends BaseRecycleContainer {
    private final LeaderboardItem item;
    private final LeaderboardHeader header;

    /**
     * Constructor for header
     * @param header Header data
     */
    public LeaderboardContainer(@NonNull LeaderboardHeader header) {
        super(true);
        this.header = header;
        this.item = null;
    }

    /**
     * Constructor for item
     * @param item Item data
     */
    public LeaderboardContainer(@NonNull LeaderboardItem item) {
        super(false);
        this.header = null;
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
