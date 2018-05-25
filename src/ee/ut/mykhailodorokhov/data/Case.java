package ee.ut.mykhailodorokhov.data;

import java.util.List;

public class Case {
    private String name;
    private List<Event> events;

    public Case(String name, List<Event> events) {
        this.name = name;
        this.events = events;
    }

    public String getName() {
        return this.name;
    }

    public List<Event> getEvents() {
        return this.events;
    }

    public String getFullCaseName() {
        String fullCaseName = "";
        for(int i=0; i < this.events.size(); i++) {
            String separator = (i!=0)?"-":"";
            fullCaseName = fullCaseName + separator + this.events.get(i).getName();
        }

        return fullCaseName;
    }

    @Override
    public String toString() {
        return this.getFullCaseName();
    }
}