package attragen.core;

import java.util.EventListener;

/**
 *
 * @author Rafał Hirsz
 */
public interface AnimationListener extends EventListener {
    public void initialized(BeginEvent evt);
    public void stepOccured(StepEvent evt);
    public void finished(FinishEvent evt);
}
