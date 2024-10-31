package attragen.formulas;

import java.awt.geom.Point2D;

/**
 * The quartic formula
 *
 * @author Rafał Hirsz
 */
public class Quartic extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        /*
         X = a1 + a2X + a3X2 + a4X3 + a5X4 + a6X3Y + a7X2Y + a8X2Y2 + a9XY + a10XY2 + a11XY3 + a12Y + a13Y2 + a14Y3 + a15Y4
         Y = a16 + a17X + a18X2 + a19X3 + a20X4 + a21X3Y + a22X2Y + a23X2Y2 + a24XY + a25XY2 + a26XY3 + a27Y + a28Y2 + a29Y3 + a30Y4
         */

        double xx = point.getX() * point.getX();
        double yy = point.getY() * point.getY();
        double xy = point.getX() * point.getY();
        double xxx = xx * point.getX();
        double xxy = xx * point.getY();
        double xyy = yy * point.getX();
        double yyy = yy * point.getY();
        double xxxx = xxx * point.getX();
        double xxxy = xxx * point.getY();
        double xxyy = xx * yy;
        double xyyy = yyy * point.getX();
        double yyyy = yyy * point.getY();

        newpoint.x = params[0] + (params[1]*point.getX()) + (params[2]*xx) + (params[3]*xxx) + (params[4]*xxxx) + (params[5]*xxxy) + (params[6]*xxy) + (params[7]*xxyy) + (params[8]*xy) + (params[9]*xyy) + (params[10]*xyyy) + (params[11]*point.getY()) + (params[12]*yy) + (params[13]*yyy) + (params[14]*yyyy);
        newpoint.y = params[15] + (params[16]*point.getX()) + (params[17]*xx) + (params[18]*xxx) + (params[19]*xxxx) + (params[20]*xxxy) + (params[21]*xxy) + (params[22]*xxyy) + (params[23]*xy) + (params[24]*xyy) + (params[25]*xyyy) + (params[26]*point.getY()) + (params[27]*yy) + (params[28]*yyy) + (params[29]*yyyy);

        return newpoint;
    }

    public int parameterCount() { return 30; }
}