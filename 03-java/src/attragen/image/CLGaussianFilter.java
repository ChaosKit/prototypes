package attragen.image;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import com.nativelibs4java.opencl.*;
import com.nativelibs4java.util.*;
import java.io.IOException;
import java.nio.IntBuffer;
import hirsz.util.MathUtils;

/**
 * A filter which applies Gaussian blur to an image.
 * Original C++ code copyright 1993-2009 NVIDIA Corporation.
 *
 * @author Rafał Hirsz (ported to Java)
 */
public class CLGaussianFilter implements BufferedImageOp {

    private float sigma = 2;                // filter sigma (blur factor)
    private final int BLOCK_DIM = 16;
    private int numThreads = 16;	    // number of threads per block
    private String clSourceFile = "RecursiveGaussian.cl";
    private String fileContents;
    // Parameters
    //private float alpha;
    //private float ema;
    //private float ema2;
    private float b1;
    private float b2;
    private float a0;
    private float a1;
    private float a2;
    private float a3;
    private float coefp;
    private float coefn;

    private CLContext context;
    private CLQueue queue;

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null) {
            dstCM = src.getColorModel();
        }
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Double();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }

    public CLGaussianFilter(float radius) {
        sigma = radius;
        calculateParams();

        context = JavaCL.createBestContext();
        queue = context.createDefaultQueue();

        try {
            fileContents = IOUtils.readTextClose(CLGaussianFilter.class.getResourceAsStream(clSourceFile));
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void calculateParams() {
        double alpha = 1.695f / sigma;
        double ema = Math.exp(-alpha);
        double ema2 = Math.exp(-2 * alpha);

        double lb1 = -2 * ema;
        double lb2 = ema2;
        b1 = (float)lb1;
        b2 = (float)lb2;

        final double k = (1 - ema) * (1 - ema) / (1 + (2 * alpha * ema) - ema2);
        double la0 = k;
        double la1 = k * (alpha - 1) * ema;
        double la2 = k * (alpha + 1) * ema;
        double la3 = -k * ema2;

        a0 = (float)la0;
        a1 = (float)la1;
        a2 = (float)la2;
        a3 = (float)la3;

        coefp = (float)((la0 + la1) / (1 + lb1 + lb2));
        coefn = (float)((la2 + la3) / (1 + lb1 + lb2));
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        int size = width * height;

        if (dst == null) {
            dst = createCompatibleDestImage(src, null);
        }

        // OpenCL magic
        int[] gaussLocalWork = new int[]{numThreads};
        int[] gaussGlobalWork = new int[]{MathUtils.roundUp(gaussLocalWork[0], width)};
        int[] transposeLocalWork = new int[]{BLOCK_DIM, BLOCK_DIM};
        int[] transposeGlobalWork = new int[]{MathUtils.roundUp(transposeLocalWork[0], width), MathUtils.roundUp(transposeLocalWork[1], height)};

        IntBuffer bufIn = IntBuffer.wrap(((DataBufferInt) src.getRaster().getDataBuffer()).getData());
        IntBuffer bufTemp = NIOUtils.directInts(size, context.getByteOrder());
        IntBuffer bufOut = NIOUtils.directInts(size, context.getByteOrder());

        try {
            CLProgram program = context.createProgram(fileContents).build();

            CLIntBuffer clBufIn = context.createIntBuffer(CLMem.Usage.Input, bufIn, true);
            CLIntBuffer clBufTemp = context.createIntBuffer(CLMem.Usage.InputOutput, bufTemp, false);
            CLIntBuffer clBufOut = context.createIntBuffer(CLMem.Usage.InputOutput, bufOut, false);

            CLKernel kernel = program.createKernel("RecursiveGaussianRGBA",
                    clBufIn, clBufTemp,
                    width, height,
                    a0, a1,
                    a2, a3,
                    b1, b2,
                    coefp, coefn);
            kernel.enqueueNDRange(queue, gaussGlobalWork, gaussLocalWork);

            CLKernel transpose = program.createKernel("Transpose",
                    clBufTemp, clBufOut,
                    width, height,
                    new CLKernel.LocalSize(4 * BLOCK_DIM * (BLOCK_DIM + 1)));
            transpose.enqueueNDRange(queue, transposeGlobalWork, transposeLocalWork);

            gaussGlobalWork[0] = MathUtils.roundUp(gaussLocalWork[0], height);
            kernel.setArg(0, clBufOut);
            kernel.setArg(1, clBufTemp);
            kernel.setArg(2, height);
            kernel.setArg(3, width);
            kernel.enqueueNDRange(queue, gaussGlobalWork, gaussLocalWork);

            transposeGlobalWork[0] = MathUtils.roundUp(transposeLocalWork[0], height);
            transposeGlobalWork[1] = MathUtils.roundUp(transposeLocalWork[1], width);
            transpose.setArg(0, clBufTemp);
            transpose.setArg(1, clBufOut);
            transpose.setArg(2, height);
            transpose.setArg(3, width);
            transpose.enqueueNDRange(queue, transposeGlobalWork, transposeLocalWork);

            clBufOut.read(queue, bufOut, true);

            queue.finish();

            // Read the buffer
            int[] data = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();
            int[] results = new int[size];
            bufOut.get(results);

            for (int i = size; i-- != 0;) {
                data[i] = results[i];
            }

        } catch (CLBuildException e) {
            System.out.println(e);
        }

        /*
        // Normalny kod CPU
        int[] bufIn = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
        int[] bufOut = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();
        int[] bufTemp = new int[size];

        RecursiveGaussianRGBAHost(bufIn, bufTemp, width, height, a0, a1, a2, a3, b1, b2, coefp, coefn);
        TransposeHost(bufTemp, bufOut, width, height);
        RecursiveGaussianRGBAHost(bufOut, bufTemp, height, width, a0, a1, a2, a3, b1, b2, coefp, coefn);
        TransposeHost(bufTemp, bufOut, height, width);*/

        return dst;
    }

    // W TYM MOMENCIE ZACZYNA SIĘ TWARDE PRZEPISANIE WERSJI C++
    private float[] rgbaUintToFloat4(int uiPackedRGBA) {
        float[] rgba = new float[4];
        rgba[0] = (float) (uiPackedRGBA & 0xff);
        rgba[1] = (float) ((uiPackedRGBA >> 8) & 0xff);
        rgba[2] = (float) ((uiPackedRGBA >> 16) & 0xff);
        rgba[3] = (float) ((uiPackedRGBA >> 24) & 0xff);
        return rgba;
    }

    private int rgbaFloat4ToUint(float[] rgba) {
        // Clamp to zero
        for (int i = 0; i < 4; ++i) {
            if (rgba[i] < 0.0f) {
                rgba[i] = 0.0f;
            }
        }

        int uiPackedPix = 0;
        uiPackedPix |= 0x000000FF & (int)rgba[0];
        uiPackedPix |= 0x0000FF00 & ((int)(rgba[1]) << 8);
        uiPackedPix |= 0x00FF0000 & ((int)(rgba[2]) << 16);
        uiPackedPix |= 0xFF000000 & ((int)(rgba[3]) << 24);
        return uiPackedPix;
    }

    private void TransposeHost(int[] uiDataIn, int[] uiDataOut, int iWidth, int iHeight) {
        for (int Y = 0; Y < iHeight; Y++) {
            int iBaseIn = Y * iWidth;
            for (int X = 0; X < iWidth; X++) {
                uiDataOut[X * iHeight + Y] = uiDataIn[iBaseIn + X];
            }
        }
    }

    private void RecursiveGaussianRGBAHost(
            int[] uiDataIn, int[] uiDataOut,
            int iWidth, int iHeight,
            float a0, float a1, float a2, float a3,
            float b1, float b2, float coefp, float coefn) {

        final boolean CLAMP_TO_EDGE = false;

        // outer loop over all columns within image
        for (int X = 0; X < iWidth; X++) {
            // start forward filter pass
            float[] xp = new float[] {0, 0, 0, 0};  // previous input
            float[] yp = new float[] {0, 0, 0, 0};  // previous output
            float[] yb = new float[] {0, 0, 0, 0};  // previous output by 2

            if (CLAMP_TO_EDGE) {
                xp = rgbaUintToFloat4(uiDataIn[X]);
                for (int i = 0; i < 4; i++) {
                    yb[i] = xp[i] * coefp;
                    yp[i] = yb[i];
                }
            }

            float[] xc = new float[] {0, 0, 0, 0};
            float[] yc = new float[] {0, 0, 0, 0};
            for (int Y = 0; Y < iHeight; Y++) {
                int iOffset = Y * iWidth + X;
                xc = rgbaUintToFloat4(uiDataIn[iOffset]);
                yc[0] = (a0 * xc[0]) + (a1 * xp[0]) - (b1 * yp[0]) - (b2 * yb[0]);
                yc[1] = (a0 * xc[1]) + (a1 * xp[1]) - (b1 * yp[1]) - (b2 * yb[1]);
                yc[2] = (a0 * xc[2]) + (a1 * xp[2]) - (b1 * yp[2]) - (b2 * yb[2]);
                yc[3] = (a0 * xc[3]) + (a1 * xp[3]) - (b1 * yp[3]) - (b2 * yb[3]);
                uiDataOut[iOffset] = rgbaFloat4ToUint(yc);
                for (int i=0; i<4; i++) {
                    xp[i] = xc[i];
                    yb[i] = yp[i];
                    yp[i] = yc[i];
                }
            }

            // start reverse filter pass: ensures response is symmetrical
            float[] xn = new float[] {0, 0, 0, 0};
            float[] xa = new float[] {0, 0, 0, 0};
            float[] yn = new float[] {0, 0, 0, 0};
            float[] ya = new float[] {0, 0, 0, 0};

            if (CLAMP_TO_EDGE) {
                // reset to last element of column
                xn = rgbaUintToFloat4(uiDataIn[(iHeight - 1) * iWidth + X]);
                for (int i = 0; i < 4; i++) {
                    xa[i] = xn[i];
                    yn[i] = xn[i] * coefn;
                    ya[i] = yn[i];
                }
            }

            float[] fTemp = new float[] {0, 0, 0, 0};
            for (int Y = iHeight - 1; Y > -1; Y--) {
                int iOffset = Y * iWidth + X;
                xc = rgbaUintToFloat4(uiDataIn[iOffset]);
                yc[0] = (a2 * xn[0]) + (a3 * xa[0]) - (b1 * yn[0]) - (b2 * ya[0]);
                yc[1] = (a2 * xn[1]) + (a3 * xa[1]) - (b1 * yn[1]) - (b2 * ya[1]);
                yc[2] = (a2 * xn[2]) + (a3 * xa[2]) - (b1 * yn[2]) - (b2 * ya[2]);
                yc[3] = (a2 * xn[3]) + (a3 * xa[3]) - (b1 * yn[3]) - (b2 * ya[3]);
                for (int i = 0; i < 4; i++) {
                    xa[i] = xn[i];
                    xn[i] = xc[i];
                    ya[i] = yn[i];
                    yn[i] = yc[i];
                }
                fTemp = rgbaUintToFloat4(uiDataOut[iOffset]);
                fTemp[0] += yc[0];
                fTemp[1] += yc[1];
                fTemp[2] += yc[2];
                fTemp[3] += yc[3];
                uiDataOut[iOffset] = rgbaFloat4ToUint(fTemp);
            }
        }

    }
}
