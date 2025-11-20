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

public class WildcardSearchActivity extends BaseActivity {
    private final String TAG = getClass().getSimpleName();

    private RFIDManager mRfidMgr;

    private EditText etTagPattern;
    private Button btnWildcardSearch;
    private ScrollView svLog;
    private TextView tvLog;

    private PreferenceUtil mPrefUtil;

    private boolean isSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wildcard_search);
        initView();

        mPrefUtil = new PreferenceUtil(WildcardSearchActivity.this);
        mRfidMgr = RFIDManager.getInstance();
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
        getRFIDApplication().setNotifyDataCallback(dataCallbacks);
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

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.wildcard_search));
        this.setSupportActionBar(toolbar);

        etTagPattern = findViewById(R.id.et_wildcard_tag_pattern);
        btnWildcardSearch = findViewById(R.id.btn_wild_search);
        btnWildcardSearch.setOnClickListener(onClickListener);
        tvLog = findViewById(R.id.tv_log);
        svLog = findViewById(R.id.sv_log);
    }

    private void rfidConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WildcardSearchActivity.this);
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
            btnWildcardSearch.setText(getString(R.string.wildcard_search));
            etTagPattern.setEnabled(true);
        } else {
            tvLog.setText("");
            btnWildcardSearch.setText(getString(R.string.stop));
            etTagPattern.setEnabled(false);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_wild_search) {
                if(!isSearching) {
                    hideSoftKeyboard(getCurrentFocus());
                    String tagPattern = etTagPattern.getText().toString();
                    if(tagPattern != null && !tagPattern.isEmpty()) {
                        isSearching = true;
                        WildcardSearchTask wildcardSearchTask = new WildcardSearchTask();
                        wildcardSearchTask.execute(tagPattern);
                    } else {
                        Toast.makeText(WildcardSearchActivity.this, getString(R.string.request_input_tag_pattern), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    isSearching = false;
                    enableView(true);
                    mRfidMgr.Stop();
                }
            }
        }
    };

    private RFIDApplication.NotifyDataCallbacks dataCallbacks = new RFIDApplication.NotifyDataCallbacks() {
        @Override
        public void notifyDataPacket(RecvPacket recvPacket) {
            tvLog.append("Found tag : " + recvPacket.RecvString);
            scrollToBottom();
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

    private class WildcardSearchTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            enableView(false);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            String tagPattern = strings[0];

            int result;
            /*
                Tag id pattern : PC + ECP (No CRC)
                length : length of tag id pattern
            */
            result = mRfidMgr.WildcardSearch(tagPattern.length(), tagPattern);
            Log.d(TAG, "Wildcard Search result : " + result);

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
//            if(result != RFIDConst.CommandErr.SUCCESS) {
//                Toast.makeText(WildcardSearchActivity.this, getString(R.string.search_failed, result), Toast.LENGTH_SHORT).show();
//            }
        }
    }
}
