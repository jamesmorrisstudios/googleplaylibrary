package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.Batch;
import com.google.android.gms.common.api.BatchResult;
import com.google.android.gms.common.api.BatchResultToken;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
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

    //Held Simple Data
    private GooglePlay.Collection leaderboardCollection = GooglePlay.Collection.PUBLIC;
    private GooglePlay.Span leaderboardSpan = GooglePlay.Span.ALL_TIME;

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

    public synchronized final void loadLeaderboardsMeta(boolean forceRefresh, @NonNull final String[] leaderboardIds) {
        Log.v("GooglePlayCalls", "Load leaderboard Meta Data");
        if(hasLeaderboardsMeta() && !forceRefresh && (this.leaderboardIds == null || Arrays.equals(leaderboardIds, this.leaderboardIds))) {
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
                        Leaderboard leaderboard = leaders.get(i);
                        if(!Utils.isInArray(leaderboard.getLeaderboardId(), leaderboardIds)) {
                            continue;
                        }
                        ArrayList<LeaderboardMetaVariantItem> variants = new ArrayList<>();
                        for(int j=0; j<leaderboard.getVariants().size(); j++) {
                            LeaderboardVariant leaderboardVariant = leaderboard.getVariants().get(j);
                            variants.add(new LeaderboardMetaVariantItem(
                                    GooglePlay.Collection.getFromInt(leaderboardVariant.getCollection()),
                                    GooglePlay.Span.getFromInt(leaderboardVariant.getTimeSpan()),
                                    leaderboardVariant.getNumScores()));
                        }
                        leaderboardsMeta.add(new LeaderboardMetaItem(leaderboard.getDisplayName(), leaderboard.getIconImageUri(), leaderboard.getLeaderboardId(), variants));
                    }
                    leaders.release();
                    loadPlayerScores(leaderboardIds);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_FAIL);
                }
            }
        }, timeout, timeUnit);
    }

    private void loadPlayerScores(final String[] leaderboardIds) {
        Log.v("GooglePlayCalls", "Load player scores");
        Batch.Builder builder = new Batch.Builder(GooglePlay.getInstance().getApiClient());

        for(String id : leaderboardIds) {
            final String idFinal = id;
                PendingResult<Leaderboards.LoadPlayerScoreResult> result = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(GooglePlay.getInstance().getApiClient(), id, leaderboardSpan.getInt(), leaderboardCollection.getInt());
                result.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                    @Override
                    public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                        if(loadPlayerScoreResult.getStatus().isSuccess()) {
                            LeaderboardScore score = loadPlayerScoreResult.getScore();
                            LeaderboardMetaItem item = getLeaderboardMetaItem(idFinal);
                            if(score != null && item != null) {
                                item.updateVariant(leaderboardCollection, leaderboardSpan, score.getDisplayRank(), score.getDisplayScore(), score.getRank(), score.getRawScore());
                                //Log.v("GooglePlayCalls", "Collection: "+leaderboardCollection.toString()+" Span: "+leaderboardSpan.toString()+" Score: "+score.getDisplayScore());
                            }
                        }
                    }
                });
                builder.add(result);
        }
        final Batch batch = builder.build();
        batch.setResultCallback(new ResultCallback<BatchResult>() {
            @Override
            public void onResult(BatchResult batchResult) {
                Log.v("GooglePlayCalls", "Batch Complete");
                Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_READY);
            }
        }, timeout, timeUnit);
    }

    @Nullable
    private LeaderboardMetaItem getLeaderboardMetaItem(String leaderboardId) {
        if(hasLeaderboardsMeta()) {
            for(LeaderboardMetaItem item : leaderboardsMeta) {
                if(item.leaderboardId.equals(leaderboardId)) {
                    return item;
                }
            }
        }
        return null;
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

    public synchronized final void loadLeaderboards(boolean forceRefresh, @Nullable final String leaderboardId) {
        if(hasLeaderboards() && !forceRefresh) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_READY);
            return;
        }
        if(!GooglePlay.getInstance().isSignedIn() || leaderboardId == null) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_FAIL);
            return;
        }
        Games.Leaderboards.loadTopScores(GooglePlay.getInstance().getApiClient(), leaderboardId, leaderboardSpan.getInt(), leaderboardCollection.getInt(),
                25, forceRefresh).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {
            @Override
            public void onResult(Leaderboards.LoadScoresResult loadScoresResult) {
                if(loadScoresResult.getStatus().isSuccess()) {
                    LeaderboardScoreBuffer leaders = loadScoresResult.getScores();
                    leaderboards = new ArrayList<>();
                    for(int i=0; i<leaders.getCount(); i++) {
                        leaderboards.add(new LeaderboardItem(leaders.get(i).getScoreHolderDisplayName(), leaders.get(i).getScoreHolderIconImageUri(),
                                leaders.get(i).getDisplayRank(), leaders.get(i).getDisplayScore(), leaders.get(i).getRank(), leaders.get(i).getRawScore()));
                    }
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_READY);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_FAIL);
                }
            }
        });
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

    public final void setLeaderboardCollection(GooglePlay.Collection collection) {
        this.leaderboardCollection = collection;
        clearLeaderboardsMetaCache();
        clearLeaderboardsCache();
    }

    public final void setLeaderboardSpan(GooglePlay.Span span) {
        this.leaderboardSpan = span;
        clearLeaderboardsMetaCache();
        clearLeaderboardsCache();
    }

    public final GooglePlay.Collection getLeaderboardCollection() {
        return this.leaderboardCollection;
    }

    public final GooglePlay.Span getLeaderboardSpan() {
        return this.leaderboardSpan;
    }

    public final void updateLeaderboard(@NonNull String leaderboardId, long value) {
        Games.Leaderboards.submitScore(GooglePlay.getInstance().getApiClient(), leaderboardId, value);
    }

}
