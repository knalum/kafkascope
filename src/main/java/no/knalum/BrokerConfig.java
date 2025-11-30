package no.knalum;

import com.jgoodies.binding.beans.Model;

public class BrokerConfig extends Model {
    private static BrokerConfig instance;

    private String url;
    private String schemaRegistryUrl;

    private BrokerConfig() {
    }

    public static BrokerConfig getInstance() {
        if (instance == null) {
            instance = new BrokerConfig();
        }
        return instance;
    }


    public String getBrokerUrl() {
        return url;
    }

    public void setUrl(String url) {
        String old = this.url;
        this.url = url;
        firePropertyChange("url", old, url);
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
        String old = this.schemaRegistryUrl;
        this.schemaRegistryUrl = schemaRegistryUrl;
        firePropertyChange("schemaRegistryUrl", old, schemaRegistryUrl);
    }
}
