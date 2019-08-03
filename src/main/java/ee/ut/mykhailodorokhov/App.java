package ee.ut.mykhailodorokhov;

import ee.ut.mykhailodorokhov.data.*;
import ee.ut.mykhailodorokhov.helpers.FileHelper;

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
        DiscoveredConstraintList conditionAwareRules = engine.discoverConditionAwareRules(eventLog);

        conditionAwareRules.getDiscoveredConstraints().stream().
                forEach(x -> System.out.println(x.toString()));
    }
}
