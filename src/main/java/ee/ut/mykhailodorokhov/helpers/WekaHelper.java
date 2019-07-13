package ee.ut.mykhailodorokhov.helpers;

import ee.ut.mykhailodorokhov.data.TreeBranch;
import ee.ut.mykhailodorokhov.data.LabeledFeatureVector;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WekaHelper {
    public static Instances toWekaDataSet(List<LabeledFeatureVector> labeledFeatureVectors) throws Exception {
        StringBuilder data = new StringBuilder();

        // getting header
        List<String> header = new ArrayList<String>();
        labeledFeatureVectors.get(0).getAttributes().keySet().forEach(x -> header.add(x));

        // converting header into the CSV format
        StringBuilder headerString = new StringBuilder();
        header.forEach(x -> headerString.append(x + ","));
        headerString.append("outcome");

        data.append(headerString.toString() + System.lineSeparator());

        // getting rows
        List<List<String>> rows = new ArrayList<List<String>>();
        for (LabeledFeatureVector labeledFeatureVector : labeledFeatureVectors) {
            // getting a row
            List<String> row = new ArrayList<String>();
            for(Map.Entry<String, String> entry : labeledFeatureVector.getAttributes().entrySet()){
                if (header.contains(entry.getKey())) { row.add(entry.getValue()); }
            }

            // converting a row into the CSV structure
            StringBuilder rowString = new StringBuilder();
            row.forEach(x -> rowString.append(x + ","));
            rowString.append(labeledFeatureVector.isActivation()?"true":"false");

            if(header.size() == row.size()) {
                data.append(rowString.toString() + System.lineSeparator());

                // rows list is used later for unary class cases check
                row.add(labeledFeatureVector.isActivation()?"true":"false");
                rows.add(row);
            }
        }

        // TODO: Optimize memory performance, currently data is stored in two similar containers

        // throwing Exception if unary class case detected
        if (rows.stream().allMatch(x -> x.get(x.size() - 1).equals("true")) ||
            rows.stream().allMatch(x -> x.get(x.size() - 1).equals("false")))
            throw new Exception("Unary class");

        // converting the string with CSV structure into stream
        InputStream stream = new ByteArrayInputStream(data.toString().getBytes(StandardCharsets.UTF_8));;

        // using CSVLoader to transform CSV-structured input stream into Weka's DataSet
        CSVLoader csv = new CSVLoader();
        csv.setSource(stream);

        return csv.getDataSet();
    }

    public static List<TreeBranch> parseJ48Tree(String J48TreeOutputString) throws Exception{
        List<TreeBranch> branches = new ArrayList<TreeBranch>();

        Pattern nodePattern = Pattern.compile("(\\w+) ([><=]+) ([\\d\\w.]+)");
        Pattern leafPattern = Pattern.compile("(\\w+) ([><=]+) ([\\d\\w.]+):");

        String[] splittedJ48TreeOutputStrings = J48TreeOutputString.split(System.getProperty("line.separator"));

        // cut anything but the tree
        String[] J48TreeStrings =
                ListHelper.sliceArray(splittedJ48TreeOutputStrings, 2, splittedJ48TreeOutputStrings.length - 5);

        // if single-element tree
        if (J48TreeStrings.length == 1) {
            Pattern stumpPattern = Pattern.compile(": (\\w+) \\(([\\d.]+)\\/([\\d.]+)\\)");
            Matcher stumpMatcher = stumpPattern.matcher(J48TreeStrings[0]);


            if(stumpMatcher.find()) {
                TreeBranch branch = new TreeBranch();
                branch.setTotal(Double.parseDouble(stumpMatcher.group(2)));
                branch.setMisclassified(Double.parseDouble(stumpMatcher.group(3)));

                if(stumpMatcher.group(1).toLowerCase().equals("true")) branch.setOutcome(true);
                else branch.setOutcome(false);

                branches.add(branch);
            }

            return branches;
        }

        // if multi-element tree
        TreeBranch prefixConditions = new TreeBranch();
        int previousDepth = 0;
        for (String J48TreeLine : J48TreeStrings) {
            Matcher leafMatcher = leafPattern.matcher(J48TreeLine);
            Matcher nodeMatcher = nodePattern.matcher(J48TreeLine);

            int currentDepth = (int)J48TreeLine.chars().filter(x -> x == '|').count();

            if( previousDepth < currentDepth) {
                if(leafMatcher.find(0)) {
                    branches.add(extractConditionFromLeaf(J48TreeLine, prefixConditions));
                } else if (nodeMatcher.find(0)) {
                    prefixConditions.addCondition(nodeMatcher.group(1), nodeMatcher.group(2), nodeMatcher.group(3));
                }

                previousDepth = currentDepth;
            } else if( currentDepth == previousDepth) {
                if(leafMatcher.find(0)) {
                    branches.add(extractConditionFromLeaf(J48TreeLine, prefixConditions));
                } else if (nodeMatcher.find(0)) {
                    prefixConditions.addCondition(nodeMatcher.group(1), nodeMatcher.group(2), nodeMatcher.group(3));
                }
            } else if (currentDepth < previousDepth) {
                prefixConditions.removeLast();

                if (leafMatcher.find(0)) {
                    branches.add(extractConditionFromLeaf(J48TreeLine, prefixConditions));
                } else if (nodeMatcher.find()) {
                    prefixConditions.addCondition(nodeMatcher.group(1), nodeMatcher.group(2), nodeMatcher.group(3));
                }

                previousDepth = currentDepth;
            }

        }

        return branches;
    }

    private static TreeBranch extractConditionFromLeaf(String string, TreeBranch prefixConditions) {
        Pattern oneArgumentPattern = Pattern.compile("(\\w+) ([><=]+) ([\\d\\w.]+): (\\w+) \\(([\\d.]+)\\)");
        Pattern twoArgumentPattern = Pattern.compile("(\\w+) ([><=]+) ([\\d\\w.]+): (\\w+) \\(([\\d.]+)\\/([\\d.]+)\\)");

        Matcher oneArgumentMatcher = oneArgumentPattern.matcher(string);
        Matcher twoArgumentMatcher = twoArgumentPattern.matcher(string);

        TreeBranch branch = new TreeBranch(prefixConditions.getConditions());

        if(oneArgumentMatcher.find(0)) {
            branch.addCondition(oneArgumentMatcher.group(1),oneArgumentMatcher.group(2),oneArgumentMatcher.group(3));
            branch.setTotal(Double.parseDouble(oneArgumentMatcher.group(5)));

            if(oneArgumentMatcher.group(4).toLowerCase().equals("true")) branch.setOutcome(true);
            else branch.setOutcome(false);

        } else if(twoArgumentMatcher.find(0)) {
            branch.addCondition(twoArgumentMatcher.group(1),twoArgumentMatcher.group(2),twoArgumentMatcher.group(3));
            branch.setTotal(Double.parseDouble(twoArgumentMatcher.group(5)));
            branch.setMisclassified(Double.parseDouble(twoArgumentMatcher.group(6)));

            if(twoArgumentMatcher.group(4).toLowerCase().equals("true")) branch.setOutcome(true);
            else branch.setOutcome(false);
        }

        return branch;
    }
}
