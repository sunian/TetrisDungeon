package net.net76.sunian314.tetrisdungeon;
public class TetrisStrategyPierreDellacherie extends TetrisStrategy{
	
	public void evaluateBestMove(int[][] grid, int blockType, boolean isHoldBlock){
		optimalLoc=0; optimalRot=0; optimalPriority=-9; optimalLinesCompleted=0; optimal=-9999.9;
		optimalGrid=null; notTooHigh = false;
		blockStates = blockConfigs[blockType];
		int landingHeight=0;
//		notTooHigh=TetrisMethods.getMaxHeight(grid)<8 && blockType!=0;
		int[] heights = TetrisMethods.getHeights(grid);
		for (int rot = 0; rot < blockStates.length; rot++) {
			if (notTooHigh && blockType==6 && 
					TetrisMethods.getHeightOfColumn(grid, 7)>2+TetrisMethods.getHeightOfColumn(grid, 8) &&
					TetrisMethods.getHeightOfColumn(grid, 9)==TetrisMethods.getHeightOfColumn(grid, 8)) {
				notTooHigh=false;
			}
			int[][] block = blockStates[rot];
			for (int loc = 0; loc <= (notTooHigh?9:10)-block[0].length; loc++) {
				int[][] _grid = TetrisMethods.cloneGrid(grid);
				int[] _heights = heights.clone();
				int smallestDiff=30, smallestCol=0;
				for (int col = 0; col < block[0].length; col++) {
					int dH = block[0][col] - _heights[loc+col];
					if (dH<smallestDiff) {
						smallestDiff=dH; smallestCol=col;
					}
				}
				
				for (int col = 0; col < block[0].length; col++) {
					int bottomLoc = heights[loc+smallestCol]+1+(block[0][col]-block[0][smallestCol]);
					
					for (int row = 0; row < block[1][col]; row++) {
						try {
							_grid[bottomLoc+row][loc+col]=2;
							_heights[loc+col]=bottomLoc+row;
							landingHeight+=bottomLoc+row;
						} catch (ArrayIndexOutOfBoundsException e) {}
														
					}
				}
				landingHeight*=.25;
				int pileHeight = TetrisMethods.getMaxHeight(_grid);
				int completedRows = TetrisMethods.removeCompletedRowsFromGrid(_grid, (notTooHigh?9:10));
				int erodedPieceCellsMetric = 0;
				if (completedRows > 0){
					int pieceCellsEliminated = TetrisMethods.countPieceCellsEliminated(_grid);
					erodedPieceCellsMetric = (completedRows+ (notTooHigh?0:2)) * pieceCellsEliminated;
					
				}
				
				int boardRowTransitions = 2 * (19 - pileHeight);
				for (int row = 0; row <=pileHeight; row++) {
					boardRowTransitions+=TetrisMethods.transitionsInRow(_grid, row);
				}
				int boardColumnTransitions = 0;
	            int boardBuriedHoles = 0;
	            int boardWells = 0;
	            for (int col = 0; col < 10; col++) {
	            	boardColumnTransitions+=TetrisMethods.transitionsInColumn(_grid, col);
	            	boardWells+=TetrisMethods.wellsInColumn(_grid, col);
	            	boardBuriedHoles+=TetrisMethods.countBuriedHolesInCol(_grid, col);
				}
	            double rating = 0;
	            rating+= -1.0 * landingHeight;
	            rating+=  1.0 * erodedPieceCellsMetric;
	            rating+= -1.0 * boardRowTransitions;
	            rating+= -1.0 * boardColumnTransitions;
	            rating+= (notTooHigh?-5.0:-4.0) * boardBuriedHoles;
	            rating+= -1.0 * boardWells;
	            
	            int absoluteDistanceX  = Math.abs(loc-blockStates[rot][2][0]);
	            int priority = 0;
	            priority += (100 * absoluteDistanceX);
	            if (loc<blockStates[rot][2][0]) priority += 10;
	            if (isHoldBlock) priority -= 10;
	            if (optimalRot==1 || optimalRot==3) {
					priority--;
	            }else if (optimalRot==2) {
	            	priority-=2;
				}
	            
	            if (rating>optimal || (rating==optimal && priority>optimalPriority)) {
					optimal=rating;
					optimalPriority=priority;
					optimalLoc=loc;
					optimalRot=rot;
					optimalLinesCompleted=completedRows;
					optimalGrid=_grid;
				}
	            
				
			}
		}
	}
}
