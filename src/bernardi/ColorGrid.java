package bernardi;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.lang.reflect.Executable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ColorGrid extends Application {

    private boolean grid[][];

    private static int numberOfCells;
    private static boolean GridLines;
    private static GridPane pane = new GridPane();
    private static boolean resetPressed;

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox();

        FlowPane flowPane = new FlowPane();
        Scene scene = new Scene(root);

        root.getChildren().addAll(pane,flowPane);

        Button start = new Button("Start");
        Button reset = new Button("Reset");
        Button createGrid = new Button("Create Grid");
        Slider slider = new Slider(0,  100,10);
        flowPane.getChildren().addAll(slider,createGrid,start,reset);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        flowPane.setPadding(new Insets(10));
        flowPane.setHgap(50);
        flowPane.setAlignment(Pos.CENTER);

        final int numCols = 180;
        final int numRows = 90;
        grid = new boolean[numRows][numCols];

        // creates grid to be shown, starting will all cells black.
        for(int i = 0; i < grid.length; i ++) {
            for(int j = 0; j < grid[i].length; j++) {
                Rectangle cell = new Rectangle(10,10,Color.BLACK);
                pane.add(cell,j,i,1,1);
            }
        }




        /* Setting grid lines to zero adds another node to the list of children! */
        pane.setGridLinesVisible(true);
        GridLines = true;
        if(GridLines) {
            numberOfCells = pane.getChildren().size() - 1;
        }
        else {
            numberOfCells = pane.getChildren().size();
        }

        ScheduledExecutorService executorService =
                Executors.newSingleThreadScheduledExecutor();


        // lambda expression to set what happens when create grid button is pushed
        createGrid.setOnAction(event -> {
            initializeArray(slider.getValue(), grid);
            setCellID(grid,pane);
            updateColors(pane);
        });

        // Lambda expression to set what happens upon start button click
        start.setOnAction(event -> {


            executorService.scheduleAtFixedRate(runnableTask,0,400,
                    TimeUnit.MILLISECONDS);

            /*
            while(true) {
                grid = simulateNextGeneration(grid);
                setCellID(grid, pane);
                updateColors(pane);
                pause(800);
                if(resetPressed) {
                    break;
                }
            }
            resetPressed = false; */

            });

        reset.setOnAction(event -> {
            System.exit(0);
            resetGrid(pane);
            resetPressed = true;

        });



        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();


    }

    /**
     * Helper method that takes a two-dimensional boolean array as parameter and
     * goes through the array, cell by cell, and applies the rules to the game of life.
     * A new two dimensional boolean array is created, with the cells in the state of
     * the next generation from the parameter array. The new array is returned.
     * @param grid
     * @return newGrid[][] after the rules are applied
     */
    private static boolean[][] simulateNextGeneration(boolean[][] grid) {
        boolean newGrid[][] = new boolean[grid.length][grid[0].length];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                // stores the surrounding cell sum for each individual cell
                int sum = 0;

                // first check if current cell is around on the border...if so, set the
                // cell = false. newGrid[i][j] = grid[i][j] would also work
                if(i == 0 || j == 0 || i == grid.length - 1 || j == grid[i].length - 1) {
                    newGrid[i][j] = false; // cell on edge
                }
                else {
                    // will hold the sum of all cells surrounding each individual cell that
                    // we are currently looking at.

                    // for each individual cell, we look at each of it's 8 touching neighbors
                    // Remember, cells on the border do not count and stay dead.
                    for (int k = i - 1; k <= i + 1; k++) {
                        for (int l = j - 1; l <= j + 1; l++) {
                            if (k == i && l == j) {
                                newGrid[i][j] = grid[i][j];
                            }
                            else {
                                if (grid[k][l] == true) {
                                    sum++;
                                }
                            }

                        }
                    }
                    // by this point, the current cell we are looking at has a sum.
                    // if the sum is 2 or 3 and the cell is currently alive, it stays alive.
                    // otherwise, if the cell is alive, it dies.
                    // it the cell is dead, it stays dead except when it has exactly 3
                    // neighbors.
                    if (grid[i][j]) {
                        if (sum == 2 || sum == 3) {
                            newGrid[i][j] = true;
                        }
                        else {
                            newGrid[i][j] = false;
                        }
                    }
                    // otherwise the cell is currently dead:
                    else {

                        if (sum == 3) {
                            newGrid[i][j] = true;
                        }
                        else {
                            newGrid[i][j] = false;
                        }
                    }
                }
            }

        }
        return newGrid;
    }

    Runnable runnableTask = () -> {

        grid = simulateNextGeneration(grid);
        setCellID(grid, pane);
        updateColors(pane);
    };

    /**
     * Method to update colors of the list of nodes of the GridPane pane
     */
    private static void updateColors(GridPane pane) {

        for(int i = 0; i < numberOfCells - 1; i++) {
            Rectangle cell = (Rectangle) pane.getChildren().get(i);
            if(cell.getId() == "ALIVE") {
                cell.setFill(Color.SEAGREEN);
            }
            else if(cell.getId() == "DEAD") {
                cell.setFill(Color.BLACK);
            }
        }
    }

    /**
     * Initializes the grid, setting each cell alive or dead randomly. The parameter
     * determines the percentage of alive cells that the grid will be initialized with.
     */
    private static void initializeArray(double percentAlive, boolean grid[][]) {
        // Initializes the grid with a random configuration.
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for(int i = 0; i < grid.length; i ++) {
            for(int j = 0; j < grid[i].length; j++) {

                if(i == 0 || i == grid.length - 1 || j == 0 || j == grid[i].length - 1) {
                    grid[i][j] = false;
                }
                else {
                    int randomNum = rand.nextInt(0, 101);
                    if (randomNum <= percentAlive) {
                        grid[i][j] = true;
                    } else {
                        grid[i][j] = false;
                    }
                }

            }
        }
    }

    private static void resetGrid(GridPane pane) {
        for(int i = 0; i < numberOfCells; i ++) {
            Rectangle cell = (Rectangle) pane.getChildren().get(i);
            cell.setId("DEAD");
            updateColors(pane);
        }

    }



    /**
     * Helper method that takes in a two dimensional boolean array and a GridPane with
     * rectangle children. It then, cell cell, goes through and sets each rectangle
     * child node of a gridPane List to the correct ID (DEAD or ALIVE)
     */
    private static void setCellID(boolean grid[][], GridPane pane) {

        for(int i = 0; i < numberOfCells; i++) {
            Rectangle cell = (Rectangle) pane.getChildren().get(i);
            if(grid[GridPane.getRowIndex(cell)][GridPane.getColumnIndex(cell)] == true) {
                cell.setId("ALIVE");
            }
            else
            {
                cell.setId("DEAD");
            }
        }

    }

    /**
     * Helper method that will cause the program to pause for the specified number of
     * milliseconds.
     */
    private static void pause(int milliseconds) {
        long timestamp = System.currentTimeMillis();

        do {

        }while(System.currentTimeMillis() < timestamp + milliseconds);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
