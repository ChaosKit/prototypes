package attragen.core;

import java.util.EventObject;

/**
 * An event that occurs on every step of the generation process.
 *
 * @author Rafał Hirsz
 */
public class StepEvent extends EventObject {
    private int maxIterations = 10;
    private int progress = 0;

    public StepEvent(Object source) {
        super(source);
    }

    public void setProgress(int steps, int total) {
        progress = steps;
        maxIterations = total;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxIterations() {
        return maxIterations;
    }
}
