package com.newbilius.HabrStatisticCollector.AnalyticsGenerators;

import com.newbilius.HabrStatisticCollector.HabrDataLoader.HabrItem;

public interface IAnalyticsGenerator {
    void generate(HabrItem[] items);
}