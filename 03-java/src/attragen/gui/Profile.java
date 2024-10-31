package attragen.gui;

import java.io.*;
import java.util.LinkedList;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * Represents a profile
 * @author Rafał Hirsz
 */
public class Profile {
    private String name;

    private int resX = 512;
    private int resY = 512;
    private int quality = 20;
    private int composite = 0;

    public Profile(String name) {
        this.name = name;
    }

    public void setResolution(int x, int y) {
        resX = x;
        resY = y;
    }
    public void setQuality(int quality) {
        this.quality = quality;
    }
    public void setCompositeLevel(int level) {
        int minRange = 0;
        int maxRange = 4;

        composite = Math.min(Math.max(minRange, level), maxRange);
    }

    public Dimension getResolution() {
        return new Dimension(resX, resY);
    }
    public int getQuality() {
        return quality;
    }
    public int getCompositeLevel() {
        return composite;
    }

    /**
     * Loads the profile from its file
     *
     * @throws FileNotFoundException
     */
    public void load() throws IOException {
        // Check if file exists
        File f = new File(getProfilePath() + File.separator + name + ".yml");
        if (!f.exists()) {
            throw new FileNotFoundException("Plik profilu nie istnieje.");
        }

        // Parse the file
        FileInputStream stream = new FileInputStream(f);
        Yaml yaml = new Yaml();
        Map<String, Integer> profile = (Map) yaml.load(stream);
        stream.close();

        // Set the properties
        setResolution(profile.get("resX"), profile.get("resY"));
        setQuality(profile.get("quality"));
        setCompositeLevel(profile.get("composite"));
    }

    /**
     * Saves the profile to its file
     * 
     * @throws IOException
     */
    public void save() throws IOException {
        File f = new File(getProfilePath() + File.separator + name + ".yml");

        // Get the properties
        Map<String, Integer> profile = new HashMap<String, Integer>();
        profile.put("resX", resX);
        profile.put("resY", resY);
        profile.put("quality", quality);
        profile.put("composite", composite);

        // Write the file
        FileWriter writer = new FileWriter(f);
        Yaml yaml = new Yaml();
        yaml.dump(profile, writer);
        writer.close();
    }

    public static boolean delete(String name) {
        File f = new File(getProfilePath() + File.separator + name + ".yml");

        if (!f.exists()) return false;
        
        return f.delete();
    }

    /**
     * Gets the profile directory
     * @return The path
     */
    public static String getProfilePath() {
        return System.getProperty("user.home") + File.separator + ".attragenprofiles";
    }

    /**
     * Gets the list of profiles
     * @return An array of profile names
     */
    public static String[] getProfiles() throws IOException {
        // Check the validity of the directory
        String profiledir = Profile.getProfilePath();
        File f = new File(profiledir);
        if (!f.exists()) {
            if (!f.mkdir()) {
                throw new IOException("Nie można utworzyć folderu profili.");
            }

            // Create a profile
            Profile newprof = new Profile("Default");
            newprof.save();
        }

        // Get the profile list
        LinkedList result = new LinkedList();

        String[] profilefiles = f.list();
        for (String file: profilefiles) {
            if (file.endsWith(".yml")) {
                String profile = file.substring(0, file.length()-4);
                result.add(profile);
            }
        }

        return (String[])result.toArray(new String[0]);
    }
}
