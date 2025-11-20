package device.sdk.sample.rfid;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import device.common.rfid.RFIDConst;
import device.common.rfid.RecvPacket;
import device.sdk.RFIDManager;
import device.sdk.sample.rfid.adapter.MultiSearchListAdapter;
import device.sdk.sample.rfid.util.PreferenceUtil;
import device.sdk.sample.rfid.util.Utils;

public class MultiSearchSettingsActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();

    private RecyclerView rvTagId;
    private MultiSearchListAdapter adapter;

    private ProgressDialog progressDialog;
    private Button btnApply;
    private Button btnClearAll;

    private RFIDManager mRfidMgr;
    private Utils mUtils;
    private PreferenceUtil mPrefUtil;

    private ArrayList<TagData> dataList = new ArrayList<>();

    private final int MAX_INDEX = 49;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_search_settings);
        Log.d(TAG,"onCreate()");
        initView();
        mUtils = new Utils(MultiSearchSettingsActivity.this);
        mPrefUtil = new PreferenceUtil(MultiSearchSettingsActivity.this);
        mRfidMgr = RFIDManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        mRfidMgr.SetResultType(RFIDConst.ResultType.RFID_RESULT_CALLBACK);
        getRFIDApplication().setNotifyDataCallback(dataCallbacks);
        mRfidMgr.GetSearchList();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.multi_search_setting));
        this.setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(MultiSearchSettingsActivity.this);
        btnApply = findViewById(R.id.btn_apply);
        btnApply.setOnClickListener(onClickListener);
        btnClearAll = findViewById(R.id.btn_clear_all);
        btnClearAll.setOnClickListener(onClickListener);

        rvTagId = findViewById(R.id.rv_tag_id);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTagId.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /* To roll-back previous Result Type */
        if(mRfidMgr.IsOpened()) {
            int currentType = mPrefUtil.getIntPreference(PreferenceUtil.KEY_RESULT_TYPE,
                    RFIDConst.ResultType.RFID_RESULT_CALLBACK);
            if(currentType != RFIDConst.ResultType.RFID_RESULT_CALLBACK) {
                mRfidMgr.SetResultType(currentType);
            }
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_apply) {
                if(adapter != null) {
                    dataList = adapter.apply();
                    SetSearchListTask setSearchListTask = new SetSearchListTask();
                    setSearchListTask.execute();
                }
            } else if(v.getId() == R.id.btn_clear_all) {
                if(adapter != null) {
                    adapter.clearAll();
                }
            }
        }
    };

    private RFIDApplication.NotifyDataCallbacks dataCallbacks = new RFIDApplication.NotifyDataCallbacks() {
        @Override
        public void notifyDataPacket(RecvPacket recvPacket) {
            Log.d(TAG,recvPacket.RecvString);
            String[] searchList = recvPacket.RecvString.split(",");
            for(String item : searchList) {
                if(item.contains(":")) {
                    String[] items = item.split(":");
                    int index = mUtils.stringToInteger(items[0]);
                    String tagId = "";
                    if(items.length > 1)
                        tagId = items[1];
                    dataList.add(index, new TagData(tagId));
                }
            }
            adapter = new MultiSearchListAdapter(MultiSearchSettingsActivity.this, dataList);
            rvTagId.setAdapter(adapter);
        }

        @Override
        public void notifyChangedState(int state) {

        }
    };

    private class SetSearchListTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mUtils.showProgress(progressDialog, MultiSearchSettingsActivity.this, true);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            mRfidMgr.ClearAllSearchList();
            for(int i = 0; i < dataList.size(); i++) {
                if(!dataList.get(i).getTagId().isEmpty()) {
                    int result = mRfidMgr.SetSearchList(i, dataList.get(i).getTagId().length(), dataList.get(i).getTagId());
                    Log.d(TAG, "index : " + i + " tag id : " + dataList.get(i) + " result : " + result);
                }
            }
            return RFIDConst.CommandErr.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mUtils.showProgress(progressDialog, MultiSearchSettingsActivity.this, false);
            finish();
        }
    }

    public class TagData {
        String tagId;

        public TagData(String tagId) {
            this.tagId = tagId;
        }

        public String getTagId() {
            return tagId;
        }

        public void setTagId(String tagId) {
            this.tagId = tagId;
        }
    }
}
