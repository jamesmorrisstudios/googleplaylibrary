package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.support.annotation.NonNull;

import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.activities.BaseActivity;

/**
 * Created by James on 4/12/2016.
 */
public class GooglePlay {

    public enum AdEvent {
        SHOW_INTERSTITIAL, SHOW_REWARD_AD,
        INTERSTITIAL_AD_UNAVAILABLE, REWARD_AD_UNAVAILABLE,
        INTERSTITIAL_DISPLAYED, REWARD_AD_WATCHED;

        public int amount;

        public final AdEvent setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Send the action event
         */
        public final void post() {
            Bus.postEnum(this);
        }

    }

    public enum GooglePlayEvent {
        SIGN_IN_SUCCESS, SIGN_IN_FAIL, SIGN_OUT,
        ACHIEVEMENTS_ITEMS_FAIL, ACHIEVEMENTS_ITEMS_READY,
        LEADERBOARDS_META_FAIL, LEADERBOARDS_META_READY,
        LEADERBOARDS_FAIL, LEADERBOARDS_READY,
        LEADERBOARDS_MORE_FAIL, LEADERBOARDS_MORE_READY,

        ONLINE_SAVE_ITEM_LOAD_FAIL, ONLINE_SAVE_ITEM_LOAD_READY,

        PLAYERS_ACTIVE_FAIL, PLAYERS_ACTIVE_READY,
        PLAYERS_ALL_FAIL, PLAYERS_ALL_READY,
        PLAYERS_ALL_MORE_FAIL, PLAYERS_ALL_MORE_READY,


        SAVE_MATCH_LOCAL_FAIL, SAVE_MATCH_LOCAL_SUCCESS,
        LOAD_MATCH_LOCAL_FAIL, LOAD_MATCH_LOCAL_SUCCESS, SELECT_LOAD_MATCH_LOCAL_FAIL,
        FINISH_MATCH_ONLINE_FAIL, FINISH_MATCH_ONLINE_SUCCESS,
        LOAD_MATCH_ONLINE_FAIL, LOAD_MATCH_ONLINE_SUCCESS, SELECT_LOAD_MATCH_ONLINE_FAIL, SELECT_PLAYERS_ONLINE_FAIL,
        TAKE_TURN_ONLINE_FAIL, TAKE_TURN_ONLINE_SUCCESS,
        START_MATCH_ONLINE_FAIL, START_MATCH_ONLINE_SUCCESS,
        REMATCH_ONLINE_FAIL, REMATCH_ONLINE_SUCCESS,
        ACCEPT_INVITATION_ONLINE_FAIL, ACCEPT_INVITATION_ONLINE_SUCCESS,

        LEADERBOARD_SPINNER_CHANGE;

        /**
         * Send the action event
         */
        public final void post() {
            Bus.postEnum(this);
        }
    }

    public enum SaveType {
        INVITATION, ONGOING, YOUR_TURN, THEIR_TURN, COMPLETE
    }

    public enum Span {
        DAILY, WEEKLY, ALL_TIME;

        public static Span getFromInt(int variantInt) {
            switch (variantInt) {
                case LeaderboardVariant.TIME_SPAN_DAILY:
                    return DAILY;
                case LeaderboardVariant.TIME_SPAN_WEEKLY:
                    return WEEKLY;
                case LeaderboardVariant.TIME_SPAN_ALL_TIME:
                    return ALL_TIME;
                default:
                    return ALL_TIME;
            }
        }

        public int getInt() {
            switch (ordinal()) {
                case 0:
                    return LeaderboardVariant.TIME_SPAN_DAILY;
                case 1:
                    return LeaderboardVariant.TIME_SPAN_WEEKLY;
                case 2:
                    return LeaderboardVariant.TIME_SPAN_ALL_TIME;
                default:
                    return LeaderboardVariant.TIME_SPAN_ALL_TIME;
            }
        }
    }

    public enum Collection {
        SOCIAL, PUBLIC;

        public static Collection getFromInt(int variantInt) {
            switch (variantInt) {
                case LeaderboardVariant.COLLECTION_PUBLIC:
                    return PUBLIC;
                case LeaderboardVariant.COLLECTION_SOCIAL:
                    return SOCIAL;
                default:
                    return PUBLIC;
            }
        }

        public int getInt() {
            switch (ordinal()) {
                case 0:
                    return LeaderboardVariant.COLLECTION_SOCIAL;
                case 1:
                    return LeaderboardVariant.COLLECTION_PUBLIC;
                default:
                    return LeaderboardVariant.COLLECTION_PUBLIC;
            }
        }
    }

    /**
     * Listener
     */
    public interface GooglePlayListener {

        /**
         * NEVER store the result from this function.
         *
         * @return
         */
        @NonNull
        GameHelper getGameHelper();
    }

}
