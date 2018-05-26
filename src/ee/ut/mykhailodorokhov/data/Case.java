package ee.ut.mykhailodorokhov.data;

import java.util.ArrayList;
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

    public List<Integer> getEventIndexesList(String eventName) {
        List<Integer> indexes = new ArrayList<>();

        // NOTE: Faster and simpler version
        for (Event event : this.events) {
            if(event.getName().equals(eventName)) indexes.add(this.events.indexOf(event));
        }

        // NOTE: Fancier but slower version with stream
        //events.stream().filter(x -> x.getName().equals(eventName)).forEachOrdered(x -> indexes.add(events.indexOf(x)));

        return indexes;
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