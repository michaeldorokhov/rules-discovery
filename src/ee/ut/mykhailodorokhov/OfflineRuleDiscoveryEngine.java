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

                    List<Integer> indexesA = caseInstance.getEventIndexesList(eventNameA);
                    List<Integer> indexesB = caseInstance.getEventIndexesList(eventNameB);

                    for(Integer indexA : indexesA) {

                        // Response
                        if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> x > indexA)) {
                            Rule newRule = new Rule(RuleEnum.RESPONSE, eventNameA, eventNameB);

                            if (rules.contains(newRule)) rules.get(rules.indexOf(newRule)).incrementFrequency();
                            else rules.add(newRule);
                        }

                        // Chain Response
                        if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> x == indexA + 1)) {
                            Rule newRule = new Rule(RuleEnum.CHAIN_RESPONSE, eventNameA, eventNameB);

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

    public List<LabeledFeatureVector> extractLabeledFeatureVectors(EventLog log, List<Rule> rules) {

        List<LabeledFeatureVector> labeledFeatureVectors = new ArrayList<>();

        for (Case caseInstance : log.getCases()) {
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

                    switch(rule.getRuleType()){
                        case RESPONSE:
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> x > indexA))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> x > indexA))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));

                            break;
                        case CHAIN_RESPONSE:
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));
                            break;
                    }

                    // TODO: add processing for other rules
                }
            }
        }
        return labeledFeatureVectors;
    }
}
