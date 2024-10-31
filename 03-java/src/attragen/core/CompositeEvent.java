package attragen.core;

import java.util.EventObject;

/**
 *
 * @author Rafał Hirsz
 */
public class CompositeEvent extends EventObject {
    public CompositeEvent(Object source) {
        super(source);
    }
}
