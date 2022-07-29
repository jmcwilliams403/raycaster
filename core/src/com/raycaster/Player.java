package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.Texture;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;

public class Player implements Disposable {
	protected double x;
	protected double y;
	protected double direction;
	protected double paces;
	protected double weaponScale;

	protected Texture weapon;
	protected float speed;

	public Player(double x, double y) {
		this(x, y, MathTools.FLOAT_ROUNDING_ERROR);
	}

	public Player(double x, double y, double direction) {
		this.x = x;
		this.y = y;
		this.direction = MathTools.truncate(direction);
		this.paces = 0;
		this.speed = 3;
		this.weapon = new Texture(Gdx.files.internal("hand.png"));
		this.weaponScale = 0.5f;
	}

	public double rotate(double angle) {
		final double direction = this.direction;
		this.direction = MathTools.remainder(direction + angle, TrigTools.TAU_D);
		return Math.copySign(this.direction - direction, angle) / 2;
	}

	public double walk(double distance, Map map, double direction) {
		double dx = Math.cos(direction) * distance, dy = Math.sin(direction) * distance;

		if (map.get(this.x + dx, this.y) <= 0)
			this.x += dx;
		else
			dx = 0;

		if (map.get(this.x, this.y + dy) <= 0)
			this.y += dy;
		else
			dy = 0;

		return Math.hypot(dx, dy);
	}

	public void update(Controls controls, Map map, double seconds) {
		if (controls.turn)
			this.rotate((controls.x / TrigTools.PI_D) * seconds);

		final double distance = this.speed * seconds;
		if (controls.move) {
			double delta = 0;
			if (controls.left)
				delta = Math.max(delta, this.walk(distance, map, this.direction - TrigTools.HALF_PI_D));
			if (controls.right)
				delta = Math.max(delta, this.walk(distance, map, this.direction + TrigTools.HALF_PI_D));
			if (controls.forward)
				delta = Math.max(delta, this.walk(distance, map, this.direction));
			if (controls.backward)
				delta = Math.max(delta, this.walk(distance, map, this.direction + TrigTools.PI_D));
			this.paces = (this.paces + delta) % TrigTools.TAU_D;
		} else if (this.paces > 0) {
			double closer = (Math.abs(this.paces - TrigTools.PI_D) > TrigTools.HALF_PI_D) ? 0 : TrigTools.PI_D;
			this.paces = MathTools.lerpAngle(this.paces, closer, distance * 2);
			if (MathTools.isZero((float)MathTools.truncate(this.paces - closer)))
				this.paces = 0;
		}
	}

	@Override
	public void dispose() {
		this.weapon.dispose();
	}
}
