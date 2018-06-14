package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.*;
import ee.ut.mykhailodorokhov.helpers.ListHelper;
import ee.ut.mykhailodorokhov.helpers.WekaHelper;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                                indexesA.stream().noneMatch(x -> indexA < x && x < ListHelper.min(indexesB))) {
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
                            if (!indexesB.isEmpty() && indexesB.stream().anyMatch(x -> indexA < x) &&
                                    indexesA.stream().noneMatch(x -> indexA < x && x < ListHelper.min(indexesB)))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), true));

                            if (indexesB.isEmpty() || indexesB.stream().noneMatch(x -> indexA < x) ||
                                    (indexesB.stream().anyMatch(x -> indexA < x) &&
                                    indexesA.stream().anyMatch(x -> indexA < x && x < ListHelper.min(indexesB))))
                                labeledFeatureVectors.add(new LabeledFeatureVector(rule, caseInstance.getEvents().get(indexA).getPayload(), false));
                            break;

                        // TODO: add processing for other rules
                    }
                }
            }
        }
        return labeledFeatureVectors;
    }

    public void classify(RuleSet rules, List<LabeledFeatureVector> labeledFeatureVectors) throws Exception {

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

            J48 tree = new J48();

            StringBuilder options = new StringBuilder();
            //options.append("-U");
            options.append("-M 7");
            tree.setOptions(options.toString().split(" "));

            tree.buildClassifier(dataSet);

                        System.out.println(tree.toSummaryString());
            System.out.println(tree);
            System.out.println(tree.graph());

            // display classifier
            /*
            final javax.swing.JFrame jf =
                    new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
            jf.setSize(500,400);
            jf.getContentPane().setLayout(new BorderLayout());
            TreeVisualizer tv = new TreeVisualizer(null,
                    tree.graph(),
                    new PlaceNode2());
            jf.getContentPane().add(tv, BorderLayout.CENTER);
            jf.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    jf.dispose();
                }
            });

            jf.setVisible(true);
            tv.fitToScreen();*/
        }
    }
}
