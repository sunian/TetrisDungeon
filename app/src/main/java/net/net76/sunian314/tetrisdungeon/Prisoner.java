package net.net76.sunian314.tetrisdungeon;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class Prisoner {
    static final double DENOMINATOR = 65532.0;
    double xPos, yPos, xVel = 0, yVel = 0, grav = 1, runVel = 1, walkVel = 1, jumpVel = 2;
    double lastX, lastY, newX, newY;
    Paint myPaint;
    int width, height;
    int myWidth = 10, myHeight = 20;
    Rect myBounds;
    private boolean alive = true, canJump = true, fallThrough = false;
    TetrisGrid grid;
    TetrisBlock myBlock;
    boolean inRightHand = false, facingRight = true;
    Rect frames = new Rect();
    int spriteW = 62, spriteH = 88;
    int topOffset, bottomOffset, leftOffset, rightOffset;
    int frameLengthIdle = 200, frameLengthWalk = 100;
    int frameCounter = 0, frameLimit = frameLengthIdle;
    int currentFrame = 0;

    public Prisoner(int screenW, int screenH, TetrisGrid tetrisGrid) {
//		System.out.println(screenW + "x" + screenH);
        width = screenW;
        height = screenH;
        spriteW = LauncherActivity.androidSpriteSheet.getWidth() / 6;
        spriteH = LauncherActivity.androidSpriteSheet.getHeight() / 3;
        this.grid = tetrisGrid;
        myHeight = (int) (screenW * 3.0 / 40.0);
        myWidth = (int) (myHeight * 45.0 / 66.0);//2/3;
        runVel = spriteW * 2;
        walkVel = runVel / 2;
        jumpVel = spriteH * 6;
        grav = jumpVel * jumpVel * .003;
        xPos = (screenW - myWidth) / 2.0;
        yPos = 1;
        myBounds = new Rect();
        updateBounds();
        myPaint = new Paint();
        myPaint.setColor(Color.rgb(178, 207, 88));
        frames = new Rect(0, 0, spriteW, spriteH);
        topOffset = (int) (myHeight / -40.0) + 2;
        bottomOffset = (int) (myHeight * 12.0 / 80.0) + 2;
        leftOffset = (int) (myWidth * -5.0 / 56.0);
        rightOffset = (int) (myWidth * 6.0 / 56.0);
    }

    private void updateBounds() {
        myBounds.set((int) (xPos), (int) (height - yPos - myHeight), (int) (xPos + myWidth), (int) (height - yPos));
        if (myBlock != null) {
            inRightHand = (xPos + myWidth / 2) < (myBlock.myBounds.left + myBlock.myBounds.right) / 2;
        }
//		System.out.println();
    }

    int gameUpdate(long milisecs) {
//		GameCanvasView.printElapsed();
        if (!alive) return -1;
        lastX = xPos;
        lastY = yPos;
        yVel -= grav * milisecs / 1000.0;
        newX = xPos + xVel * milisecs / 1000.0;
        newY = yPos + yVel * milisecs / 1000.0;
        //do collision detection
        if (newY < 1) {
            newY = 1;
            canJump = true;
//			intersectH = false;
//			row--;
            yVel = 0;
        } else if (newY >= height - myHeight) {
            newY = height - myHeight;
//			intersectH = false;
            yVel = 0;
            return 1;
        }
        if (newX < 1) {
            newX = 1;
//			intersectV = false;
            xVel = 0;
        } else if (newX >= width - myWidth) {
            newX = width - myWidth;
//			intersectV = false;
            xVel = 0;
        }
        boolean intersectH = (newY - 1) % GameCanvasView.blockSize >= (GameCanvasView.blockSize - myHeight - 1);
        boolean intersectV = (newX - 1) % GameCanvasView.blockSize >= (GameCanvasView.blockSize - myWidth - 1);
        int row = (int) ((newY - 1) / GameCanvasView.blockSize);
        int col = (int) ((newX - 1) / GameCanvasView.blockSize);

        if (intersectH && intersectV) {
            checkCollisionsRight(row, col);
            checkCollisionsBelow(row + 1, col);
            checkCollisionsRight(row + 1, col);
            checkCollisionsBelow(row + 1, col + 1);
        } else if (intersectH) checkCollisionsBelow(row + 1, col);
        else if (intersectV) checkCollisionsRight(row, col);
//		System.out.println(lastX + " => " +newX + "   " + intersectV);
        xPos = newX;
        yPos = newY;
        if (newX != lastX || newY != lastY) {
            transmit();
        }
        updateBounds();
        updateFrame(milisecs);
        if (myBlock != null) {//have grabbed a block
            double min = GameCanvasView.blockSize / 2.0 + 1;
            double handX = inRightHand ? myBounds.right : myBounds.left;
            double handY = (myBounds.top + myBounds.bottom) / 2.0;
            int blockX = (myBlock.myBounds.left + myBlock.myBounds.right) / 2;
            int blockY = (myBlock.myBounds.top + myBlock.myBounds.bottom) / 2;
            int oldRow = myBlock.row, oldCol = myBlock.col;
            if (handX - blockX > min) {
                if (grid.blockGrid[myBlock.row][myBlock.col + 1] == null) myBlock.drag(0, 1);
            } else if (blockX - handX > min) {
                if (grid.blockGrid[myBlock.row][myBlock.col - 1] == null) myBlock.drag(0, -1);
            }
            if (handY - blockY > min) {
                if (grid.blockGrid[myBlock.row - 1][myBlock.col] == null) myBlock.drag(-1, 0);
            } else if (blockY - handY > min) {
                if (grid.blockGrid[myBlock.row + 1][myBlock.col] == null) myBlock.drag(1, 0);
            }
            if (myBlock.row != oldRow || myBlock.col != oldCol) {
                transmitBlock();
                MainActivity.tetrisGridView.postInvalidate();
            }
        }
//		System.out.println(milisecs);
        return 0;
    }

    void updateFrame(long milisecs) {
        frameCounter += milisecs;
        if (frameCounter >= frameLimit) {
            frameCounter -= frameLimit;
            currentFrame = (currentFrame + 1) % 4;
            int row, col = currentFrame < 3 ? currentFrame : 1;
            if (!facingRight) {
                col += 3;
            }
            if (yVel > 0) {
                row = 2;
            } else {
                if (xVel == 0) {
                    row = 0;
                } else {
                    row = 1;
                }
            }
            transmitFrame(row, col);
            setFrameBounds(row, col);
        }
    }

    void setFrameBounds(int row, int col) {
        frames.left = col * spriteW;
        frames.right = (col + 1) * spriteW;
        frames.top = row * spriteH;
        frames.bottom = (row + 1) * spriteH;
    }

    /*
     * GameCanvasView.blockSize * col,
     * (19 - row) * GameCanvasView.blockSize,
     * GameCanvasView.blockSize * (col + 1),
     * (20 - row) * GameCanvasView.blockSize
     */
    void checkCollisionsBelow(int row, int col) {
        if (col > 9 || row > 19) return;
        int bottom = GameCanvasView.blockSize * row;
        int left = GameCanvasView.blockSize * col;
        int right = left + GameCanvasView.blockSize;
        boolean compAbove = lastY > bottom;
        boolean compBelow = lastY + myHeight < bottom - 1;
        boolean compLeft = lastX + myWidth < left;
        boolean compRight = lastX > right;
        if (grid.wallBelow(row, col)) {
            if (!compLeft && !compRight && !compAbove && !compBelow) {
                canJump = newY + myHeight / 2 >= bottom;
                newY = canJump ? Math.max(newY, bottom + 1) : Math.min(newY, bottom - 1 - myHeight);
            } else if (!compLeft && !compRight) {
                if (compAbove) {
                    canJump = true;
                    newY = Math.max(newY, bottom + 1);
                } else if (compBelow) {
                    canJump = false;
                    newY = Math.min(newY, bottom - 1 - myHeight);
                }
                yVel = 0;
            } else if (!compAbove && !compBelow) {
                if (compRight) {
                    newX = Math.max(newX, right + 1);
                } else if (compLeft) {
                    newX = Math.min(newX, left - 2 - myWidth);
                }
            } else {
                double dy = newY - lastY, dx = newX - lastX;
                if (dy != 0 && dx != 0) {
                    double interY = compAbove ? bottom - newY + 1 : newY + myHeight - bottom + 1;
                    double interX = compRight ? right - newX + 1 : newX + myWidth - left + 1;
                    if (interX > 0 && interY > 0) {
                        if (interY / dy > interX / dx) {
                            newX = compRight ? Math.max(newX, right + 1) : Math.min(newX, left - 2 - myWidth);
                        } else {
                            canJump = compAbove;
                            newY = compAbove ? Math.max(newY, bottom + 1) : Math.min(newY, bottom - 1 - myHeight);
                            yVel = 0;
                        }
                    }
                }
            }
        } else if (!fallThrough && grid.platform(row, col) && compAbove && !compLeft && !compRight) {
            canJump = true;
            newY = Math.max(newY, bottom + 1);
            yVel = 0;
        }
    }

    void checkCollisionsRight(int row, int col) {
        if (col > 9 || row > 19) return;
        if (grid.wallRight(row, col)) {
            int bottom = GameCanvasView.blockSize * row;
            int top = GameCanvasView.blockSize + bottom;
            int right = GameCanvasView.blockSize * (col + 1);
            boolean compAbove = lastY > top;
            boolean compBelow = lastY + myHeight < bottom;
            boolean compLeft = lastX + myWidth < right;
            boolean compRight = lastX > right;
            if (!compLeft && !compRight && !compAbove && !compBelow) {
                newX = newX + myWidth / 2 >= right ? Math.max(newX, right + 1) : Math.min(newX, right - 1 - myWidth);
            } else if (!compAbove && !compBelow) {
                if (compRight) {
                    newX = Math.max(newX, right + 1);
                } else if (compLeft) {
                    newX = Math.min(newX, right - 1 - myWidth);
                }
            } else if (!compLeft && !compRight) {
                if (compAbove) {
                    canJump = true;
                    newY = Math.max(newY, top + 1);
                } else if (compBelow) {
                    canJump = false;
                    newY = Math.min(newY, bottom - 1 - myHeight);
                }
                yVel = 0;
            } else {
                double dy = newY - lastY, dx = newX - lastX;
                if (dy != 0 && dx != 0) {
                    double interY = compAbove ? top - newY + 1 : newY + myHeight - bottom + 1;
                    double interX = compRight ? right - newX + 1 : newX + myWidth - right + 1;
                    if (interX > 0 && interY > 0) {
                        if (interY / dy > interX / dx) {
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

    void setPos(int x, int y) {
        xPos = x;
        yPos = y;
        updateBounds();
    }

    void transmit() {
        byte[] bytes = new byte[5];
        bytes[0] = TetrisControls.PRISONER;
        int x = (int) (xPos / width * DENOMINATOR);
        int y = (int) (yPos / height * DENOMINATOR);
        bytes[2] = (byte) (x & 0x0ff);
        bytes[4] = (byte) (y & 0x0ff);
        x = x >> 8;
        y = y >> 8;
        bytes[1] = (byte) (x & 0x0ff);
        bytes[3] = (byte) (y & 0x0ff);
        MainActivity.writeToStream(bytes);
    }

    void receive(byte[] bytes) {
        if (bytes.length != 4) return;
        int x = (bytes[1] & 0x0ff) | ((bytes[0] & 0x0ff) << 8);
        int y = (bytes[3] & 0x0ff) | ((bytes[2] & 0x0ff) << 8);
        x = (int) (x / DENOMINATOR * width);
        y = (int) (y / DENOMINATOR * height);
        setPos(x, y);
    }

    void moveRight(float weight) {
//		xVel = weight > .42 ? runVel : walkVel;
        if (weight >= 0.4) {
            weight = 0.4f;
        }
        xVel = runVel * weight / .2;
        frameLimit = frameLengthWalk;
        facingRight = true;
        frameCounter = frameLimit;
    }

    void moveLeft(float weight) {
//		xVel = weight > .42 ? -runVel : -walkVel;
        if (weight >= 0.4) {
            weight = 0.4f;
        }
        xVel = -runVel * weight / .2;
        frameLimit = frameLengthWalk;
        facingRight = false;
        frameCounter = frameLimit;
    }

    void stop() {
        xVel = 0;
        frameLimit = frameLengthIdle;
        fallThrough = false;
    }

    void jump() {
        if (canJump) {
            yVel = jumpVel;
            canJump = false;
            frameLimit = frameLengthIdle;
            frameCounter = frameLimit;
            currentFrame = 0;
        }
    }

    void drop() {
        fallThrough = true;
    }

    void toggleGrip() {
        if (myBlock == null) {
            int row = (int) ((yPos + myHeight / 2) / GameCanvasView.blockSize);
            int col = (int) ((xPos + myWidth / 2) / GameCanvasView.blockSize);
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
        transmitBlock();
    }

    void kill() {
        alive = false;
    }

    boolean isAlive() {
        return alive;
    }

    void transmitBlock() {
        byte[] bytes = new byte[2];
        if (myBlock == null) {
            bytes[0] = TetrisControls.NULL_TYPE;
            bytes[1] = TetrisControls.PRISONER_BLOCK;
        } else {
            bytes[0] = TetrisControls.PRISONER_BLOCK;
            bytes[1] = (byte) (myBlock.row * 10 + myBlock.col);
        }
        MainActivity.writeToStream(bytes);
    }

    void receiveBlock(byte location) {
        int loc = location < 0 ? location + 256 : location;
        int row = loc / 10;
        int col = loc % 10;
        if (myBlock == null) myBlock = new TetrisBlock(null, 2, row, col);
        else myBlock.position(row, col);
        updateBounds();
    }

    void transmitFrame(int row, int col) {
        byte[] bytes = new byte[2];
        bytes[0] = TetrisControls.PRISONER_FRAME;
        bytes[1] = (byte) ((row & 0x03) | ((col & 0x07) << 2));
        MainActivity.writeToStream(bytes);
    }

    void receiveFrame(byte loc) {
        int row = loc & 0x03;
        int col = (loc >> 2) & 0x07;
        setFrameBounds(row, col);
    }
}
