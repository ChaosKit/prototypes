package attragen.formulas;

import java.awt.geom.Point2D;
import java.util.jar.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * An abstract class for defining the attractor formula
 *
 * @author Rafał Hirsz
 */
abstract public class Formula {
    protected double[] params;

    /**
     * Lists all defined formulas
     *
     * @return An array containing names of formulas
     */
    public static String[] listFormulas() throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname.
        //There may be more than one if a package is split over multiple jars/paths
        List<String> classes = new ArrayList<String>();
        ArrayList<File> directories = new ArrayList<File>();
        String pckgname = "attragen.formulas";

        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            // Ask for all resources for the path
            Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
            while (resources.hasMoreElements()) {
                URL res = resources.nextElement();
                if (res.getProtocol().equalsIgnoreCase("jar")){
                    JarURLConnection conn = (JarURLConnection) res.openConnection();
                    JarFile jar = conn.getJarFile();
                    for (JarEntry e:Collections.list(jar.entries())){

                        if (e.getName().startsWith(pckgname.replace('.', '/'))
                            && e.getName().endsWith(".class") && !e.getName().contains("$")){
                            String className =
                                    e.getName().replace("/",".").substring(pckgname.length() + 1,e.getName().length() - 6);
                            classes.add(className);
                        }
                    }
                }else
                    directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be " +
                    "a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be " +
                    "a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying " +
                    "to get all resources for " + pckgname);
        }

        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (String file : files) {
                    // we are only interested in .class files
                    if (file.endsWith(".class")) {
                        // removes the .class extension
                        classes.add(file.substring(0, file.length() - 6));
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " (" + directory.getPath() +
                                    ") does not appear to be a valid package");
            }
        }

        classes.remove("Formula");
        
        return (String[])classes.toArray(new String[0]);
    }

    /**
     * Sets the formula parameters
     * 
     * @param original An array containing the parameters
     */
    public void setParameters(double[] original) {
        params = original;
    }

    /**
     * Gets the formula parameters
     *
     * @return An array containing the parameters
     */
    public double[] getParameters() {
        return params;
    }

    /**
     * Calculate the next point in a system
     *
     * @param point The point based on which will the next be calculated
     * @return A new point
     */
    abstract public Point2D.Double calculatePoint(Point2D.Double point);

    /**
     * Gets the parameter count of a formula.
     *
     * @return The parameter count
     */
    abstract public int parameterCount();
}
