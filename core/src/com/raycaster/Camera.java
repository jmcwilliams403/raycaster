package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class Camera {
    protected double width;
    protected double height;
    protected double resolution;
    protected double spacing;
    protected double fov;
    protected double range;
    protected double lightRange;
    protected double scale;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    
    public Camera(OrthographicCamera camera, double resolution, double fov) {
        this.camera = camera;
        this.batch = new SpriteBatch();
        this.batch.setProjectionMatrix(camera.combined);
        this.shapeRenderer = new ShapeRenderer();
        this.shapeRenderer.setProjectionMatrix(camera.combined);
        this.width = this.camera.viewportWidth;
        this.height = this.camera.viewportHeight;
        this.resolution = resolution;
        this.spacing = this.width / resolution;
        this.fov = Math.toRadians(fov);
        this.range = 32;
        this.lightRange = 16;
        this.scale = (this.width + this.height) / 640;
    }

    public void render(Player player, Map map) {
        this.drawSky(player.direction, map.skybox, map.light);
        this.drawGround(player, map);
        this.drawColumns(player, map);
        this.drawWeapon(player.weapon, player.paces);
    }

    private void drawSky(double direction, Texture sky, int ambient) {
        double width = this.width * (Math.PI / this.fov);
        double left = width * -direction / Raycaster.TAU;

        batch.begin();
        batch.draw(sky, (float) left, (float) 0, (float) width, (float) this.height, 0, 0, sky.getWidth(), sky.getHeight(), false, true);
        if (left < width - this.width) {
            batch.draw(sky, (float) (left + width), (float) 0, (float) width, (float) this.height, 0, 0, sky.getWidth(), sky.getHeight(), false, true);
        }
        batch.end();

        if (ambient > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setColor(new Color(0xFFFFFF00 | ambient));
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.rect(0, 0, (float) this.width, (float) this.height/2);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private void drawGround(Player player, Map map)
    {
        Pixmap buffer = new Pixmap((int)this.resolution, (int)this.resolution, Format.RGB888);
        buffer.setFilter(Filter.NearestNeighbour);

        final double scale = Math.max(map.groundTexture.getWidth(), map.groundTexture.getHeight());
        final double tx = player.x * scale;
        final double ty = player.y * scale;
        final double scaleX = Math.sin(player.direction) * scale;
        final double scaleY = Math.cos(player.direction) * scale;
        for (int y = 0; y < this.resolution; y++) {
            final double dx = scaleX / (1 + y);
            final double dy = scaleY / (1 + y);

            double sx = tx + (this.resolution / 2) * (dx + dy);
            double sy = ty + (this.resolution / 2) * (dx - dy);

            for (int x = 0; x < this.resolution; x++, sx -= dx, sy += dy)
                buffer.drawPixel(x, y, map.groundTexture.getPixel((int) Math.abs(sx % map.groundTexture.getWidth()), (int) Math.abs(sy % map.groundTexture.getHeight())));
        }

        batch.begin();
        batch.draw(new Texture(buffer, Format.RGB888, true),0,(float)this.height/2,(float)this.width,(float)this.height,0,0,(int)this.resolution,(int)this.resolution,false, true);
        batch.end();
        buffer.dispose();
    	
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rectLine((float)this.width/2, (float)this.height/2, (float)this.width/2, (float)this.height, (float)this.width, Color.BLACK, new Color(0,0,0,(float)map.light / 256f));
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawColumns(Player player, Map map) {
        for (int column = 0; column < this.resolution; column++) {
            double angle = column / this.resolution;
            angle = this.fov * (0.5 - (2 * Math.atan2(1-angle,angle) / Math.PI));
            Ray ray = this.cast(map, player.x, player.y, player.direction + angle, this.range);
            Texture texture = map.wallTexture;
            double left = Math.floor(column * this.spacing);
            double width = Math.ceil(this.spacing);
            int hit = 0;

            while (hit < ray.steps.size() && ray.steps.get(hit).height <= 0)
                hit++;

            for (int s = 0; s < ray.steps.size(); s++) {
                if (s == hit) {
                    Step step = ray.steps.get(s);
                    double textureX = Math.floor(texture.getWidth() * step.offset);
                    Projection wall = this.project(step.height, angle, step.distance, (int)width);

                    batch.begin();
                    batch.draw(texture, (float) left, (float) wall.top, (float) width, (float) wall.height, (int) textureX, 0, 1, texture.getHeight(), false, true);
                    batch.end();

                    Gdx.gl.glEnable(GL20.GL_BLEND);
                    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                    shapeRenderer.setColor(0, 0, 0, (float) Math.max((step.distance + step.shading) / this.lightRange - (map.light/256)*this.lightRange, 0));
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.rect((float) left, (float) wall.top, (float) width, (float) wall.height);
                    shapeRenderer.end();
                    Gdx.gl.glDisable(GL20.GL_BLEND);
                }
            }
        }
    }

    private void drawWeapon(Texture weapon, double paces) {
        double bobX = Math.cos(paces) * this.scale * 6;
        double bobY = Math.sin(paces * 2) * this.scale * 6;
        double left = this.width * 0.66 + bobX;
        double top = this.height * 0.6 + bobY;
        batch.begin();
        batch.draw(weapon, (float) left, (float) top, (float) (weapon.getWidth() * this.scale), (float) (weapon.getHeight() * this.scale), 0, 0, weapon.getWidth(), weapon.getHeight(), false, true);
        batch.end();
    }

    private double alias(double i, int v) {
        return (v == 0)? i : (int)(i/v)*v;
    }

    private Projection project(double height, double angle, double distance, int spacing) {
        double z = distance * Math.cos(angle);
        double wallHeight = this.alias(this.height * height / z, spacing);
        double bottom = this.alias(this.height / 2 * (1 + 1 / z), spacing);
        return new Projection(bottom - wallHeight, wallHeight);
    }
    
    public Ray cast(Map map, double x, double y, double angle, double range) {
        return new Ray(map, new Step(x, y, 0, 0, 0, 0, 0), Math.sin(angle), Math.cos(angle), range);
    }
}
