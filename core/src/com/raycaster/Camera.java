package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;

public class Camera implements Disposable {
	protected class Projection {
		protected double top;
		protected double height;

		public Projection(double angle, Ray.Step step) {
			double z = step.distance * TrigTools.cos(angle);
			this.height = viewportHeight * step.height / z;
			this.top = viewportHeight / 2 * (1 + 1 / z) - this.height;
		}
	}

	protected int viewportWidth;
	protected int viewportHeight;
	protected int resolution;
	protected double spacing;
	protected double fov;
	protected double focalLength;
	protected double range;
	protected double lightRange;

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;

	public Camera(int width, int height, int resolution, int fov) {
		this.viewportWidth = width;
		this.viewportHeight = height;
		// Setup 2d camera with top left coordinates
		// http://stackoverflow.com/questions/7708379/changing-the-coordinate-system-in-libgdx-java/7751183#7751183
		// This forces us to flip textures on the y axis, eg. in Camera#drawSky
		this.camera = new OrthographicCamera(width, height);
		this.camera.setToOrtho(true, width, height);
		this.batch = new SpriteBatch();
		this.batch.setProjectionMatrix(camera.combined);
		this.shapeRenderer = new ShapeRenderer();
		this.shapeRenderer.setProjectionMatrix(camera.combined);
		this.resolution = resolution;
		this.spacing = (double) this.viewportWidth / this.resolution;
		this.fov =  TrigTools.degreesToRadiansD * MathTools.clamp(fov, 0, 180);
		this.focalLength = TrigTools.PI_D/this.fov-1;
		this.range = 32;
		this.lightRange = 16;
	}

	public void update() {
		this.camera.update();
	}
	
	public void render(Player player, Map map) {
		float ambient = (float) Byte.toUnsignedInt(map.light) / 0xFF;
		this.drawSky(player, map.skybox, ambient);
		this.drawFloor(player, map.floorTexture, ambient);
		this.drawColumns(player, map, ambient);
		this.drawWeapon(player.weapon, player.weaponScale, player.paces);
	}

	private void drawSky(Player player, Map.SkyBox skybox, float ambient) {
		drawFlat(player.direction, skybox.background, 4, true);
		
		if (ambient > 0) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.setColor(1, 1, 1, ambient);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.rect(0, 0, this.viewportWidth, this.viewportHeight / 2);
			shapeRenderer.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
		
		drawFlat(player.x, player.y, 50, player.direction, skybox.foreground, 2500, true);
	}

	private void drawFloor(Player player, Texture texture, float ambient) {
		drawFlat(player.x,player.y,player.direction, texture, 1, false);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.rectLine(this.viewportWidth / 2, this.viewportHeight / 2, this.viewportWidth / 2, this.viewportHeight, this.viewportWidth, new Color(0, 0, 0, 1f - ambient), Color.CLEAR);
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
	
	private void drawFlat(double angle, Texture texture, double scale, boolean flip) {
		drawFlat(0,0,0,angle, texture, scale, flip);
	}
	
	private void drawFlat(double x, double y, double angle, Texture texture, double scale, boolean flip) {
		drawFlat(x, y, 0, angle, texture, scale, flip);
	}
	
	private void drawFlat(double px, double py, double pz, double angle, Texture texture, double scale, boolean flip) {
		Pixmap buffer = new Pixmap(this.resolution, this.resolution, Pixmap.Format.RGBA8888);
		buffer.setFilter(Pixmap.Filter.NearestNeighbour);
		TextureData textureData = texture.getTextureData();
		boolean isPrepared = textureData.isPrepared();
		if (!isPrepared)
			textureData.prepare();
		Pixmap flat = textureData.consumePixmap();
		
		final int width = flat.getWidth(), height = flat.getHeight();
		
		final double size = Math.max(width, height) / Math.sqrt(scale);
		
		final double tx = px * size;
		final double ty = py * size;
		final double tz = Math.max(1d + 2 * pz, 1d) * size;
		
		final double sin = TrigTools.sin(angle);
		final double cos = TrigTools.cos(angle);
		
		final int horizon = this.resolution/2;
		
		final double scaleY = horizon * tz;
		final double scaleX = horizon * this.focalLength;
		
		for (int y = 0; y < horizon; y++) {
			double distance = scaleY / (1 + y);
			double ratio = distance/scaleX;
			
			final double dx = -sin * ratio;
			final double dy = cos * ratio;

			double sx = tx + distance * cos - horizon * dx;
			double sy = ty + distance * sin - horizon * dy;

			for (int x = 0; x < this.resolution; x++, sx += dx, sy += dy)
				buffer.drawPixel(x, y+horizon, flat.getPixel((int) MathTools.remainder(sx, width), (int) MathTools.remainder(sy, height)));
		}
		
		if (!isPrepared)
			flat.dispose();
		
		batch.begin();
		batch.draw(new Texture(buffer, Pixmap.Format.RGBA8888, true), 0, 0, this.viewportWidth, this.viewportHeight, 0, 0, this.resolution, this.resolution, false, !flip);
		batch.end();
		buffer.dispose();
	}

	private void drawColumns(Player player, Map map, float ambient) {
		for (int column = 0; column < this.resolution; column++) {
			double angle = TrigTools.atan2(2d * column / this.resolution - 1, this.focalLength);
			Ray ray = new Ray(map, player.x, player.y, player.direction + angle, this.range);
			Texture texture = map.wallTexture;
			int left = MathTools.floor(column * this.spacing);
			int width = MathTools.ceil(this.spacing);

			for (int hit = 0; hit < ray.steps.size(); hit++) {
				if (ray.steps.get(hit).height > 0) {
					Ray.Step step = ray.steps.get(hit);
					int textureX = MathTools.floor(texture.getWidth() * step.offset);
					Projection wall = new Projection(angle, step);

					int top = this.alias(wall.top);
					int height = this.alias(wall.height);

					batch.begin();
					batch.draw(texture, left, top, width, height, textureX, 0, 1, texture.getHeight(), false, true);
					batch.end();

					Gdx.gl.glEnable(GL20.GL_BLEND);
					Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
					shapeRenderer.setColor(0, 0, 0, (float) MathTools.clamp((step.distance + step.shading) / this.lightRange - ambient, 0d, 1d));
					shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
					shapeRenderer.rect(left, top, width, height);
					shapeRenderer.end();
					Gdx.gl.glDisable(GL20.GL_BLEND);

					break;
				}
			}
		}
	}

	private void drawWeapon(Texture weapon, double scale, double paces) {
		double ratio = (double) weapon.getWidth() / weapon.getHeight();
		int width = this.alias(this.viewportHeight * scale * ratio);
		int height = this.alias(this.viewportHeight * scale);
		int left = this.alias((this.viewportWidth - width / 2) - TrigTools.sin(paces) * width / 4);
		int top = this.alias((this.viewportHeight - height / 2) - TrigTools.cos(paces * 2) * height / 4);
		batch.begin();
		batch.draw(weapon, left, top, width, height, 0, 0, weapon.getWidth(), weapon.getHeight(), false, true);
		batch.end();
	}

	private int alias(double d) {
		int spacing = MathTools.ceil(this.spacing);
		return (int) (d / spacing) * spacing;
	}

	@Override
	public void dispose() {
		this.batch.dispose();
		this.shapeRenderer.dispose();
	}
}
