package hirsz.util;

import com.nativelibs4java.opencl.JavaCL;

/**
 *
 * @author Rafał Hirsz
 */
public class OpenCL {
    public static boolean detect() {
        try {
            JavaCL.listPlatforms();
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }
}
