package ee.ut.mykhailodorokhov.data;

import java.util.Objects;

public class Constraint {
    private ConstraintType constraintType;
    private String eventA;
    private String eventB;

    public Constraint(ConstraintType constraintType, String eventA, String eventB) {
        this.constraintType = constraintType;
        this.eventA = eventA;
        this.eventB = eventB;
    }

    public ConstraintType getConstraintType() { return this.constraintType; }
    public String getEventA() { return this.eventA; }
    public String getEventB() { return this.eventB; }

    @Override
    public String toString() {
        return this.constraintType.toString() + "(" + this.eventA + ", " + this.eventB + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Constraint constraint = (Constraint) o;
        return this.constraintType == constraint.constraintType &&
               this.eventA.equals(constraint.eventA) &&
               this.eventB.equals(constraint.eventB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraintType, eventA, eventB);
    }
}
