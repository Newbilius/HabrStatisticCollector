package com.newbilius.HabrStatisticCollector.AnalyticsGenerators;

import com.newbilius.HabrStatisticCollector.CSVWriteHelper;
import com.newbilius.HabrStatisticCollector.HabrItem;

import java.io.IOException;

public class FullTable implements IAnalyticsGenerator {
    @Override
    public void generate(HabrItem[] items) {
        try {
            try (var csvWriter = CSVWriteHelper.createCSVWriter("Статьи с основными параметрами.csv")) {
                csvWriter.writeNext(new String[]{
                        "Заголовок",
                        "Плюсы",
                        "Закладки",
                        "Комментарии",
                        "Просмотры",
                        "URL",
                        "Автор",
                });

                for (var item : items) {
                    csvWriter.writeNext(new String[]{
                            item.Title,
                            String.valueOf(item.Score),
                            String.valueOf(item.Bookmarks),
                            String.valueOf(item.Comments),
                            String.valueOf(item.Views),
                            item.Url,
                            item.Author
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}