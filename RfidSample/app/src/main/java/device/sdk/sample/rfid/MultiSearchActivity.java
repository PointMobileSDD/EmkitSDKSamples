package device.sdk.sample.rfid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import device.common.rfid.BattEvent;
import device.common.rfid.RFIDConst;
import device.common.rfid.RecvPacket;
import device.sdk.RFIDManager;
import device.sdk.sample.rfid.util.PreferenceUtil;
import device.sdk.sample.rfid.util.Utils;

public class MultiSearchActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

    private RFIDManager mRfidMgr;

    private Button btnMultiSearch;
    private Button btnMultiSearchSettingSettings;
    private TextView tvLog;
    private Button btnClearLog;

    private Utils mUtils;
    private PreferenceUtil mPrefUtil;

    private boolean isSearching = false;
    private int numberOfTags = 0;
    private int foundTags = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_search);
        mUtils = new Utils(this);
        mPrefUtil = new PreferenceUtil(MultiSearchActivity.this);
        mRfidMgr = RFIDManager.getInstance();

        initView();

        if(!mRfidMgr.IsOpened()) {
            rfidConnectDialog();
        } else {
            //Disable battery event while searching
            BatteryTask batteryTask = new BatteryTask();
            batteryTask.execute(false);
        }
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.multi_search));
        this.setSupportActionBar(toolbar);

        btnMultiSearch = findViewById(R.id.btn_multi_search);
        btnMultiSearch.setOnClickListener(onClickListener);
        btnMultiSearchSettingSettings = findViewById(R.id.btn_multi_search_setting);
        btnMultiSearchSettingSettings.setOnClickListener(onClickListener);
        tvLog = findViewById(R.id.tv_log);
        btnClearLog = findViewById(R.id.btn_clear_log);
        btnClearLog.setOnClickListener(onClickListener);
    }

    private void rfidConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MultiSearchActivity.this);
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

    private void hideSoftKeyboard(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(view != null) {
                    Log.d(TAG, "view : " + view);
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRfidMgr.SetResultType(RFIDConst.ResultType.RFID_RESULT_CALLBACK);
        getRFIDApplication().setNotifyDataCallback(dataCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isSearching) {
            mRfidMgr.Stop();
        }
        /* To roll-back previous Result Type */
        if(mRfidMgr.IsOpened()) {
            int currentType = mPrefUtil.getIntPreference(PreferenceUtil.KEY_RESULT_TYPE,
                    RFIDConst.ResultType.RFID_RESULT_CALLBACK);
            if(currentType != RFIDConst.ResultType.RFID_RESULT_CALLBACK) {
                mRfidMgr.SetResultType(currentType);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Rollback battery event when Single Search finished
        BatteryTask batteryTask = new BatteryTask();
        batteryTask.execute(true);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_multi_search_setting) {
                Intent intent = new Intent(MultiSearchActivity.this, MultiSearchSettingsActivity.class);
                startActivity(intent);
            }
            else if(v.getId() == R.id.btn_multi_search) {
                if(!isSearching) {
                    tvLog.setText("");
                    hideSoftKeyboard(getCurrentFocus());
                    btnMultiSearch.setText(getString(R.string.stop));
                    isSearching = true;
                    MultiSearchTask multiSearchTask = new MultiSearchTask();
                    multiSearchTask.execute();
                } else {
                    btnMultiSearch.setText(getString(R.string.multi_search));
                    isSearching = false;
                    mRfidMgr.Stop();
                }
            } else if(v.getId() == R.id.btn_clear_log) {
                if(tvLog != null)
                    tvLog.setText("");
            }
        }
    };

    private RFIDApplication.NotifyDataCallbacks dataCallback = new RFIDApplication.NotifyDataCallbacks() {
        @Override
        public void notifyDataPacket(RecvPacket recvPacket) {
            Log.d(TAG, recvPacket.RecvString);
            if(recvPacket.RecvString.contains(":")) {
                String[] data = recvPacket.RecvString.split(":");
                tvLog.append("Found tag : " + data[1] + "\n");
                foundTags++;
                if(foundTags == numberOfTags) {
                    isSearching = false;
                    tvLog.append("Found all tags");
                }
            }
        }

        @Override
        public void notifyChangedState(int state) {

        }
    };

    private class BatteryTask extends AsyncTask<Boolean, Void, Integer> {

        @Override
        protected Integer doInBackground(Boolean... isEnable) {
            int count = 0;
            int result;

            do {
                try {
                    BattEvent battEvent = new BattEvent();
                    if(isEnable[0]) {
                        battEvent.batt = 1;
                        battEvent.charge = 1;
                        battEvent.cycle = 10;
                    } else {
                        battEvent.batt = 0;
                        battEvent.charge = 0;
                        battEvent.cycle = 10;
                    }
                    result = mRfidMgr.SetBattEvent(battEvent);
                    if(result == RFIDConst.CommandErr.SUCCESS) {
                        break;
                    } else {
                        count++;
                    }

                    if(count > 2) {
                        break;
                    }

                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while(true);

            return result;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }

    private class MultiSearchTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            int result = 0;

            numberOfTags = 0;
            foundTags = 0;

            result = mRfidMgr.MultiSearch();
            Log.d(TAG, "MultiSearch result : " + result);

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
//            if(result != RFIDConst.CommandErr.SUCCESS) {
//                Toast.makeText(MultiSearchActivity.this, getString(R.string.search_failed, result), Toast.LENGTH_SHORT).show();
//            }
        }
    }
}
