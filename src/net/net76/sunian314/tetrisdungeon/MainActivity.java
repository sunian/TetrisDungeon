package net.net76.sunian314.tetrisdungeon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener {

	static boolean connected = false, isPrisoner = true, startNew = true, allowOutput = false;
	static GameCanvasView gameCanvasView;
	static TetrisGridView tetrisGridView;
	static int myScore = 0;
	private BluetoothSocket btSocket = null;
	static BufferedOutputStream outStream = null;
//	static print outStream = null;
	static BufferedInputStream inStream = null;
	private Thread bluetoothThread;
	ListView pairedDevicesList;
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private BluetoothAdapter btAdapter;
	static int players = 2;

	// Well known SPP UUID
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//SPP
//	private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//	private static final UUID MY_UUID = UUID.fromString("a5b64918-ff42-4199-823d-a0b11f675826");
//	private static final UUID MY_UUID = UUID.fromString("00000003-0000-1000-8000-00805F9B34FB");//RFCOMM
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		pairedDevicesList = (ListView) findViewById(R.id.peerlist);
		pairedDevicesList.setAdapter(pairedDevicesArrayAdapter);
		pairedDevicesList.setOnItemClickListener(this);
		connected = false;
		startNew = true;
		outStream = null;
		inStream = null;

        // Get the local Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 3);
        }

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
            	pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
		
        Button hostButton = (Button) findViewById(R.id.button_server);
        hostButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		hostGame();
        	}
        });
        
        Button p1Button = (Button) findViewById(R.id.button_single);
        p1Button.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		players = 1;
        		findViewById(R.id.peerselect).setVisibility(View.GONE);
				gameCanvasView.startGame(MainActivity.this);
        	}
        });
        
		gameCanvasView = (GameCanvasView) findViewById(R.id.gameCanvasView1);
		tetrisGridView = (TetrisGridView)findViewById(R.id.tetrisGridView1);
		
	}
	void runBluetoothThread(){
        bluetoothThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("reading...");
					int input = inStream.read();
					if (input == 42) connected = true;
					System.out.println(input + " " + connected);
					if (connected){
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								findViewById(R.id.peerselect).setVisibility(View.GONE);
								gameCanvasView.startGame(MainActivity.this);
								
							}
						});
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        bluetoothThread.start();
	}
	void startNewGame(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (gameCanvasView == null) return;
				isPrisoner = !isPrisoner;
				gameCanvasView.setupGame();
				gameCanvasView.startGame(MainActivity.this);
			}
		});
	}
	void quitGame(final boolean notify){
		startNew = false;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (findViewById(R.id.peerselect).getVisibility() == View.GONE){
					disconnect(notify);
					gameCanvasView = null;
					tetrisGridView = null;
					setResult(LauncherActivity.RESTART);
					finish();
//					gameCanvasView.setupGame();
//					findViewById(R.id.peerselect).setVisibility(View.VISIBLE);
				}
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_forfeit:
			forfeitGame();
			return true;
		case R.id.menu_quit:
			quitGame(true);
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	void forfeitGame() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (findViewById(R.id.peerselect).getVisibility() == View.GONE){
					startNew = true;
					gameCanvasView.myControls.killThreads();
					writeToStream(TetrisControls.FORFEIT);
					allowOutput = false;
				}				
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
//		registerReceiver(receiver, intentFilter);
	}
	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("pause");
//		unregisterReceiver(receiver);
	}
	@Override
	public void onBackPressed() {
		System.out.println("back pressed");
//		disconnect(true);
		super.onBackPressed();
	}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("destroyed!");

		disconnect(true);
    }
    void disconnect(boolean notify){
    	showToast("disconnecting...");
    	boolean canWrite = connected;
    	connected = false;
    	try {
//			if (bluetoothThread.isAlive()) bluetoothThread.join();
			if (btSocket != null) {
				if (canWrite ){
					outStream.write(TetrisControls.QUIT_GAME);
					outStream.flush();
				}
				if (inStream != null){
					inStream.close();
				}
				if (outStream != null){
					outStream.close();
				}
				btSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    private void connectToHost(BluetoothDevice device){
    	isPrisoner = false;
        try {
        	btAdapter.cancelDiscovery();
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
//			Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//			btSocket = (BluetoothSocket)m.invoke(device, Integer.valueOf(1));
			btSocket.connect();
			System.out.println("host responded");
			showToast(device.getName() + " responded");
			inStream = new BufferedInputStream(btSocket.getInputStream());
			outStream = new BufferedOutputStream(btSocket.getOutputStream());
			runBluetoothThread();
			outStream.write(42);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			showToast("connection failed");
			System.out.println("no connection");
//		} catch (NoSuchMethodException e) {
//			showToast("createRfcommSocket DNE!");
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
		}
    }
    static void writeToStream(byte[] bytes){
    	if (!connected || !allowOutput) return;
    	try {
			outStream.write(bytes);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    static void writeToStream(int aChar){
    	if (!connected || !allowOutput) return;
    	try {
    		outStream.write(aChar);
    		outStream.flush();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    void showToast(final String msg){
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this.getBaseContext(), msg, Toast.LENGTH_SHORT).show();				
			}
		});
    }
    
	private void hostGame(){
    	isPrisoner = true;
    	Thread serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				BluetoothServerSocket mmServerSocket;
		    	try {
		    		mmServerSocket = btAdapter.listenUsingRfcommWithServiceRecord("TetrisDungeon", MY_UUID);
		    		showToast("waiting...");
					btSocket = mmServerSocket.accept();
					if (btSocket != null){
						showToast("Request from " + btSocket.getRemoteDevice().getName());
						System.out.println(btSocket.getRemoteDevice().getName());
						outStream = new BufferedOutputStream(btSocket.getOutputStream());
						inStream = new BufferedInputStream(btSocket.getInputStream());
						runBluetoothThread();
						System.out.println("connecting to " + btSocket.getRemoteDevice().getAddress());
						outStream.write(42);
						outStream.flush();
						
					}
					mmServerSocket.close();
				} catch (IOException e) {
					showToast("server failed");
					e.printStackTrace();
				}
			}
		});
		serverThread.start();
    	
    }

	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int arg2, long id) {
		// Get the device MAC address, which is the last 17 chars in the View
        String info = ((TextView) v).getText().toString();
        String address = info.substring(info.length() - 17);
        
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        for (BluetoothDevice dev : btAdapter.getBondedDevices()){
        	if (dev.getAddress().equals(address)){
        		device = dev;
        		break;
        	}
        }
        connectToHost(device);
	}
   
}
