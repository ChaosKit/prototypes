package attragen.image;

import java.awt.image.*;

/**
 *
 * @author Rafał Hirsz
 */
public class PixelAccess {
    private int[] buffer;
    private int stride, offset;

    public PixelAccess(BufferedImage img) {
        WritableRaster raster = img.getRaster();

        SinglePixelPackedSampleModel model;
        buffer = ((DataBufferInt) raster.getDataBuffer()).getData();
        model = (SinglePixelPackedSampleModel) raster.getSampleModel();

        stride = model.getScanlineStride();
        int sx = raster.getSampleModelTranslateX();
        int sy = raster.getSampleModelTranslateY();
        offset = -(sy*stride + sx);
    }

    public void setPixel(int x, int y, int r, int g, int b) {
        buffer[offset + y*stride + x] = 0xff000000 | ((r << 16) | (g << 8) | b);
    }
    public void setPixel(int x, int y, int[] color) {
        setPixel(x, y, color[0], color[1], color[2]);
    }
    public int[] getPixel(int x, int y) {
        int val = getPackedPixel(x, y);
        int[] color = new int[3];
        color[2] = val & 255;
        color[1] = (val >> 8) & 255;
        color[0] = (val >> 16) & 255;
        return color;
    }
    public int getPackedPixel(int x, int y) {
        return buffer[offset + y*stride + x];
    }
    public void setPackedPixed(int x, int y, int color) {
        buffer[offset + y*stride + x] = color;
    }
}
