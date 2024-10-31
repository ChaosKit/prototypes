package hirsz.util;

/**
 *
 * @author Rafał Hirsz
 */
public class MathUtils {
    public static int roundUp(int groupSize, int globalSize) {
        int r = globalSize % groupSize;

        if (r == 0) return globalSize;
        return globalSize + groupSize - r;
    }
}
