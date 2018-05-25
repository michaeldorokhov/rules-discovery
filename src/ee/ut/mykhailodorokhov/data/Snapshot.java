package ee.ut.mykhailodorokhov.data;

import java.util.Map;

public class Snapshot {
    private Map<String, String> attributes;
    private boolean outcome;

    public Snapshot(Map<String, String> attributes, boolean outcome) {
        this.attributes = attributes;
        this.outcome = outcome;
    }

    public Map<String, String> getAttributes() { return attributes; }

    public boolean isActivation() { return outcome; }
}
