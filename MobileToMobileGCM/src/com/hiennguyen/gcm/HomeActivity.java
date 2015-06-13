package com.hiennguyen.gcm;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class HomeActivity extends FragmentActivity implements
		ActionBar.TabListener {
	AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	ViewPager mViewPager;
	ActionBar.Tab listTab, notiTab, settingTab, findFriendTab;
	//SessionManager sm;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_home);
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
				getSupportFragmentManager());
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});
		listTab = actionBar.newTab().setIcon(R.drawable.ic_menu_allfriends)
				.setTabListener(this);
		notiTab = actionBar.newTab().setIcon(R.drawable.ic_popup_reminder)
				.setTabListener(this);
		settingTab = actionBar.newTab().setIcon(R.drawable.ic_action_settings)
				.setTabListener(this);
		findFriendTab = actionBar.newTab().setIcon(R.drawable.ic_action_search)
				.setTabListener(this);
		actionBar.addTab(listTab);
		actionBar.addTab(notiTab);

		actionBar.addTab(findFriendTab);
		actionBar.addTab(settingTab);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder ad = new AlertDialog.Builder(this);

		ad.setPositiveButton(getResources().getString(R.string.ok_dialog),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

		ad.setNegativeButton(getResources().getString(R.string.cancel_dialog),
				null);

		ad.setMessage(getResources().getString(R.string.exit_confirm_dialog));
		ad.setTitle(getResources().getString(R.string.exit_title_dialog));
		ad.show();
	}

	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				return new ListUserFragment();

			case 1:
				return new MyNotificationFragment();
			case 2:
				return new FindFriendFragment();
			case 3:
				// return new SettingFragment();
				return new SettingFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 4;
		}
	}
}
