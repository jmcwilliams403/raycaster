package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import static com.raycaster.Raycaster.*;

public class Camera implements Disposable {
	protected class Projection {
		protected double top;
		protected double height;

		public Projection(double angle, Ray.Step step) {
			double z = step.distance * Math.cos(angle);
			this.height = viewportHeight * step.height / z;
			this.top = viewportHeight / 2 * (1 + 1 / z) - this.height;
		}
	}

	protected int viewportWidth;
	protected int viewportHeight;
	protected double resolution;
	protected double spacing;
	protected double fov;
	protected double focalLength;
	protected double range;
	protected double lightRange;

	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;

	public Camera(OrthographicCamera camera, double resolution) {
		this(camera, resolution, 90);
	}

	public Camera(OrthographicCamera camera, double resolution, double fov) {
		this.batch = new SpriteBatch();
		this.batch.setProjectionMatrix(camera.combined);
		this.shapeRenderer = new ShapeRenderer();
		this.shapeRenderer.setProjectionMatrix(camera.combined);
		this.viewportWidth = (int) camera.viewportWidth;
		this.viewportHeight = (int) camera.viewportHeight;
		this.resolution = resolution;
		this.spacing = this.viewportWidth / resolution;
		this.fov = Math.toRadians(fov);
		this.focalLength = this.fov/PI;
		this.range = 32;
		this.lightRange = 16;
	}

	public void render(Player player, Map map) {
		float ambient = (float) (map.light & 0xFF) / 0xFF;
		batch.setColor(new Color(map.light | 0xFF));
		this.drawSky(player, map, ambient);
		this.drawFloor(player, map.floorTexture, ambient);
		this.drawColumns(player, map, ambient);
		this.drawWeapon(player.weapon, player.weaponScale, player.paces);
	}

	private void drawSky(Player player, Map map, float ambient) {
		
		Texture texture = map.skybox;
		TextureRegion sky = new TextureRegion(texture,0,0,texture.getWidth()*2,texture.getHeight());
		sky.flip(false, true);
		int width = (int) Math.ceil((double)sky.getRegionWidth() * ((double)this.viewportHeight / (double)sky.getRegionHeight())*this.fov);
		int left = (int) Math.floor(width * -player.direction / TAU);

		batch.begin();
		batch.draw(sky, left, 0, width, this.viewportHeight);
		if (left < width - this.viewportWidth) {
			batch.draw(sky, (left + width), 0, width, this.viewportHeight);
		}
		batch.end();

		if (ambient > 0) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			shapeRenderer.setColor(1, 1, 1, ambient);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.rect(0, 0, this.viewportWidth, this.viewportHeight / 2);
			shapeRenderer.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
		
		drawFlat(player, map.cloudMap, 100, true);
	}

	private void drawFloor(Player player, Texture texture, float ambient) {
		drawFlat(player, texture, 0, false);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.rectLine(this.viewportWidth / 2, this.viewportHeight / 2, this.viewportWidth / 2, this.viewportHeight, this.viewportWidth, new Color(0, 0, 0, 1f - ambient), Color.CLEAR);
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
	
	private void drawFlat(Player player, Texture texture, float offset, boolean flip) {
		drawFlat(player, texture, offset, flip, Color.CLEAR);
	}
	
	private void drawFlat(Player player, Texture texture, float offset, boolean flip, Color fill) {
		Pixmap buffer = new Pixmap((int) this.resolution, (int) this.resolution, Format.RGBA8888);
		buffer.setFilter(Filter.NearestNeighbour);
		buffer.setColor(fill);
		buffer.fill();
		TextureData textureData = texture.getTextureData();
		boolean isPrepared = textureData.isPrepared();
		if (!isPrepared)
			textureData.prepare();
		Pixmap floor = textureData.consumePixmap();

		final int horizon = (int)this.resolution/2;
		final double scale = Math.max(floor.getWidth(), floor.getHeight());
		final double tx = player.x * scale / (1f + offset);
		final double ty = player.y * scale / (1f + offset);
		final double scaleX = Math.sin(player.direction) * scale;
		final double scaleY = Math.cos(player.direction) * scale;
		for (int y = 0; y < horizon; y++) {
			final double dx = scaleX / (1 + y);
			final double dy = scaleY / (1 + y);

			double sx = tx + horizon * (dx + dy);
			double sy = ty + horizon * (dx - dy);

			for (int x = 0; x < this.resolution; x++, sx -= dx, sy += dy)
				buffer.drawPixel(x, y + horizon, floor.getPixel((int) Math.abs(sx % floor.getWidth()), (int) Math.abs(sy % floor.getHeight())));
		}
		
		if (!isPrepared)
			floor.dispose();
		
		batch.begin();
		batch.draw(new Texture(buffer, Format.RGBA8888, true), 0, 0, this.viewportWidth, this.viewportHeight, 0, 0, (int) this.resolution, (int) this.resolution, false, !flip);
		batch.end();
		buffer.dispose();
	}

	private void drawColumns(Player player, Map map, float ambient) {
		for (int column = 0; column < this.resolution; column++) {
			double delta = (1 + column) / this.resolution - 0.5;
			double angle = Math.atan2(delta, this.focalLength);
			Ray ray = new Ray(map, player.x, player.y, player.direction + angle, this.range);
			Texture texture = map.wallTexture;
			int left = (int) Math.floor(column * this.spacing);
			int width = (int) Math.ceil(this.spacing);

			for (int hit = 0; hit < ray.steps.size(); hit++) {
				if (ray.steps.get(hit).height > 0) {
					Ray.Step step = ray.steps.get(hit);
					int textureX = (int) Math.floor(texture.getWidth() * step.offset);
					Projection wall = new Projection(angle, step);

					int top = this.alias(wall.top);
					int height = this.alias(wall.height);

					batch.begin();
					batch.draw(texture, left, top, width, height, textureX, 0, 1, texture.getHeight(), false, true);
					batch.end();

					Gdx.gl.glEnable(GL20.GL_BLEND);
					Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
					shapeRenderer.setColor(0, 0, 0, (float) Math.max(Math.min((step.distance + step.shading) / this.lightRange, 1d) - ambient, 0d));
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
		double ratio = (double) weapon.getWidth() / (double) weapon.getHeight();
		int width = this.alias(this.viewportHeight * scale * ratio);
		int height = this.alias(this.viewportHeight * scale);
		int left = this.alias((this.viewportWidth - width / 2) - Math.sin(paces) * width / 4);
		int top = this.alias((this.viewportHeight - height / 2) - Math.cos(paces * 2) * height / 4);
		batch.begin();
		batch.draw(weapon, left, top, width, height, 0, 0, weapon.getWidth(), weapon.getHeight(), false, true);
		batch.end();
	}

	private int alias(double d) {
		int spacing = (int) Math.ceil(this.spacing);
		return (int) (d / spacing) * spacing;
	}

	@Override
	public void dispose() {
		this.batch.dispose();
		this.shapeRenderer.dispose();
	}
}
