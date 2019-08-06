package ee.ut.mykhailodorokhov.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DiscoveredConstraintList {
    private List<DiscoveredConstraint> constraints;

    public DiscoveredConstraintList() {
        this.constraints = new ArrayList<DiscoveredConstraint>();
    }

    public DiscoveredConstraintList(List<DiscoveredConstraint> constraints) {
        this.constraints = constraints;
    }

    public void registerActivation(Constraint constraint) {
        Optional<DiscoveredConstraint> possiblyAlreadyDiscoveredConstraint =
                this.constraints.stream().filter(x -> x.getConstraint().equals(constraint)).findFirst();

        if (possiblyAlreadyDiscoveredConstraint.isPresent()) {
            // Now that we know that such DiscoveredConstraint is already present in the list
            // we can safely use get() method of the Optional class and simply increment constraint's relevance.
            possiblyAlreadyDiscoveredConstraint.get().incrementRelevance();
        } else {
            // If not, we create the new DiscoveredConstraint from scratch.
            this.constraints.add(new DiscoveredConstraint(constraint, 1));
        }
    }

    public List<DiscoveredConstraint> getDiscoveredConstraints() { return this.constraints; }

    public DiscoveredConstraintList getDiscoveredConstraintsWithMinimumRelevance(Integer minimumRelevance) {
        return new DiscoveredConstraintList(this.constraints.stream().
                filter(x -> x.getRelevance() >= minimumRelevance).
                collect(Collectors.toList()));
    }

    public DiscoveredConstraintList getDiscoveredConstraintsSortedByRelevance() {
        List<DiscoveredConstraint> sortedDiscoveredConstraints = new ArrayList<>(this.constraints);
        sortedDiscoveredConstraints.sort(Comparator.comparing(x -> ((DiscoveredConstraint)x).getRelevance()).reversed());

        return new DiscoveredConstraintList(sortedDiscoveredConstraints);
    }
}
