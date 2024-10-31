package attragen.core;

import java.util.EventListener;

/**
 * The step event listener.
 *
 * @author Rafał Hirsz
 */
public interface GeneratorListener extends EventListener {
    public void initialized(BeginEvent evt);
    public void generationStarted(GenerateEvent evt);
    public void compositingStarted(CompositeEvent evt);
    public void stepOccured(StepEvent evt);
    public void finished(FinishEvent evt);
}
