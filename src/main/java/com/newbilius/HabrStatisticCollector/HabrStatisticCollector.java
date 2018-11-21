package com.newbilius.HabrStatisticCollector;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.newbilius.HabrStatisticCollector.AnalyticsGenerators.IAnalyticsGenerator;
import com.newbilius.HabrStatisticCollector.CommandLineParser.CommandLineArgumentsParser;
import com.newbilius.HabrStatisticCollector.CommandLineParser.CommandLineArgumentsParserResult;
import com.newbilius.HabrStatisticCollector.CommandLineParser.Option;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.reflections.Reflections;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;

public class HabrStatisticCollector {

    private static String url;
    private static String yearFrom;
    private static String yearTo;
    private static boolean saveJson;
    private static boolean skipTags;
    private static String jsonFileForLoading;

    public static void main(String[] args) {
        prepareOptions(args);

        HabrItem[] parsedItems = new HabrItem[0];

        if (jsonFileForLoading.isBlank()) {
            parsedItems = parseData();
            if (saveJson)
                saveParsedItems(parsedItems);
        } else {
            Gson gson = new Gson();
            try {
                var reader = new JsonReader(new FileReader(jsonFileForLoading));
                parsedItems = gson.fromJson(reader, parsedItems.getClass());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        var generators = new Reflections("com.newbilius").getSubTypesOf(IAnalyticsGenerator.class);
        for (var generator : generators) {
            try {
                if (!Modifier.isAbstract(generator.getModifiers())) {
                    var generatorInstance = generator.getDeclaredConstructor().newInstance();
                    generatorInstance.generate(parsedItems);
                }
            } catch (NoSuchMethodException
                    | IllegalAccessException
                    | InstantiationException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveParsedItems(HabrItem[] parsedItems) {
        var json = new Gson().toJson(parsedItems);
        try (PrintWriter pw = new PrintWriter("ARTICLES.json")) {
            pw.println(json);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static HabrItem[] parseData() {
        var parsedItems = new ArrayList<HabrItem>();

        var nextPage = url;
        while (!nextPage.isBlank())
            nextPage = loadPage(nextPage, parsedItems);

        return parsedItems.toArray(new HabrItem[0]);
    }

    private static String loadPage(String url,
                                   ArrayList<HabrItem> parsedItems) {
        try {
            print(String.format("Запрашиваем страницу %s", url));
            var page = Jsoup.connect(url).get();
            var items = page.select("div.company_blog ul.content-list li.content-list__item article");
            for (var item : items) {
                var parsedItem = new HabrItem();
                var articleUrl = item.select("h2.post__title a").first().attr("href");
                var date = item.select("span.post__time").text();

                if (parsedItems.isEmpty() && !date.contains(yearFrom)) {
                    print("Пропустили статью - пока не дошли до начального года");
                    continue;
                }

                if (date.contains(yearTo)) {
                    return "";
                }

                print(String.format("Обрабатываем статью номер %s", parsedItems.size() + 1));

                parsedItem.Title = item.select("h2.post__title").text();
                parsedItem.Author = item.select("header span.user-info__nickname").text();
                parsedItem.DateTime = date;
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
                    print(String.format("Качаем тэги к статье номер %s", parsedItems.size() + 1));
                    var pageContent = Jsoup.connect(articleUrl).get();
                    parsedItem.Tags = pageContent.select("ul.inline-list_fav-tags li")
                            .stream()
                            .map(Element::text)
                            .toArray(String[]::new);
                }

                parsedItems.add(parsedItem);
            }

            return "https://habr.com" + page.select("a#next_page")
                    .first()
                    .attr("href");

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static int parseInt(String text) {
        if (text.contains("k")) {
            var changedText = text
                    .replace("k", "")
                    .replace(",", ".");
            return (int) (Float.parseFloat(changedText) * 1000);
        }
        if (text.isBlank())
            return 0;
        return Integer.parseInt(text);
    }

    private static void prepareOptions(String[] args) {
        var argsParser = new CommandLineArgumentsParser();

        var urlOption = new Option("url", true, true, "путь к блогу (пример: https://habr.com/company/skbkontur/)");
        argsParser.addOption(urlOption);

        var yearFromOption = new Option("yearFrom", false, true, "год начала парсинга (пример: 2018, по-умолчанию - текущий год)");
        argsParser.addOption(yearFromOption);

        var yearToOption = new Option("yearTo", false, true, "год окончания парсинга (пример: 2017, по-умолчанию - текущий год-1)");
        argsParser.addOption(yearToOption);

        var noJsonOption = new Option("notSaveJson", false, false, "не сохранять JSON с промежуточными данными");
        argsParser.addOption(noJsonOption);

        var skipTagsOption = new Option("skipTags", false, false, "не загружает каждую статью для получения тегов");
        argsParser.addOption(skipTagsOption);

        var loadFromJsonOption = new Option("loadJson", false, true, "построить статистику по заранее загруженному JSON");
        argsParser.addOption(loadFromJsonOption);

        CommandLineArgumentsParserResult cmd = argsParser.parse(args);

        if (cmd.haveValue(loadFromJsonOption)) {
            jsonFileForLoading = cmd.getString(loadFromJsonOption, "");
            if (!jsonFileForLoading.isBlank())
                return;
        }

        if (cmd.haveError()) {
            for (var error : cmd.getErrors()) {
                print(error);
            }
            argsParser.printHelp();
            System.exit(1);
        }

        url = cmd.getString(urlOption, "");
        var currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearFrom = String.valueOf(cmd.getInt(yearFromOption, currentYear));
        yearTo = String.valueOf(cmd.getInt(yearToOption, currentYear - 1));
        saveJson = !cmd.haveValue(noJsonOption);
        skipTags = cmd.haveValue(skipTagsOption);
    }

    private static void print(String text) {
        System.out.println(text);
    }
}