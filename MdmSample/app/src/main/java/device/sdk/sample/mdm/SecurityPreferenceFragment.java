package device.sdk.sample.mdm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.view.MenuItem;

import device.sdk.Control;

public class SecurityPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String KEY_ENABLED_SCREEN_CAPTURE = "enabled_screen_capture";
    private static final String KEY_LOCK_UNKNOWN_SOURCES_OPTION = "lock_unknown_sources_option";
    private static final String KEY_UNKNOWN_SOURCES = "unknown_sources";
    private static final String KEY_DISALLOW_UNKNOWN_SOURCES = "disallow_unknown_sources";
    private static final String KEY_DISALLOW_CAMERA = "disallow_camera";
    private static final String KEY_DISALLOW_FACTORY_RESET = "disallow_factory_reset";
    private static final String KEY_FACTORY_RESET = "factory_reset";

    private static final String DISALLOW_CAMERA = "no_camera";

    private SwitchPreference mEnabledScreenCapture;
    private SwitchPreference mLockUnknownSourcesOption;
    private SwitchPreference mUnknownSources;
    private SwitchPreference mDisallowUnknownSources;
    private SwitchPreference mDisallowCamera;
    private SwitchPreference mDisallowFactoryReset;
    private Preference mFactoryReset;

    private static final Control mControl = new Control();

    private void bindPreferenceChange() {
        /* KEY_ENABLED_SCREEN_CAPTURE */
        mEnabledScreenCapture = findPreference(KEY_ENABLED_SCREEN_CAPTURE);
        mEnabledScreenCapture.setChecked(mControl.isEnabledScreenCapture());
        mEnabledScreenCapture.setOnPreferenceChangeListener(this);
        onPreferenceChange(mEnabledScreenCapture, mEnabledScreenCapture.isChecked());
        /* KEY_UNKNOWN_SOURCES */
        mUnknownSources = findPreference(KEY_UNKNOWN_SOURCES);
        mUnknownSources.setChecked(mControl.isNonMarketAppsAllowed());
        mUnknownSources.setOnPreferenceChangeListener(this);
        onPreferenceChange(mUnknownSources, mUnknownSources.isChecked());
        /* KEY_DISALLOW_UNKNOWN_SOURCES */
        mDisallowUnknownSources = findPreference(KEY_DISALLOW_UNKNOWN_SOURCES);
        mDisallowUnknownSources.setChecked(mControl.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES));
        mDisallowUnknownSources.setOnPreferenceChangeListener(this);
        onPreferenceChange(mDisallowUnknownSources, mDisallowUnknownSources.isChecked());
        /* KEY_LOCK_UNKNOWN_SOURCES_OPTION */
        mLockUnknownSourcesOption = findPreference(KEY_LOCK_UNKNOWN_SOURCES_OPTION);
        mLockUnknownSourcesOption.setChecked(mControl.isNonMarketAppsAllowedOption());
        mLockUnknownSourcesOption.setOnPreferenceChangeListener(this);
        onPreferenceChange(mLockUnknownSourcesOption, mLockUnknownSourcesOption.isChecked());
        /* KEY_DISALLOW_CAMERA */
        mDisallowCamera = findPreference(KEY_DISALLOW_CAMERA);
        mDisallowCamera.setChecked(mControl.hasUserRestriction(DISALLOW_CAMERA));
        mDisallowCamera.setOnPreferenceChangeListener(this);
        onPreferenceChange(mDisallowCamera, mDisallowCamera.isChecked());
        /* KEY_DISALLOW_FACTORY_RESET */
        mDisallowFactoryReset = findPreference(KEY_DISALLOW_FACTORY_RESET);
        mDisallowFactoryReset.setChecked(mControl.hasUserRestriction(UserManager.DISALLOW_FACTORY_RESET));
        mDisallowFactoryReset.setOnPreferenceChangeListener(this);
        onPreferenceChange(mDisallowFactoryReset, mDisallowFactoryReset.isChecked());
        /* KEY_DISALLOW_FACTORY_RESET */
        mFactoryReset = findPreference(KEY_FACTORY_RESET);
        mFactoryReset.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean result = true;
        final String key = preference.getKey();
        if (KEY_ENABLED_SCREEN_CAPTURE.equals(key)) {
            mControl.setEnabledScreenCapture((Boolean) value);
            mEnabledScreenCapture.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_LOCK_UNKNOWN_SOURCES_OPTION.equals(key)) {
            mControl.setNonMarketAppsAllowedOption((Boolean) value);
            mLockUnknownSourcesOption.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
            boolean enable = mControl.isNonMarketAppsAllowedOption() && !mControl.isRestricted() &&
                    !mControl.hasUserRestriction(UserManager.DISALLOW_INSTALL_APPS);
            mUnknownSources.setEnabled(enable);
        } else if (KEY_UNKNOWN_SOURCES.equals(key)) {
            mControl.setNonMarketAppsAllowed((Boolean) value);
            mUnknownSources.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_DISALLOW_UNKNOWN_SOURCES.equals(key)) {
            mControl.setUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, (Boolean) value);
            mDisallowUnknownSources.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
            boolean enable = mControl.isNonMarketAppsAllowedOption() && !mControl.isRestricted() &&
                    !mControl.hasUserRestriction(UserManager.DISALLOW_INSTALL_APPS);
            mUnknownSources.setEnabled(enable);
        } else if (KEY_DISALLOW_CAMERA.equals(key)) {
            mControl.setUserRestriction(DISALLOW_CAMERA, (Boolean) value);
            mDisallowCamera.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_DISALLOW_FACTORY_RESET.equals(key)) {
            mControl.setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, (Boolean) value);
            mDisallowFactoryReset.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        }
        return result;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mFactoryReset) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getString(R.string.pref_message_factory_reset));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mControl.doMasterClear(true, true);
                }
            });
            builder.show();
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_security);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindPreferenceChange();
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