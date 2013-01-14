package net.net76.sunian314.tetrisdungeon;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;


public class TetrisBlock {
	//0 I red
	//1 O yellow
	//2 T magenta
	//3 L orange
	//4 J blue
	//5 S cyan
	//6 z purple
	int type;
	int wallside = 0;// Left Top Right Bottom
	int row, col;
	boolean stationary, partOfCurrent, grabbed;
	Rect myBounds;
	TetrisGrid grid;
	Path platform;
	
	public TetrisBlock(TetrisGrid tetrisGrid, int type, int r, int c){
		this.type = type;
		grid = tetrisGrid;
		row = r;
		col = c;
		stationary = false;
		grabbed = false;
		partOfCurrent = true;
		if (Math.random() > .25) {
			wallside = (int)(Math.random() * 4) + 1;
		}
		myBounds = new Rect();
		updateBounds();
	}
	RectF getBounds(){
		return new RectF(myBounds);
	}
	private void updateBounds(){
		myBounds.set(GameCanvasView.blockSize * col, (19 - row) * GameCanvasView.blockSize, GameCanvasView.blockSize * (col + 1), (20 - row) * GameCanvasView.blockSize);
		platform = new Path();
		platform.moveTo(myBounds.left, myBounds.bottom - 1);
		platform.lineTo(myBounds.right, myBounds.bottom - 1);
	}
	void drawWall(Canvas canvas){
		if (wallside > 0){
			switch (wallside) {
			case 1:
				canvas.drawRect(myBounds.left - 1, myBounds.top, myBounds.left + 1, myBounds.bottom, GameCanvasView.wallPaint);
				break;
			case 2:
				canvas.drawRect(myBounds.left, myBounds.top - 1, myBounds.right, myBounds.top + 1, GameCanvasView.wallPaint);
				break;
			case 3:
				canvas.drawRect(myBounds.right - 1, myBounds.top, myBounds.right + 1, myBounds.bottom, GameCanvasView.wallPaint);
				break;
			case 4:
				canvas.drawRect(myBounds.left, myBounds.bottom - 1, myBounds.right, myBounds.bottom + 1, GameCanvasView.wallPaint);
				break;
			default:
				break;
			}
		}
	}
	void drawPlatform(Canvas canvas){
		canvas.drawPath(platform, GameCanvasView.platformPaint);
	}
	void rotate(){
		updateBounds();
		if (wallside == 0) return;
		wallside = (wallside % 4) + 1;
	}
	void rotate(boolean clockwise){
		if (wallside == 0) return;
		if (!clockwise)  wallside += 2;
		wallside = (wallside % 4) + 1;
	}
	void fall(int rows){
		drag(-rows, 0);
	}
	void move(int rows, int cols){
		row += rows;
		col += cols;
		updateBounds();
	}
	void position(int r, int c){//called by Tetris Player
		row = r;
		col = c;
		updateBounds();
	}
	void drag(int rows, int cols){
		if (grid.blockGrid[row][col] == this) {
			grid.blockGrid[row][col] = null;
			if (!partOfCurrent) grid.transmitBlock(row, col);
		}
		move(rows, cols);
		grid.blockGrid[row][col] = this;
		if (!partOfCurrent) grid.transmitBlock(row, col);
	}
}
