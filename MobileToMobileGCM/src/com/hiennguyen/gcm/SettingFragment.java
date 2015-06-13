package com.hiennguyen.gcm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

public class SettingFragment extends Fragment {
	RadioGroup rgSelectSound;
	Switch switchSound,switchVibrate,switchNoti;
	ImageView ivChangePassword, ivLogout;
	public static final String PROPERTY_NOTI = "is_noti";
	public static final String PROPERTY_VIBRATE = "is_vibrate";
	public static final String PROPERTY_SOUND = "is_sound";
	public static final String PROPERTY_SOUND_INDEX = "sound_index";
	SessionManager sm;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		sm=new SessionManager(getActivity());
		View rootView = inflater.inflate(R.layout.fragment_setting, container,
				false);
		ivChangePassword=(ImageView)rootView.findViewById(R.id.ivChangePassword);
		ivLogout=(ImageView)rootView.findViewById(R.id.ivlogout);
		ivChangePassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
				startActivity(intent);
			}
		});
		ivLogout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sm.logoutUser();
				getActivity().finish();
			}
		});
		rgSelectSound = (RadioGroup) rootView.findViewById(R.id.rgSelectSound);

		SharedPreferences prefs = getSettingsPref();
		int soundIndex = prefs.getInt(PROPERTY_SOUND_INDEX, 1);
		for (int i = 0; i < rgSelectSound.getChildCount(); i++) {
			if (soundIndex == i) {
				((RadioButton) rgSelectSound.getChildAt(i)).setChecked(true);
			}
		}

		rgSelectSound
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						SharedPreferences.Editor editor = getSettingsPref()
								.edit();
						switch (checkedId) {
						case R.id.rd01:
							editor.putInt(PROPERTY_SOUND_INDEX, 1);
							break;
						case R.id.rd02:
							editor.putInt(PROPERTY_SOUND_INDEX, 2);
							break;
						case R.id.rd03:
							editor.putInt(PROPERTY_SOUND_INDEX, 3);
							break;
						case R.id.rd04:
							editor.putInt(PROPERTY_SOUND_INDEX, 4);
							break;
						case R.id.rd05:
							editor.putInt(PROPERTY_SOUND_INDEX, 5);
							break;
						case R.id.rd00:
							editor.putInt(PROPERTY_SOUND_INDEX, 0);
							break;
						}
						editor.apply();
					}
				});
		//Notification
		switchNoti=(Switch)rootView.findViewById(R.id.switchNoti);
		boolean noti=prefs.getBoolean(PROPERTY_NOTI, true);
		if (noti) {
			switchNoti.setChecked(true);
		}else {
			switchNoti.setChecked(false);
		}
		switchNoti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = getSettingsPref()
						.edit();
				if (isChecked) {
					editor.putBoolean(PROPERTY_NOTI, true);
				} else {
					editor.putBoolean(PROPERTY_NOTI, false);
				}
				editor.apply();
			}
		});
		// Notification sound
		switchSound = (Switch) rootView.findViewById(R.id.switchSound);
		boolean Sound = prefs.getBoolean(PROPERTY_SOUND, true);
		if (Sound) {
			switchSound.setChecked(true);
			changeStatusRadioGroup(true);
		} else {
			switchSound.setChecked(false);
			changeStatusRadioGroup(false);
		}
		switchSound
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						;
						SharedPreferences.Editor editor = getSettingsPref()
								.edit();
						if (isChecked) {
							editor.putBoolean(PROPERTY_SOUND, true);
							changeStatusRadioGroup(true);
						} else {
							editor.putBoolean(PROPERTY_SOUND, false);
							changeStatusRadioGroup(false);
						}
						editor.apply();
					}
				});
		// Notification Vibrate
		switchVibrate = (Switch) rootView.findViewById(R.id.switchVibrate);
		boolean Vibrate = prefs.getBoolean(PROPERTY_VIBRATE, true);
		if (Vibrate) {
			switchVibrate.setChecked(true);
		} else {
			switchVibrate.setChecked(false);
		}
		switchVibrate
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						SharedPreferences.Editor editor = getSettingsPref()
								.edit();
						if (isChecked) {
							editor.putBoolean(PROPERTY_VIBRATE, true);
						} else {
							editor.putBoolean(PROPERTY_VIBRATE, false);
						}
						editor.apply();
					}
				});

		return rootView;
	}

	public SharedPreferences getSettingsPref() {
		return getActivity().getSharedPreferences(
				SettingFragment.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	public void changeStatusRadioGroup(boolean enable) {
		for (int i = 0; i < rgSelectSound.getChildCount(); i++) {
			((RadioButton) rgSelectSound.getChildAt(i)).setEnabled(enable);
		}
	}
}
