package com.rombus.evilbones.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Player {
    protected double x;
    protected double y;
    protected double direction;
    protected double paces;

    protected Texture weapon;
    protected float speed;

    public Player(double x, double y, double direction) {
        this.x = x;
        this.y = y;
        this.direction = Math.toRadians(direction);
        this.paces = 0;
        this.speed = 3;
        this.weapon = new Texture(Gdx.files.internal("knife_hand.png"));
    }

    public void rotate(double angle) {
        this.direction = (this.direction + angle + Raycaster.TAU) % (Raycaster.TAU);
    }

    public void walk(double distance, Map map, double direction) {
        double dx = Math.cos(direction) * distance;
        double dy = Math.sin(direction) * distance;
        if (map.get(this.x + dx, this.y) <= 0) this.x += dx;
        if (map.get(this.x, this.y + dy) <= 0) this.y += dy;
    }

    public void update(Controls controls, Map map, double seconds) {
        if (controls.turnLeft) this.rotate(-Math.PI * seconds);
        if (controls.turnRight) this.rotate(Math.PI * seconds);
        
        if (controls.move) {
	        double distance = this.speed * seconds;
	        if (controls.left) this.walk(distance, map, this.direction - Math.PI/2);
	        if (controls.right) this.walk(distance, map, this.direction + Math.PI/2);
	        if (controls.forward) this.walk(distance, map, this.direction);
	        if (controls.backward) this.walk(distance, map, this.direction + Math.PI);
	        this.paces = (this.paces + distance) % Math.PI;
        }
        else {
        	this.paces = 0;
        }
    }

    public Point toPoint() {
        return new Point(this.x, this.y);
    }
}
