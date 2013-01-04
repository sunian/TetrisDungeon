package net.net76.sunian314.tetrisdungeon;

import java.util.ArrayList;
import java.util.HashMap;
import net.net76.sunian314.tetrisdungeon.R;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.Config;

public class TetrisPiece {
	TetrisBlock[] blocks = new TetrisBlock[4];
	int orientation = 1;
	int type;
	TetrisGrid grid;

	Bitmap bmap;
	boolean currentBitmap; 
	private Canvas canvas;
//	Semaphore mutex = new Semaphore(1);
	//format: {block#, row offset, col offset}
	//key: type + orientation
	static HashMap<String, ArrayList<Integer>> checks = new HashMap<String, ArrayList<Integer>>();
	static HashMap<String, Integer[]> fallOrders = new HashMap<String, Integer[]>();
	static int[][][][] rotationOffsets;
	public TetrisPiece(TetrisGrid tetrisGrid, int type){
		this.type = type;
		grid = tetrisGrid;
		switch (type) {
		case 0://I
			blocks[0] = new TetrisBlock(grid, type, 19, 4);
			blocks[1] = new TetrisBlock(grid, type, 18, 4);
			blocks[2] = new TetrisBlock(grid, type, 17, 4);
			blocks[3] = new TetrisBlock(grid, type, 16, 4);
			break;
		case 1://O
			blocks[0] = new TetrisBlock(grid, type, 19, 4);
			blocks[1] = new TetrisBlock(grid, type, 19, 5);
			blocks[2] = new TetrisBlock(grid, type, 18, 4);
			blocks[3] = new TetrisBlock(grid, type, 18, 5);
			break;
		case 2://T
			blocks[1] = new TetrisBlock(grid, type, 19, 4);
			blocks[0] = new TetrisBlock(grid, type, 18, 4);
			blocks[3] = new TetrisBlock(grid, type, 17, 4);
			blocks[2] = new TetrisBlock(grid, type, 18, 5);
			break;
		case 3://L
			blocks[1] = new TetrisBlock(grid, type, 19, 4);
			blocks[0] = new TetrisBlock(grid, type, 18, 4);
			blocks[2] = new TetrisBlock(grid, type, 17, 4);
			blocks[3] = new TetrisBlock(grid, type, 17, 5);
			break;
		case 4://J
			blocks[1] = new TetrisBlock(grid, type, 19, 5);
			blocks[0] = new TetrisBlock(grid, type, 18, 5);
			blocks[2] = new TetrisBlock(grid, type, 17, 5);
			blocks[3] = new TetrisBlock(grid, type, 17, 4);
			break;
		case 5://S
			blocks[2] = new TetrisBlock(grid, type, 19, 5);
			blocks[3] = new TetrisBlock(grid, type, 19, 6);
			blocks[0] = new TetrisBlock(grid, type, 18, 4);
			blocks[1] = new TetrisBlock(grid, type, 18, 5);
			break;
		case 6://Z
			blocks[2] = new TetrisBlock(grid, type, 18, 5);
			blocks[3] = new TetrisBlock(grid, type, 18, 6);
			blocks[0] = new TetrisBlock(grid, type, 19, 4);
			blocks[1] = new TetrisBlock(grid, type, 19, 5);
			break;
		default:
			break;
		}
		bmap = Bitmap.createBitmap(grid.width, grid.height, Config.ARGB_4444);
		canvas = new Canvas(bmap);
	}
	boolean addToGrid(){
		boolean out = true;
		for (TetrisBlock block : blocks) {
			if (grid.blockGrid[block.row][block.col] != null) out = false;
			grid.blockGrid[block.row][block.col] = block;
		}
		return out;
	}
	void removeFromGrid(){
		for (TetrisBlock block : blocks) {
			grid.blockGrid[block.row][block.col] = null;
		}
	}
	boolean rotate(){
		if (grid == null) return false;
		for (int i = 0; i < 4; i++) {
			if (!grid.spotIsEmtpy(blocks[i].row - rotationOffsets[type][orientation - 1][i][0], 
					blocks[i].col + rotationOffsets[type][orientation - 1][i][1])) {
				return false;
			}
		}
		removeFromGrid();
//		System.out.println(blocks[2].row + ", " + blocks[2].col);
		for (int i = 0; i < 4; i++) {
			blocks[i].row -= rotationOffsets[type][orientation - 1][i][0];
			blocks[i].col += rotationOffsets[type][orientation - 1][i][1];
			blocks[i].rotate();
		}
				
		addToGrid();
		orientation = (orientation % 4) + 1;
		return true;
	}
	boolean fall(){
		for (TetrisBlock block : blocks) {
			if (!grid.spotIsEmtpy(block.row - 1, block.col)) {
				return false;
			}
		}
		for (TetrisBlock block : blocks){
			block.fall(1);
		}
		return true;
	}
	boolean move(boolean right){
		int offset = right ? 1 : -1;
		for (TetrisBlock block : blocks){
			if (!grid.spotIsEmtpy(block.row, block.col + offset)){
				return false;
			}
		}
		removeFromGrid();
		for (TetrisBlock block : blocks){
			block.move(0, offset);
		}
		addToGrid();
		return true;
	}
	void draw(){
		bmap.eraseColor(Color.TRANSPARENT);
		for (TetrisBlock block : blocks){
			canvas.drawRoundRect(block.getBounds(), GameCanvasView.blockSize/6, GameCanvasView.blockSize/6, TetrisGridView.blockPaints[block.type]);
//			canvas.drawRect(block.myBounds, TetrisGrid.blockPaints[block.type]);
			block.drawPlatform(canvas);
		}
		for (TetrisBlock block : blocks){
			block.drawWall(canvas);
		}
	}
	void settle(){
		for (TetrisBlock block : blocks){
			block.stationary = true;
			block.partOfCurrent = false;
			grid.addPlatform(block);
			grid.addWall(block);
		}
	}
	static void createChecklist(){
		checks.clear();
		ArrayList<Integer> checksI1 = new ArrayList<Integer>();
		checksI1.add(3);
		ArrayList<Integer> checksI2 = new ArrayList<Integer>();
		checksI2.add(0);
		checksI2.add(1);
		checksI2.add(2);
		checksI2.add(3);
		ArrayList<Integer> checksI3 = new ArrayList<Integer>();
		checksI3.add(0);
		ArrayList<Integer> checksI4 = checksI2;
		checks.put("01", checksI1);
		checks.put("02", checksI2);
		checks.put("03", checksI3);
		checks.put("04", checksI4);
		
		ArrayList<Integer> checksO1 = new ArrayList<Integer>();
		checksO1.add(2);
		checksO1.add(3);
		ArrayList<Integer> checksO2 = new ArrayList<Integer>();
		checksO2.add(1);
		checksO2.add(3);
		ArrayList<Integer> checksO3 = new ArrayList<Integer>();
		checksO3.add(1);
		checksO3.add(0);
		ArrayList<Integer> checksO4 = new ArrayList<Integer>();
		checksO4.add(2);
		checksO4.add(0);
		
		checks.put("11", checksO1);
		checks.put("12", checksO2);
		checks.put("13", checksO3);
		checks.put("14", checksO4);
		
		ArrayList<Integer> checksT1 = new ArrayList<Integer>();
		checksT1.add(3);
		checksT1.add(2);
		ArrayList<Integer> checksT2 = new ArrayList<Integer>();
		checksT2.add(3);
		checksT2.add(2);
		checksT2.add(1);
		ArrayList<Integer> checksT3 = new ArrayList<Integer>();
		checksT3.add(1);
		checksT3.add(2);
		ArrayList<Integer> checksT4 = new ArrayList<Integer>();
		checksT4.add(3);
		checksT4.add(0);
		checksT4.add(1);
		
		checks.put("21", checksT1);
		checks.put("22", checksT2);
		checks.put("23", checksT3);
		checks.put("24", checksT4);
		
		ArrayList<Integer> checksL1 = new ArrayList<Integer>();
		checksL1.add(3);
		checksL1.add(2);
		ArrayList<Integer> checksL2 = new ArrayList<Integer>();
		checksL2.add(3);
		checksL2.add(0);
		checksL2.add(1);
		ArrayList<Integer> checksL3 = new ArrayList<Integer>();
		checksL3.add(3);
		checksL3.add(1);
		ArrayList<Integer> checksL4 = new ArrayList<Integer>();
		checksL4.add(1);
		checksL4.add(0);
		checksL4.add(2);

		checks.put("31", checksL1);
		checks.put("32", checksL2);
		checks.put("33", checksL3);
		checks.put("34", checksL4);
		
		ArrayList<Integer> checksJ1 = new ArrayList<Integer>();
		checksJ1.add(3);
		checksJ1.add(2);
		ArrayList<Integer> checksJ2 = new ArrayList<Integer>();
		checksJ2.add(2);
		checksJ2.add(0);
		checksJ2.add(1);
		ArrayList<Integer> checksJ3 = new ArrayList<Integer>();
		checksJ3.add(3);
		checksJ3.add(1);
		ArrayList<Integer> checksJ4 = new ArrayList<Integer>();
		checksJ4.add(1);
		checksJ4.add(0);
		checksJ4.add(3);

		checks.put("41", checksJ1);
		checks.put("42", checksJ2);
		checks.put("43", checksJ3);
		checks.put("44", checksJ4);
		
		ArrayList<Integer> checksS1 = new ArrayList<Integer>();
		checksS1.add(0);
		checksS1.add(1);
		checksS1.add(3);
		ArrayList<Integer> checksS2 = new ArrayList<Integer>();
		checksS2.add(3);
		checksS2.add(1);
		ArrayList<Integer> checksS3 = new ArrayList<Integer>();
		checksS3.add(3);
		checksS3.add(2);
		checksS3.add(0);
		ArrayList<Integer> checksS4 = new ArrayList<Integer>();
		checksS4.add(2);
		checksS4.add(0);
		
		checks.put("51", checksS1);
		checks.put("52", checksS2);
		checks.put("53", checksS3);
		checks.put("54", checksS4);

		ArrayList<Integer> checksZ1 = new ArrayList<Integer>();
		checksZ1.add(0);
		checksZ1.add(2);
		checksZ1.add(3);
		ArrayList<Integer> checksZ2 = new ArrayList<Integer>();
		checksZ2.add(3);
		checksZ2.add(1);
		ArrayList<Integer> checksZ3 = new ArrayList<Integer>();
		checksZ3.add(3);
		checksZ3.add(1);
		checksZ3.add(0);
		ArrayList<Integer> checksZ4 = new ArrayList<Integer>();
		checksZ4.add(2);
		checksZ4.add(0);
		
		checks.put("61", checksZ1);
		checks.put("62", checksZ2);
		checks.put("63", checksZ3);
		checks.put("64", checksZ4);
	}
	static void createFallOrders(){
		fallOrders.clear();
		fallOrders.put("01", new Integer[]{3,2,1,0});
		fallOrders.put("02", new Integer[]{3,2,1,0});
		fallOrders.put("03", new Integer[]{0,1,2,3});
		fallOrders.put("04", new Integer[]{3,2,1,0});

		fallOrders.put("11", new Integer[]{3,2,1,0});
		fallOrders.put("12", new Integer[]{3,1,2,0});
		fallOrders.put("13", new Integer[]{0,1,2,3});
		fallOrders.put("14", new Integer[]{0,2,1,3});

		fallOrders.put("21", new Integer[]{3,2,0,1});
		fallOrders.put("22", new Integer[]{3,2,1,0});
		fallOrders.put("23", new Integer[]{1,2,0,3});
		fallOrders.put("24", new Integer[]{3,0,1,2});

		fallOrders.put("31", new Integer[]{3,2,0,1});
		fallOrders.put("32", new Integer[]{3,2,1,0});
		fallOrders.put("33", new Integer[]{1,0,2,3});
		fallOrders.put("34", new Integer[]{2,1,0,3});

		fallOrders.put("41", new Integer[]{3,2,0,1});
		fallOrders.put("42", new Integer[]{2,1,0,3});
		fallOrders.put("43", new Integer[]{1,0,2,3});
		fallOrders.put("44", new Integer[]{3,1,0,2});

		fallOrders.put("51", new Integer[]{0,1,2,3});
		fallOrders.put("52", new Integer[]{3,2,1,0});
		fallOrders.put("53", new Integer[]{3,2,1,0});
		fallOrders.put("54", new Integer[]{0,1,2,3});

		fallOrders.put("61", new Integer[]{3,2,1,0});
		fallOrders.put("62", new Integer[]{3,2,1,0});
		fallOrders.put("63", new Integer[]{0,1,2,3});
		fallOrders.put("64", new Integer[]{0,1,2,3});
	}
	static void createRotationOffsets(){
		rotationOffsets = new int[][][][]{
				{//I
					{{1,2},{0,1},{-1, 0},{-2, -1}},
					{{2,-2},{1,-1},{0, 0},{-1, 1}},
					{{-2,-1},{-1,0},{0, 1},{1, 2}},
					{{-1,1},{0,0},{1, -1},{2, -2}}
				},
				{//O
					{{0,1},{1,0},{-1, 0},{0, -1}},
					{{1, 0},{0,-1},{0, 1},{-1,0}},
					{{0, -1},{-1, 0},{1,0},{0,1}},
					{{-1,0},{0, 1},{0,-1},{1, 0}}
				},
				{//T
					{{0,0},{1,1},{1,-1},{-1,-1}},
					{{0,0},{1,-1},{-1,-1},{-1,1}},
					{{0,0},{-1,-1},{-1,1},{1,1}},
					{{0,0},{-1,1},{1,1},{1,-1}}
				},
				{//L
					{{0,0},{1,1},{-1,-1},{0,-2}},
					{{0,0},{1,-1},{-1,1},{-2,0}},
					{{0,0},{-1,-1},{1,1},{0,2}},
					{{0,0},{-1,1},{1,-1},{2,0}}
				},
				{//J
					{{0,0},{1,1},{-1,-1},{-2,0}},
					{{0,0},{1,-1},{-1,1},{0,2}},
					{{0,0},{-1,-1},{1,1},{2,0}},
					{{0,0},{-1,1},{1,-1},{0,-2}}
				},
				{//S
					{{-1,0},{0,-1},{1,0},{2,-1}},
					{{0,2},{-1,1},{0,0},{-1,-1}},
					{{2,-1},{1,0},{0,-1},{-1,0}},
					{{-1,-1},{0,0},{-1,1},{0,2}}
				},
				{//Z
					{{0,1},{1,0},{0,-1},{1,-2}},
					{{1,1},{0,0},{-1,1},{-2,0}},
					{{1,-2},{0,-1},{1,0},{0,1},},
					{{-2,0},{-1,1},{0,0},{1,1}}
				}
			};
	}
}
