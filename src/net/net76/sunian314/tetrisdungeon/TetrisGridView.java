package net.net76.sunian314.tetrisdungeon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TetrisGridView extends View {
	static Paint[] blockPaints = {new Paint(), new Paint(), new Paint(), new Paint(), new Paint(), new Paint(), new Paint()};
	static Paint[] blockPaints2 = new Paint[7];
	Paint textPaint = new Paint();
	TetrisGrid grid;
	public TetrisGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		textPaint.setColor(Color.WHITE);
		blockPaints[0].setColor(Color.RED);
		blockPaints[1].setColor(Color.rgb(253,107,9));//orange
		blockPaints[2].setColor(Color.MAGENTA);
		blockPaints[3].setColor(Color.rgb(0,127,127));//dark cyan
		blockPaints[4].setColor(Color.BLUE);
		blockPaints[5].setColor(Color.rgb(127,0,55));//grape
		blockPaints[6].setColor(Color.rgb(127,51,0));//brown
		blockPaints[2].setColor(Color.rgb(178,0,255));//purple
		for (int i = 0; i < blockPaints.length; i++) {
			blockPaints2[i] = darkenPaint(blockPaints[i]);
			blockPaints2[i].setStyle(Style.STROKE);
			blockPaints2[i].setStrokeWidth(3);
		}
//		final Handler h = new Handler();
//		h.post(new Runnable() {
//			@Override
//			public void run() {
//				if (MainActivity.gameCanvasView.grid != null && MainActivity.gameCanvasView.grid.dirty) invalidate();
//				h.postDelayed(this, 20);
//			}
//		});
	}
	RectF decor = new RectF();
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawText("Score:", 3, 10, textPaint);
		canvas.drawText("" + MainActivity.myScore, 3, 30, textPaint);
		canvas.translate(GameCanvasView.transX, GameCanvasView.transY);
		if (MainActivity.gameCanvasView == null) return;
		grid = MainActivity.gameCanvasView.grid;
		if (grid == null) return;
//		Bitmap bmap = currentBitmap ? bmap1 : bmap2;
//		Canvas canvas = currentBitmap ? canvas1 : canvas2;
//		bmap.eraseColor(Color.TRANSPARENT);
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 10; j++) {
				TetrisBlock block = grid.blockGrid[i][j];
				if (block != null && !block.partOfCurrent){
//					System.out.println(block.row + "x" + block.col + "  " + block.myBounds);
					canvas.drawRoundRect(block.getBounds(), GameCanvasView.blockSize/6, GameCanvasView.blockSize/6, blockPaints[block.type]);
					decor.set(block.getBounds());
					decor.left += GameCanvasView.blockSize/4.0;
					decor.right -= GameCanvasView.blockSize/4.0;
					decor.top += GameCanvasView.blockSize/4.0;
					decor.bottom -= GameCanvasView.blockSize/4.0;
					canvas.drawRoundRect(decor, GameCanvasView.blockSize/12.0f, GameCanvasView.blockSize/12.0f, blockPaints2[block.type]);
//					canvas.drawRect(block.myBounds, blockPaints[block.type]);
					
				}
			}
		}
		
		canvas.drawBitmap(grid.bmapWalls, 0, 0, null);
		grid.dirty = false;
	}
	private Paint darkenPaint(Paint p){
		Paint paint = new Paint(p);
		int color = p.getColor();
		paint.setColor(Color.rgb((int)(Color.red(color)*0.75), (int)(Color.green(color)*0.75), (int)(Color.blue(color)*0.75)));
		return paint;
	}
}
