package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.EventLog;
import ee.ut.mykhailodorokhov.data.Rule;
import ee.ut.mykhailodorokhov.data.Snapshot;
import ee.ut.mykhailodorokhov.file.EventLogParser;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    /**
     * Entry point.
     *
     * @param args Console arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        File[] selectedFiles;

        selectedFiles = openFile("Select Petri Net");
        File selectedFile;

        if (selectedFiles.length > 0) {
            selectedFile = selectedFiles[0];
        } else {
            System.out.println("Aborted by user.");
            return;
        }

        EventLogParser parser = new EventLogParser();
        EventLog log = parser.parseFromCSV(selectedFile);

        OfflineRuleDiscoveryEngine engine = new OfflineRuleDiscoveryEngine();
        List<Rule> rules = engine.discoverRules(log);

        rules.sort(Comparator.comparing(Rule::getFrequency).reversed());

        DataAwareActivationViolationExtractor extractor = new DataAwareActivationViolationExtractor();
        List<Snapshot> data = extractor.extract(log, rules.stream().filter(x -> x.getFrequency() > 5).collect(Collectors.toList()));
    }

    private static File[] openFile(String message)
    {
        FileDialog loadDialog = new FileDialog(new Frame(), message, FileDialog.LOAD);

        loadDialog.setMultipleMode(false);
        loadDialog.setVisible(true);

        return loadDialog.getFiles();
    }
}
