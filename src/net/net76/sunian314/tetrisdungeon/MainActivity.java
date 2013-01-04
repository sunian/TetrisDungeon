package net.net76.sunian314.tetrisdungeon;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener {

	boolean connected = false;
	static GameCanvasView gameCanvasView;
	static TetrisGridView tetrisGridView;
	
	WifiP2pManager manager;
	Channel channel;
	BroadcastReceiver receiver;
	IntentFilter intentFilter = new IntentFilter();
	ListView peerList;
	private ArrayAdapter<String> peerArrayAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.peerselect).setVisibility(View.GONE);
		/*peerArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		peerList = (ListView) findViewById(R.id.peerlist);
		peerList.setAdapter(peerArrayAdapter);
		peerList.setOnItemSelectedListener(this);
		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
		manager.discoverPeers(channel, new ActionListener() {
			@Override
			public void onSuccess() {
				
			}
			@Override
			public void onFailure(int reason) {
				if (peerArrayAdapter.getCount() == 0){
					peerArrayAdapter.add("No peers were found");
				}
			}
		});*/
		
		gameCanvasView = (GameCanvasView) findViewById(R.id.gameCanvasView1);
		tetrisGridView = (TetrisGridView)findViewById(R.id.tetrisGridView1);
		
		gameCanvasView.setOnTouchListener(new GremlinControls(gameCanvasView));
		gameCanvasView.startGame();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
//		registerReceiver(receiver, intentFilter);
	}
	@Override
	protected void onPause() {
		super.onPause();
//		unregisterReceiver(receiver);
	}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("destroyed!");

		disconnect();
    }
    private void disconnect(){
    	connected = false;
    }
    
    void showToast(final String msg){
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this.getBaseContext(), msg, Toast.LENGTH_SHORT).show();				
			}
		});
    }
    /**
     * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
     */
    public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager manager;
        private Channel channel;
        private MainActivity activity;
        PeerListListener peerListListener;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
        	super();
            this.manager = manager;
            this.channel = channel;
            this.activity = activity;
            peerListListener = new PeerListListener() {
				@Override
				public void onPeersAvailable(WifiP2pDeviceList peers) {
					for (WifiP2pDevice device : peers.getDeviceList()){
						peerArrayAdapter.add(device.deviceName + "\n" + device.deviceAddress);
					}
				}
			};
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println(action);
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
            	int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                	showToast("Wifi-Direct is supported on your device.");
                } else {
                    showToast("Warning: Wifi-Direct is not supported on your device.");
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
            	if (manager != null){
            		manager.requestPeers(channel, peerListListener);
            	}
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
    }
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onItemSelected(AdapterView<?> adapterView, View v, int arg2, long id) {
//		manager.stopPeerDiscovery(channel, null);
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
   
}
