package com.newbilius.HabrStatisticCollector.AnalyticsGenerators;

import com.newbilius.HabrStatisticCollector.HabrDataLoader.HabrItem;

public class AuthorTable extends SummaryBySomethingInfo {
    @Override
    public String getSomethingName() {
        return "Автор";
    }

    @Override
    public String getFileName() {
        return "Популярные авторы";
    }

    @Override
    public String[] getSomethingValues(HabrItem item) {
        return new String[]
                {
                        item.Author
                };
    }
}