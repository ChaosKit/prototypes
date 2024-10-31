package attragen.formulas;

import java.awt.geom.Point2D;
import static java.lang.Math.*;

/**
 * The modified Peter de Jong formula
 *
 * @author Rafał Hirsz
 */
public class DeJongMod extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        newpoint.x = params[0] * sin(params[1] * point.getY()) - cos(params[2] * point.getX());
        newpoint.y = params[3] * sin(params[1] * point.getX()) - cos(params[2] * point.getY());

        return newpoint;
    }

    public int parameterCount() { return 4; }
}
