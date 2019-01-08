package ee.ut.mykhailodorokhov.data;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ConditionTest {

    @Test
    public void OptimizeConditionsTest() {
        List<Condition> conditions = Lists.newArrayList(
                new Condition<Double>("a", "<", 20.0),
                new Condition<Double>("a", "<", 10.0),
                new Condition<Double>("a", "<=", 10.0),
                new Condition<Double>("a", ">", 1.0),
                new Condition<Double>("a", ">=", 5.0),
                new Condition<Double>("a", ">", 5.0));

        List<Condition> optimizedConditions = Lists.newArrayList(
                new Condition<Double>("a", "<", 10.0),
                new Condition<Double>("a", ">", 5.0));

        Assert.assertEquals(optimizedConditions, Conditions.optimizeConditions(conditions));
    }
}
