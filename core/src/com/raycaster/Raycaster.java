package com.raycaster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;

public class Raycaster extends ApplicationAdapter {
	protected static final double PI = Math.PI;
	protected static final double TAU = PI*2;
	protected static final double ETA = PI/2;
	protected static final float EPSILON = 0x1p-23f;
	private static final int VIRTUAL_WIDTH = 1920;
	private static final int VIRTUAL_HEIGHT = 1080;
	private static final float ASPECT_RATIO = (float) VIRTUAL_WIDTH / (float) VIRTUAL_HEIGHT;
	private Player player;
	private Map map;
	private Controls controls;
	private Camera camera;
	private float seconds = 0;
	private Rectangle viewport;
	private float scale = 1f;
	private OrthographicCamera orthoCamera;

	@Override
	public void create() {
		// Setup 2d camera with top left coordinates
		// http://stackoverflow.com/questions/7708379/changing-the-coordinate-system-in-libgdx-java/7751183#7751183
		// This forces us to flip textures on the y axis, eg. in Camera#drawSky
		orthoCamera = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
		orthoCamera.setToOrtho(true, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

		this.player = new Player(15.5, -1.5);
		this.map = new Map(32);
		this.controls = new Controls();
		this.camera = new Camera(orthoCamera, 360);

		this.map.randomize(0.3f);
	}

	@Override
	public void resize(int width, int height) {
		// calculate new viewport
		float aspectRatio = (float) width / (float) height;

		float x = 0f, y = 0f;
		if (aspectRatio > ASPECT_RATIO) {
			scale = (float) height / (float) VIRTUAL_HEIGHT;
			x = (width - VIRTUAL_WIDTH * scale) / 2f;
		} else if (aspectRatio < ASPECT_RATIO) {
			scale = (float) width / (float) VIRTUAL_WIDTH;
			y = (height - VIRTUAL_HEIGHT * scale) / 2f;
		} else {
			scale = (float) width / (float) VIRTUAL_WIDTH;
		}

		float w = (float) VIRTUAL_WIDTH * scale;
		float h = (float) VIRTUAL_HEIGHT * scale;
		viewport = new Rectangle(x, y, w, h);
	}

	@Override
	public void render() {
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		orthoCamera.update();
		Gdx.gl.glViewport((int) viewport.x, (int) viewport.y, (int) viewport.width, (int) viewport.height);

		seconds = Gdx.graphics.getDeltaTime();
		controls.update();
		player.update(controls, map, seconds);
		camera.render(player, map);
	}
	
	@Override
	public void dispose() {
		this.camera.dispose();
		this.map.dispose();
		this.player.dispose();
	}
}
