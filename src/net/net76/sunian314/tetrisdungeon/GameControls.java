package net.net76.sunian314.tetrisdungeon;

import android.view.View.OnTouchListener;

public abstract class GameControls implements OnTouchListener {
	public boolean running = true;
	public void killThreads(){
		running = false;
	}
}
