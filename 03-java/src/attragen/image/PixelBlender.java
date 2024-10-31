package attragen.image;

import java.awt.Color;
import tips4java.HSLColor;

/**
 *
 * @author Rafał Hirsz
 */
public class PixelBlender {
    public static int[] alpha(int[] src, Color color, double level) {
        int[] dst = new int[3];

        dst[0] = (int)(src[0] + (color.getRed() - src[0]) * level);
        dst[1] = (int)(src[1] + (color.getGreen() - src[1]) * level);
        dst[2] = (int)(src[2] + (color.getBlue() - src[2]) * level);

        return dst;
    }

   public static int[] fade(int[] src, double level) {
        int[] dst = new int[3];

        dst[0] = (int)(src[0] * level);
        dst[1] = (int)(src[1] * level);
        dst[2] = (int)(src[2] * level);

        return dst;
    }

    public static int[] add(int[] src, int[] dst) {
        int[] color = new int[3];

        color[0] = src[0] + dst[0];
        color[1] = src[1] + dst[1];
        color[2] = src[2] + dst[2];

        if (color[0] > 255) color[0] = 255;
        if (color[1] > 255) color[1] = 255;
        if (color[2] > 255) color[2] = 255;

        return color;
    }

    public static int[] subtract(int[] src, int[] dst) {
        int[] color = new int[3];

        color[0] = src[0] + dst[0] - 255;
        color[1] = src[1] + dst[1] - 255;
        color[2] = src[2] + dst[2] - 255;

        if (color[0] < 0) color[0] = 0;
        if (color[1] < 0) color[1] = 0;
        if (color[2] < 0) color[2] = 0;

        return color;
    }

    public static int[] softLight(int[] src, int[] dst) {
        int[] color = new int[3];

        double[] dsrc = {src[0]/255, src[1]/255, src[2]/255};
        double[] ddst = {dst[0]/255, dst[1]/255, dst[2]/255};

        for (int i=0; i<3; i++) {
            if (dsrc[i] <= 0.5) {
                color[i] = (int)Math.round(((2*dsrc[i]-1)*(ddst[i]-ddst[i]*ddst[i])+ddst[i])*255);
            } else {
                color[i] = (int)Math.round(((2*dsrc[i]-1)*(Math.sqrt(ddst[i])-ddst[i])+ddst[i])*255);
            }
        }

        return color;
    }

    public static int[] color(int[] src, int[] dst) {
        int[] color = new int[3];

        float[] ahsl = HSLColor.fromRGB(new Color(src[0], src[1], src[2]));
        float[] bhsl = HSLColor.fromRGB(new Color(dst[0], dst[1], dst[2]));
        Color res = HSLColor.toRGB(ahsl[0], ahsl[1], bhsl[2]);

        color[0] = res.getRed();
        color[1] = res.getGreen();
        color[2] = res.getBlue();

        return color;
    }
}
