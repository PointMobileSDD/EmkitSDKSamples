package device.sdk.sample.button;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.text.Selection;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import device.common.HiJackData;
import device.common.HijackingKeys;
import device.sdk.KeyManager;


/** Simple Guide (SDK V.2.9.4+)
 *
 *************************** Setting side *******************************
 *
 * import device.common.HiJackData;
 * import device.sdk.KeyManager;
 *
 * int count = 0;                                                         // Number of items changed
 * boolean bBroadcastIntent = false;
 * boolean bCustomIntent = false;
 * boolean bNoAction = false;
 * boolean bSetDefault = false;
 * KeyManager keyManager = new KeyManager();							  // Create a key manager to use key mapping
 *
 * try {
 *      HiJackData[] dataList = keyManager.getAllHiJackData();            // Getting the all items which to be able to change
 *	    for (HiJackData data : dataList) {
 *		    switch (data.getDefaultKeyCode()) {							  // Find the key entry to be changed
 *              case 1010: // KEYCODE_SCANNER_F
 *              case 1011: // KEYCODE_SCANNER_R
 *              case 1012: // KEYCODE_SCANNER_L
 *              case 1013: // KEYCODE_SCANNER_B
 *				    data.setFlag(HiJackData.FLAG_UPDATE);				  // Set the update flag
 *				    if (bBroadcastIntent) {								  // Use the broadcast intent function
 * 					    data.setConvertKeyCode(0);
 * 					    data.setConvertSymbol("KEYCODE_UNKNOWN");
 *                      data.setActivityNameOfExecuteApp("");
 *                      data.setPackageNameOfExecuteApp("");
 *                      data.setCustomBroadcastIntent("");
 *                      data.setBroadcastKey(1);						  // Set the value (1) and set the other value to 0 or blank.
 *                  }
 *				    if (bCustomIntent) {								  // Use the custom intent function
 * 					    data.setConvertKeyCode(0);
 * 					    data.setConvertSymbol("KEYCODE_UNKNOWN");
 *					    data.setActivityNameOfExecuteApp("");
 *                      data.setPackageNameOfExecuteApp("");
 *                      data.setCustomBroadcastIntent("intent.string.what.you.want"); // Set the intent string value what you want and set the other value to 0 or blank.
 *                      data.setBroadcastKey(0);
 *				    }
 *                  if (bNoAction) {								      // Set to make no action
 * 					    data.setConvertKeyCode(0);						  // Set the value (0) and set the other value to 0 or blank.
 * 					    data.setConvertSymbol("KEYCODE_UNKNOWN");		  // Set the KEYCODE_UNKNOWN string value
 *					    data.setActivityNameOfExecuteApp("");
 *                      data.setPackageNameOfExecuteApp("");
 *                      data.setCustomBroadcastIntent("");
 *                      data.setBroadcastKey(0);
 *                  }
 *				    if (bSetDefault) {								      // Set to make default
 * 					    data.setConvertKeyCode(data.getDefineKeyCode());  // Set the primary key code value using the getDefineKeyCode() function
 * 					    data.setConvertSymbol(data.getDefineSymbol());	  // Set the primary symbol value using the getDefineSymbol() function
 *					    data.setActivityNameOfExecuteApp("");
 *                      data.setPackageNameOfExecuteApp("");
 *                      data.setCustomBroadcastIntent("");
 *                      data.setBroadcastKey(0);
 *                  }
 * 				    break;
 *          }
 * 	    }
 * 	    count = getKeyManager().setAllHiJackData(dataList);				  // Apply changed items
 * } catch (RemoteException e) {										  // Catch the RemoteException for the remote call
 * 	    e.printStackTrace();
 * } finally {
 *      if (count > 0) {
 * 		    Toast.makeText(this, "Complete to save", Toast.LENGTH_SHORT).show();
 * 	    } else {
 * 		    Toast.makeText(this, "toast_no_change", Toast.LENGTH_SHORT).show();
 * 	    }
 * }
 *
 **************************** Receiver side *******************************
 *
 * // public static final String INTENT_BROADCAST_INTENT_KEY = "device.common.BROADCAST_KEYEVENT"; // A fixed intent value that is passed when using BroadcastIntent. HiJackData.INTENT_BROADCAST_KEY
 * // public static final String EXTRA_BROADCAST_INTENT_KEY = "EXTRA_KEYEVENT";                    // HiJackData.EXTRA_BROADCAST_KEYEVENT
 * // public static final String EXTRA_CUSTOM_INTENT_KEY = "EXTRA_ISKEYDOWN";	                   // HiJackData.EXTRA_CUSTOM_BROADCAST_ISKEYDOWN
 *
 * @Override
 * public void onReceive(Context context, Intent intent) { // If you use the BroadcastIntent feature and the CustomIntent feature, you can receive intent with BroadcastReceiver.
 *      if (HiJackData.INTENT_BROADCAST_KEY.equals(intent.getAction())) {
 *		    KeyEvent event = (KeyEvent) intent.getParcelableExtra(HiJackData.EXTRA_BROADCAST_KEYEVENT);	// The KeyEvent Object is passed to the Extra value.
 *          if (event.getAction() == KeyEvent.ACTION_DOWN) {
 *			    // DOWN ACTION
 *		    } else {
 *			    // UP ACTION
 *		    }
 *      } else if ("intent.string.what.you.want".equals(intent.getAction())) { // The custom intent string defined in the code above is passed. "intent.string.what.you.want"
 *		    boolean isKeyDown = intent.getBooleanExtra(HiJackData.EXTRA_CUSTOM_BROADCAST_ISKEYDOWN, false);	// The up-down state can be known.
 *		    if (isKeyDown) {
 *			    // DOWN ACTION
 *		    } else {
 *			    // UP ACTION
 *		    }
 *	    }
 * }
 *
 */

public class ButtonSampleActivity extends Activity {
    private static final String TAG = ButtonSampleActivity.class.getSimpleName();

    private static final String KEYCODE_HOME_SYMBOL = "KEYCODE_HOME";
    private static final int KEYCODE_HOME_CODE = 3;

    private HiJackData[] mHiJackDataList;
    private KeyManager mKeyManager;

    private TextView mPropertyLabel;
    private TextView mPropertySymbol;
    private TextView mDefineCurrent;
    private TextView mDefinePath;
    private Spinner mKeypadMode;

    private Button mSaveProperty;
    private Button mDefaultProperty;
    private Button mSaveDefine;
    private Button mDefaultDefine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_sample);

        init();
        button();

        updatePropertyUI();
        updateDefineUI();
        dumpHiJackData();
        dumpHijackingKeys();
    }

    private void button() {
        mSaveProperty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHiJackDataList == null) return;
                mHiJackDataList[0].setFlag(HiJackData.FLAG_UPDATE);
                mHiJackDataList[0].setConvertKeyCode(KEYCODE_HOME_CODE);
                mHiJackDataList[0].setConvertSymbol(KEYCODE_HOME_SYMBOL);
                changeProperty();
            }
        });

        mDefaultProperty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHiJackDataList == null) return;
                mHiJackDataList[0].setFlag(HiJackData.FLAG_UPDATE);
                mHiJackDataList[0].setConvertKeyCode(mHiJackDataList[0].getDefineKeyCode());
                mHiJackDataList[0].setConvertSymbol(mHiJackDataList[0].getDefineSymbol());
                changeProperty();
            }
        });

        mSaveDefine.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDefinePath == null) return;
                changeDefine();
            }
        });

        mDefaultDefine.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultDefine();
            }
        });

		mKeypadMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setKeypadMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void init() {
        mKeyManager = KeyManager.getInstance();

        mDefaultProperty = (Button) findViewById(R.id.default_properties);
        mSaveProperty = (Button) findViewById(R.id.save_properties);
        mSaveDefine = (Button) findViewById(R.id.save_define);
        mDefaultDefine = (Button) findViewById(R.id.default_define);

        mPropertyLabel = (TextView) findViewById(R.id.label_properies);
        mPropertySymbol = (TextView) findViewById(R.id.symbol_properies);
        mDefineCurrent = (TextView) findViewById(R.id.current_define);
        mDefinePath = (TextView) findViewById(R.id.path_define);

		mKeypadMode = (Spinner) findViewById(R.id.keypad_mode);
        mKeypadMode.setSelection(getCurrentMode());
    }

	    private void updatePropertyUI() {
        if (mHiJackDataList == null) {
            try {
                mHiJackDataList = mKeyManager.getAllHiJackData();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if (mPropertyLabel != null)
            mPropertyLabel.setText(mHiJackDataList[0].getLabel());
        if (mPropertySymbol != null)
            mPropertySymbol.setText(mHiJackDataList[0].getConvertSymbol());

        dumpHiJackData();
        dumpHijackingKeys();
    }

    private void changeProperty() {
        int count = 0;
        try {
            count = mKeyManager.setAllHiJackData(mHiJackDataList);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            if (count > 0) {
                Toast.makeText(this, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show();
                updatePropertyUI();
            } else {
                Toast.makeText(this, getString(R.string.toast_no_change), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateDefineUI() {
        String currentKCMapFile = "";
        try {
            currentKCMapFile = mKeyManager.getCurrentKCMapFile();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (mDefineCurrent != null)
            mDefineCurrent.setText(currentKCMapFile);
        if (mDefinePath != null) {
            mDefinePath.setText(Environment.getExternalStorageDirectory() + "/");
            Selection.setSelection(mDefinePath.getEditableText(), mDefinePath.length());
        }
    }

    private void changeDefine() {
        int result = 0;
        try {
            result = mKeyManager.changeKCMapFile(mDefinePath.getText().toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            if (result == 0) {
                Toast.makeText(this, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show();
                updateDefineUI();
            } else {
                String failMsg = getString(R.string.toast_save_fail) + "(" + result + ")";
                Toast.makeText(this, failMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void defaultDefine() {
        boolean result = true;
        try {
            result = mKeyManager.removeKCMapFile();
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            if (result) {
                Toast.makeText(this, getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show();
                updateDefineUI();
            } else {
                Toast.makeText(this, getString(R.string.toast_save_fail), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getCurrentMode() {
        try {
            return mKeyManager.getKeypadMode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return KeyManager.KEYPAD_MODE_NORMAL;
    }

    private boolean setKeypadMode(int mode) {
        try {
            return mKeyManager.setKeypadMode(mode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void dumpHiJackData() {
        Log.e(TAG, "These are the stored data to the database... Start dump >>>");
        HiJackData[] hiJackDataList = null;
        try {
            hiJackDataList = mKeyManager.getAllHiJackData();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (hiJackDataList != null) {
            for (HiJackData data : hiJackDataList) {
                Log.e(TAG, "ID(" + data.getID()
                        + "), Label(" + data.getLabel()
                        + "), DefaultKey(" + data.getDefaultSymbol() + "-" + data.getDefineKeyCode()
                        + "), ConvertKey(" + data.getConvertSymbol() + "-" + data.getConvertKeyCode() + ")");
            }
        }
        Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End dump <<<");
    }

    private void dumpHijackingKeys() {
        Log.e(TAG, "These are the defined key list... Start dump >>>");
        HijackingKeys[] hijackingKeys = null;
        try {
            hijackingKeys = mKeyManager.getHijackingKeys();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (hijackingKeys != null) {
            for (HijackingKeys data : hijackingKeys) {
                Log.e(TAG, "Label(" + data.getLabel()
                        + "), DefaultKey(" + data.getDefaultSymbol() + "-" + data.getDefineKeyCode() + ")");
            }
        }
        Log.e(TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End dump <<<");
    }
}
