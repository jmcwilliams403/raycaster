package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class Map implements Disposable {
	protected int size;
	protected int[][] wallGrid;
	protected int light;
	protected Texture skybox;
	protected Texture cloudMap;
	protected Texture wallTexture;
	protected Texture floorTexture;

	public Map(int size) {
		this.size = size;
		this.wallGrid = new int[this.size][this.size];
		this.light = 0xFFFFDF20;
		this.skybox = new Texture(Noise.perlinNoise(2160, 1080, 1, new Color(0xDFEFFFFF)));
		this.skybox.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
		this.wallTexture = new Texture(Gdx.files.internal("wall.png"));
		this.floorTexture = new Texture(Gdx.files.internal("floor.png"));
		this.cloudMap = new Texture(Noise.fractalNoise(512, 512, 16, new Color(0xDFEFFFFF), 0.75f));
	}

	public Integer get(double x, double y) {
		return this.get((int) Math.floor(x), (int) Math.floor(y));
	}

	public Integer get(int x, int y) {
		int height;
		try {
			height = this.wallGrid[x][y];
		} catch (ArrayIndexOutOfBoundsException e) {
			height = -1;
		}
		return height;
	}

	public void randomize(float chance) {
		for (int x = 0; x < this.size; x++) {
			for (int y = 0; y < this.size; y++) {
				this.wallGrid[x][y] = Math.random() < chance ? 1 : 0;
			}
		}
	}

	@Override
	public void dispose() {
		this.skybox.dispose();
		this.wallTexture.dispose();
		this.floorTexture.dispose();
	}
}
