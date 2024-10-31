package attragen.formulas;

import java.awt.geom.Point2D;
import static java.lang.Math.*;

/**
 * The Clifford formula
 *
 * @author Rafał Hirsz
 */
public class Clifford extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        /*
         * xn+1 = sin(a yn) + c cos(a xn)
           yn+1 = sin(b xn) + d cos(b yn)
         */

        newpoint.x = sin(params[0] * point.getY()) + params[2] * cos(params[0] * point.getX());
        newpoint.y = sin(params[1] * point.getX()) + params[3] * cos(params[1] * point.getY());

        return newpoint;
    }

    public int parameterCount() { return 4; }
}