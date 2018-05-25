package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.EventLog;
import ee.ut.mykhailodorokhov.file.EventLogParser;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

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

        // OfflineRuleDiscoveryEngine engine = new OfflineRuleDiscoveryEngine();
    }

    private static File[] openFile(String message)
    {
        FileDialog loadDialog = new FileDialog(new Frame(), message, FileDialog.LOAD);

        loadDialog.setMultipleMode(false);
        loadDialog.setVisible(true);

        return loadDialog.getFiles();
    }
}
