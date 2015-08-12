package com.jamesmorrisstudios.googleplaylibrary.game;

/**
 * Created by James on 8/1/2015.
 */
public class GameDetails {

    public enum MatchType {
        ONLINE, OFFLINE
    }

    public enum NumberPlayers {
        ONE, TWO, THREE, FOUR, FIVE, SIX
    }

    //TWO works with 2, 4, or 6 players
    public enum NumberTeams {
        FREE_FOR_ALL, TWO
    }

    public enum MatchVariant {
        VARIANT_1(1), VARIANT_2(2), VARIANT_3(3), VARIANT_4(4), VARIANT_5(5), VARIANT_6(6);

        public final int number;

        MatchVariant(int number) {
            this.number = number;
        }

        public final MatchVariant getVariant(int number) {
            switch(number) {
                case 1:
                    return VARIANT_1;
                case 2:
                    return VARIANT_2;
                case 3:
                    return VARIANT_3;
                case 4:
                    return VARIANT_4;
                case 5:
                    return VARIANT_5;
                case 6:
                    return VARIANT_6;
                default:
                    return VARIANT_1;
            }
        }
    }

    public enum MatchAddon {
        ADDON_1, ADDON_2, ADDON_3, ADDON_4, ADDON_5, ADDON_6
    }

    public enum PlayerType {
        HUMAN, EASY, MEDIUM, HARD
    }

    public enum Player {
        PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4, PLAYER_5, PLAYER_6;

        public Team getTeam(NumberTeams numberTeams) {
            switch(numberTeams) {
                case FREE_FOR_ALL:
                    return Team.NONE;
                case TWO:
                    switch(this) {
                        case PLAYER_1:
                        case PLAYER_3:
                        case PLAYER_5:
                            return Team.TEAM_1;
                        case PLAYER_2:
                        case PLAYER_4:
                        case PLAYER_6:
                            return Team.TEAM_2;
                    }
                default:
                    return Team.NONE;
            }
        }

        public Player getPlayer(int index) {
            switch(index) {
                case 0:
                    return PLAYER_1;
                case 1:
                    return PLAYER_2;
                case 2:
                    return PLAYER_3;
                case 3:
                    return PLAYER_4;
                case 4:
                    return PLAYER_5;
                case 5:
                    return PLAYER_6;
                default:
                    return PLAYER_1;
            }
        }

        public int getIndex(Player player) {
            switch(player) {
                case PLAYER_1:
                    return 0;
                case PLAYER_2:
                    return 1;
                case PLAYER_3:
                    return 2;
                case PLAYER_4:
                    return 3;
                case PLAYER_5:
                    return 4;
                case PLAYER_6:
                    return 5;
                default:
                    return 0;
            }
        }
    }

    public enum Team {
        NONE, TEAM_1, TEAM_2
    }

}
