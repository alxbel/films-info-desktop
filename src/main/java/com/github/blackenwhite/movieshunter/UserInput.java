package com.github.blackenwhite.movieshunter;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created on 02.10.2015.
 */
public class UserInput {
    private String startYear;
    private String endYear;
    private String startMonth;
    private String endMonth;
    private ArrayList<String> months;
    private int startMonthIndex;
    private int endMonthIndex;

    public UserInput(ArrayList<HashMap<String, String>> input) {
        startYear = input.get(0).get("startYear");
        endYear = input.get(0).get("endYear");
        startMonth = input.get(1).get("startMonth");
        endMonth = input.get(1).get("endMonth");
        months = new ArrayList<>();
        for (String month : new DateFormatSymbols(Locale.ENGLISH).getMonths()) {
            months.add(month);
        }
        startMonthIndex = months.indexOf(startMonth);
        endMonthIndex = months.indexOf(endMonth);
    }

    public int getStartYear() {
        return Integer.parseInt(startYear);
    }

    public int getEndYear() {
        return Integer.parseInt(endYear);
    }

    public String getStartMonth() {
        return startMonth;
    }

    public String getEndMonth() {
        return endMonth;
    }

    public int getStartMonthIndex() {
        return startMonthIndex;
    }

    public int getEndMonthIndex() {
        return endMonthIndex;
    }

    @Override
    public String toString() {
        return String.format("[%s(%d) %s] - [%s(%d) %s]",
                startMonth, startMonthIndex, startYear,
                endMonth, endMonthIndex, endYear);
    }
}
