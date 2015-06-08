package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Logger;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.jamesmorrisstudios.utilitieslibrary.app.AppUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by James on 5/11/2015.
 */
public class GooglePlayCalls extends GooglePlayCallsBase {
    public static final String TAG = "GooglePlayCalls";
    private static GooglePlayCalls instance = null;

    //Cached Data
    private ArrayList<AchievementItem> achievements = null;
    private ArrayList<LeaderboardItem> leaderboards = null;
    private ArrayList<LeaderboardMetaItem> leaderboardsMeta = null;
    private String[] leaderboardIds = null;

    //Timeout
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private long timeout = 10000; //10 seconds

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
        if(hasAchievements() && !forceRefresh) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_READY);
            return;
        }
        if(!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_FAIL);
            return;
        }
        Games.Achievements.load(GooglePlay.getInstance().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Achievements.LoadAchievementsResult>() {
            @Override
            public void onResult(Achievements.LoadAchievementsResult loadAchievementsResult) {
                Logger.v(Logger.LoggerCategory.MAIN, TAG, "Achievements loaded");
                if (loadAchievementsResult.getStatus().isSuccess()) {
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
        }, timeout, timeUnit);
    }

    /**
     *
     */
    public synchronized void clearAchievementsCache() {
        achievements = null;
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

    public final void unlockAchievement(@NonNull String achievementId) {
        Games.Achievements.unlock(GooglePlay.getInstance().getApiClient(), achievementId);
    }

    public final void incrementAchievement(@NonNull String achievementId, int numberIncrements) {
        Games.Achievements.increment(GooglePlay.getInstance().getApiClient(), achievementId, numberIncrements);
    }

    public synchronized final void loadLeaderboardsMeta(boolean forceRefresh, final String[] leaderboardIds) {
        if(hasLeaderboardsMeta() && !forceRefresh && (leaderboardIds == null && this.leaderboardIds == null || Arrays.equals(leaderboardIds, this.leaderboardIds))) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_READY);
            return;
        }
        if(!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_FAIL);
            return;
        }
        this.leaderboardIds = leaderboardIds;
        Games.Leaderboards.loadLeaderboardMetadata(GooglePlay.getInstance().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Leaderboards.LeaderboardMetadataResult>() {
            @Override
            public void onResult(Leaderboards.LeaderboardMetadataResult leaderboardMetadataResult) {
                if(leaderboardMetadataResult.getStatus().isSuccess()) {
                    LeaderboardBuffer leaders = leaderboardMetadataResult.getLeaderboards();
                    leaderboardsMeta = new ArrayList<>();
                    for(int i=0; i<leaders.getCount(); i++) {
                        if(leaderboardIds != null && !Utils.isInArray(leaders.get(i).getLeaderboardId(), leaderboardIds)) {
                            continue;
                        }
                        ArrayList<LeaderboardMetaVariantItem> variants = new ArrayList<>();
                        for(int j=0; j<leaders.get(i).getVariants().size(); j++) {
                            if(leaders.get(i).getVariants().get(j).hasPlayerInfo()) {
                                variants.add(new LeaderboardMetaVariantItem(
                                        GooglePlay.Collection.getFromInt(leaders.get(i).getVariants().get(j).getCollection()),
                                        GooglePlay.Span.getFromInt(leaders.get(i).getVariants().get(j).getTimeSpan()),
                                        leaders.get(i).getVariants().get(j).getNumScores(),
                                        leaders.get(i).getVariants().get(j).getDisplayPlayerRank(),
                                        leaders.get(i).getVariants().get(j).getDisplayPlayerScore(),
                                        leaders.get(i).getVariants().get(j).getPlayerRank(),
                                        leaders.get(i).getVariants().get(j).getRawPlayerScore()
                                        ));
                            } else {
                                variants.add(new LeaderboardMetaVariantItem(
                                        GooglePlay.Collection.getFromInt(leaders.get(i).getVariants().get(j).getCollection()),
                                        GooglePlay.Span.getFromInt(leaders.get(i).getVariants().get(j).getTimeSpan()),
                                        leaders.get(i).getVariants().get(j).getNumScores()));
                            }
                        }
                        leaderboardsMeta.add(new LeaderboardMetaItem(leaders.get(i).getDisplayName(), leaders.get(i).getIconImageUri(), leaders.get(i).getLeaderboardId(), variants));
                    }
                    leaders.release();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_READY);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_FAIL);
                }
            }
        }, timeout, timeUnit);
    }

    public synchronized void clearLeaderboardsMetaCache() {
        leaderboardsMeta = null;
        leaderboardIds = null;
    }

    @NonNull
    public synchronized final ArrayList<LeaderboardMetaItem> getLeaderboardsMeta() {
        if(hasLeaderboardsMeta()) {
            return leaderboardsMeta;
        }
        return new ArrayList<>();
    }

    public synchronized final boolean hasLeaderboardsMeta() {
        return leaderboardsMeta != null;
    }

    public synchronized final void loadLeaderboards(boolean forceRefresh, String leaderboardId, GooglePlay.Span span, GooglePlay.Collection collection) {
        if(hasLeaderboards() && !forceRefresh) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_READY);
            return;
        }
        if(!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_FAIL);
            return;
        }

        int spanInt = span.getInt();
        int collectionInt = collection.getInt();

        Games.Leaderboards.loadLeaderboardMetadata(GooglePlay.getInstance().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Leaderboards.LeaderboardMetadataResult>() {
            @Override
            public void onResult(Leaderboards.LeaderboardMetadataResult leaderboardMetadataResult) {
                if(leaderboardMetadataResult.getStatus().isSuccess()) {
                    //Get a list of all the leaderboard Ids
                    LeaderboardBuffer leaders = leaderboardMetadataResult.getLeaderboards();

                    //leaders.get(0).


                    leaders.release();
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_FAIL);
                }
            }
        }, timeout, timeUnit);



/*
        Games.Leaderboards.loadTopScores(GooglePlay.getInstance().getApiClient(), leaderboardId, spanInt, collectionInt,
                25, forceRefresh).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {
            @Override
            public void onResult(Leaderboards.LoadScoresResult loadScoresResult) {

            }
        });
*/
    }

    /**
     *
     */
    public synchronized void clearLeaderboardsCache() {
        leaderboards = null;
    }

    @NonNull
    public synchronized final ArrayList<LeaderboardItem> getLeaderboards() {
        if(hasLeaderboards()) {
            return leaderboards;
        }
        return new ArrayList<>();
    }

    public synchronized final boolean hasLeaderboards() {
        return leaderboards != null;
    }

    public final void updateLeaderboard(@NonNull String leaderboardId, long value) {
        Games.Leaderboards.submitScore(GooglePlay.getInstance().getApiClient(), leaderboardId, value);
    }

}
