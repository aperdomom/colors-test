package com.example.mahisoft.mylocationapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.Collection;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link BluetoothFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BluetoothFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private int mSection;
    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;
    private BeaconConsumer beaconConsumer;
    private BootstrapNotifier application;
    Context context;
    TextView textView;
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                //Toast.makeText(context, "Device: " + name + ",  RSSI: " + rssi + "dBm", Toast.LENGTH_SHORT).show();
                textView.setText(textView.getText()+ " Device: " + name + ",  RSSI: " + rssi + "dBm\n");
            }
        }
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment BluetoothFragment.
     */
    public static BluetoothFragment newInstance(int sectionNumber) {
        BluetoothFragment fragment = new BluetoothFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        if (getArguments() != null) {
            mSection = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        getActivity().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        beaconManager = BeaconManager.getInstanceForApplication(this.getActivity());

        beaconConsumer = new InternalBeaconConsumer();
        beaconManager.bind(beaconConsumer);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(
                R.layout.fragment_bluetooth,
                container,
                false
        );
        Button button = (Button) contentView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BTAdapter.startDiscovery();
            }
        });
        textView = (TextView) contentView.findViewById(R.id.text);

        return contentView;
    }


    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(beaconConsumer);
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }




    private class InternalBeaconConsumer implements BeaconConsumer {

        /**
         * Method reserved for system use
         */
        @Override
        public void onBeaconServiceConnect() {
            beaconManager.setRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    if (beacons.size() > 0) {
                        Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                    }else {
                        Log.i(TAG, "Non Devices found");
                    }
                }
            });

            try {
                Region _tagRegion = new Region("myUniqueBeaconId",
                        null,
                        null, null);

                beaconManager.startRangingBeaconsInRegion(_tagRegion);
            } catch (RemoteException e) {    }

        }

        /**
         * Method reserved for system use
         */
        @Override
        public boolean bindService(Intent intent, ServiceConnection conn, int arg2) {
            return getActivity().getApplicationContext().bindService(intent, conn, arg2);
        }

        /**
         * Method reserved for system use
         */
        @Override
        public Context getApplicationContext() {
            return getActivity().getApplicationContext();
        }

        /**
         * Method reserved for system use
         */
        @Override
        public void unbindService(ServiceConnection conn) {
            getActivity().getApplicationContext().unbindService(conn);
        }
    }
}
