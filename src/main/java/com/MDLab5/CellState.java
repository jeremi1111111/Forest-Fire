package com.MDLab5;

import javafx.scene.paint.Color;

public enum CellState {
    BURNED(Color.BLACK),
    TREE(Color.GREEN, ForestViewController.BURNING_TIME, ForestViewController.BURNING_CHANCE),
    FIRE(Color.RED),
    SAPLING,
    WATER(Color.BLUE, 3, 0),
    GRASS(Color.DARKGREEN, ForestViewController.BURNING_TIME, ForestViewController.BURNING_CHANCE / 20),
    DIRT,
    ROCK(Color.GREY);
    public final Color color;
    public final int burningTime;
    public final double burningChance;
    CellState() {
        this.burningTime = 0;
        this.burningChance = 0;
        this.color = Color.BLACK;
    }

    CellState(Color c) {
        this.color = c;
        this.burningTime = 0;
        this.burningChance = 0;
    }

    CellState(Color c, int burningTime, double burningChance) {
        this.color = c;
        this.burningTime = burningTime;
        this.burningChance = burningChance;
    }
}
