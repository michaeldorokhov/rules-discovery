package ee.ut.mykhailodorokhov.data;

import java.util.ArrayList;
import java.util.List;

/** Represents discovered Declare Constraint, that is also data-aware.
 * @author Mykhailo Dorokhov
 */
public class DiscoveredConstraint {
    private Constraint constraint;
    private Integer relevance;
    private List<Condition> prettyConditions;

    public DiscoveredConstraint(Constraint constraint, Integer relevance) {
        this.constraint = constraint;
        this.relevance = relevance;
        this.prettyConditions = new ArrayList<Condition>();
    }

    public DiscoveredConstraint(ConstraintType constraintType, String eventA, String eventB, Integer relevance) {
        this.constraint = new Constraint(constraintType, eventA, eventB);
        this.relevance = relevance;
        this.prettyConditions = new ArrayList<Condition>();
    }

    public Constraint getConstraint() { return this.constraint; }

    public ConstraintType getConstraintType() { return this.constraint.getConstraintType(); }
    public String getEventA() { return this.constraint.getEventA(); }
    public String getEventB() { return this.constraint.getEventB(); }

    public void setRelevance(Integer relevance) { this.relevance = relevance; }
    public Integer getRelevance() { return this.relevance; }
    public Integer incrementRelevance() { return ++this.relevance; }

    public void setPrettyConditions(List<Condition> prettyConditions) { this.prettyConditions = prettyConditions; }
    public List<Condition> getPrettyConditions() { return this.prettyConditions; }

    public Boolean isConditionAware() {
        if (this.prettyConditions != null) return this.prettyConditions.size() > 0;
        else return false;
    }

    @Override
    public String toString() {
        StringBuilder conditionsString = new StringBuilder();
        if(this.isConditionAware()) {
            this.prettyConditions.forEach(x -> conditionsString.append(" | " + x.toString()));
        }

        return this.constraint.toString() + conditionsString.toString();
    }
}
