package attragen.formulas;

import java.awt.geom.Point2D;
import static java.lang.Math.*;

/**
 * The trigonometric formula
 *
 * @author Rafał Hirsz
 */
public class Trigonometric extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        newpoint.x = params[0] * sin(params[1] * point.getY()) + params[2] * cos(params[3] * point.getX());
        newpoint.y = params[4] * sin(params[5] * point.getX()) + params[6] * cos(params[7] * point.getY());

        return newpoint;
    }

    public int parameterCount() { return 8; }
}
