package com.github.matsud224.waveviz;

import org.jruby.RubyProc;

import java.util.HashMap;

public class Waveviz {
    private HashMap<String, RubyProc> formatters = new HashMap<>();

    public void registerFormatter(String name, RubyProc proc) {
        formatters.put(name, proc);
        System.out.printf("Formatter \"%s\" is registered.\n", name);
    }

    public HashMap<String, RubyProc> getFormatters() {
        return formatters;
    }
}
