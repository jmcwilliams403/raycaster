package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.Color;

public class Map {
    protected int size;
    protected int[][] wallGrid;
    protected int light;
    protected Texture skybox;
    protected Texture wallTexture;
    protected Color groundColor;

    public Map(int size) {
        this.size = size;
        this.wallGrid = new int[this.size][this.size];
        this.light = 32;
        this.skybox = new Texture(Gdx.files.internal("panorama.png"));
        this.wallTexture = new Texture(Gdx.files.internal("wall.png"));
        this.groundColor = new Color(0x6d5844FF);
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
                this.wallGrid[x][y] = MathUtils.randomBoolean(0.3f)? 1 : 0;
            }
        }
    }

    public Ray cast(Point point, double angle, double range) {
        return new Ray(this, new Step(point.x, point.y, 0, 0, 0, 0, 0), Math.sin(angle), Math.cos(angle), range);
    }
}
