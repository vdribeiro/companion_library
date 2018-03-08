package companion.support.v8.media;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.media.AudioFormat;
import android.media.AudioRecord;

import companion.support.v8.util.ArraysUtils;
import companion.support.v8.util.LogHelper;

/**
 * Wav recorder.
 * 
 * @author Vitor Ribeiro
 *
 */
public class WavAudioRecorder {

	/** Log tag. */
	protected final static String TAG = WavAudioRecorder.class.getSimpleName();

	/**
	 * INITIALIZING : recorder is initializing;
	 * READY : recorder has been initialized, recorder not yet started
	 * RECORDING : recording
	 * ERROR : reconstruction needed
	 * STOPPED: reset needed
	 */
	public enum State {INITIALIZING, READY, RECORDING, ERROR, STOPPED}

	/** Recorder state. */
	public State state = State.ERROR;

	/** Recorder. */
	public AudioRecord audioRecorder = null;

	/** Number of channels. */
	public short numberOfChannels = 1;
	/** Sample size in bits. */
	public short sampleSize = 16;

	/** Current amplitude. */
	public int currentAmplitude = 0;
	/** Number of samples recorded so far. */
	public int averageCount = 0;
	/** Average signal power. */
	public double averageSignalPower = 0;

	/** Output file path. */
	private String filePath = null;
	/** Number of frames written to file on each output. */
	private int framePeriod = 0;
	/** Number of bytes written to file after header. */
	private int payloadSize = 0;
	/** Buffer for output. */
	private byte[] buffer = null;
	/** File writer. */
	private RandomAccessFile randomAccessWriter = null;

	public WavAudioRecorder(AudioRecord audioRecord, String filePath) {
		super();
		this.filePath = filePath;

		if (audioRecord==null) {
			return;
		}
		this.audioRecorder = audioRecord;

		if (audioRecorder.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) {
			sampleSize = 8;
		}
		if (audioRecorder.getChannelConfiguration() == AudioFormat.CHANNEL_IN_STEREO) {
			numberOfChannels = 2;
		}

		int bufferSize = AudioRecord.getMinBufferSize(
			audioRecorder.getSampleRate(), 
			audioRecorder.getChannelConfiguration(), 
			audioRecorder.getAudioFormat()
		);
		framePeriod = bufferSize / ( 2 * sampleSize * numberOfChannels / 8 );

		audioRecorder.setRecordPositionUpdateListener(updateListener);
		audioRecorder.setPositionNotificationPeriod(framePeriod);

		state = State.INITIALIZING;
	}

	private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
		@Override
		public void onPeriodicNotification(AudioRecord recorder) {
			if (audioRecorder==null) {
				return;
			}
			
			// Fill buffer
			int bufferResult = audioRecorder.read(buffer, 0, buffer.length);

			// Get signal power
			try {
				short[] shortBuffer = new short[buffer.length/2];
				ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortBuffer);
				double signalPower = MediaUtils.getSignalPower(ArraysUtils.copyOf(shortBuffer, bufferResult/2));
				averageSignalPower = (averageSignalPower * averageCount + signalPower) / (averageCount + 1);
				averageCount++;
			} catch (Exception e) {
				LogHelper.e(TAG, "Cannot get signal power", e);
			}

			// Write buffer to file
			try { 
				randomAccessWriter.write(buffer);
				payloadSize += buffer.length;
				if (sampleSize == 16) {
					// 16bit sample size
					for (int i=0; i<buffer.length/2; i++) { 
						short curSample = getShort(buffer[i*2], buffer[i*2+1]);
						if (curSample > currentAmplitude) { 
							// Check amplitude
							currentAmplitude = curSample;
						}
					}
				} else { 
					// 8bit sample size
					for (byte aBuffer : buffer) {
						if (aBuffer > currentAmplitude) {
							// Check amplitude
							currentAmplitude = aBuffer;
						}
					}
				}
			} catch (Exception e) {
				LogHelper.e(TAG, "Error occured in updateListener", e);
			}
		}

		@Override
		public void onMarkerReached(AudioRecord recorder) {
			// Ignore
		}
	};

	/**
	 * Returns the largest amplitude sampled since the last call to this method.
	 * @return returns the largest amplitude since the last call, or 0 when not in recording state. 
	 * 
	 */
	public int getMaxAmplitude() {
		if (state == State.RECORDING) {
			int result = currentAmplitude;
			currentAmplitude = 0;
			return result;
		} else {
			return 0;
		}
	}

	/**
	 * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state and the file path was not set
	 * the recorder is set to the ERROR state, which makes a reconstruction necessary.
	 * In case of an exception, the state is changed to ERROR.
	 */
	public void prepare() {
		try {
			if (state == State.INITIALIZING) {
				if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) & (filePath != null)) {
					// write file header as per http://soundfile.sapp.org/doc/WaveFormat/
					randomAccessWriter = new RandomAccessFile(filePath, "rw");
					// Set file length to 0, to prevent unexpected behavior in case the file already existed
					randomAccessWriter.setLength(0); 
					// RIFF header
					randomAccessWriter.writeBytes("RIFF");
					// Final file size not known yet, write 0 
					randomAccessWriter.writeInt(0); 
					// WAVE header
					randomAccessWriter.writeBytes("WAVE");
					// 'fmt ' chunk
					randomAccessWriter.writeBytes("fmt ");
					// Sub-chunk size, 16 for PCM
					randomAccessWriter.writeInt(Integer.reverseBytes(16));
					// AudioFormat, 1 for PCM
					randomAccessWriter.writeShort(Short.reverseBytes((short) 1));
					// Number of channels, 1 for mono, 2 for stereo
					randomAccessWriter.writeShort(Short.reverseBytes(numberOfChannels));
					// Sample rate
					randomAccessWriter.writeInt(Integer.reverseBytes(audioRecorder.getSampleRate()));
					// Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
					randomAccessWriter.writeInt(Integer.reverseBytes(audioRecorder.getSampleRate()*numberOfChannels*sampleSize/8));
					// Block align, NumberOfChannels*BitsPerSample/8
					randomAccessWriter.writeShort(Short.reverseBytes((short)(numberOfChannels*sampleSize/8))); 
					// Bits per sample
					randomAccessWriter.writeShort(Short.reverseBytes(sampleSize));
					// 'data' sub chunk
					randomAccessWriter.writeBytes("data");
					// Data chunk size not known yet, write 0
					randomAccessWriter.writeInt(0); 

					buffer = new byte[framePeriod*sampleSize/8*numberOfChannels];
					state = State.READY;
				} else {
					LogHelper.e(TAG, "prepare() method called on uninitialized recorder");
					state = State.ERROR;
				}
			} else {
				LogHelper.e(TAG, "prepare() method called on illegal state");
				release();
				state = State.ERROR;
			}
		} catch(Exception e) {
			LogHelper.e(TAG, "Unknown error occured in prepare()", e);
			state = State.ERROR;
		}
	}

	/**
	 * Starts the recording, and sets the state to RECORDING.
	 * Call after prepare().
	 */
	public void start() {
		if (audioRecorder != null && state == State.READY) {
			averageSignalPower = 0;
			averageCount = 0;
			payloadSize = 0;
			audioRecorder.startRecording();
			audioRecorder.read(buffer, 0, buffer.length);

			state = State.RECORDING;
		} else {
			LogHelper.e(TAG, "start() called on illegal state");
			state = State.ERROR;
		}
	}

	/**
	 * Stops the recording, and sets the state to STOPPED.
	 * In case of further usage, a reset is needed.
	 * Also finalizes the wave file.
	 */
	public void stop() {
		if (audioRecorder != null && state == State.RECORDING) {
			audioRecorder.stop();
			state = State.STOPPED;

			try {
				// Write size to RIFF header
				randomAccessWriter.seek(4); 
				randomAccessWriter.writeInt(Integer.reverseBytes(36+payloadSize));

				// Write size to Subchunk2Size field
				randomAccessWriter.seek(40); 
				randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));

				randomAccessWriter.close();
			} catch(Exception e) {
				LogHelper.e(TAG, "Exception occured while closing output file");
				state = State.ERROR;
			}
		} else {
			LogHelper.e(TAG, "stop() called on illegal state");
			state = State.ERROR;
		}
	}

	/**
	 * Releases the resources associated with this class, 
	 * and removes the unnecessary files, when necessary.
	 */
	public void release() {
		if (state == State.RECORDING) {
			stop();
		} else {
			if ((state == State.READY)) {
				try {
					// Remove prepared file
					randomAccessWriter.close(); 
				} catch (Exception e) {
					LogHelper.e(TAG, "Exception occured while closing output file");
				}
				(new File(filePath)).delete();
			}
		}

		if (audioRecorder != null) {
			audioRecorder.release();
		}
	}

	/**
	 * Resets the recorder to the INITIALIZING state, as if it was just created.
	 * In case the class was in RECORDING state, the recording is stopped.
	 * In case of exceptions the class is set to the ERROR state.
	 */
	public void reset() {
		try {
			if (state != State.ERROR) {
				release();
				currentAmplitude = 0;

				// Reset audioRecorder
				int bufferSize = AudioRecord.getMinBufferSize(
					audioRecorder.getSampleRate(), 
					audioRecorder.getChannelConfiguration(), 
					audioRecorder.getAudioFormat()
				);
				audioRecorder = new AudioRecord(
					audioRecorder.getAudioSource(), 
					audioRecorder.getSampleRate(), 
					audioRecorder.getChannelConfiguration(), 
					audioRecorder.getAudioFormat(), 
					bufferSize
				);
				audioRecorder.setRecordPositionUpdateListener(updateListener);
				audioRecorder.setPositionNotificationPeriod(framePeriod);

				state = State.INITIALIZING;
			}
		} catch (Exception e) {
			LogHelper.e(TAG, e.getMessage());
			state = State.ERROR;
		}
	}

	/** 
	 * Converts a byte[2] to a short, in LITTLE_ENDIAN format
	 */
	protected short getShort(byte argB1, byte argB2) {
		return (short) (argB1 | (argB2 << 8));
	}
}
