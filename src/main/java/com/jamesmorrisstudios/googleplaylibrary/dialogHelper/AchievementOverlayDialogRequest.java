package com.jamesmorrisstudios.googleplaylibrary.dialogHelper;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementContainer;

/**
 * Created by James on 8/11/2015.
 */
public class AchievementOverlayDialogRequest {
    public final AchievementContainer item;

    public AchievementOverlayDialogRequest(@NonNull AchievementContainer item) {
        this.item = item;
    }

}
