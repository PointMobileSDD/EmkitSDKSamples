package device.sdk.sample.mdm;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import android.view.MenuItem;

import androidx.preference.DropDownPreference;
import androidx.preference.SeekBarPreference;

import java.util.ArrayList;
import java.util.List;

import device.sdk.Control;

public class DisplayPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String KEY_BRIGHTNESS_LEVEL = "brightness_level";
    private static final String KEY_AUTO_ROTATE = "auto_rotate";
    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private SeekBarPreference mBrightnessLevel;
    private ListPreference mScreenTimeout;
    private DropDownPreference mAutoRotate;
    private int mMaxBrightnessLevel = 0;

    private static final Control mControl = new Control();

    private void bindPreferenceChange() {
        /* KEY_BRIGHTNESS_LEVEL */
        mBrightnessLevel = findPreference(KEY_BRIGHTNESS_LEVEL);
        mMaxBrightnessLevel = mControl.getScreenBrightnessMax();
        mBrightnessLevel.setMax(mMaxBrightnessLevel);
        mBrightnessLevel.setValue(mControl.getScreenBrightness());
        mBrightnessLevel.setOnPreferenceChangeListener(this);
        onPreferenceChange(mBrightnessLevel, mBrightnessLevel.getValue());
        /* KEY_AUTO_ROTATE */
        mAutoRotate = findPreference(KEY_AUTO_ROTATE);
        if (mControl.isRotationLockToggleVisible()) {
            List<String> items = new ArrayList<>();
            items.add(getString(R.string.pref_auto_rotate_rotate));
            int rotateLockedResourceId;
            if (mControl.areAllRotationsAllowed()) {
                rotateLockedResourceId = R.string.pref_auto_rotate_stay_in_current;
            } else {
                if (mControl.getRotationLockOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                    rotateLockedResourceId = R.string.pref_auto_rotate_stay_in_portrait;
                } else {
                    rotateLockedResourceId = R.string.pref_auto_rotate_stay_in_landscape;
                }
            }
            items.add(getString(rotateLockedResourceId));
            mAutoRotate.setEntries(items.toArray(new CharSequence[items.size()]));
            mAutoRotate.setEntryValues(items.toArray(new CharSequence[items.size()]));
            mAutoRotate.setValueIndex(mControl.isRotationLocked() ? 1 : 0);
            mAutoRotate.setSummary(mAutoRotate.getValue());
            mAutoRotate.setOnPreferenceChangeListener(this);
        } else {
            mAutoRotate.setEnabled(false);
        }
        /* KEY_SCREEN_TIMEOUT */
        mScreenTimeout = findPreference(KEY_SCREEN_TIMEOUT);
        mScreenTimeout.setValue(String.valueOf(mControl.getScreenOffTimeout()));
        mScreenTimeout.setOnPreferenceChangeListener(this);
        onPreferenceChange(mScreenTimeout, mScreenTimeout.getValue());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean result = true;
        final String key = preference.getKey();
        if (KEY_BRIGHTNESS_LEVEL.equals(key)) {
            mControl.setScreenBrightness(false, (Integer) value);
            mBrightnessLevel.setTitle(getString(R.string.pref_title_brightness_level, (Integer) value, mMaxBrightnessLevel));
        } else if (KEY_AUTO_ROTATE.equals(key)) {
            mControl.setRotationLock(mAutoRotate.findIndexOfValue((String) value) == 1);
            mAutoRotate.setSummary((String) value);
        } else if (KEY_SCREEN_TIMEOUT.equals(key)) {
            int currentTimeout = Integer.parseInt((String) value);
            mControl.setScreenOffTimeout(currentTimeout);
            if (currentTimeout < -1) {
                mScreenTimeout.setSummary("");
            } else {
                final CharSequence[] entries = mScreenTimeout.getEntries();
                final CharSequence[] values = mScreenTimeout.getEntryValues();
                if (entries == null || entries.length == 0) {
                    mScreenTimeout.setSummary("");
                } else if(currentTimeout == -1){
                    mScreenTimeout.setSummary(entries[values.length-1].toString());
                } else {
                    int best = 0;
                    for (int i = 0; i < values.length; i++) {
                        long timeout = Long.parseLong(values[i].toString());
                        if (currentTimeout >= timeout && (timeout > 0)) { best = i; }
                    }
                    mScreenTimeout.setSummary(getString(R.string.pref_summary_screen_timeout, entries[best]));
                }
            }
        }
        return result;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_display);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindPreferenceChange();
    }

    @Override
    public void onPause() {
        super.onPause();
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