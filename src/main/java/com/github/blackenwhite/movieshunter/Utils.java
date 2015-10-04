package com.github.blackenwhite.movieshunter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    public static void hackTooltipTiming(Tooltip tooltip) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field activationTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            activationTimer.setAccessible(true);
            Timeline objActivationTimer = (Timeline) activationTimer.get(objBehavior);

            objActivationTimer.getKeyFrames().clear();
            objActivationTimer.getKeyFrames().add(new KeyFrame(new Duration(2000)));

            Field hideTimer = objBehavior.getClass().getDeclaredField("hideTimer");
            hideTimer.setAccessible(true);
            Timeline objHideTimer = (Timeline) hideTimer.get(objBehavior);

            objHideTimer.getKeyFrames().clear();
            objHideTimer.getKeyFrames().add(new KeyFrame(new Duration(10000)));

            Field leftTimer = objBehavior.getClass().getDeclaredField("leftTimer");
            leftTimer.setAccessible(true);
            Timeline objLeftTimer = (Timeline) leftTimer.get(objBehavior);

            objLeftTimer.getKeyFrames().clear();
            objLeftTimer.getKeyFrames().add(new KeyFrame(new Duration(1)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Tooltip behavior is controlled by a private class javafx.scene.control.Tooltip$TooltipBehavior.
     * All Tooltips share the same TooltipBehavior instance via a static private member BEHAVIOR, which
     * has default values of 1sec for opening, 5secs visible, and 200 ms close delay (if mouse exits from node before 5secs).
     *
     * The hack below constructs a custom instance of TooltipBehavior and replaces private member BEHAVIOR with
     * this custom instance.
     * </p>
     *
     */
    public static void setupCustomTooltipBehavior(int openDelayInMillis, int visibleDurationInMillis, int closeDelayInMillis) {
        try {

            Class TTBehaviourClass = null;
            Class<?>[] declaredClasses = Tooltip.class.getDeclaredClasses();
            for (Class c:declaredClasses) {
                if (c.getCanonicalName().equals("javafx.scene.control.Tooltip.TooltipBehavior")) {
                    TTBehaviourClass = c;
                    break;
                }
            }
            if (TTBehaviourClass == null) {
                // abort
                return;
            }
            Constructor constructor = TTBehaviourClass.getDeclaredConstructor(
                    Duration.class, Duration.class, Duration.class, boolean.class);
            if (constructor == null) {
                // abort
                return;
            }
            constructor.setAccessible(true);
            Object newTTBehaviour = constructor.newInstance(
                    new Duration(openDelayInMillis), new Duration(visibleDurationInMillis),
                    new Duration(closeDelayInMillis), false);
            if (newTTBehaviour == null) {
                // abort
                return;
            }
            Field ttbehaviourField = Tooltip.class.getDeclaredField("BEHAVIOR");
            if (ttbehaviourField == null) {
                // abort
                return;
            }
            ttbehaviourField.setAccessible(true);

            // Cache the default behavior if needed.
            Object defaultTTBehavior = ttbehaviourField.get(Tooltip.class);
            ttbehaviourField.set(Tooltip.class, newTTBehaviour);

        } catch (Exception e) {
            System.out.println("Aborted setup due to error:" + e.getMessage());
        }
    }

    public static String divideIntoParagraphs(String text) {
        final int paragraphSize = 30;
        String dividedText = null;
        StringBuffer buffer = new StringBuffer();
        int counter = 0;

        for (int i = 0; i < text.length(); i++) {
            if (i != 0 && (counter % paragraphSize) == 0) {
                if (text.charAt(i) != ' ') {
                    while (text.charAt(i) != ' ') {
                        if (i == text.length() - 1) break;
                        buffer.append(text.charAt(i++));
                    }
                    buffer.append('\n');
                } else {
                    buffer.append('\n');
                }
            } else {
                buffer.append(text.charAt(i));
            }
            counter++;
        }
        dividedText = buffer.toString();

        return dividedText;
    }
}
