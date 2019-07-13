package ee.ut.mykhailodorokhov.data;

import java.util.*;
import java.util.stream.Collectors;

public class RuleSet {
    private Map<Rule, Integer> rules;

    public RuleSet() {
        this.rules = new HashMap<Rule, Integer>();
    }

    public RuleSet(List<Rule> rules) {
        this.rules = new HashMap<Rule, Integer>();
        rules.forEach(rule -> this.rules.put(rule, 1));

    }

    public void addRuleOccurence(Rule rule) {
        if (this.rules.containsKey(rule)) {
            Integer newFrequency = this.rules.get(rule) + 1;
            this.rules.replace(rule, newFrequency);
        } else {
            this.rules.put(rule, 1);
        }
    }

    public List<Rule> getRules() { return this.rules.keySet().stream().collect(Collectors.toList()); }

    public List<Rule> getRulesSortedByFrequency() {
        List<Integer> sortedFrequencies = this.rules.values().stream().collect(Collectors.toList());
        sortedFrequencies.sort(Comparator.naturalOrder());

        List<Rule> sortedRules = new ArrayList<>();

        for(Integer i : sortedFrequencies) {
            this.rules.entrySet().stream().
                    filter(x -> x.getValue() == i).
                    forEach(x -> sortedRules.add(x.getKey()));
        }

        return sortedRules;
    }

    public Integer getFrequencyOfRule(Rule rule) { return this.rules.get(rule); }
    public void incrementFrequencyOfRule(Rule rule) {
        Integer newFrequency = this.rules.get(rule) + 1;
        this.rules.replace(rule, newFrequency);
    }

    public List<Rule> getRulesWithMinimumFrequency(Integer minimumFrequency) {
        List<Rule> filteredRules = new ArrayList<>();
        this.rules.entrySet().stream().
                filter(x -> x.getValue() > minimumFrequency).
                forEach(x -> filteredRules.add(x.getKey()));

        return filteredRules;
    }
}
