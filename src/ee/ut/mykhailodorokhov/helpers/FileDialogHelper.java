package ee.ut.mykhailodorokhov.helpers;

import java.awt.*;
import java.io.File;
import java.nio.file.NoSuchFileException;

public class FileDialogHelper {

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
