package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.support.annotation.NonNull;

import com.google.android.gms.games.achievement.Achievement;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementItem;

/**
 * Created by James on 5/27/2015.
 */
public class GooglePlayCallsBase {

    /**
     * Gets the Achievements state
     *
     * @param ach Achievement
     * @return The Achievement state
     */
    @NonNull
    protected final AchievementItem.AchievementState getAchievementState(@NonNull Achievement ach) {
        switch (ach.getState()) {
            case Achievement.STATE_HIDDEN:
                return AchievementItem.AchievementState.HIDDEN;
            case Achievement.STATE_REVEALED:
                return AchievementItem.AchievementState.REVEALED;
            case Achievement.STATE_UNLOCKED:
                return AchievementItem.AchievementState.UNLOCKED;
            default:
                return AchievementItem.AchievementState.REVEALED;
        }
    }


}
