package attragen.gui;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

/**
 *
 * @author Rafal
 */
public class DataHandler {
    public static void save(File f, String formula, double[] params, Point2D.Double start) throws IOException {
        BufferedWriter buf = new BufferedWriter(new FileWriter(f));

        buf.write(formula); buf.newLine();

        buf.append(Double.toString(start.x)); buf.newLine();
        buf.append(Double.toString(start.y)); buf.newLine();

        for (double par: params) {
            buf.append(Double.toString(par));
            buf.newLine();
        }

        buf.close();
    }

    public static Map<String, Object> load(File f) throws IOException {
        BufferedReader buf = new BufferedReader(new FileReader(f));
        Map<String, Object> result = new HashMap<String, Object>();
        String line;

        // Read the formula
        if ((line = buf.readLine()) != null) {
            result.put("formula", line);
        }

        // Read the start point
        Point2D.Double point = new Point2D.Double();
        String x = buf.readLine();
        String y = buf.readLine();
        if ((x != null) && (y != null)) {
            point.setLocation(Double.parseDouble(x), Double.parseDouble(y));
        }
        result.put("start", point);

        // Read the params
        List<Double> params = new LinkedList();
        while ((line = buf.readLine()) != null) {
            params.add(Double.valueOf(line));
        }
        result.put("params", params.toArray(new Double[0]));
        buf.close();
        
        return result;
    }
}
