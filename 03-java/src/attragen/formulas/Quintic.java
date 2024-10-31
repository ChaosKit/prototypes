package attragen.formulas;

import java.awt.geom.Point2D;

/**
 * The quintic formula
 *
 * @author Rafał Hirsz
 */
public class Quintic extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        /*
         X = a1 + a2X + a3X2 + a4X3 + a5X4 + a6X5 + a7X4Y + a8X3Y + a9X3Y2 + a10X2Y + a11X2Y2 + a12X2Y3 + a13XY + a14XY2 + a15XY3 + a16XY4 + a17Y + a18Y2 + a19Y3 + a20Y4 + a21Y5
         Y = a22 + a23X + a24X2 + a25X3 + a26X4 + a27X5 + a28X4Y + a29X3Y + a30X3Y2 + a31X2Y + a32X2Y2 + a33X2Y3 + a34XY + a35XY2 + a36XY3 + a37XY4 + a38Y + a39Y2 + a40Y3 + a41Y4 + a42Y5
         */

        double xx = point.getX() * point.getX();  // The quadratic part
        double yy = point.getY() * point.getY();
        double xy = point.getX() * point.getY();
        double xxx = xx * point.getX();           // The cubic part
        double xxy = xx * point.getY();
        double xyy = yy * point.getX();
        double yyy = yy * point.getY();
        double xxxx = xxx * point.getX();         // The quartic part
        double xxxy = xxx * point.getY();
        double xxyy = xx * yy;
        double xyyy = yyy * point.getX();
        double yyyy = yyy * point.getY();
        double xxxxx = xxxx * point.getX();       // The quintic part ^^
        double xxxxy = xxxx * point.getY();
        double xxxyy = xxx * yy;
        double xxyyy = xx * xxx;
        double xyyyy = yyyy * point.getX();
        double yyyyy = yyyy * point.getY();

        newpoint.x =  params[0] +
                     (params[1]*point.getX()) +
                     (params[2]*xx) +
                     (params[3]*xxx) +
                     (params[4]*xxxx) +
                     (params[5]*xxxxx) +
                     (params[6]*xxxxy) +
                     (params[7]*xxxy) +
                     (params[8]*xxxyy) +
                     (params[9]*xxy) +
                     (params[10]*xxyy) +
                     (params[11]*xxyyy) +
                     (params[12]*xy) +
                     (params[13]*xyy) +
                     (params[14]*xyyy) +
                     (params[15]*xyyyy) + 
                     (params[16]*point.getY()) + 
                     (params[17]*yy) + 
                     (params[18]*yyy) + 
                     (params[19]*yyyy) + 
                     (params[20]*yyyyy);

        newpoint.y =  params[21] +
                     (params[22]*point.getX()) +
                     (params[23]*xx) +
                     (params[24]*xxx) +
                     (params[25]*xxxx) +
                     (params[26]*xxxxx) +
                     (params[27]*xxxxy) +
                     (params[28]*xxxy) +
                     (params[29]*xxxyy) +
                     (params[30]*xxy) +
                     (params[31]*xxyy) +
                     (params[32]*xxyyy) +
                     (params[33]*xy) +
                     (params[34]*xyy) +
                     (params[35]*xyyy) +
                     (params[36]*xyyyy) +
                     (params[37]*point.getY()) +
                     (params[38]*yy) +
                     (params[39]*yyy) +
                     (params[40]*yyyy) +
                     (params[41]*yyyyy);

        return newpoint;
    }

    public int parameterCount() { return 42; } // OMG
}