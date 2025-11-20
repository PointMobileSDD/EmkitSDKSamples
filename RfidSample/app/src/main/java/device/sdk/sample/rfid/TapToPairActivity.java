package device.sdk.sample.rfid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import device.common.DevInfoIndex;
import device.common.rfid.RFIDCallback;
import device.common.rfid.RFIDConst;
import device.sdk.RFIDManager;
import device.sdk.sample.rfid.R;
import device.sdk.sample.rfid.util.PreferenceUtil;
import device.sdk.sample.rfid.util.Utils;

public class TapToPairActivity extends AppCompatActivity
{
    private final String TAG = getClass().getSimpleName();
    private final String NFC_SETTING_ACTION = "com.android.settings.ADVANCED_CONNECTED_DEVICE_SETTINGS";

    private RFIDManager mRfidMgr;

    private PreferenceUtil mPrefUtil;
    private Utils mUtil;
    private ProgressDialog mProgress = null;

    private String mDeviceName;
    private String mMacAddress;

   private boolean isFirstAction;

   private ImageView mImageRF300;
   private ImageView mImageRF851;

   private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_to_pair);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.nfc_tap_to_pair);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mImageRF851 = findViewById(R.id.iv_tap_to_pair_rf851);
        mImageRF300 = findViewById(R.id.iv_tap_to_pair_rf300);

        mRfidMgr = RFIDManager.getInstance();
        mPrefUtil = new PreferenceUtil(device.sdk.sample.rfid.TapToPairActivity.this);
        mUtil = new Utils(this);
        mProgress = new ProgressDialog(device.sdk.sample.rfid.TapToPairActivity.this);

        setImage();
    }

    private void setImage()
    {
        if(mUtil.getDevice() == DevInfoIndex.PM30_MAJOR)
        {
            mImageRF300.setVisibility(View.VISIBLE);
            mImageRF851.setVisibility(View.GONE);
        }
        else
        {
            mImageRF300.setVisibility(View.GONE);
            mImageRF851.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume");
        super.onResume();

        isFirstAction = true;

        registerStateReceiver(mBluetoothStateReceiver);
        checkNFC();
    }

    private void registerStateReceiver(BroadcastReceiver receiver)
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.EXTRA_CONNECTION_STATE);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        try
        {
            registerReceiver(receiver, intentFilter);
        } catch(Exception e)
        {
            Log.d(TAG,"Broadcast receiver is already registered");
        }
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause");
        unRegisterStateReceiver(mBluetoothStateReceiver);
        super.onPause();
    }

    private void unRegisterStateReceiver(BroadcastReceiver broadcastReceiver)
    {
        try
        {
            unregisterReceiver(broadcastReceiver);
        } catch(Exception e)
        {
            Log.d(TAG, "Broadcast receiver is not registered");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestory");

        removHandlerMessage();
    }

    private void removHandlerMessage()
    {
        if(mHandler != null)
            mHandler.removeMessages(0);
        else
            Log.d(TAG,"Handler is null");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
//            if(!isFail)
//                setResult(Activity.RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkNFC()
    {
        boolean result = false;

        NfcManager nfcManager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
        NfcAdapter nfcAdapter = nfcManager.getDefaultAdapter();
        if(nfcAdapter != null)
        {
            if(nfcAdapter.isEnabled())
                result = true;
            else
                requestNFCEnable();
        }
        else
        {
            showNFCNotSupport();
            result = false;
        }

        return result;
    }

    private void showNFCNotSupport()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(device.sdk.sample.rfid.TapToPairActivity.this);
        builder.setTitle(R.string.nfc_tap_to_pair);
        builder.setMessage(R.string.nfc_not_support);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void requestNFCEnable()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(device.sdk.sample.rfid.TapToPairActivity.this);
        builder.setTitle(R.string.nfc_tap_to_pair);
        builder.setMessage(R.string.nfc_enable);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    startActivity(new Intent(NFC_SETTING_ACTION));
                else
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "-- action : " + action);

            if(action.equalsIgnoreCase(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                Log.d(TAG, "-- state :  BluetoothDevice.EXTRA_BOND_STATE " + state);

                if(state == BluetoothDevice.BOND_BONDING)
                {
                    Log.d(TAG, "-- state :  BluetoothDevice.BOND_BONDING " + state);
                }
                else if(state == BluetoothDevice.BOND_BONDED)
                {
                    Log.d(TAG, "-- state :  BluetoothDevice.BOND_BONDED -- macAddress : " + device.getAddress() + " Device : " + device.getName());
                    Log.d(TAG,isFirstAction ? "isFirstAction = true" : "isFirstAction = false");
                    if(isFirstAction)
                    {
                        mRfidMgr.RegisterRFIDCallback(mRfidStateCallback);
                        Log.d(TAG,"Registered RFIDCallback");
                        connectDevice(device);
                        isFirstAction = false;
                    }
                }
                else if(state == BluetoothDevice.BOND_NONE)
                {
                    Log.d(TAG, "-- state :  BluetoothDevice.BOND_NONE " + state);
                }
            }
            else if(action.equalsIgnoreCase(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED))
            {
                Log.d(TAG, "ACTION_CONNECTION_STATE_CHANGED");
                Log.d(TAG, "STATE_CHANGED -- macAddress : " + device.getAddress() + " device : " + device.getName());
                Log.d(TAG,isFirstAction ? "isFirstAction = true" : "isFirstAction = false");
                if(isFirstAction)
                {
                    mRfidMgr.RegisterRFIDCallback(mRfidStateCallback);
                    Log.d(TAG,"Registered RFIDCallback");
                    connectDevice(device);
                    isFirstAction = false;
                }
            }
        }
    };

    private void connectDevice(BluetoothDevice device)
    {
        mDeviceName = device.getName();
        mMacAddress = device.getAddress();
        String bondedMacAddr = mPrefUtil.getStringPreference(PreferenceUtil.KEY_CONNECT_BT_MACADDR);
        String bondedDevice = mPrefUtil.getStringPreference(PreferenceUtil.KEY_CONNECT_BT_NAME);

        //Try to connect when another reader is connected
        if(mRfidMgr.IsOpened())
        {
            if(!bondedDevice.equalsIgnoreCase(mDeviceName))
            {
                mUtil.showProgress(mProgress, device.sdk.sample.rfid.TapToPairActivity.this, true);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED)
                {
                    Log.d(TAG, "Currently connected. Disconnect connected device first.");
                    Log.d(TAG, "Previous device --- MacAddress : " + bondedMacAddr + " Device Name : " + bondedDevice);
                    mRfidMgr.Close();
                    Log.d(TAG,"close18");
                    mRfidMgr.DisconnectBTDevice();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_already_connected),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else
        {
            mUtil.showProgress(mProgress, device.sdk.sample.rfid.TapToPairActivity.this, true);
            Log.d(TAG, "ConnectBTDevice");
            mRfidMgr.ConnectBTDevice(mMacAddress, mDeviceName);
            clean();
        }
    }

    private void sleep(int mills)
    {
        try
        {
            Thread.sleep(mills);
        } catch(InterruptedException e)
        {
        }
    }

    private void clean()
    {
        mHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(TAG,"clean");
                isFirstAction = true;
                mRfidMgr.UnregisterRFIDCallback(mRfidStateCallback);
                Log.d(TAG,"Unregistered RFIDCallback");
                mUtil.showProgress(mProgress, device.sdk.sample.rfid.TapToPairActivity.this, false);
            }
        }, 10000);
    }

    private Handler mCallbackHandler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            return true;
        }
    });

    private RFIDCallback mRfidStateCallback = new RFIDCallback(mCallbackHandler)
    {
        @Override
        public void onNotifyChangedState(int state)
        {
            super.onNotifyChangedState(state);
            if(state == RFIDConst.DeviceState.BT_CONNECTED)
            {
                Log.d(TAG, "BT_CONNECTED");
                removHandlerMessage();
                AsyncRfidOpen asyncRfidOpen = new AsyncRfidOpen();
                asyncRfidOpen.execute();
            }
            else if(state == RFIDConst.DeviceState.BT_DISCONNECTED)
            {
                Log.d(TAG, "BT_DISCONNECTED");
                sleep(50);
                Log.d(TAG, "Connect with tapped device -- MacAddress : " +  mMacAddress + "Device Name : " + mDeviceName);
                mRfidMgr.ConnectBTDevice(mMacAddress, mDeviceName);
                clean();
            }
        }
    };

    class AsyncRfidOpen extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... voids)
        {
            boolean result = true;
            int tryCount = 0;
            Log.d(TAG, "Invoke RFID OPEN");
            mRfidMgr.Open(RFIDConst.DeviceType.DEVICE_BT);
            do
            {
                if(mRfidMgr.IsOpened())
                {
                    Log.d(TAG, "RFID OPENED");
                    result = true;
                    break;
                }
                else
                {
                    tryCount++;
                }

                if(tryCount > 10)
                {
                    result = false;
                    break;
                }
            }
            while(true);

            if(result)
            {
                String macAddr = mUtil.getBTMacAddress();
                String devName = mUtil.getDeviceName();

                if(macAddr != null && !macAddr.isEmpty())
                    mPrefUtil.putStringPrefrence(PreferenceUtil.KEY_CONNECT_BT_MACADDR, macAddr);

                if(devName != null && !devName.isEmpty())
                    mPrefUtil.putStringPrefrence(PreferenceUtil.KEY_CONNECT_BT_NAME, devName);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            mRfidMgr.UnregisterRFIDCallback(mRfidStateCallback);
            Log.d(TAG,"Unregistered RFIDCallback");
            mUtil.showProgress(mProgress, device.sdk.sample.rfid.TapToPairActivity.this, false);

            if(result)
            {
                Log.d(TAG, "AsyncRFIDOpen Success");
                unRegisterStateReceiver(mBluetoothStateReceiver);
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                Log.d(TAG, "Tap-To-Pair finish");
                finish();
            }
            else
            {
                Log.d(TAG, "AsyncRFIDOpen Failed");
                mRfidMgr.Close();
            }
        }
    }
}
