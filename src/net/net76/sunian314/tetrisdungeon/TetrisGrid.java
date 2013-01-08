package net.net76.sunian314.tetrisdungeon;

import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;

public class TetrisGrid {
	int width, height;
	TetrisBlock[][] blockGrid = new TetrisBlock[20][10];
	private boolean[][] vertWalls = new boolean[20][10];
	private boolean[][] horizWalls = new boolean[20][10];
	private boolean[][] platforms = new boolean[20][10];
	TetrisPiece currentPiece;
	boolean complete = false;
	ArrayList<TetrisBlock> fallingBlocks = new ArrayList<TetrisBlock>();
	Bitmap bmapWalls;
	boolean currentBitmap, dirty = true; 
	private Canvas canvasWalls;
	private boolean initialized = false;
	//0 I red
	//1 O orange
	//2 T magenta
	//3 L cyan
	//4 J cyan
	//5 S blue
	//6 z blue
	public TetrisGrid(int screenW, int screenH){
		width = screenW;
		height = screenH;
		for (int i = 0; i < 10; i++) {
			horizWalls[0][i] = true;
		}
//		bmap = Bitmap.createBitmap(width, height, Config.ARGB_4444);
//		canvas = new Canvas(bmap);
//		bmap2 = Bitmap.createBitmap(width, height, Config.ARGB_4444);
//		canvas2 = new Canvas(bmap2);
		bmapWalls = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		canvasWalls = new Canvas(bmapWalls);

	}
	void createPiece(int type){
		
	}
	protected void onDraw(Canvas canvas) {
		if (!initialized) return;
		
//		currentBitmap = !currentBitmap;
//		dirty = false;
	}
	Bitmap getBitmap(){
		
		return null;
//		return true ? bmap2: bmap1;
	}
	boolean platform(int row, int col){
		return platforms[row][col];
	}
	boolean wallRight(int row, int col){
		return vertWalls[row][col];
	}
	boolean wallBelow(int row, int col){
		return horizWalls[row][col];
	}
	boolean wallLeft(int row, int col){
		return col > 0 ? vertWalls[row][col - 1] : true;
	}
	boolean wallAbove(int row, int col){
		return row < 19 ? horizWalls[row + 1][col] : true;
	}
	void addWall(int row, int col, int side){
		if (side < 1) return;
		switch (side) {
		case 1:
			if (col < 1) return;
			vertWalls[row][col - 1] = true;
			break;
		case 2:
			if (row > 18) return;
			horizWalls[row + 1][col] = true;
			break;
		case 3:
			vertWalls[row][col] = true;
			break;
		case 4:
			horizWalls[row][col] = true;
			break;
		default:
			break;
		}
	}
	void addWall(TetrisBlock block){
		addWall(block.row, block.col, block.wallside);
		block.drawWall(canvasWalls);
	}
	void addPlatform(TetrisBlock block){
		if (platforms[block.row][block.col]) return;
		platforms[block.row][block.col] = true;
		if (!wallBelow(block.row, block.col));
			block.drawPlatform(canvasWalls);
	}
	boolean spotIsEmtpy(int row, int col){//returns true if spot is empty
		if (col < 0 || col > 9 || row < 0 || row > 19) return false;
		return (blockGrid[row][col] == null || (!blockGrid[row][col].stationary));
	}
	boolean canGrabBlock(int row, int col){//returns true spot contains grabbable block
		if (col < 0 || col > 9 || row < 0 || row > 19) return false;
		return (blockGrid[row][col] != null && (!blockGrid[row][col].partOfCurrent));
	}
	public int gameUpdate() {
		if (currentPiece == null){
			currentPiece = new TetrisPiece(this, (int)(Math.random()*7));
			if (!currentPiece.addToGrid()) {
				currentPiece.settle();
				currentPiece = null;
				TetrisPiece.transmitNULL();
				complete = true;
				try {
					MainActivity.outStream.write(TetrisControls.SKY_OPEN);
					MainActivity.outStream.flush();
				} catch (IOException e) {e.printStackTrace();}
				MainActivity.tetrisGridView.postInvalidate();
				return -1;
			} else {
				currentPiece.transmit();
			}
		} else {
			if (!currentPiece.fall()) {
				currentPiece.settle();
				currentPiece = null;
				TetrisPiece.transmitNULL();
				eliminateRows();
				MainActivity.tetrisGridView.postInvalidate();
			} else {
				currentPiece.transmit();
			}
		}
		for (TetrisBlock block : fallingBlocks){
			if (spotIsEmtpy(block.row - 1, block.col)) {
				block.fall(1);
				MainActivity.tetrisGridView.postInvalidate();
			} else {
				block.stationary = true;
				fallingBlocks.remove(block);
			}
		}
		if (currentPiece == null) {
			return 1;
		} else {
			currentPiece.draw();
		}
		return 0;
	}
	void eliminateRows(){
		int gap = 0;
		for (int i = 0; i < 20; i++) {
			boolean isFull = true;
			for (int j = 0; j < 10; j++) {
				if (spotIsEmtpy(i, j)) {
					isFull = false;
					break;
				}
			}
			if (isFull){
				gap++;
				for (int j = 0; j < 10; j++) {
					blockGrid[i][j] = null;
					transmitBlock(i, j);
				}
				Prisoner prisoner = MainActivity.gameCanvasView.prisoner;
				Rect gremlinBounds = prisoner.myBounds;
				if (gremlinBounds.top > GameCanvasView.blockSize * (19 - i) && gremlinBounds.bottom < GameCanvasView.blockSize * (20 - i)){
					prisoner.kill();
					try {
						MainActivity.outStream.write(TetrisControls.PRISONER_DEAD);
						MainActivity.outStream.flush();
					} catch (IOException e) {e.printStackTrace();}
				}
			} else if (gap > 0){
				for (TetrisBlock block : blockGrid[i]) {
					if (block != null && block.stationary) block.fall(gap);
				}
			}
		}
	}
	void transmitBlock(int row, int col){
		if (row < 0 || row > 19 || col < 0 || col > 9) return;
		byte[] bytes = new byte[3];
		bytes[0] = TetrisControls.GRID;
		bytes[1] = (byte) (row * 10 + col);
		if (canGrabBlock(row, col)){
			bytes[2] = (byte) blockGrid[row][col].type;
		} else {
			bytes[2] = TetrisControls.NULL_TYPE;
		}
		try {
			MainActivity.outStream.write(bytes);
			MainActivity.outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void receiveBlock(byte location, byte type){
		int loc = location < 0 ? location + 256 : location;
		int row = loc / 10;
		int col = loc % 10;
		if (type == TetrisControls.NULL_TYPE){
			blockGrid[row][col] = null;
		} else {
			if (blockGrid[row][col] == null) {
				blockGrid[row][col] = new TetrisBlock(this, type, row, col);
				blockGrid[row][col].partOfCurrent = false;
			}
			else blockGrid[row][col].type = type;
		}
		MainActivity.tetrisGridView.postInvalidate();
	}
	void transmitWall(TetrisBlock block){
		byte[] bytes = new byte[3];
		bytes[0] = TetrisControls.WALL;
		bytes[1] = (byte) (block.row * 10 + block.col);
		bytes[2] = (byte) block.wallside;
		try {
			MainActivity.outStream.write(bytes);
			MainActivity.outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void receiveWall(byte location, byte side) {
		int loc = location < 0 ? location + 256 : location;
		int row = loc / 10;
		int col = loc % 10;
		TetrisBlock block = new TetrisBlock(null, 2, row, col);
		block.wallside = side;
		addWall(block);
	}
	void transmitPlatform(TetrisBlock block){
		byte[] bytes = new byte[2];
		bytes[0] = TetrisControls.PLATFORM;
		bytes[1] = (byte) (block.row * 10 + block.col);
		try {
			MainActivity.outStream.write(bytes);
			MainActivity.outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void receivePlatform(byte location) {
		int loc = location < 0 ? location + 256 : location;
		int row = loc / 10;
		int col = loc % 10;
		TetrisBlock block = new TetrisBlock(null, 2, row, col);
		addPlatform(block);
	}
}
