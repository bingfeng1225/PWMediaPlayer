package cn.qd.peiwen.pwvideoplayer.parsmeter;

public class Parameters {
    private boolean pauseByUser = false;
    private boolean enterBackground = false;

    public Parameters() {

    }

    public boolean isPauseByUser() {
        return pauseByUser;
    }

    public void setPauseByUser(boolean pauseByUser) {
        this.pauseByUser = pauseByUser;
    }

    public boolean isEnterBackground() {
        return enterBackground;
    }

    public void setEnterBackground(boolean enterBackground) {
        this.enterBackground = enterBackground;
    }

    public boolean isConditionsMeetRequirements() {
        if (pauseByUser) {
            return false;
        } else if (enterBackground) {
            return false;
        }
        return true;
    }
}
