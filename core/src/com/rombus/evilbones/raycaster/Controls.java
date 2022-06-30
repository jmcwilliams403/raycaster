package com.rombus.evilbones.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Controls {
    protected boolean left, right, strafeLeft, strafeRight, forward, backward;
    private int oldX;

    public Controls() {
        left = right = strafeLeft = strafeRight = forward = backward = false;
        oldX = Gdx.input.getX();

        Gdx.input.setCursorCatched(true);
    }

    public void update() {
        left = right = strafeLeft = strafeRight = forward = backward = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            strafeLeft= true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            strafeRight = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            forward = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            backward = true;
        }


        // Add mouse movement
        if (oldX > Gdx.input.getX()){
            left = true;
        }
        else if(oldX < Gdx.input.getX()){
            right = true;
        }
        oldX = Gdx.input.getX();
    }
}
