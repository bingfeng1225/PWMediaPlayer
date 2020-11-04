package cn.qd.peiwen.player.listener;


import cn.qd.peiwen.player.VideoPlayer;
import cn.qd.peiwen.player.enmudefine.ErrorType;
import cn.qd.peiwen.player.enmudefine.PlayerState;

/**
 * Created by nick on 2017/3/29.
 */

public interface IPlayerListener {
    void onPlayerStoped(VideoPlayer player, Object object);

    void onPlayerCompleted(VideoPlayer player, Object object);

    void onPlayerBufferingStart(VideoPlayer player, Object object);

    void onPlayerBufferingEnded(VideoPlayer player, Object object);

    void onPlayerErrorOccurred(VideoPlayer player, Object object, ErrorType error);

    void onPlayerStateChanged(VideoPlayer player, Object object, PlayerState state);

    void onBufferingUpdated(VideoPlayer player, Object object, int bufferPercentage);

    void onVideoSizeChanged(VideoPlayer player, Object object, int width, int height);

    void onPlayerProgressChanged(VideoPlayer player, Object object, int position, int duration);

}
