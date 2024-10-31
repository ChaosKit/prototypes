package attragen.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Rafał Hirsz
 */
public class ImageFilter extends FileFilter {
    public String getDescription() {
        return "PNG (.png)";
    }

    public boolean accept(File f) {
        if (f.isDirectory()) return true;

        String extension = getExtension(f);
        if (extension != null) {
            return extension.equals("png");
        }

        return false;
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
