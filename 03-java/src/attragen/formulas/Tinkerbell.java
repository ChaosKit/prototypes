package attragen.formulas;

import java.awt.geom.Point2D;

/**
 * The Tinkerbell formula
 *
 * @author Rafał Hirsz
 */
public class Tinkerbell extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        double xx = point.getX() * point.getX();
        double yy = point.getY() * point.getY();
        double xy = point.getX() * point.getY();

        newpoint.x = xx - yy + params[0]*point.getX() + params[1]*point.getY();
        newpoint.y = 2*xy + params[2]*point.getX() + params[3]*point.getY();

        return newpoint;
    }

    public int parameterCount() { return 4; }
}
