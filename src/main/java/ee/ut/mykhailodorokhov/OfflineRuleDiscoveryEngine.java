package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.*;
import ee.ut.mykhailodorokhov.helpers.ListHelper;
import ee.ut.mykhailodorokhov.helpers.WekaHelper;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;

public class OfflineRuleDiscoveryEngine {

    public DiscoveredConstraintList discoverConditionAwareRules(EventLog log) throws Exception{
        // Discover all the possible constraints
        DiscoveredConstraintList allDiscoveredConstraints = this.discoverConstraints(log);

        // Filter constraints with the given relevance and sorting them by relevance for convinience
        DiscoveredConstraintList discoveredConstraints = allDiscoveredConstraints.
                getDiscoveredConstraintsWithMinimumRelevance(10).
                getDiscoveredConstraintsSortedByRelevance();

        // Extract labeled feature vectors from the log
        List<LabeledFeatureVector> labeledFeatureVectors = this.extractLabeledFeatureVectors(log, discoveredConstraints);

        // Discover conditions for the constraints
        this.extractConditions(discoveredConstraints, labeledFeatureVectors);

        return discoveredConstraints;
    }

    public DiscoveredConstraintList discoverConstraints(EventLog eventLog) {
        List<Case> cases = eventLog.getCases();
        List<String> uniqueEventNames = eventLog.getUniqueEventNames();

        DiscoveredConstraintList discoveredConstraints = new DiscoveredConstraintList();

        for(Case caseInstance : cases) {

            for(String eventNameA : uniqueEventNames) {
                for(String eventNameB : uniqueEventNames) {
                    if(eventNameA.equals(eventNameB)) continue;

                    List<Integer> indexesA = caseInstance.getEventIndexesList(eventNameA);
                    List<Integer> indexesB = caseInstance.getEventIndexesList(eventNameB);

                    // Rules that are activated by A
                    for(Integer indexA : indexesA) {

                        // Responded Existense
                        if (!indexesB.isEmpty()) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.RESPONDED_EXISTENCE, eventNameA, eventNameB));
                        }

                        // Not Responded Existence
                        if (indexesB.isEmpty()) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.NOT_RESPONDED_EXISTENCE, eventNameA, eventNameB));
                        }

                        // Response
                        if (indexesB.stream().anyMatch(x -> indexA < x)) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.RESPONSE, eventNameA, eventNameB));
                        }

                        // Not Response
                        if (indexesB.stream().noneMatch(x -> indexA < x)) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.NOT_RESPONSE, eventNameA, eventNameB));
                        }

                        // Chain Response
                        if (indexesB.stream().anyMatch(x -> x == indexA + 1)) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.CHAIN_RESPONSE, eventNameA, eventNameB));
                        }

                        // Not Chain Response
                        if (indexesB.stream().noneMatch(x -> x == indexA + 1)) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.NOT_CHAIN_RESPONSE, eventNameA, eventNameB));
                        }

                        // Alternate Response
                        if (indexesB.stream().anyMatch(x -> indexA < x) &&
                            indexesA.stream().noneMatch(x -> indexA < x && x < ListHelper.minInteger(indexesB))) {

                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.ALTERNATE_RESPONSE, eventNameA, eventNameB));
                        }
                    }

                    // Rules that are activated by B
                    for(Integer indexB : indexesB) {

                        // Precedence
                        if (indexesA.stream().anyMatch(x -> indexB > x)) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.PRECEDENCE, eventNameA, eventNameB));
                        }

                        // Not Precedence
                        if (indexesA.stream().noneMatch(x -> indexB > x)) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.NOT_PRECEDENCE, eventNameA, eventNameB));
                        }

                        // Chain Precedence
                        if (indexesA.stream().anyMatch(x -> x == indexB - 1)) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.CHAIN_PRECEDENCE, eventNameA, eventNameB));
                        }

                        // Not Chain Prevedence
                        if (indexesA.stream().noneMatch(x -> x == indexB - 1)) {
                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.NOT_CHAIN_PRECEDENCE, eventNameA, eventNameB));
                        }

                        // Alternate Precedence
                        if (indexesA.stream().anyMatch(x -> indexB > x) &&
                            indexesB.stream().noneMatch(x -> ListHelper.maxInteger(indexesA) < x && x < indexB )) {

                            discoveredConstraints.registerActivation(
                                    new Constraint(ConstraintType.ALTERNATE_PRECEDENCE, eventNameA, eventNameB));
                        }
                    }
                }
            }
        }

        return discoveredConstraints;
    }

    public List<LabeledFeatureVector> extractLabeledFeatureVectors(EventLog log, DiscoveredConstraintList discoveredConstraint) {

        List<LabeledFeatureVector> labeledFeatureVectors = new ArrayList<>();

        for (Case caseInstance : log.getCases()) {
            for (DiscoveredConstraint discoveredConstrain : discoveredConstraint.getDiscoveredConstraints()) {

                List<Integer> indexesA = caseInstance.getEventIndexesList(discoveredConstrain.getEventA());
                List<Integer> indexesB = caseInstance.getEventIndexesList(discoveredConstrain.getEventB());

                // Rules that are activated by A
                for (Integer indexA : indexesA) {

                    switch(discoveredConstrain.getConstraintType()){
                        case RESPONDED_EXISTENCE:
                            if (!indexesB.isEmpty())
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        true));

                            if (indexesB.isEmpty())
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        false));

                            break;
                        case NOT_RESPONDED_EXISTENCE:
                            if (indexesB.isEmpty())
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        true));

                            if (!indexesB.isEmpty())
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        false));

                            break;
                        case RESPONSE:
                            if (indexesB.stream().anyMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        true));

                            if (indexesB.stream().noneMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        false));

                            break;
                        case NOT_RESPONSE:
                            if (indexesB.stream().noneMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        true));

                            if (indexesB.stream().anyMatch(x -> indexA < x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        false));
                            break;
                        case CHAIN_RESPONSE:
                            if (indexesB.stream().anyMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        true));

                            if (indexesB.stream().noneMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        false));
                            break;
                        case NOT_CHAIN_RESPONSE:
                            if (indexesB.stream().noneMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        true));

                            if (indexesB.stream().anyMatch(x -> x == indexA + 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        false));
                            break;
                        case ALTERNATE_RESPONSE:
                            if (indexesB.stream().anyMatch(x -> indexA < x) &&
                                    indexesA.stream().noneMatch(x -> indexA < x && x < ListHelper.minInteger(indexesB)))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        true));

                            if (indexesB.stream().noneMatch(x -> indexA < x) ||
                                    (indexesB.stream().anyMatch(x -> indexA < x) &&
                                    indexesA.stream().anyMatch(x -> indexA < x && x < ListHelper.minInteger(indexesB))))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexA).getPayload(),
                                        false));
                            break;
                    }
                }

                // Rules that are activated by B
                for(Integer indexB : indexesB) {
                    switch(discoveredConstrain.getConstraintType()) {
                        case PRECEDENCE:
                            if (indexesA.stream().anyMatch(x -> indexB > x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        true));
                            if (indexesA.stream().noneMatch(x -> indexB > x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        false));

                            break;
                        case NOT_PRECEDENCE:
                            if (indexesA.stream().noneMatch(x -> indexB > x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        true));

                            if (indexesA.stream().anyMatch(x -> indexB > x))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        false));

                            break;
                        case CHAIN_PRECEDENCE:
                            if (indexesA.stream().anyMatch(x -> x == indexB - 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        true));

                            if (indexesA.stream().noneMatch(x -> x == indexB - 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        true));

                            break;
                        case NOT_CHAIN_PRECEDENCE:
                            if (indexesA.stream().noneMatch(x -> x == indexB - 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        true));

                            if (indexesA.stream().anyMatch(x -> x == indexB - 1))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        false));
                            break;
                        case ALTERNATE_PRECEDENCE:
                            if (indexesA.stream().anyMatch(x -> indexB > x) &&
                                    indexesB.stream().noneMatch(x -> ListHelper.maxInteger(indexesA) < x && x < indexB ))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        true));

                            if (indexesA.stream().noneMatch(x -> indexB > x) ||
                                    (indexesA.stream().anyMatch(x -> indexB > x) &&
                                    indexesB.stream().anyMatch(x -> ListHelper.maxInteger(indexesA) < x && x < indexB )))
                                labeledFeatureVectors.add(new LabeledFeatureVector(
                                        discoveredConstrain.getConstraint(),
                                        caseInstance.getEvents().get(indexB).getPayload(),
                                        false));
                            break;
                    }
                }
            }
        }

        return labeledFeatureVectors;
    }

    public void extractConditions(DiscoveredConstraintList rules, List<LabeledFeatureVector> labeledFeatureVectors) throws Exception {
        for(DiscoveredConstraint constraint : rules.getDiscoveredConstraints()) {

            List<LabeledFeatureVector> relevantFeatureVectors = labeledFeatureVectors.stream().
                    filter(x -> x.getConstraint().equals(constraint.getConstraint())).collect(Collectors.toList());

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

            // Tree parameters
            StringBuilder options = new StringBuilder();
            //options.append("-U");
            //options.append("-M 7");

            J48 tree = new J48();
            tree.setOptions(options.toString().split(" "));
            tree.buildClassifier(dataSet);

            // Parsing J48 tree output
            List<Condition> conditions = new ArrayList<>();
            List<TreeBranch> treeBranches = WekaHelper.parseJ48Tree(tree.toString());

            treeBranches.stream().
                    filter(x -> x.getSupport() > 0.1 && x.isTrue()).
                    forEach(x -> conditions.addAll(x.getConditions()));

            List<Condition> prettyConditions = Conditions.optimizeConditions(conditions);

            // Adding discovered and optimized conditions to the constraint
            constraint.setPrettyConditions(prettyConditions);
        }
    }
}
