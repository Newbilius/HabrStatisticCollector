package com.newbilius.HabrStatisticCollector.AnalyticsGenerators;

import com.newbilius.HabrStatisticCollector.CSVWriteHelper;
import com.newbilius.HabrStatisticCollector.HabrItem;

import java.io.IOException;
import java.util.HashMap;

public class HubsTable extends SummaryBySomethingInfo {
    @Override
    public String getSomethingName() {
        return "Хаб";
    }

    @Override
    public String getFileName() {
        return "Популярные хабы";
    }

    @Override
    public String[] getSomethingValues(HabrItem item) {
        return item.Hubs;
    }
}