package com.newbilius.HabrStatisticCollector;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CSVWriteHelper {
    public static CSVWriter createCSVWriter(String fileName) throws IOException {
        return new CSVWriter(new FileWriter(fileName,
                StandardCharsets.UTF_8,
                false), ';');
    }
}