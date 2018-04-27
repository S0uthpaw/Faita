package com.example.tyler.spaceinvaders_2;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Tyler on 2/21/2018.
 */

public class SpaceInvadersView extends SurfaceView implements Runnable {


    public SpaceInvadersView(Context context, int x, int y) throws IOException {

        // The next line of code asks the
        // SurfaceView class to set up our object.
        // How kind.
        super(context);

        // globally available copy of the context so we can use it in another method
        this.context = context;

        // Initializes ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        // SoundPool deprecated - will look in to it later
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

        try{
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        }catch(IOException e){
            // Prints error message to console
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();
    }

    private void prepareLevel(){

        // initialize all the game objects

        // new player ship
        player = new Player(context, screenX, screenY);
        // players projectile
        proj = new Projectile(screenY);
        // invadersBullets array
        for(int i = 0; i < invadersBullets.length; i++){
            invadersBullets[i] = new Projectile(screenY);
        }
        // army of invaders
        numInvaders = 0;
        for(int column = 0; column < 6; column ++ ){
            for(int row = 0; row < 5; row ++ ){
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
                numInvaders ++;
            }
        }

        // create the shelters
        numBricks = 0;
        for(int shelterNumber = 0; shelterNumber < 4; shelterNumber++){
            for(int column = 0; column < 10; column ++ ) {
                for (int row = 0; row < 5; row++) {
                    bricks[numBricks] = new Defense(row, column, shelterNumber, screenX, screenY);
                    numBricks++;
                }
            }
        }
    }
    @Override
    public void run() {
        while (playing) {

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            if (!paused) {
                update();
            }

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // use to time animations
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

            if(!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {

                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {

                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // Reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // Alter value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
            }

        }
    }

        private void update(){
            // checks player win/lose condition
            boolean lost = false;

            // Did an invader bump into the side of the screen
            boolean bumped = false;
            if(bumped){

                // Move all the invaders down and change direction
                for(int i = 0; i < numInvaders; i++){
                    invaders[i].dropDownAndReverse();
                    // Have the invaders landed
                    if(invaders[i].getY() > screenY - screenY / 10){
                        lost = true;
                    }
                }



                // Increase the menace level
                // By making the sounds more frequent
                menaceInterval = menaceInterval - 80;
            }

            // Move the player's ship
            player.update(fps);
            // Update all the invaders if visible
            for(int i = 0; i < numInvaders; i++){

                if(invaders[i].getVisibility()) {
                    // Move the next invader
                    invaders[i].update(fps);

                    // Does he want to take a shot?
                    if(invaders[i].takeAim(player.getX(),
                            player.getLength())){

                        // If so try and spawn a proj
                        if(invadersBullets[nextBullet].shoot(invaders[i].getX()
                                        + invaders[i].getLength() / 2,
                                invaders[i].getY(), proj.DOWN)) {

                            // Shot fired
                            // Prepare for the next shot
                            nextBullet++;

                            // Loop back to the first one if we have reached the last
                            if (nextBullet == maxInvaderBullets) {
                                // This stops the firing of another proj until one completes its journey
                                // Because if proj 0 is still active shoot returns false.
                                nextBullet = 0;
                            }
                        }
                    }

                    // If that move caused them to bump the screen change bumped to true
                    if (invaders[i].getX() > screenX - invaders[i].getLength()
                            || invaders[i].getX() < 0){

                        bumped = true;

                    }
                }

            }

                // Update all the invaders bullets if active
            for(int i = 0; i< invadersBullets.length; i++){
                if(invadersBullets[i].getStatus()){
                    invadersBullets[i].update(fps);
                }
            }
            // Did an invader bump into the edge of the screen
            if(bumped){

                // Move all the invaders down and change direction
                for(int i = 0; i < numInvaders; i++){
                    invaders[i].dropDownAndReverse();
                    // Have the invaders landed
                    if(invaders[i].getY() > screenY - screenY / 10){
                        lost = true;
                    }
                }

                // Increase the menace level
                // By making the sounds more frequent
                menaceInterval = menaceInterval - 80;
            }

            if(lost){
                prepareLevel();
            }

            // Update the players proj
            if(proj.getStatus()){
                proj.update(fps);
            }
            // Has the player's proj hit the top of the screen
            if(proj.getImpactPointY() < 0){
                proj.setInactive();
            }

            // Did invaders proj hit the bottom of the screen
            for(int i = 0; i < invadersBullets.length; i++){
                if(invadersBullets[i].getImpactPointY() > screenY){
                    invadersBullets[i].setInactive();
                }
            }
            // Did player's proj hit an invader

            if(proj.getStatus()) {
                for (int i = 0; i < numInvaders; i++) {
                    if (invaders[i].getVisibility()) {
                        if (RectF.intersects(proj.getRect(), invaders[i].getRect())) {
                            invaders[i].setInvisible();
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                            proj.setInactive();
                            score = score + 10;

                            // Has the player won
                            if(score == numInvaders * 10){
                                paused = true;
                                score = 0;
                                lives = 3;
                                prepareLevel();
                            }
                        }
                    }
                }
            }
            // check to see if an invader proj hit a brick
            for(int i = 0; i < invadersBullets.length; i++){
                if(invadersBullets[i].getStatus()){
                    for(int j = 0; j < numBricks; j++){
                        if(bricks[j].getVisibility()){
                            if(RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())){
                                // A collision has occurred
                                invadersBullets[i].setInactive();
                                bricks[j].setInvisible();
                                soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                            }
                        }
                    }
                }

            }

            // check to see if a player proj hit a brick
            if(proj.getStatus()){
                for(int i = 0; i < numBricks; i++){
                    if(bricks[i].getVisibility()){
                        if(RectF.intersects(proj.getRect(), bricks[i].getRect())){
                            // Collision detected
                            proj.setInactive();
                            bricks[i].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }

            // Has an invader proj hit the player ship
            for(int i = 0; i < invadersBullets.length; i++){
                if(invadersBullets[i].getStatus()){
                    if(RectF.intersects(player.getRect(), invadersBullets[i].getRect())){
                        invadersBullets[i].setInactive();
                        lives --;
                        soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                        // Is it game over?
                        if(lives == 0){
                            paused = true;
                            lives = 3;
                            score = 0;
                            prepareLevel();

                        }
                    }
                }
            }

        }

        private void draw(){
            // Validates drawing surface
            if (ourHolder.getSurface().isValid()) {
                // Lock the canvas ready to draw
                canvas = ourHolder.lockCanvas();

                // creates background
                canvas.drawColor(Color.argb(255, 26, 128, 182));

                // brush color
                paint.setColor(Color.argb(255,  255, 255, 255));

                // Draw the player spaceship
                canvas.drawBitmap(player.getBitmap(), player.getX(), screenY - player.getHeight(), paint);
                // Draw the invaders
                for(int i = 0; i < numInvaders; i++){
                    if(invaders[i].getVisibility()) {
                        if(uhOrOh) {
                            canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                        }else{
                            canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                        }
                    }
                }
                // Draw the bricks if visible
                for(int i = 0; i < numBricks; i++){
                    if(bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                // Draw the players proj if active
                if(proj.getStatus()){
                    canvas.drawRect(proj.getRect(), paint);
                }
                // Draw the invaders proj if active

                // Update all invader projectiles if active
                for(int i = 0; i < invadersBullets.length; i++){
                    if(invadersBullets[i].getStatus()) {
                        canvas.drawRect(invadersBullets[i].getRect(), paint);
                    }
                }
                // Draw the score and remaining lives
                // Change the brush color
                paint.setColor(Color.argb(255,  249, 129, 0));
                paint.setTextSize(40);
                canvas.drawText("Score: " + score + "   Lives: " + lives, 10,50, paint);

                // Draw everything to the screen
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

    // If SpaceInvadersActivity is paused/stopped
    // shutdown our thread.
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    // If SpaceInvadersActivity is started then
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // The SurfaceView class implements onTouchListener
    // We can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                paused = false;

                if(motionEvent.getY() > screenY - screenY / 8) {
                    if (motionEvent.getX() > screenX / 2) {
                        player.setMovementState(player.RIGHT);
                    } else {
                        player.setMovementState(player.LEFT);
                    }

                }

                if(motionEvent.getY() < screenY - screenY / 8) {
                    // Shots fired
                    if(proj.shoot(player.getX()+
                            player.getLength()/2,screenY,proj.UP)){
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }
                break;



            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                if(motionEvent.getY() > screenY - screenY / 10) {
                    player.setMovementState(player.STOPPED);
                }
                break;
        }
        return true;
    }


    Context context;

    // This is our thread
    private Thread gameThread = null;

    // Our SurfaceHolder to lock the surface before we draw our graphics
    private SurfaceHolder ourHolder;

    // A boolean which we will set and unset
    // when the game is running- or not.
    private volatile boolean playing;

    // Game is paused at the start
    private boolean paused = true;

    // A Canvas and a Paint object
    private Canvas canvas;
    private Paint paint;

    // This variable tracks the game frame rate
    private long fps;

    // This is used to help calculate the fps
    private long timeThisFrame;

    // The size of the screen in pixels
    private int screenX;
    private int screenY;

    // The players ship
    private Player player;

    // The player's bullet
    private Projectile proj;

    // The invaders bullets
    private Projectile[] invadersBullets = new Projectile[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;

    // Up to 60 invaders
    Invader[] invaders = new Invader[60];
    int numInvaders = 0;

    // The player's shelters are built from bricks
    private Defense[] bricks = new Defense[400];
    private int numBricks;

    // For sound FX
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;


    int score = 0;


    private int lives = 3;

    // menace frequency?
    private long menaceInterval = 1000;
    // Which menace sound should play next
    private boolean uhOrOh;
    // last time a menacing sound was played
    private long lastMenaceTime = System.currentTimeMillis();

}