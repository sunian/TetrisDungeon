package net.net76.sunian314.tetrisdungeon;

public class SinglePlayerPrisonerControls extends PrisonerControls {
    int difficulty;

    public SinglePlayerPrisonerControls(MainActivity act, int diff) {
        super(act);
        difficulty = diff;
    }

    @Override
    void createPrisonerPhysics() {
        prisonerPhysicsThread = new Thread(() -> {
            while (gameCanvasView.prisoner == null) ;
            long previousTime = System.currentTimeMillis();
            while (running) {
                long time = System.currentTimeMillis();
                long dt = time - previousTime;
                previousTime = time;
                int status = gameCanvasView.prisoner.gameUpdate(dt);
                if (status == 1) {
                    MainActivity.myScore++;
                }
                if (status != 0) running = false;
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            running = false;
            mainActivity.quitGame(false);
        });
        prisonerPhysicsThread.start();
    }

    @Override
    void createTetrisControlThread() {//controlled by AI

        Thread thread = new Thread(() -> {
            int[][] grid = new int[20][10];
            TetrisStrategyPierreDellacherie blockCurrent = new TetrisStrategyPierreDellacherie();
            int delay = 50;
            if (difficulty == 0) {
                delay = 450;
            } else if (difficulty == 1) {
                delay = 200;
            }
            while (running) {
                if (gameCanvasView.grid.currentPiece == null) {
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    for (int i = 0; i < 20; i++) {
                        for (int j = 0; j < 10; j++) {
                            grid[i][j] = gameCanvasView.grid.canGrabBlock(i, j) ? 1 : 0;
                        }
                    }
                    if (gameCanvasView.grid.currentPiece == null) continue;
                    blockCurrent.evaluateBestMove(grid, gameCanvasView.grid.currentPiece.type, false);
                    if (gameCanvasView.grid.currentPiece == null) continue;
                    if (blockCurrent.optimalRot != gameCanvasView.grid.currentPiece.orientation) {
                        gameCanvasView.grid.currentPiece.rotate();
                    } else if (blockCurrent.optimalLoc < gameCanvasView.grid.currentPiece.getLocation()) {
                        gameCanvasView.grid.currentPiece.move(false);
                    } else if (blockCurrent.optimalLoc > gameCanvasView.grid.currentPiece.getLocation()) {
                        gameCanvasView.grid.currentPiece.move(true);
                    } else {
                        gameCanvasView.grid.currentPiece.drop();
                        gameCanvasView.grid.gameUpdate();
                    }
                    if (gameCanvasView.grid.currentPiece != null)
                        gameCanvasView.grid.currentPiece.draw();
                    try {
                        Thread.sleep((int) (Math.random() * delay + delay));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
}
