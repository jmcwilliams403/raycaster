package com.raycaster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;

public class Raycaster extends ApplicationAdapter {
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

	@Override
	public void create() {
		this.player = new Player(1.5, 15.5);
		this.map = new Map(32);
		this.controls = new Controls();
		this.camera = new Camera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, 360, 90);
		this.map.randomize(0.3f);
		this.map.set(player.x, player.y, 0);
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

		camera.update();
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
