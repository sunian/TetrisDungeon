package net.net76.sunian314.tetrisdungeon;
public abstract class TetrisStrategy {
	int[][] grid;
	int blockType;
	int optimalLoc=0, optimalRot=0, optimalPriority=-9, optimalLinesCompleted=0;
	double optimal=-9999.9;
	int[][] optimalGrid=null;
	boolean notTooHigh=false, isHoldBlock;
	int[][][] blockStates;
	
	void evaluateBestMove(){
		
	}

//	I  44:209:255
	int[][][][] blockConfigs = {{
	{{0}, {4}, {4}},
	{{0,0,0,0}, {1,1,1,1}, {3}},
	},
	
//	O 255:217: 59
	{
		{{0,0}, {2,2}, {4}}
	},
//	T 232: 76:201
	{
	{{0,1}, {3,1}, {4}},
	{{1,0,1}, {1,2,1}, {3}},
	{{1,0}, {1,3}, {3}},
	{{0,0,0}, {1,2,1}, {3}}
	},

//	L 255:156: 35	ORANGE
	{
	{{0,0}, {3,1}, {4}},
	{{0,1,1}, {2,1,1}, {3}},
	{{2,0}, {1,3}, {3}},
	{{0,0,0}, {1,1,2}, {3}}
	},

//	J  68:124:255	BLUE
	{
	{{0,0}, {1,3}, {4}},
	{{0,0,0}, {2,1,1}, {4}},
	{{0,2}, {3,1}, {5}},
	{{1,1,0}, {1,1,2}, {4}}
	},
	
//	S 134:234: 51	GREEN
	{
		{{0,0,1}, {1,2,1}, {4}},
		{{1,0}, {2,2}, {4}}
	},
	
//	Z 255: 67: 92	RED
	{
		{{1,0,0}, {1,2,1}, {4}},
		{{0,1}, {2,2}, {4}}
	}
	};
}
