package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.Batch;
import com.google.android.gms.common.api.BatchResult;
import com.google.android.gms.common.api.BatchResultToken;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.games.internal.api.TurnBasedMultiplayerImpl;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.ParticipantEntity;
import com.google.android.gms.games.multiplayer.ParticipantEntityCreator;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.LoadMatchesResponse;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchEntity;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchEntityCreator;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.Logger;
import com.jamesmorrisstudios.appbaselibrary.Utils;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementItem;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardItem;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardMetaItem;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardMetaVariantItem;
import com.jamesmorrisstudios.googleplaylibrary.data.OnlineSaveItem;
import com.jamesmorrisstudios.googleplaylibrary.data.PlayerPickerItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by James on 5/11/2015.
 */
public class GooglePlayCalls {
    public static final String TAG = "GooglePlayCalls";
    private static GooglePlayCalls instance = null;
    private GooglePlay.GooglePlayListener listener;

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

    /**
     * Attach to the containing activity. Call from onCreate
     *
     * @param listener Activity implementing the build manager listener.
     */
    public final void attach(@NonNull final GooglePlay.GooglePlayListener listener) {
        this.listener = listener;
    }

    /**
     * Detach from activity. Call from onDestroy
     */
    public final void detach() {
        this.listener = null;
    }

    public final boolean isSignedIn() {
        return listener != null && listener.getGameHelper() != null && listener.getGameHelper().isSignedIn();
    }

    public final Player getCurrentPlayer() {
        return Games.Players.getCurrentPlayer(listener.getGameHelper().getApiClient());
    }







    public synchronized final void loadPlayersActive(boolean forceRefresh) {
        if (hasPlayersActive() && !forceRefresh) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ACTIVE_READY);
            return;
        }
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ACTIVE_FAIL);
            return;
        }
        Games.Players.loadConnectedPlayers(listener.getGameHelper().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Players.LoadPlayersResult>() {
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
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ALL_FAIL);
            return;
        }
        Games.Players.loadInvitablePlayers(listener.getGameHelper().getApiClient(), 25, forceRefresh).setResultCallback(new ResultCallback<Players.LoadPlayersResult>() {
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
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.PLAYERS_ALL_MORE_FAIL);
            return;
        }
        playersAllMore = null;
        Games.Players.loadMoreInvitablePlayers(listener.getGameHelper().getApiClient(), 10).setResultCallback(new ResultCallback<Players.LoadPlayersResult>() {
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
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ACHIEVEMENTS_ITEMS_FAIL);
            return;
        }
        Games.Achievements.load(listener.getGameHelper().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Achievements.LoadAchievementsResult>() {
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
        Games.Achievements.unlock(listener.getGameHelper().getApiClient(), achievementId);
    }

    public final void incrementAchievement(@NonNull String achievementId, int numberIncrements) {
        Games.Achievements.increment(listener.getGameHelper().getApiClient(), achievementId, numberIncrements);
    }






    public synchronized final void loadLeaderboardsMeta(boolean forceRefresh, @NonNull final String[] leaderboardIds) {
        Log.v("GooglePlayCalls", "Load ic_leaderboard Meta Data");
        if (hasLeaderboardsMeta() && !forceRefresh && (this.leaderboardIds == null || Arrays.equals(leaderboardIds, this.leaderboardIds))) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_READY);
            return;
        }
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_FAIL);
            return;
        }
        this.leaderboardIds = leaderboardIds;
        Games.Leaderboards.loadLeaderboardMetadata(listener.getGameHelper().getApiClient(), forceRefresh).setResultCallback(new ResultCallback<Leaderboards.LeaderboardMetadataResult>() {
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
        final Batch.Builder builder = new Batch.Builder(listener.getGameHelper().getApiClient());
        final ArrayList<PlayerScoresContainer> playerScores = new ArrayList<>();
        for (String id : leaderboardIds) {
            PendingResult<Leaderboards.LoadPlayerScoreResult> result = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(listener.getGameHelper().getApiClient(), id, leaderboardSpan.getInt(), leaderboardCollection.getInt());
            playerScores.add(new PlayerScoresContainer(id, builder.add(result)));
        }
        final Batch batch = builder.build();
        batch.setResultCallback(new ResultCallback<BatchResult>() {
            @Override
            public void onResult(@NonNull BatchResult batchResult) {
                Log.v("GooglePlayCalls", "Batch Complete");
                if(batchResult.getStatus().isSuccess()) {
                    for(PlayerScoresContainer playerScore : playerScores) {
                        Leaderboards.LoadPlayerScoreResult result = batchResult.take(playerScore.token);
                        if(result.getStatus().isSuccess()) {
                            LeaderboardScore score = result.getScore();
                            LeaderboardMetaItem item = getLeaderboardMetaItem(playerScore.id);
                            if (score != null && item != null) {
                                item.updateVariant(leaderboardCollection, leaderboardSpan, score.getDisplayRank(), score.getDisplayScore(), score.getRank(), score.getRawScore());
                            }
                        }
                    }
                }
                Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_META_READY);
            }
        }, timeout, timeUnit);
    }

    private class PlayerScoresContainer {
        public final String id;
        public final BatchResultToken<Leaderboards.LoadPlayerScoreResult> token;

        public PlayerScoresContainer(@NonNull final String id, @NonNull final BatchResultToken<Leaderboards.LoadPlayerScoreResult> token) {
            this.id = id;
            this.token = token;
        }

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
        if (!isSignedIn() || leaderboardId == null) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_FAIL);
            return;
        }
        Games.Leaderboards.loadTopScores(listener.getGameHelper().getApiClient(), leaderboardId, leaderboardSpan.getInt(), leaderboardCollection.getInt(),
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
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARDS_MORE_FAIL);
            return;
        }
        Log.v("GooglePlayCalls", "Load leaderboards more");
        Games.Leaderboards.loadMoreScores(listener.getGameHelper().getApiClient(), leaderboardBuffer, 10, PageDirection.NEXT).setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {
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

    public final void updateLeaderboard(@NonNull String leaderboardId, long value) {
        if (!isSignedIn()) {
            //TODO error
            return;
        }
        Games.Leaderboards.submitScore(listener.getGameHelper().getApiClient(), leaderboardId, value);
    }



    public synchronized final void loadOnlineSaves(boolean forceRefresh) {
        if (hasOnlineSaveItems() && !forceRefresh) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ONLINE_SAVE_ITEM_LOAD_READY);
            return;
        }
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ONLINE_SAVE_ITEM_LOAD_FAIL);
            return;
        }


        Games.TurnBasedMultiplayer.loadMatchesByStatus(listener.getGameHelper().getApiClient(),
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



    public final void acceptInvitationOnline(@NonNull Invitation invitation) {
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.ACCEPT_INVITATION_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.acceptInvitation(listener.getGameHelper().getApiClient(), invitation.getInvitationId()).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
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
        if (!isSignedIn()) {
            //TODO error
            return;
        }
        Games.TurnBasedMultiplayer.dismissMatch(listener.getGameHelper().getApiClient(), matchId);
    }

    public final void rematchOnline(@NonNull String matchId) {
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.REMATCH_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.rematch(listener.getGameHelper().getApiClient(), matchId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
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
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.START_MATCH_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.createMatch(listener.getGameHelper().getApiClient(), matchConfig).setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
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
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.FINISH_MATCH_ONLINE_FAIL);
            return;
        }
        if (results != null) {
            Games.TurnBasedMultiplayer.finishMatch(listener.getGameHelper().getApiClient(), match.getMatchId(), data, results).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
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
            Games.TurnBasedMultiplayer.finishMatch(listener.getGameHelper().getApiClient(), match.getMatchId(), data).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
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
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.TAKE_TURN_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.takeTurn(listener.getGameHelper().getApiClient(), match.getMatchId(), data, nextParticipantId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
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
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_FAIL);
            return;
        }
        onlineMatch = match;
        Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_SUCCESS);
    }

    public final void loadMatchOnline(@NonNull String matchId) {
        if (!isSignedIn()) {
            Bus.postEnum(GooglePlay.GooglePlayEvent.LOAD_MATCH_ONLINE_FAIL);
            return;
        }
        Games.TurnBasedMultiplayer.loadMatch(listener.getGameHelper().getApiClient(), matchId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.LoadMatchResult>() {
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
