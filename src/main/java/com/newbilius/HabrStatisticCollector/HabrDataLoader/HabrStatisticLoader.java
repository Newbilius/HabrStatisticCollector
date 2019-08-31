package com.newbilius.HabrStatisticCollector.HabrDataLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HabrStatisticLoader {
    private final String url;
    private final String yearFrom;
    private final String yearTo;
    private final boolean skipTags;
    private ILoadingProcessCallback loadingProcessCallback;

    public HabrStatisticLoader(String url,
                               String yearFrom,
                               String yearTo,
                               boolean skipTags,
                               ILoadingProcessCallback loadingProcessCallback) {
        this.url = url;
        this.yearFrom = yearFrom;
        this.yearTo = yearTo;
        this.skipTags = skipTags;
        this.loadingProcessCallback = loadingProcessCallback;
    }

    //да-да-да, очень плохо смешивать сразу две ответственности, но у нас тут нужно для парсинг данных управляет дальнейшей загрузкой, так что я пока даже не знаю...
    public HabrItem[] loadAndParseData() {
        var parsedItems = new ArrayList<HabrItem>();

        var nextPage = url;
        while (!nextPage.isBlank())
            nextPage = loadPage(nextPage, parsedItems);

        return parsedItems.toArray(new HabrItem[0]);
    }

    private String loadPage(String url,
                            ArrayList<HabrItem> parsedItems) {
        try {
            loadingProcessCallback.print(String.format("Запрашиваем страницу %s", url));
            var page = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .get();
            var items = page.select("ul.content-list li.content-list__item article");
            for (var item : items) {
                var parsedItem = new HabrItem();
                var articleUrl = item.select("h2.post__title a").first().attr("href");
                var date = item.select("span.post__time").text().toLowerCase();

                if (parsedItems.isEmpty()
                        && !date.contains(yearFrom)
                        && !date.contains("сегодня")
                        && !date.contains("вчера")) {
                    loadingProcessCallback.print("Пропустили статью - пока не дошли до начального года");
                    continue;
                }

                if (date.contains(yearTo)) {
                    return "";
                }

                loadingProcessCallback.print(String.format("Обрабатываем статью номер %s", parsedItems.size() + 1));

                parsedItem.Title = item.select("h2.post__title").text();
                parsedItem.Author = item.select("header span.user-info__nickname").text();
                parsedItem.DateTime = PrepareDateTime(date);
                parsedItem.Url = articleUrl;
                parsedItem.Bookmarks = parseInt(item.select("span.bookmark__counter").text());
                parsedItem.Views = parseInt(item.select("span.post-stats__views-count").text());
                parsedItem.Comments = parseInt(item.select("span.post-stats__comments-count").text());
                parsedItem.Score = parseInt(item.select("span.voting-wjt__counter").text());
                parsedItem.Hubs = item.select("ul.post__hubs li.inline-list__item_hub")
                        .stream()
                        .map(hab -> hab.select("a.hub-link").text())
                        .toArray(String[]::new);

                if (!skipTags) {
                    loadingProcessCallback.print(String.format("Качаем тэги к статье номер %s", parsedItems.size() + 1));
                    var pageContent = Jsoup.connect(articleUrl).get();
                    parsedItem.Tags = pageContent.select("ul.inline-list_fav-tags li")
                            .stream()
                            .map(Element::text)
                            .toArray(String[]::new);
                }

                parsedItems.add(parsedItem);
            }

            var nextPage = page.select("a#next_page")
                    .first();
            if (nextPage != null)
                return "https://habr.com" + nextPage.attr("href");
            else
                return "";

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
    private final Date currentDate = new Date();
    private Date yesterdayDate;

    private String PrepareDateTime(String date) {
        if (date.contains("сегодня"))
            return outputDateFormat.format(currentDate);
        if (date.contains("вчера")) {
            if (yesterdayDate == null) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -1);
                yesterdayDate = cal.getTime();
            }
            return outputDateFormat.format(yesterdayDate);
        }
        try {
            return outputDateFormat.format(inputDateFormat.parse(date));
        } catch (ParseException e) {
            return date;
        }
    }

    private static int parseInt(String text) {
        if (text.contains("k")) {
            var changedText = text
                    .replace("k", "")
                    .replace(",", ".");
            return (int) (Float.parseFloat(changedText) * 1000);
        }
        var isNegative = false;
        if (text.contains("–")) {
            text = text
                    .replace("–", "");
            isNegative = true;
        }

        if (text.isBlank())
            return 0;

        if (isNegative)
            return 0 - Integer.parseInt(text);
        return Integer.parseInt(text);
    }

    public interface ILoadingProcessCallback {
        void print(String text);
    }
}