package com.dastanapps.mediasdk.opengles.encoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaAudioEncoder extends MediaEncoder {
    private static final String TAG = "MediaAudioEncoder";

    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 44100;    // 44.1[KHz] is only setting guaranteed to be available on all devices.
    private static final int BIT_RATE = 96000;
    public static final int SAMPLES_PER_FRAME = 1024;    // AAC, bytes/frame/channel

    private AudioThread mAudioThread = null;

    public MediaAudioEncoder(final MediaMuxerWrapper muxer, final MediaEncoderListener listener) {
        super(muxer, listener);
    }

    @Override
    protected void prepare(VideoProfile profile) throws IOException {
        Log.v(TAG, "prepare:");
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;
        // prepare MediaCodec for AAC encoding of audio data from inernal mic.
        final MediaCodecInfo audioCodecInfo = selectAudioCodec(MIME_TYPE);
        if (audioCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        Log.i(TAG, "selected codec: " + audioCodecInfo.getName());

        final MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, profile.audioSampleRate, profile.audioChannels);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_STEREO);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, profile.audioBitRate);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, profile.audioChannels);
//		audioFormat.setLong(MediaFormat.KEY_MAX_INPUT_SIZE, inputFile.length());
//      audioFormat.setLong(MediaFormat.KEY_DURATION, (long)durationInMs );
        Log.i(TAG, "format: " + audioFormat);


        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        Log.i(TAG, "prepare finishing");
        if (mListener != null) {
            try {
                mListener.onPrepared(this);
            } catch (final Exception e) {
                Log.e(TAG, "prepare:", e);
            }
        }
    }

    @Override
    protected void startRecording() {
        super.startRecording();
        // create and execute audio capturing thread using internal mic
        if (mAudioThread == null) {
            mAudioThread = new AudioThread();
            mAudioThread.start();
        }
    }

    @Override
    protected void release() {
        mAudioThread = null;
        super.release();
    }

    private static final int[] AUDIO_SOURCES = new int[]{
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };

    /**
     * Thread to capture audio data from internal mic as uncompressed 16bit PCM data
     * and write them to the MediaCodec encoder
     */
    private class AudioThread extends Thread {
        private AudioRecord findAudioRecord() {
            int[] samplingRates = new int[]{44100, 22050, 11025, 8000};
            int[] audioFormats = new int[]{
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioFormat.ENCODING_PCM_8BIT};
            int[] channelConfigs = new int[]{
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.CHANNEL_IN_MONO};

            for (int rate : samplingRates) {
                for (int format : audioFormats) {
                    for (int config : channelConfigs) {
                        try {
                            int bufferSize = AudioRecord.getMinBufferSize(rate, config, format);
                            if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                                for (int source : AUDIO_SOURCES) {
                                    AudioRecord recorder = new AudioRecord(source, rate, config, format, bufferSize * 4);
                                    if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                        return recorder;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Init AudioRecord Error." + Log.getStackTraceString(e));
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            try {

                AudioRecord audioRecord = findAudioRecord();
                if (audioRecord != null) {
                    try {
                        if (mIsCapturing) {
                            Log.v(TAG, "AudioThread:start audio recording");
                            final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
                            int readBytes;
                            audioRecord.startRecording();
                            try {
                                for (; mIsCapturing && !mRequestStop && !mIsEOS; ) {
                                    // read audio data from internal mic
                                    buf.clear();
                                    readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME);
                                    if (readBytes > 0) {
                                        // set audio data to encoder
                                        buf.position(readBytes);
                                        buf.flip();
                                        encode(buf, readBytes, getPTSUs());
                                        frameAvailableSoon();
                                    }
                                }
                                frameAvailableSoon();
                            } finally {
                                audioRecord.stop();
                            }
                        }
                    } finally {
                        audioRecord.release();
                    }
                } else {
                    Log.e(TAG, "failed to initialize AudioRecord");
                }
            } catch (final Exception e) {
                Log.e(TAG, "AudioThread#run", e);
            }
            Log.v(TAG, "AudioThread:finished");
        }
    }

    /**
     * select the first codec that match a specific MIME type
     *
     * @param mimeType
     * @return
     */
    private static final MediaCodecInfo selectAudioCodec(final String mimeType) {
        Log.v(TAG, "selectAudioCodec:");
        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                Log.i(TAG, "supportedType:" + codecInfo.getName() + ",MIME=" + type);
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

}
