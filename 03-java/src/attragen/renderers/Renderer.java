package attragen.renderers;

/**
 *
 * @author Rafał Hirsz
 */
abstract public class Renderer {
    protected double[][] values;
    protected double step = 0.3;
    protected attragen.core.Generator parent;

    public void setData(double[][] data) {
        values = data;
    }
    public double[][] getData() {
        return values;
    }
    public void setStep(double value) {
        step = value;
    }
    public void setParent(attragen.core.Generator gen) {
        parent = gen;
    }
    public void addPixel(int x, int y) {
        values[x][y] += step;
    }
    public void addPixel(double x, double y) {
        int w = values.length;
        int h = values[0].length;

        int ix = (int)(x*w);
        int iy = (int)(y*h);

        double diffx = (x*w)-ix;
        double diffy = (y*h)-iy;
        double diffx2 = 1 - diffx;
        double diffy2 = 1 - diffy;

        if ((ix>=0) && (ix<w-1) && (iy>=0) && (iy<h-1)) {
            values[ix][iy] += step * diffx2 * diffy2;
            values[ix+1][iy] += step * diffx * diffy2;
            values[ix][iy+1] += step * diffx2 * diffy;
            values[ix+1][iy+1] += step * diffx * diffy;
        }
    }

    abstract public void toFile(String name);
    abstract public void prepare();
    abstract public void render();
    abstract public void setPointColor(int x, int y, java.awt.Color color);
    abstract public void setPointColor(double x, double y, java.awt.Color color);
    abstract public void colorize();
    abstract public void postprocess(int level);
}
