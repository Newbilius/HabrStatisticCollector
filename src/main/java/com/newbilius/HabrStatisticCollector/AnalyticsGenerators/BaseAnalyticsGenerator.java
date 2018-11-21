package com.newbilius.HabrStatisticCollector.AnalyticsGenerators;

import com.newbilius.HabrStatisticCollector.CSVWriteHelper;
import com.newbilius.HabrStatisticCollector.HabrDataLoader.HabrItem;

import java.io.IOException;

public abstract class BaseAnalyticsGenerator implements IAnalyticsGenerator {
    public abstract String fileName();

    public abstract String[] getHeaders();

    public abstract String[] getData(HabrItem item);

    @Override
    public void generate(HabrItem[] items) {
        try {
            try (var csvWriter = CSVWriteHelper.createCSVWriter(fileName() + ".csv")) {
                csvWriter.writeNext(getHeaders());

                for (var item : items)
                    csvWriter.writeNext(getData(item));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}