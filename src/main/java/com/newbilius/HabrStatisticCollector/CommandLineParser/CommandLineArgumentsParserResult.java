package com.newbilius.HabrStatisticCollector.CommandLineParser;

import java.util.HashMap;

public class CommandLineArgumentsParserResult {
    private HashMap<String, String> options = new HashMap<>();

    public void addOption(String arg, String argParam) {
        options.put(arg, argParam);
    }

    public void addOption(String arg) {
        options.put(arg, "");
    }

    public boolean haveValue(Option option) {
        return options.containsKey(option.param);
    }

    public boolean getBoolean(Option option, boolean defaultValue) {
        if (options.containsKey(option.param))
            return Boolean.parseBoolean(options.get(option.param));
        return defaultValue;
    }

    public String getString(Option option, String defaultValue) {
        return options.getOrDefault(option.param, defaultValue);
    }

    public int getInt(Option option, int defaultValue) {
        if (options.containsKey(option.param))
            return Integer.parseInt(options.get(option.param));
        return defaultValue;
    }
}