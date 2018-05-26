package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.*;

import java.util.ArrayList;
import java.util.List;

public class DataAwareActivationViolationExtractor {
    public List<Snapshot> extract(EventLog log, List<Rule> rules) {

        List<Snapshot> snapshots = new ArrayList<>();

        for(Case caseInstance : log.getCases()) {
            for (Rule rule : rules) {

                List<Integer> indexesA = new ArrayList<>();
                caseInstance.getEvents().stream().
                        filter(x -> x.getName().equals(rule.getEventA())).
                        forEachOrdered(x -> indexesA.add(caseInstance.getEvents().indexOf(x)));

                List<Integer> indexesB = new ArrayList<>();
                caseInstance.getEvents().stream().
                        filter(x -> x.getName().equals(rule.getEventB())).
                        forEachOrdered(x -> indexesB.add(caseInstance.getEvents().indexOf(x)));

                for (Integer indexA : indexesA) {

                    //
                    // Response
                    //
                    if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> x > indexA))
                        snapshots.add(new Snapshot(caseInstance.getEvents().get(indexA).getPayload(), false));

                    if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> x > indexA))
                        snapshots.add(new Snapshot(caseInstance.getEvents().get(indexA).getPayload(), true));

                    // TODO: add processing for other rules
                }
            }
        }
        return snapshots;
    }
}
