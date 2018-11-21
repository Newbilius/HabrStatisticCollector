package com.newbilius.HabrStatisticCollector;

public class Option {
    public final String param;
    public final boolean required;
    public boolean withArgs;
    public final String description;

    public Option(String param,
                  boolean required,
                  boolean withArgs,
                  String description) {
        this.param = param;
        this.required = required;
        this.withArgs = withArgs;
        this.description = description;
    }
}
