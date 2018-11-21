package com.newbilius.HabrStatisticCollector;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.newbilius.HabrStatisticCollector.AnalyticsGenerators.IAnalyticsGenerator;
import com.newbilius.HabrStatisticCollector.CommandLineParser.CommandLineArgumentsParser;
import com.newbilius.HabrStatisticCollector.CommandLineParser.CommandLineArgumentsParserResult;
import com.newbilius.HabrStatisticCollector.CommandLineParser.Option;
import com.newbilius.HabrStatisticCollector.HabrDataLoader.HabrItem;
import com.newbilius.HabrStatisticCollector.HabrDataLoader.HabrStatisticLoader;
import org.reflections.Reflections;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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

        if (jsonFileForLoading == null || jsonFileForLoading.isBlank()) {
            parsedItems = new HabrStatisticLoader(url, yearFrom, yearTo, skipTags,
                    HabrStatisticCollector::print).loadAndParseData();
            if (saveJson)
                saveParsedItems(parsedItems);
        } else
            parsedItems = loadDataFromJsonFile(parsedItems);

        callStatisticGenerators(parsedItems);
    }

    private static void callStatisticGenerators(HabrItem[] parsedItems) {
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

    private static HabrItem[] loadDataFromJsonFile(HabrItem[] parsedItems) {
        Gson gson = new Gson();
        try {
            var reader = new JsonReader(new FileReader(jsonFileForLoading));
            parsedItems = gson.fromJson(reader, parsedItems.getClass());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return parsedItems;
    }

    private static void saveParsedItems(HabrItem[] parsedItems) {
        var json = new Gson().toJson(parsedItems);
        try (PrintWriter pw = new PrintWriter("ARTICLES.json")) {
            pw.println(json);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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