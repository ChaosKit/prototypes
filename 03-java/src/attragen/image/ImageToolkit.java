package attragen.image;

import com.jhlabs.image.GaussianFilter;
import hirsz.util.OpenCL;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.*;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * Provides tools for image handling
 * @author Rafał Hirsz
 */
public class ImageToolkit {
    public static final int ADD = 1;
    public static final int SUBTRACT = 2;
    public static final int SOFTLIGHT = 3;

    public static final int[] BLACK = {0,0,0};
    public static final int[] WHITE = {255,255,255};

    protected BufferedImage img;
    protected Rectangle bounds;
    protected PixelAccess pa;

    public ImageToolkit(BufferedImage image) {
        img = image;
        pa = new PixelAccess(img);
        bounds = img.getData().getBounds();
    }

    public void fill(int[] color) {
        int maxx = bounds.x + bounds.width;
        int maxy = bounds.y + bounds.height;

        for (int x = bounds.x; x < maxx; x++) {
            for (int y = bounds.y; y < maxy; y++) {
                pa.setPixel(x, y, color);
            }
        }
    }

    public void rectangle(Rectangle rect, Color col, double alpha) {
        rectangle(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, col, alpha);
    }

    public void rectangle(int x1, int y1, int x2, int y2, Color col, double alpha) {
        int sizex = bounds.x + bounds.width;
        int sizey = bounds.y + bounds.height;

        int minx = Math.max(bounds.x, x1);
        int miny = Math.max(bounds.y, y1);
        int maxx = Math.min(sizex, x2);
        int maxy = Math.min(sizey, y2);

        int[] color = new int[3];
        for (int x = minx; x < maxx; x++) {
            for (int y = miny; y < maxy; y++) {
                int[] oldpixel = pa.getPixel(x, y);
                color = PixelBlender.alpha(oldpixel, col, alpha);
                pa.setPixel(x, y, color);
            }
        }
    }

    private double getAvgValue(double[][] array) {
        int maxx = array.length;
        int maxy = array[0].length;

        double sum = 0;
        int count = 0;

        for (int x = 0; x < maxx; x++) {
            for (int y = 0; y < maxy; y++) {
                if (array[x][y] > 0) {
                    sum += array[x][y];
                    ++count;
                }
            }
        }

        return sum / count;
    }

    private double getMaxValue(double[][] array) {
        int maxx = array.length;
        int maxy = array[0].length;

        double max = -1;

        for (int x = 0; x < maxx; x++) {
            for (int y = 0; y < maxy; y++) {
                if (array[x][y] > max) {
                    max = array[x][y];
                }
            }
        }

        return max;
    }

    private int getMode(double[][] array) {
        int maxx = array.length;
        int maxy = array[0].length;

        //int limit = (int)Math.round(getMaxValue(array) * 0.03);

        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();

        // Fill
        for (int x = 0; x < maxx; x++) {
            for (int y = 0; y < maxy; y++) {
                Integer val = Integer.valueOf((int)Math.round(array[x][y]));
                if (val.intValue() > 0) {
                    if (counts.containsKey(val)) {
                        counts.put(val, Integer.valueOf(counts.get(val).intValue()+1));
                    } else {
                        counts.put(val, Integer.valueOf(1));
                    }
                }
            }
        }

        // Find max
        int maxk = -1;
        int maxv = -1;
        Iterator it = counts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Integer> count = (Map.Entry<Integer, Integer>) it.next();
            if (count.getValue().intValue() > maxv) {
                maxv = count.getValue().intValue();
                maxk = count.getKey().intValue();
            }
        }

        return maxk;
    }

    public void drawPixel(int x, int y, int r, int g, int b) {
        if (!bounds.contains(x, y)) return;

        pa.setPixel(x, y, r, g, b);
    }

    public void blur(float radius) {
        if (OpenCL.detect()) {
            CLGaussianFilter filter = new CLGaussianFilter(radius);
            filter.filter(img, img);
        } else {
            GaussianFilter filter = new GaussianFilter(radius);
            filter.filter(img, img);
        }
    }

    public void blend(int mode, BufferedImage image) {
        PixelAccess pa2 = new PixelAccess(image);

        int maxx = bounds.x + bounds.width;
        int maxy = bounds.y + bounds.height;

        int[] color = new int[3];
        for (int x = bounds.x; x < maxx; x++) {
            for (int y = bounds.y; y < maxy; y++) {
                int[] pixela = pa.getPixel(x, y);
                int[] pixelb = pa2.getPixel(x, y);

                switch (mode) {
                    case ADD: color = PixelBlender.add(pixela, pixelb); break;
                    case SUBTRACT: color = PixelBlender.subtract(pixela, pixelb); break;
                    case SOFTLIGHT: color = PixelBlender.softLight(pixela, pixelb); break;
                }

                pa.setPixel(x, y, color);
            }
        }
    }

    public void fade(double alpha) {
        int maxx = bounds.x + bounds.width;
        int maxy = bounds.y + bounds.height;

        int[] color = new int[3];
        for (int x = bounds.x; x < maxx; x++) {
            for (int y = bounds.y; y < maxy; y++) {
                int[] oldpixel = pa.getPixel(x, y);
                color = PixelBlender.fade(oldpixel, alpha);
                pa.setPixel(x, y, color);
            }
        }
    }

    public void savePNG(String name) {
        File f = new File(name);
        try {
            ImageIO.write(img, "png", f);
        } catch (java.io.IOException e) {}
    }

    public BufferedImage cloneImage() {
        WritableRaster rasterCopy = img.copyData(null);
        return new BufferedImage(img.getColorModel(), rasterCopy, img.isAlphaPremultiplied(), null);
    }
}
