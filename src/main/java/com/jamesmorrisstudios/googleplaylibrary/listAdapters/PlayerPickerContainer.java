package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.PlayerHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.PlayerItem;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerPickerContainer extends BaseRecycleContainer {
    private final PlayerHeader header;
    private final PlayerItem item;

    public PlayerPickerContainer(PlayerHeader header) {
        super(true);
        this.header = header;
        this.item = null;
    }

    public PlayerPickerContainer(PlayerItem item) {
        super(false);
        this.header = null;
        this.item = item;
    }

    @Override
    public BaseRecycleItem getHeader() {
        return header;
    }

    @Override
    public BaseRecycleItem getItem() {
        return item;
    }

}
