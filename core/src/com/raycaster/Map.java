package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class Map implements Disposable {
	protected class SkyBox implements Disposable{
		protected Texture background;
		protected Texture foreground;
		
		protected SkyBox(int backgroundWidth, int backgroundHeight, int cloudMapWidth, int cloudMapHeight, int cloudMapDepth, float gain, float clip, int color) {
			this.background = new Texture(Noise.perlinNoise(backgroundWidth, backgroundHeight, 1, gain, 1f, new Color(color|0xFF)));
			this.background.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
			this.foreground = new Texture(Noise.fractalNoise(cloudMapWidth, cloudMapHeight, cloudMapDepth, gain, clip, new Color(color)));
		}
		
		protected SkyBox(int backgroundWidth, int backgroundHeight, int foregroundWidth, int foregroundHeight, int foregroundDepth, float gain, float clip, Color color) {
			this(backgroundWidth, backgroundHeight, foregroundWidth, foregroundHeight, foregroundDepth, gain, clip, Color.rgba8888(color));
		}
		
		protected SkyBox(int backgroundWidth, int backgroundHeight, int foregroundWidth, int foregroundHeight, int foregroundDepth, float gain, float clip) {
			this(backgroundWidth, backgroundHeight, foregroundWidth, foregroundHeight, foregroundDepth, gain, clip, 0xFFFFFFFF);
		}
		
		protected SkyBox(int backgroundSize, int foregroundSize, int foregroundDepth, float gain, float clip, int color) {
			this(backgroundSize, backgroundSize, foregroundSize, foregroundSize, foregroundDepth, gain, clip, color);
		}
		
		protected SkyBox(int backgroundSize, int foregroundSize, int foregroundDepth, float gain, float clip, Color color) {
			this(backgroundSize, backgroundSize, foregroundSize, foregroundSize, foregroundDepth, gain, clip, color);
		}
		
		protected SkyBox(int backgroundSize, int foregroundSize, int foregroundDepth, float gain, float clip) {
			this(backgroundSize, backgroundSize, foregroundSize, foregroundSize, foregroundDepth, gain, clip);
		}
		
		@Override
		public void dispose() {
			background.dispose();
			foreground.dispose();
		}
	}
	
	protected int size;
	protected int[][] wallGrid;
	protected int light;
	protected SkyBox skybox;
	protected Texture wallTexture;
	protected Texture floorTexture;

	public Map(int size) {
		this.size = size;
		this.wallGrid = new int[this.size][this.size];
		this.light = 0xFFFFDF20;
		this.skybox = new SkyBox(1080,512,4,0.1f,0.9f,0x9097A47F);
		this.wallTexture = new Texture(Gdx.files.internal("wall.png"));
		this.floorTexture = new Texture(Gdx.files.internal("floor.png"));
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
