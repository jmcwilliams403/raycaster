package com.raycaster;

import java.util.ArrayList;
import java.util.List;

public class Ray {
    protected class Step {
        protected double x;
        protected double y;
        protected double height;
        protected double length;
        protected double distance;
        protected double shading;
        protected double offset;

        public Step(double x, double y) {
            this.x = x;
            this.y = y;
            this.height = 0;
            this.length = 0;
            this.distance = 0;
            this.shading = 0;
            this.offset = 0;
        }
        
        public Step(double rise, double run, double x, double y, boolean inverted) {
            if (run == 0) {
            	this.x = 0;
            	this.y = 0;
            	this.height = 0;
            	this.length = Double.POSITIVE_INFINITY;
            	this.distance = 0;
            	this.shading = 0;
            	this.offset = 0;
            }
            else {
            	double dx = run > 0 ? Math.floor(x + 1) - x : Math.ceil(x - 1) - x;
                double dy = dx * (rise / run);
                
                this.x = inverted ? y + dy : x + dx;
                this.y = inverted ? x + dx : y + dy;
                this.height = 0;
                this.length = Math.hypot(dx, dy);
                this.distance = 0;
                this.shading = 0;
                this.offset = 0;
            }
        }
        
        public Step(Step step, double shiftX, double shiftY, double distance, double offset) {
            double dx = cos < 0 ? shiftX : 0;
            double dy = sin < 0 ? shiftY : 0;
            
            this.x = step.x;
            this.y = step.y;
            this.height = map.get(this.x - dx, this.y - dy);
            this.length = step.length;
            this.distance = distance + this.length;
            if (shiftX == 1)
                this.shading = cos < 0 ? 2 : 0;
            else
                this.shading = sin < 0 ? 2 : 1;
            this.offset = offset - Math.floor(offset);
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
            Step stepX = new Step(sin, cos, nextStep.x, nextStep.y, false);
            Step stepY = new Step(cos, sin, nextStep.y, nextStep.x, true);
            nextStep = stepX.length < stepY.length
                ? new Step(stepX, 1, 0, nextStep.distance, stepX.y)
                : new Step(stepY, 0, 1, nextStep.distance, stepY.x);
    	}
    	while (nextStep.distance < range);
    }
}