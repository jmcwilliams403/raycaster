package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;

public class Map {
    protected int size;
    protected int[][] wallGrid;
    protected int light;
    protected Texture skybox;
    protected Texture wallTexture;
    protected Pixmap floorTexture;

    public Map(int size) {
        this.size = size;
        this.wallGrid = new int[this.size][this.size];
        this.light = 32;
        this.skybox = new Texture(Gdx.files.internal("panorama.png"));
        this.wallTexture = new Texture(Gdx.files.internal("wall.png"));
        this.floorTexture = new Pixmap(Gdx.files.internal("floor.png"));
    }
    
    public Integer get(double x, double y) {
        return this.get((int)Math.floor(x), (int)Math.floor(y));
    }
    
    public Integer get(int x, int y) {
        return (x < 0 || x > this.size - 1 || y < 0 || y > this.size - 1)? -1 : this.wallGrid[x][y];
    }

    public void randomize() {
        for (int x = 0; x < this.size; x++) {
            for (int y = 0; y < this.size; y++) {
                this.wallGrid[x][y] = Math.random() < 0.3 ? 1 : 0;
            }
        }
    }
}
