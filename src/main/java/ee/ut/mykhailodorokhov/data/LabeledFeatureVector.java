package ee.ut.mykhailodorokhov.data;

import java.util.Map;

public class LabeledFeatureVector {
    private Rule rule;
    private Map<String, String> attributes;
    private boolean outcome;

    public LabeledFeatureVector(Rule rule, Map<String, String> attributes, boolean outcome) {
        this.rule = rule;
        this.attributes = attributes;
        this.outcome = outcome;
    }

    public Rule getRule() { return this.rule; }

    public Map<String, String> getAttributes() { return this.attributes; }

    public boolean isActivation() { return this.outcome; }

    @Override
    public String toString() {
        StringBuilder attributesString = new StringBuilder();
        for (Map.Entry<String, String> entry : this.attributes.entrySet())
        {
            attributesString.append(entry.getKey() + ":" + entry.getValue() + " ");
        }

        return this.rule.toString() + " - { " + attributesString.toString() + "} - " + this.outcome;
    }
}
