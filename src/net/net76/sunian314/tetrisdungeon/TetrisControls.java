package net.net76.sunian314.tetrisdungeon;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import net.net76.sunian314.tetrisdungeon.R;

public class TetrisControls implements OnTouchListener {
	Thread gameThread;
	GameCanvasView gameCanvasView;
	float xDown, yDown;
	long tDown;
	
	public TetrisControls(GameCanvasView v){
		gameCanvasView = v;
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
			break;
		case MotionEvent.ACTION_MOVE:
			float dx= event.getX() - xDown, dy= event.getY() - yDown;
			long dt = event.getEventTime() - tDown;
			if ((dx*dx > swipeLength * swipeLength) || (dy*dy > swipeLength * swipeLength)){
//				if (dx > swipeLength) gameCanvasView.gremlin.runRight();
//				if (dx < -swipeLength) gameCanvasView.gremlin.runLeft();
//				if (dy > swipeLength) gameCanvasView.gremlin.drop();
//				if (dy < -swipeLength) gameCanvasView.gremlin.jump();
				
				setDownHere(event);
			} else if (dt > 666){
				setDownHere(event);
			}
			break;
		case MotionEvent.ACTION_UP:
			gameCanvasView.gremlin.stop();
			break;
		default:
			System.out.println("touch action " + event.getAction());
			break;
		}
			
		return true;
	}
	private void createTicker() {
		gameThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (gameCanvasView.grid == null);
				while (true) {
					long time = System.currentTimeMillis();
					gameCanvasView.grid.gameUpdate();
					long dt = System.currentTimeMillis() - time;
					try {Thread.sleep(1000 - dt);} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		});
		gameThread.start();
	}
}
