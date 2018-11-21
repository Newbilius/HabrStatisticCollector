package com.newbilius.HabrStatisticCollector.AnalyticsGenerators;

import com.newbilius.HabrStatisticCollector.HabrDataLoader.HabrItem;

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