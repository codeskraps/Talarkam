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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;

public class TalarkamActivity extends Activity implements OnClickListener {
	private static final String TAG = TalarkamActivity.class.getSimpleName();
	private static final int MY_DATA_CHECK_CODE = 99;
	private static final int TONE_SELECT = 98;
	private static final int DIALOG = 97;
	private static final String M_HOUR = "mhour";
	private static final String M_MINT = "mmint";
	private static final String M_SNOOZE = "snooze";
	private static final String SNOOZE_T = "snoozet";
	private static final String TONE = "tone";
	private static final String FIRST_LAUNCH = "firstlaunch";
	private static final String CHKVOLUME = "chkVolume";
	private static final String SKBVOLUME = "skbVolume";

	private SharedPreferences prefs = null;
	private Toast mToast = null;
	private TimePicker tpAlarm = null;
	private CheckBox chkSnooze = null;
	private EditText etxtSnoozeMin = null;
	private Button btnSetTone = null;
	private CheckBox chkVolume = null;
	private SeekBar skbVolume = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate Started");

		setContentView(R.layout.alarm_controller);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		tpAlarm = (TimePicker) findViewById(R.id.tpAlarm);
		chkSnooze = (CheckBox) findViewById(R.id.chkSnooze);
		etxtSnoozeMin = (EditText) findViewById(R.id.etxtSnoozeMin);
		Button button = (Button) findViewById(R.id.btnSetAlarm);
		btnSetTone = (Button) findViewById(R.id.btnSetTone);
		chkVolume = (CheckBox) findViewById(R.id.chkVolume);
		skbVolume = (SeekBar) findViewById(R.id.skbVolume);
		
		button.setOnClickListener(setAlarmListener);
		btnSetTone.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();

		tpAlarm.setCurrentHour(prefs.getInt(M_HOUR, 0));
		tpAlarm.setCurrentMinute(prefs.getInt(M_MINT, 0));
		chkSnooze.setChecked(prefs.getBoolean(M_SNOOZE, false));
		etxtSnoozeMin.setText(prefs.getString(SNOOZE_T, "0"));
		chkVolume.setChecked(prefs.getBoolean(CHKVOLUME, false));
		skbVolume.setProgress(prefs.getInt(SKBVOLUME, 0));

		Uri alarmUri = Uri.parse(prefs.getString(TONE, null));
		Ringtone ringtone = RingtoneManager.getRingtone(this, alarmUri);
		if (ringtone != null) {
			String name = ringtone.getTitle(this);
			btnSetTone.setText(name);
		} else
			btnSetTone.setText(getString(R.string.btn_SetAlarmTone));

		if (prefs.getBoolean(FIRST_LAUNCH, true)) {
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

			SharedPreferences.Editor editor = prefs.edit();

			editor.putBoolean(FIRST_LAUNCH, false);
			editor.commit();
		}
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();

		SharedPreferences.Editor editor = prefs.edit();

		editor.putInt(M_HOUR, tpAlarm.getCurrentHour());
		editor.putInt(M_MINT, tpAlarm.getCurrentMinute());
		editor.putBoolean(M_SNOOZE, chkSnooze.isChecked());
		editor.putString(SNOOZE_T, etxtSnoozeMin.getText().toString());
		editor.putBoolean(CHKVOLUME, chkVolume.isChecked());
		editor.putInt(SKBVOLUME, skbVolume.getProgress());

		editor.commit();
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
				RingtoneManager.TYPE_ALARM);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm");
		if (prefs.getString(TONE, null) == null) {
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
					(Uri) null);
		} else {
			Log.d(TAG, "Uri: " + prefs.getString(TONE, null));
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
					Uri.parse(prefs.getString(TONE, null)));
		}
		TalarkamActivity.this.startActivityForResult(intent, TONE_SELECT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");

		if (resultCode == Activity.RESULT_OK && requestCode == TONE_SELECT) {
			Uri uri = data
					.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			Log.d(TAG, "Tone: " + uri.toString());

			if (uri != null) {

				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(TONE, uri.toString());
				editor.commit();

				Uri alarmUri = Uri.parse(prefs.getString(TONE, null));
				Ringtone ringtone = RingtoneManager.getRingtone(this, alarmUri);
				if (ringtone != null) {
					String name = ringtone.getTitle(this);
					btnSetTone.setText(name);
				} else
					btnSetTone.setText(getString(R.string.btn_SetAlarmTone));
				
			}
		} else if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				showDialog(DIALOG);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
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

	private OnClickListener setAlarmListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.add(Calendar.SECOND, cal.get(Calendar.SECOND) * -1);

			int alarmMint = tpAlarm.getCurrentMinute();
			int alarmHour = tpAlarm.getCurrentHour();
			int currentHour = cal.get(Calendar.HOUR_OF_DAY);
			int currentMint = cal.get(Calendar.MINUTE);
			int toAddMint = 0;
			int toAddHour = 0;

			if (currentMint > alarmMint)
				toAddMint = (60 - currentMint) + alarmMint;
			else
				toAddMint = alarmMint - currentMint;

			if (currentHour > alarmHour) {
				toAddHour = (24 - currentHour) + alarmHour;
			} else if (currentHour == alarmHour) {
				if (currentMint > alarmMint)
					toAddHour = 23;
				else if (currentMint == alarmMint)
					toAddHour = 24;
				else
					toAddHour = 0;
			} else
				toAddHour = alarmHour - currentHour;

			cal.add(Calendar.MINUTE, toAddMint);
			cal.add(Calendar.HOUR_OF_DAY, toAddHour);

			Intent intent = new Intent(getApplicationContext(),
					AlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(
					getApplicationContext(), 0, intent, 0);

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

			if (!chkSnooze.isChecked()
					&& !etxtSnoozeMin.getText().toString().equals("")) {

				am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

			} else {

				int elapseTime = Integer.parseInt(etxtSnoozeMin.getText()
						.toString());
				am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
						elapseTime * 60000, sender);
			}

			SharedPreferences.Editor editor = prefs.edit();

			editor.putInt(M_HOUR, alarmHour);
			editor.putInt(M_MINT, alarmMint);
			editor.putBoolean(M_SNOOZE, chkSnooze.isChecked());
			editor.putString(SNOOZE_T, etxtSnoozeMin.getText().toString());
			editor.putBoolean(CHKVOLUME, chkVolume.isChecked());
			editor.putInt(SKBVOLUME, skbVolume.getProgress());

			editor.commit();

			if (mToast != null) {
				mToast.cancel();
			}
			mToast = Toast.makeText(TalarkamActivity.this,
					"Alarm scheduled for " + toAddHour + " hour/s and "
							+ toAddMint + " minute/s from now",
					Toast.LENGTH_LONG);
			mToast.show();

			TalarkamActivity.this.finish();
		}
	};

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