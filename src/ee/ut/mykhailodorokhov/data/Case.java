package ee.ut.mykhailodorokhov.data;

import java.util.ArrayList;
import java.util.List;

public class Case {
    private String id;
    private List<Event> events;

    public Case(String caseID, List<Event> events) {
        this.id = caseID;
        this.events = events;
    }

    public String getId() { return this.id; }

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