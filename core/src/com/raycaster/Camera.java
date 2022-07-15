package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class Camera {
    protected class Projection {
        protected double top;
        protected double height;
        
        public Projection(double angle, double height, Ray.Step step) {
            double z = step.distance * Math.cos(angle);
            this.height = height * step.height / z;
            this.top = height / 2 * (1 + 1 / z) - this.height;
        }
	}
	
    protected double width;
    protected double height;
    protected double resolution;
    protected double spacing;
    protected double fov;
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
        this.width = camera.viewportWidth;
        this.height = camera.viewportHeight;
        this.resolution = resolution;
        this.spacing = this.width / resolution;
        this.fov = Math.toRadians(fov);
        this.range = 32;
        this.lightRange = 16;
    }

    public void render(Player player, Map map) {
        float ambient = (float)Byte.toUnsignedInt(map.light)/255f;
        batch.setColor(Color.WHITE);
        this.drawSky(player.direction, map.skybox, ambient);
        this.drawFloor(player, map.floorTexture, ambient);
        this.drawColumns(player, map, ambient);
        this.drawWeapon(player.weapon, player.weaponScale, player.paces);
    }

    private void drawSky(double direction, Texture sky, float ambient) {
        double width = this.width * (Math.PI*2 / this.fov);
        double left = width * -direction / (Math.PI*2);

        batch.begin();
        batch.draw(sky, (float) left, (float) 0, (float) width, (float) this.height, 0, 0, sky.getWidth()*2, sky.getHeight(), false, true);
        if (left < width - this.width) {
            batch.draw(sky, (float) (left + width), (float) 0, (float) width, (float) this.height, 0, 0, sky.getWidth()*2, sky.getHeight(), false, true);
        }
        batch.end();
        
        if (ambient > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setColor(1,1,1,ambient);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.rect(0, 0, (float) this.width, (float) this.height/2);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private void drawFloor(Player player, Texture texture, float ambient)
    {
        Pixmap buffer = new Pixmap((int)this.resolution, (int)this.resolution, Format.RGBA8888);
        buffer.setFilter(Filter.NearestNeighbour);
        TextureData textureData = texture.getTextureData();
        if(!textureData.isPrepared())
            textureData.prepare();
        Pixmap floor = textureData.consumePixmap();

        final double scale = Math.max(floor.getWidth(), floor.getHeight());
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
                buffer.drawPixel(x, y, floor.getPixel((int) Math.abs(sx % floor.getWidth()), (int) Math.abs(sy % floor.getHeight())));
        }
        floor.dispose();

        batch.begin();
        batch.draw(new Texture(buffer, Format.RGBA8888, true),0,(float)this.height/2,(float)this.width,(float)this.height,0,0,(int)this.resolution,(int)this.resolution,false, true);
        batch.end();
        buffer.dispose();
    	
        if (ambient > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.rectLine((float)this.width/2, (float)this.height/2, (float)this.width/2, (float)this.height, (float)this.width, new Color(0,0,0,1f-ambient), Color.CLEAR);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private void drawColumns(Player player, Map map, float ambient) {
        for (int column = 0; column < this.resolution; column++) {
            double x = column / this.resolution;
            double angle = this.fov * (0.5 - (2 * Math.atan2(1-x,x) / Math.PI));
            Ray ray = new Ray(map, player.x, player.y, player.direction + angle, this.range);
            Texture texture = map.wallTexture;
            int left = (int)Math.floor(column * this.spacing);
            int width = (int)Math.ceil(this.spacing);

            for (int hit = 0; hit < ray.steps.size(); hit++) {
                if (ray.steps.get(hit).height > 0) {
                    Ray.Step step = ray.steps.get(hit);
                    int textureX = (int)Math.floor(texture.getWidth() * step.offset);
                    Projection wall = new Projection(angle, this.height, step);

                    int top = this.alias(wall.top);
                    int height = this.alias(wall.height);

                    batch.begin();
                    batch.draw(texture, left, top, width, height, textureX, 0, 1, texture.getHeight(), false, true);
                    batch.end();

                    Gdx.gl.glEnable(GL20.GL_BLEND);
                    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                    shapeRenderer.setColor(0,0,0,(float)Math.max(Math.min((step.distance + step.shading) / this.lightRange, 1d) - ambient, 0d));
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
        double ratio = (double)weapon.getWidth()/(double)weapon.getHeight();
        int width = this.alias(this.height*scale*ratio);
        int height = this.alias(this.height*scale);
        int left = this.alias((this.width - width / 2) - Math.sin(paces) * width / 4);
        int top = this.alias((this.height - height / 2) - Math.cos(paces * 2) * height / 4);
        batch.begin();
        batch.draw(weapon, left, top, width, height, 0, 0, weapon.getWidth(), weapon.getHeight(), false, true);
        batch.end();
    }
        
    private int alias(double d) {
    	int spacing = (int)Math.ceil(this.spacing);
        return (int)(d/spacing)*spacing;
    }
}
