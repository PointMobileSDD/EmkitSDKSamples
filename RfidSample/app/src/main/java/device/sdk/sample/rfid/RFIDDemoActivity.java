package device.sdk.sample.rfid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import device.common.rfid.AccessTag;
import device.common.rfid.ModeOfInvent;
import device.common.rfid.RFIDConst;
import device.common.rfid.RecvPacket;
import device.common.rfid.ReportFormatOfInvent_ext;
import device.common.rfid.SelConfig;
import device.sdk.RFIDManager;
import device.sdk.sample.rfid.adapter.RfidRvAdapter;
import device.sdk.sample.rfid.adapter.item.RfidListItem;
import device.sdk.sample.rfid.custom.DialogWriteTag;
import device.sdk.sample.rfid.custom.OperationResult;
import device.sdk.sample.rfid.util.PreferenceUtil;
import device.sdk.sample.rfid.util.Utils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RFIDDemoActivity extends BaseActivity {
    public static final String D = "Debug";
    private final String TAG = getClass().getSimpleName();
    private static int REQUEST_BLUETOOTH = 0x1004;
    private static int REQUEST_WRITE_TAG = 0x1005;
    private final int HANDLER_MSG_SAVE_LOG = 0x1001;

    private final int RFID_MODE = 0;
    private final int SCAN_MODE = 1;

    private final int RESERVED = 0;
    private final int EPC = 1;
    private final int TID = 2;
    private final int USER = 3;

    private final int USER_BIT = 0x200;
    private final int TID_BIT = 0x80;
    private final int EPC_BIT = 0x20;
    private final int ACS_PWD_BIT = 0x08;
    private final int KILL_PWD_BIT = 0x02;

    private final String RFID_CONTROL_FOLDER = "RFIDControl";
    private final String SAVED_LOG_FOLDER = "RFIDLogs";
    private final String SAVED_LOG_FILE = "RFIDLogFile";
    private final String SAVED_CSV_FILE = "RFID";
    private final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HHmmss_SSS", Locale.getDefault());

    private RFIDManager mRfidMgr;
    private BluetoothAdapter mBluetoothAdapter;
    private ModeOfInvent mOperationMode = new ModeOfInvent();

    private RecyclerView mRecycleViewRfid;
    private RfidRvAdapter mRvAdapter;

    private ArrayList<RfidListItem> mItems;
    private HashMap<String, Integer> mHashItems;

    private int mReadCount = 0;
    private TextView mTextReadCount;
    private int mTotalCount = 0;
    private TextView mTextTotalCount;

    private boolean mAutoScanStart = false;
    private boolean mIsReadStart = false;
    private int mAutoScanInterval = 0;

    private boolean mIsSaveLog = false;
    private File mFile;
    private long mLogStartTime = -1;

    private SimpleDateFormat mSimpleDataFormat;
    private boolean mIsTemp = false;
    private String mTempValue = "0";
    private int mVolumeValue;

    private ProgressDialog mProgress = null;

    private TextView mTextTemp;
    private LinearLayout mLinearTemp;
    private LinearLayout mLinearCheckbox;

    private Utils mUtil;
    private PreferenceUtil mPrefUtil;

    private LinearLayout mLinearOperationResult;
    private TextView mTextOperationResult;
    private CheckBox mCheckboxContinuous;
    private CheckBox mCheckboxAutoScan;
    private CheckBox mCheckboxSaveLog;
    private CheckBox mCheckboxBeepSound;
    private Button mBtnStartInventory;
    private Button mBtnWriteTag;
    private Button mBtnReadTag;
    private Button mBtnLockTag;
    private Button mBtnUnlockTag;

    private boolean isPause = false;
    private Toast mToast;

    private Menu mOptionMenu;
    private boolean mIsAsc = true;

    private LogSaveHandler mHandler = new LogSaveHandler(this);

    private OperationResult mOperationResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        mUtil = new Utils(this);
        mPrefUtil = new PreferenceUtil(getApplicationContext());
        mProgress = new ProgressDialog(RFIDDemoActivity.this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_rfid_demo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRfidMgr = RFIDManager.getInstance();

        mLinearOperationResult = findViewById(R.id.linear_operation_result);
        mTextOperationResult = findViewById(R.id.tv_operation_result);
        mTextOperationResult.setOnClickListener(mOnClickListener);
        mCheckboxContinuous = findViewById(R.id.checkbox_continuous);
        mCheckboxAutoScan = findViewById(R.id.checkbox_auto_scan);
        mCheckboxSaveLog = findViewById(R.id.checkbox_save_log);
        mCheckboxBeepSound = findViewById(R.id.checkbox_beep_sound);
        mCheckboxContinuous.setOnClickListener(mOnCheckboxListener);
        mCheckboxAutoScan.setOnClickListener(mOnCheckboxListener);
        mCheckboxSaveLog.setOnClickListener(mOnCheckboxListener);
        mCheckboxBeepSound.setOnClickListener(mOnCheckboxListener);
        mBtnStartInventory = findViewById(R.id.btn_start_inventory);
        mBtnWriteTag = findViewById(R.id.btn_write_tag);
        mBtnReadTag = findViewById(R.id.btn_read_tag);
        mBtnStartInventory.setOnClickListener(mOnClickListener);
        mBtnWriteTag.setOnClickListener(mOnClickListener);
        mBtnReadTag.setOnClickListener(mOnClickListener);
        mLinearCheckbox = findViewById(R.id.linear_checkbox);
        mBtnLockTag = findViewById(R.id.btn_lock_tag);
        mBtnLockTag.setOnClickListener(mOnClickListener);
        mBtnUnlockTag = findViewById(R.id.btn_unlock_tag);
        mBtnUnlockTag.setOnClickListener(mOnClickListener);

        mItems = new ArrayList<>();

        mRecycleViewRfid = (RecyclerView) findViewById(R.id.recy_tag_item);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecycleViewRfid.setLayoutManager(mLinearLayoutManager);
        mRvAdapter = new RfidRvAdapter(mItems);
        mRecycleViewRfid.setAdapter(mRvAdapter);

        mHashItems = new HashMap<>();

        mReadCount = 0;
        mTextReadCount = (TextView) findViewById(R.id.text_total_read_count);
        mTotalCount = 0;
        mTextTotalCount = (TextView) findViewById(R.id.text_total_count);

        mSimpleDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        mTextTemp = (TextView) findViewById(R.id.text_temperature);
        mLinearTemp = (LinearLayout) findViewById(R.id.linear_temp);

        mOperationResult = new OperationResult();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Check EXTERNAL_STORAGE_PERMISSION
        if(!checkPermission())
            requestPermission();

        bluetoothOn();
    }

    private void checkTempOption() {
        mIsTemp = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_UPDATE_TEMP, false);
        ReportFormatOfInvent_ext reportFormatOfInvent_ext = new ReportFormatOfInvent_ext();
        if(mIsTemp) {
            Log.d(TAG, "Temp option on");
            reportFormatOfInvent_ext.temp = 1;
            mLinearTemp.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "Temp option off");
            reportFormatOfInvent_ext.temp = 0;
            mLinearTemp.setVisibility(View.GONE);
        }
        int result = mRfidMgr.SetInventoryReportFormat_ext(reportFormatOfInvent_ext);
        if(result == RFIDConst.CommandErr.COMM_ERR) {
            mTextTemp.setText(getString(R.string.temp_fail));
        }
    }

    private void bluetoothOn() {
        if(!mRfidMgr.IsOpened())
            rfidConnectDialog();

        if(mPrefUtil.getStringPreference(PreferenceUtil.KEY_OPEN_OPTION, mUtil.getDefaultOption())
                .equalsIgnoreCase(RFIDControlActivity.OpenOption.BLUETOOTH.toString())) {
            if(!mRfidMgr.IsOpened()) {
                mRfidMgr.DisconnectBTDevice();
                Log.d(TAG, "disconnect 11");
            }

            if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
            }
        }
    }

    private void initState() {
        //Operation Mode Value
        mRfidMgr.GetOperationMode(mOperationMode);
        if(mOperationMode.single == RFIDConst.RFIDConfig.INVENTORY_MODE_CONTINUOUS) {
            mCheckboxContinuous.setChecked(true);
            mCheckboxAutoScan.setChecked(false);
            mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SCAN_AUTO_ENABLE, false);
        } else {
            mCheckboxContinuous.setChecked(false);
        }
        Log.e("checkchange_init", mOperationMode.single == RFIDConst.RFIDConfig.INVENTORY_MODE_SINGLE ?
                "single" : "continuous");

        //Auto Scan Value
        boolean isAutoScan = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_SCAN_AUTO_ENABLE);
        mCheckboxAutoScan.setChecked(isAutoScan);
        if(isAutoScan) {
            mCheckboxContinuous.setChecked(false);
            mOperationMode.single = RFIDConst.RFIDConfig.INVENTORY_MODE_SINGLE;
            mRfidMgr.SetOperationMode(mOperationMode);
        }

        //Log Save Value
        boolean isSaveLOG = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_SAVE_LOG);

        if(isSaveLOG) {
            mCheckboxSaveLog.setChecked(true);
        } else {
            mCheckboxSaveLog.setChecked(false);
        }

        //Volume Value
        // Log.e("initState", "mVolume === " + mVolumeValue);
        if(mVolumeValue == 1 || mVolumeValue == 2) {
            mPrefUtil.putIntPreference(PreferenceUtil.KEY_BEEP_SOUND, mVolumeValue);
            mCheckboxBeepSound.setChecked(true);
        } else if(mVolumeValue == 0) {
            mCheckboxBeepSound.setChecked(false);
        }
    }

    private boolean checkPermission() {
        int permissionResult = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionResult == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.finishAffinity(this);
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(mAutoScanStart || mIsReadStart) {
            mAutoScanStart = false;
            mIsReadStart = false;

            mRfidMgr.Stop();
            //stopScan();
            stopScanCompletable();
        }

        if(item.getItemId() == R.id.action_sort) {
            if(mIsAsc)
                mIsAsc = false;
            else
                mIsAsc = true;

            mItems.clear();
            Iterator it = sortByValue(mHashItems, mIsAsc).iterator();
            while(it.hasNext()) {
                String key = (String) it.next();
                int value = mHashItems.get(key);
                RfidListItem listItem = new RfidListItem(key, value);
                mItems.add(listItem);
            }

            mRvAdapter.notifyDataSetChanged();
            //readTagDataTest();
        } else if(item.getItemId() == R.id.action_delete) {
            mHashItems.clear();
            mItems.clear();
            mReadCount = 0;
            mTotalCount = 0;
            mTextReadCount.setText("0");
            mTextTotalCount.setText("0");

            mRvAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
        getRFIDApplication().setNotifyDataCallback(mDataCallbacks);
        Log.d(TAG, "onResume()");
        //checkIsOpened(this, mRfidMgr);

        if(mRfidMgr.IsOpened()) {
            //Change ResultType to Callback
            mRfidMgr.SetResultType(RFIDConst.ResultType.RFID_RESULT_CALLBACK);
            mUtil.showProgress(mProgress, RFIDDemoActivity.this, true);
            AsyncGetVolume asyncGetVolume = new AsyncGetVolume();
            asyncGetVolume.execute();
        }
        //checkTempOption();
        mRfidMgr.SetTriggerInventory(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        isPause = true;

        /* To roll-back previous Result Type */
        if(mRfidMgr.IsOpened()) {
            int currentType = mPrefUtil.getIntPreference(PreferenceUtil.KEY_RESULT_TYPE,
                    RFIDConst.ResultType.RFID_RESULT_CALLBACK);
            if(currentType != RFIDConst.ResultType.RFID_RESULT_CALLBACK) {
                mRfidMgr.SetResultType(currentType);
            }
        }

        if(mIsReadStart || mAutoScanStart) {
            mIsReadStart = false;
            mAutoScanStart = false;
            stopScanCompletable();
            mRfidMgr.Stop();
            Log.d(TAG, "stop2");
        }

        if(mToast != null) {
            Log.d(TAG, "mToast cancel");
            mToast.cancel();
        }
    }

    @SuppressLint("SetTextI18n")
    private void startWriteTag(String writeData, int memBank, int offset, String acsPwd) {
        changeVisibilityOfButtons(mBtnWriteTag, true);
        Single.create(subscriber -> {
            Log.d(TAG, "startWriteTag()+++");

            changeToSingleMode();
            mUtil.sleep(1000);

            OperationResult result = writeTag(writeData, memBank, offset, acsPwd);
            mUtil.sleep(1000);

            rollbackMode();
            mUtil.sleep(1000);

            subscriber.onSuccess(result);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(result -> {
                    mUtil.showProgress(mProgress, RFIDDemoActivity.this, false);
                    OperationResult operationResult = (OperationResult) result;
                    changeVisibilityOfButtons(mBtnWriteTag, false);
                    changeBackgroundColor(operationResult.isSuccess());
                    mTextOperationResult.setText((operationResult.isSuccess() ? getString(R.string.write_success) : getString(R.string.write_fail))
                            + "\ntagId : " + operationResult.tagId + ", errOp : " + operationResult.errOp + ", errTag : " + operationResult.errTag);
                    if(operationResult.isSuccess()) {
                        Log.d(TAG, "startWriteTag()--- success");
                    } else {
                        Log.d(TAG, "startWriteTag()--- command err");
                    }
                })
                .doOnError(throwable -> {
                    mUtil.showProgress(mProgress, RFIDDemoActivity.this, false);
                    changeVisibilityOfButtons(mBtnWriteTag, false);
                    throwable.printStackTrace();
                })
                .subscribe();
    }

    @SuppressLint("SetTextI18n")
    private void startReadTag(int length, int memBank, int offset, String acsPwd) {
        changeVisibilityOfButtons(mBtnReadTag, true);
        Single.create(subscriber -> {
            Log.d(TAG, "starReadTag()+++");

            changeToSingleMode();
            mUtil.sleep(1000);

            OperationResult result = readTag(length, memBank, offset, acsPwd);
            mUtil.sleep(1000);

            rollbackMode();
            subscriber.onSuccess(result);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(result -> {
                    OperationResult operationResult = (OperationResult) result;
                    changeVisibilityOfButtons(mBtnReadTag, false);
                    changeBackgroundColor(operationResult.isSuccess());
                    mTextOperationResult.setText((operationResult.isSuccess() ? getString(R.string.read_success) : getString(R.string.read_fail))
                            + "\ntagId : " + operationResult.tagId + ", errOp : " + operationResult.errOp + ", errTag : " + operationResult.errTag);
                    if(operationResult.isSuccess())
                        Log.d(TAG, "startReadTag()--- success");
                    else
                        Log.d(TAG, "startReadTag()--- command err");
                })
                .doOnError(throwable -> {
                    changeVisibilityOfButtons(mBtnReadTag, false);
                    throwable.printStackTrace();
                })
                .subscribe();
    }

    @SuppressLint("SetTextI18n")
    private void startLockTag(boolean isLock) {
        if(isLock) {
            changeVisibilityOfButtons(mBtnLockTag, true);
        } else {
            changeVisibilityOfButtons(mBtnUnlockTag, true);
        }

        Single.create(subscriber -> {
            Log.d(TAG, "starLockTag()+++");
            changeToSingleMode();
            if(isLock) {
                // Must set different access password from write/read access password
                writeTag("0000000012345678", RESERVED, 0, "0");
            }

            OperationResult result = lockTag(isLock);

            rollbackMode();
            subscriber.onSuccess(result);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(result -> {
                    OperationResult operationResult = (OperationResult) result;
                    if(isLock) {
                        changeVisibilityOfButtons(mBtnLockTag, false);
                        changeBackgroundColor(operationResult.isSuccess());
                        mTextOperationResult.setText((operationResult.isSuccess() ? getString(R.string.lock_success) : getString(R.string.lock_fail))
                                + "\ntagId : " + operationResult.tagId + ", errOp : " + operationResult.errOp + ", errTag : " + operationResult.errTag);
                    } else {
                        changeVisibilityOfButtons(mBtnUnlockTag, false);
                        changeBackgroundColor(operationResult.isSuccess());
                        mTextOperationResult.setText((operationResult.isSuccess() ? getString(R.string.unlock_success) : getString(R.string.unlock_fail))
                                + "\ntagId : " + operationResult.tagId + ", errOp : " + operationResult.errOp + ", errTag : " + operationResult.errTag);
                    }
                    if(operationResult.isSuccess())
                        Log.d(TAG, "startLockTag()--- success");
                    else
                        Log.d(TAG, "startLockTag()--- command err");
                })
                .doOnError(throwable -> {
                    if(isLock) {
                        changeVisibilityOfButtons(mBtnLockTag, false);
                    } else {
                        changeVisibilityOfButtons(mBtnUnlockTag, false);
                    }
                    throwable.printStackTrace();
                })
                .subscribe();
    }

    private void changeVisibilityOfButtons(Button selectedButton, boolean isTaskRunning) {
        Button[] buttons = {mBtnStartInventory, mBtnWriteTag, mBtnReadTag, mBtnLockTag, mBtnUnlockTag};

        for(Button button : buttons) {
            if(button.getId() != selectedButton.getId()) {
                if(isTaskRunning)
                    button.setVisibility(View.INVISIBLE);
                else
                    button.setVisibility(View.VISIBLE);
            }
        }
    }

    private void changeBackgroundColor(boolean isSuccess) {
        mLinearOperationResult.setBackgroundColor(isSuccess ? Color.BLUE : Color.RED);
    }

    /* 한번만 tag write 하기 위해 모드를 싱글로 변경 및 원하는 tag만 write하기 위하여 select inclusion 설정
         RFIDConst.RFIDConfig.INVENTORY_MODE_SINGLE : mode를 single로 변경
         RFIDConst.RFIDConfig.INVENTORY_SELECT_INCLUSION : Select로 설정된 tag pattern을 가진 tag만 read/write 하도록 설정
     */
    private boolean changeToSingleMode() {
        Log.d(TAG, "changeToSingleMode()+++");
        ModeOfInvent modeOfInvent = new ModeOfInvent();
        modeOfInvent.single = RFIDConst.RFIDConfig.INVENTORY_MODE_SINGLE;
        //modeOfInvent.select = RFIDConst.RFIDConfig.INVENTORY_SELECT_INCLUSION;
        int result = mRfidMgr.SetOperationMode(modeOfInvent);
        Log.d(TAG, "changeToSingleMode()--- result : " + result);
        return result == RFIDConst.CommandErr.SUCCESS;
    }

    /* 기존에 설정되어있던 모드로 되돌림 */
    private boolean rollbackMode() {
        Log.d(TAG, "rollbackMode()+++");
        boolean isContinuous = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_SCAN_CONTINUOUS_ENABLE, true);
        Log.d(TAG, isContinuous ? "isContinuous true" : "isContinuous false");
        ModeOfInvent modeOfInvent = new ModeOfInvent();
        if(!isContinuous)
            modeOfInvent.single = RFIDConst.RFIDConfig.INVENTORY_MODE_SINGLE;
        else
            modeOfInvent.single = RFIDConst.RFIDConfig.INVENTORY_MODE_CONTINUOUS;
        modeOfInvent.select = mPrefUtil.getIntPreference(PreferenceUtil.KEY_SELECT, RFIDConst.RFIDConfig.INVENTORY_SELECT_NONE);
        int result = mRfidMgr.SetOperationMode(modeOfInvent);
        Log.d(TAG, "rollbackMode()--- result : " + result);
        return result == RFIDConst.CommandErr.SUCCESS;
    }

    /* write를 원하는 tag의 filter 설정 */
    private boolean setFilter(SelConfig selConfig) {
        Log.d(TAG, "setFilter+++");
        int result = mRfidMgr.SetSelMask(selConfig);
        Log.d(TAG, "setFilter--- result : " + result);
        return result == RFIDConst.CommandErr.SUCCESS;
    }

    /*변경하고자하는 tag의 현재 데이터만 write/read 되도록 설정 */
    private SelConfig getSelect(String tagPattern) {
        SelConfig selConfig = new SelConfig();
        selConfig.index = 3;
        selConfig.memBank = 3;
        selConfig.action = 1;
        selConfig.target = 4;
        selConfig.selectData = tagPattern;
        selConfig.length = selConfig.selectData.length() * 4;
        selConfig.offset = 8 * 4;

        return selConfig;
    }

    /* 설정했던 tag filter를 제거함. 제거해주지 않으면 tag read시 filter에 해당하는 tag만 읽어짐 */
    private SelConfig getDefaultSelect() {
        SelConfig selConfig = new SelConfig();
        selConfig.index = 3;
        selConfig.memBank = 3;
        selConfig.action = 1;
        selConfig.target = 4;
        selConfig.selectData = "";
        selConfig.length = selConfig.selectData.length();
        selConfig.offset = 0;

        return selConfig;
    }

    /* Tag 데이터를 write */
    private OperationResult writeTag(String tagIdToWrite, int memBank, int offset, String acsPwd) {
        Log.d(TAG, "writeTag()+++");
        AccessTag accessTag = new AccessTag();
        accessTag.wTagData = tagIdToWrite;
        accessTag.length = accessTag.wTagData.length() / 4; //unit : word
        if((accessTag.wTagData.length() % 4) != 0)
            accessTag.length += 1;
        accessTag.memBank = memBank;

        /* Set to write from the EPC part except for PC and CRC
         *  EPC memory bank structure : PC + CRC + EPC ( PC,CRC size : 2bytes each )
         *  Unit : word ( 2bytes per word )
         * */
        accessTag.offset = offset;

        accessTag.acsPwd = acsPwd;

        int result = mRfidMgr.WriteTag(accessTag);

        if(accessTag.errOp != RFIDConst.CommandErr.SUCCESS || accessTag.errTag != RFIDConst.CommandErr.SUCCESS) {
            result = RFIDConst.CommandErr.COMM_ERR;
        }
        //errTag, errOp 값 확인
        Log.d(TAG, "accessTag.errOp : " + accessTag.errOp + " accessTag.errTag : " + accessTag.errTag);
        Log.d(TAG, "writeTag()--- : " + result);

        return setOperationResult(result, accessTag);
    }

    /* Tag 데이터를 read
     * length
     *  unit : word (word = 2bytes)
     *  memBank
     *  0 : RESERVED bank
     *  1 : EPC bank
     *  2 : TID bank
     *  3 : USER bank
     *
     *  offset
     *  unit : word (word = 2bytes)
     *
     *  acsPwd
     *  default : "0"
     * */
    private OperationResult readTag(int length, int memBank, int offset, String acsPwd) {
        Log.d(TAG, "readTag()+++");
        changeToSingleMode();
        AccessTag accessTag = new AccessTag();
        accessTag.length = length;
        accessTag.memBank = memBank;
        accessTag.offset = offset;
        accessTag.acsPwd = acsPwd;
        int result = mRfidMgr.ReadTag(accessTag);

        Log.d(TAG, "readTag() accessTag.tagId : " + accessTag.tagId + " accessTag.errTag : " + accessTag.errTag);
        return setOperationResult(result, accessTag);
    }

    /* RFIDManager LockTag api 설명
        lockTag : tag 의 memory bank 를 lock. 해당 memory bank 를 lock 하는 경우 tag 를 read 할 수 없음

        LockMask field
        |   9   |   8   |   7   |   6   |   5   |   4   |   3   |   2   |   1    |   0   |
        | user  |  N/A  |  tid  |  N/A  |  epc  |  N/A  |acs pwd|  N/A  |kill pwd|  N/A  |

        @param lockMask : 설정하고 싶은 memory bank 의 bit 를 모두 더함
        @param lockEnable : lockMask 에 추가한 memory bank 의 lock, unlock 여부. lock 하고 싶은 경우 bit 를 더하고 unlock 하고 싶은 경우 bit 를 더하지 않으면 됨
        (lockMask 에 추가되지 않은 memory bank 는 lockEnable 에서 lock 을 하더라도 반영되지 않음)
        @param acsTag : write tag, read tag 시 사용하는 password 와 다르게 설정되어야함
     */
    private OperationResult lockTag(boolean isLock) {
        AccessTag accessTag = new AccessTag();
        accessTag.acsPwd = "0x12345678";
        int result = mRfidMgr.LockTag(EPC_BIT, isLock ? EPC_BIT : 0, accessTag); // ex) USER unlock EPC lock 설정
        if(accessTag.errOp != RFIDConst.CommandErr.SUCCESS || accessTag.errTag != RFIDConst.CommandErr.SUCCESS) {
            result = RFIDConst.CommandErr.COMM_ERR;
        }
        Log.d(TAG, "lockTag : result : " + result + " tagId : " + accessTag.tagId + " errOp : " + accessTag.errOp + " errTag : " + accessTag.errTag);
        return setOperationResult(result, accessTag);
    }

    private OperationResult setOperationResult(int result, AccessTag accessTag) {
        if(mOperationResult != null) {
            mOperationResult.setCommandResult(result);
            mOperationResult.setTagId(accessTag.tagId);
            mOperationResult.setErrOp(accessTag.errOp);
            mOperationResult.setErrTag(accessTag.errTag);

            if(result == RFIDConst.CommandErr.SUCCESS && accessTag.errTag == RFIDConst.CommandErr.SUCCESS
                    && accessTag.errOp == RFIDConst.CommandErr.SUCCESS) {
                mOperationResult.setSuccess(true);
            } else {
                mOperationResult.setSuccess(false);
            }
        }
        return mOperationResult;
    }

    private void startScanCompletable() {
        Log.d(D, "startScanCompletable");
        changeStartToStop();
        Completable.create(subscriber -> {
            startScan();
            subscriber.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "Completed start scan");
                }, throwable -> {
                    Log.d(TAG, "Error in startScanCompletable");
                });
    }

    private void stopScanCompletable() {
        Log.d(D, "stopScanCompletable");
        changeStopToStart();
        Completable.create(subscriber -> {
            stopScan();
            subscriber.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "Completed stop scan");
                }, throwable -> {
                    Log.d(TAG, "Error in stopScanCompletable");
                });
    }

    private void triggerStartScanCompletable() {
        Log.d(D, "triggerStartScanCompletable");
        changeStartToStop();
        Completable.create(subscriber -> {
            triggerScanStart();
            subscriber.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "Completed stop scan");
                }, throwable -> {
                    Log.d(TAG, "Error in triggerStartScanCompletable");
                });
    }

    private void triggerStopScanCompletable() {
        Log.d(D, "triggerStopScanCompletable");
        changeStopToStart();
        Completable.create(subscriber -> {
            triggerScanStop();
            subscriber.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "Completed stop scan");
                }, throwable -> {
                    Log.d(TAG, "Error in triggerStopScanCompletable");
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");

        if(mIsReadStart || mAutoScanStart) {
            mIsReadStart = false;
            mAutoScanStart = false;
            stopScanCompletable();
            mRfidMgr.Stop();
            Log.d(TAG, "stop4");
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_BLUETOOTH) {
            if(resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_disable), Toast.LENGTH_SHORT).show();
                Log.d("finish()", "finishing!!!!");
                finish();
            }
        } else if(requestCode == REQUEST_WRITE_TAG && resultCode == Activity.RESULT_OK) {
            String currentTagId = data.getStringExtra(DialogWriteTag.CURRENT_TAG_ID);
            String tagIdToWrite = data.getStringExtra(DialogWriteTag.TAG_ID_TO_WRITE);
            if(tagIdToWrite != null) {
                Log.d(TAG, "currentTagId : " + currentTagId + " tagIdToWrite : " + tagIdToWrite);
                mUtil.showProgress(mProgress, RFIDDemoActivity.this, true);
                startWriteTag(tagIdToWrite, EPC, 2, "0");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Handler mAutoScanHandler = new Handler() {
        @Override
        public void dispatchMessage(@NonNull Message msg) {

            super.dispatchMessage(msg);
        }
    };

    private void startAutoScan() {
        try {
            mAutoScanStart = true;
            int interval = mPrefUtil.getIntPreference(PreferenceUtil.KEY_SCAN_AUTO_INTERVAL);
            mAutoScanInterval = (interval + 1) * 1000;
            Log.d(TAG, "autoScanInterval" + mAutoScanInterval);
            mAutoScanHandler.post(runnable);
        } catch(Exception e) {
        }
    }

    /* To check if data packet received after invoked startInventory on auto read mode */
    private CountDownTimer mCallbackCheckTimer = new CountDownTimer(5000, 1000) {
        @Override
        public void onTick(long l) {
        }

        @Override
        public void onFinish() {
            boolean isAutoScan = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_SCAN_AUTO_ENABLE);

            if(isAutoScan) {
                Log.d(TAG, "=== Didn't receive RFIDCallback, Restart StartAutoScan");
                mAutoScanHandler.removeCallbacksAndMessages(null);
                mAutoScanHandler.post(runnable);
            }
        }
    };

    /* Start reading RFID tag */
    private void startScan() {
        mIsSaveLog = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_SAVE_LOG, false);
        if(mIsSaveLog) {
            String currentDate = mSimpleDataFormat.format(new Date());
            String strStart = "Start Scan : " + currentDate;
            logToFile(strStart);
            mHandler.sendEmptyMessage(HANDLER_MSG_SAVE_LOG);
        }

        boolean isAutoScan = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_SCAN_AUTO_ENABLE);
        if(isAutoScan) {
            startAutoScan();
        } else {
            AsyncStart asyncStart = new AsyncStart();
            asyncStart.execute();
        }
    }

    private void changeStopToStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLinearCheckbox.setVisibility(View.VISIBLE);
                mBtnStartInventory.setText(R.string.read_rfid_tag);
            }
        });
    }

    private void changeStartToStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtnStartInventory.setText(R.string.stop_rfid_tag);
                mLinearCheckbox.setVisibility(View.GONE);
            }
        });
    }

    /* Stop reading RFID tag */
    private void stopScan() {
        mAutoScanStart = false;
        mAutoScanHandler.removeCallbacksAndMessages(null);
        callbackTimerCancel();
    }

    private void rfidConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RFIDDemoActivity.this);
        builder.setTitle(R.string.connect_dlg_title);
        builder.setMessage(R.string.not_connected);
        builder.setCancelable(false);
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    Log.d("finish()", "finishing!!!!");
                    finish();
                }
                return false;
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), RFIDControlActivity.class);
                startActivity(intent);
                Log.d("finish()", "finishing!!!!");
                finish();

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private String getBatteryInfo(boolean isReading) {
        int batteryVolt = 0;
        int batteryLevel = 0;
        int chargingState = 0;
        int count = 0;

        do {
            batteryLevel = mRfidMgr.GetBattLevel();
            Log.d(TAG, "GetBattLevel :" + batteryLevel);
            if(batteryLevel > 0) {
                break;
            } else
                count++;

            if(count > 3)
                break;
        }
        while(true);

        count = 0;

        do {
            chargingState = mRfidMgr.GetChargingState();
            Log.d(TAG, "GetChargingState : " + chargingState);
            if(chargingState >= RFIDConst.CommandErr.SUCCESS) {
                if(chargingState == 1 || chargingState == 0)
                    break;
            } else {
                count++;
            }

            if(count > 3)
                break;

            mUtil.sleep(100);
        }
        while(true);

        do {
            batteryVolt = mRfidMgr.GetBattVolt();
            Log.d(TAG, "GetBattVolt :" + batteryVolt);
            if(batteryVolt >= 0)
                break;
            else
                count++;

            if(count > 3)
                break;

            mUtil.sleep(100);
        }
        while(true);

        String batteryInfo = batteryVolt + "mV,  " + batteryLevel + "%,  "
                + ((chargingState == 1) ? getString(R.string.charging) : getString(R.string.not_charging));

        return batteryInfo;
    }

    private boolean makeFolderAndFile() {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }

        if(mFile != null) {
            return true;
        }

        String fileName = SAVED_LOG_FILE + "_" + LOG_DATE_FORMAT.format(new Date()) + ".log";
        mUtil.createFolder(RFID_CONTROL_FOLDER);
        mUtil.createFolder(RFID_CONTROL_FOLDER + "/" + SAVED_LOG_FOLDER);
        mFile = mUtil.createFile(RFID_CONTROL_FOLDER + "/" + SAVED_LOG_FOLDER + "/" + fileName);
        if(mFile.exists()) {
            String title = "CurrentTime, ReadCount[Number of Tag, Total Read], Batt(mV), Batt(%), Charging flag, RFID Module Temp";
            logToFile(title);
        }

        return true;
    }

    public void saveLog(String batteryInfo) {
        if(mLogStartTime < 0)
            mLogStartTime = System.currentTimeMillis();

        String currentDate = mSimpleDataFormat.format(new Date());
        //String eslapseTime = formatElapsedTime(System.currentTimeMillis() - mLogStartTime);

        //String logStr = currentDate+",  [" + mTotalCount + "  " + mReadCount + "],  " + eslapseTime + ",  " + mBatterySummary;
        String logStr = currentDate + ",  [" + mTotalCount + "  " + mReadCount + "],  " + batteryInfo + ",  " + mTempValue;

        logToFile(logStr);

    }

    private synchronized boolean logToFile(String s) {
        if(mFile == null || !mFile.exists()) {
            makeFolderAndFile();
        }

        boolean result = true;
        FileWriter fw = null;
        BufferedWriter writer = null;
        StringBuilder buffer = new StringBuilder();
        try {
            fw = new FileWriter(mFile, true);
            writer = new BufferedWriter(fw);
            buffer.append(s + "\r\n");
            writer.write(buffer.toString());
            writer.flush();
        } catch(IOException e) {
            e.printStackTrace();
            result = false;
        } finally {
            buffer.setLength(0);
            buffer = null;
            try {
                if(writer != null) {
                    writer.close();
                    writer = null;
                }
                if(fw != null) {
                    fw.close();
                    fw = null;
                }
            } catch(IOException ie) {
                ie.printStackTrace();
            }
        }
        return result;
    }

    private void addScanData(final String ogrData) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        String data = ogrData;
                        Log.d(TAG, ogrData);
                        if(mIsTemp) {
                            String[] arrStr = data.split(",h="); //h= : RFID Module temperature
                            if(arrStr.length > 1) {
                                data = arrStr[0];
                                mTempValue = arrStr[1];
                                mTextTemp.setText(mTempValue);
                            }
                        }

                        Integer count = mHashItems.get(data);
                        if(count == null) {
                            mTotalCount += 1;
                            mHashItems.put(data, 1);
                            mItems.add(new RfidListItem(data, 1));
                            mRvAdapter.notifyItemChanged(mItems.size() - 1);
                        } else {
                            mHashItems.put(data, count + 1);
                            for(int i = 0; i < mItems.size(); i++) {
                                RfidListItem rfItem = mItems.get(i);
                                if(rfItem.getName().equalsIgnoreCase(data)) {
                                    rfItem.setCount(count + 1);
                                    mRvAdapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                        }
                        mTextTotalCount.setText("" + mTotalCount);
                        mReadCount += 1;
                        mTextReadCount.setText("" + mReadCount);

                    }
                });
    }

    public List sortByValue(final Map map, boolean isAsc) {

        List<String> list = new ArrayList();
        list.addAll(map.keySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) o2).compareTo(o1);
            }
        });

        if(isAsc) {
            mOptionMenu.getItem(0).setIcon(R.drawable.sort_down);
        } else {
            mOptionMenu.getItem(0).setIcon(R.drawable.sort_up);
            Collections.reverse(list); // 주석시 오름차순
        }

        return list;
    }

    private RFIDApplication.NotifyDataCallbacks mDataCallbacks = new RFIDApplication.NotifyDataCallbacks() {
        @Override
        public void notifyDataPacket(RecvPacket recvPacket) {
            Log.d(TAG, "notifyDataPacket : " + recvPacket.RecvString);

            callbackTimerCancel();

            addScanData(recvPacket.RecvString);

            boolean isAutoScan = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_SCAN_AUTO_ENABLE);
            mRfidMgr.GetOperationMode(mOperationMode);
            boolean isContinuous = mOperationMode.single == RFIDConst.RFIDConfig.INVENTORY_MODE_CONTINUOUS;
            if(!isContinuous && !isAutoScan) {
                mIsReadStart = false;
                stopScanCompletable();
                mRfidMgr.Stop();
            }

            mAutoScanHandler.removeCallbacksAndMessages(null);

            if(mIsReadStart && mAutoScanStart)
                mAutoScanHandler.postDelayed(runnable, mAutoScanInterval);


        }

        @Override
        public void notifyChangedState(int state) {
            Log.d(TAG, "notifyChangedState : " + state);

            if(state == RFIDConst.DeviceState.TRIGGER_RFID_KEYDOWN) {
                Log.d(D, "Trigger key down");
                Log.d(TAG, "TRIGGER_RFID_KEYDOWN");
                mAutoScanHandler.removeCallbacksAndMessages(null);
                if(!mIsReadStart && !mAutoScanStart) {
                    mIsReadStart = true;
                    triggerStartScanCompletable();
                } else {
                    mIsReadStart = false;
                    stopScanCompletable();
                }
            } else if(state == RFIDConst.DeviceState.TRIGGER_RFID_KEYUP) {
                Log.d(D, "Trigger key up");
                Log.d(TAG, "TRIGGER_RFID_KEYUP");
                if(!mAutoScanStart) {
                    mIsReadStart = false;
                    triggerStopScanCompletable();
                }
            } else if(state == RFIDConst.DeviceState.TRIGGER_SCAN_KEYDOWN) {
                Log.d(TAG, "TRIGGER_SCAN_KEYDOWN");
                if(!isPause)
                    Toast.makeText(getApplicationContext(), getString(R.string.scanner_mode), Toast.LENGTH_LONG).show();
            } else if(state == RFIDConst.DeviceState.LOW_BATT) {
                Log.d(TAG, "LOW_BATT");
                mIsReadStart = false;
                stopScanCompletable();
                mRfidMgr.Stop();
                Log.d(TAG, "stop7");
            } else if(state == RFIDConst.DeviceState.BT_DISCONNECTED) {
                Log.d(TAG, "BT_DISCONNECTED");
                mRfidMgr.Close();
                Log.d(TAG, "close12");
                mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SAVE_LOG, false);
                mIsReadStart = false;
                RFIDControlActivity.POWER_OFF_FLAG = true;
                Intent intent = new Intent(RFIDDemoActivity.this, RFIDControlActivity.class);
                startActivity(intent);
            } else if(state == RFIDConst.DeviceState.USB_DISCONNECTED
                    && mPrefUtil.getStringPreference(PreferenceUtil.KEY_OPEN_OPTION)
                    .equalsIgnoreCase(RFIDControlActivity.OpenOption.WIRED.toString())) {
                Log.d(TAG, "USB_DISCONNECTED");
                Log.d(TAG, mIsReadStart ? "mIsReadStart : true" : "mIsReadStart : false");
                RFIDControlActivity.USB_DETACHED_FLAG = true;
                mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SAVE_LOG, false);
                mIsReadStart = false;
                Intent connectionIntent = new Intent(RFIDDemoActivity.this, RFIDControlActivity.class);
                startActivity(connectionIntent);
            } else if(state == RFIDConst.DeviceState.POWER_OFF
                    && mPrefUtil.getStringPreference(PreferenceUtil.KEY_OPEN_OPTION)
                    .equalsIgnoreCase(RFIDControlActivity.OpenOption.WIRED.toString())) {
                mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SAVE_LOG, false);
                mIsReadStart = false;
                RFIDControlActivity.POWER_OFF_FLAG = true;
                Intent intent = new Intent(RFIDDemoActivity.this, RFIDControlActivity.class);
                startActivity(intent);
            }
        }
    };

    private void callbackTimerCancel() {
        try {
            mCallbackCheckTimer.cancel();
            Log.d(TAG, "CallbackTimer Canceled");

        } catch(Exception e) {
        }
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(mAutoScanStart) {
                Log.d(TAG, "CallbackTimer Started");
                mCallbackCheckTimer.start();
                Log.d(TAG, "--- handler start ");
                mRfidMgr.StartInventory();
            }
        }
    };

    private void triggerScanStart() {
        mIsSaveLog = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_SAVE_LOG, false);
        if(mIsSaveLog) {
            String currentDate = mSimpleDataFormat.format(new Date());
            String strStart = "Start Scan : " + currentDate;
            logToFile(strStart);
            mHandler.sendEmptyMessage(HANDLER_MSG_SAVE_LOG);
        }

        boolean isAutoScan = mPrefUtil.getBooleanPreference(PreferenceUtil.KEY_SCAN_AUTO_ENABLE);
        if(isAutoScan) {
            mAutoScanStart = true;
            int interval = mPrefUtil.getIntPreference(PreferenceUtil.KEY_SCAN_AUTO_INTERVAL);
            mAutoScanInterval = (interval + 1) * 1000;
            Log.d(TAG, "autoScanInterval" + mAutoScanInterval);
        }
    }

    private void triggerScanStop() {
        if(mIsSaveLog) {
            mUtil.startFileOnlyMediaScan(mFile.getParent());
        }
    }

    private View.OnClickListener mOnCheckboxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.checkbox_auto_scan) {
                if(mCheckboxAutoScan.isChecked()) {
                    mCheckboxContinuous.setChecked(false);
                    mOperationMode.single = RFIDConst.RFIDConfig.INVENTORY_MODE_SINGLE;
                    mRfidMgr.SetOperationMode(mOperationMode);

                    mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SCAN_AUTO_ENABLE, true);
                } else {
                    mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SCAN_AUTO_ENABLE, false);
                }
                Log.e("checkchange_autoscan", mOperationMode.single == RFIDConst.RFIDConfig.INVENTORY_MODE_SINGLE ?
                        "single" : "continuous");
            } else if(v.getId() == R.id.checkbox_continuous) {
                if(mCheckboxContinuous.isChecked()) {
                    mCheckboxAutoScan.setChecked(false);
                    mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SCAN_CONTINUOUS_ENABLE, true);
                    mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SCAN_AUTO_ENABLE, false);
                    mOperationMode.single = RFIDConst.RFIDConfig.INVENTORY_MODE_CONTINUOUS;
                    mAutoScanStart = false;
                } else {
                    mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SCAN_CONTINUOUS_ENABLE, false);
                    mOperationMode.single = RFIDConst.RFIDConfig.INVENTORY_MODE_SINGLE;
                }
                mRfidMgr.SetOperationMode(mOperationMode);
                Log.e("checkchange_continuous", mOperationMode.single == RFIDConst.RFIDConfig.INVENTORY_MODE_SINGLE ?
                        "single" : "continuous");
            } else if(v.getId() == R.id.checkbox_save_log) {
                if(mCheckboxSaveLog.isChecked()) {
                    mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SAVE_LOG, true);
                } else {
                    mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_SAVE_LOG, false);
                }
            } else if(v.getId() == R.id.checkbox_beep_sound) {
                mUtil.showProgress(mProgress, RFIDDemoActivity.this, true);

                if(mCheckboxBeepSound.isChecked()) {
                    AsyncSetVolume asyncSetVolume = new AsyncSetVolume();
                    asyncSetVolume.execute(mPrefUtil.getIntPreference(PreferenceUtil.KEY_BEEP_SOUND, RFIDConst.DeviceConfig.BUZZER_HIGH));
                } else {
                    AsyncSetVolume asyncSetVolume = new AsyncSetVolume();
                    asyncSetVolume.execute(RFIDConst.DeviceConfig.BUZZER_MUTE);
                }
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_start_inventory) {
                /* Stop */
                if(mIsReadStart || mAutoScanStart) {
                    //stopScan();
                    stopScanCompletable();
                    mIsReadStart = false;
                    AsyncStop asyncStop = new AsyncStop();
                    asyncStop.execute();
                    //mPrefUtil.putBooleanPreference(PreferenceUtil.KEY_IS_READ_TEMP, false);
                }
                /* Read */
                else {
                    int mode = mRfidMgr.GetTriggerMode();
                    Log.d("mode", "trigger mode : " + mode);
                    switch(mode) {
                        case SCAN_MODE:
                            if(!isPause) {
                                Log.d(TAG, "RFID Demo is currently paused");
                                Toast.makeText(getApplicationContext(), getString(R.string.scanner_mode), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case RFID_MODE:
                        default:
                            mIsReadStart = true;
                            startScanCompletable();
                            break;
                    }
                }
            } else if(v.getId() == R.id.btn_write_tag) {
                Intent intent = new Intent(RFIDDemoActivity.this, DialogWriteTag.class);
                startActivityForResult(intent, REQUEST_WRITE_TAG);
            } else if(v.getId() == R.id.btn_read_tag) {
                startReadTag(7, EPC, 0, "0");
            } else if(v.getId() == R.id.btn_lock_tag) {
                startLockTag(true);
            } else if(v.getId() == R.id.btn_unlock_tag) {
                startLockTag(false);
            }
        }
    };

    private static class LogSaveHandler extends Handler {
        private final WeakReference<RFIDDemoActivity> weakReference;

        public LogSaveHandler(RFIDDemoActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RFIDDemoActivity rfidDemoActivity = weakReference.get();
            rfidDemoActivity.handleMessage(msg);
        }
    }

    private void handleMessage(Message msg) {
        switch(msg.what) {
            case HANDLER_MSG_SAVE_LOG:
                if(mIsReadStart && mIsSaveLog) {
                    Log.d(TAG, "MSG_SAVE_LOG");
                    /* Save log every 10 seconds to check battery info */
                    saveLog(getBatteryInfo(true));
                    mHandler.sendEmptyMessageDelayed(HANDLER_MSG_SAVE_LOG, 10 * 1000);
                }
        }
    }

    private void removeHandlerMessage() {
        if(mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        else
            Log.d(TAG, "Handler is null");
    }

    class AsyncGetVolume extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mUtil.showProgress(mProgress, RFIDDemoActivity.this, true);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(mRfidMgr.IsOpened()) {
                int count = 0;
                do {
                    mVolumeValue = mRfidMgr.GetBuzzerVol();
                    Log.e("AsyncGetVolume", "mVolume === " + mVolumeValue);
                    if(mVolumeValue > 0) {
                        if(mVolumeValue == 0 || mVolumeValue == 1 || mVolumeValue == 2) {
                            break;
                        }
                    } else {
                        count++;
                    }

                    if(count > 2)
                        break;

                    try {
                        Thread.sleep(100);
                    } catch(Exception e) {
                    }
                }
                while(true);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if(isSuccess) {
                initState();
            } else {
                Log.d("FailMessage", "GetBuzzerVol Failed");
            }
            mUtil.showProgress(mProgress, RFIDDemoActivity.this, false);
            super.onPostExecute(isSuccess);
        }
    }

    class AsyncSetVolume extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... integers) {
            if(mRfidMgr.IsOpened()) {
                do {
                    int result = mRfidMgr.SetBuzzerVol(integers[0]);
                    Log.e("buzzerVolumeResult=====", "" + result);
                    if(result != -1 && result != -100) {
                        break;
                    }
                }
                while(true);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            mUtil.showProgress(mProgress, RFIDDemoActivity.this, false);
            super.onPostExecute(isSuccess);
        }
    }

    class AsyncStop extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "stop9");
            mRfidMgr.Stop();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(mIsSaveLog) {
                removeHandlerMessage();
                mUtil.startFileOnlyMediaScan(mFile.getParent());
                saveLog(getBatteryInfo(false));
            }
        }
    }

    class AsyncStart extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            mRfidMgr.StartInventory();
            return null;
        }
    }

    /* To detect status bar touched */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int Y = (int) event.getY();

        if(Y < 400) {
            onWindowFocusChanged(true);
        }
        return true;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        Log.d("Focus debug", "Focus changed !");

        if(!hasFocus) {
            Log.d("Focus debug", "Lost focus !");

            if(mIsReadStart || mAutoScanStart) {
                Log.e(TAG, "Stop RFID Reading");
                mIsReadStart = false;
                mAutoScanStart = false;
                stopScanCompletable();
                mRfidMgr.Stop();
                Log.d("stop", "stop10");
            }
        }
    }
}
