package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.*;
import ee.ut.mykhailodorokhov.helpers.ListHelper;
import ee.ut.mykhailodorokhov.helpers.WekaHelper;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class OfflineRuleDiscoveryEngine {

    public void discoverConditionAwareRules(EventLog log) throws Exception{
        // Discover rules
        RuleSet rules = this.discoverRules(log, 5);

        rules = new RuleSet(rules.getRulesSortedByFrequency());

        // Extract labeled feature vectors
        List<LabeledFeatureVector> data = this.extractLabeledFeatureVectors(log, rules);

        // Define conditions for the rules
        this.extractConditions(rules, data);
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
                                indexesA.stream().noneMatch(x -> indexA < x && x < ListHelper.minInteger(indexesB))) {
                            Rule newRule = new Rule(RuleEnum.ALTERNATE_RESPONSE, eventNameA, eventNameB);
                            rules.addRuleOccurence(newRule);
                        }

                        // TODO: add other rules for discovery
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
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> indexA < x) &&
                                    indexesA.stream().noneMatch(x -> indexA < x && x < ListHelper.minInteger(indexesB)))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> indexA < x) ||
                                    (indexesB.stream().anyMatch(x -> indexA < x) &&
                                    indexesA.stream().anyMatch(x -> indexA < x && x < ListHelper.minInteger(indexesB))))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));
                            break;

                        // TODO: add processing for other rules
                    }
                }
            }
        }
        return labeledFeatureVectors;
    }

    public void extractConditions(RuleSet rules, List<LabeledFeatureVector> labeledFeatureVectors) throws Exception {

        // Debug related code
        int i = 0;
        // Debug related code

        for(Rule rule : rules.getRules()) {

            // Debug related code
            System.out.println("=====================");
            i++;
            System.out.println(i + " - Rule: " + rule.toString());
            // Debug related code

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
            //options.append("-U");
            //options.append("-M 7");

            J48 tree = new J48();
            tree.setOptions(options.toString().split(" "));
            tree.buildClassifier(dataSet);

            System.out.println(tree.toString());

            List<Condition> conditions = new ArrayList<>();

            List<ConditionVector> conditionVectors = WekaHelper.parseJ48Tree(tree.toString());
            conditionVectors.stream().
                    filter(x -> x.getSupport() > 0.1 && x.isTrue()).
                    forEach(x -> conditions.addAll(x.getConditions()));

            List<Condition> prettyConditions = this.optimizeConditions(conditions);

            System.out.println("------------------");

            // TODO: parse and get conditions from the tree
        }
    }

    private List<Condition> optimizeConditions(List<Condition> conditions) {
        List<Condition> optimizedConditions = new ArrayList<>();

        List<Condition> numericUnequalities = this.getElementsOf(conditions, Double.class);
        List<String> variables = numericUnequalities.stream().map(x -> x.getVariable()).distinct().collect(toList());
        for (String variable : variables) {
            List<Double> upperBounds = numericUnequalities.stream().
                    filter(x -> x.getOperator().equals("<=") || x.getOperator().equals("<"))
                    .map(x -> (Double)x.getValue()).collect(toList());

            List<Double> lowerBounds = numericUnequalities.stream().
                    filter(x -> x.getOperator().equals(">=") || x.getOperator().equals(">"))
                    .map(x -> (Double)x.getValue()).collect(toList());

            if (upperBounds.size() > 0) {
                Double upperBound = ListHelper.minDouble(upperBounds);
                optimizedConditions.add(new Condition(variable, "<=", upperBound));
            }

            if (lowerBounds.size() > 0) {
                Double lowerBound = ListHelper.maxDouble(lowerBounds);
                optimizedConditions.add(new Condition(variable, ">=", lowerBound));
            }
        }

        return optimizedConditions;
    }

    private List<Condition> getElementsOf(List<Condition> list, Class type) {
        return list.stream()
                .filter(x -> x.getValue().getClass().equals(type))
                .collect(toList());
    }
}
