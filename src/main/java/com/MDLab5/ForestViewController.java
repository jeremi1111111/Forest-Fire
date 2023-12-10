package com.MDLab5;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class ForestViewController {
    public final static int NB_RADIUS = 1;
    public final static double BURNING_CHANCE = 0.15;
    public final static int BURNING_TIME = 5;
    public final static int SIMULATION_RATE = 100;
    public final static int GRID_SIZE = 400;

    @FXML
    Canvas canvas;
    @FXML
    Timeline animation;
    @FXML
    HBox controlPanel;
    @FXML
    TextField iteration;
    @FXML
    Button ritualButton;
    @FXML
    Button dropButton;
    @FXML
    HBox sliderBox;
    @FXML
    Slider windY;
    @FXML
    Slider windX;

    boolean ritualFlag = false;
    boolean dropFlag = false;

    @FXML
    protected void initialize() {
        canvas.setVisible(false);
        canvas.setManaged(false);
        controlPanel.setVisible(false);
        controlPanel.setManaged(false);
    }

    @FXML
    protected void onRitualButtonClick() {
        ritualFlag = true;
    }

    @FXML
    protected void onDropButtonClick() {
        dropFlag = true;
    }

    @FXML
    protected void onSimulateButtonClick() {
//        if (animation != null) {
//            animation.stop();
//            return;
//        }
        ritualFlag = false;
        dropFlag = false;

        int gridSizeY = GRID_SIZE;
        int gridSizeX = GRID_SIZE;
        int cellSize = 8 * 100 / GRID_SIZE;
        CellState[][] stateMatrix = new CellState[gridSizeY][gridSizeX];

        for (int i = 0; i < 10 * GRID_SIZE / 100; i++) {
            int y = (int) (Math.random() * GRID_SIZE * 0.9 + GRID_SIZE * 0.05);
            int x = (int) (Math.random() * GRID_SIZE * 0.9 + GRID_SIZE * 0.05);
            int rSizeY = (int) (Math.random() * 10) + 5;
            int rSizeX = (int) (Math.random() * 10) + 5;
            for (int j = 0; j < rSizeY; j++)
                for (int k = 0; k < rSizeX; k++) {
                    if ((j == 0 && k == 0)
                            || (j == 0 && k == rSizeX - 1)
                            || (j == rSizeY - 1 && k == 0)
                            || (j == rSizeY - 1 && k == rSizeX - 1))
                        continue;
                    stateMatrix[y + j][x + k] = CellState.ROCK;
                }
        }

        int rivY = 0;
        int rivX = (int) (Math.random() * 50) + 50;
        int rivSize = 4;
        while (rivY < gridSizeY) {
            for (int i = 0; i < rivSize + 4; i++) {
                if (rivX + i < 0 || rivX + i >= gridSizeX) continue;
                if (i < 2 || i > rivSize + 1)
                    stateMatrix[rivY][rivX + i] = CellState.GRASS;
                else
                    stateMatrix[rivY][rivX + i] = CellState.WATER;
            }
            rivY++;
            rivX += (int) (Math.random() * 4) - 1;
        }

        int fY = 50;
        int fX = 25;
        do {
            fY = (int) (Math.random() * GRID_SIZE * 0.75 + GRID_SIZE * 0.125);
            fX = (int) (Math.random() * GRID_SIZE * 0.75 + GRID_SIZE * 0.125);
        } while (stateMatrix[fY][fX] != null);

        stateMatrix[fY][fX] = CellState.FIRE;

        for (int y = 0; y < gridSizeY; y++)
            for (int x = 0; x < gridSizeX; x++)
                stateMatrix[y][x] = stateMatrix[y][x] == null ? CellState.TREE : stateMatrix[y][x];
        Board forest = new Board(stateMatrix, cellSize, NB_RADIUS);
        canvas.setVisible(true);
        canvas.setManaged(true);
        canvas.setHeight(gridSizeY * cellSize);
        canvas.setWidth(gridSizeX * cellSize);
        forest.drawCanvas(canvas.getGraphicsContext2D());

        GraphicsContext gc = canvas.getGraphicsContext2D();
        controlPanel.setVisible(true);
        controlPanel.setManaged(true);
        canvas.getScene().getWindow().sizeToScene();
        canvas.getScene().getWindow().centerOnScreen();

        animation = new Timeline(new KeyFrame(Duration.millis(SIMULATION_RATE), (event) -> {
            forest.setWind(windY.getValue(), -windX.getValue());
            forest.nextIteration(ritualFlag, dropFlag);
            if (!forest.updateCanvas(gc, 1, 0)) {
                animation.stop();
            }
            iteration.setText(String.valueOf(forest.getCurrentMaxIteration()));
        }));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();
    }
}
