package com.newbilius.HabrStatisticCollector;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.newbilius.HabrStatisticCollector.HabrDataLoader.HabrItem;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class HabrDataJsonFileManager {

    public static HabrItem[] loadDataFromJsonFile(String fileName) {
        HabrItem[] parsedItems = new HabrItem[0];
        Gson gson = new Gson();
        try {
            var reader = new JsonReader(new FileReader(fileName, StandardCharsets.UTF_8));
            return gson.fromJson(reader, parsedItems.getClass());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return parsedItems;
    }

    public static void saveParsedItems(String fileName, HabrItem[] parsedItems) {
        var json = new Gson().toJson(parsedItems);
        try (PrintWriter pw = new PrintWriter("ARTICLES.json",
                StandardCharsets.UTF_8)) {
            pw.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}