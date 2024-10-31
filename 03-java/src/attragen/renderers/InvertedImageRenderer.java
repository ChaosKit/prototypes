package attragen.renderers;

/**
 *
 * @author Rafał Hirsz
 */
public class InvertedImageRenderer extends ImageRenderer {
    public InvertedImageRenderer(java.awt.image.BufferedImage img) {
        super(img);
    }

    @Override public void prepare() {
        toolkit.fill(attragen.image.ImageToolkit.WHITE);
    }

    @Override protected int getColor(double value) {
        return 255 - Math.min(255, (int)Math.round(value));
    }

    @Override protected int blendMode() {
        return attragen.image.ImageToolkit.SUBTRACT;
    }
}
