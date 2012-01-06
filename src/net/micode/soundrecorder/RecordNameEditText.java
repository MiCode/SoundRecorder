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
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RecordNameEditText extends EditText {

    private Context mContext;

    private InputMethodManager mInputMethodManager;

    private OnNameChangeListener mNameChangeListener;

    private String mDir;

    private String mExtension;

    private String mOriginalName;

    public interface OnNameChangeListener {

        void onNameChanged(String name);
    }

    public RecordNameEditText(Context context) {
        super(context, null);
        mContext = context;
        mInputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mNameChangeListener = null;
    }

    public RecordNameEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mInputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mNameChangeListener = null;
    }

    public RecordNameEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mInputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mNameChangeListener = null;
    }

    public void setNameChangeListener(OnNameChangeListener listener) {
        mNameChangeListener = listener;
    }

    public void initFileName(String dir, String extension, boolean englishOnly) {
        mDir = dir;
        mExtension = extension;

        // initialize the default name
        if (!englishOnly) {
            setText(getProperFileName(mContext.getString(R.string.default_record_name)));
        } else {
            SimpleDateFormat dataFormat = new SimpleDateFormat("MMddHHmmss");
            setText(getProperFileName("rec_" + dataFormat.format(Calendar.getInstance().getTime())));
        }
    }

    private String getProperFileName(String name) {
        String uniqueName = name;

        if (isFileExisted(uniqueName)) {
            int i = 2;
            while (true) {
                String temp = uniqueName + "(" + i + ")";
                if (!isFileExisted(temp)) {
                    uniqueName = temp;
                    break;
                }
                i++;
            }
        }
        return uniqueName;
    }

    private boolean isFileExisted(String name) {
        String fullName = mDir + "/" + name.trim() + mExtension;
        File file = new File(fullName);
        return file.exists();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                if (mNameChangeListener != null) {
                    String name = getText().toString().trim();
                    if (!TextUtils.isEmpty(name)) {
                        // use new name
                        setText(name);
                        mNameChangeListener.onNameChanged(name);

                    } else {
                        // use original name
                        setText(mOriginalName);
                    }
                    clearFocus();

                    // hide the keyboard
                    mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!focused && mNameChangeListener != null) {
            String name = getText().toString().trim();
            if (!TextUtils.isEmpty(name)) {
                // use new name
                setText(name);
                mNameChangeListener.onNameChanged(name);

            } else {
                // use original name
                setText(mOriginalName);
            }

            // hide the keyboard
            mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        } else if (focused) {
            mOriginalName = getText().toString();
        }
    }
}
