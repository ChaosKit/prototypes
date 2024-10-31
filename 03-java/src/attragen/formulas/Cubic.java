package attragen.formulas;

import java.awt.geom.Point2D;

/**
 * The cubic formula
 *
 * @author Rafał Hirsz
 */
public class Cubic extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        /*
         * Xn+1 = a1 + a2Xn + a3Xn2 + a4Xn3 + a5Xn2Yn+ a6XnYn + a7XnYn2 + a8Yn + a9Yn2 + a10Yn3
            Yn+1 = a11 + a12Xn + a13Xn2 + a14Xn3 + a15Xn2Yn+ a16XnYn + a17XnYn2 + a18Yn + a19Yn2 + a20Yn3
         */

        double xx = point.getX() * point.getX();
        double yy = point.getY() * point.getY();
        double xy = point.getX() * point.getY();
        double xxx = xx * point.getX();
        double xxy = xx * point.getY();
        double xyy = yy * point.getX();
        double yyy = yy * point.getY();

        newpoint.x = params[0] + (params[1]*point.getX()) + (params[2]*xx) + (params[3]*xxx) + (params[4]*xxy) + (params[5]*xy) + (params[6]*xyy) + (params[7]*point.getY()) + (params[8]*yy) + (params[9]*yyy);
        newpoint.y = params[10] + (params[11]*point.getX()) + (params[12]*xx) + (params[13]*xxx) + (params[14]*xxy) + (params[15]*xy) + (params[16]*xyy) + (params[17]*point.getY()) + (params[18]*yy) + (params[19]*yyy);

        return newpoint;
    }

    public int parameterCount() { return 20; }
}
