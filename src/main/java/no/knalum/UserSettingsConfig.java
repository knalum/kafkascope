package no.knalum;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class UserSettingsConfig {
    public static Dimension getMessageModalDimensions() {
        Properties props = loadSettings();
        int width = Integer.parseInt(props.getProperty("message.table.value.dialog.width", "400"));
        int height = Integer.parseInt(props.getProperty("message.table.value.dialog.height", "300"));
        return new Dimension(width, height);
    }

    public static void saveSettings(Properties props, String appName) {
        Path dir = getAppDataDir();
        Path file = dir.resolve("settings.properties");

        try (OutputStream out = Files.newOutputStream(file)) {
            props.store(out, "MyApp Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties loadSettings() {
        Path dir = getAppDataDir();
        Path file = dir.resolve("settings.properties");

        Properties props = new Properties();

        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return props;
    }


    public static Path getAppDataDir() {
        String appName = "kafkascope";
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        Path path;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            path = Paths.get(appData, appName);
        } else if (os.contains("mac")) {
            path = Paths.get(userHome, "Library", "Application Support", appName);
        } else {
            // Linux or Unix
            path = Paths.get(userHome, ".config", appName);
        }

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Could not create app data directory", e);
        }

        return path;
    }

    public static void setMessageModalDimensions(Dimension size) {
        Properties props = loadSettings();
        props.setProperty("message.table.value.dialog.width", Integer.toString(size.width));
        props.setProperty("message.table.value.dialog.height", Integer.toString(size.height));
        saveSettings(props, "kafkascope");
    }
}
