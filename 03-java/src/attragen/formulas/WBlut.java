package attragen.formulas;

import java.awt.geom.Point2D;

/**
 *
 * @author Rafał Hirsz
 */
public class WBlut extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();
        double p0 = (params[0]*0.25)+0.5;

        newpoint.x = p0 * (Math.sin(params[1] * point.y) + params[2] * Math.cos(params[1]*point.x)) + (1-p0)*(point.y + params[3]*(point.x/Math.abs(point.x))*Math.sqrt(Math.abs(params[4]*point.x - params[5])));
        newpoint.y = p0 * (Math.sin(params[6] * point.x) + params[7] * Math.cos(params[6]*point.y)) + (1-p0)*(params[8]-point.x);

        return newpoint;
    }

    public int parameterCount() { return 9; }
}
