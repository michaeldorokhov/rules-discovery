package ee.ut.mykhailodorokhov.data;

import java.util.Date;
import java.util.Map;

public class Event {
    private String name;
    private Date timestamp;
    private Map<String, String> payload;

    public Event(String name, Map<String, String> payload, Date timestamp)
    {
        this.name = name;
        this.payload = payload;

        this.timestamp = timestamp;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getPayload() {
        return this.payload;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
