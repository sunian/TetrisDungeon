package net.net76.sunian314.tetrisdungeon;

public class TetrisMethods {
	public static int removeCompletedRowsFromGrid(int[][] _grid, int rowWidth){
		int rowsRemoved=0;
		for (int row = _grid.length-1; row >=0; row--) {
			boolean rowIsComplete=true;
			for (int col = 0; col < rowWidth; col++) {
				if (_grid[row][col]==0) rowIsComplete=false;
			}
			if (rowIsComplete) {
				removeRowsFromGrid(_grid, row, rowWidth); 
				rowsRemoved+=1;
			}
		}
		return rowsRemoved;
	}
	public static int countCompletedRowsFromGrid(int[][] _grid, int rowWidth){
		int rowsRemoved=0;
		for (int row = _grid.length-1; row >=0; row--) {
			boolean rowIsComplete=true;
			for (int col = 0; col < rowWidth; col++) {
				if (_grid[row][col]==0) rowIsComplete=false;
			}
			if (rowIsComplete) rowsRemoved++;
		}
		return rowsRemoved;
	}
	public static void removeRowsFromGrid(int[][] _grid, int row, int rowWidth){
		for (int r = row; r < rowWidth-1; r++) {
			_grid[r]=_grid[r+1].clone();
		}
		
	}
	public static int transitionsInRow(int[][] grid, int row){
		int transitionCount = 0;
		for (int col = 0; col < grid[row].length-1; col++) {
			if (grid[row][col] != grid[row][col+1]) {
				transitionCount++;
			}
		}
		transitionCount+=(grid[row][0]==0?1:0)+(grid[row][9]==0?1:0);
		return transitionCount;
	}
	public static int transitionsInColumn(int[][] grid, int col){
		int transitionCount = 0;
		for (int row = 0; row < 19; row++) {
			if (grid[row][col] != grid[row+1][col]) {
				transitionCount++;
			}
		}
		transitionCount+=(grid[0][col]==0?1:0)+(grid[19][col]>0?1:0);
		return transitionCount;
	}
	public static int wellsInColumn(int[][] grid, int col){
		int wellValue=0;
		int blankCount=0;
		for (int row = 0; row < grid.length; row++) {
			if (grid[row][col]==0) {
				if ((col==0 || grid[row][col-1]>0) && (col==9 || grid[row][col+1]>0)) {
					blankCount++;
				}else {
					blankCount=0;
				}
				if (blankCount>2) wellValue++;
			}
		}
		return wellValue;
	}
	public static int getHeightOfColumn(int[][] grid, int col){
		for (int row = 19; row >=0; row--) {
			if (grid[row][col]>0) return row;
		}
		return -1;
	}
	public static int[] getHeights(int[][] grid){
		int[] heights = new int[10];
		for (int col = 0; col < heights.length; col++) {
			heights[col] = getHeightOfColumn(grid, col);
		}
		return heights;
	}
	public static int getMaxHeight(int[][]grid){
		int maxHeight=0;
		for (int col = 0; col < 10; col++) {
			int height = getHeightOfColumn(grid, col);
			if (height>maxHeight) maxHeight = height;
		}
		return maxHeight;
	}
	public static int countPieceCellsEliminated(int[][] grid){
		int cellsFound=0;
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[row].length; col++) {
				if (grid[row][col]==2) cellsFound++;
			}
			if (cellsFound==4) break;
		}
		return 4-cellsFound;
	}
	public static int countBuriedHolesInCol(int[][]grid, int col){
		int holes=0;
		for (int row = 0; row < getHeightOfColumn(grid, col); row++) {
			if (grid[row][col]==0) holes+=1;//(int)(3/Math.log1p(row+1));
		}
		return holes;
	}
	public static int[][] cloneGrid(int[][] grid){
		int[][] clone = new int[20][10];
		for (int i = 0; i < clone.length; i++) {
			clone[i]=grid[i].clone();
		}
		return clone;
	}
	public static int countHolesAdded(int[][] grid){
		int holes=0;
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[row].length; col++) {
				if (grid[row][col]==0 && 
					row < getHeightOfColumn(grid, col) ) {
					holes++;
				}
			}
		}
		return holes;
	}
}
