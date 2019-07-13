package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.*;
import ee.ut.mykhailodorokhov.file.EventLogParser;
import ee.ut.mykhailodorokhov.helpers.FileHelper;

import java.io.File;

public class App {

    /**
     * Entry point.
     *
     * @param args Console arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        EventLog eventLog = FileHelper.openEventLog();

        OfflineRuleDiscoveryEngine engine = new OfflineRuleDiscoveryEngine();
        RuleSet conditionAwareRules = engine.discoverConditionAwareRules(eventLog);

        conditionAwareRules.getRules().stream().
                filter(x -> x.isConditionAware()).
                forEach(x -> System.out.println(x.toString()));
    }
}
