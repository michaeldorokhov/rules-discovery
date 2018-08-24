package ee.ut.mykhailodorokhov.data;

import ee.ut.mykhailodorokhov.helpers.ListHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ConditionVector {
    private List<Condition> conditions;
    private Boolean outcome;

    private Double total;
    private Double misclassified;
    private Double support;

    public ConditionVector() {
        this.conditions = new ArrayList<Condition>();
        this.total = 0.;
        this.misclassified = 0.;
        this.outcome = false;
    }

    public ConditionVector(List<Condition> conditions) {
        this.conditions = new ArrayList<>(conditions);
        this.total = 0.;
        this.misclassified = 0.;
        this.outcome = false;

    }

    public void add(String variable, String inequalityOperator, String value) {
        if(this.isDouble(value)) {
            Double doubleValue = Double.parseDouble(value);
            this.conditions.add(new Condition<Double>(variable, inequalityOperator, doubleValue));
        } else {
            this.conditions.add(new Condition<String>(variable, inequalityOperator, value));
        }
    }

    public void removeLast() {
        this.conditions.remove(this.conditions.size() - 1);
    }

    public List<Condition> getConditions() { return conditions; }

    public Boolean isConditionless() {
        return this.conditions.size() == 0;
    }

    public Boolean isTrue() { return outcome; }
    public void setOutcome(Boolean outcome) { this.outcome = outcome; }

    public Double getSupport() {
        if(this.support == null) {
            this.support = (this.total - this.misclassified) / this.total;
        }

        return this.support;
    }

    public void setSupport(Double support) { this.support = support; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Double getMisclassified() { return misclassified; }
    public void setMisclassified(Double misclassified) { this.misclassified = misclassified; }

    private Boolean isDouble( String string ) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder conditionsString = new StringBuilder();
        if(this.conditions.size()!=0) {
            this.conditions.forEach(x -> conditionsString.append(" | " + x.toString()));
            conditionsString.append(" --> ");
        }

        return String.format("%s%s (%.2f)", conditionsString.toString(), this.outcome.toString(), this.getSupport());
    }
}
