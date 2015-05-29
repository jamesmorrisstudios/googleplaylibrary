package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Logger;

import java.util.ArrayList;

/**
 * Created by James on 5/11/2015.
 */
public class GooglePlayCalls extends GooglePlayCallsBase {
    public static final String TAG = "GooglePlayCalls";
    private static GooglePlayCalls instance = null;

    //Cached Data
    private ArrayList<AchievementItem> achievements;

    /**
     * Required private constructor to enforce singleton
     */
    private GooglePlayCalls() {}

    /**
     * @return The instance of GooglePlayCalls
     */
    @NonNull
    public static GooglePlayCalls getInstance() {
        if(instance == null) {
            instance = new GooglePlayCalls();
        }
        return instance;
    }

    /**
     * Downloads the achievements to a cached copy.
     * Retrieve with getAchievements
     * Subscribe to GooglePlayEvent.ACHIEVEMENTS_ITEMS_READY to know when data is ready
     * @param forceRefresh True to force a data refresh. Use only for user initiated refresh
     */
    public synchronized final void loadAchievements(boolean forceRefresh) {
        if(!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_FAIL);
            return;
        }
        Games.Achievements.load(GooglePlay.getInstance().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Achievements.LoadAchievementsResult>() {
            @Override
            public void onResult(Achievements.LoadAchievementsResult loadAchievementsResult) {
                Logger.v(Logger.LoggerCategory.MAIN, TAG, "Achievements loaded");
                if(loadAchievementsResult.getStatus().isSuccess()) {
                    AchievementBuffer achieve = loadAchievementsResult.getAchievements();
                    achievements = new ArrayList<>();
                    for (int i = 0; i < achieve.getCount(); i++) {
                        Achievement ach = achieve.get(i);
                        String title = ach.getName();
                        AchievementItem.AchievementState state = getAchievementState(ach);

                        if (state != AchievementItem.AchievementState.HIDDEN) {
                            if (ach.getType() == Achievement.TYPE_INCREMENTAL) {
                                achievements.add(new AchievementItem(title, ach.getDescription(), ach.getRevealedImageUri(), ach.getUnlockedImageUri(),
                                        ach.getXpValue(), ach.getCurrentSteps(), ach.getTotalSteps(), state));
                            } else {
                                achievements.add(new AchievementItem(title, ach.getDescription(), ach.getRevealedImageUri(), ach.getUnlockedImageUri(),
                                        ach.getXpValue(), -1, -1, state));
                            }
                        }
                    }
                    achieve.release();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_READY);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_FAIL);
                }
            }
        });
    }

    /**
     * Gets an arrayList of all the achievements
     * If achievements are not ready this will return an empty array.
     * Call hasAchievements before this.
     * @return ArrayList of achievements
     */
    @NonNull
    public synchronized final ArrayList<AchievementItem> getAchievements() {
        if(hasAchievements()) {
            return achievements;
        }
        return new ArrayList<>();
    }

    /**
     * Check this before calling getAchievements
     * @return True if we have achievements downloaded.
     */
    public synchronized final boolean hasAchievements() {
        return achievements != null;
    }

    public final int getNumberAchievements() {
        if(hasAchievements()) {
            return achievements.size();
        }
        return -1;
    }

    public final int getNumberCompletedAchievements() {
        if(hasAchievements()) {
            int count = 0;
            for(AchievementItem item : achievements) {
                if(item.state == AchievementItem.AchievementState.UNLOCKED) {
                    count++;
                }
            }
            return count;
        }
        return -1;
    }

}
