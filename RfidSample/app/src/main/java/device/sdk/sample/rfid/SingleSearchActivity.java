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
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import device.common.rfid.BattEvent;
import device.common.rfid.RFIDConst;
import device.common.rfid.RecvPacket;
import device.sdk.RFIDManager;
import device.sdk.sample.rfid.util.PreferenceUtil;
import device.sdk.sample.rfid.util.Utils;


public class SingleSearchActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

    private RFIDManager mRfidMgr;
    private TagInfo mTagInfo;

    private ScrollView svLog;
    private EditText etInput;
    private EditText etThreshold;
    private EditText etStep;
    private Button btnSingleSearch;
    private TextView tvLog;
    private Button btnClearLog;

    private Utils mUtils;
    private PreferenceUtil mPrefUtil;

    private boolean isSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_search);
        initView();

        mUtils = new Utils(this);
        mPrefUtil = new PreferenceUtil(SingleSearchActivity.this);
        mRfidMgr = RFIDManager.getInstance();
        mTagInfo = new TagInfo();
        if(!mRfidMgr.IsOpened()) {
            rfidConnectDialog();
        } else {
            mRfidMgr.SetResultType(RFIDConst.ResultType.RFID_RESULT_CALLBACK);
            //Disable battery event while searching
            BatteryTask batteryTask = new BatteryTask();
            batteryTask.execute(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRFIDApplication().setNotifyDataCallback(dataCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isSearch) {
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

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.single_search));
        this.setSupportActionBar(toolbar);

        svLog = findViewById(R.id.sv_log);
        etInput = findViewById(R.id.et_single_tag);
        etThreshold = findViewById(R.id.et_threshold);
        etStep = findViewById(R.id.et_step);
        btnSingleSearch = findViewById(R.id.btn_single_search);
        btnSingleSearch.setOnClickListener(onClickListener);
        tvLog = findViewById(R.id.tv_log);
        btnClearLog = findViewById(R.id.btn_clear_log);
        btnClearLog.setOnClickListener(onClickListener);
    }

    private void rfidConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SingleSearchActivity.this);
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

    private void scrollToBottom() {
        svLog.post(new Runnable() {
            @Override
            public void run() {
                svLog.scrollTo(0, tvLog.getHeight());
            }
        });
    }

    private void enableView(boolean isEnable) {
        if(isEnable) {
            btnSingleSearch.setText(getText(R.string.single_search));
            etInput.setEnabled(true);
            etStep.setEnabled(true);
            etThreshold.setEnabled(true);
        } else {
            btnSingleSearch.setText(getText(R.string.stop));
            tvLog.setText("");
            etInput.setEnabled(false);
            etStep.setEnabled(false);
            etThreshold.setEnabled(false);
        }
    }

    private void setData(RecvPacket recvPacket) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,recvPacket.RecvString);
                String[] tagInfo = recvPacket.RecvString.split(",");
                String tagId = tagInfo[0];
                String field = tagInfo[1].split("=")[0];
                String rfPower = tagInfo[1].split("=")[1];
                if(field.equals("p")) {
                    tvLog.append("Tag : " + tagId + "\nRF Power :" + rfPower + "\n\n");
                    scrollToBottom();

                    if(mUtils.stringToInteger(rfPower) == mTagInfo.threshold) {
                        enableView(true);
                    }
                }
            }
        });
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_single_search) {
                if(!isSearch) {
                    String tagId = etInput.getText().toString(); //3400123412341234123412343333
                    int threshold = mUtils.stringToInteger(etThreshold.getText().toString());
                    int step = mUtils.stringToInteger(etStep.getText().toString());
                    if(tagId == null || tagId.isEmpty()) {
                        Toast.makeText(SingleSearchActivity.this, getString(R.string.request_input_tagid), Toast.LENGTH_SHORT).show();
                    } else if(threshold < 0) {
                        Toast.makeText(SingleSearchActivity.this, getString(R.string.request_input_threshold), Toast.LENGTH_SHORT).show();
                    } else if(step < 0) {
                        Toast.makeText(SingleSearchActivity.this, getString(R.string.request_input_step), Toast.LENGTH_SHORT).show();
                    } else if(threshold < 3 || threshold > 29) {
                        Toast.makeText(SingleSearchActivity.this, getString(R.string.notify_wrong_range_of_threshold), Toast.LENGTH_SHORT).show();
                    } else if(step < 3 || step > 6) {
                        Toast.makeText(SingleSearchActivity.this, getString(R.string.notify_wrong_range_of_step), Toast.LENGTH_SHORT).show();
                    } else {
                        hideSoftKeyboard(getCurrentFocus());
                        mTagInfo.setTagId(tagId);
                        mTagInfo.setThreshold(threshold);
                        mTagInfo.setStep(step);
                        isSearch = true;
                        SingleSearchTask singleSearchTask = new SingleSearchTask();
                        singleSearchTask.execute(mTagInfo);
                    }
                } else {
                    mRfidMgr.Stop();
                    isSearch = false;
                    enableView(true);
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
            setData(recvPacket);
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

    private class SingleSearchTask extends AsyncTask<TagInfo, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enableView(false);
        }

        @Override
        protected Integer doInBackground(TagInfo... tagInfo) {
            int result;
            /*
                Length : 4~128
                Tag id : PC + EPC (No CRC)
                Threshold : 3~29
                Step : 3~6
                     */
            result = mRfidMgr.SingleSearch(tagInfo[0].getTagId().length(), tagInfo[0].getTagId(), tagInfo[0].getThreshold(), tagInfo[0].getStep());
            Log.d(TAG, "Single Search result : " + result);

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
//            if(result != RFIDConst.CommandErr.SUCCESS) {
//                Toast.makeText(SingleSearchActivity.this, getString(R.string.search_failed, result), Toast.LENGTH_SHORT).show();
//            }
        }
    }

    private class TagInfo {
        private String tagId;
        private int threshold;
        private int step;

        public String getTagId() {
            return tagId;
        }

        public void setTagId(String tagId) {
            this.tagId = tagId;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
        }
    }
}
