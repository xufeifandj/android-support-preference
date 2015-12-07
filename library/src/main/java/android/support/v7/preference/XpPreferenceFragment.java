package android.support.v7.preference;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import net.xpece.android.support.preference.XpEditTextPreference;
import net.xpece.android.support.preference.XpEditTextPreferenceDialogFragment;
import net.xpece.android.support.preference.XpMultiSelectListPreference;
import net.xpece.android.support.preference.XpMultiSelectListPreferenceDialogFragment;
import net.xpece.android.support.preference.XpSeekBarDialogPreference;
import net.xpece.android.support.preference.XpSeekBarPreferenceDialogFragment;

import java.lang.reflect.Field;

/**
 * @author Eugen on 6. 12. 2015.
 */
public abstract class XpPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = XpPreferenceFragment.class.getSimpleName();

    private static final Field FIELD_PREFERENCE_MANAGER;

    static {
        Field preferenceManager = null;
        try {
            preferenceManager = PreferenceFragmentCompat.class.getDeclaredField("mPreferenceManager");
            preferenceManager.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        FIELD_PREFERENCE_MANAGER = preferenceManager;
    }

    private void setPreferenceManager(PreferenceManager manager) {
        try {
            FIELD_PREFERENCE_MANAGER.set(this, manager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void onCreatePreferences(final Bundle bundle, final String s) {
        // Clear the original Preference Manager
        PreferenceManager manager = getPreferenceManager();
        manager.setOnNavigateToScreenListener(null);

        // Setup custom Preference Manager
        manager = new XpPreferenceManager(getStyledContext());
        setPreferenceManager(manager);
        manager.setOnNavigateToScreenListener(this);

        onCreatePreferences2(bundle, s);
    }

    public abstract void onCreatePreferences2(final Bundle bundle, final String s);

    private Context getStyledContext() {
        return getPreferenceManager().getContext();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        boolean handled = false;

        // This has to be done first. Doubled call in super :(
        if (this.getCallbackFragment() instanceof PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) {
            handled = ((PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) this.getCallbackFragment()).onPreferenceDisplayDialog(this, preference);
        }
        if (!handled && this.getActivity() instanceof PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) {
            handled = ((PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) this.getActivity()).onPreferenceDisplayDialog(this, preference);
        }

        // Handling custom preferences.
        if (!handled) {
            if (this.getFragmentManager().findFragmentByTag("android.support.v7.preference.PreferenceFragment.DIALOG") == null) {
                DialogFragment f;
                if (preference instanceof XpEditTextPreference) {
                    f = XpEditTextPreferenceDialogFragment.newInstance(preference.getKey());
//                } else if (preference instanceof XpListPreference) {
//                    f = ListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
                } else if (preference instanceof XpMultiSelectListPreference) {
                    f = XpMultiSelectListPreferenceDialogFragment.newInstance(preference.getKey());
                } else if (preference instanceof XpSeekBarDialogPreference) {
                    f = XpSeekBarPreferenceDialogFragment.newInstance(preference.getKey());
                } else {
                    super.onDisplayPreferenceDialog(preference);
                    return;
                }

                f.setTargetFragment(this, 0);
                f.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            }
        }
    }
}
