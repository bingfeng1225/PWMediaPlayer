package cn.qd.peiwen.pwvideoplayer;


/**
 * Created by nick on 2017/12/26.
 */

public class Parameters {
    public int duration = 0;
    public int position = 0;
    public Object playObject = null;
    public boolean pauseByUser = false;
    public boolean enterBackground = true;

    public boolean isConditionsMeetRequirements() {
        if (pauseByUser) {
            return false;
        } else if (enterBackground) {
            return false;
        }
        return true;
    }
}
