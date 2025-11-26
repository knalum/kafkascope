package no.knalum;

import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class ConfigSaver {
    public static void saveConfig() {
        BrokerConfig config = BrokerConfig.getInstance();
        Gson gson = new Gson();
        String json = gson.toJson(config);
        try (FileWriter writer = new FileWriter("broker-config.json")) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadConfig() {
        try (FileReader reader = new FileReader("broker-config.json")) {
            Gson gson = new Gson();
            BrokerConfig loaded = gson.fromJson(reader, BrokerConfig.class);
            if (loaded != null && loaded.getUrl() != null) {
                BrokerConfig.getInstance().setUrl(loaded.getUrl());
            }
        } catch (FileNotFoundException e) {
            // Config file does not exist, ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
