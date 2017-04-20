package floatingmuseum.sample.sonic;

/**
 * Created by Floatingmuseum on 2017/4/20.
 */

public class GlobeConfig extends SingleTaskConfig {

    private int activeTaskNumber = 3;

    public int getActiveTaskNumber() {
        return activeTaskNumber;
    }

    public GlobeConfig setActiveTaskNumber(int activeTaskNumber) {
        this.activeTaskNumber = activeTaskNumber;
        return this;
    }
}
