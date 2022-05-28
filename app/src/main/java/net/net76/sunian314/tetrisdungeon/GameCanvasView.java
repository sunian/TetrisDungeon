package net.net76.sunian314.tetrisdungeon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import net.net76.sunian314.tetrisdungeon.GameControls.Explosion;

public class GameCanvasView extends View {
    boolean initialized = false;
    int myWidth, myHeight;
    static int blockSize;
    static int transX, transY;
    Prisoner prisoner;
    TetrisGrid grid;
    static Paint defaultPaint = new Paint(), gameBorderPaint = new Paint(), skyPaint = new Paint();
    static Paint platformPaint = new Paint(), wallPaint = new Paint(), sunlightPaint = new Paint();
    Paint randomPaint = new Paint();
    Path gameBorder = new Path();
    final Handler mHandler = new Handler();
    boolean running = false;
    GameControls myControls;

//	private Runnable mTick = new Runnable() {
//	    public void run() {
//	        invalidate();
//	        mHandler.postDelayed(this, 10); // 20ms == 60fps
//	    }
//	};

    public GameCanvasView(Context context) {
        super(context);
    }

    public GameCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //usually this one is called
    }

    public GameCanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void startGame(final MainActivity mainActivity) {
        if (MainActivity.players > 1) {
            setControls(MainActivity.isPrisoner ? new PrisonerControls(mainActivity) : new TetrisControls(mainActivity));
        } else {
            setControls(new SinglePlayerPrisonerControls(mainActivity, mainActivity.myAIDifficulty));
        }
//		mHandler.removeCallbacks(mTick);
//		mHandler.postDelayed(mTick, 20);
        MainActivity.allowOutput = MainActivity.players > 1;
        running = true;
        invalidate();
        System.out.println("I am the " + (MainActivity.isPrisoner ? "Prisoner" : "Dungeon Master"));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        initialize();
    }

    void initialize() {
        if (initialized) return;
        if (getHeight() > 2 * getWidth()) {
            myWidth = 10 * (getWidth() / 10);
            myHeight = 2 * myWidth;
        } else {
            myHeight = 20 * (getHeight() / 20);
            myWidth = myHeight / 2;
        }
        transX = (getWidth() - myWidth) / 2;
        transY = getHeight() - myHeight;
        blockSize = myWidth / 10;
        initializePaints();

        setupGame();

        gameBorder.moveTo(0, myHeight);
        gameBorder.lineTo(0, 0);
        gameBorder.lineTo(myWidth, 0);
        gameBorder.lineTo(myWidth, myHeight - 1);
        gameBorder.lineTo(0, myHeight - 1);

        initialized = true;
    }

    void setupGame() {
        setControls(null);
        running = false;
        if (grid != null) grid.bmapWalls.recycle();
        grid = new TetrisGrid(myWidth, myHeight);
        prisoner = new Prisoner(myWidth, myHeight, grid);
        MainActivity.tetrisGridView.postInvalidate();
    }

    void initializePaints() {
        wallPaint.setColor(Color.GREEN);

        defaultPaint.setColor(Color.GRAY);
        defaultPaint.setStyle(Style.STROKE);
        defaultPaint.setStrokeWidth(2);

        gameBorderPaint.setStyle(Style.STROKE);
        gameBorderPaint.setColor(Color.GREEN);
        gameBorderPaint.setStrokeWidth(1.0f);
//		gameBorderPaint.setPathEffect(new DashPathEffect(new float[] {8,7}, 0));

        platformPaint.setStyle(Style.STROKE);
        platformPaint.setColor(Color.LTGRAY);
        platformPaint.setStrokeWidth(1.0f);
        platformPaint.setPathEffect(new DashPathEffect(new float[]{6, 5}, 0));

//		skyPaint.setColor(Color.CYAN);
        randomPaint.setStyle(Style.FILL);
//		LinearGradient grad = new LinearGradient(myWidth/2, myHeight/2, myWidth/2, Color.rgb(42, 42, 200), Color.rgb(73, 150, 220), TileMode.CLAMP);
        LinearGradient grad = new LinearGradient(myWidth / 2, transY * -2, myWidth / 2, (myHeight * 7) / 8, 0xB2BED444, Color.TRANSPARENT, TileMode.CLAMP);
//		LinearGradient grad2 = new LinearGradient(myWidth/2, transY * -2, myWidth/2, (myHeight * 2)/3, 0xbbbbbb22, Color.TRANSPARENT, TileMode.CLAMP);
        sunlightPaint.setShader(grad);
//		skyPaint.setShader(grad2);
    }

    public void setControls(GameControls controls) {
        myControls = controls;
        setOnTouchListener(myControls);
    }

    Rect spriteRect = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
//		startTimer();
//		printElapsed();
        super.onDraw(canvas);
        if (!initialized) {
            initialize();
            return;
        }
        canvas.translate(transX, transY);
//		canvas.drawBitmap(grid.bmap, 0, 0, null);
//		printElapsed();
        if (grid.currentPiece != null) canvas.drawBitmap(grid.currentPiece.bmap, 0, 0, null);
//		printElapsed();
        canvas.drawPath(gameBorder, gameBorderPaint);
        if (grid.complete) {
//			canvas.drawRect(1, 0 - transY - 5, myWidth - 1, 0, skyPaint);
            canvas.drawRect(1, 0 - transY - 5, myWidth - 1, myHeight, sunlightPaint);
//			canvas.drawRect(1, 0 - transY - 5, myWidth - 1, myHeight , skyPaint);
        }
        if (grid.blastLineRow > 0) {
//			randomPaint.setColor(Color.HSVToColor(new float[]{(float) (Math.random()*360), 1, 1}));
//			canvas.drawRect(1, blockSize * (19 - grid.blastLineRow) - 1, myWidth - 1, blockSize * (19 - grid.blastLineRow) + 1, randomPaint);
        }
        if (prisoner != null && prisoner.isAlive()) {
            spriteRect.set(prisoner.myBounds.left + prisoner.leftOffset,
                    prisoner.myBounds.top + prisoner.topOffset,
                    prisoner.myBounds.right + prisoner.rightOffset,
                    prisoner.myBounds.bottom + prisoner.bottomOffset);
            canvas.drawBitmap(LauncherActivity.androidSpriteSheet, prisoner.frames, spriteRect, null);
//			canvas.drawRect(prisoner.myBounds, defaultPaint);
            if (prisoner.myBlock != null) {
                int blockX = (prisoner.myBlock.myBounds.left + prisoner.myBlock.myBounds.right) / 2;
                int blockY = (prisoner.myBlock.myBounds.top + prisoner.myBlock.myBounds.bottom) / 2;
                canvas.drawLine(prisoner.inRightHand ? prisoner.myBounds.right : prisoner.myBounds.left, (int) (prisoner.myBounds.top + prisoner.myHeight * 0.7575), blockX, blockY, defaultPaint);
                canvas.drawCircle(blockX, blockY, blockSize / 5, defaultPaint);
            }
        }
        if (myControls != null) {
            synchronized (myControls.explosions) {
                for (Explosion e : myControls.explosions) {
                    canvas.drawBitmap(LauncherActivity.explosionSpriteSheet, e.bitmapFrame, e.positionRect, null);
                }
            }
        }
//		printElapsed();
//		System.out.println("---");
        if (running) invalidate();
//		if (grid.dirty) {
//			grid.draw();
//		}
    }

    static long timer;

    static void startTimer() {
        timer = System.nanoTime();
    }

    static void printElapsed() {
        System.out.println(1000000000.0 / (System.nanoTime() - timer));
        timer = System.nanoTime();
    }
}
