package com.newbilius.HabrStatisticCollector.AnalyticsGenerators;

import com.newbilius.HabrStatisticCollector.CSVWriteHelper;
import com.newbilius.HabrStatisticCollector.HabrItem;

import java.io.IOException;
import java.util.HashMap;


public class TagsTable extends SummaryBySomethingInfo {
    @Override
    public String getSomethingName() {
        return "Тэг";
    }

    @Override
    public String getFileName() {
        return "Популярные тэги";
    }

    @Override
    public String[] getSomethingValues(HabrItem item) {
        return item.Tags;
    }
}
