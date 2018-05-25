package ee.ut.mykhailodorokhov.data;

public class Rule {
    private RuleEnum ruleType;

    private Event eventA;
    private Event eventB;

    public Rule(RuleEnum ruleType, Event eventA, Event eventB) {
        this.ruleType = ruleType;
        this.eventA = eventA;
        this.eventB = eventB;
    }

    public RuleEnum getRuleType() { return this.ruleType; }

    public Event getEventA() { return this.eventA; }

    public Event getEventB() { return this.eventB; }

}
