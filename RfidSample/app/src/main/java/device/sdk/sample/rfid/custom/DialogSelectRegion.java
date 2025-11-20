package device.sdk.sample.rfid.custom;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import device.common.rfid.RFIDConst;
import device.sdk.RFIDManager;

import device.sdk.sample.rfid.R;
import device.sdk.sample.rfid.util.Utils;

public class DialogSelectRegion extends Activity
{
    private TextView mTitle;
    private TextView mSubTitle;
    private Spinner mSpRegion;
    private Button mBtnOk;
    private Button mBtnCancel;

    private RFIDManager mRfidMgr;
    private Utils mUtil;
    private ProgressDialog mProgress;

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_spinner);

        mTitle = findViewById(R.id.tv_title);
        mTitle.setText("Select Region");
        mSubTitle = findViewById(R.id.tv_sub_title);
        mSubTitle.setText("Select Region : ");
        mSpRegion = findViewById(R.id.sp_select_region);
        mBtnOk = findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(mOnClickListener);
        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(mOnClickListener);

        mRfidMgr = RFIDManager.getInstance();
        mUtil = new Utils(DialogSelectRegion.this);
        mProgress = new ProgressDialog(DialogSelectRegion.this);

        AsyncGetRegion asyncGetRegion = new AsyncGetRegion();
        asyncGetRegion.execute();
    }

    private void initRegion(String oemInfo)
    {
        if(oemInfo != null)
        {
            String arrRegion[] = getAvailableRegion();
            if(arrRegion != null)
            {
                for(int i = 0; i < arrRegion.length; i++)
                {
                    if(arrRegion[i].equalsIgnoreCase(oemInfo))
                    {
                        mSpRegion.setSelection(i);
                    }
                }
            }
        }
    }

    private String[] getAvailableRegion()
    {
        String availableRegion = mRfidMgr.GetAvailableRegion();
        if(availableRegion != null)
        {
            String arrRegion[] = availableRegion.split(",");
            //Log.d("AAAA", arrRegion[0] + arrRegion[1] + arrRegion[2]);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrRegion);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpRegion.setAdapter(adapter);

            return arrRegion;
        }

        return null;
    }

    private String setRegion()
    {
        String selectedRegion = mSpRegion.getSelectedItem().toString();
        return selectedRegion;
    }

    private void showToast(final String msg)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v.getId() == R.id.btn_ok)
            {
                AsyncSetRegion asyncSetRegion = new AsyncSetRegion();
                asyncSetRegion.execute(setRegion());
            }
            else if(v.getId() == R.id.btn_cancel)
            {
                finish();
            }
        }
    };

    class AsyncGetRegion extends AsyncTask<Void, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            //mUtil.showProgress(mProgress, DialogSelectRegion.this, true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids)
        {
            try
            {
                int count = 0;
                do
                {
                    String oemInfo = mRfidMgr.GetOemInfo();
                    if(oemInfo != null)
                    {
                        Log.e("oemInfo : ", oemInfo);
                        return oemInfo;
                    }
                    else
                    {
                        count++;
                    }

                    if(count > 5)
                    {
                        return null;
                    }
                    Thread.sleep(100);
                }
                while(true);

            } catch(Exception e)
            {
            }
            return null;
        }

        @Override
        protected void onPostExecute(String oemInfo)
        {
            initRegion(oemInfo);
            //mUtil.showProgress(mProgress, DialogSelectRegion.this, false);
            super.onPostExecute(oemInfo);
        }
    }

    class AsyncSetRegion extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            mUtil.showProgress(mProgress, DialogSelectRegion.this, true);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... strings)
        {
            Log.d(TAG, "selected Region : " + strings[0]);

            try
            {
                int count = 0;
                do
                {
                    int result = mRfidMgr.SetRegion(strings[0]);
                    if(result == RFIDConst.CommandErr.SUCCESS)
                    {
                        Log.d("Channel","region select : " + result);
                        return true;
                    }
                    else
                    {
                        count++;
                    }

                    if(count > 5)
                    {
                        return false;
                    }
                    Thread.sleep(100);
                }
                while(true);
            } catch(Exception e)
            {
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess)
        {
            if(isSuccess)
            {
                finish();
            }
            else
            {
                showToast(getString(R.string.region_setting_fail));
            }
            mUtil.showProgress(mProgress, DialogSelectRegion.this, false);
            super.onPostExecute(isSuccess);
        }
    }
}
