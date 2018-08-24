package ee.ut.mykhailodorokhov.data;

import java.util.Objects;

public class Rule {
    protected RuleEnum ruleType;

    protected String eventA;
    protected String eventB;

    public Rule(RuleEnum ruleType, String eventA, String eventB) {
        this.ruleType = ruleType;
        this.eventA = eventA;
        this.eventB = eventB;
    }

    public RuleEnum getRuleType() { return this.ruleType; }

    public String getEventA() { return this.eventA; }

    public String getEventB() { return this.eventB; }

    @Override
    public String toString() {
        return this.ruleType.toString() + "(" + this.eventA + ", " + this.eventB + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;
        return this.ruleType == rule.ruleType &&
               Objects.equals(this.eventA, rule.eventA) &&
               Objects.equals(this.eventB, rule.eventB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleType, eventA, eventB);
    }
}
