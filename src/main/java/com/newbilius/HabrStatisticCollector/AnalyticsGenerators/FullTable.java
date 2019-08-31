package com.newbilius.HabrStatisticCollector.AnalyticsGenerators;

import com.newbilius.HabrStatisticCollector.HabrDataLoader.HabrItem;

public class FullTable extends BaseAnalyticsGenerator {
    @Override
    public String fileName() {
        return "Статьи с основными параметрами";
    }

    @Override
    public String[] getHeaders() {
        return new String[]{
                "Заголовок",
                "Плюсы",
                "Закладки",
                "Комментарии",
                "Просмотры",
                "URL",
                "Автор",
                "Дата"
        };
    }

    @Override
    public String[] getData(HabrItem item) {
        return new String[]{
                item.Title,
                String.valueOf(item.Score),
                String.valueOf(item.Bookmarks),
                String.valueOf(item.Comments),
                String.valueOf(item.Views),
                item.Url,
                item.Author,
                item.DateTime
        };
    }
}