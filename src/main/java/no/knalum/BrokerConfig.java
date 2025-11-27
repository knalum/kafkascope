package no.knalum;

import com.jgoodies.binding.beans.Model;

public class BrokerConfig extends Model {
    private static BrokerConfig instance;

    private String url;

    private BrokerConfig() {
    }

    public static BrokerConfig getInstance() {
        if (instance == null) {
            System.out.println("Broker Config created");
            instance = new BrokerConfig();
        }
        return instance;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        String old = this.url;
        this.url = url;
        firePropertyChange("url", old, url);
    }
}
