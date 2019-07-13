package ee.ut.mykhailodorokhov.data;

import java.util.List;
import java.util.Objects;

public class Rule {
    protected RuleType ruleType;

    protected String eventA;
    protected String eventB;

    protected List<Condition> prettyConditions;

    public Rule(RuleType ruleType, String eventA, String eventB) {
        this.ruleType = ruleType;
        this.eventA = eventA;
        this.eventB = eventB;
    }

    public Rule(RuleType ruleType, String eventA, String eventB, List<Condition> prettyConditions) {
        this.ruleType = ruleType;
        this.eventA = eventA;
        this.eventB = eventB;
        this.prettyConditions = prettyConditions;
    }

    public void setPrettyConditions(List<Condition> prettyConditions) {
        this.prettyConditions = prettyConditions;
    }

    public RuleType getRuleType() { return this.ruleType; }

    public String getEventA() { return this.eventA; }

    public String getEventB() { return this.eventB; }

    public List<Condition> getPrettyConditions() { return prettyConditions; }

    public Boolean isConditionAware() {
        if (prettyConditions != null) return prettyConditions.size() > 0;
        else return false;
    }

    @Override
    public String toString() {
        StringBuilder conditionsString = new StringBuilder();
        if(this.isConditionAware()) {
            this.prettyConditions.forEach(x -> conditionsString.append(" | " + x.toString()));
        }

        return this.ruleType.toString() + "(" + this.eventA + ", " + this.eventB + ")" + conditionsString.toString();
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
