package net.net76.sunian314.tetrisdungeon;

import java.io.IOException;
import java.util.Arrays;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TetrisControls implements OnTouchListener {
	static final char CURRENT_PIECE = '#';
	static final char GRID = '+';
	static final char WALL = '|';
	static final char PLATFORM = '_';
	static final char PRISONER = 'P';
	static final char NULL_TYPE = 'N';
	static final char PRISONER_DEAD = 'X';
	static final char SKY_OPEN = '^';
	
	Thread gameThread;
	MainActivity mainActivity;
	GameCanvasView gameCanvasView;
	float xDown, yDown;
	long tDown;
	boolean isTap = false, mustReset = false;
	
	public TetrisControls(MainActivity act){
		mainActivity = act;
		gameCanvasView = MainActivity.gameCanvasView;
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
				if (dx > swipeLength) sendToHost('R');
				if (dx < -swipeLength) sendToHost('L');
				if (dy > swipeLength && !mustReset) {sendToHost('D'); mustReset = true;}
				if (dy < -swipeLength && !mustReset) {sendToHost('U'); mustReset = true;}
				
				setDownHere(event);
			} else if (dt > 666){
				setDownHere(event);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (isTap){
				sendToHost('T');
			}
			mustReset = false;
			break;
		default:
			System.out.println("touch action " + event.getAction());
			break;
		}
			
		return true;
	}
	private void createReadThread(){

        Thread readThread = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] gridBuffer = new byte[2];
				byte[] pieceBuffer = new byte[6];
				byte[] prisonerBuffer = new byte[4];
				try {
					while (MainActivity.connected) {
//						System.out.println("reading");
						int input = MainActivity.inStream.read();
//						System.out.println("read: " + input);
						if (input < 0 || input == '!') break;
						switch (input) {
						case CURRENT_PIECE:
//							System.out.println("CURRENT_PIECE");
							int type = MainActivity.inStream.read();
							if (type == NULL_TYPE){
								gameCanvasView.grid.currentPiece = null;
								continue;
							}
							input = MainActivity.inStream.read(pieceBuffer);
							if (input < pieceBuffer.length) mainActivity.showToast("CURRENT_PIECE: " + input);
							else {
//								System.out.println(type + ": " + Arrays.toString(pieceBuffer));
								if (gameCanvasView.grid.currentPiece == null) gameCanvasView.grid.currentPiece = new TetrisPiece(gameCanvasView.grid, type);
								gameCanvasView.grid.currentPiece.receive(pieceBuffer);
							}
							break;
						case PRISONER:
							input = MainActivity.inStream.read(prisonerBuffer);
							if (input < prisonerBuffer.length) mainActivity.showToast("PRISONER: " + input);
							else {
								gameCanvasView.prisoner.receive(prisonerBuffer);
							}
							break;
						case GRID:
							input = MainActivity.inStream.read(gridBuffer);
							if (input < gridBuffer.length) mainActivity.showToast("GRID: " + input);
							else {
								gameCanvasView.grid.receiveBlock(gridBuffer[0], gridBuffer[1]);
							}
							break;
						case WALL:
							input = MainActivity.inStream.read(gridBuffer);
							if (input < gridBuffer.length) mainActivity.showToast("WALL: " + input);
							else {
								gameCanvasView.grid.receiveWall(gridBuffer[0], gridBuffer[1]);
							}
							break;
						case PLATFORM:
							gameCanvasView.grid.receivePlatform((byte) MainActivity.inStream.read());
							break;
						case PRISONER_DEAD:
							gameCanvasView.prisoner.kill();
							break;
						case SKY_OPEN:
							gameCanvasView.grid.complete = true;
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
	private void sendToHost(char aByte){
		try {
			MainActivity.outStream.write(aByte);
			MainActivity.outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
