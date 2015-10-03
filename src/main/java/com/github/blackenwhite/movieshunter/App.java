package com.github.blackenwhite.movieshunter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The {@code App} class represents main app class.
 * The TrackerService creates task for processing movies,
 * and after succeed, shows processed data in the UI
 *
 * @since 30.01.2015
 */

import com.github.blackenwhite.movieshunter.ui.MainWindow;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

public class App extends Application {
    private static MoviesCollector moviesCollector;
    private static SearchService searchService;
    private static MainWindow mainWindow;

    @Override
    public void start(Stage stage) throws Exception {
        searchService = new SearchService();
        mainWindow = new MainWindow();
        mainWindow.initUi(stage);
        searchService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                Utils.logInfo(App.class + " searchService success");
                if (!moviesCollector.isCancelled()) {
                    mainWindow.setTableData(moviesCollector.getMovies());
                } else {
                    mainWindow.setTableData(null);
                }

                mainWindow.enableProcessButton();
                searchService.reset();
            }
        });
    }

    public static void startSearchThread() {
        searchService.start();
    }

    public static void cancelSearchThread() {
        if (searchService.isRunning()) {
            moviesCollector.cancelSearch();
            searchService.reset();
        }
    }

    private static class SearchService extends Service<String> {
        protected Task<String> createTask() {
            return new Task<String>() {
                protected String call()
                        throws IOException {
                    String result = null;
                    try {
                        moviesCollector = new MoviesCollector(mainWindow);
                        moviesCollector.findMovies();
                        result = moviesCollector.toString();
                    }
                    finally {}
                    return result;
                }
            };
        }
    }

    public static void main(String[] args) {
        launch();
    }
}