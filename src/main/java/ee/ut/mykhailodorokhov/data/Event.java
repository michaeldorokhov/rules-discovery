package ee.ut.mykhailodorokhov.data;

import java.util.Date;
import java.util.Map;

public class Event {
    private Integer id;
    private String name;
    private Date timestamp;
    private Map<String, String> payload;

    public Event(Integer id, String name, Map<String, String> payload, Date timestamp)
    {
        this.id = id;
        this.name = name;
        this.payload = payload;

        this.timestamp = timestamp;
    }

    public Integer getId() { return id; }

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
