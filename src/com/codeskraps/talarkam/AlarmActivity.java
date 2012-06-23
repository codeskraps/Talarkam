/**
 * Talarkam
 * Copyright (C) Carles Sentis 2011 <codeskraps@gmail.com>
 *
 * Talarkam is free software: you can
 * redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later
 * version.
 *  
 * Talarkam is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *  
 * You should have received a copy of the GNU
 * General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.codeskraps.talarkam;

import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AlarmActivity extends Activity implements OnInitListener {
	private static final String TAG = AlarmActivity.class.getSimpleName();
	private static final String LANGUAGE = "lstLanguage";
	private static final String SNOOZE_T = "snoozet";
	private static final String TONE = "tone";
	private static final String FIRST_TIME = "first_time";
	private static final int DIALOG = 97;

	private Toast mToast = null;
	private Handler mTimeTaskHandler = null;
	private Handler mTalkTaskHandler = null;
	private TextToSpeech mTts = null;
	private Button btnTime = null;
	private int elapseTime;
	private SharedPreferences prefs = null;
	private MediaPlayer mMediaPlayer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate Started");

		setContentView(R.layout.alarm_off);

		btnTime = (Button) findViewById(R.id.txtTime);
		Button btnStop = (Button) findViewById(R.id.btnStop);

		btnTime.setOnClickListener(tellTime);
		btnStop.setOnClickListener(stopAlarmListener);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		elapseTime = Integer.parseInt(prefs.getString(SNOOZE_T, "0"));

		mTimeTaskHandler = new Handler();
		mTimeTaskHandler.removeCallbacks(mUpdateTimeTask);
		mTimeTaskHandler.postDelayed(mUpdateTimeTask, 0);

		mTalkTaskHandler = new Handler();
		mTalkTaskHandler.removeCallbacks(mUpdateTalkTask);
		mTalkTaskHandler.postDelayed(mUpdateTalkTask, 0);

		setVolumeControlStream(TextToSpeech.Engine.DEFAULT_STREAM);
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(FIRST_TIME, true);
		editor.commit();
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());

			String time = getTime();
			btnTime.setText(time);
			Log.d(TAG, time);

			mTimeTaskHandler.postDelayed(mUpdateTimeTask,
					60000 - (cal.get(Calendar.SECOND) * -1));
		}
	};

	private Runnable mUpdateTalkTask = new Runnable() {
		public void run() {

			if (prefs.getBoolean(FIRST_TIME, false)) {
				try {
					mMediaPlayer = new MediaPlayer();
					String tone = prefs.getString(TONE, null);
					Uri myUri = null;
					if (tone == null) {
						myUri = RingtoneManager
								.getDefaultUri(RingtoneManager.TYPE_ALARM);
					} else {
						myUri = Uri.parse(tone);
					}
					mMediaPlayer.setDataSource(AlarmActivity.this, myUri);
					final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
						mMediaPlayer
								.setAudioStreamType(AudioManager.STREAM_ALARM);
						mMediaPlayer.setLooping(true);
						mMediaPlayer.prepare();
						mMediaPlayer.start();
					}
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			} else
				mTts = new TextToSpeech(AlarmActivity.this, AlarmActivity.this);

			mTalkTaskHandler.postDelayed(mUpdateTalkTask, elapseTime * 60000);
		}
	};

	private OnClickListener tellTime = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (prefs.getBoolean(FIRST_TIME, false)) {
				mMediaPlayer.stop();
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(FIRST_TIME, false);
				editor.commit();
			}
			mTts = new TextToSpeech(AlarmActivity.this, AlarmActivity.this);
		}
	};

	private OnClickListener stopAlarmListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			Intent intent = new Intent(getApplicationContext(),
					AlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(
					getApplicationContext(), 0, intent, 0);

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

			try {
				am.cancel(sender);
			} catch (Exception e) {
				Log.e(TAG, "AlarmManager not cancel: " + e.getMessage());
			}

			if (mToast != null) {
				mToast.cancel();
			}
			mToast = Toast.makeText(AlarmActivity.this, "Alarm stopped",
					Toast.LENGTH_SHORT);
			mToast.show();

			mMediaPlayer.stop();

			AlarmActivity.this.finish();
		}
	};

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int languageSelected = Integer.parseInt(prefs.getString(LANGUAGE,
					Integer.toString(0)));
			int result = 0;
			switch (languageSelected) {
			case 0:
				break;
			case 1:
				result = mTts.setLanguage(Locale.US);
				break;
			case 2:
				result = mTts.setLanguage(Locale.UK);
				break;
			case 3:
				result = mTts.setLanguage(Locale.FRENCH);
				break;
			case 4:
				result = mTts.setLanguage(Locale.GERMAN);
				break;
			case 5:
				result = mTts.setLanguage(Locale.ITALIAN);
				break;
			case 6:
				result = mTts.setLanguage(new Locale("es", "ES"));
				break;
			}
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e(TAG, "Language is not available.");
				showDialog(DIALOG);
			} else {

				String time = getTime();
				btnTime.setText(time);
				mTts.speak(time, TextToSpeech.QUEUE_FLUSH, null);

			}
		} else {
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	private String getTime() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());

		String stringHour, stringMint;

		int currentHour = cal.get(Calendar.HOUR_OF_DAY);
		stringHour = String.valueOf(currentHour);
		if (currentHour < 10)
			stringHour = "0" + stringHour;

		int currentMint = cal.get(Calendar.MINUTE);
		stringMint = String.valueOf(currentMint);
		if (currentMint < 10)
			stringMint = "0" + stringMint;

		return String.format("%s:%s", stringHour, stringMint);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					getResources()
							.getString(R.string.pf_languageMissingSummary))
					.setTitle(
							getResources().getString(
									R.string.pf_languageMissingTittle))
					.setCancelable(false)
					.setPositiveButton(getResources().getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent installIntent = new Intent();
									installIntent
											.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
									startActivity(installIntent);
								}
							})
					.setNegativeButton(getResources().getString(R.string.no),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");

		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}

		mTimeTaskHandler.removeCallbacks(mUpdateTimeTask);
		mTalkTaskHandler.removeCallbacks(mUpdateTalkTask);

		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown");

		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			Log.d(TAG, "keyEvent.keyMenu");
			startActivity(new Intent(this, PrefsActivity.class));
			return true;
		}

		Log.d(TAG, "return false");
		return super.onKeyDown(keyCode, event);
	}
}
