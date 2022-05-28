package net.net76.sunian314.tetrisdungeon;

import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;

public class PrisonerControls extends GameControls {
    Thread prisonerPhysicsThread, tetrisThread;
    MainActivity mainActivity;
    GameCanvasView gameCanvasView;
    float xDown, yDown;
    float xPrev, yPrev;
    boolean isTap = false;
    long tDown, tPrev;

    public PrisonerControls(MainActivity act) {
        super();
        mainActivity = act;
        gameCanvasView = MainActivity.gameCanvasView;
//		TetrisPiece.createChecklist();
//		TetrisPiece.createFallOrders();
        TetrisPiece.createRotationOffsets();
        createPrisonerPhysics();
        createTicker();
        createTetrisControlThread();
    }

    private void setDownHere(MotionEvent event) {
        xDown = event.getX();
        yDown = event.getY();
        tDown = event.getEventTime();
        setPrevHere(event);
    }

    private void setPrevHere(MotionEvent event) {
        xPrev = event.getX();
        yPrev = event.getY();
        tPrev = event.getEventTime();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int swipeLength = gameCanvasView.myWidth / 5;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setDownHere(event);
                isTap = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTap && (Math.abs(event.getX() - xDown) > swipeLength ||
                        Math.abs(event.getY() - yDown) > swipeLength)) {
                    isTap = false;
                }
                float dx = event.getX() - xPrev, dy = event.getY() - yPrev;
                long dt = event.getEventTime() - tPrev;
                if ((dx * dx > swipeLength * swipeLength) || (dy * dy > swipeLength * swipeLength)) {
                    if (gameCanvasView.prisoner.isAlive()) {
                        if (dx > swipeLength)
                            gameCanvasView.prisoner.moveRight(event.getPressure());
                        if (dx < -swipeLength)
                            gameCanvasView.prisoner.moveLeft(event.getPressure());
                        if (dy > swipeLength) gameCanvasView.prisoner.drop();
                        if (dy < -swipeLength) gameCanvasView.prisoner.jump();
                    }

                    setPrevHere(event);
                } else if (dt > 666) {
                    setPrevHere(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                gameCanvasView.prisoner.stop();
                if (isTap) {
                    if (gameCanvasView.prisoner.isAlive()) {
                        gameCanvasView.prisoner.toggleGrip();
                    }
                }
                break;
            default:
                System.out.println("touch action " + event.getAction());
                break;
        }

        return true;
    }

    void createPrisonerPhysics() {
        prisonerPhysicsThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (gameCanvasView.prisoner == null) ;
                long previousTime = System.currentTimeMillis();
                while (running) {
                    long time = System.currentTimeMillis();
                    long dt = time - previousTime;
                    previousTime = time;
                    int status = gameCanvasView.prisoner.gameUpdate(dt);
                    if (status == 1) {
                        MainActivity.writeToStream(TetrisControls.PRISONER_ESCAPE);
                        MainActivity.myScore++;
                    }
                    if (status != 0) running = false;
//					try {Thread.sleep(666);} catch (InterruptedException e) {e.printStackTrace();}
//					previousTime = System.currentTimeMillis();
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("P conn: " + MainActivity.connected + "   new: " + MainActivity.startNew);
                if (MainActivity.connected && MainActivity.startNew) {
                    mainActivity.startNewGame();
                }
            }
        });
        prisonerPhysicsThread.start();
    }

    private void createTicker() {
        tetrisThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (gameCanvasView.grid == null) ;
                while (running) {
                    long time = System.currentTimeMillis();
                    int status = gameCanvasView.grid.gameUpdate();
                    if (status < 0) {
                        break;
                    }
                    long dt = System.currentTimeMillis() - time;
                    if (dt > 600) dt = 600;
//					System.out.println(dt);
                    try {
                        Thread.sleep((status == 1 ? 666 : 1337) - dt);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        tetrisThread.start();
    }

    void createTetrisControlThread() {

        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (MainActivity.connected && running) {
                        int input = MainActivity.inStream.read();
                        if (input < 0) break;
                        if (input == TetrisControls.QUIT_GAME) {
                            running = false;
                            mainActivity.quitGame(false);
                            break;
                        }
                        if (gameCanvasView.grid.currentPiece == null) {
                            TetrisPiece.transmitNULL();
                        }
                        switch (input) {
                            case 'U':
                                if (gameCanvasView.grid.currentPiece != null) {
                                    gameCanvasView.grid.currentPiece.rotate();
                                    gameCanvasView.grid.currentPiece.transmit();
                                    gameCanvasView.grid.currentPiece.draw();
                                }
                                break;
                            case 'L':
                                if (gameCanvasView.grid.currentPiece != null) {
                                    gameCanvasView.grid.currentPiece.move(false);
                                    gameCanvasView.grid.currentPiece.transmit();
                                    gameCanvasView.grid.currentPiece.draw();
                                }
                                break;
                            case 'R':
                                if (gameCanvasView.grid.currentPiece != null) {
                                    gameCanvasView.grid.currentPiece.move(true);
                                    gameCanvasView.grid.currentPiece.transmit();
                                    gameCanvasView.grid.currentPiece.draw();
                                }
                                break;
                            case 'D':
                                if (gameCanvasView.grid.currentPiece != null) {
                                    gameCanvasView.grid.currentPiece.drop();
                                    gameCanvasView.grid.currentPiece.transmit();
                                    gameCanvasView.grid.currentPiece.draw();
                                }
                                break;
                            case 'T':
                                if (gameCanvasView.grid.currentPiece != null) {
                                    gameCanvasView.grid.gameUpdate();
//								gameCanvasView.grid.currentPiece.draw();
                                }
                                break;
                            case TetrisControls.FORFEIT:
                                MainActivity.allowOutput = false;
                                MainActivity.myScore++;
                                MainActivity.startNew = true;
                                running = false;
                                break;

                            default:
                                break;
                        }
                    }
                } catch (IOException e) {
                    mainActivity.showToast("error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        readThread.start();
    }
}
