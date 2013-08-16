package so.sauru.sr_gallery;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import so.sauru.UriUtils;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OrganizeActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	/* my variables */
	static ArrayList<Uri> uriList = new ArrayList<Uri> ();
	ArrayList<File> fileList = new ArrayList<File> ();
	static String GALLORG = "GallOrg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organize);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		/* real works */
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		uriList.clear();
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			if (extras != null) {
				Uri fileUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
				Log.d(GALLORG, "SEND URI:" + fileUri.toString());
				uriList.add(fileUri);
				Log.d(GALLORG, "SEND URI.Path:" + fileUri.getPath());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.organize, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				startActivity(settingsIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment;
			if (position == 0) {
				fragment = new FileSectionFragment();
			} else {
				fragment = new DummySectionFragment();
			}
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_orgtab_1_file).toUpperCase(l);
			case 1:
				return getString(R.string.title_orgtab_2_meta).toUpperCase(l);
			case 2:
				return getString(R.string.title_orgtab_3_exif).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_organize_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	public static class FileSectionFragment extends Fragment {
		public FileSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rV = inflater.inflate(R.layout.fragment_gallorg_file,
					container, false);
			EditText etUri = (EditText) rV.findViewById(R.id.go_file_uri_value);
			EditText etPath = (EditText) rV.findViewById(R.id.go_file_path_value);
			EditText etName = (EditText) rV.findViewById(R.id.go_file_name_value);
			EditText etSize = (EditText) rV.findViewById(R.id.go_file_size_value);
			EditText etDate = (EditText) rV.findViewById(R.id.go_file_date_value);
			EditText etHash = (EditText) rV.findViewById(R.id.go_file_hash_value);

			/** get file information and show it **/
			Log.d(GALLORG, uriList.toString());
			if (uriList.size() == 1) {
				Uri tU = uriList.get(0);
				File tF = UriUtils.getFileFromUri(tU, this.getActivity());
				SimpleDateFormat dF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				etUri.setText(tU.toString());
				etPath.setText(tF.getParent());
				etName.setText(tF.getName());
				etSize.setText(tF.length()/1024 + "KB");
				etDate.setText(dF.format(new Date(tF.lastModified())));
				etHash.setText("Not implemented yet");
			} else if (uriList.size() > 0) {
				etUri.setText("Bulk");
				etPath.setText("Bulk");
				etName.setText("Bulk");
			} else {
				etUri.setText("Empty");
				etPath.setText("Empty");
				etName.setText("Empty");
				Log.e(GALLORG, "Error! empty URI list: " + uriList.toString());
			}

			/** get directory from... **/
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
			File gRoot = new File(prefs.getString("gallorg_root", "/"));
			Log.d(GALLORG, gRoot.getPath());

			/* generate dir list */
			ArrayList<String> dirStrList = new ArrayList<String> ();
			dirStrList.add("<Camera>");
			if (gRoot.exists() && gRoot.isDirectory()) {
				ArrayList<File> dirList = new ArrayList<File> (Arrays.
						asList(gRoot.listFiles()));
				Collections.sort(dirList);
				Iterator<File> e = dirList.iterator();
				while (e.hasNext()) {
					dirStrList.add(((File) e.next()).getName());
				}
				Log.d(GALLORG, dirStrList.toString());
			}
			dirStrList.add("New");

			Spinner spAlbums = (Spinner) rV.findViewById(R.id.og_albums_spinner);
			ArrayAdapter<String> aa = new ArrayAdapter <String> (this.getActivity(),
					android.R.layout.simple_spinner_dropdown_item, dirStrList);
			spAlbums.setAdapter(aa);
			//spAlbums.setOnItemSelectedListener(this);

			//Toast.makeText(this, uriList.get(0).toString(), Toast.LENGTH_LONG).show();
			return rV;
		}
	}
}
