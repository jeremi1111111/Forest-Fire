package com.MDLab5;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Board {
    final double cellSize;
    final int height;
    final int width;
    int currentMaxIteration = 0;
    Cell[][] board;
    //    double[] wind = {1, 1};
    double[] wind = {0, 0};
    //    double[] wind = {-1, -1};
    double[][] windWeights;
    int nbRadius;
    int nbSize;
    int noUpdatesCount = 0;
    int dwRow = -1;

    public Board(CellState[][] stateMatrix, int cellSize, int nbRadius) {
        this.cellSize = cellSize;
        this.height = stateMatrix.length;
        this.width = stateMatrix[0].length;

        this.board = new Cell[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                board[y][x] = new Cell(x, y, cellSize * x, cellSize * y, stateMatrix[y][x], this);

        this.nbRadius = nbRadius;
        this.nbSize = 2 * nbRadius + 1;

        calculateWindWeights();
    }

    private void calculateWindWeights() {
        this.windWeights = new double[nbSize][nbSize];
        double[] wY = new double[nbSize];
        double[] wX = new double[nbSize];
        int[] check = {
                wind[0] < 0 ? -1 : 1,
                wind[1] < 0 ? -1 : 1
        };
        for (int i = -nbRadius; i <= nbRadius; i++) {
            wY[nbRadius + i] = Math.abs(wind[0]) * (i == check[0] ? 1 : (double) (i - check[0]) * check[0] / 2.);
            wX[nbRadius + i] = Math.abs(wind[1]) * (i == check[1] ? 1 : (double) (i - check[1]) * check[1] / 2.);
        }
        for (int i = -nbRadius, y = 0; i <= nbRadius; i++, y++) {
            for (int j = -nbRadius, x = 0; j <= nbRadius; j++, x++) {
                windWeights[y][x] = 1
                        + wY[y] - Math.abs(wY[y] * j) / 4.
                        + wX[x] - Math.abs(wX[x] * i) / 4.;
                if (windWeights[y][x] < 0)
                    windWeights[y][x] = 0;
            }
        }
    }

    public void printBoard() {
        for (Cell[] cells : board) {
            for (Cell cell : cells)
                System.out.print(cell.getState(currentMaxIteration) + " ");
            System.out.println();
        }
        System.out.println();
    }

    private void drawCell(GraphicsContext gc, Cell cell, int iteration) {
        gc.setFill(cell.getState(iteration).color);
        gc.fillRect(cell.getCanvasX(), cell.getCanvasY(), cellSize, cellSize);
    }

    public boolean updateCanvas(GraphicsContext gc, int last, int next) {
        boolean flag = false;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = board[y][x];
                if (!cell.isUpdated(last, next)) continue;
                drawCell(gc, cell, next);
                flag = true;
            }
        }
        if (!flag)
            noUpdatesCount++;
        else
            noUpdatesCount = 0;
        return noUpdatesCount <= 5;
    }

    public void drawCanvas(GraphicsContext gc) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = board[y][x];
                drawCell(gc, cell, 0);
            }
        }
    }

    private boolean findFire(Cell cell) {
        if (dwRow != -1 && (cell.boardY < dwRow || cell.boardY > dwRow + 1))
            return false;
        for (int i = -nbRadius - 2; i <= nbRadius + 2; i++) {
            for (int j = 0; j < 2; j++) {
                Cell other = getCell(cell.boardY + 2 + j, cell.boardX + i);
                if (other != null && other.getState(0) == CellState.FIRE) // this is a bit wrong, some cells already progressed
                    return true;
                other = getCell(cell.boardY -2 - j, cell.boardX + i);
                if (other != null && other.getState(0) == CellState.FIRE)
                    return true;
            }
        }
        return false;
    }

    public void nextIteration(boolean rockFlag, boolean dropFlag) {
        for (Cell[] row : board) {
            for (Cell cell : row) {
                if (cell.getState(0) == CellState.FIRE) {
                    if (cell.getBurnTimer() == 0)
                        cell.updateCell(CellState.BURNED);
                    else
                        cell.updateCell(CellState.FIRE);
                    continue;
                }
                if (dropFlag) {
                    if (dwRow == -1)
                        if (findFire(cell))
                            dwRow = cell.boardY;
                    if (cell.getState(0) == CellState.WATER && cell.getBurnTimer() == 1) {
                        cell.updateCell(CellState.GRASS);
                        continue;
                    }
                    if (findFire(cell) && cell.getState(0) == CellState.TREE) {
                        cell.updateCell(CellState.WATER);
                        continue;
                    }
                }
                double chance = cell.getBurnChance();
                if (chance >= 1. || Math.random() < chance)
                    cell.updateCell(rockFlag ? CellState.ROCK : CellState.FIRE);
                else
                    cell.updateCell(cell.getState(0));
            }
        }
        if (dropFlag && dwRow != -1)
            dwRow += 2;
        currentMaxIteration++;
    }

    public int getCurrentMaxIteration() {
        return currentMaxIteration;
    }

    public double[] getWind() {
        return wind;
    }

    public Cell getCell(int y, int x) {
        if (y < 0 || y >= height
                || x < 0 || x >= width)
            return null;
        return board[y][x];
    }

    public void setWind(double wY, double wX) {
        if (wY == wind[0] && wX == wind[1])
            return;
        wind = new double[] {wY, wX};
        calculateWindWeights();
    }
}
