package com.github.blackenwhite.movieshunter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * The {@code Utils} class contains logger methods
 * and methods for grabbing (getting) url resources,
 * such as json files and html pages
 *
 * @since 28.01.2015
 */
public class Utils {
    public static void logDebug(Object o) {
        if (Constants.Logs.Debug.ENABLED == true) {
            System.out.println(String.format(Constants.Logs.FORMAT, Constants.Logs.Debug.TITLE, new Date(), o));
        }
    }

    public static void logErr(Object o) {
        if (Constants.Logs.Errors.ENABLED == true) {
            System.out.println(String.format(Constants.Logs.FORMAT, Constants.Logs.Errors.TITLE, new Date(), o));
        }
    }

    public static void logInfo(Object o) {
        if (Constants.Logs.Info.ENABLED == true) {
            System.out.println(String.format(Constants.Logs.FORMAT, Constants.Logs.Info.TITLE, new Date(), o));
        }
    }

    public static int getCurrentYear() {
        GregorianCalendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        return year;
    }

    public static int getCurrentMonth() {
        GregorianCalendar calendar = new GregorianCalendar();
        int month = calendar.get(Calendar.MONTH);
        return month;
    }
}
