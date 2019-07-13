package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.*;
import ee.ut.mykhailodorokhov.helpers.ListHelper;
import ee.ut.mykhailodorokhov.helpers.WekaHelper;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;

public class OfflineRuleDiscoveryEngine {

    public RuleSet discoverConditionAwareRules(EventLog log) throws Exception{
        // Discover rules
        RuleSet rules = this.discoverRules(log, 5);

        // Sort rules by frequency for convinience
        rules = new RuleSet(rules.getRulesSortedByFrequency());

        // Extract labeled feature vectors
        List<LabeledFeatureVector> data = this.extractLabeledFeatureVectors(log, rules);

        // Discover conditions for the rules
        this.extractConditions(rules, data);

        return rules;
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

                    // Rules that are activated by A
                    for(Integer indexA : indexesA) {

                        // Responded Existense
                        if (indexesB.size() > 0) {
                            rules.addRuleOccurence(new Rule(RuleType.RESPONDED_EXISTENCE, eventNameA, eventNameB));
                        }

                        // Not Responded Existence
                        if (indexesB.size() == 0) {
                            rules.addRuleOccurence(new Rule(RuleType.NOT_RESPONDED_EXISTENCE, eventNameA, eventNameB));
                        }

                        // Response
                        if (indexesB.stream().anyMatch(x -> indexA < x)) {
                            Rule newRule = new Rule(RuleType.RESPONSE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Not Response
                        if (indexesB.stream().noneMatch(x -> indexA < x)) {
                            Rule newRule = new Rule(RuleType.NOT_RESPONSE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Chain Response
                        if (indexesB.stream().anyMatch(x -> x == indexA + 1)) {
                            Rule newRule = new Rule(RuleType.CHAIN_RESPONSE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Not Chain Response
                        if (indexesB.stream().noneMatch(x -> x == indexA + 1)) {
                            Rule newRule = new Rule(RuleType.NOT_CHAIN_RESPONSE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Alternate Response
                        if (indexesB.stream().anyMatch(x -> indexA < x) &&
                            indexesA.stream().noneMatch(x -> indexA < x && x < ListHelper.minInteger(indexesB))) {

                            Rule newRule = new Rule(RuleType.ALTERNATE_RESPONSE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }
                    }

                    // Rules that are activated by B
                    for(Integer indexB : indexesB) {

                        // Precedence
                        if (indexesA.stream().anyMatch(x -> indexB > x)) {
                            Rule newRule = new Rule(RuleType.PRECEDENCE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Not Precedence
                        if (indexesA.stream().noneMatch(x -> indexB > x)) {
                            Rule newRule = new Rule(RuleType.NOT_PRECEDENCE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Chain Precedence
                        if (indexesA.stream().anyMatch(x -> x == indexB - 1)) {
                            Rule newRule = new Rule(RuleType.CHAIN_PRECEDENCE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Not Chain Prevedence
                        if (indexesA.stream().noneMatch(x -> x == indexB - 1)) {
                            Rule newRule = new Rule(RuleType.NOT_CHAIN_PRECEDENCE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // Alternate Precedence
                        if (indexesA.stream().anyMatch(x -> indexB > x) &&
                            indexesB.stream().noneMatch(x -> indexB > x && x > ListHelper.maxInteger(indexesA))) {

                            Rule newRule = new Rule(RuleType.ALTERNATE_PRECEDENCE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }
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

                // Rules that are activated by A
                for (Integer indexA : indexesA) {

                    switch(rule.getRuleType()){
                        case RESPONDED_EXISTENCE:
                            if (!indexesB.isEmpty())
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty())
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));

                            break;
                        case NOT_RESPONDED_EXISTENCE:
                            if (indexesB.isEmpty())
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (!indexesB.isEmpty())
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));

                            break;
                        case RESPONSE:
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));

                            break;
                        case NOT_RESPONSE:
                            if (!indexesB.isEmpty() && indexesB.stream().noneMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));

                            break;
                        case CHAIN_RESPONSE:
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));

                            break;
                        case NOT_CHAIN_RESPONSE:
                            if (indexesB.stream().noneMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));

                            break;
                        case ALTERNATE_RESPONSE:
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> indexA < x) &&
                                    indexesA.stream().noneMatch(x -> indexA < x && x < ListHelper.minInteger(indexesB)))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> indexA < x) ||
                                    (indexesB.stream().anyMatch(x -> indexA < x) &&
                                    indexesA.stream().anyMatch(x -> indexA < x && x < ListHelper.minInteger(indexesB))))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));
                            break;
                    }
                }

                // Rules that are activated by B
                for(Integer indexB : indexesB) {
                    switch(rule.getRuleType()) {
                        case PRECEDENCE:
                            if (indexesA.stream().anyMatch(x -> indexB > x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), true));

                            if (indexesA.stream().noneMatch(x -> indexB > x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), false));

                            break;
                        case NOT_PRECEDENCE:
                            if (indexesA.stream().noneMatch(x -> indexB > x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), true));

                            if (indexesA.stream().anyMatch(x -> indexB > x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), false));

                            break;
                        case CHAIN_PRECEDENCE:
                            if (indexesA.stream().anyMatch(x -> x == indexB - 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), true));

                            if (indexesA.stream().noneMatch(x -> x == indexB - 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), false));

                            break;
                        case NOT_CHAIN_PRECEDENCE:
                            if (indexesA.stream().noneMatch(x -> x == indexB - 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), true));

                            if (indexesA.stream().anyMatch(x -> x == indexB - 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), false));

                            break;
                        case ALTERNATE_PRECEDENCE:
                            if (indexesA.stream().anyMatch(x -> indexB > x) &&
                                    indexesB.stream().noneMatch(x -> indexB > x && x > ListHelper.maxInteger(indexesA)))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), true));

                            // TODO: Alternate precedence violation
                            if (true)
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexB).getPayload(), true));

                            break;
                    }
                }
            }
        }

        return labeledFeatureVectors;
    }

    public void extractConditions(RuleSet rules, List<LabeledFeatureVector> labeledFeatureVectors) throws Exception {
        for(Rule rule : rules.getRules()) {

            List<LabeledFeatureVector> relevantFeatureVectors =
                    labeledFeatureVectors.stream().filter(x -> x.getRule().equals(rule)).collect(Collectors.toList());

            Instances dataSet = null;

            try {
                dataSet = WekaHelper.toWekaDataSet(relevantFeatureVectors);
            }
            catch (Exception ex) {
                System.out.println("Unary class. Skipped.");
                continue;
            }

            int classIndex = relevantFeatureVectors.get(0).getAttributes().size();
            dataSet.setClassIndex(classIndex);

            dataSet.enumerateAttributes();

            StringBuilder options = new StringBuilder();

            // Tree parameters
            //options.append("-U");
            //options.append("-M 7");

            J48 tree = new J48();
            tree.setOptions(options.toString().split(" "));
            tree.buildClassifier(dataSet);

            List<Condition> conditions = new ArrayList<>();

            List<TreeBranch> treeBranches = WekaHelper.parseJ48Tree(tree.toString());
            treeBranches.stream().
                    filter(x -> x.getSupport() > 0.1 && x.isTrue()).
                    forEach(x -> conditions.addAll(x.getConditions()));

            List<Condition> prettyConditions = Conditions.optimizeConditions(conditions);

            // adding discovered conditions to the rule
            rule.setPrettyConditions(prettyConditions);
        }
    }
}
