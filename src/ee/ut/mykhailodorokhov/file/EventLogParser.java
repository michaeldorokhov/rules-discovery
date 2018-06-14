package ee.ut.mykhailodorokhov.file;

import ee.ut.mykhailodorokhov.data.Case;
import ee.ut.mykhailodorokhov.data.Event;
import ee.ut.mykhailodorokhov.data.EventLog;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;

public class EventLogParser {

    private String splitCharacter = "[;,]";

    public EventLog fromCSV(File csvFile) throws Exception {
        FileReader fileReader = new FileReader(csvFile);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));

        // Reading header

        String[] header = bufferedReader.readLine().split(this.splitCharacter);

        String dateFormat = header[2];

        List<String> attributeNames = new ArrayList<String>();
        for( int i = 4; i < header.length; i++) attributeNames.add(header[i]);

        // Reading the body of the CSV file

        Map<String, List<Event>> events = new HashMap<>();

        String line;

        String caseID;
        Integer eventID;
        String eventName;
        Date timestamp;
        Map<String, String> payload;

        while ((line = bufferedReader.readLine()) != null) {
            String[] caseString = line.split(this.splitCharacter);

            caseID = caseString[0];

            eventID = Integer.parseInt(caseString[1]);

            SimpleDateFormat parser = new SimpleDateFormat(dateFormat);
            timestamp = parser.parse(caseString[2]);

            eventName = caseString[3];

            payload = new HashMap<>();
            for( int i = 4; i < caseString.length; i++) payload.put(attributeNames.get(i-4), caseString[i]);

            if (events.containsKey(caseID)) {
                events.get(caseID).add(new Event(eventID, eventName, payload, timestamp));
            } else {
                events.put(caseID, new ArrayList<>( Arrays.asList( new Event(eventID, eventName, payload, timestamp) ) ));
            }
        }
        fileReader.close();

        // Making list of cases out of the map with CaseID as keys
        List<Case> cases = new ArrayList<>();
        for(String caseId : events.keySet()) {
            cases.add(new Case(caseId, events.get(caseId)));
        }

        return new EventLog(cases);
    }

    public EventLog fromXES(File xesFile) {

        XesXmlParser parser = new XesXmlParser();

        XLog eventLogXES = null;

        if(parser.canParse(xesFile)){
            try {
                eventLogXES = parser.parse(xesFile).stream().findFirst().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<Case> cases = new ArrayList<Case>();

        for(XTrace caseEntity : eventLogXES){
            List<Event> events = new ArrayList<Event>();
            String caseId = XConceptExtension.instance().extractName(caseEntity);

            /*
            XAttributeMap caseXAttributes = caseEntity.getAttributes();
            List<String> caseAttributes = new ArrayList<String>();
            for(String key : caseXAttributes.keySet()){
                caseAttributes.add(caseXAttributes.get(key).toString());
            }*/

            for(XEvent event : caseEntity){
                String eventName = XConceptExtension.instance().extractName(event);
                Date timestamp = XTimeExtension.instance().extractTimestamp(event);

                XAttributeMap eventAttributes = event.getAttributes();
                Map <String, String> payload = new HashMap<>();
                for(String key :eventAttributes.keySet()){
                    // Apparently lower case attributes in .xes files are technical data
                    if (Character.isUpperCase(key.charAt(0))) {
                        payload.put(key, eventAttributes.get(key).toString());
                    }
                }

                events.add(new Event(1, eventName, payload, timestamp));
            }

            cases.add(new Case(caseId, events));
        }

        return new EventLog(cases);
    }
}
