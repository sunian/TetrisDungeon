package net.net76.sunian314.tetrisdungeon;

import java.io.IOException;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class PrisonerControls extends GameControls {
	Thread prisonerPhysicsThread, tetrisThread;
	MainActivity mainActivity;
	GameCanvasView gameCanvasView;
	float xDown, yDown;
	boolean isTap = false;
	long tDown;
	
	public PrisonerControls(MainActivity act){
		super();
		mainActivity = act;
		gameCanvasView = MainActivity.gameCanvasView;
//		TetrisPiece.createChecklist();
//		TetrisPiece.createFallOrders();
		TetrisPiece.createRotationOffsets();
		createPrisonerPhysics();
		createTicker();
		createReadThread();
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
				if (gameCanvasView.prisoner.isAlive()){
					if (dx > swipeLength) gameCanvasView.prisoner.moveRight(event.getPressure());
					if (dx < -swipeLength) gameCanvasView.prisoner.moveLeft(event.getPressure());
					if (dy > swipeLength) gameCanvasView.prisoner.drop();
					if (dy < -swipeLength) gameCanvasView.prisoner.jump();
				}
				
				setDownHere(event);
			} else if (dt > 666){
				setDownHere(event);
			} else {
//				isTap = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			gameCanvasView.prisoner.stop();
			if (isTap){
				if (gameCanvasView.prisoner.isAlive()){
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
	private void createPrisonerPhysics() {
		prisonerPhysicsThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (gameCanvasView.prisoner == null);
				long previousTime = System.currentTimeMillis();
				while (running) {
					long time = System.currentTimeMillis();
					long dt = time - previousTime;
					previousTime = time;
					int status = gameCanvasView.prisoner.gameUpdate(dt); 
					if (status == 1){
						MainActivity.writeToStream(TetrisControls.PRISONER_ESCAPE);
						MainActivity.myScore++;
					}
					if (status != 0) running = false;
//					try {Thread.sleep(666);} catch (InterruptedException e) {e.printStackTrace();}
//					previousTime = System.currentTimeMillis();
					try {Thread.sleep(16);} catch (InterruptedException e) {e.printStackTrace();}
				}
				System.out.println("P conn: " + MainActivity.connected + "   new: " + MainActivity.startNew);
				if (MainActivity.connected && MainActivity.startNew){
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
				while (gameCanvasView.grid == null);
				while (running) {
					long time = System.currentTimeMillis();
					int status = gameCanvasView.grid.gameUpdate(); 
					if (status < 0) {
						break;
					}
					long dt = System.currentTimeMillis() - time;
					if (dt > 600) dt = 600;
//					System.out.println(dt);
					try {Thread.sleep((status == 1 ? 666 : 1337) - dt);} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		});
		tetrisThread.start();
	}
	private void createReadThread(){

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
						if (gameCanvasView.grid.currentPiece == null){
							TetrisPiece.transmitNULL();
						}
						switch (input) {
						case 'U':
							if (gameCanvasView.grid.currentPiece != null){
								gameCanvasView.grid.currentPiece.rotate();
								gameCanvasView.grid.currentPiece.transmit();
								gameCanvasView.grid.currentPiece.draw();
							}
							break;
						case 'L':
							if (gameCanvasView.grid.currentPiece != null){
								gameCanvasView.grid.currentPiece.move(false);
								gameCanvasView.grid.currentPiece.transmit();
								gameCanvasView.grid.currentPiece.draw();
							}
							break;
						case 'R':
							if (gameCanvasView.grid.currentPiece != null){
								gameCanvasView.grid.currentPiece.move(true);
								gameCanvasView.grid.currentPiece.transmit();
								gameCanvasView.grid.currentPiece.draw();
							}
							break;
						case 'D':
							if (gameCanvasView.grid.currentPiece != null){
								gameCanvasView.grid.currentPiece.drop();
								gameCanvasView.grid.currentPiece.transmit();
								gameCanvasView.grid.currentPiece.draw();
							}
							break;
						case 'T':
							if (gameCanvasView.grid.currentPiece != null){
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
