package com.example.meikobb.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.example.meikobb.R;

public class SettingsActivity extends Activity {
	
	/* 定数 */
	public static final String PREF_KEY_AUTH_ID = "auth_id";
	public static final String PREF_KEY_AUTH_PW = "auth_pw";

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		
		// アクションバー　アイコンの「戻る」機能追加
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home: {
			finish();
			
			return true;
		}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		
		// 設定値が変更されたときに呼ばれる
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			
			if( key.equals(PREF_KEY_AUTH_ID) ) {
				EditTextPreference authID = (EditTextPreference) findPreference(key);
				authID.setSummary(authID.getText());
				MainActivity.initialize();
			} else if( key.equals(PREF_KEY_AUTH_PW) ) {
				EditTextPreference authPW = (EditTextPreference) findPreference(key);
				authPW.setSummary( (authPW.getText() == null || authPW.getText().isEmpty()) ? "-" : "(hidden)" );
				MainActivity.initialize();
			}
			
		}
		
		
		// フラグメント生成時に呼ばれる
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);
			
			// 保存されている設定値を項目名の下に表示
			EditTextPreference authID = (EditTextPreference) findPreference(PREF_KEY_AUTH_ID);
			authID.setSummary( (authID.getText() == null) ? "-" : authID.getText() );
			EditTextPreference authPW = (EditTextPreference) findPreference(PREF_KEY_AUTH_PW);
			if( authPW != null ) {
				authPW.setSummary( (authPW.getText() == null) ? "-" : "(hidden)" );
			}
			
		}
		
		
		@Override
		public void onResume() {
			super.onResume();
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

	}
	
	@Override
	public void onPause() {
		super.onPause();
		
	}
	
}
