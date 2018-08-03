package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.EventLog;
import ee.ut.mykhailodorokhov.data.Rule;
import ee.ut.mykhailodorokhov.data.LabeledFeatureVector;
import ee.ut.mykhailodorokhov.data.RuleSet;
import ee.ut.mykhailodorokhov.file.EventLogParser;
import ee.ut.mykhailodorokhov.helpers.FileDialogHelper;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.Comparator;
import java.util.List;

public class App {

    /**
     * Entry point.
     *
     * @param args Console arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        FileDialogHelper fileDialogHelper = new FileDialogHelper();
        File selectedFile = null;

        try {
            selectedFile = fileDialogHelper.openFile("Select Petri Net");
        }
        catch (NoSuchFileException ex) {
            System.out.println(ex.getMessage());
            return;
        }

        EventLogParser parser = new EventLogParser();
        EventLog eventLog = null;

        // Parse .xes file
        if (selectedFile.getName().contains(".xes"))
            eventLog = parser.fromXES(selectedFile);

        // Parse .csv file
        if (selectedFile.getName().contains(".csv"))
            eventLog = parser.fromCSV(selectedFile);

        OfflineRuleDiscoveryEngine engine = new OfflineRuleDiscoveryEngine();

        // Discover rules
        RuleSet rules = engine.discoverRules(eventLog, 5);

        // Sort rules by frequency
        rules.getRules().sort(Comparator.comparing(Rule::getFrequency).reversed());

        // Extract labeled feature vectors
        List<LabeledFeatureVector> data = engine.extractLabeledFeatureVectors(eventLog, rules);

        // Classify
        engine.classify(rules, data);
    }
}
