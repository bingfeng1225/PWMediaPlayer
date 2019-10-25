package cn.qd.peiwen.pwvideoplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.view.SurfaceHolder;


import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import cn.qd.peiwen.pwtools.logger.PWLogger;
import cn.qd.peiwen.pwvideoplayer.enmudefine.ErrorType;
import cn.qd.peiwen.pwvideoplayer.enmudefine.PlayerState;
import cn.qd.peiwen.pwvideoplayer.listener.IPlayerListener;


/**
 * Created by nick on 2017/12/12.
 */

public class VideoPlayer implements
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnVideoSizeChangedListener {

    private Timer timer;
    private Context context;
    private Parameters parameters;
    private MediaPlayer mediaPlayer;
    private WeakReference<IPlayerListener> listener;

    private int seekTime = 0;
    private boolean ended = false;
    private boolean stoped = false;
    private boolean buffing = false;
    private ErrorType errorType = ErrorType.MEDIA_ERROR_NONE;
    private PlayerState playerState = PlayerState.PLAYER_UNKNOWN;

    public VideoPlayer(Context context) {
        this.context = context;
        this.parameters = new Parameters();
    }

    public void init() {
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnInfoListener(this);
        this.mediaPlayer.setOnErrorListener(this);
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnCompletionListener(this);
        this.mediaPlayer.setOnVideoSizeChangedListener(this);
        this.mediaPlayer.setOnBufferingUpdateListener(this);
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.setPlayerState(PlayerState.PLAYER_IDLE);
        this.mediaPlayer.reset();
    }

    public void setDisplay(SurfaceHolder holder) {
        this.mediaPlayer.setDisplay(holder);
    }

    public void release() {
        this.unload();
        this.mediaPlayer.release();
        this.playerState = PlayerState.PLAYER_UNKNOWN;
        this.mediaPlayer = null;
    }

    /************************ 属性接口 **********************************/
    public Object lastObject() {
        return this.parameters.playObject;
    }

    public int lastDuration() {
        return this.parameters.duration;
    }

    public int lastPosition() {
        return this.parameters.position;
    }

    public PlayerState playerState() {
        return playerState;
    }

    public void setSeekTime(int seekTime) {
        this.seekTime = seekTime;
    }

    public void setPlayObject(Object playObject) {
        this.parameters.position = 0;
        this.parameters.duration = 0;
        this.parameters.playObject = playObject;
    }

    public void setErrorType(ErrorType errorType) {
        if (errorType != this.errorType) {
            this.errorType = errorType;
            this.firePlayerErrorOccurred();
        }
    }

    public void setListener(IPlayerListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    public void setPlayerState(PlayerState playerState) {
        if (playerState != this.playerState) {
            this.playerState = playerState;
            this.firePlayerStateChanged();
        }
    }

    /************************ 状态校验接口 **********************************/
    public boolean canSeek() {
        if (null != mediaPlayer && isPrepared() && !isError() && !isStoped()) {
            return true;
        }
        return false;
    }

    public boolean canPause() {
        if (null != mediaPlayer && isPrepared() && !isError() && !isEnded() && !isStoped() && !isPaused()) {
            return true;
        }
        return false;
    }

    public boolean canResume() {
        if (null != mediaPlayer && isPrepared() && !isError() && !isEnded() && !isStoped() && !isPlaying()) {
            return true;
        }
        return false;
    }

    public boolean isError() {
        return (errorType != ErrorType.MEDIA_ERROR_NONE);
    }

    public boolean isEnded() {
        return ended;
    }

    public boolean isStoped() {
        return stoped;
    }

    public boolean isPaused() {
        return (playerState == PlayerState.PLAYER_PAUSED);
    }

    public boolean isPlaying() {
        return (playerState == PlayerState.PLAYER_PLAYING);
    }

    public boolean isBuffing() {
        return buffing;
    }

    public boolean isPrepared() {
        return (null != mediaPlayer && playerState.index() >= PlayerState.PLAYER_PREPARED.index());
    }

    public boolean isPrepareing() {
        return (null != mediaPlayer && playerState.index() >= PlayerState.PLAYER_PREPAREING.index());
    }

    public boolean hasPlayContent() {
        if (null != mediaPlayer && !isStoped() && !isError() && !isEnded() && isPrepareing()) {
            return true;
        }
        return false;
    }

    /************************ 周期及控制接口 **********************************/
    public void load(String url) {
        try {
            if (TextUtils.isEmpty(url)) {
                throw new RuntimeException("play url is null");
            }
            this.setPlayerState(PlayerState.PLAYER_PREPAREING);
            if (url.startsWith("/")) {
                this.mediaPlayer.setDataSource(context, Uri.fromFile(new File(url)));
            } else
                if (url.startsWith("assets://")) {
                String assets = url.substring("assets://".length());
                AssetFileDescriptor descriptor = context.getAssets().openFd(assets);
                this.mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            } else {
                this.mediaPlayer.setDataSource(url);
            }
            this.mediaPlayer.prepareAsync();
        } catch (Exception e) {
            PWLogger.e(e);
            this.setErrorType(ErrorType.MEDIA_ERROR_INTERNAL_DEVICE_ERROR);
        }
    }

    public void stop() {
        if (this.hasPlayContent()) {
            this.stopTimer();
            this.stoped = true;
            this.mediaPlayer.reset();
            this.firePlayerStoped();
        }
    }

    public void unload() {
        this.stopTimer();
        this.mediaPlayer.reset();
        this.ended = false;
        this.stoped = false;
        this.buffing = false;
        this.seekTime = 0;
        this.setErrorType(ErrorType.MEDIA_ERROR_NONE);
        this.setPlayerState(PlayerState.PLAYER_IDLE);
    }

    public void rewind() {
        if (this.canSeek()) {
            int cur = mediaPlayer.getCurrentPosition();
            if (cur > 5 * 1000) {
                seekToTime((cur - 5 * 1000));
            } else {
                seekToTime(0);
            }
        }
    }

    public void fastForward() {
        if (this.canSeek()) {
            int cur = mediaPlayer.getCurrentPosition();
            int total = mediaPlayer.getDuration();
            if (total - cur > 5 * 1000) {
                seekToTime((cur + 5 * 1000));
            } else {
                seekToTime(total);
            }
        }
    }

    public void seekToTime(int msec) {
        if (this.canSeek()) {
            mediaPlayer.seekTo(msec);
        }
    }

    public void pause() {
        if (!this.parameters.pauseByUser) {
            this.parameters.pauseByUser = true;
            this.parametersChanged();
        }
    }

    public void resume() {
        if (!this.parameters.pauseByUser) {
            this.parameters.pauseByUser = false;
            this.parametersChanged();
        }
    }

    public void enterBackground() {
        if (!this.parameters.enterBackground) {
            this.parameters.enterBackground = true;
            this.parametersChanged();
        }
    }

    public void becomeForeground() {
        if (this.parameters.enterBackground) {
            this.parameters.enterBackground = false;
            this.parametersChanged();
        }
    }

    public void changedVolume(float volume){
        if(null != this.mediaPlayer){
            this.mediaPlayer.setVolume(volume,volume);;
        }
    }

    private void parametersChanged() {
        if (this.parameters.isConditionsMeetRequirements()) {
            if (this.canResume()) {
                this.startTimer();
                this.mediaPlayer.start();
                this.setPlayerState(PlayerState.PLAYER_PLAYING);
            }
        } else {
            if (this.canPause()) {
                this.stopTimer();
                this.mediaPlayer.pause();
                this.setPlayerState(PlayerState.PLAYER_PAUSED);
            }
        }
    }

    /************************ 播放进度相关的接口 **********************************/
    private void startTimer() {
        if (null == this.timer) {
            this.timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    parameters.position = mediaPlayer.getCurrentPosition();
                    firePlayerProgressChanged(parameters.position, parameters.duration);
                }
            }, 100, 1000);
        }
    }

    private void stopTimer() {
        if (null != this.timer) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    /************************ 播放器状态监听接口 **********************************/
    @Override
    public void onPrepared(MediaPlayer mp) {
        this.setPlayerState(PlayerState.PLAYER_PREPARED);
        if (this.seekTime > 0) {
            this.seekToTime(this.seekTime);
        }
        parameters.duration = mediaPlayer.getDuration();
        parameters.position = this.seekTime;
        if (this.parameters.isConditionsMeetRequirements()) {
            startTimer();
            this.mediaPlayer.start();
            this.setPlayerState(PlayerState.PLAYER_PLAYING);
        } else {
            this.setPlayerState(PlayerState.PLAYER_PAUSED);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        this.stopTimer();
        this.ended = true;
        parameters.position = parameters.duration;
        this.firePlayerCompleted();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        this.fireBufferingUpdated(percent);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (!this.buffing) {
                    this.buffing = true;
                    this.firePlayerBufferingStart();
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (this.buffing) {
                    this.buffing = false;
                    this.firePlayerBufferingEnded();
                }
                break;
        }
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        this.fireVideoSizeChanged(width,height);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (what == -38) {
            return true;
        }
        this.stopTimer();
        ErrorType errorType;
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_IO:
                // Stream服务端返回错误 (bad request, unauthorized, forbidden, not found etc)
                errorType = ErrorType.MEDIA_ERROR_INVALID_REQUEST;
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                // 端无法连接stream服务端
                errorType = ErrorType.MEDIA_ERROR_SERVICE_UNAVAILABLE;
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                // 端内部错误
                errorType = ErrorType.MEDIA_ERROR_INTERNAL_DEVICE_ERROR;
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                // stream服务端接受请求，但未能正确处理 ?????
                errorType = ErrorType.MEDIA_ERROR_INTERNAL_SERVER_ERROR;
                break;
            default:
                // 未知错误
                errorType = ErrorType.MEDIA_ERROR_UNKNOWN;
                break;
        }
        this.setErrorType(errorType);
        return true;
    }

    /************************ 分发事件的接口 **********************************/
    private void firePlayerStoped() {
        if (null != this.listener && null != this.listener.get()) {
            listener.get().onPlayerStoped(this, lastObject());
        }
    }

    private void firePlayerCompleted() {
        if (null != this.listener && null != this.listener.get()) {
            listener.get().onPlayerCompleted(this, lastObject());
        }
    }

    private void firePlayerStateChanged() {
        if (null != this.listener && null != this.listener.get()) {
            listener.get().onPlayerStateChanged(this, lastObject(), this.playerState);
        }
    }

    private void firePlayerErrorOccurred() {
        if (null != this.listener && null != this.listener.get()) {
            listener.get().onPlayerErrorOccurred(this, lastObject(), this.errorType);
        }
    }

    private void firePlayerBufferingEnded() {
        if (null != this.listener && null != this.listener.get()) {
            listener.get().onPlayerBufferingEnded(this, lastObject());
        }
    }

    private void firePlayerBufferingStart() {
        if (null != this.listener && null != this.listener.get()) {
            listener.get().onPlayerBufferingStart(this, lastObject());
        }
    }

    private void fireBufferingUpdated(int bufferPercentage) {
        if (null != this.listener && null != this.listener.get()) {
            listener.get().onBufferingUpdated(this, lastObject(), bufferPercentage);
        }
    }

    private void fireVideoSizeChanged(int width, int height) {
        if (null != this.listener && null != this.listener.get()) {
            listener.get().onVideoSizeChanged(this, lastObject(), width, height);
        }
    }

    private void firePlayerProgressChanged(int position, int duration) {
        if (null != this.listener && null != this.listener.get()) {
            listener.get().onPlayerProgressChanged(this, lastObject(), position, duration);
        }
    }
}
