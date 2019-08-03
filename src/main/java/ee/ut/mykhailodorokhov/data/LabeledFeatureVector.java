package ee.ut.mykhailodorokhov.data;

import java.util.Map;

public class LabeledFeatureVector {
    private Constraint constraint;
    private Map<String, String> attributes;
    private boolean outcome;

    public LabeledFeatureVector(Constraint constraint, Map<String, String> attributes, boolean outcome) {
        this.constraint = constraint;
        this.attributes = attributes;
        this.outcome = outcome;
    }

    public Constraint getConstraint() { return this.constraint; }

    public Map<String, String> getAttributes() { return this.attributes; }

    public boolean isActivation() { return this.outcome; }

    @Override
    public String toString() {
        StringBuilder attributesString = new StringBuilder();
        for (Map.Entry<String, String> entry : this.attributes.entrySet())
        {
            attributesString.append(entry.getKey() + ":" + entry.getValue() + " ");
        }

        return this.constraint.toString() + " - { " + attributesString.toString() + "} - " + this.outcome;
    }
}
