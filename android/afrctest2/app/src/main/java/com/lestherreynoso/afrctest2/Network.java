package com.lestherreynoso.afrctest2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
// * {@link Network.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Network#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Network extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String ROUTER_NAME = "arfc";
    public static final String ROUTER_PASSWORD = "arfcarfc";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button connectButton;
    private Button startServerButton;
    private Button stopServerButton;
    private Button sendButton;
    private EditText ipAddressEditText;
    EditText routerSsidEditText;
    EditText routerPassEditText;
    EditText sendEditText;
//    EditText relayIpEditText;
    private String ipAddress;
    public  TextView networkDebug;
    private String connectedNetwork;
    WifiManager wifiManager;
    static Handler mhandler = new Handler();
    MainActivity mActivity = new MainActivity();
    Handler ndHandler;
    Boolean serverRunning = false;
    private String routerName;
    private String routerPass;
    private String relayIp;
//    Context mcontext = getActivity().getApplicationContext();



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Network.
     */
    // TODO: Rename and change types and number of parameters
    public static Network newInstance(String param1, String param2) {
        Network fragment = new Network();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Network() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_network, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        connectButton = (Button) view.findViewById(R.id.connectButton);
        startServerButton = (Button) view.findViewById(R.id.startServerbutton);
        stopServerButton = (Button) view.findViewById(R.id.stopSeverButton);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        ipAddressEditText = (EditText) view.findViewById(R.id.ipAddressEditText);
        routerSsidEditText = (EditText) view.findViewById(R.id.routerSsidEditText);
        routerPassEditText = (EditText) view.findViewById(R.id.routerPassEditText);
        sendEditText = (EditText) view.findViewById(R.id.sendEditText);
//        relayIpEditText = (EditText) view.findViewById(R.id.relayIpEditText);
        ipAddress = String.valueOf(ipAddressEditText.getText());
        networkDebug = (TextView) view.findViewById(R.id.debugTextView);
        networkDebug.setMovementMethod(new ScrollingMovementMethod());
        final Server server = new Server();
        ndHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {

                final String dMsg = (String) msg.obj;
                if (serverRunning) {
                    mActivity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
//                            networkDebug.append("Recieved: " + dMsg + "\n");
                              networkDebug.append(dMsg +"\n");
                        }
                    });
                }
                super.handleMessage(msg);
            }
        };

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress = String.valueOf(ipAddressEditText.getText());
                initSettings();
                if (ipAddress.isEmpty()){
                    networkDebug.append("touched connect with no ip entry \n");
                }else {
                    networkDebug.append("touched connect with ip entry of: " + ipAddress + "\n");
                }
                if(isConnectedToARFC()){
                    //get ip and connect to it
                    networkDebug.append("already connected to "+ ROUTER_NAME + "\n");
                }
                else{
                    networkDebug.append("not connected to "+ ROUTER_NAME + "\n");
                    if(isARFCInRange()){
                        connectToARFC();
                    }
                    else{
                        networkDebug.append(ROUTER_NAME + " is not in range\n");
                    }

                }
                networkDebug.append("Range: "+getARFCRange()+"%");
                networkDebug.append("\n");
            }
        });

        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!serverRunning) {
                    networkDebug.append("Starting Server...\n");
//                server.start(MainActivity.mainHandler);
                    server.start(ndHandler);
//                mhandler.post(UDPClientRunnable);
                    networkDebug.append("Running...\n");
                    serverRunning = true;
                }
                else{
                    networkDebug.append("Server already running \n");
                }
            }
        });
        stopServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(serverRunning) {
                    networkDebug.append("Stopping Server...\n");
//                server.start(MainActivity.mainHandler);
                    server.stop();
//                mhandler.post(UDPClientRunnable);
                    networkDebug.append("done.\n");
                    serverRunning = false;
                }
                else{
                    networkDebug.append("Server not running \n");
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(serverRunning) {
                    if(!sendEditText.getText().toString().isEmpty()) {
                        server.send(sendEditText.getText().toString());
                    }else{
                        server.send("nothing was inputted so yea..");
                    }
//                    networkDebug.append("sent \n");
                }else{
                    networkDebug.append("Server not running \n");
                }
            }
        });
    }

    private void initSettings() {
        if(routerSsidEditText.getText().toString().isEmpty()){
            routerName = "arfc";
        }else{routerName = routerSsidEditText.getText().toString();}
        if(routerPassEditText.getText().toString().isEmpty()){
            routerPass = "arfcarfc";
        }else{routerPass = routerPassEditText.getText().toString();}
//        if(relayIpEditText.getText().toString().isEmpty()){
//            relayIp = "192.168.0.1";
//        }else{relayIp = relayIpEditText.getText().toString();}
    }

    private int getARFCRange() {
        wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        int rssi = wifiManager.getConnectionInfo().getRssi();
        int level = wifiManager.calculateSignalLevel(rssi, 11);
        int signalStrength =  (int) ((level/10.0)*100);
        return signalStrength;
    }

    private boolean isARFCInRange() {
        networkDebug.append("verifying if "+ ROUTER_NAME +" is in range\n");
//        WifiManager wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();

        networkDebug.append("results of scan \n");
        for (ScanResult i : scanResults){
            networkDebug.append(i.SSID +"\n");
            if(i.SSID.equals(ROUTER_NAME)){
                return true;
            }
        }
        //TODO async task to initiate scan
        return false;
    }

    private void connectToARFC() {
//        networkDebug.append("connecting to "+ ROUTER_NAME +"\n");
        networkDebug.append("connecting to "+ routerName +"\n");

//        WifiManager wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
//        String networkSSID = ROUTER_NAME;
//        String networkPass = ROUTER_PASSWORD;
        String networkSSID = routerName;
        String networkPass = routerPass;

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";

        //WEP
//        conf.wepKeys[0] = "\"" + networkPass + "\"";
//        conf.wepTxKeyIndex = 0;
//        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        //WPA
        conf.preSharedKey = "\""+ networkPass +"\"";
        //OPEN
//        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
//                networkDebug.append("connected to "+ ROUTER_NAME +"\n");
                networkDebug.append("connected to "+ routerName +"\n");
                break;
            }
            else{
//                networkDebug.append("failed to find configuration for "+ ROUTER_NAME +"\n");
                networkDebug.append("failed to find configuration for "+ routerName +"\n");
                networkDebug.append(i.SSID.toString()+ "\n"); //show networks
            }
        }
    }

    private Boolean isConnectedToARFC() {
        networkDebug.append("verifying if device is already connected to arfc\n");
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
//            networkDebug.append("networkInfo is not null and is connected \n");
//            networkDebug.append(networkInfo.getDetailedState().toString()+"\n");
//            networkDebug.append(networkInfo.getExtraInfo() + "\n");
//            networkDebug.append(networkInfo.toString() +"\n");
            connectedNetwork = networkInfo.getExtraInfo().replace("\"", "");
//            if (connectedNetwork.equals(ROUTER_NAME)){
            if (connectedNetwork.equals(routerName)){
                return true;
            }
            else return false;
        } else {
            networkDebug.append("networkInfo is null or not connected \n");
            connectedNetwork = "not connected to a network";
            return false;
        }
    }
}
