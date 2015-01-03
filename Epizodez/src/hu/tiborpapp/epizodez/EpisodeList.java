package hu.tiborpapp.epizodez;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class EpisodeList extends ListActivity {

	private ListView lv;

	private SQLiteDatabase db;

	private String showId, showTitle;
	private final static String TAG = MainActivity.class.getName();

	private ImageView banner;
	private ImageDownloader imageDownloader;

	private ArrayList<HashMap<String, String>> seasonList;

	private static final String XML_TITLE = "title";
	private static final String XML_AIRDATE = "airdate";
	private static final String XML_SEASONNO = "";
	private static final String XML_EPNO = "seasonnum";

	private ProgressDialog pDialog;
	private Button btnStart, btnSelectAll;
	private TextView lblShowName;
	
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eplist);

		db = openOrCreateDatabase("ShowsDB", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS shows(showname VARCHAR, epname VARCHAR);");

		Intent i = getIntent();

		showId = i.getStringExtra("showID");
		showTitle = i.getStringExtra("title");

		btnStart = (Button) findViewById(R.id.btnStart);
		btnSelectAll = (Button) findViewById(R.id.btnSelectAll);
		seasonList = new ArrayList<HashMap<String, String>>();
		lblShowName = (TextView) findViewById(R.id.showName);

		Log.d(TAG, showId);
		Log.d(TAG, showTitle);

		lblShowName.setText(showTitle);

		lv = getListView();

		start();

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				String epTitle = seasonList.get(position).get("title");
				Log.d(TAG, epTitle);

				{
					String dbShowTitle = showTitle.replace("'", "");
					String dbEpisodeTitle = epTitle.replace("'", "");

					db.execSQL("INSERT INTO shows VALUES('" + dbShowTitle
							+ "','" + dbEpisodeTitle + "');");
					showMessage(getString(R.string.success_),
							getString(R.string.record_added_));
				}
			}
		});
	}

	
	/**
	 * Makes an instance of GetEpisodes and invokes its execute() method to start the AsyncTask.
	 */
	public void start() {

		GetEpisodes getEpisodesObject = new GetEpisodes();
		getEpisodesObject.execute();
	}

	/**
	 * AsyncTask class to get XML by making HTTP call
	 * */
	public class GetEpisodes extends AsyncTask<Void, Void, Void> {

		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(EpisodeList.this);
			pDialog.setTitle(R.string.downloading_show_data_);
			pDialog.setMessage(getString(R.string.please_wait_));
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * Downloads the previously selected TV-show's XML which
		 * contains the episode list and its banner/cover image.
		 */
		@Override
		protected Void doInBackground(Void... params) {
			ServiceHandler sh = new ServiceHandler();

			final String url1 = "http://services.tvrage.com/feeds/full_show_info.php?sid=";

			final String seasonsFinalURL = url1 + showId;

			String xmlResponse = sh.makeServiceCall(seasonsFinalURL,
					ServiceHandler.GET);

			// TESZT //
			Log.d(TAG, xmlResponse);
			Log.d(TAG, seasonsFinalURL);

			if (xmlResponse != null) {
				try {

					// getting DOM element
					Document doc = sh.getDomElement(xmlResponse);

					// banner
					NodeList showNode = doc.getElementsByTagName("Show");
					Element test = (Element) showNode.item(0);
					String url = sh.getValue(test, "image");
					Log.d(TAG, url);

					banner = (ImageView) findViewById(R.id.imageView2);
					imageDownloader = (ImageDownloader) new ImageDownloader(
							banner).execute(url);

					// filtering the "Season" node
					NodeList seasonNode = doc.getElementsByTagName("Season");

					// looping through all item nodes <episode>
					for (int i = 0; i <= seasonNode.getLength(); i++) {
						Element s = (Element) seasonNode.item(i);

						String seasonNo = s.getAttribute("no");

						// TEST
						String nodeSorSzam = String.valueOf(seasonNode
								.getLength());
						Log.d(TAG, nodeSorSzam);

						NodeList episodeNode = s.getElementsByTagName("episode");

						for (int j = 0; j < episodeNode.getLength(); j++) {

							HashMap<String, String> xmlItems = new HashMap<String, String>();
							Element e = (Element) episodeNode.item(j);
							String episodeNo = sh.getValue(e, XML_EPNO);

							// add child nodes to the HashMap key-value

							xmlItems.put(XML_TITLE, sh.getValue(e, XML_TITLE));
							xmlItems.put(XML_AIRDATE, sh.getValue(e, XML_AIRDATE));
							xmlItems.put(XML_SEASONNO, "Season " + seasonNo);
							xmlItems.put(XML_EPNO, "Episode " + episodeNo);

							// adding HashList to ArrayList
							seasonList.add(xmlItems);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return null;
		}
		
		/**
		 * Closes the ProgressDialog. Makes an adapter for the seasonList and
		 * fills up the items got as result from doInBackground(). Shows the
		 * image with a fade in animation.
		 */
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (pDialog.isShowing()) {
				pDialog.dismiss();
			}

			ListAdapter adapter = new SimpleAdapter(EpisodeList.this,
					seasonList, R.layout.list_item, 
							new String[] { XML_TITLE, XML_AIRDATE, XML_SEASONNO, XML_EPNO }, 
							new int[] { R.id.name, R.id.airdate, R.id.seasonno, R.id.epno });
			setListAdapter(adapter);
			Animation fadeInAnimation = AnimationUtils.loadAnimation(EpisodeList.this, R.anim.fadein);
			banner.startAnimation(fadeInAnimation);
			Log.d(TAG, "STOP");
		}
	}

	/**
	 * Shows informations as message on AlertDialog.
	 * @param title
	 * @param message
	 */
	public void showMessage(String title, String message) {
		Builder builder = new Builder(this);
		builder.setCancelable(true);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.show();
	}

	/**
	 * An instance of Cursor does an SQL query on the 'Shows' database and shows its
	 * result invoking the showMessage() method.
	 * @param view
	 */
	public void selectAll(View view) {
		if (view == btnSelectAll) {
			Cursor c = db.rawQuery("SELECT * FROM shows", null);
			if (c.getCount() == 0) {
				showMessage(getString(R.string.error), getString(R.string.no_records_were_found_));
				return;
			}
			StringBuffer buffer = new StringBuffer();
			while (c.moveToNext()) {
				buffer.append(c.getString(0) + ": " + c.getString(1) + "\n");
			}
			showMessage(getString(R.string.list_of_seen_episodes),buffer.toString());
		}
	}

}
