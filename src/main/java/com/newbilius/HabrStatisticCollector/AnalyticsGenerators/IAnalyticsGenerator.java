package com.newbilius.HabrStatisticCollector.AnalyticsGenerators;

import com.newbilius.HabrStatisticCollector.HabrItem;

public interface IAnalyticsGenerator {
    void generate(HabrItem[] items);
}