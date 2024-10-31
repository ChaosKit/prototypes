package attragen.renderers;

import attragen.image.ImageToolkit;
import attragen.image.PixelBlender;
import java.awt.Rectangle;
import java.awt.image.*;

/**
 *
 * @author Rafał Hirsz
 */
public class ImageRenderer extends Renderer {
    protected BufferedImage image, original, blur;
    protected ImageToolkit toolkit, originalToolkit, blurToolkit;

    public ImageRenderer(BufferedImage img) {
        image = img;
        toolkit = new ImageToolkit(image);

        blur = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        blurToolkit = new ImageToolkit(blur);
        blurToolkit.fill(ImageToolkit.BLACK);
    }

    @Override
    public void prepare() {
        toolkit.fill(ImageToolkit.BLACK);
    }

    @Override
    public void render() {
        int maxx = values.length;
        int maxy = values[0].length;

        for (int x = 0; x < maxx; x++) {
            for (int y = 0; y < maxy; y++) {
                if (!parent.isRunning()) return;

                int color = getColor(values[x][y]);

                drawPixel(x, y, color);
            }
        }
    }

    protected int getColor(double value) {
        return Math.min(255, (int)Math.round(value));
    }

    protected void drawPixel(int x, int y, int color) {
        toolkit.drawPixel(x, y, color, color, color);
    }

    @Override
    public void setPointColor(int x, int y, java.awt.Color color) {
       /*int off = 5;

        Rectangle rect = new Rectangle(x-off, y-off, off*2, off*2);
        blurToolkit.rectangle(rect, color, 0.075);*/
        blurToolkit.drawPixel(x, y, color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public void setPointColor(double x, double y, java.awt.Color color) {
        int w = values.length;
        int h = values[0].length;

        setPointColor((int)(x*w), (int)(y*h), color);
    }

    @Override
    public void colorize() {
        blurToolkit.blur(5);

        if (!parent.isRunning()) return;

        original = toolkit.cloneImage();
        originalToolkit = new ImageToolkit(original);

        Raster srcRaster = blur.getData();
        WritableRaster dstRaster = image.getRaster();

        int maxx = blur.getWidth();
        int maxy = blur.getHeight();

        int[] aColor = new int[3];
        int[] bColor = new int[3];
        int[] resColor = new int[3];
        for (int i=0; i<maxx; i++) {
            for (int j=0; j<maxy; j++) {
                if (!parent.isRunning()) return;

                aColor = srcRaster.getPixel(i, j, aColor);
                bColor = dstRaster.getPixel(i, j, bColor);

                resColor = colorizeMethod(aColor, bColor);

                dstRaster.setPixel(i, j, resColor);
            }
        }
    }

    @Override
    public void postprocess(int level) {
        BufferedImage temp;
        ImageToolkit tk;

        if (!parent.isRunning()) return;

        if (level > 0) {
            int w = values.length;
            int h = values[0].length;

            temp = toolkit.cloneImage();
            tk = new ImageToolkit(temp);
            tk.blur((float)(Math.sqrt(w*h) * 0.125));
            toolkit.blend(blendMode(), temp);
        }

        if (!parent.isRunning()) return;

        if (level > 1) {
            temp = toolkit.cloneImage();
            tk = new ImageToolkit(temp);
            tk.blur(10);
            tk.fade(0.3f);
            toolkit.blend(blendMode(), temp);
        }      
    }

    @Override
    public void toFile(String name) {
        toolkit.savePNG(name);
    }

    protected int[] colorizeMethod(int[] a, int[] b) {
        return PixelBlender.color(a, b);
    }

    protected int blendMode() {
        return ImageToolkit.ADD;
    }
}
