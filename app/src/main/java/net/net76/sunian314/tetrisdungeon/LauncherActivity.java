package net.net76.sunian314.tetrisdungeon;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class LauncherActivity extends Activity {
    static final int RESTART = 123574127;//leet for restart
    static Bitmap androidSpriteSheet, explosionSpriteSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Launching game");
        androidSpriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.android_sprites);
        explosionSpriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.explosion_sprites);
        startActivityForResult(new Intent(this, MainActivity.class), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESTART) {
            startActivityForResult(new Intent(this, MainActivity.class), 0);
        } else {
            System.out.println("Exiting game");
            finish();
        }
    }
}
