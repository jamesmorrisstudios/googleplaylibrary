package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.content.ComponentName;
import android.content.Intent;
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
import com.google.android.gms.games.PageDirection;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayerBuffer;
import com.google.android.gms.games.Players;
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
import com.google.android.gms.games.multiplayer.turnbased.LoadMatchesResponse;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.Logger;
import com.jamesmorrisstudios.appbaselibrary.Utils;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementItem;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardItem;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardMetaItem;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardMetaVariantItem;
import com.jamesmorrisstudios.googleplaylibrary.data.OnlineSaveItem;
import com.jamesmorrisstudios.googleplaylibrary.data.PlayerPickerItem;

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
    private ArrayList<PlayerPickerItem> playersActive = null;
    private ArrayList<PlayerPickerItem> playersAll = null;
    private ArrayList<PlayerPickerItem> playersAllMore = null;
    private PlayerBuffer playersAllBuffer = null;
    private ArrayList<AchievementItem> achievements = null;
    private ArrayList<LeaderboardItem> leaderboards = null;
    private ArrayList<LeaderboardItem> leaderboardsMore = null;
    private LeaderboardScoreBuffer leaderboardBuffer = null;
    private ArrayList<LeaderboardMetaItem> leaderboardsMeta = null;
    private String[] leaderboardIds = null;
    private String[] achievementIds = null;
    private ArrayList<OnlineSaveItem> onlineSaveItems = null;

    //Local save cache
    private byte[] localSaveData = null;

    //Online save cache
    private TurnBasedMatch onlineMatch;

    //Held Simple Data
    private GooglePlay.Collection leaderboardCollection = GooglePlay.Collection.PUBLIC;
    private GooglePlay.Span leaderboardSpan = GooglePlay.Span.ALL_TIME;

    //Timeout
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private long timeout = 15000; //15 seconds

    /**
     * Required private constructor to enforce singleton
     */
    private GooglePlayCalls() {
    }

    /**
     * @return The instance of GooglePlayCalls
     */
    @NonNull
    public static GooglePlayCalls getInstance() {
        if (instance == null) {
            instance = new GooglePlayCalls();
        }
        return instance;
    }

    public final Player getCurrentPlayer() {
        return Games.Players.getCurrentPlayer(GooglePlay.getInstance().getApiClient());
    }

    public synchronized final void loadPlayersActive(boolean forceRefresh) {
        if (hasPlayersActive() && !forceRefresh) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ACTIVE_READY);
            return;
        }
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ACTIVE_FAIL);
            return;
        }
        Games.Players.loadConnectedPlayers(GooglePlay.getInstance().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Players.LoadPlayersResult>() {
            @Override
            public void onResult(Players.LoadPlayersResult loadPlayersResult) {
                if (loadPlayersResult.getStatus().isSuccess()) {
                    PlayerBuffer players = loadPlayersResult.getPlayers();
                    playersActive = new ArrayList<>();
                    for (int i = 0; i < players.getCount(); i++) {
                        playersActive.add(new PlayerPickerItem(players.get(i).freeze()));
                    }
                    players.release();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ACTIVE_READY);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ACTIVE_FAIL);
                }
            }
        });
    }

    public synchronized final void loadPlayersAll(boolean forceRefresh) {
        if (hasPlayersAll() && !forceRefresh) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ALL_READY);
            return;
        }
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ALL_FAIL);
            return;
        }
        Games.Players.loadInvitablePlayers(GooglePlay.getInstance().getApiClient(), 25, forceRefresh).setResultCallback(new ResultCallback<Players.LoadPlayersResult>() {
            @Override
            public void onResult(Players.LoadPlayersResult loadPlayersResult) {
                if (loadPlayersResult.getStatus().isSuccess()) {
                    playersAllBuffer = loadPlayersResult.getPlayers();
                    playersAll = new ArrayList<>();
                    for (int i = 0; i < playersAllBuffer.getCount(); i++) {
                        playersAll.add(new PlayerPickerItem(playersAllBuffer.get(i).freeze()));
                    }
                    playersAllBuffer.release();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ALL_READY);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ALL_FAIL);
                }
            }
        });
    }

    public synchronized final void loadPlayersAllMore() {
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ALL_MORE_FAIL);
            return;
        }
        playersAllMore = null;
        Games.Players.loadMoreInvitablePlayers(GooglePlay.getInstance().getApiClient(), 10).setResultCallback(new ResultCallback<Players.LoadPlayersResult>() {
            @Override
            public void onResult(Players.LoadPlayersResult loadPlayersResult) {
                if (loadPlayersResult.getStatus().isSuccess()) {
                    playersAllBuffer = loadPlayersResult.getPlayers();
                    playersAllMore = new ArrayList<>();
                    for (int i = 0; i < playersAllBuffer.getCount(); i++) {
                        if (!doesPlayerIdAlreadyExist(playersAllBuffer.get(i).getPlayerId())) {
                            PlayerPickerItem item = new PlayerPickerItem(playersAllBuffer.get(i).freeze());
                            playersAllMore.add(item);
                            playersAll.add(item);
                        }
                    }
                    playersAllBuffer.release();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ALL_MORE_READY);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ALL_MORE_FAIL);
                }
            }
        });
    }

    private boolean doesPlayerIdAlreadyExist(@NonNull String playerId) {
        for (PlayerPickerItem item : playersAll) {
            if (item.player.getPlayerId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    public synchronized void clearPlayersCache() {
        playersActive = null;
        playersAll = null;
        playersAllMore = null;
    }

    @NonNull
    public synchronized final ArrayList<PlayerPickerItem> getPlayersActive() {
        if (hasPlayersActive()) {
            return playersActive;
        }
        return new ArrayList<>();
    }

    @NonNull
    public synchronized final ArrayList<PlayerPickerItem> getPlayersAll() {
        if (hasPlayersAll()) {
            return playersAll;
        }
        return new ArrayList<>();
    }

    @NonNull
    public synchronized final ArrayList<PlayerPickerItem> getPlayersAllMore() {
        if (hasPlayersAllMore()) {
            return playersAllMore;
        }
        return new ArrayList<>();
    }

    public synchronized final boolean hasPlayersActive() {
        return playersActive != null;
    }

    public synchronized final boolean hasPlayersAll() {
        return playersAll != null;
    }

    public synchronized final boolean hasPlayersAllMore() {
        return playersAllMore != null;
    }

    /**
     * Downloads the achievements to a cached copy.
     * Retrieve with getAchievements
     * Subscribe to GooglePlayEvent.ACHIEVEMENTS_ITEMS_READY to know when data is ready
     *
     * @param forceRefresh True to force a data refresh. Use only for user initiated refresh
     */
    public synchronized final void loadAchievements(boolean forceRefresh, @NonNull final String[] achievementIds) {
        if (hasAchievements() && !forceRefresh && (this.achievementIds == null || Arrays.equals(achievementIds, this.achievementIds))) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_READY);
            return;
        }
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_FAIL);
            return;
        }
        Games.Achievements.load(GooglePlay.getInstance().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Achievements.LoadAchievementsResult>() {
            @Override
            public void onResult(Achievements.LoadAchievementsResult loadAchievementsResult) {
                Logger.v(Logger.LoggerCategory.BASE, TAG, "Achievements loaded");
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
     *
     * @return ArrayList of achievements
     */
    @NonNull
    public synchronized final ArrayList<AchievementItem> getAchievements() {
        if (hasAchievements()) {
            return achievements;
        }
        return new ArrayList<>();
    }

    /**
     * Check this before calling getAchievements
     *
     * @return True if we have achievements downloaded.
     */
    public synchronized final boolean hasAchievements() {
        return achievements != null;
    }

    public final int getNumberAchievements() {
        if (hasAchievements()) {
            return achievements.size();
        }
        return -1;
    }

    public final int getNumberCompletedAchievements() {
        if (hasAchievements()) {
            int count = 0;
            for (AchievementItem item : achievements) {
                if (item.state == AchievementItem.AchievementState.UNLOCKED) {
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
        Log.v("GooglePlayCalls", "Load ic_leaderboard Meta Data");
        if (hasLeaderboardsMeta() && !forceRefresh && (this.leaderboardIds == null || Arrays.equals(leaderboardIds, this.leaderboardIds))) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_READY);
            return;
        }
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_FAIL);
            return;
        }
        this.leaderboardIds = leaderboardIds;
        Games.Leaderboards.loadLeaderboardMetadata(GooglePlay.getInstance().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Leaderboards.LeaderboardMetadataResult>() {
            @Override
            public void onResult(Leaderboards.LeaderboardMetadataResult leaderboardMetadataResult) {
                Log.v("GooglePlayCalls", "Load ic_leaderboard Meta Data complete");
                if (leaderboardMetadataResult.getStatus().isSuccess()) {
                    Log.v("GooglePlayCalls", "Load ic_leaderboard Meta Data complete success");
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

        for (String id : leaderboardIds) {
            final String idFinal = id;
            PendingResult<Leaderboards.LoadPlayerScoreResult> result = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(GooglePlay.getInstance().getApiClient(), id, leaderboardSpan.getInt(), leaderboardCollection.getInt());
            result.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                @Override
                public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                    if (loadPlayerScoreResult.getStatus().isSuccess()) {
                        LeaderboardScore score = loadPlayerScoreResult.getScore();
                        LeaderboardMetaItem item = getLeaderboardMetaItem(idFinal);
                        if (score != null && item != null) {
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
        if (hasLeaderboardsMeta()) {
            for (LeaderboardMetaItem item : leaderboardsMeta) {
                if (item.leaderboardId.equals(leaderboardId)) {
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
        if (hasLeaderboardsMeta()) {
            return leaderboardsMeta;
        }
        return new ArrayList<>();
    }

    public synchronized final boolean hasLeaderboardsMeta() {
        return leaderboardsMeta != null;
    }

    public synchronized final void loadLeaderboards(boolean forceRefresh, @Nullable final String leaderboardId) {
        if (hasLeaderboards() && !forceRefresh) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_READY);
            return;
        }
        if (!GooglePlay.getInstance().isSignedIn() || leaderboardId == null) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_FAIL);
            return;
        }
        Games.Leaderboards.loadTopScores(GooglePlay.getInstance().getApiClient(), leaderboardId, leaderboardSpan.getInt(), leaderboardCollection.getInt(),
                25, forceRefresh).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {
            @Override
            public void onResult(Leaderboards.LoadScoresResult loadScoresResult) {
                if (loadScoresResult.getStatus().isSuccess()) {
                    leaderboardBuffer = loadScoresResult.getScores();
                    leaderboards = new ArrayList<>();
                    for (int i = 0; i < leaderboardBuffer.getCount(); i++) {
                        leaderboards.add(new LeaderboardItem(leaderboardBuffer.get(i).getScoreHolderDisplayName(), leaderboardBuffer.get(i).getScoreHolderIconImageUri(),
                                leaderboardBuffer.get(i).getDisplayRank(), leaderboardBuffer.get(i).getDisplayScore(), leaderboardBuffer.get(i).getRank(), leaderboardBuffer.get(i).getScoreHolder().freeze()));
                    }
                    leaderboardBuffer.release();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_READY);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_FAIL);
                }
            }
        });
    }

    public synchronized final void loadLeaderboardsMore() {
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_MORE_FAIL);
            return;
        }
        Log.v("GooglePlayCalls", "Load leaderboards more");
        Games.Leaderboards.loadMoreScores(GooglePlay.getInstance().getApiClient(), leaderboardBuffer, 10, PageDirection.NEXT).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {
            @Override
            public void onResult(Leaderboards.LoadScoresResult loadScoresResult) {
                if (loadScoresResult.getStatus().isSuccess()) {
                    leaderboardBuffer = loadScoresResult.getScores();
                    leaderboardsMore = new ArrayList<>();
                    for (int i = 0; i < leaderboardBuffer.getCount(); i++) {
                        if (leaderboardBuffer.get(i).getRank() > leaderboards.get(leaderboards.size() - 1).playerRank) {
                            leaderboardsMore.add(new LeaderboardItem(leaderboardBuffer.get(i).getScoreHolderDisplayName(), leaderboardBuffer.get(i).getScoreHolderIconImageUri(),
                                    leaderboardBuffer.get(i).getDisplayRank(), leaderboardBuffer.get(i).getDisplayScore(), leaderboardBuffer.get(i).getRank(), leaderboardBuffer.get(i).getScoreHolder().freeze()));
                        }
                    }
                    leaderboards.addAll(leaderboardsMore);
                    leaderboardBuffer.release();
                    Log.v("GooglePlayCalls", "Load leaderboards more. SUCCESS");
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_MORE_READY);
                } else {
                    Log.v("GooglePlayCalls", "Load leaderboards more. FAIL");
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_MORE_FAIL);
                }
            }
        });
    }

    /**
     *
     */
    public synchronized void clearLeaderboardsCache() {
        leaderboards = null;
        leaderboardsMore = null;
    }

    @NonNull
    public synchronized final ArrayList<LeaderboardItem> getLeaderboards() {
        if (hasLeaderboards()) {
            return leaderboards;
        }
        return new ArrayList<>();
    }

    @NonNull
    public synchronized final ArrayList<LeaderboardItem> getLeaderboardsMore() {
        if (hasLeaderboardsMore()) {
            return leaderboardsMore;
        }
        return new ArrayList<>();
    }

    public synchronized final boolean hasLeaderboards() {
        return leaderboards != null;
    }

    public synchronized final boolean hasLeaderboardsMore() {
        return leaderboardsMore != null;
    }

    public final GooglePlay.Collection getLeaderboardCollection() {
        return this.leaderboardCollection;
    }

    public final void setLeaderboardCollection(GooglePlay.Collection collection) {
        this.leaderboardCollection = collection;
        clearLeaderboardsMetaCache();
        clearLeaderboardsCache();
    }

    public final GooglePlay.Span getLeaderboardSpan() {
        return this.leaderboardSpan;
    }

    public final void setLeaderboardSpan(GooglePlay.Span span) {
        this.leaderboardSpan = span;
        clearLeaderboardsMetaCache();
        clearLeaderboardsCache();
    }

    public synchronized final void loadOnlineSaves(boolean forceRefresh) {
        if (hasOnlineSaveItems() && !forceRefresh) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ONLINE_SAVE_ITEM_LOAD_READY);
            return;
        }
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ONLINE_SAVE_ITEM_LOAD_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.loadMatchesByStatus(GooglePlay.getInstance().getApiClient(),
                new int[]{TurnBasedMatch.MATCH_TURN_STATUS_INVITED, TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN, TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN,
                        TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE}).setResultCallback(new ResultCallback<TurnBasedMultiplayer.LoadMatchesResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.LoadMatchesResult loadMatchesResult) {
                if (loadMatchesResult.getStatus().isSuccess()) {
                    onlineSaveItems = new ArrayList<>();
                    LoadMatchesResponse matches = loadMatchesResult.getMatches();
                    OnlineSaveItem item;
                    //Invitations
                    for (int i = 0; i < matches.getInvitations().getCount(); i++) {
                        item = makeOnlineSaveItem(matches.getInvitations().get(i), GooglePlay.SaveType.INVITATION);
                        if (item != null) {
                            Log.v("GooglePlayCalls", "Adding invitation");
                            onlineSaveItems.add(item);
                        }
                    }
                    matches.getInvitations().release();
                    //My Turn
                    for (int i = 0; i < matches.getMyTurnMatches().getCount(); i++) {
                        item = makeOnlineSaveItem(matches.getMyTurnMatches().get(i), GooglePlay.SaveType.YOUR_TURN);
                        if (item != null) {
                            Log.v("GooglePlayCalls", "Adding my turn");
                            onlineSaveItems.add(item);
                        }
                    }
                    matches.getMyTurnMatches().release();
                    //Their Turn
                    for (int i = 0; i < matches.getTheirTurnMatches().getCount(); i++) {
                        item = makeOnlineSaveItem(matches.getTheirTurnMatches().get(i), GooglePlay.SaveType.THEIR_TURN);
                        if (item != null) {
                            Log.v("GooglePlayCalls", "Adding their turn");
                            onlineSaveItems.add(item);
                        }
                    }
                    matches.getTheirTurnMatches().release();
                    //Completed
                    for (int i = 0; i < matches.getCompletedMatches().getCount(); i++) {
                        item = makeOnlineSaveItem(matches.getCompletedMatches().get(i), GooglePlay.SaveType.COMPLETE);
                        if (item != null) {
                            Log.v("GooglePlayCalls", "Adding completed");
                            onlineSaveItems.add(item);
                        }
                    }
                    matches.getCompletedMatches().release();
                    matches.release();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.ONLINE_SAVE_ITEM_LOAD_READY);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.ONLINE_SAVE_ITEM_LOAD_FAIL);
                }
                loadMatchesResult.release();
            }
        });
    }

    @Nullable
    private OnlineSaveItem makeOnlineSaveItem(@NonNull Invitation invitation, GooglePlay.SaveType saveType) {
        return new OnlineSaveItem(false); //TODO
    }

    @Nullable
    private OnlineSaveItem makeOnlineSaveItem(@NonNull TurnBasedMatch match, GooglePlay.SaveType saveType) {
        return new OnlineSaveItem(true); //TODO
    }

    public final boolean hasOnlineSaveItems() {
        return onlineSaveItems != null;
    }

    public final boolean hasOnlineSaveItems(GooglePlay.SaveType saveType) {
        if (onlineSaveItems == null) {
            return false;
        }
        for (OnlineSaveItem item : onlineSaveItems) {
            if (item.saveType == saveType) {
                return true;
            }
        }
        return false;
    }

    public final void clearOnlineSaveItems() {
        onlineSaveItems = null;
    }

    @NonNull
    public final ArrayList<OnlineSaveItem> getOnlineSaveItems(GooglePlay.SaveType saveType) {
        if (hasOnlineSaveItems()) {
            ArrayList<OnlineSaveItem> items = new ArrayList<>();
            for (OnlineSaveItem item : onlineSaveItems) {
                if (item.saveType == saveType) {
                    items.add(item);
                }
            }
            return items;
        }
        return new ArrayList<>();
    }

    public final void updateLeaderboard(@NonNull String leaderboardId, long value) {
        if (!GooglePlay.getInstance().isSignedIn()) {
            //TODO error
            return;
        }
        Games.Leaderboards.submitScore(GooglePlay.getInstance().getApiClient(), leaderboardId, value);
    }

    public final void acceptInvitationOnline(@NonNull Invitation invitation) {
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ACCEPT_INVITATION_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.acceptInvitation(GooglePlay.getInstance().getApiClient(), invitation.getInvitationId()).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                if (initiateMatchResult.getStatus().isSuccess()) {
                    onlineMatch = initiateMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.ACCEPT_INVITATION_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.ACCEPT_INVITATION_ONLINE_FAIL);
                }
            }
        });
    }

    public final void dismissMatchOnline(@NonNull String matchId) {
        if (!GooglePlay.getInstance().isSignedIn()) {
            //TODO error
            return;
        }
        Games.TurnBasedMultiplayer.dismissMatch(GooglePlay.getInstance().getApiClient(), matchId);
    }

    public final void rematchOnline(@NonNull String matchId) {
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.REMATCH_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.rematch(GooglePlay.getInstance().getApiClient(), matchId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                if (initiateMatchResult.getStatus().isSuccess()) {
                    onlineMatch = initiateMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.REMATCH_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.REMATCH_ONLINE_FAIL);
                }
            }
        });
    }

    public final void startMatchOnline(@NonNull TurnBasedMatchConfig matchConfig) {
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.START_MATCH_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.createMatch(GooglePlay.getInstance().getApiClient(), matchConfig).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                if (initiateMatchResult.getStatus().isSuccess()) {
                    onlineMatch = initiateMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.START_MATCH_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.START_MATCH_ONLINE_FAIL);
                }
            }
        });
    }

    public final void finishMatchOnline(@NonNull TurnBasedMatch match, @NonNull byte[] data, @Nullable List<ParticipantResult> results) {
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.FINISH_MATCH_ONLINE_FAIL);
            return;
        }
        if (results != null) {
            Games.TurnBasedMultiplayer.finishMatch(GooglePlay.getInstance().getApiClient(), match.getMatchId(), data, results).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.UpdateMatchResult updateMatchResult) {
                    if (updateMatchResult.getStatus().isSuccess()) {
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
                    if (updateMatchResult.getStatus().isSuccess()) {
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
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.TAKE_TURN_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.takeTurn(GooglePlay.getInstance().getApiClient(), match.getMatchId(), data, nextParticipantId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.UpdateMatchResult updateMatchResult) {
                if (updateMatchResult.getStatus().isSuccess()) {
                    onlineMatch = updateMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.TAKE_TURN_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.TAKE_TURN_ONLINE_FAIL);
                }
            }
        });
    }

    public final void loadMatchOnline(@NonNull TurnBasedMatch match) {
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_FAIL);
            return;
        }
        onlineMatch = match;
        Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_SUCCESS);
    }

    public final void loadMatchOnline(@NonNull String matchId) {
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.loadMatch(GooglePlay.getInstance().getApiClient(), matchId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.LoadMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.LoadMatchResult loadMatchResult) {
                if (loadMatchResult.getStatus().isSuccess()) {
                    onlineMatch = loadMatchResult.getMatch();
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_FAIL);
                }
            }
        });
    }

    public final void saveGameLocal(@NonNull final byte[] data, @NonNull final Bitmap screenshot, @NonNull final String description, @NonNull String saveName) {
        if (!GooglePlay.getInstance().isSignedIn()) {
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
        if (!GooglePlay.getInstance().isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_LOCAL_FAIL);
            return;
        }
        Games.Snapshots.open(GooglePlay.getInstance().getApiClient(), saveName, true).setResultCallback(new ResultCallback<Snapshots.OpenSnapshotResult>() {
            @Override
            public void onResult(Snapshots.OpenSnapshotResult openSnapshotResult) {
                if (openSnapshotResult.getStatus().isSuccess()) {
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
                if (commitSnapshotResult.getStatus().isSuccess()) {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.SAVE_MATCH_LOCAL_SUCCESS);
                } else {
                    Bus.postEnum(GooglePlay.GooglePlayEvent.SAVE_MATCH_LOCAL_FAIL);
                }
            }
        }, timeout, timeUnit);
    }

    /**
     * Build the basic intent. This is common to all linked pages
     * This adds the current user's playerId. This is important.
     */
    private Intent buildDeepLinkIntent() {
        if (!GooglePlay.getInstance().isSignedIn()) {
            return null;
        }
        Intent intent = new Intent();
        //Clear the activity so the back button returns to your app
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Manually specify the package and activity name
        intent.setComponent(new ComponentName("com.google.android.ic_play_match.games", "com.google.android.gms.games.ui.destination.api.ApiActivity"));
        //Not really needed as default happens if you don't specify it.
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //You must specify the current players user. It ensures that Google Play Games is logged in as the same person.
        intent.putExtra("com.google.android.gms.games.ACCOUNT_KEY", Games.Players.getCurrentPlayerId(GooglePlay.getInstance().getApiClient()));
        //I have not tested with this but there is an option to specify the minimum version
        //intent.putExtra("com.google.android.gms.games.MIN_VERSION", ???);
        return intent;
    }

    /**
     * Fire the intent if Google Play Games is installed.
     * Otherwise handle the error
     */
    private boolean startGooglePlayGames(Intent intent) {
        AppBase.getContext().startActivity(intent);
        return true;
        /*
        //This assumes it is running in a fragment. Adjust getActivity() as needed.
        PackageManager packageManager = AppBase.getContext().getPackageManager();
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;
        if(isIntentSafe) {
            AppBase.getContext().startActivity(intent);
            return true;
        } else {
            return false;
        }
        */
    }

    /**
     * Launches the about screen for your game
     */
    public final boolean launchGPSAbout() {
        Intent intent = buildDeepLinkIntent();
        if (intent == null) {
            return false;
        }
        intent.putExtra("com.google.android.gms.games.SCREEN", 1050);
        intent.putExtra("com.google.android.gms.games.GAME", Games.GamesMetadata.getCurrentGame(GooglePlay.getInstance().getApiClient()));
        return startGooglePlayGames(intent);
    }

    /**
     * Launches the achievements page
     */
    public final boolean launchGPSAchievements() {
        Intent intent = buildDeepLinkIntent();
        if (intent == null) {
            return false;
        }
        intent.putExtra("com.google.android.gms.games.SCREEN", 1051);
        intent.putExtra("com.google.android.gms.games.GAME", Games.GamesMetadata.getCurrentGame(GooglePlay.getInstance().getApiClient()));
        return startGooglePlayGames(intent);
    }

    /**
     * Launches the leaderboards page
     */
    public final boolean launchGPSLeaderboards() {
        Intent intent = buildDeepLinkIntent();
        if (intent == null) {
            return false;
        }
        intent.putExtra("com.google.android.gms.games.SCREEN", 1052);
        intent.putExtra("com.google.android.gms.games.GAME", Games.GamesMetadata.getCurrentGame(GooglePlay.getInstance().getApiClient()));
        return startGooglePlayGames(intent);
    }

    /**
     * Launches to a specific ic_leaderboard. You must specify the leaderboardId
     */
    public final boolean launchGPSLeaderboard(String leaderboardId) {
        Intent intent = buildDeepLinkIntent();
        if (intent == null) {
            return false;
        }
        intent.putExtra("com.google.android.gms.games.SCREEN", 1053);
        intent.putExtra("com.google.android.gms.games.GAME", Games.GamesMetadata.getCurrentGame(GooglePlay.getInstance().getApiClient()));
        intent.putExtra("com.google.android.gms.games.LEADERBOARD_ID", leaderboardId);
        return startGooglePlayGames(intent);
    }

    /**
     * Launches the list of players (in your circles) with this game.
     */
    public final boolean launchGPSPlayers() {
        Intent intent = buildDeepLinkIntent();
        if (intent == null) {
            return false;
        }
        intent.putExtra("com.google.android.gms.games.SCREEN", 1054);
        intent.putExtra("com.google.android.gms.games.GAME", Games.GamesMetadata.getCurrentGame(GooglePlay.getInstance().getApiClient()));
        return startGooglePlayGames(intent);
    }

    /**
     * Launches the quests available for this game
     */
    public final boolean launchGPSQuests() {
        Intent intent = buildDeepLinkIntent();
        if (intent == null) {
            return false;
        }
        intent.putExtra("com.google.android.gms.games.SCREEN", 1055);
        intent.putExtra("com.google.android.gms.games.GAME", Games.GamesMetadata.getCurrentGame(GooglePlay.getInstance().getApiClient()));
        return startGooglePlayGames(intent);
    }

    /**
     * Shows the current players profile
     */
    public final boolean launchGPSProfile() {
        Intent intent = buildDeepLinkIntent();
        if (intent == null) {
            return false;
        }
        intent.putExtra("com.google.android.gms.games.SCREEN", 1101);
        return startGooglePlayGames(intent);
    }

    /**
     * Shows the compare profiles page. You must pass a full Player object for the second player.
     * Make sure you use .freeze() on it so you can release your buffers.
     */
    public final boolean launchGPSProfileCompare(Player player) {
        //Bus.postObject(new CompareProfilesRequest(player));
        //return true;

        Intent intent = buildDeepLinkIntent();
        if (intent == null) {
            return false;
        }
        intent.putExtra("com.google.android.gms.games.SCREEN", 1102);
        intent.putExtra("com.google.android.gms.games.OTHER_PLAYER", player);
        return startGooglePlayGames(intent);

    }

    /**
     * Show the inbox (matches) list.
     */
    public final boolean launchGPSInbox() {
        Intent intent = buildDeepLinkIntent();
        if (intent == null) {
            return false;
        }
        intent.putExtra("com.google.android.gms.games.SCREEN", 1200);
        return startGooglePlayGames(intent);
    }

}
