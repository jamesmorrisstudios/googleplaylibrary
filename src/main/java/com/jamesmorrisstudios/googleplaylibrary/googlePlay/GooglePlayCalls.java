package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.Batch;
import com.google.android.gms.common.api.BatchResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Logger;
import com.jamesmorrisstudios.utilitieslibrary.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private String[] achievementIds = null;

    //Local save cache
    private byte[] localSaveData = null;

    //Online save cache
    private TurnBasedMatch onlineMatch;

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
    public synchronized final void loadAchievements(boolean forceRefresh, @NonNull final String[] achievementIds) {
        if(hasAchievements() && !forceRefresh && (this.achievementIds == null || Arrays.equals(achievementIds, this.achievementIds))) {
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
                if (!loadAchievementsResult.getStatus().isSuccess()) {
                    loadAchievementsResult.release();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_FAIL);
                }
                AchievementBuffer achieve = loadAchievementsResult.getAchievements();
                achievements = new ArrayList<>();
                for (int i = 0; i < achieve.getCount(); i++) {
                    Achievement ach = achieve.get(i);
                    if (!Utils.isInArray(ach.getAchievementId(), achievementIds)) {
                        continue;
                    }
                    AchievementItem.AchievementState state = getAchievementState(ach);
                    if (state != AchievementItem.AchievementState.HIDDEN) {
                        if (ach.getType() == Achievement.TYPE_INCREMENTAL) {
                            achievements.add(new AchievementItem(ach.getName(), ach.getDescription(), ach.getRevealedImageUri(), ach.getUnlockedImageUri(),
                                    ach.getXpValue(), ach.getCurrentSteps(), ach.getTotalSteps(), state));
                        } else {
                            achievements.add(new AchievementItem(ach.getName(), ach.getDescription(), ach.getRevealedImageUri(), ach.getUnlockedImageUri(),
                                    ach.getXpValue(), -1, -1, state));
                        }
                    }
                }
                achieve.close();
                achieve.release();
                loadAchievementsResult.release();
                Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_READY);
            }
        }, timeout, timeUnit);
    }

    /**
     *
     */
    public synchronized void clearAchievementsCache() {
        achievements = null;
        achievementIds = null;
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
                Log.v("GooglePlayCalls", "Load leaderboard Meta Data complete");
                if (leaderboardMetadataResult.getStatus().isSuccess()) {
                    Log.v("GooglePlayCalls", "Load leaderboard Meta Data complete success");
                    LeaderboardBuffer leaders = leaderboardMetadataResult.getLeaderboards();
                    leaderboardsMeta = new ArrayList<>();
                    for (int i = 0; i < leaders.getCount(); i++) {
                        Leaderboard leaderboard = leaders.get(i);
                        if (!Utils.isInArray(leaderboard.getLeaderboardId(), leaderboardIds)) {
                            continue;
                        }
                        ArrayList<LeaderboardMetaVariantItem> variants = new ArrayList<>();
                        for (int j = 0; j < leaderboard.getVariants().size(); j++) {
                            LeaderboardVariant leaderboardVariant = leaderboard.getVariants().get(j);
                            variants.add(new LeaderboardMetaVariantItem(
                                    GooglePlay.Collection.getFromInt(leaderboardVariant.getCollection()),
                                    GooglePlay.Span.getFromInt(leaderboardVariant.getTimeSpan()),
                                    leaderboardVariant.getNumScores()));
                        }
                        leaderboardsMeta.add(new LeaderboardMetaItem(leaderboard.getDisplayName(), leaderboard.getIconImageUri(), leaderboard.getLeaderboardId(), variants));
                    }
                    leaders.close();
                    leaders.release();
                    leaderboardMetadataResult.release();
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
                    leaders.release();
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

    public final void acceptInvitationOnline(@NonNull Invitation invitation) {
        Games.TurnBasedMultiplayer.acceptInvitation(GooglePlay.getInstance().getApiClient(), invitation.getInvitationId()).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                if(initiateMatchResult.getStatus().isSuccess()) {
                    onlineMatch = initiateMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.ACCEPT_INVITATION_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.ACCEPT_INVITATION_ONLINE_FAIL);
                }
            }
        });
    }

    public final void rematchOnline(@NonNull TurnBasedMatch match) {
        Games.TurnBasedMultiplayer.rematch(GooglePlay.getInstance().getApiClient(), match.getMatchId()).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                if(initiateMatchResult.getStatus().isSuccess()) {
                    onlineMatch = initiateMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.REMATCH_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.REMATCH_ONLINE_FAIL);
                }
            }
        });
    }

    public final void startMatchOnline(@NonNull TurnBasedMatchConfig matchConfig) {
        Games.TurnBasedMultiplayer.createMatch(GooglePlay.getInstance().getApiClient(), matchConfig).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                if(initiateMatchResult.getStatus().isSuccess()) {
                    onlineMatch = initiateMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.START_MATCH_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.START_MATCH_ONLINE_FAIL);
                }
            }
        });
    }

    public final void finishMatchOnline(@NonNull TurnBasedMatch match, @NonNull byte[] data, @Nullable List<ParticipantResult> results) {
        if(results != null) {
            Games.TurnBasedMultiplayer.finishMatch(GooglePlay.getInstance().getApiClient(), match.getMatchId(), data, results).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.UpdateMatchResult updateMatchResult) {
                    if(updateMatchResult.getStatus().isSuccess()) {
                        onlineMatch = updateMatchResult.getMatch();
                        Bus.postEnum(GooglePlay.GooglePlayEvent.FINISH_MATCH_ONLINE_SUCCESS);
                    } else {
                        Bus.postEnum(GooglePlay.GooglePlayEvent.FINISH_MATCH_ONLINE_FAIL);
                    }
                }
            });
        } else {
            Games.TurnBasedMultiplayer.finishMatch(GooglePlay.getInstance().getApiClient(), match.getMatchId(), data).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.UpdateMatchResult updateMatchResult) {
                    if(updateMatchResult.getStatus().isSuccess()) {
                        onlineMatch = updateMatchResult.getMatch();
                        Bus.postEnum(GooglePlay.GooglePlayEvent.FINISH_MATCH_ONLINE_SUCCESS);
                    } else {
                        Bus.postEnum(GooglePlay.GooglePlayEvent.FINISH_MATCH_ONLINE_FAIL);
                    }
                }
            });
        }
    }

    public final void takeTurnOnline(@NonNull TurnBasedMatch match, @NonNull String nextParticipantId, @NonNull byte[] data) {
        Games.TurnBasedMultiplayer.takeTurn(GooglePlay.getInstance().getApiClient(), match.getMatchId(), data, nextParticipantId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.UpdateMatchResult updateMatchResult) {
                if(updateMatchResult.getStatus().isSuccess()) {
                    onlineMatch = updateMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.TAKE_TURN_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.TAKE_TURN_ONLINE_FAIL);
                }
            }
        });
    }

    public final void loadMatchOnline(@NonNull String matchId) {
        Games.TurnBasedMultiplayer.loadMatch(GooglePlay.getInstance().getApiClient(), matchId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.LoadMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.LoadMatchResult loadMatchResult) {
                if(loadMatchResult.getStatus().isSuccess()) {
                    onlineMatch = loadMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_FAIL);
                }
            }
        });
    }

    public final void saveGameLocal(@NonNull final byte[] data, @NonNull final Bitmap screenshot, @NonNull final String description, @NonNull String saveName) {
        if(!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.SAVE_MATCH_LOCAL_FAIL);
            return;
        }
        Games.Snapshots.open(GooglePlay.getInstance().getApiClient(), saveName, true).setResultCallback(new ResultCallback<Snapshots.OpenSnapshotResult>() {
            @Override
            public void onResult(Snapshots.OpenSnapshotResult openSnapshotResult) {
                processAndWriteSnapshotResult(openSnapshotResult, 0, data, screenshot, description);
            }
        }, timeout, timeUnit);
    }

    public final void loadGameLocal(@NonNull String saveName) {
        if(!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_LOCAL_FAIL);
            return;
        }
        Games.Snapshots.open(GooglePlay.getInstance().getApiClient(), saveName, true).setResultCallback(new ResultCallback<Snapshots.OpenSnapshotResult>() {
            @Override
            public void onResult(Snapshots.OpenSnapshotResult openSnapshotResult) {
                if(openSnapshotResult.getStatus().isSuccess()){
                    Snapshot snapshot = openSnapshotResult.getSnapshot();
                    try {
                        localSaveData = snapshot.getSnapshotContents().readFully();
                        Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_LOCAL_SUCCESS);
                    } catch (IOException e) {
                        Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_LOCAL_FAIL);
                    }
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_LOCAL_FAIL);
                }
            }
        }, timeout, timeUnit);
    }

    /**
     * Conflict resolution for when Snapshots are opened.
     *
     * @param result The open snapshot result to resolve on open.
     */
    private void processAndWriteSnapshotResult(@NonNull Snapshots.OpenSnapshotResult result, final int retryCount,
                                               @NonNull final byte[] data, @NonNull final Bitmap screenshot, @NonNull final String description) {
        Snapshot mResolvedSnapshot;
        int status = result.getStatus().getStatusCode();

        if (status == GamesStatusCodes.STATUS_OK) {
            writeSnapshot(result.getSnapshot(), data, screenshot, description);
        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONTENTS_UNAVAILABLE) {
            writeSnapshot(result.getSnapshot(), data, screenshot, description);
        } else if (status == GamesStatusCodes.STATUS_SNAPSHOT_CONFLICT) {
            Snapshot snapshot = result.getSnapshot();
            Snapshot conflictSnapshot = result.getConflictingSnapshot();

            // Resolve between conflicts by selecting the newest of the conflicting snapshots.
            mResolvedSnapshot = snapshot;

            if (snapshot.getMetadata().getLastModifiedTimestamp() < conflictSnapshot.getMetadata().getLastModifiedTimestamp()) {
                mResolvedSnapshot = conflictSnapshot;
            }

            Games.Snapshots.resolveConflict(GooglePlay.getInstance().getApiClient(), result.getConflictId(), mResolvedSnapshot).setResultCallback(new ResultCallback<Snapshots.OpenSnapshotResult>() {
                @Override
                public void onResult(Snapshots.OpenSnapshotResult openSnapshotResult) {
                    if (retryCount < 3) {
                        processAndWriteSnapshotResult(openSnapshotResult, retryCount + 1, data, screenshot, description);
                    } else {
                        Bus.postEnum(GooglePlay.GooglePlayEvent.SAVE_MATCH_LOCAL_FAIL);
                    }
                }
            }, timeout, timeUnit);
        } else {
            Bus.postEnum(GooglePlay.GooglePlayEvent.SAVE_MATCH_LOCAL_FAIL);
        }
    }

    /**
     * Generates metadata, takes a screenshot, and performs the write operation for saving a snapshot.
     */
    private void writeSnapshot(@NonNull Snapshot snapshot, @NonNull byte[] data, @NonNull Bitmap screenshot, @NonNull String description) {
        // Set the data payload for the snapshot.
        snapshot.getSnapshotContents().writeBytes(data);
        // Save the snapshot.
        SnapshotMetadataChange metadataChange;
        metadataChange = new SnapshotMetadataChange.Builder()
                .setCoverImage(screenshot)
                .setDescription(description)
                .build();
        Games.Snapshots.commitAndClose(GooglePlay.getInstance().getApiClient(), snapshot, metadataChange).setResultCallback(new ResultCallback<Snapshots.CommitSnapshotResult>() {
            @Override
            public void onResult(Snapshots.CommitSnapshotResult commitSnapshotResult) {
                if(commitSnapshotResult.getStatus().isSuccess()) {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.SAVE_MATCH_LOCAL_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.SAVE_MATCH_LOCAL_FAIL);
                }
            }
        }, timeout, timeUnit);
    }

}
