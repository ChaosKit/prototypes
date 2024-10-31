package attragen.formulas;

import java.awt.geom.Point2D;

/**
 * The quadratic formula
 *
 * @author Rafał Hirsz
 */
public class Quadratic extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        double xx = point.getX() * point.getX();
        double yy = point.getY() * point.getY();
        double xy = point.getX() * point.getY();

        newpoint.x = params[0] + (params[1]*point.getX()) + (params[2]*xx) + (params[3]*xy) + (params[4]*point.getY()) + (params[5]*yy);
        newpoint.y = params[6] + (params[7]*point.getX()) + (params[8]*xx) + (params[9]*xy) + (params[10]*point.getY()) + (params[11]*yy);

        return newpoint;
    }

    public int parameterCount() { return 12; }
}
