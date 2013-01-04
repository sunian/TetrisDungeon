package net.net76.sunian314.tetrisdungeon;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import net.net76.sunian314.tetrisdungeon.R;

public class GremlinControls implements OnTouchListener {
	Thread gremlinPhysicsThread, tetrisThread;
	GameCanvasView gameCanvasView;
	float xDown, yDown;
	boolean isTap = false;
	long tDown;
	
	public GremlinControls(GameCanvasView v){
		gameCanvasView = v;
//		TetrisPiece.createChecklist();
//		TetrisPiece.createFallOrders();
		TetrisPiece.createRotationOffsets();
		createGremlinPhysics();
		createTicker();
	}
	private void setDownHere(MotionEvent event){
		xDown = event.getX();
		yDown = event.getY();
		tDown = event.getEventTime();
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int swipeLength = gameCanvasView.myWidth/5;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setDownHere(event);
			isTap = true;
			break;
		case MotionEvent.ACTION_MOVE:
			isTap = false;
			float dx= event.getX() - xDown, dy= event.getY() - yDown;
			long dt = event.getEventTime() - tDown;
			if ((dx*dx > swipeLength * swipeLength) || (dy*dy > swipeLength * swipeLength)){
				if (gameCanvasView.gremlin.isAlive()){
					if (dx > swipeLength) gameCanvasView.gremlin.moveRight(event.getPressure());
					if (dx < -swipeLength) gameCanvasView.gremlin.moveLeft(event.getPressure());
					if (dy > swipeLength) gameCanvasView.gremlin.drop();
					if (dy < -swipeLength) gameCanvasView.gremlin.jump();
				}
				
				if (gameCanvasView.grid.currentPiece != null){
					if (dx > swipeLength) gameCanvasView.grid.currentPiece.move(true);
					if (dx < -swipeLength) gameCanvasView.grid.currentPiece.move(false);
					if (dy > swipeLength) gameCanvasView.grid.gameUpdate();
					if (dy < -swipeLength) gameCanvasView.grid.currentPiece.rotate();
					if (gameCanvasView.grid.currentPiece != null) gameCanvasView.grid.currentPiece.draw();
				}
				
				setDownHere(event);
			} else if (dt > 666){
				setDownHere(event);
			} else {
//				isTap = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			gameCanvasView.gremlin.stop();
			if (isTap){
				if (gameCanvasView.gremlin.isAlive()){
					gameCanvasView.gremlin.toggleGrip();
				}
				if (gameCanvasView.grid.currentPiece != null)
					gameCanvasView.grid.gameUpdate();	
			}
			break;
		default:
			System.out.println("touch action " + event.getAction());
			break;
		}
			
		return true;
	}
	private void createGremlinPhysics() {
		gremlinPhysicsThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (gameCanvasView.gremlin == null);
				long previousTime = System.currentTimeMillis();
				while (true) {
					long time = System.currentTimeMillis();
					long dt = time - previousTime;
					previousTime = time;
					gameCanvasView.gremlin.gameUpdate(dt);
//					try {Thread.sleep(666);} catch (InterruptedException e) {e.printStackTrace();}
//					previousTime = System.currentTimeMillis();
					try {Thread.sleep(16);} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		});
		gremlinPhysicsThread.start();
	}
	private void createTicker() {
		tetrisThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (gameCanvasView.grid == null);
				while (true) {
					long time = System.currentTimeMillis();
					if (gameCanvasView.grid.gameUpdate() < 0)
						try {
							tetrisThread.join();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					long dt = System.currentTimeMillis() - time;
//					System.out.println(dt);
					try {Thread.sleep(1337 - dt);} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		});
		tetrisThread.start();
	}
}
