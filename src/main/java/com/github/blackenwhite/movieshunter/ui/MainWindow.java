package com.github.blackenwhite.movieshunter.ui;

import com.github.blackenwhite.movieshunter.*;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.util.Callback;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static com.github.blackenwhite.movieshunter.Constants.UI.*;

/**
 * The {@code UiMain} class represents the UI form
 * with a simple progress gif image and
 * {@code TableView} instance that contains
 * processed movies data
 *
 * @since 30.01.2015
 */
public class MainWindow extends Application {
    private Stage stage;
    private Scene scene;
    private static GridPane mainPane;
    private GridPane topPane;
    private GridPane tablePane;
    private static TableView table;

    private ObservableList<Integer> yearsList;
    private ObservableList<String> monthsList;
    private static ComboBox startYearBox;
    private static ComboBox endYearBox;
    private static ComboBox startMonthBox;
    private static ComboBox endMonthBox;
    private Button processBtn;
    private Button cancelBtn;

    private static int previousLoadId = 0;
    private static int previousRegularId = 0;


    public static void main(String[] args) {
        Application.launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        initUi(stage);
//        Utils.setupCustomTooltipBehavior(2000, 20000, 1);
    }

    public void initUi(Stage stage) {
        mainPane = new GridPane();
        mainPane.setAlignment(Pos.CENTER);
        mainPane.setVgap(VGAP);

        initTopPane();
        initTable();

        stage.setTitle(Constants.UI.APP_TITLE);
        stage.setWidth(STAGE_WIDTH);
        stage.setHeight(STAGE_HEIGHT);
        stage.setResizable(false);
        scene = new Scene(mainPane);

        try {
            stage.getIcons().add(new Image(Constants.UI.Images.APP_ICON));
        } catch (IllegalArgumentException e) {
            Utils.logErr(e.getCause());
        }
        stage.setScene(scene);
        try {
            scene.getStylesheets().add(Constants.UI.CSS_FILE);
        } catch (IllegalArgumentException e) {
            Utils.logErr(e.getMessage());
        }
        setRandomRegularBack();
        stage.show();
    }


    public void setTableData(LinkedList<Movie> moviesList) {
        setRandomRegularBack();
        processBtn.setDisable(false);
        if (moviesList == null) {
            setTableLabel(MoviesCollector.getStatus());
            cancelBtn.setDisable(true);
        } else {
            ObservableList<Movie> data = FXCollections.observableArrayList(moviesList);
            table.setItems(data);
            cancelBtn.setDisable(true);
        }
    }

    public GridPane getMainPane() {
        return mainPane;
    }

    public static void refreshProgress(Double progress) {
        Label progressBar = new Label(String.format("%.2f%%", progress));
        progressBar.setId("standby");
        table.setPlaceholder(progressBar);
    }

    public void enableProcessButton() {
        processBtn.setDisable(false);
    }

    public int getPreviousRegularId() {
        return previousRegularId;
    }

    public void setPreviousRegularId(int id) {
        previousRegularId = id;
    }

    private void setTableLabel(String labelStr) {
        table.getItems().removeAll(table.getItems());
        Label label = new Label(labelStr);
        label.setId("standby");
        table.setPlaceholder(label);
    }

    private void initTopPane() {
        topPane = new GridPane();
        topPane.setAlignment(Pos.CENTER);
        topPane.setHgap(30);

//        ObservableList<Integer> days =
//                FXCollections.observableArrayList(
//                    7, 15, 30, 60, 90
//                );
//        final ComboBox daysBox = new ComboBox(days);
//        daysBox.setPromptText("Days range");
//        daysBox.setTooltip(new Tooltip("Default: " + Constants.Misc.RANGE));
//        daysBox.setId("bevel-grey");
//
//        ObservableList<Double> ratings =
//                FXCollections.observableArrayList(
//                        6.0, 6.5, 7.0, 7.5, 8.0
//                );
//        final ComboBox ratingsBox = new ComboBox(ratings);
//        ratingsBox.setPromptText("Minimum rating");
//        ratingsBox.setTooltip(new Tooltip("Default: " + Constants.Movies.MIN_RATING));
//        ratingsBox.setId("bevel-grey");


        int year = 1995;
        int[] years = new int[Utils.getCurrentYear() - year + 1];
        for (int i = 0; i < years.length; i++) {
            years[i] = year++;
        }
        yearsList = FXCollections.observableArrayList(
                        new ArrayList<Integer>() {{
                            for (int year : years) {
                                add(year);
                            }
                        }}
                );
        monthsList = FXCollections.observableList(
                        new ArrayList<String>() {{
                            for (String m : Constants.Misc.MONTHS) {
                                if (m.equals("")) continue;
                                add(m);
                            }
                        }}
                );

        startYearBox = new ComboBox(yearsList);
        endYearBox = new ComboBox(yearsList);
        startMonthBox = new ComboBox(monthsList);
        endMonthBox = new ComboBox(monthsList);


        int endYear = yearsList.get(yearsList.size() - 1);
        int startYear = endYear;
        int endMonthIndex = Utils.getCurrentMonth();
        int startMonthIndex = endMonthIndex - 1;
        if (endMonthIndex == 0) {
            startMonthIndex = 11; // december
            startYear -= 1;
        }
        startYearBox.setValue(startYear);
        endYearBox.setValue(endYear);
        endMonthBox.setValue(monthsList.get(endMonthIndex));
        startMonthBox.setValue(monthsList.get(startMonthIndex));
        startYearBox.setValue(Constants.Misc.DEFAULT_START_YEAR);
        endYearBox.setValue(Constants.Misc.DEFAULT_END_YEAR);
        endMonthBox.setValue(Constants.Misc.DEFAULT_END_MONTH);
        startMonthBox.setValue(Constants.Misc.DEFAULT_START_MONTH);

        startYearBox.setId("bevel-grey");
        endYearBox.setId("bevel-grey");
        startMonthBox.setId("bevel-grey");
        endMonthBox.setId("bevel-grey");

        processBtn = new Button("Process");
        processBtn.setId("glass-grey");
        processBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (fieldsAreValid()) {
                    setTableLabel("PLEASE STAND BY");
                    setRandomLoadBack();
                    cancelBtn.setDisable(false);
                    processBtn.setDisable(true);
                    App.startSearchThread();
                } else {
                    setTableLabel(Constants.UI.STATUS_INTERRUPTED_INVALID_INPUT);
                    cancelBtn.setDisable(true);
                    processBtn.setDisable(false);
                }
            }
        });

        cancelBtn = new Button("Cancel");
        cancelBtn.setId("glass-grey");
        cancelBtn.setDisable(true);
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                MoviesCollector.setStatus(Constants.UI.STATUS_CANCELLED);
                setTableLabel(Constants.UI.STATUS_CANCELLED);
                cancelBtn.setDisable(true);
                App.cancelSearchThread();
            }
        });

        topPane.addRow(0, startYearBox, startMonthBox, processBtn);
        topPane.addRow(1, endYearBox, endMonthBox, cancelBtn);
        topPane.setVgap(10);

        mainPane.addRow(0, topPane);
    }

    public static UserInput getUserInput() {
        ArrayList<HashMap<String, String>> input = new ArrayList<>();
        HashMap<String, String> years = new HashMap<String, String>() {{
            put("startYear", startYearBox.getValue().toString());
            put("endYear", endYearBox.getValue().toString());
        }};
        HashMap<String, String> months = new HashMap<String, String>() {{
            put("startMonth", startMonthBox.getValue().toString());
            put("endMonth", endMonthBox.getValue().toString());
        }};
        input.add(years);
        input.add(months);
        return new UserInput(input);
    }

    private boolean fieldsAreValid() {
        int startYear = Integer.parseInt(startYearBox.getValue().toString());
        int endYear = Integer.parseInt(endYearBox.getValue().toString());
        if (startYear <= endYear) {
            if (monthsList.indexOf(startMonthBox.getValue()) <= monthsList.indexOf(endMonthBox.getValue())) {
                return true;
            }
        }
        return false;
    }

    private static void setRandomRegularBack() {
        int rnd;
        do {
            rnd = new Random().nextInt(Constants.UI.REGULAR_STYLES) + 1;
        } while (rnd == previousRegularId);
        previousRegularId = rnd;
        mainPane.setId("regular_" + rnd);
    }

    private static void setRandomLoadBack() {
        int rnd;
        do {
            rnd = new Random().nextInt(Constants.UI.LOAD_STYLES) + 1;
        } while (rnd == previousLoadId);
        previousLoadId = rnd;
        mainPane.setId("load_" + rnd);
    }

    private void initTable() {
        tablePane = new GridPane();
        tablePane.setId("glass-grey");
        tablePane.setAlignment(Pos.CENTER);
        table = new TableView();
        table.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    Movie movie = (Movie)table.getSelectionModel().getSelectedItem();
                    System.out.println(movie.getImdbLink());
                    openWebpage(movie.getImdbURL());
                }
//              else if (event.getClickCount() == 1) {
//                    table.setRowFactory(new Callback<TableView, TableRow>() {
//                        @Override
//                        public TableRow call(final TableView tv) {
//                            return new TableRow<Movie>() {
//                                @Override
//                                public void updateItem(Movie movie, boolean empty) {
//                                    super.updateItem(movie, empty);
//                                    if (movie == null) {
//                                        setTooltip(null);
//                                    } else {
//                                        Tooltip tooltip = new Tooltip();
//                                        tooltip.setText(movie.getDirector());
//                                        setTooltip(tooltip);
//                                    }
//                                }
//                            };
//                        }
//                    });
//                }
            }
        });

        table.setRowFactory(new Callback<TableView, TableRow>() {
            @Override
            public TableRow call(final TableView tv) {
                return new TableRow<Movie>() {
                    @Override
                    public void updateItem(Movie movie, boolean empty) {
                        super.updateItem(movie, empty);
                        if (movie == null) {
                            setTooltip(null);
                        } else {
                            setTooltip(null);
                            Tooltip tooltip = new Tooltip();
                            Utils.hackTooltipTiming(tooltip);
                            tooltip.setText(movie.getDescription());
                            setTooltip(tooltip);
                        }
                    }
                };
            }
        });
        table.setMinWidth(Constants.UI.TABLE_WIDTH);
        table.setMaxWidth(Constants.UI.TABLE_WIDTH);
        table.setMinHeight(Constants.UI.TABLE_HEIGHT);
        table.setMaxHeight(Constants.UI.TABLE_HEIGHT);
        table.getColumns().addAll(initColumns());
        table.setEditable(true);
        tablePane.add(table, 0, 0);
        mainPane.addRow(2, tablePane);
    }

    private static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<TableColumn> initColumns() {
        ArrayList<TableColumn> columns = new ArrayList<>();
        String[][] cols = {
                {"Eng", "titleEng"},
                {"Rus", "titleRus"},
                {"Rating", "rating"},
                {"Director", "director"},
                {"Premiere", "premiere"}
//                {"IMDB", "imdbLinkShort"}
        };

        for (String[] s : cols) {
            TableColumn column = new TableColumn(s[0]);
            column.setCellValueFactory(new PropertyValueFactory<Movie, String>(s[1]));
            switch (s[0]) {
                case "Eng":
                    column.setMinWidth(TABLE_WIDTH * 0.24);
                    column.setMaxWidth(TABLE_WIDTH * 0.25);
                    break;
                case "Rus":
                    column.setMinWidth(TABLE_WIDTH * 0.25);
                    column.setMaxWidth(TABLE_WIDTH * 0.25);
                    break;
                case "Rating":
                    column.setMinWidth(TABLE_WIDTH * 0.09);
                    column.setMaxWidth(TABLE_WIDTH * 0.09);
                    break;
                case "Director":
                    column.setMinWidth(TABLE_WIDTH * 0.25);
                    column.setMaxWidth(TABLE_WIDTH * 0.25);
                    break;
                case "Premiere":
                    column.setMinWidth(TABLE_WIDTH * 0.16);
                    column.setMaxWidth(TABLE_WIDTH * 0.16);
                    break;
                default:
                    break;
            }
            columns.add(column);
        }
        return columns;
    }
}
