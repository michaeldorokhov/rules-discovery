package ee.ut.mykhailodorokhov.file;

import ee.ut.mykhailodorokhov.data.Case;
import ee.ut.mykhailodorokhov.data.Event;
import ee.ut.mykhailodorokhov.data.EventLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class EventLogParser {

    private String splitCharacter = "[;,]";

    public EventLog parseFromCSV(File eventLogFile) throws Exception {
        FileReader fileReader = new FileReader(eventLogFile);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(eventLogFile));

        //
        // Reading header
        //
        String[] header = bufferedReader.readLine().split(this.splitCharacter);

        String dateFormat = header[2];

        List<String> attributeNames = new ArrayList<String>();
        for( int i = 3; i < header.length; i++) attributeNames.add(header[i]);

        //
        // Reading the body of the CSV file
        //
        Map<String, List<Event>> events = new HashMap<>();

        String line;

        String caseID;
        Integer eventID;
        String eventName;
        Date date;
        Map<String, String> payload;

        while ((line = bufferedReader.readLine()) != null) {
            String[] caseString = line.split(this.splitCharacter);

            caseID = caseString[0];

            eventID = Integer.parseInt(caseString[1]);

            SimpleDateFormat parser = new SimpleDateFormat(dateFormat);
            date = parser.parse(caseString[2]);

            eventName = caseString[3];

            payload = new HashMap<>();
            for( int i = 4; i < caseString.length; i++) payload.put(attributeNames.get(i-4), caseString[i]);

            if (events.containsKey(caseID)) {
                events.get(caseID).add(new Event(eventID, eventName, payload, date));
            } else {
                events.put(caseID, new ArrayList<>( Arrays.asList( new Event(eventID, eventName, payload, date) ) ));
            }
        }
        fileReader.close();

        //
        // Making list of cases out of the map with CaseID as keys
        //
        List<Case> cases = new ArrayList<>();
        for(String caseName : events.keySet()) {
            cases.add(new Case(caseName, events.get(caseName)));
        }

        return new EventLog(cases);
    }
}
