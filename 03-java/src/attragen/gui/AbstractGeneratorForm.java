package attragen.gui;

/**
 *
 * @author Rafał Hirsz
 */
public class AbstractGeneratorForm extends javax.swing.JFrame {
    public attragen.core.Generator gen;
    public void abortGeneration() {
        if (!gen.isRunning()) return;

        gen.abort();
    }
}
