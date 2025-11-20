package device.sdk.sample.mdm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.view.MenuItem;

import androidx.preference.DropDownPreference;

import device.sdk.Control;

public class NetworkPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String KEY_ENABLED_NFC = "enabled_nfc";
    private static final String KEY_AIRPLANE_MODE = "airplane_mode";
    private static final String KEY_ENABLED_WIFI_ON_SLEEP = "enabled_wifi_on_sleep";
    private static final String KEY_ENABLED_WIFI = "enabled_wifi";
    private static final String KEY_LOCK_NFC_OPTION = "lock_nfc_option";
    private SwitchPreference mEnabledNfc;
    private SwitchPreference mAirplaneMode;
    private DropDownPreference mEnabledWifiOnSleep;
    private SwitchPreference mEnabledWifi;
    private SwitchPreference mLockNfcOption;

    private static final Control mControl = new Control();

    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(intent.getAction())) {
                handleNfcStateChanged(intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF));
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                handleWifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            }
        }
    };

    private void handleNfcStateChanged(int state) {
        switch (state) {
            case NfcAdapter.STATE_OFF:
                mEnabledNfc.setChecked(false);
                mEnabledNfc.setEnabled(true);
                mEnabledNfc.setSummary(getString(R.string.pref_summary_string_param, "OFF"));
                break;
            case NfcAdapter.STATE_ON:
                mEnabledNfc.setChecked(true);
                mEnabledNfc.setEnabled(true);
                mEnabledNfc.setSummary(getString(R.string.pref_summary_string_param, "ON"));
                break;
            case NfcAdapter.STATE_TURNING_ON:
                mEnabledNfc.setEnabled(false);
                mEnabledNfc.setSummary(getString(R.string.pref_summary_string_param, "TURNING ON"));
                break;
            case NfcAdapter.STATE_TURNING_OFF:
                mEnabledNfc.setEnabled(false);
                mEnabledNfc.setSummary(getString(R.string.pref_summary_string_param, "TURNING OFF"));
                break;
        }
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mEnabledWifi.setEnabled(false);
                mEnabledWifi.setSummary(getString(R.string.pref_summary_string_param, "ENABLING"));
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                mEnabledWifi.setChecked(true);
                mEnabledWifi.setEnabled(true);
                mEnabledWifi.setSummary(getString(R.string.pref_summary_string_param, "ENABLED"));
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mEnabledWifi.setEnabled(false);
                mEnabledWifi.setSummary(getString(R.string.pref_summary_string_param, "DISABLING"));
                break;
            case WifiManager.WIFI_STATE_DISABLED:
            default:
                mEnabledWifi.setChecked(false);
                mEnabledWifi.setEnabled(true);
                mEnabledWifi.setSummary(getString(R.string.pref_summary_string_param, "DISABLED"));
                break;
        }
    }

    private void bindPreferenceChange() {
        /* KEY_ENABLED_NFC */
        mEnabledNfc = findPreference(KEY_ENABLED_NFC);
        handleNfcStateChanged(mControl.getNfcEnabled());
        mEnabledNfc.setOnPreferenceChangeListener(this);
        /* KEY_LOCK_NFC_OPTION */
        mLockNfcOption = findPreference(KEY_LOCK_NFC_OPTION);
        mLockNfcOption.setChecked(mControl.isNfcEnabledOption());
        mLockNfcOption.setOnPreferenceChangeListener(this);
        onPreferenceChange(mLockNfcOption, mLockNfcOption.isChecked());
        /* KEY_AIRPLANE_MODE */
        mAirplaneMode = findPreference(KEY_AIRPLANE_MODE);
        mAirplaneMode.setChecked(mControl.isAirplaneModeOn());
        mAirplaneMode.setOnPreferenceChangeListener(this);
        onPreferenceChange(mAirplaneMode, mAirplaneMode.isChecked());
        /* KEY_ENABLED_WIFI_ON_SLEEP */
        mEnabledWifiOnSleep = findPreference(KEY_ENABLED_WIFI_ON_SLEEP);
        mEnabledWifiOnSleep.setValueIndex(mControl.getWifiSleepPolicy());
        mEnabledWifiOnSleep.setSummary(mEnabledWifiOnSleep.getValue());
        mEnabledWifiOnSleep.setOnPreferenceChangeListener(this);
        /* KEY_ENABLED_WIFI */
        mEnabledWifi = findPreference(KEY_ENABLED_WIFI);
        handleWifiStateChanged(mControl.getWifiState());
        mEnabledWifi.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean result = true;
        final String key = preference.getKey();
        if (KEY_ENABLED_NFC.equals(key)) {
            mControl.setNfcEnabled((Boolean) value);
        } else if (KEY_AIRPLANE_MODE.equals(key)) {
            mControl.setAirplaneModeOn((Boolean) value);
            mAirplaneMode.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_ENABLED_WIFI_ON_SLEEP.equals(key)) {
            mControl.setWifiSleepPolicy(mEnabledWifiOnSleep.findIndexOfValue((String) value));
            mEnabledWifiOnSleep.setSummary((String) value);
        } else if (KEY_ENABLED_WIFI.equals(key)) {
            mControl.setWifiEnabled((Boolean) value);
        } else if (KEY_LOCK_NFC_OPTION.equals(key)) {
            mControl.setNfcEnabledOption((Boolean) value);
            mEnabledNfc.setEnabled(mControl.isNfcEnabledOption());
        }
        return result;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_network);
        mIntentFilter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mIntentFilter);
        bindPreferenceChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), MdmSampleActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}