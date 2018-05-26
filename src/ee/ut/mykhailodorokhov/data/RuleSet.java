package ee.ut.mykhailodorokhov.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RuleSet {
    List<Rule> rules;

    public RuleSet() {
        this.rules = new ArrayList<>();
    }

    public RuleSet(List<Rule> rules) {
        this.rules = rules;
    }

    public void addRuleOccurence(Rule rule) {
        if (this.rules.contains(rule)) this.rules.get(rules.indexOf(rule)).incrementFrequency();
        else this.rules.add(rule);
    }

    public List<Rule> getRulesWithMinimumFrequency(Integer minimumFrequency) {
        return this.rules.stream().filter(x -> x.getFrequency() > minimumFrequency).collect(Collectors.toList());
    }

    public List<Rule> getRules() { return this.rules; }
}
