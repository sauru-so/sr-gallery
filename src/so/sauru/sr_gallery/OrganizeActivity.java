package so.sauru.sr_gallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import so.sauru.ImageUtils;
import so.sauru.MediaStoreHelper;
import so.sauru.UriUtils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
	static ArrayList<File> fileList = new ArrayList<File> ();
	private static final String LOGTAG = "SR.GallOrg";
	static final int COPY_BUFFER_SIZE = 32 * 1024;
	static String new_album = new String();
	static File gallorg_root = null;
	static File gallorg_dest = null;
	static ArrayAdapter<CharSequence> aaAlbums;
	static Spinner spAlbums;

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

		/* real works (added by me) started here... */
		generateFileList();
	}

	private void generateFileList() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		uriList.clear();
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			if (extras != null) {
				Uri contentUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
				uriList.add(contentUri);
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
			if (extras != null) {
				ArrayList<Uri> uriArray = extras
						.getParcelableArrayList(Intent.EXTRA_STREAM);
				uriList.addAll(uriArray);
			}
		}

		fileList.clear();
		Iterator <Uri> e = uriList.iterator();
		while (e.hasNext()) {
			fileList.add(UriUtils.getFileFromUri(e.next(), this));
		}
		Collections.sort(fileList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gallorg, menu);
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
		View rV = null;
		GridView gv = null;
		Handler handler = new Handler();
		ProgressBar pb = null;
		int total = 0;
		int count = 0;
		boolean isFullyMoved = true;

		public FileSectionFragment() {
		}

		/** gallorg view with shared contents **/
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			/** get file information and show it **/
			Log.d(LOGTAG, uriList.toString());
			Log.d(LOGTAG, fileList.toString());
			if (uriList.size() == 1) {
				rV = inflater.inflate(R.layout.fragment_gallorg_file,
						container, false);
				EditText etUri = (EditText) rV.findViewById(R.id.go_file_uri_value);
				EditText etPath = (EditText) rV.findViewById(R.id.go_file_path_value);
				EditText etName = (EditText) rV.findViewById(R.id.go_file_name_value);
				EditText etSize = (EditText) rV.findViewById(R.id.go_file_size_value);
				EditText etDate = (EditText) rV.findViewById(R.id.go_file_date_value);
				EditText etHash = (EditText) rV.findViewById(R.id.go_file_hash_value);

				Uri tU = uriList.get(0);
				File tF = UriUtils.getFileFromUri(tU, this.getActivity());
				SimpleDateFormat dF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
						Locale.getDefault());
				etUri.setText(tU.toString());
				etPath.setText(tF.getParent());
				etName.setText(tF.getName());
				etSize.setText(tF.length()/1024 + "KB");
				etDate.setText(dF.format(new Date(tF.lastModified())));
				etHash.setText("Not implemented yet");

				ImageView ivPrev = (ImageView) rV
						.findViewById(R.id.go_image_preview);
				ivPrev.setImageBitmap(ImageUtils
						.createThumbnail(tF.getAbsolutePath(), 500, 240));
			} else if (uriList.size() > 1) {
				rV = inflater.inflate(R.layout.fragment_gallorg_multi,
						container, false);
				gv = (GridView) rV.findViewById(R.id.go_thumb_grid);
				gv.setAdapter(new ImageAdapter());
			} else {
				rV = inflater.inflate(R.layout.fragment_gallorg_file,
						container, false);
				rV.findViewById(R.id.go_single_image).setVisibility(View.GONE);
				rV.findViewById(R.id.go_dest_bar).setVisibility(View.GONE);
				rV.findViewById(R.id.go_action_bar).setVisibility(View.GONE);
				Toast.makeText(getActivity(),
						"Oops! No Media.", Toast.LENGTH_LONG).show();
				Log.e(LOGTAG, "Oops! empty URI list: " + uriList.toString());
				return rV;
			}

			/** get directory from... **/
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			String pref_gallery_root = prefs.getString("gallorg_root",
					getString(R.string.pref_gallery_root_default));
			if (pref_gallery_root.equals(getString(R.string
					.pref_gallery_root_default))) {
				gallorg_root = Environment
						.getExternalStoragePublicDirectory(Environment
						.DIRECTORY_PICTURES);
			} else {
				gallorg_root = new File(pref_gallery_root);
			}
			Log.i(LOGTAG, gallorg_root.getPath());

			/** generate album list from gallery root. **/
			ArrayList<CharSequence> dirStrList = new ArrayList<CharSequence> ();
			dirStrList.add(getString(R.string.go_list_camera)); /* to restore to Camera */
			dirStrList.add(getString(R.string.go_list_new)); /* for new folder creation. */
			if (gallorg_root.exists() && gallorg_root.isDirectory()) {
				ArrayList<File> dirList = new ArrayList<File> (Arrays.
						asList(gallorg_root.listFiles()));
				Collections.sort(dirList);
				Iterator<File> e = dirList.iterator();
				/* TODO now just support 1-level sub-directory. */
				while (e.hasNext()) {
					File t = (File) e.next();
					if (t.isDirectory()) {
						dirStrList.add(t.getName());
					}
				}
			} else {
				Toast.makeText(getActivity(), "gallorg_root does not exist or...",
						Toast.LENGTH_SHORT).show();
			}
			Log.d(LOGTAG, dirStrList.toString());

			spAlbums = (Spinner) rV.findViewById(R.id.go_albums_spinner);
			aaAlbums = new ArrayAdapter <CharSequence> (this
					.getActivity(), android.R.layout
					.simple_spinner_item, dirStrList);
			aaAlbums.setDropDownViewResource(android.R.layout
					.simple_spinner_dropdown_item);
			spAlbums.setAdapter(aaAlbums);
			spAlbums.setOnItemSelectedListener(new OnAlbumSelectedListener());

			rV.findViewById(R.id.go_action_move)
				.setOnClickListener(new OnBtnClickListener());
			rV.findViewById(R.id.go_action_cancel)
				.setOnClickListener(new OnBtnClickListener());
			return rV;
		}

		public static class ViewHolder {
			public ImageView iv;
		}

		public class ImageAdapter extends BaseAdapter {
			int width;
			FragmentActivity activity;

			public ImageAdapter() {
				activity = getActivity();
				int hori_m = activity.getResources()
						.getDimensionPixelSize(R.dimen.activity_horizontal_margin);
				Display d = activity.getWindowManager().getDefaultDisplay();
				width = (d.getWidth() - hori_m * 2) / 5;
			}

			@Override
			public int getCount() {
				return fileList.size();
			}

			@Override
			public Object getItem(int arg0) {
				return null;
			}

			@Override
			public long getItemId(int arg0) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder = null;
				if (convertView == null) {
					convertView = new ImageView(activity);
					holder = new ViewHolder();
					holder.iv = (ImageView) convertView;
					holder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
					holder.iv.setLayoutParams(new GridView
							.LayoutParams(width, width));
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				holder.iv.setImageResource(R.drawable.blank);
				new LazyLoadImage(holder, position, width).execute();

				return convertView;
			}
		}

		public class LazyLoadImage extends AsyncTask<Void, Void, Bitmap> {
			private ViewHolder holder = null;
			private int position = 0;
			private int width = 0;

			public LazyLoadImage(ViewHolder holder, int position, int w) {
				this.holder = holder;
				this.position = position;
				this.width = w;
			}

			@Override
			protected Bitmap doInBackground(Void... params) {
				try {
					Bitmap bmp = ImageUtils.createThumbnail(fileList
							.get(position).toString(), width, width);
					return bmp;
				} catch (IndexOutOfBoundsException e) {
					Log.w(LOGTAG, "out of bounds exception occur! ignore!");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Bitmap bmp) {
				holder.iv.setImageBitmap(bmp);
				holder.iv.getDrawable().setCallback(null);
			}
		}

		public class OnBtnClickListener implements View.OnClickListener {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.go_action_move:
					Log.d(LOGTAG, "let's move to " + gallorg_dest.toString());
					// exchange target selector with progress bar.
					rV.findViewById(R.id.go_dest_bar).setVisibility(View.GONE);
					pb = (ProgressBar) rV.findViewById(R.id.go_progressbar);
					pb.setVisibility(View.VISIBLE);
					if (makeDestDir(gallorg_dest)) {
						moveFiles();
					} else {
						Toast.makeText(getActivity(), "Oops! cannot create album!",
								Toast.LENGTH_SHORT).show();
						// restore target selector.
						pb.setVisibility(View.GONE);
						rV.findViewById(R.id.go_dest_bar).setVisibility(View.VISIBLE);
					}
					break;
				case R.id.go_action_cancel:
					Toast.makeText(getActivity(), "CANCEL", Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(getActivity(), "WHAT", Toast.LENGTH_SHORT).show();	
				}
			}

			private boolean makeDestDir(File dir) {
				if (dir.exists()) {
					return true;
				} else {
					return gallorg_dest.mkdirs();
				}
			}

			private boolean moveFile(File src, File dst) {
				// work-around for checking if same partition.
				if (src.getParentFile().getTotalSpace()
						== dst.getParentFile().getTotalSpace()) {
					Log.d(LOGTAG, "ok, moveFile within same partition...");
					if (src.renameTo(dst)) {
						return true;
					} else {
						return false;
					}
				} else {
					Log.d(LOGTAG, "ok, moveFile across partitions...");
					try {
						FileInputStream input = new FileInputStream(src);
						FileOutputStream output = new FileOutputStream(dst);
						byte[] buffer = new byte[COPY_BUFFER_SIZE];

						while (true) {
							int bytes = input.read(buffer);
							if (bytes <= 0) {
								break;
							}
							output.write(buffer, 0, bytes);
						}

						output.close();
						input.close();
						dst.setLastModified(src.lastModified());
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
					return true;
				}
			}

			private boolean deleteFile(File file) {
				MediaStoreHelper msh = new MediaStoreHelper(getActivity());
				Uri uri = msh.getContentUriFromFile(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						file.toString());
				if (uri == null) {
					Log.w(LOGTAG, "uri for " + file + "is null. skip.");
					return false;
				}
				Log.d(LOGTAG, "about to delete " + file + ", " + uri.toString());
				if (1 == msh.delete(uri)) {
					Log.i(LOGTAG, "ok, " + file.getName() + " deleted.");
					return true;
				} else {
					Log.e(LOGTAG, "XXX oops! how many?");
					return false;
				}
			}

			private boolean moveFiles() {
				total = fileList.size();
				count = 0;

				new Thread(new Runnable() {
					@Override
					public void run() {
						Iterator<File> e = fileList.iterator();
						while (e.hasNext()) {
							File file = (File) e.next();
							File dest = new File(gallorg_dest.getAbsolutePath()
									+ "/" + file.getName());
							long file_size = file.length();
							Log.d(LOGTAG, "move " + file.getAbsolutePath()
									+ " to " + dest.getAbsolutePath());

							if (dest.exists()) {	// do nothing for this.
								Log.w(LOGTAG,
										"file already exists!" + dest.toString());
								isFullyMoved = false;
							} else if (moveFile(file, dest)) {
								Log.d(LOGTAG, "ok, moved to " + dest.toString());
								MediaStoreHelper msh = new MediaStoreHelper(
										getActivity());
								Uri uri = msh.insert(dest);
								Log.d(LOGTAG, "- new uri: " + uri);
								//msh.scanDir(gallorg_dest.toString());
								e.remove();
								if (dest.exists() == true
										&& file_size == dest.length()) {
									deleteFile(file);
								}
							} else {
								Log.e(LOGTAG,
										"oops! error on moving!" + file.toString());
								isFullyMoved = false;
							}

							count += 1;
							handler.post(new Runnable() { // progress per file.
								public void run() {
									pb.setProgress(100 * count / total);
								}
							});
						}
						Log.i(LOGTAG, "good! isFullyMoved: " + isFullyMoved);
						handler.post(new Runnable() { // final ui update
							public void run() {
								((ImageAdapter) gv.getAdapter())
										.notifyDataSetChanged();
								if (isFullyMoved) {
									Toast.makeText(getActivity(), getResources()
											.getString(R.string.go_move_all_done),
											Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(getActivity(), getResources()
											.getString(R.string.go_move_partially_done),
											Toast.LENGTH_SHORT).show();
								}
								pb.setVisibility(View.GONE);
								rV.findViewById(R.id.go_dest_bar)
										.setVisibility(View.VISIBLE);
							}
						});
					}
				}).start();
				return true;
			}
		}

		public class OnAlbumSelectedListener implements OnItemSelectedListener {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				String selected = parent.getItemAtPosition(pos).toString();
				if (selected.equals(getString(R.string.go_list_camera))) {
					gallorg_dest = new File(Environment
							.getExternalStoragePublicDirectory(Environment
									.DIRECTORY_DCIM) + "/Camera");
				} else if (selected.equals(getString(R.string.go_list_new))) {
					getNewAlbum();
					return;
				} else {
					gallorg_dest = new File(gallorg_root + "/" + selected);
				}
				Log.d(LOGTAG, "gallorg dest is " + gallorg_dest.toString());
				Toast.makeText(parent.getContext(), "dest is " + gallorg_dest.toString(),
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}

			public void getNewAlbum() {
				AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
				alert.setTitle(R.string.go_newalbum_title);
				alert.setMessage(R.string.go_newalbum_desc);
				final EditText input = new EditText(getActivity());
				alert.setView(input);
				alert.setPositiveButton(R.string.generic_ok, new DialogInterface
						.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new_album = input.getText().toString().trim();
								Log.d(LOGTAG, "new album name: " + new_album);
								aaAlbums.add(new_album);
								aaAlbums.notifyDataSetChanged();
								spAlbums.setSelection(spAlbums.getCount() - 1);
							}
						});
				alert.setNegativeButton(R.string.generic_cancel, new DialogInterface
						.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
				alert.show();
			}
		}
	}
}
/* vim: set ts=2 sw=2: */