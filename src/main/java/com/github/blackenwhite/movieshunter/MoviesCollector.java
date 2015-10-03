package com.github.blackenwhite.movieshunter;

import com.github.blackenwhite.movieshunter.ui.MainWindow;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * The {@code MoviesCollector} class represents a collection of all processed movies.
 * First, it processes movie from url resources (html & json)
 * Then, it adds this movie to the collection
 * It uses {@code Movie} class methods to process each movie
 *
 * @since 28.01.2015
 */
public class MoviesCollector {
    LinkedList<Movie> movies;
    private MainWindow ui;
    private boolean cancelled = false;
    private static Double progress;

    public MoviesCollector(MainWindow ui) {
        this.ui = ui;
        movies = new LinkedList<Movie>();
    }

    @Override
    public String toString() {
        if (movies.size() == 0) {
            return null;
        }
        Utils.logInfo("moviesCollector.size()=" + movies.size());
        StringBuffer buf = new StringBuffer();
        for (Movie movie : movies) {
            if (movie.getImdbLink() != null) {
                buf.append(movie + "\n\n");
            }
        }
        return buf.toString();
    }

    private void sort() {
        Collections.sort(movies);
    }

    public LinkedList<Movie> getMovies() {
        return movies;
    }

    public void cancelSearch() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void findMovies() {
        Utils.logInfo(getClass() + ".findMovies()");
        ArrayList<Element> moviesNotParsed = getMoviesAsHtml();
        if (moviesNotParsed == null) {
            return;
        }


        final Double percent = 100.0 / moviesNotParsed.size();
        progress = 0.0;
        for (Element el : moviesNotParsed) {

            // Update progress
            {
                double incr = percent >= 1.0 ? 1.0 : percent / 5.0;
                for (double i = 0.0; i < percent; i += incr) {
                    final double value = progress + i;
                    if (value > 100.0) break;
                    Task<Void> progressUpdater = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    MainWindow.refreshProgress(value);
                                }
                            });
                            return null;
                        }
                    };
                    progressUpdater.run();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                progress += percent;
            }

            // Shutdown thread if cancel pushed
            if (cancelled) break;

            Movie movie = new Movie();
            String release = Arrays.asList(el.getElementsByClass(Constants.DOM.Classes.PREMIER_DATE).text()).get(0).trim();
            movie.setPremiere(release);

            String titleRus = Arrays.asList(el.getElementsByClass(Constants.DOM.Classes.TITLE_FILM).text()).get(0).trim();
            movie.setTitleRus(titleRus);

            String titleEng = Arrays.asList(el.getElementsByClass(Constants.DOM.Classes.BIGTEXT).text())
                    .get(0).replaceAll(Constants.Replacements.Replaceable.BRACKETS, Constants.Replacements.Replacement.NULL).trim();
            if (titleEng.length() != 0) {
                movie.setTitleEng(titleEng);
            } else {
                movie.setTitleEng(null);
            }
            movie.setTitleWordsList();
            movie.parseAndSetFieldsFromJson();
            Elements countryNotParsed = el.getElementsByClass(Constants.DOM.Classes.TEXT);

            try {
                String country = countryNotParsed.get(1).text().replaceAll(Constants.Replacements.Replaceable.COUNTRY, Constants.Replacements.Replacement.NULL).trim();
                movie.setCountry(country);
            } catch (IndexOutOfBoundsException e) {
                Utils.logErr("MoviesCollector.getMovies: set country " + e);
            }
            String director = Arrays.asList(el.getElementsByAttributeValue(Constants.DOM.Attributes.ITEMPROP_KEY, Constants.DOM.Attributes.ITEMPROP_VALUE_DIRECTOR).text()).get(0).trim();
            movie.setDirector(director);
            Elements genreNotParsed = el.getElementsByClass(Constants.DOM.Classes.TEXTGRAY);
            String genre = genreNotParsed.get(0).text().replaceAll(Constants.Replacements.Replaceable.GENRE, Constants.Replacements.Replacement.NULL).trim();
            movie.setGenre(genre);
            movie.setRatingFromIMDB();
            if (movie.getRating() != Constants.IMDB.RATING_NOT_SET && movie.getRating() >= Constants.Movies.MIN_RATING) {
                movies.add(movie);
            }
        }
        sort();
    }


    private static ArrayList<Element> getMoviesAsHtml() {
        Utils.logInfo(MoviesCollector.class + ".getMoviesAsHtml()");
        final ArrayList<Element> moviesNotParsed = new ArrayList<>();
        /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
        Element body = getHTMLContent(Constants.URL.CURRENT_MOVIES);
        if (body == null) {
            return null;
        }
        /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
        Elements titles = body.getElementsByClass(Constants.DOM.Classes.TITLE_FILM);
        for (Element t : titles) {
            moviesNotParsed.addAll(t.parent().parent().getElementsByTag(Constants.DOM.Tags.TR));
        }
        return moviesNotParsed;
    }

    private static Element getHTMLContent(String url) {
        Utils.logInfo(MoviesCollector.class + ".getHTMLContent("+url+")");
        Element content = Jsoup.parseBodyFragment("").body();
        UserInput input = MainWindow.getUserInput();
        System.out.println(input);
        final int startYear = input.getStartYear();
        final int endYear = input.getEndYear();
        for (int year = startYear; year <= endYear; year++) {
            int startMonth;
            int endMonth;
            startMonth = year == startYear ? input.getStartMonthIndex() : 0;
            endMonth = year == endYear ? input.getEndMonthIndex() : Constants.Misc.MONTHS_IN_YEAR - 1;

            for (int monthIndex = startMonth; monthIndex <= endMonth; monthIndex++) {
                System.out.println(String.format("%s %d", Constants.Misc.MONTHS[monthIndex], year));
                try {
                    Document html = Jsoup.connect(url)
                            .data("selmonth", String.format("%d", monthIndex+1))
                            .data("selyear", String.format("%d", year))
                            .post();
                    Document doc = Jsoup.parseBodyFragment(html.toString());
                    content.append(doc.body().toString());
                } catch (IOException e) {
                    Utils.logErr(MoviesCollector.class + ".getHTMLContent(" + url + "): " + e);
                }
            }
        }
        return content;
    }
}
