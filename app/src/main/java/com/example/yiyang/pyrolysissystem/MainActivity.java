package com.example.yiyang.pyrolysissystem;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();


    public static final int REQUEST_SERIAL_PORT = 1;
    private boolean isConnect = false;

    private static UsbSerialPort sPort = null;

    private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;


    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };


    private static final String ACTION_USB_PERMISSION =
            "com.example.yiyang.pyrolysissystem.USB_PERMISSION";

//    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (ACTION_USB_PERMISSION.equals(action)) {
//                synchronized (this) {
//                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        if (device != null) {
//                            //call method to set up device communication
//                        }
//                    } else {
//                        Log.d(TAG, "permission denied for device " + device);
//                    }
//                }
//            }
//        }
//    };

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private StringBuilder mBuffer = new StringBuilder();


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SERIAL_PORT) {
//                Toast.makeText(this, "Settings Back", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Navigate back from SettingsActivity");
            }
        }

    }

    private void openSerialPort() {
        if (sPort == null) {
            mTitleTextView.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());

            if (connection == null) {
                mTitleTextView.setText("Opening device failed");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
            mTitleTextView.setText("Serial device: " + sPort.getClass().getSimpleName());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        mTitleTextView = (TextView) findViewById(R.id.demoTitle);
        mDumpTextView = (TextView) findViewById(R.id.consoleText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);


        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

//        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//        registerReceiver(mUsbReceiver, filter);
    }

//    @Override
//    protected void onStop() {
//        unregisterReceiver(mUsbReceiver);
//        super.onStop();
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_SERIAL_PORT);
                return true;

//            case R.id.action_connect:
//                if (isConnect) {
//                    //close serial port
//                    item.setIcon(getDrawable(R.drawable.ic_action_play));
//                } else {
//                    //open serial port
//                    item.setIcon(getDrawable(R.drawable.ic_action_stop));
//
//                }
//                isConnect = !isConnect;
//
//                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }


//    void showStatus(TextView theTextView, String theLabel, boolean theValue) {
//        String msg = theLabel + ": " + (theValue ? "enabled" : "disabled") + "\n";
//        theTextView.append(msg);
//    }


    @Override
    protected void onResume() {
        super.onResume();
        if (sPort != null) {
            UsbDevice device = sPort.getDriver().getDevice();
            if (mUsbManager.hasPermission(device)) {
                openSerialPort();
            } else {
//                mUsbManager.requestPermission(sPort.getDriver().getDevice(), mPermissionIntent);
                Toast.makeText(this, "No permission for using " + sPort.getDriver().getDevice().toString(), Toast.LENGTH_LONG).show();
            }
        }

        onDeviceStateChange();
    }

    @Override
    protected void onPause() {
        if (sPort!=null) {
            try {
                sPort.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());

        try {
            mBuffer.append(new String(data, "ASCII"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        checkBuffer();


    }

    private void checkBuffer() {
        String MsgSeparator = "\r";
        boolean isDone = false;
        while (!isDone) {
            int end = mBuffer.indexOf(MsgSeparator);
            if (end == -1)
                isDone = true;
            else {
                String msg = mBuffer.substring(0, end);
                mBuffer.delete(0, end + MsgSeparator.length());
//                Log.i("Test", msg);
                parseMessage(msg);
            }
        }
    }

    private void parseMessage(String msg) {
        Map<String, Integer> map = new HashMap<String, Integer>();

        map.put("H1", R.id.txtH1);
        map.put("H2", R.id.txtH2);
        map.put("H3", R.id.txtH3);
        map.put("H4", R.id.txtH4);
        map.put("H5", R.id.txtH5);
        map.put("T1", R.id.txtT1);
        map.put("T2", R.id.txtT2);
        map.put("M1", R.id.txtM1);
        map.put("M2", R.id.txtM2);

        String SentenceSeparator = ",";

        String[] datas = msg.split(SentenceSeparator);

        int id = 0;
        switch (datas[0]) {
            case "H1":
            case "H2":
            case "H3":
            case "H4":
            case "H5":
            case "T1":
            case "T2":
            case "M1":
            case "M2":
                id = map.get(datas[0]);
                ((TextView) findViewById(id)).setText(datas[0] + " : " + datas[1]);
                break;

        }
    }


    static void setSerialPort(UsbSerialPort port) {
        sPort = port;
    }

}
