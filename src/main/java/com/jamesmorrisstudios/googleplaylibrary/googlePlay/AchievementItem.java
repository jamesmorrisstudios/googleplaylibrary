package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.net.Uri;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementItem extends BaseRecycleItem {
    public final String title, description;
    public final Uri imageRevealedUri, imageUnlockedUri;
    public final long xp;
    public final int currentSteps, totalSteps;
    public final AchievementState state;

    /**
     * Achievement state
     */
    public enum AchievementState {
        HIDDEN, REVEALED, UNLOCKED
    }

    /**
     * Achievement item container. This is built directly out of the google ic_play_match version and container
     * all the data needed for achievements in game
     * @param title Title
     * @param description Description
     * @param imageRevealedUri Uri of revealed image
     * @param imageUnlockedUri Uri of unlocked image
     * @param xp Amount of XP this unlocked
     * @param currentSteps Number of steps complete.
     * @param totalSteps Total bitIndex of steps
     * @param state Current state (unlocked, revealed, etc)
     */
    public AchievementItem(String title, String description,
                           Uri imageRevealedUri, Uri imageUnlockedUri,
                           long xp, int currentSteps, int totalSteps, AchievementState state) {
        this.title = title;
        this.description = description;
        this.imageRevealedUri = imageRevealedUri;
        this.imageUnlockedUri = imageUnlockedUri;
        this.xp = xp;
        this.currentSteps = currentSteps;
        this.totalSteps = totalSteps;
        this.state = state;
    }

}
