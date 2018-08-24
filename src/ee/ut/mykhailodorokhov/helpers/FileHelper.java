package ee.ut.mykhailodorokhov.helpers;

import ee.ut.mykhailodorokhov.data.EventLog;
import ee.ut.mykhailodorokhov.file.EventLogParser;

import java.awt.*;
import java.io.File;
import java.nio.file.NoSuchFileException;

public class FileHelper {

    public static EventLog openEventLog () throws Exception{
        FileHelper fileDialogHelper = new FileHelper();
        File selectedFile = null;

        selectedFile = fileDialogHelper.openFile("Select Event log");

        EventLogParser parser = new EventLogParser();
        EventLog eventLog = null;

        // Parse .xes file
        if (selectedFile.getName().contains(".xes"))
            eventLog = parser.fromXES(selectedFile);

        // Parse .csv file
        if (selectedFile.getName().contains(".csv"))
            eventLog = parser.fromCSV(selectedFile);

        return eventLog;
    }

    public static File openFile(String message) throws NoSuchFileException
    {
        FileDialog loadDialog = new FileDialog(new Frame(), message, FileDialog.LOAD);

        loadDialog.setMultipleMode(false);
        loadDialog.setVisible(true);

        File[] selectedFiles = loadDialog.getFiles();

        if (selectedFiles.length == 0) {
            throw new NoSuchFileException("User did not select any file");
        }

        return selectedFiles[0];
    }

}
