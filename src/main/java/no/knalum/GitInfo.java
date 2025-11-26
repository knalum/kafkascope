package no.knalum;

import java.io.InputStream;
import java.util.Properties;

public class GitInfo {
    private static Properties props = new Properties();

    static {
        try (InputStream in = GitInfo.class.getResourceAsStream("/version.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getGitTag() {
        return props.getProperty("git.tag", "unknown");
    }

    public static String getCommit() {
        return props.getProperty("git.commit", "unknown");
    }

    public static String getBranch() {
        return props.getProperty("git.branch", "unknown");
    }
}
