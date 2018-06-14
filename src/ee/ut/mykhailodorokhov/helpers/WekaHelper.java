package ee.ut.mykhailodorokhov.helpers;

import com.sun.deploy.util.StringUtils;
import ee.ut.mykhailodorokhov.data.LabeledFeatureVector;
import ee.ut.mykhailodorokhov.data.Rule;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WekaHelper {
    public static Instances toWekaDataSet(List<LabeledFeatureVector> labeledFeatureVectors) throws Exception {
        StringBuilder data = new StringBuilder();

        // getting header
        List<String> header = new ArrayList<String>();
        labeledFeatureVectors.get(0).getAttributes().keySet().forEach(x -> header.add(x));

        // converting header into teh CSV format
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
        if (rows.stream().allMatch(x -> x.get(x.size() - 1).equals("true")) || rows.stream().allMatch(x -> x.get(x.size() - 1).equals("false")))
            throw new Exception("Unary class");

        // converting the string with CSV structure into stream
        InputStream stream = new ByteArrayInputStream(data.toString().getBytes(StandardCharsets.UTF_8));;

        // using CSVLoader to transform CSV-structured input stream into Weka's DataSet
        CSVLoader csv = new CSVLoader();
        csv.setSource(stream);

        return csv.getDataSet();
    }
}
