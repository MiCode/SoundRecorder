/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.soundrecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SoundRecorderPreferenceActivity extends PreferenceActivity {
    private static final String RECORD_TYPE = "pref_key_record_type";

    private static final String ENABLE_HIGH_QUALITY = "pref_key_enable_high_quality";

    private static final String ENABLE_SOUND_EFFECT = "pref_key_enable_sound_effect";

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
    }

    public static String getRecordType(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(RECORD_TYPE, context.getString(R.string.prefDefault_recordType));
    }

    public static boolean isHighQuality(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(ENABLE_HIGH_QUALITY, true);
    }

    public static boolean isEnabledSoundEffect(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(ENABLE_SOUND_EFFECT, true);
    }
}
