package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.digital.MathTools;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import squidpony.squidgrid.mapping.ConnectingMapGenerator;
import squidpony.squidmath.RNG;
import squidpony.ArrayTools;
import squidpony.squidgrid.MimicFill;

public class Map implements Disposable {
	protected class SkyBox implements Disposable{
		protected Texture background;
		protected Texture foreground;
		
		protected SkyBox(int backgroundWidth, int backgroundHeight, int cloudMapWidth, int cloudMapHeight, int cloudMapDepth, float gain, float clip, Color color) {
			this.background = new Texture(Noise.perlinNoise(backgroundWidth, backgroundHeight, 1, gain, 1f, color));
			this.foreground = new Texture(Noise.fractalNoise(cloudMapWidth, cloudMapHeight, cloudMapDepth, gain, clip, color));
		}
		
		protected SkyBox(int backgroundWidth, int backgroundHeight, int foregroundWidth, int foregroundHeight, int foregroundDepth, float gain, float clip, int color) {
			this(backgroundWidth, backgroundHeight, foregroundWidth, foregroundHeight, foregroundDepth, gain, clip, new Color(color));
		}
		
		protected SkyBox(int backgroundWidth, int backgroundHeight, int foregroundWidth, int foregroundHeight, int foregroundDepth, float gain, float clip) {
			this(backgroundWidth, backgroundHeight, foregroundWidth, foregroundHeight, foregroundDepth, gain, clip, Color.WHITE);
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
	
	protected int width;
	protected int height;
	protected int[][] wallGrid;
	protected byte light;
	protected SkyBox skybox;
	protected Texture wallTexture;
	protected Texture floorTexture;

	public Map(int size) {
		this(size, size);
	}
	
	public Map(int width, int height) {
		this.width = width;
		this.height = height;
		this.wallGrid = new int[this.width][this.height];
		this.light = 0x20;
		this.skybox = new SkyBox(1080,512,4,0.2f,0.9f,0x9097A4FF);
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

	public void set(double x, double y, int height) {
		this.set((int) Math.floor(x), (int) Math.floor(y), height);
	}
	
	public void set(int x, int y, int height) {
		try {
			this.wallGrid[x][y] = height;
		} catch (ArrayIndexOutOfBoundsException e) {
			return;
		}
	}
	
	public void randomize(float chance) {
		if (chance < 1f) {
			final int roomSize = MathTools.round(0.5f/chance);
			boolean[][] temp = MimicFill.mapToSample(new ConnectingMapGenerator(width, height, roomSize,roomSize, new RNG(),1).generate(),'#');
			for (int x = 0; x < this.width; x++) {
				for (int y = 0; y < this.height; y++) {
					this.wallGrid[x][y] = temp[x][y]? 1 : 0;
				}
			}
		}
		else {
			ArrayTools.fill(wallGrid, 1);
		}
	}

	@Override
	public void dispose() {
		this.skybox.dispose();
		this.wallTexture.dispose();
		this.floorTexture.dispose();
	}
}
