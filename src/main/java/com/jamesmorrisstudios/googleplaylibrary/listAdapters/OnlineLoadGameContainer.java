package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.data.OnlineLoadHeader;
import com.jamesmorrisstudios.googleplaylibrary.data.OnlineSaveItem;

/**
 * Online load game data container.
 *
 * Created by James on 8/3/2015.
 */
public class OnlineLoadGameContainer extends BaseRecycleContainer {
    private final OnlineLoadHeader header;
    private final OnlineSaveItem item;

    /**
     * Constructor for header
     * @param header Header data
     */
    public OnlineLoadGameContainer(@NonNull OnlineLoadHeader header) {
        super(true);
        this.header = header;
        this.item = null;
    }

    /**
     * Constructor for item
     * @param item Item data
     */
    public OnlineLoadGameContainer(@NonNull OnlineSaveItem item) {
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
