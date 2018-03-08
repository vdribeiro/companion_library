package companion.support.v8.media;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;

import companion.support.v8.os.Storage;
import companion.support.v8.util.LogHelper;

/**
 * Utility class for Media.
 * 
 * @author Vitor Ribeiro
 * 
 */
public class MediaUtils {

	/** Log Tag */
	private static final String TAG = MediaUtils.class.getSimpleName();

	/** Default silence threshold. */
	public static final int DEFAULT_SILENCE_THRESHOLD = 2000;
	/** All devices should have a microphone. */
	public static final int DEFAULT_SOURCE = AudioSource.MIC;
	/** 44.1 kHz is supported by all devices. */
	public static final int DEFAULT_FREQUENCY = 44100;
	/** MONO is supported by all devices. */
	public static final int DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
	/** 16Bit is supported by all devices. */
	public static final int DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	/** This prevents the class from being instantiated. 
	 */
	private MediaUtils() {
	}

	/**
	 * Creates a media recorder.
	 * @param path of the output file to be produced.
	 * @param format of the output file produced during recording, 
	 * as per {@link android.media.MediaRecorder.OutputFormat OutputFormat}.
	 * @param source to be used for recording, 
	 * as per {@link android.media.MediaRecorder.AudioSource AudioSource}.
	 * @param encoder to be used for recording, 
	 * as per {@link android.media.MediaRecorder.AudioEncoder AudioEncoder}.
	 * @return MediaRecorder object.
	 * @throws Exception if the recorder could not be created.
	 */
	public static MediaRecorder createRecorder(String path, int format, int source, int encoder) throws Exception {
		if (!Storage.isWritable()) {
			return null;
		}

		MediaRecorder recorder = new MediaRecorder();
		recorder.setAudioSource(source);
		recorder.setOutputFormat(format);
		recorder.setOutputFile(path);
		recorder.setAudioEncoder(encoder);
		recorder.prepare();

		return recorder;
	}

	/**
	 * Creates a media recorder with the default configurations.
	 * @param path of the output file to be produced.
	 * @return MediaRecorder object.
	 * @throws Exception if the recorder could not be created.
	 */
	public static MediaRecorder createRecorder(String path) throws Exception {
		return createRecorder(path, OutputFormat.THREE_GPP, DEFAULT_SOURCE, AudioEncoder.AMR_NB);
	}

	/**
	 * Stops media recording.
	 */
	public static void stopRecorder(MediaRecorder recorder) {
		try {
			recorder.stop();
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
		}
		try {
			recorder.release();
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
		}

		recorder=null;
		Thread.yield();
	}

	/**
	 * Creates an audio recorder.
	 * @param source to be used for recording, 
	 * as per {@link android.media.MediaRecorder.AudioSource AudioSource}.
	 * @param encoder to be used for recording, 
	 * as per {@link android.media.AudioFormat AudioFormat}.
	 * @param channelConfiguration describes the configuration of the audio channels, 
	 * as per {@link android.media.AudioFormat AudioFormat}.
	 * @param frequency the sample rate expressed in Hertz.
	 * @param bufferSize the minimal buffer size should be obtained with 
	 * {@link android.media.AudioRecord AudioRecord.getMinBufferSize}. 
	 * It seems to be at least 1024 bytes, this is most likely due to a MMIO hardware limit.
	 * It should also be given extra space to prevent overflow.
	 * @return AudioRecord object.
	 * @throws Exception if the recorder could not be created.
	 */
	public static AudioRecord createAudioRecorder(int source, int encoder, int channelConfiguration, int frequency, int bufferSize) throws Exception {
		AudioRecord audioRecord = new AudioRecord(source, frequency, channelConfiguration, encoder, bufferSize);
		if (audioRecord.getState()==AudioRecord.STATE_UNINITIALIZED) {
			throw new Exception("No Audio Recorder");
		}

		return audioRecord;
	}

	/**
	 * Creates an audio recorder.
	 * @param encoder to be used for recording, 
	 * as per {@link android.media.AudioFormat AudioFormat}.
	 * @param channelConfiguration describes the configuration of the audio channels, 
	 * as per {@link android.media.AudioFormat AudioFormat}.
	 * @param frequency the sample rate expressed in Hertz.
	 * It seems to be at least 1024 bytes, this is most likely due to a MMIO hardware limit.
	 * It should also be given extra space to prevent overflow.
	 * @return AudioRecord object.
	 * @throws Exception if the recorder could not be created.
	 */
	public static AudioRecord createAudioRecorder(int encoder, int channelConfiguration, int frequency) throws Exception {
		int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, encoder);
		return createAudioRecorder(DEFAULT_SOURCE, encoder, channelConfiguration, frequency, bufferSize);
	}

	/**
	 * Creates an audio recorder with the default configurations.
	 * @return AudioRecord object.
	 * @throws Exception if the recorder could not be created.
	 */
	public static AudioRecord createAudioRecorder() throws Exception {
		int bufferSize = AudioRecord.getMinBufferSize(DEFAULT_FREQUENCY, DEFAULT_CHANNEL, DEFAULT_ENCODING);
		return createAudioRecorder(DEFAULT_SOURCE, DEFAULT_ENCODING, DEFAULT_CHANNEL, DEFAULT_FREQUENCY, bufferSize);
	}

	/**
	 * Stops audio recording.
	 */
	public static void stopAudioRecorder(AudioRecord recorder) {
		try {
			recorder.stop();
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
		}
		try {
			recorder.release();
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
		}

		recorder=null;
		Thread.yield();
	}

	/** Get appropriate buffer size for a given recorder and time.
	 * @param encoder to be used for recording, 
	 * as per {@link android.media.AudioFormat AudioFormat}.
	 * @param channelConfiguration describes the configuration of the audio channels, 
	 * as per {@link android.media.AudioFormat AudioFormat}.
	 * @param frequency the sample rate expressed in Hertz.
	 * @param milliseconds time of recording.
	 * @return buffer size.
	 */
	public static int getAudioBufferSize(int encoder, int channelConfiguration, int frequency, int milliseconds) {

		float percentOfASecond = (float) milliseconds / 1000.0f;
		int numSamplesRequired = (int) ((float) frequency * percentOfASecond);
		int minBufferSize = AudioRecord.getMinBufferSize(frequency,	channelConfiguration, encoder);

		int bufferSize = minBufferSize;
		if (encoder == AudioFormat.ENCODING_PCM_16BIT) {
			bufferSize = numSamplesRequired * 2;
		} else if (encoder == AudioFormat.ENCODING_PCM_8BIT) {
			bufferSize = numSamplesRequired;
		}

		if (bufferSize < minBufferSize) {
			// Increase buffer to hold enough samples
			bufferSize = minBufferSize;
		}

		return bufferSize;
	}

	/** 
	 * Gets a list of the possible audio configurations in this device according to the given parameters.
	 * @param encoders to be used for recording, as per AudioFormat.
	 * @param channelConfigurations describes the configuration of the audio channels, as per AudioFormat.
	 * @param sampleRates the sample rates expressed in Hertz.
	 * @return arrayList of supported AudioRecord objects.
	 */
	public static ArrayList<AudioRecord> getAudioRecorderConfigurations(int encoders[], int channelConfigurations[], int[] sampleRates) {
		ArrayList<AudioRecord> configurations = new ArrayList<AudioRecord>();
		for (int encoder : encoders) {
			for (int channelConfiguration : channelConfigurations) {
				for (int frequency : sampleRates) {
					try {
						AudioRecord record = createAudioRecorder(encoder, channelConfiguration, frequency);
						configurations.add(record);
					} catch (Exception e) {
						// Not supported
					}
				}
			}
		}
		return configurations;
	}
	
	/**
	 * Gets a list of the default possible audio configurations in this device.
	 * The combinations are 8 and 16 bit MONO and STEREO formats 
	 * with sample rates ranging from 44100 to 8000.
	 * @return arrayList of supported AudioRecord objects.
	 */
	public static ArrayList<AudioRecord> getAudioRecorderConfigurations() {
		int encoders[] = {AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT};
		int channelConfigurations[] = {AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};
		int[] sampleRates = {44100, 22050, 16000, 11025, 8000};

		return getAudioRecorderConfigurations(encoders, channelConfigurations, sampleRates);
	}

	/**
	 * Gets the best possible audio configuration in this device.
	 * @return supported AudioRecord object.
	 */
	public static AudioRecord getAudioRecorderDefaultConfiguration() {
		AudioRecord configuration = null;
		int encoders[] = {AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT};
		int channelConfigurations[] = {AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};
		int[] sampleRates = {44100, 22050, 16000, 11025, 8000};
		
		for (int encoder : encoders) {
			for (int channelConfiguration : channelConfigurations) {
				for (int frequency : sampleRates) {
					try {
						configuration = createAudioRecorder(encoder, channelConfiguration, frequency);
						break;
					} catch (Exception e) {
						// Not supported
					}
				}
			}
		}

		return configuration;
	}

	/** Check if the audio is below the default silence threshold.
	 * @param audioData audio buffer.
	 * @return true if it is considered silence, false otherwise.
	 */
	public static boolean isSilence(short[] audioData) {
        return (int) getSignalPower(audioData) < DEFAULT_SILENCE_THRESHOLD;

    }

	/** Get the signal power.
	 * @param audioData audio buffer.
	 * @return signal power.
	 */
	public static double getSignalPower(short[] audioData) {
		if (audioData == null) {
			return 0;
		}
		if (audioData.length == 0) {
			return 0;
		}

		double ms = 0;
		for (short anAudioData : audioData) {
			ms += anAudioData * anAudioData;
		}
		ms /= audioData.length;

		return Math.sqrt(ms);
	}

	/** Get the number of zeros in the audio.
	 * @param audioData audio buffer.
	 * @return number of zeros.
	 */
	public static int countZeros(short[] audioData) {
		if (audioData == null) {
			return 0;
		}
		if (audioData.length == 0) {
			return 0;
		}

		int numZeros = 0;
		for (short anAudioData : audioData) {
			if (anAudioData == 0) {
				numZeros++;
			}
		}
		return numZeros;
	}

	/**
	 * Calculate frequency using zero crossings.
	 * 
	 * @param audioData audio buffer.
	 * @param sampleRate sample rate in Hz.
	 * @return frequency.
	 */
	public static int calculateZeroCrossing(short[] audioData, int sampleRate) {
		int numSamples = audioData.length;
		int numCrossing = 0;
		for (int p = 0; p < numSamples-1; p++) {
			if ((audioData[p] > 0 && audioData[p + 1] <= 0) || (audioData[p] < 0 && audioData[p + 1] >= 0)) {
				numCrossing++;
			}
		}

		float numSecondsRecorded = (float)numSamples/(float)sampleRate;
		float numCycles = numCrossing/2;
		float frequency = numCycles/numSecondsRecorded;

		return (int)frequency;
	}

	/** Get seconds per sample.
	 * @param sampleRate sample rate in Hz.
	 * @return seconds per sample.
	 */
	public static float secondsPerSample(int sampleRate) {
		return 1.0f / (float) sampleRate;
	}

	/** Get number of samples after a given time.
	 * @param sampleRate sample rate in Hz.
	 * @param milliseconds time.
	 * @return number of samples.
	 */
	public static int numSamplesInTime(int sampleRate, float milliseconds) {
		return (int)((float) sampleRate * (milliseconds/1000f));
	}

	/**
	 * Read an audio file and returns its contents as an array of shorts.
	 * @param file audio file.
	 * @param swap true if it is a little-endian file, 
	 * false if big-endian file.
	 * @return short array.
	 * @throws Exception if the parsing failed.
	 */
	public static short[] audioFileToShortArray(File file, boolean swap) throws Exception {
		short[] outputArray = new short[(int)file.length()/2];

		// Read file
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		byte[] buffer = new byte[4096];
		while(bis.read(buffer) != - 1){
			baos.write(buffer);
		}
		byte[] outputByteArray = baos.toByteArray();
		bis.close();
		baos.close();

		// Test for swap
		if(swap){
			for(int i=0; i < outputByteArray.length - 1; i=i+2){
				byte byte0 = outputByteArray[i];
				outputByteArray[i] = outputByteArray[i+1];
				outputByteArray[i+1] = byte0;
			}
		}
		for(int i=0, j=0; i < outputByteArray.length; i+= 2, j++){
			if (swap) {
				outputArray[j] = (short) ((outputByteArray[i + 1] & 0xff) << 8 | (outputByteArray[i] & 0xff));
			} else {
				outputArray[j] = (short) ((outputByteArray[i] & 0xff) << 8 | (outputByteArray[i + 1] & 0xff));
			}
		}
		return outputArray;
	}
}
