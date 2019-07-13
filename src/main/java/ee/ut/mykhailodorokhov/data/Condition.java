package ee.ut.mykhailodorokhov.data;

public class Condition<T> {
    private String variable;
    private String operator;
    private T value;

    public Condition(String variable, String operator, T value) {
        this.variable = variable;
        this.operator = operator;
        this.value = value;
    }

    public String getVariable() { return variable; }

    public String getOperator() { return operator; }

    public T getValue() { return value; }

    public Boolean isNumberic() {
        return this.value.getClass().equals(Double.class);
    }

    @Override
    public String toString() {
        return this.variable.toString() + " " + this.operator + " " + this.value;
    }
}
