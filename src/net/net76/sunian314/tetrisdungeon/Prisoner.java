package net.net76.sunian314.tetrisdungeon;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import net.net76.sunian314.tetrisdungeon.R;

public class Prisoner {
	double xPos, yPos, xVel=0, yVel=0, grav = 1, runVel=1, walkVel=1, jumpVel=2;
	double lastX, lastY, newX, newY;
	Paint myPaint;
	int width, height;
	int myWidth = 10, myHeight = 20;
	Rect myBounds;
	private boolean alive = true, canJump = true, fallThrough = false;
	TetrisGrid grid;
	TetrisBlock myBlock;
	boolean inRightHand = false;
	
	public Prisoner(int screenW, int screenH, TetrisGrid tetrisGrid){
//		System.out.println(screenW + "x" + screenH);
		width = screenW;
		height = screenH;
		this.grid = tetrisGrid;
		myHeight = screenW * 3/40;
		myWidth = myHeight * 2/3;
		runVel = myWidth * 4;
		walkVel = runVel / 2;
		jumpVel = myHeight * 14;
		grav = jumpVel * jumpVel * .0067;
		xPos = (screenW - myWidth) / 2.0;
		yPos = 1;
		myBounds = new Rect();
		updateBounds();
		myPaint = new Paint();
		myPaint.setColor(Color.rgb(178,207,88));
	}
	private void updateBounds(){
		myBounds.set((int)(xPos), (int)(height - yPos - myHeight), (int)(xPos + myWidth), (int)(height - yPos));
		if (myBlock != null) {
			inRightHand = (xPos + myWidth/2) < (myBlock.myBounds.left + myBlock.myBounds.right) / 2;
		}
//		System.out.println();
	}
	void gameUpdate(long milisecs){
//		GameCanvasView.printElapsed();
		if (!alive) return;
		lastX = xPos; lastY = yPos;
		yVel -= grav * milisecs / 1000.0;
		newX = xPos + xVel * milisecs / 1000.0;
		newY = yPos + yVel * milisecs / 1000.0;
		//do collision detection
		if (newY < 1){
			newY = 1;
			canJump = true;
//			intersectH = false;
//			row--;
			yVel = 0;
		} else if (newY >= height - myHeight){
			newY = height - myHeight;
//			intersectH = false;
			yVel = 0;
		}
		if (newX < 1){
			newX = 1;
//			intersectV = false;
			xVel = 0;
		} else if (newX >= width - myWidth){
			newX = width - myWidth;
//			intersectV = false;
			xVel = 0;
		}
		boolean intersectH = (newY - 1) % GameCanvasView.blockSize >= (GameCanvasView.blockSize - myHeight - 1);
		boolean intersectV = (newX - 1) % GameCanvasView.blockSize >= (GameCanvasView.blockSize - myWidth - 1);
		int row = (int) ((newY - 1) / GameCanvasView.blockSize);
		int col = (int) ((newX - 1) / GameCanvasView.blockSize);
		
		if (intersectH && intersectV){
			checkCollisionsRight(row, col);
			checkCollisionsBelow(row + 1, col);
			checkCollisionsRight(row + 1, col);
			checkCollisionsBelow(row + 1, col + 1);
		} 
		else if (intersectH) checkCollisionsBelow(row + 1, col);
		else if (intersectV) checkCollisionsRight(row, col);
//		System.out.println(lastX + " => " +newX + "   " + intersectV);
		xPos = newX; yPos = newY;
		updateBounds();
		if (myBlock != null){//have grabbed a block
			double min = GameCanvasView.blockSize / 2.0 + 1;
			double handX = inRightHand ? myBounds.right : myBounds.left;
			double handY = (myBounds.top + myBounds.bottom )/2.0;
			int blockX = (myBlock.myBounds.left + myBlock.myBounds.right)/2;
			int blockY = (myBlock.myBounds.top + myBlock.myBounds.bottom)/2;
			if (handX - blockX > min){
				if (grid.blockGrid[myBlock.row][myBlock.col + 1] == null) myBlock.drag(0, 1);
			} else if (blockX - handX > min){
				if (grid.blockGrid[myBlock.row][myBlock.col - 1] == null) myBlock.drag(0, -1);
			}
			if (handY - blockY > min){
				if (grid.blockGrid[myBlock.row - 1][myBlock.col] == null) myBlock.drag(-1, 0);
			} else if (blockY - handY > min){
				if (grid.blockGrid[myBlock.row + 1][myBlock.col] == null) myBlock.drag(1, 0);
			}
			MainActivity.tetrisGridView.postInvalidate();
		}
//		System.out.println(milisecs);
	}
	/*
	 * GameCanvasView.blockSize * col, 
	 * (19 - row) * GameCanvasView.blockSize, 
	 * GameCanvasView.blockSize * (col + 1), 
	 * (20 - row) * GameCanvasView.blockSize
	 */
	void checkCollisionsBelow(int row, int col){
		if (col > 9 || row > 19) return;
		int bottom = GameCanvasView.blockSize * row;
		int left = GameCanvasView.blockSize * col;
		int right = left + GameCanvasView.blockSize;
		boolean compAbove = lastY > bottom;
		boolean compBelow = lastY + myHeight < bottom - 1;
		boolean compLeft = lastX + myWidth < left;
		boolean compRight = lastX > right;
		if (grid.wallBelow(row, col)){
			if (!compLeft && !compRight){
				if (compAbove){
					canJump = true;
					newY = Math.max(newY, bottom + 1);
				} else if (compBelow){
					canJump = false;
					newY = Math.min(newY, bottom - 1 - myHeight);
				}
				yVel = 0;
			} else if (!compAbove && !compBelow){
				if (compRight){
					newX = Math.max(newX, right + 1);
				} else if (compLeft){
					newX = Math.min(newX, left - 2 - myWidth);
				}
			} else {
				double dy = newY - lastY, dx = newX - lastX;
				if (dy != 0 && dx != 0) {
					double interY = compAbove ? bottom - newY + 1 : newY + myHeight - bottom + 1; 
					double interX = compRight ? right - newX + 1 : newX + myWidth - left + 1;
					if (interX > 0 && interY > 0){
						if (interY/dy > interX/dx){
							newX = compRight ? Math.max(newX, right + 1) : Math.min(newX, left - 2 - myWidth);
						} else {
							canJump = compAbove;
							newY = compAbove ? Math.max(newY, bottom + 1) : Math.min(newY, bottom - 1 - myHeight);
							yVel = 0;
						}
					}
				}
			}
		} else if (!fallThrough && grid.platform(row, col) && compAbove && !compLeft && !compRight){
			canJump = true;
			newY = Math.max(newY, bottom + 1);
			yVel = 0;
		}
	}
	void checkCollisionsRight(int row, int col){
		if (col > 9 || row > 19) return;
		if (grid.wallRight(row, col)){
			int bottom = GameCanvasView.blockSize * row;
			int top = GameCanvasView.blockSize + bottom;
			int right = GameCanvasView.blockSize * (col + 1);
			boolean compAbove = lastY > top;
			boolean compBelow = lastY + myHeight < bottom;
			boolean compLeft = lastX + myWidth < right;
			boolean compRight = lastX > right;
			if (!compAbove && !compBelow){
				if (compRight){
					newX = Math.max(newX, right + 1);
				} else if (compLeft){
					newX = Math.min(newX, right - 1 - myWidth);
				}
			} else if (!compLeft && !compRight){
				if (compAbove){
					canJump = true;
					newY = Math.max(newY, top + 1);
				} else if (compBelow){
					canJump = false;
					newY = Math.min(newY, bottom - 1 - myHeight);
				}
				yVel = 0;
			} else {
				double dy = newY - lastY, dx = newX - lastX;
				if (dy != 0 && dx != 0) {
					double interY = compAbove ? top - newY + 1 : newY + myHeight - bottom + 1; 
					double interX = compRight ? right - newX + 1 : newX + myWidth - right + 1;
					if (interX > 0 && interY > 0){
						if (interY/dy > interX/dx){
							newX = compRight ? Math.max(newX, right + 1) : Math.min(newX, right - 1 - myWidth);
						} else {
							canJump = compAbove;
							newY = compAbove ? Math.max(newY, top + 1) : Math.min(newY, bottom - 1 - myHeight);
							yVel = 0;
						}
					}
				}
			}
		}
	}
	void setPos(int x, int y){
		xPos = x;
		yPos = y;
		updateBounds();
	}
	void moveRight(float weight){
//		xVel = weight > .42 ? runVel : walkVel;
		xVel = runVel * weight / .5;
	}
	void moveLeft(float weight){
//		xVel = weight > .42 ? -runVel : -walkVel;
		xVel = -runVel * weight / .5;
	}
	void stop(){
		xVel = 0;
		fallThrough = false;
	}
	void jump(){
		if (canJump) {
			yVel = jumpVel;
			canJump = false;
		}
	}
	void drop(){
		fallThrough = true;
	}
	void toggleGrip(){
		if (myBlock == null){
			int row = (int)((yPos + myHeight/2) / GameCanvasView.blockSize);
			int col = (int)((xPos + myWidth/2) / GameCanvasView.blockSize);
			if (grid.canGrabBlock(row, col)) {
				myBlock = grid.blockGrid[row][col];
				myBlock.stationary = true;
				grid.fallingBlocks.remove(myBlock);
			}
		} else {
			myBlock.stationary = false;
			grid.fallingBlocks.add(myBlock);
			myBlock = null;
		}
	}
	void kill(){
		alive = false;
	}
	boolean isAlive(){
		return alive;
	}
}
