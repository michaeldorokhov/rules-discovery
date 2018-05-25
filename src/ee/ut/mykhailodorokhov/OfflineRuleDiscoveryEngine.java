package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.*;

import java.util.ArrayList;
import java.util.List;

public class OfflineRuleDiscoveryEngine {

    public List<Rule> discoverRules(EventLog log) {
        List<Case> cases = log.getCases();
        List<String> uniqueEventNames = log.getUniqueEventNames();

        List<Rule> rules = new ArrayList<>();

        for(Case caseInstance : cases) {

            for(String eventNameA : uniqueEventNames) {
                for(String eventNameB : uniqueEventNames) {
                    if(eventNameA.equals(eventNameB)) continue;

                    List<Integer> indexesA = new ArrayList<>();
                    caseInstance.getEvents().stream().
                            filter(x -> x.getName().equals(eventNameA)).
                            forEachOrdered(x -> indexesA.add(caseInstance.getEvents().indexOf(x)));

                    List<Integer> indexesB = new ArrayList<>();
                    caseInstance.getEvents().stream().
                            filter(x -> x.getName().equals(eventNameB)).
                            forEachOrdered(x -> indexesB.add(caseInstance.getEvents().indexOf(x)));

                    for(Integer indexA : indexesA)
                        for (Integer indexB : indexesB) {

                            //
                            // Response
                            //
                            if (indexA < indexB) {
                                Rule newRule = new Rule(RuleEnum.RESPONSE,
                                               caseInstance.getEvents().get(indexA).getName(),
                                               caseInstance.getEvents().get(indexB).getName());

                                if (rules.contains(newRule)) rules.get(rules.indexOf(newRule)).incrementFrequency();
                                else rules.add(newRule);
                            }

                            // TODO: add other rules discovery
                        }
                }
            }

        }

        return rules;
    }
}
