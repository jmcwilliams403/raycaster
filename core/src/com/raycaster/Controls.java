package com.raycaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Controls {
    protected boolean turnLeft, turnRight, left, right, forward, backward, move, turn;
    protected int x;

    public Controls() {
        turnLeft = turnRight = left = right = forward = backward = move = turn = false;
        x = 0;
        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(x,0);
    }

    public void update() {
        turnLeft = turnRight = left = right = forward = backward = move = turn = false;

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
        
        move = (left ^ right) || (forward ^ backward);

        // Add mouse movement
        x = Gdx.input.getDeltaX();
        if (x < 0){
            turnLeft = true;
        }
        else if(x > 0){
            turnRight = true;
        }
        
        turn = turnLeft ^ turnRight;
    }
}
