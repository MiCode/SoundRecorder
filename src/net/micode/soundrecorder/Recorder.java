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

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;

public class Recorder implements OnCompletionListener, OnErrorListener {
    private static final String SAMPLE_PREFIX = "recording";

    private static final String SAMPLE_PATH_KEY = "sample_path";

    private static final String SAMPLE_LENGTH_KEY = "sample_length";

    public static final String SAMPLE_DEFAULT_DIR = "/sound_recorder";

    public static final int IDLE_STATE = 0;

    public static final int RECORDING_STATE = 1;

    public static final int PLAYING_STATE = 2;

    public static final int PLAYING_PAUSED_STATE = 3;

    private int mState = IDLE_STATE;

    public static final int NO_ERROR = 0;

    public static final int STORAGE_ACCESS_ERROR = 1;

    public static final int INTERNAL_ERROR = 2;

    public static final int IN_CALL_RECORD_ERROR = 3;

    public interface OnStateChangedListener {
        public void onStateChanged(int state);

        public void onError(int error);
    }

    private Context mContext;

    private OnStateChangedListener mOnStateChangedListener = null;

    private long mSampleStart = 0; // time at which latest record or play
                                   // operation started

    private int mSampleLength = 0; // length of current sample

    private File mSampleFile = null;

    private File mSampleDir = null;

    private MediaPlayer mPlayer = null;

    public Recorder(Context context) {
        mContext = context;
        File sampleDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + SAMPLE_DEFAULT_DIR);
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        mSampleDir = sampleDir;

        syncStateWithService();
    }

    public boolean syncStateWithService() {
        if (RecorderService.isRecording()) {
            mState = RECORDING_STATE;
            mSampleStart = RecorderService.getStartTime();
            mSampleFile = new File(RecorderService.getFilePath());
            return true;
        } else if (mState == RECORDING_STATE) {
            // service is idle but local state is recording
            return false;
        } else if (mSampleFile != null && mSampleLength == 0) {
            // this state can be reached if there is an incoming call
            // the record service is stopped by incoming call without notifying
            // the UI
            return false;
        }
        return true;
    }

    public void saveState(Bundle recorderState) {
        recorderState.putString(SAMPLE_PATH_KEY, mSampleFile.getAbsolutePath());
        recorderState.putInt(SAMPLE_LENGTH_KEY, mSampleLength);
    }

    public String getRecordDir() {
        return mSampleDir.getAbsolutePath();
    }

    public int getMaxAmplitude() {
        if (mState != RECORDING_STATE)
            return 0;
        return RecorderService.getMaxAmplitude();
    }

    public void restoreState(Bundle recorderState) {
        String samplePath = recorderState.getString(SAMPLE_PATH_KEY);
        if (samplePath == null)
            return;
        int sampleLength = recorderState.getInt(SAMPLE_LENGTH_KEY, -1);
        if (sampleLength == -1)
            return;

        File file = new File(samplePath);
        if (!file.exists())
            return;
        if (mSampleFile != null
                && mSampleFile.getAbsolutePath().compareTo(file.getAbsolutePath()) == 0)
            return;

        delete();
        mSampleFile = file;
        mSampleLength = sampleLength;

        signalStateChanged(IDLE_STATE);
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }

    public int state() {
        return mState;
    }

    public int progress() {
        if (mState == RECORDING_STATE) {
            return (int) ((System.currentTimeMillis() - mSampleStart) / 1000);
        } else if (mState == PLAYING_STATE || mState == PLAYING_PAUSED_STATE) {
            if (mPlayer != null) {
                return (int) (mPlayer.getCurrentPosition() / 1000);
            }
        }

        return 0;
    }

    public float playProgress() {
        if (mPlayer != null) {
            return ((float) mPlayer.getCurrentPosition()) / mPlayer.getDuration();
        }
        return 0.0f;
    }

    public int sampleLength() {
        return mSampleLength;
    }

    public File sampleFile() {
        return mSampleFile;
    }

    public void renameSampleFile(String name) {
        if (mSampleFile != null && mState != RECORDING_STATE && mState != PLAYING_STATE) {
            if (!TextUtils.isEmpty(name)) {
                String oldName = mSampleFile.getAbsolutePath();
                String extension = oldName.substring(oldName.lastIndexOf('.'));
                File newFile = new File(mSampleFile.getParent() + "/" + name + extension);
                if (!TextUtils.equals(oldName, newFile.getAbsolutePath())) {
                    if (mSampleFile.renameTo(newFile)) {
                        mSampleFile = newFile;
                    }
                }
            }
        }
    }

    /**
     * Resets the recorder state. If a sample was recorded, the file is deleted.
     */
    public void delete() {
        stop();

        if (mSampleFile != null)
            mSampleFile.delete();

        mSampleFile = null;
        mSampleLength = 0;

        signalStateChanged(IDLE_STATE);
    }

    /**
     * Resets the recorder state. If a sample was recorded, the file is left on
     * disk and will be reused for a new recording.
     */
    public void clear() {
        stop();
        mSampleLength = 0;
        signalStateChanged(IDLE_STATE);
    }

    public void reset() {
        stop();

        mSampleLength = 0;
        mSampleFile = null;
        mState = IDLE_STATE;

        File sampleDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + SAMPLE_DEFAULT_DIR);
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        mSampleDir = sampleDir;

        signalStateChanged(IDLE_STATE);
    }

    public boolean isRecordExisted(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(mSampleDir.getAbsolutePath() + "/" + path);
            return file.exists();
        }
        return false;
    }

    public void startRecording(int outputfileformat, String name, String extension,
            boolean highQuality, long maxFileSize) {
        stop();

        if (mSampleFile == null) {
            try {
                mSampleFile = File.createTempFile(SAMPLE_PREFIX, extension, mSampleDir);
                renameSampleFile(name);
            } catch (IOException e) {
                setError(STORAGE_ACCESS_ERROR);
                return;
            }
        }

        RecorderService.startRecording(mContext, outputfileformat, mSampleFile.getAbsolutePath(),
                highQuality, maxFileSize);
        mSampleStart = System.currentTimeMillis();
    }

    public void stopRecording() {
        if (RecorderService.isRecording()) {
            RecorderService.stopRecording(mContext);
            mSampleLength = (int) ((System.currentTimeMillis() - mSampleStart) / 1000);
            if (mSampleLength == 0) {
                // round up to 1 second if it's too short
                mSampleLength = 1;
            }
        }
    }

    public void startPlayback(float percentage) {
        if (state() == PLAYING_PAUSED_STATE) {
            mSampleStart = System.currentTimeMillis() - mPlayer.getCurrentPosition();
            mPlayer.seekTo((int) (percentage * mPlayer.getDuration()));
            mPlayer.start();
            setState(PLAYING_STATE);
        } else {
            stop();

            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(mSampleFile.getAbsolutePath());
                mPlayer.setOnCompletionListener(this);
                mPlayer.setOnErrorListener(this);
                mPlayer.prepare();
                mPlayer.seekTo((int) (percentage * mPlayer.getDuration()));
                mPlayer.start();
            } catch (IllegalArgumentException e) {
                setError(INTERNAL_ERROR);
                mPlayer = null;
                return;
            } catch (IOException e) {
                setError(STORAGE_ACCESS_ERROR);
                mPlayer = null;
                return;
            }

            mSampleStart = System.currentTimeMillis();
            setState(PLAYING_STATE);
        }
    }

    public void pausePlayback() {
        if (mPlayer == null) {
            return;
        }

        mPlayer.pause();
        setState(PLAYING_PAUSED_STATE);
    }

    public void stopPlayback() {
        if (mPlayer == null) // we were not in playback
            return;

        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        setState(IDLE_STATE);
    }

    public void stop() {
        stopRecording();
        stopPlayback();
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        stop();
        setError(STORAGE_ACCESS_ERROR);
        return true;
    }

    public void onCompletion(MediaPlayer mp) {
        stop();
    }

    public void setState(int state) {
        if (state == mState)
            return;

        mState = state;
        signalStateChanged(mState);
    }

    private void signalStateChanged(int state) {
        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onStateChanged(state);
    }

    public void setError(int error) {
        if (mOnStateChangedListener != null)
            mOnStateChangedListener.onError(error);
    }
}
