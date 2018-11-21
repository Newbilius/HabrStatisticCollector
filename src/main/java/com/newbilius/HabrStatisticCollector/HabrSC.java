package com.newbilius.HabrStatisticCollector;

import com.newbilius.HabrStatisticCollector.CommandLineParser.CommandLineArgumentsParseException;
import com.newbilius.HabrStatisticCollector.CommandLineParser.CommandLineArgumentsParser;
import com.newbilius.HabrStatisticCollector.CommandLineParser.CommandLineArgumentsParserResult;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Calendar;

public class HabrSC {

    private static String url;
    private static int currentYear;
    private static int yearFrom;
    private static int yearTo;
    private static boolean saveJson;

    public static void main(String[] args) {
        prepareParams(args);
        parseData();
    }

    private static void parseData() {
        try {
            var page = Jsoup.connect(url).get();
            var items = page.select("div.company_blog ul.content-list li.content-list__item article");
            for (var item :
                    items) {
                var author = item.select("header span.user-info__nickname").text();
                var dateTimeText = item.select("span.post__time").text();
                var title = item.select("h2.post__title").text();
                var postUrl = item.select("h2.post__title a").first().attr("href");

                var hubs = item.select("ul.post__hubs li.inline-list__item_hub");
                for (var hab : hubs) {
                    var habUrl = hab.select("a.hub-link").first().attr("href");
                    var habTitle = hab.select("a.hub-link").text();
                    print(habTitle + " = " + habUrl);
                }

                var bookmarks = item.select("span.bookmark__counter").text();
                var views = item.select("span.post-stats__views-count").text();
                var counter = item.select("span.voting-wjt__counter").text();
                var comments = item.select("span.voting-wjt__counter").text();

                if (views.contains("k")) {
                    views = views.replace("k", "").replace(",", ".");
                    views = String.valueOf((int) (Float.parseFloat(views) * 1000));
                }

                var pageContent = Jsoup.connect(postUrl).get();
                var tags = pageContent.select("ul.inline-list_fav-tags li");
                for (var tag : tags) {
                    print("tag " + tag.text());
                }

                print(author);
                print(dateTimeText);
                print(title + " " + postUrl);
                print("views " + views);
                print("bookmarks " + bookmarks);
                print("counter " + counter);
                print("comments " + comments);
                print("---------------------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void prepareParams(String[] args) {
        var argsParser = new CommandLineArgumentsParser();

        var urlOption = new Option("url", true, true, "путь к блогу (пример: https://habr.com/company/skbkontur/)");
        argsParser.addOption(urlOption);

        var yearFromOption = new Option("yearFrom", false, true, "год начала парсинга (пример: 2018, по-умолчанию - текущий год)");
        argsParser.addOption(yearFromOption);

        var yearToOption = new Option("yearTo", false, true, "год окончания парсинга (пример: 2017, по-умолчанию - текущий год-1)");
        argsParser.addOption(yearToOption);

        var noJsonOption = new Option("notSaveJson", false, false, "не сохранять JSON с промежуточными данными");
        argsParser.addOption(noJsonOption);

        CommandLineArgumentsParserResult cmd = null;
        try {
            cmd = argsParser.parse(args);
        } catch (CommandLineArgumentsParseException e) {
            print(e.getMessage());
            argsParser.printHelp();
        }

        if (cmd == null)
            return;

        url = cmd.getString(urlOption, "");
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearFrom = cmd.getInt(yearFromOption, currentYear);
        yearTo = cmd.getInt(yearToOption, currentYear - 1);
        saveJson = cmd.getBoolean(noJsonOption, true);
    }

    private static void print(String text) {
        System.out.println(text);
    }
}