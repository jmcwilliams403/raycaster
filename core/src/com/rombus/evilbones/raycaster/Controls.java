package com.rombus.evilbones.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Controls {
    protected boolean turnLeft, turnRight, left, right, forward, backward, move;
    private int oldX;

    public Controls() {
        turnLeft = turnRight = left = right = forward = backward = false;
        oldX = Gdx.input.getX();

        Gdx.input.setCursorCatched(true);
    }

    public void update() {
        turnLeft = turnRight = left = right = forward = backward = move = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            left = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            right = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            forward = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            backward = true;
        }
        
        move = (left || right || forward || backward);

        // Add mouse movement
        if (oldX > Gdx.input.getX()){
            turnLeft = true;
        }
        else if(oldX < Gdx.input.getX()){
            turnRight = true;
        }
        oldX = Gdx.input.getX();
    }
}
