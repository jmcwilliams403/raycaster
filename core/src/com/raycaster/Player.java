package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Player {
    protected double x;
    protected double y;
    protected double direction;
    protected double paces;

    protected Texture weapon;
    protected float speed;

    public Player(double x, double y) {
        this(x, y, Math.PI/2);
    }
    
    public Player(double x, double y, double direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.paces = 0;
        this.speed = 3;
        this.weapon = new Texture(Gdx.files.internal("hand.png"));
    }

    public void rotate(double angle) {
        this.direction = (this.direction + angle + Math.PI*2) % (Math.PI*2);
    }

    public double walk(double distance, Map map, double direction) {
        double dx = Math.cos(direction) * distance, dy = Math.sin(direction) * distance;
        
        if (map.get(this.x + dx, this.y) <= 0) this.x += dx;
        else dx = 0;
        
        if (map.get(this.x, this.y + dy) <= 0) this.y += dy;
        else dy = 0;
        
        return Math.hypot(dx, dy);
    }

    public void update(Controls controls, Map map, double seconds) {
    	if (controls.turn) this.rotate((controls.x / Math.PI) * seconds);

        if (controls.move) {
	        final double distance = this.speed * seconds;
	        double delta = 0;
	        if (controls.left) delta = Math.max(delta, this.walk(distance, map, this.direction - Math.PI/2));
	        if (controls.right) delta = Math.max(delta, this.walk(distance, map, this.direction + Math.PI/2));
	        if (controls.forward) delta = Math.max(delta, this.walk(distance, map, this.direction));
	        if (controls.backward) delta = Math.max(delta, this.walk(distance, map, this.direction + Math.PI));
	        this.paces = (this.paces + delta) % (Math.PI*2);
        }
        else {
        	this.paces = 0;
        }
    }
}
