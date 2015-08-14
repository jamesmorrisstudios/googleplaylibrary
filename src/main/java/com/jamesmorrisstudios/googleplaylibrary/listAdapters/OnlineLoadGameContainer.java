package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.OnlineLoadHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.OnlineSaveItem;

/**
 * Created by James on 8/3/2015.
 */
public class OnlineLoadGameContainer extends BaseRecycleContainer {
    private final OnlineLoadHeader header;
    private final OnlineSaveItem item;

    public OnlineLoadGameContainer(OnlineLoadHeader header) {
        super(true);
        this.header = header;
        this.item = null;
    }

    public OnlineLoadGameContainer(OnlineSaveItem item) {
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
