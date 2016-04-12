package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.data.PlayerPickerHeader;
import com.jamesmorrisstudios.googleplaylibrary.data.PlayerPickerItem;

/**
 * Player picker data container.
 *
 * Created by James on 8/11/2015.
 */
public class PlayerPickerContainer extends BaseRecycleContainer {
    private final PlayerPickerHeader header;
    private final PlayerPickerItem item;

    /**
     * Constructor for header
     * @param header Header data
     */
    public PlayerPickerContainer(@NonNull PlayerPickerHeader header) {
        super(true);
        this.header = header;
        this.item = null;
    }

    /**
     * Constructor for item
     * @param item Item data
     */
    public PlayerPickerContainer(@NonNull PlayerPickerItem item) {
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
