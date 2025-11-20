package device.sdk.sample.mdm;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.telephony.TelephonyManager;
import android.view.MenuItem;

import androidx.preference.DropDownPreference;
import androidx.preference.SeekBarPreference;

import device.common.DevInfoIndex;
import device.sdk.Control;

public class NotificationPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String KEY_MEDIA_VOLUME = "media_volume";
    private static final String KEY_ALARM_VOLUME = "alarm_volume";
    private static final String KEY_RING_VOLUME = "ring_volume";
    private static final String KEY_NOTIFICATION_VOLUME = "notification_volume";
    private static final String KEY_DIALPAD_TONES = "dialpad_tones";
    private static final String KEY_SCREEN_LOCKING_SOUNDS = "screen_locking_sounds";
    private static final String KEY_CHARGING_SOUNDS = "charging_sounds";
    private static final String KEY_DOCKING_SOUNDS = "docking_sounds";
    private static final String KEY_TOUCH_SOUNDS = "touch_sounds";
    private static final String KEY_VIBRATE_ON_TOUCH = "vibrate_on_touch";
    private static final String KEY_DOCK_AUDIO_MEDIA = "dock_audio_media";
    private static final String KEY_EMERGENCY_TONES = "emergency_tones";

    private SeekBarPreference mMediaVolume;
    private SeekBarPreference mAlarmVolume;
    private SeekBarPreference mRingVolume;
    private SeekBarPreference mNotificationVolume;
    private SwitchPreference mDialpadTones;
    private SwitchPreference mScreenLockingSounds;
    private SwitchPreference mChargningSounds;
    private SwitchPreference mDockingSounds;
    private SwitchPreference mTouchSounds;
    private SwitchPreference mVibrateOnTouch;
    private DropDownPreference mDockAudioMedia;
    private DropDownPreference mEmergencyTones;

    private int mMaxStreamMusic = 0;
    private int mMaxStreamAlarm = 0;
    private int mMaxStreamRing = 0;
    private int mMaxStreamNotification = 0;

    private static final Control mControl = new Control();

    private void bindPreferenceChange() {
        /* KEY_MEDIA_VOLUME */
        mMediaVolume = findPreference(KEY_MEDIA_VOLUME);
        mMaxStreamMusic = mControl.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mMediaVolume.setMax(mMaxStreamMusic);
        mMediaVolume.setValue(mControl.getStreamVolume(AudioManager.STREAM_MUSIC));
        mMediaVolume.setOnPreferenceChangeListener(this);
        onPreferenceChange(mMediaVolume, mMediaVolume.getValue());
        /* KEY_ALARM_VOLUME */
        mAlarmVolume = findPreference(KEY_ALARM_VOLUME);
        mMaxStreamAlarm = mControl.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        mAlarmVolume.setMax(mMaxStreamAlarm);
        mAlarmVolume.setValue(mControl.getStreamVolume(AudioManager.STREAM_ALARM));
        mAlarmVolume.setOnPreferenceChangeListener(this);
        onPreferenceChange(mAlarmVolume, mAlarmVolume.getValue());
        /* KEY_RING_VOLUME */
        mRingVolume = findPreference(KEY_RING_VOLUME);
        /* KEY_NOTIFICATION_VOLUME */
        mNotificationVolume = findPreference(KEY_NOTIFICATION_VOLUME);
        boolean isVoiceCapable = ((TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE)).isVoiceCapable();
        if (isVoiceCapable) {
            mMaxStreamRing = mControl.getStreamMaxVolume(AudioManager.STREAM_RING);
            mRingVolume.setMax(mMaxStreamRing);
            mRingVolume.setValue(mControl.getStreamVolume(AudioManager.STREAM_RING));
            mRingVolume.setOnPreferenceChangeListener(this);
            onPreferenceChange(mRingVolume, mRingVolume.getValue());
            mNotificationVolume.setTitle(getString(R.string.pref_title_notification_volume, 0, 0));
            mNotificationVolume.setEnabled(false);
        } else {
            mMaxStreamNotification = mControl.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            mNotificationVolume.setMax(mMaxStreamNotification);
            mNotificationVolume.setValue(mControl.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
            mNotificationVolume.setOnPreferenceChangeListener(this);
            onPreferenceChange(mNotificationVolume, mNotificationVolume.getValue());
            mRingVolume.setTitle(getString(R.string.pref_title_ring_volume, 0, 0));
            mRingVolume.setEnabled(false);
        }
        /* KEY_DIALPAD_TONES */
        mDialpadTones = findPreference(KEY_DIALPAD_TONES);
        int dialpadTones = mControl.getEnabledOtherSounds(Control.SOUND_DIALPAD);
        if (dialpadTones == DevInfoIndex.NOT_SUPPORTED) {
            mDialpadTones.setSummary(getString(R.string.pref_summary_not_supported));
            mDialpadTones.setEnabled(false);
        } else {
            mDialpadTones.setChecked((dialpadTones != 0));
            mDialpadTones.setOnPreferenceChangeListener(this);
            onPreferenceChange(mDialpadTones, mDialpadTones.isChecked());
        }
        /* KEY_SCREEN_LOCKING_SOUNDS */
        mScreenLockingSounds = findPreference(KEY_SCREEN_LOCKING_SOUNDS);
        int screenLockingSounds = mControl.getEnabledOtherSounds(Control.SOUND_SCREEN_LOCKING);
        if (screenLockingSounds == DevInfoIndex.NOT_SUPPORTED) {
            mScreenLockingSounds.setSummary(getString(R.string.pref_summary_not_supported));
            mScreenLockingSounds.setEnabled(false);
        } else {
            mScreenLockingSounds.setChecked((screenLockingSounds != 0));
            mScreenLockingSounds.setOnPreferenceChangeListener(this);
            onPreferenceChange(mScreenLockingSounds, mScreenLockingSounds.isChecked());
        }
        /* KEY_CHARGING_SOUNDS */
        mChargningSounds = findPreference(KEY_CHARGING_SOUNDS);
        int chargingSounds = mControl.getEnabledOtherSounds(Control.SOUND_CHARGING);
        if (chargingSounds == DevInfoIndex.NOT_SUPPORTED) {
            mChargningSounds.setSummary(getString(R.string.pref_summary_not_supported));
            mChargningSounds.setEnabled(false);
        } else {
            mChargningSounds.setChecked((chargingSounds != 0));
            mChargningSounds.setOnPreferenceChangeListener(this);
            onPreferenceChange(mChargningSounds, mChargningSounds.isChecked());
        }
        /* KEY_DOCKING_SOUNDS */
        mDockingSounds = findPreference(KEY_DOCKING_SOUNDS);
        int dockingSounds = mControl.getEnabledOtherSounds(Control.SOUND_DOCKING);
        if (dockingSounds == DevInfoIndex.NOT_SUPPORTED) {
            mDockingSounds.setSummary(getString(R.string.pref_summary_not_supported));
            mDockingSounds.setEnabled(false);
        } else {
            mDockingSounds.setChecked((dockingSounds != 0));
            mDockingSounds.setOnPreferenceChangeListener(this);
            onPreferenceChange(mDockingSounds, mDockingSounds.isChecked());
        }
        /* KEY_TOUCH_SOUNDS */
        mTouchSounds = findPreference(KEY_TOUCH_SOUNDS);
        int touchSounds = mControl.getEnabledOtherSounds(Control.SOUND_TOUCH);
        if (touchSounds == DevInfoIndex.NOT_SUPPORTED) {
            mTouchSounds.setSummary(getString(R.string.pref_summary_not_supported));
            mTouchSounds.setEnabled(false);
        } else {
            mTouchSounds.setChecked((touchSounds != 0));
            mTouchSounds.setOnPreferenceChangeListener(this);
            onPreferenceChange(mTouchSounds, mTouchSounds.isChecked());
        }
        /* KEY_VIBRATE_ON_TOUCH */
        mVibrateOnTouch = findPreference(KEY_VIBRATE_ON_TOUCH);
        int vibrateOnTouch = mControl.getEnabledOtherSounds(Control.SOUND_VIBRATE_ON_TOUCH);
        if (vibrateOnTouch == DevInfoIndex.NOT_SUPPORTED) {
            mVibrateOnTouch.setSummary(getString(R.string.pref_summary_not_supported));
            mVibrateOnTouch.setEnabled(false);
        } else {
            mVibrateOnTouch.setChecked((vibrateOnTouch != 0));
            mVibrateOnTouch.setOnPreferenceChangeListener(this);
            onPreferenceChange(mVibrateOnTouch, mVibrateOnTouch.isChecked());
        }
        /* KEY_DOCK_AUDIO_MEDIA */
        mDockAudioMedia = findPreference(KEY_DOCK_AUDIO_MEDIA);
        int dockAudioMedia = mControl.getEnabledOtherSounds(Control.SOUND_DOCK_AUDIO_MEDIA);
        if (dockAudioMedia == DevInfoIndex.NOT_SUPPORTED) {
            mDockAudioMedia.setEnabled(false);
        } else {
            mDockAudioMedia.setValueIndex(dockAudioMedia);
            mDockAudioMedia.setOnPreferenceChangeListener(this);
        }
        /* KEY_EMERGENCY_TONES */
        mEmergencyTones = findPreference(KEY_EMERGENCY_TONES);
        int emergencyTones = mControl.getEnabledOtherSounds(Control.SOUND_EMERGENCY);
        if (emergencyTones == DevInfoIndex.NOT_SUPPORTED) {
            mEmergencyTones.setEnabled(false);
        } else {
            mEmergencyTones.setValueIndex(emergencyTones);
            mEmergencyTones.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean result = true;
        final String key = preference.getKey();
        if (KEY_MEDIA_VOLUME.equals(key)) {
            mControl.setStreamVolume(AudioManager.STREAM_MUSIC, (Integer) value, 0);
            mMediaVolume.setTitle(getString(R.string.pref_title_media_volume, (Integer) value, mMaxStreamMusic));
        } else if (KEY_ALARM_VOLUME.equals(key)) {
            mControl.setStreamVolume(AudioManager.STREAM_ALARM, (Integer) value, 0);
            mAlarmVolume.setTitle(getString(R.string.pref_title_alarm_volume, (Integer) value, mMaxStreamAlarm));
        } else if (KEY_RING_VOLUME.equals(key)) {
            mControl.setStreamVolume(AudioManager.STREAM_RING, (Integer) value, 0);
            mRingVolume.setTitle(getString(R.string.pref_title_ring_volume, (Integer) value, mMaxStreamRing));
        } else if (KEY_NOTIFICATION_VOLUME.equals(key)) {
            mControl.setStreamVolume(AudioManager.STREAM_NOTIFICATION, (Integer) value, 0);
            mNotificationVolume.setTitle(getString(R.string.pref_title_notification_volume, (Integer) value, mMaxStreamNotification));
        } else if (KEY_DIALPAD_TONES.equals(key)) {
            mControl.setEnabledOtherSounds(Control.SOUND_DIALPAD, (Boolean) value ? 1 : 0);
            mDialpadTones.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_SCREEN_LOCKING_SOUNDS.equals(key)) {
            mControl.setEnabledOtherSounds(Control.SOUND_SCREEN_LOCKING, (Boolean) value ? 1 : 0);
            mScreenLockingSounds.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_CHARGING_SOUNDS.equals(key)) {
            mControl.setEnabledOtherSounds(Control.SOUND_CHARGING, (Boolean) value ? 1 : 0);
            mChargningSounds.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_DOCKING_SOUNDS.equals(key)) {
            mControl.setEnabledOtherSounds(Control.SOUND_DOCKING, (Boolean) value ? 1 : 0);
            mDockingSounds.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_TOUCH_SOUNDS.equals(key)) {
            mControl.setEnabledOtherSounds(Control.SOUND_TOUCH, (Boolean) value ? 1 : 0);
            mTouchSounds.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_VIBRATE_ON_TOUCH.equals(key)) {
            mControl.setEnabledOtherSounds(Control.SOUND_VIBRATE_ON_TOUCH, (Boolean) value ? 1 : 0);
            mVibrateOnTouch.setSummary(getString(R.string.pref_summary_boolean_param, (Boolean) value));
        } else if (KEY_DOCK_AUDIO_MEDIA.equals(key)) {
            mControl.setEnabledOtherSounds(Control.SOUND_DOCK_AUDIO_MEDIA, (Integer) value);
        } else if (KEY_EMERGENCY_TONES.equals(key)) {
            mControl.setEnabledOtherSounds(Control.SOUND_EMERGENCY, (Integer) value);
        }
        return result;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_notification);
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