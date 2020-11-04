package cn.qd.peiwen.player.enmudefine;

/**
 * Created by nick on 2017/3/29.
 */

public enum PlayerState {
    PLAYER_UNKNOWN(0),
    PLAYER_IDLE(1),
    PLAYER_PREPAREING(2),
    PLAYER_PREPARED(3),
    PLAYER_PLAYING(4),
    PLAYER_PAUSED(5);

    private final int index;

    PlayerState(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
