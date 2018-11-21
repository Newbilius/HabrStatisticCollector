package com.newbilius.HabrStatisticCollector.CommandLineParser;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandLineArgumentsParser {
    private ArrayList<Option> options = new ArrayList<>();

    public void addOption(Option option) {
        options.add(option);
    }

    public CommandLineArgumentsParserResult parse(String[] args)
            throws CommandLineArgumentsParseException {

        exitIfOnlyHelpParam(args);

        var result = new CommandLineArgumentsParserResult();
        for (var i = 0; i < args.length; i++) {
            var arg = args[i].toLowerCase().trim();
            var commandOptional = options.stream()
                    .filter(x -> ("-" + x.param.toLowerCase()).equals(arg)
                            || ("--" + x.param.toLowerCase()).equals(arg))
                    .findFirst();

            if (!commandOptional.isPresent())
                throw new CommandLineArgumentsParseException(getUnknownOptionExceptionText(arg));
            var command = commandOptional.get();
            if (command.withArgs) {
                if (i + 1 >= args.length)
                    throw new CommandLineArgumentsParseException(notSettedParamOfOptionExceptionText(arg));
                var argParam = args[i + 1];
                if (argParam.startsWith("-"))
                    throw new CommandLineArgumentsParseException(notSettedParamOfOptionExceptionText(arg));
                i++;
                result.addOption(command.param, argParam);
            } else {
                result.addOption(command.param);
            }
        }

        for (var option : options) {
            if (option.required && !result.haveValue(option))
                throw new CommandLineArgumentsParseException(notSettedRequiredParamExceptionText(option.param));
        }

        return result;
    }

    private void exitIfOnlyHelpParam(String[] args) throws CommandLineArgumentsParseException {
        if (Arrays.stream(args)
                .anyMatch(s -> s.contains("-help")
                        || s.contains("-h")
                        || s.contains("-?")))
            throw new CommandLineArgumentsParseException(helpExceptionText());

        if (args.length == 1
                && (args[0].equals("?")
                || args[0].equals("h")
                || args[0].contains("help")))
            throw new CommandLineArgumentsParseException(helpExceptionText());

        if (args.length == 0)
            throw new CommandLineArgumentsParseException(helpExceptionText());
    }

    public void printHelp() {
        printHelpHeader();
        for (var option : options)
            System.out.println(getHelpTextLine(option));
    }

    //выделены для потенциальной локализации

    @SuppressWarnings("WeakerAccess")
    protected String helpExceptionText() {
        return "Помощь по использованию";
    }

    @SuppressWarnings("WeakerAccess")
    protected String notSettedRequiredParamExceptionText(String arg) {
        return String.format("Не указан обязательный параметр %s", arg);
    }

    @SuppressWarnings("WeakerAccess")
    protected String notSettedParamOfOptionExceptionText(String arg) {
        return String.format("Не указан параметр опции %s", arg);
    }

    @SuppressWarnings("WeakerAccess")
    protected String getUnknownOptionExceptionText(String arg) {
        return String.format("Неизвестный параметр: %s", arg);
    }

    @SuppressWarnings("WeakerAccess")
    protected String getHelpTextLine(Option option) {
        return String.format("%-20s\t\t\t%s%s",
                "-" + option.param + (option.withArgs ? " <аргумент>" : ""),
                option.required ? "[обязательный] " : "",
                option.description);
    }

    @SuppressWarnings("WeakerAccess")
    protected void printHelpHeader() {
        System.out.println("Параметры:");
    }
}

