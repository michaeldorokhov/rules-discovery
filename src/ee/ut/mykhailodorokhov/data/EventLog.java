package ee.ut.mykhailodorokhov.data;

import java.util.ArrayList;
import java.util.List;

public class EventLog {
    private List<Case> cases;

    public EventLog(List<Case> cases) {
        this.cases = cases;
    }

    public List<Case> getCases() {
        return this.cases;
    }

    public List<String> getUniqueEventNames() {
        List<String> names = new ArrayList<String>();

        for(Case caseInstance : this.cases) {
            for(Event event : caseInstance.getEvents()) {
                if(!names.contains(event.getName())) {
                    names.add(event.getName());
                }
            }
        }

        return names;
    }
}
