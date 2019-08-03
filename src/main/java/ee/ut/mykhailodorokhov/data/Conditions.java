package ee.ut.mykhailodorokhov.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class Conditions {

    public static List<Condition> optimizeConditions(List<Condition> conditions) {
        List<Condition> optimizedConditions = new ArrayList<>();

        //
        // Non-numeric conditions
        //
        List<Condition> nonNumericConditions = conditions.stream().
                filter(x -> !x.isNumberic() && x.getOperator().equals("=")).
                map(x -> (Condition<String>)x).
                collect(toList());

        // Removing repeating conditions
        List<String> nonNumericVariables = nonNumericConditions.stream().map(x -> x.getVariable()).distinct().collect(toList());
        for (String variable : nonNumericVariables) {
            List<String> variableNumericConditionValues = nonNumericConditions.stream().
                    filter(x -> x.getVariable().toString().equals(variable)).
                    map(x -> x.getValue().toString()).distinct().collect(toList());

            variableNumericConditionValues.forEach((x) -> {
                optimizedConditions.add(new Condition(variable, "=", x));
            });
        }

        //
        // Numeric conditions
        //
        List<Condition> numericConditions = conditions.stream().filter(Condition::isNumberic).collect(toList());

        // Retreiving list of all the veriables that are used in unequalities
        List<String> numericVariables = numericConditions.stream().map(x -> x.getVariable()).distinct().collect(toList());
        for (String variable : numericVariables) {
            List<Condition> variableNumericConditions = numericConditions.stream().
                    filter(x -> x.getVariable() == variable).collect(toList());

            // Determining right bound condition
            List<Condition<Double>> rightBounds = variableNumericConditions.stream().
                    filter(x -> x.getOperator().equals("<") || x.getOperator().equals("<=")).
                    map(x -> (Condition<Double>)x).collect(toList());

            Optional<Condition<Double>> rightInnerBound =
                    rightBounds.stream().min((x1,x2) -> {
                        if ((x1.getValue()).equals(x2.getValue())) {
                            if(x1.getOperator() == x2.getOperator()) return 0;

                            if(x1.getOperator().equals("<")) return -1;
                            else return 1;
                        } else {
                            return Double.compare(x1.getValue(),x2.getValue());
                        }
                    });

            if (rightInnerBound.isPresent()) optimizedConditions.add(rightInnerBound.get());

            // Determining left bound condition
            List<Condition<Double>> leftBounds = variableNumericConditions.stream().
                    filter(x -> x.getOperator().equals(">") || x.getOperator().equals(">=")).
                    map(x -> (Condition<Double>)x).collect(toList());

            Optional<Condition<Double>> leftInnerBound =
                    leftBounds.stream().max((x1,x2) -> {
                        if ((x1.getValue()).equals(x2.getValue())) {
                            if(x1.getOperator() == x2.getOperator()) return 0;

                            if(x1.getOperator().equals(">")) return 1;
                            else return -1;
                        } else {
                            return Double.compare(x1.getValue(),x2.getValue());
                        }
                    });

            if (leftInnerBound.isPresent()) optimizedConditions.add(leftInnerBound.get());
        }

        return optimizedConditions;
    }
}
