package attragen.formulas;

import java.awt.geom.Point2D;
import java.util.Random;

/**
 *
 * @author Rafał Hirsz
 */
public class Sierpinski extends Formula {
    public Point2D.Double calculatePoint(Point2D.Double point) {
        Point2D.Double newpoint = new Point2D.Double();

        Random rnd = new Random();
        switch (rnd.nextInt(3)) {
            case 0:
                newpoint.x = point.x / 2;
                newpoint.y = point.y / 2;
                break;
            case 1:
                newpoint.x = (point.x + 1) / 2;
                newpoint.y = point.y / 2;
                break;
            case 2:
                newpoint.x = point.x / 2;
                newpoint.y = (point.y + 1) / 2;
                break;
        }

        return newpoint;
    }

    public int parameterCount() { return 0; }
}
