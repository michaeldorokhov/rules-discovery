package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OfflineRuleDiscoveryEngine {

    public RuleSet discoverRules(EventLog log) {
        return this.discoverRules(log, 3);
    }

    public RuleSet discoverRules(EventLog log, Integer minimumFrequency) {
        List<Case> cases = log.getCases();
        List<String> uniqueEventNames = log.getUniqueEventNames();

        RuleSet rules = new RuleSet();

        for(Case caseInstance : cases) {

            for(String eventNameA : uniqueEventNames) {
                for(String eventNameB : uniqueEventNames) {
                    if(eventNameA.equals(eventNameB)) continue;

                    List<Integer> indexesA = caseInstance.getEventIndexesList(eventNameA);
                    List<Integer> indexesB = caseInstance.getEventIndexesList(eventNameB);

                    for(Integer indexA : indexesA) {

                        // Response
                        if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> indexA < x)) {
                            Rule newRule = new Rule(RuleEnum.RESPONSE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Chain Response
                        if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> x == indexA + 1)) {
                            Rule newRule = new Rule(RuleEnum.CHAIN_RESPONSE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Alternate Response
                        if (!indexesB.isEmpty() &&
                            indexesB.stream().anyMatch(x -> indexA < x) &&
                            indexesA.stream().noneMatch(x -> indexA < x && x < indexesB.stream().min(Comparator.comparing(Integer::valueOf)).get())) {

                            Rule newRule = new Rule(RuleEnum.ALTERNATE_RESPONSE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);

                        }

                        // TODO: add other rules discovery
                    }
                }
            }
        }

        return new RuleSet(rules.getRulesWithMinimumFrequency(minimumFrequency));
    }

    public List<LabeledFeatureVector> extractLabeledFeatureVectors(EventLog log, RuleSet rules) {

        List<LabeledFeatureVector> labeledFeatureVectors = new ArrayList<>();

        for (Case caseInstance : log.getCases()) {
            for (Rule rule : rules.getRules()) {

                List<Integer> indexesA = caseInstance.getEventIndexesList(rule.getEventA());
                List<Integer> indexesB = caseInstance.getEventIndexesList(rule.getEventB());

                for (Integer indexA : indexesA) {

                    switch(rule.getRuleType()){
                        case RESPONSE:
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));

                            break;
                        case CHAIN_RESPONSE:
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));
                            break;
                        case ALTERNATE_RESPONSE:
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> indexA < x)
                                    && indexesA.stream().noneMatch(x -> indexA < x && x < indexesB.stream().min(Comparator.comparing(Integer::valueOf)).get()))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> indexA < x)
                                    ||
                                    (indexesB.stream().anyMatch(x -> indexA < x)
                                    && indexesA.stream().anyMatch(x -> indexA < x && x < indexesB.stream().min(Comparator.comparing(Integer::valueOf)).get()) ))
                                    labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));
                            break;

                        // TODO: add processing for other rules
                    }
                }
            }
        }
        return labeledFeatureVectors;
    }
}
