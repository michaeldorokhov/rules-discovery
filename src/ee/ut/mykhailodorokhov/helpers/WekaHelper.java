package ee.ut.mykhailodorokhov.helpers;

import ee.ut.mykhailodorokhov.data.LabeledFeatureVector;
import ee.ut.mykhailodorokhov.data.Rule;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WekaHelper {
    public static Instances toWekaDataSet(Rule rule, List<LabeledFeatureVector> labeledFeatureVectors) throws Exception {
        StringBuilder data = new StringBuilder();

        // getting header

        StringBuilder header = new StringBuilder();
        labeledFeatureVectors.get(0).getAttributes().keySet().
                stream().forEach(x -> header.append(x + ","));

        header.append("outcome");
        data.append(header.toString() + System.lineSeparator());

        // getting data rows

        for (LabeledFeatureVector labeledFeatureVector : labeledFeatureVectors.stream().filter(x -> x.getRule().equals(rule)).collect(Collectors.toList())) {
            StringBuilder row = new StringBuilder();

            for(Map.Entry<String, String> entry : labeledFeatureVector.getAttributes().entrySet()){
                row.append(entry.getValue() + ",");
            }

            row.append(labeledFeatureVector.isActivation()?"true":"false");

            data.append(row.toString() + System.lineSeparator());
        }

        // converting the string with CSV-structured into stream
        InputStream stream = new ByteArrayInputStream(data.toString().getBytes(StandardCharsets.UTF_8));;

        // using CSVLoader to transform CSV-structured input stream into Weka's DataSet
        CSVLoader csv = new CSVLoader();
        csv.setSource(stream);

        return csv.getDataSet();
    }
}
