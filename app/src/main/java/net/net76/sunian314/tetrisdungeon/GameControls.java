package net.net76.sunian314.tetrisdungeon;

import android.graphics.Rect;
import android.view.View.OnTouchListener;

import java.util.ArrayList;

public abstract class GameControls implements OnTouchListener {
    public boolean running = true;
    public ArrayList<Explosion> explosions = new ArrayList<>();
    static int explosionSpriteSize;

    public GameControls() {
        explosionSpriteSize = LauncherActivity.explosionSpriteSheet.getHeight();
    }

    public void killThreads() {
        running = false;
    }

    public void createExplosionThread(final int row) {
        Thread thread = new Thread(() -> {
            ArrayList<Explosion> temp = new ArrayList<>();
            synchronized (explosions) {
                for (int i = 0; i < 10; i++) {
                    Explosion e = new Explosion(row, i);
                    temp.add(e);
                    explosions.add(e);
                }
            }
            for (int i = 0; i < 5; i++) {
                for (Explosion e : temp) {
                    e.updateFrame(i);
                }
                try {
                    Thread.sleep(65);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (explosions) {
                explosions.removeAll(temp);
            }
        });
        thread.start();
    }

    public static class Explosion {
        Rect bitmapFrame, positionRect;

        //		int frameNum = 0;
//		int frameCounter = 0;
//		static final int frameDuration = 100;
        public Explosion(int row, int col) {
            bitmapFrame = new Rect(0, 0, explosionSpriteSize, explosionSpriteSize);
            positionRect = new Rect();
            positionRect.left = (int) (GameCanvasView.blockSize * (col - 0.904761));
            positionRect.top = (int) ((19 - row - 6.0 / 7.0) * GameCanvasView.blockSize);
            positionRect.right = (int) (GameCanvasView.blockSize * (col + 1.904761));
            positionRect.bottom = (int) ((20 - row + .9524) * GameCanvasView.blockSize);
        }

        void updateFrame(int frameNum) {
//			frameNum++;
//			if (frameNum > 4) return true;
            bitmapFrame.left = frameNum * explosionSpriteSize;
            bitmapFrame.right = bitmapFrame.left + explosionSpriteSize;
//			return false;
        }
    }
}
