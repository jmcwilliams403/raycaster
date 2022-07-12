package com.raycaster;

import java.util.ArrayList;
import java.util.List;

public class Ray {
    protected class Step {
        protected double x;
        protected double y;
        protected double height;
        protected double distance;
        protected double length;
        protected double shading;
        protected double offset;

        public Step(double x, double y) {
            this(x,y,0,0,0,0,0);
        }

        public Step(double x, double y, double height, double distance, double length, double shading, double offset) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.distance = distance;
            this.length = length;
            this.shading = shading;
            this.offset = offset;
        }
    }

    protected Map map;
    protected double sin;
    protected double cos;
    protected List<Step> steps;

    public Ray(Map map, double x, double y, double angle, double range) {
        this.steps = new ArrayList<Step>();
        this.map = map;
        this.sin = Math.sin(angle);
        this.cos = Math.cos(angle);

        this.cast(x, y, range);
    }

    protected void cast(double x, double y, double range) {
    	Step nextStep = new Step(x, y);
    	do {
    		this.steps.add(nextStep);
            Step stepX = step(sin, cos, nextStep.x, nextStep.y, false);
            Step stepY = step(cos, sin, nextStep.y, nextStep.x, true);
            nextStep = stepX.length < stepY.length
                ? inspect(stepX, 1, 0, nextStep.distance, stepX.y)
                : inspect(stepY, 0, 1, nextStep.distance, stepY.x);
    	}
    	while (nextStep.distance < range);
    }

    protected Step step(double rise, double run, double x, double y, boolean inverted) {
        if (run == 0) return new Step(0, 0, 0, 0, Double.POSITIVE_INFINITY, 0, 0);
        double dx = run > 0 ? Math.floor(x + 1) - x : Math.ceil(x - 1) - x;
        double dy = dx * (rise / run);
        return new Step(inverted ? y + dy : x + dx, inverted ? x + dx : y + dy, 0, 0, Math.hypot(dx, dy), 0, 0);
    }

    protected Step inspect(Step step, double shiftX, double shiftY, double distance, double offset) {
        double dx = cos < 0 ? shiftX : 0;
        double dy = sin < 0 ? shiftY : 0;
        step.height = map.get(step.x - dx, step.y - dy);
        step.distance = distance + step.length;
        if (shiftX == 1) step.shading = cos < 0 ? 2 : 0;
        else step.shading = sin < 0 ? 2 : 1;
        step.offset = offset - Math.floor(offset);
        return step;
    }
}