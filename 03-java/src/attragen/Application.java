package attragen;

import attragen.gui.MainForm;
import attragen.ubergui.TheForm;
import attragen.test.Test;
import javax.swing.UIManager;

/**
 *
 * @author evol
 */
public class Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (javax.swing.UnsupportedLookAndFeelException e) {
        }

        String gui = "default";

        if (args.length > 0) {
            if (args[0].equals("--gui")) {
                gui = args[1];
            }
        }

        if (gui.equals("uber")) {
            TheForm form = new TheForm();
            form.setVisible(true);
        } else if (gui.equals("test")) {
            Test form = new Test();
            form.setVisible(true);
        } else {
            MainForm mainform = new MainForm();
            mainform.setVisible(true);
        }
    }

    /**
     * Gets the application path
     * @return The path
     */
    public static String getPath() {
        /*String path;

        try {
            path = URLDecoder.decode(ProfileForm.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");

            String os = System.getProperty("os.name");
            if (os.toLowerCase().contains("windows")) {
                path = path.substring(1);
            }
        } catch (UnsupportedEncodingException ex) {
            path = System.getProperty("user.dir");
        }*/

        return System.getProperty("user.dir");
    }

}
