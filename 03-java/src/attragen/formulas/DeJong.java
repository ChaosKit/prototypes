package attragen.formulas;

import java.awt.geom.Point2D;
import static java.lang.Math.*;

/**
 * The Peter de Jong formula
 *
 * @author Rafał Hirsz
 */
public class DeJong extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        newpoint.x = sin(params[0] * point.getY()) - cos(params[1] * point.getX());
        newpoint.y = sin(params[2] * point.getX()) - cos(params[3] * point.getY());

        return newpoint;
    }

    public int parameterCount() { return 4; }
}
