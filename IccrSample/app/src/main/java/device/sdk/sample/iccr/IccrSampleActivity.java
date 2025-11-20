package device.sdk.sample.iccr;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import device.sdk.MsrManager;
import device.common.MsrResultCallback;

public class IccrSampleActivity extends Activity {

    private static final String TAG = IccrSampleActivity.class.getName();

    public static MsrManager msrManager;

    private Button ATRBtn;
    private Button APDUBtn;
    private Button PowerDownBTn;
    private Button EMVBtn;
    private TextView ResultStatusView;
    private TextView CardStatusView;
    private EditText ApduInput;
    private RadioButton radioIsoBtn;

    public int count  = 0;
    public boolean isInserted = false;
    private boolean isConnected = false;

    private static final int msrResultCallbackRead = 0;
    private static final int msrResultCallbackFail = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iccr_sample);

        Initialize();
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop++++");
        if(msrManager != null){
            msrManager.PowerDown();
            msrManager.DeviceMsrClose();
            msrManager = null;
        }
        Log.i(TAG, "onStop----");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onStop++++");
        if(msrManager == null){
            msrManager = new MsrManager();
            msrManager.DeviceMsrOpen(msrResultCallback);
        }
        Log.i(TAG, "onStop----");
    }

    private void Initialize() {
        // Button Initialize
        Log.i(TAG, "Initizlize++++");
        ATRBtn = (Button) findViewById(R.id.ATRBtn);
        ATRBtn.setOnClickListener(ClickEvent);
        radioIsoBtn = (RadioButton) findViewById(R.id.atrModIsoBtn);
        APDUBtn = (Button) findViewById(R.id.APDUBtn);
        APDUBtn.setOnClickListener(ClickEvent);
        PowerDownBTn = (Button) findViewById(R.id.PowerDownBtn);
        PowerDownBTn.setOnClickListener(ClickEvent);
        EMVBtn = (Button) findViewById(R.id.EMVBtn);
        EMVBtn.setOnClickListener(ClickEvent);
        ResultStatusView = (TextView) findViewById(R.id.ResultStatusView);
        ResultStatusView.setMovementMethod(new ScrollingMovementMethod());
        CardStatusView = (TextView) findViewById(R.id.cardStatusView);
        ApduInput = (EditText) findViewById(R.id.apdu_input);
        ApduInput.setInputType(InputType.TYPE_CLASS_NUMBER+InputType.TYPE_CLASS_PHONE);
        msrManager = new MsrManager();
        msrManager.DeviceMsrOpen(msrResultCallback);


        if(msrManager.GetCardInserted() == MsrManager.CardInsertState.ICC_DETECT){
            isInserted = true;
        }else{
            isInserted = false;
        }

        ChangeUI();
        String version =  msrManager.getFirmwareVersion();

        Log.i(TAG, "version");
        Log.i(TAG, "Initizlize----");
    }

    MsrResultCallback msrResultCallback = new MsrResultCallback() {
        @Override
        public void onResult(int cmd, int status) {
            super.onResult(cmd, status);
            int track2result = (status >> 8) & 0x2;

            if(track2result ==0){
                mUIHandler.sendEmptyMessage(msrResultCallbackRead);
            }else{
                mUIHandler.sendEmptyMessage(msrResultCallbackFail);
            }
        }
    };

    private final Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            int status = (int) msg.arg1;
            switch (msg.what) {
                case msrResultCallbackRead:
                    ResultStatusView.setHint(msrManager.DeviceMsrGetData(0x07).getMsrTrack2() +"\n"+ getString(R.string.ejection_str));
                    break;
                case msrResultCallbackFail:
                    ResultStatusView.setHint(getString(R.string.null_str));
                    break;
            }
        }
    };

    View.OnClickListener ClickEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            
            if (id == R.id.ATRBtn) {
                Log.i(TAG, "ATR On++++");
                if(msrManager.GetCardInserted() == MsrManager.CardInsertState.ICC_DETECT){
                    isInserted = true;
                }else{
                    isInserted = false;
                }ChangeUI();

                MsrManager.AtrOnMode atrMode;

                if (radioIsoBtn.isChecked()) {
                    atrMode = MsrManager.AtrOnMode.ISO ;         //ISO Mode
                } else {
                    atrMode = MsrManager.AtrOnMode.EMV;         //EMV Mode
                }
                if (msrManager.AtrOn(atrMode)) {
                    msrManager.SendAPDU(new byte[]{0x00 ,(byte)0xA4 ,0x04 ,0x00 ,0x0C ,(byte)0xD2 ,0x76 ,0x00 ,0x01 ,0x35 ,0x4B ,0x41 ,0x53 ,0x4D ,0x30 ,0x31 ,0x00 ,0x00});
                    ResultStatusView.setHint(getString(R.string.atr_on_success_str));
                    isConnected = true;
                } else {
                    ResultStatusView.setHint(getString(R.string.atr_on_fail_str));
                    isConnected = false;
                }
                Log.i(TAG, "ATR On----");
            } else if (id == R.id.APDUBtn) {
                Log.i(TAG, "Send APDU++++");
                if(msrManager.GetCardInserted() == MsrManager.CardInsertState.ICC_DETECT){
                    isInserted = true;
                }else{
                    isInserted = false;
                }
                ChangeUI();

                if (isConnected) {
                    byte[] TxBuffer;
                    byte[] RxBuffer;

                    if (ApduInput.length() != 0) {
                        Log.i(TAG, "Send customer command");
                        byte[] Temp = new byte[260];
                        TxBuffer = StringToHexData(ApduInput.getText().toString(), Temp);

                    } else {
                        Log.i(TAG, "Send defualt command");
                        TxBuffer = new byte[]{0x00, (byte) 0xa4, 0x04, 0x00, 0x07, (byte) 0xa0, 0x00, 0x00, 0x00, 0x04, 0x10, 0x10, 0x00};
                    }
                    RxBuffer = msrManager.SendAPDU(TxBuffer);
                    if (RxBuffer != null) {
                        StringBuilder str = new StringBuilder();

                        for (int i = 0; i < RxBuffer.length; i++)
                            str.append(String.format("%02x ", RxBuffer[i] & 0xff));

                        ResultStatusView.setHint(str.toString());
                    }else{
                        ResultStatusView.setHint(getString(R.string.null_str));
                    }
                } else {
                    ResultStatusView.setHint(getString(R.string.apdu_error_str));
                }

                Log.i(TAG, "Send APDU----");
            } else if (id == R.id.PowerDownBtn) {
                Log.i(TAG, "Power Down++++");
                msrManager.DeviceMsrStopRead();
                msrManager.PowerDown();
                ResultStatusView.setHint(getString(R.string.atr_power_down));
                isConnected = false;
                Log.i(TAG, "Power Down----");
            } else if (id == R.id.EMVBtn) {
                Log.i(TAG, "EMVBtn++++");
                count = 0;
                msrManager.paymentTransactionStart((byte)0x03,1004, (short)0,(short) 0,(short)0,(short)0,(short)0,(short)0,(short)0,(short)0);
                ResultStatusView.setHint(getString(R.string.wait_str));
                Log.i(TAG, "EMVBtn----");
            }
        }
    };

    private void ChangeUI() {
        Log.i(TAG, "ChangeUI++++");
        if (isInserted == true) {
            Log.i(TAG, "Card inserted");
            CardStatusView.setHint(getString(R.string.card_inserted));
        } else {
            Log.i(TAG, "Card not inserted");
            CardStatusView.setHint(getString(R.string.card_not_inserted));
        }
        Log.i(TAG, "ChangeUI----");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_info) {
            openInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openInfo() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        String version = getString(R.string.msg_version_suffix);
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (pi != null) {
                version = pi.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        alert.setMessage(getString(R.string.app_name) + " v" + version);
        alert.show();
    }

    private byte HexData(char data) {
        byte ret = (byte) 0xff;
        if (0x30 <= data && 0x39 >= data) {
            ret = (byte) (data - 0x30);
        } else if (0x41 <= data && 0x46 >= data) {
            ret = (byte) (data - 0x41 + 10);
        } else if (0x61 <= data && 0x66 >= data) {
            ret = (byte) (data - 0x61 + 10);
        }
        return ret;
    }

    private byte[] StringToHexData(String data, byte[] writeData) {
        int length = 0, charlength = 0;
        int strlength = data.length();
        char temp;
        byte temp2;
        for (int i = 0; i < strlength; i++) {
            temp = data.charAt(i);
            if (temp == ' ') {
                if (charlength != 0)
                    length++;
                charlength = 0;
                continue;
            } else if (charlength >= 2) {
                charlength = 0;
                length++;
            }

            temp2 = HexData(temp);
            if (temp2 != 0xff) {
                writeData[length] = (byte) ((writeData[length] * (charlength * 0x10)) + (temp2 & 0xF));
                charlength++;
            }
        }
        if (charlength != 0)
            length++;

        byte[] resultBytes = new byte[length];
        for (int i = 0; i < resultBytes.length; i++) {
            resultBytes[i] = writeData[i];
        }

        return resultBytes;
    }

}