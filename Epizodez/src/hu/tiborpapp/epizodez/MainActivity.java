package hu.tiborpapp.epizodez;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends ListActivity {
	
	
	private ProgressDialog pDialog;
	private Button btnStart;
	private AutoCompleteTextView autoCompTV;
	private ListView lv;
	private String stringArray[] = null;
	private InputMethodManager mgr;
	
	// URL to get XML - working link
	private String url1 = "http://services.tvrage.com/feeds/search.php?show=";
	private String url2 = "";

	
	// XML NODE names
	private static final String XML_SHOW = "show";
	private static final String XML_SHOWID = "showid";
	private static final String XML_SHOWNAME = "name";
	private static final String XML_STARTED = "started";
	private static final String XML_SEASONS = "seasons";
	private static final String XML_STATUS = "status";
	
	// Hashmap for ListView
	ArrayList<HashMap<String, String>> showList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnStart = (Button) findViewById(R.id.btnStart);	
		showList = new ArrayList<HashMap<String, String>>();
		autoCompTV = (AutoCompleteTextView) findViewById(R.id.edtAutoComp);
		
		mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(btnStart.getWindowToken(),0);
        
        
		// invoking startSearch() method + hiding the keyboard
		btnStart.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startSearch(v);
				InputMethodManager mgr = (InputMethodManager) 
						getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(btnStart.getWindowToken(), 0);
			}
		});
		
		// ...allowing to enter text only in one line and submit with the Enter key + hiding keyboard
		autoCompTV.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int actionID, KeyEvent event) {
				 if (actionID == EditorInfo.IME_ACTION_DONE) {
	                    btnStart.performClick();
	                    mgr.hideSoftInputFromWindow(autoCompTV.getWindowToken(),0);
	                    return true;	                    
	                }
				return false;
			}
		});
			
		// invoking txtReadIn() method to use for AutoCompleteTextView
		txtReadIn();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, stringArray);
		autoCompTV.setAdapter(adapter);
		
		lv = getListView();
		
		// adding Context menu option
		registerForContextMenu(lv);

		// setting ListView on item click listener
		lv.setOnItemClickListener(new OnItemClickListener() {

			// passing the selected show's ID to another activity
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent in = new Intent(getApplicationContext(),
						EpisodeList.class);

				HashMap<String, String> pass = showList.get(position);
				String showID = pass.get("showid");
				String title = pass.get("name");

				in.putExtra("showID", showID);
				in.putExtra("title", title);
				startActivity(in);
			}
		});

		lv.setLongClickable(true);
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	if (v.getId()==lv.getId()) {
    		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
    		HashMap<String, String> map = showList.get(info.position);
    	    String headerTitle = map.get("title");
    		menu.setHeaderTitle(headerTitle);
    		
    		// Context menu pop-up - not working yet
    		String[] menuItems = new String[] 
    				{getResources().getString(R.string.add),
    				getResources().getString(R.string.delete)}; 
    		for (int i = 0; i<menuItems.length; i++) {
    			menu.add(Menu.NONE, i, i, menuItems[i]);
			}
    	}
    }
    	
	
	public void startSearch(View view) {
		// Calling AsyncTask in order to get XML

		if (!isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.no_active_internet_connection_, Toast.LENGTH_LONG).show();
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.enable_wi_fi_connection_);
			alert.setMessage(R.string.can_t_download_data_without_internet_connection_);

			alert.setPositiveButton(R.string.enable,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							// anonymous object
							final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
							final boolean wifiEnabled = wifiManager.isWifiEnabled();
							if (!wifiEnabled) {
								wifiManager.setWifiEnabled(true);
							}
						}
					});

			alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int whichButton) {
							return;
						}
					});

			alert.show();
		}
		// making and executing a new GetData object if the device is connected to the Internet
		if (isNetworkAvailable(getApplicationContext()) == true) {
			GetData getDataObject = new GetData();
			getDataObject.execute();
			showList.clear();
		}

	}

	/**
	 * AsyncTask class to get XML by making HTTP call
	 * */
	public class GetData extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setTitle(R.string.downloading_show_data_);
			pDialog.setMessage(getResources().getString(R.string.please_wait_));
			pDialog.setCancelable(false);
			pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, 
					getResources().getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			pDialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {

			ServiceHandler sh = new ServiceHandler();

			// making a request to URL and getting response
			url2 = autoCompTV.getText().toString().replace(" ", "+");
			final String finalURL = url1 + url2;

			String response = sh.makeServiceCall(finalURL, ServiceHandler.GET);
			Log.d("#RESPONSE#", response);

			try {

				// ===================================== XML ================================================ //

				
				// getting DOM element
				Document doc = sh.getDomElement(response); 

				NodeList nl = doc.getElementsByTagName(XML_SHOW);
				// looping through all item nodes <item>
				for (int i = 0; i < nl.getLength(); i++) {
					// creating new HashMap
					HashMap<String, String> xmlItems = new HashMap<String, String>();
					Element e = (Element) nl.item(i);
					String seasons = sh.getValue(e, XML_SEASONS);

					
					// add child nodes to the HashMap key-value
					xmlItems.put(XML_SHOWID, sh.getValue(e, XML_SHOWID));
					xmlItems.put(XML_SHOWNAME, sh.getValue(e, XML_SHOWNAME));
					xmlItems.put(XML_STARTED, sh.getValue(e, XML_STARTED));
					xmlItems.put(XML_STATUS, sh.getValue(e, XML_STATUS));
					xmlItems.put(XML_SEASONS, seasons + " Seasons");

					// TEST
					String teszt2 = nl.toString();
					Log.d("&&&&&&&", teszt2);
					
					
					// adding HashList to ArrayList
					showList.add(xmlItems);
				}
				// TEST
				Log.d("URL", url2);
				Log.d("finalURL", finalURL);
				Log.d("Response: ", response);

			}

			catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// Dismiss the progress dialog
			if (pDialog.isShowing())
				pDialog.dismiss();

			// Updating parsed XML data into ListView
			ListAdapter adapter = new SimpleAdapter(MainActivity.this,
					showList, R.layout.list_item, new String[] { XML_SHOWNAME,
							XML_STATUS, XML_SEASONS, XML_STARTED }, new int[] {
							R.id.name, R.id.airdate, R.id.seasonno, R.id.epno });
			setListAdapter(adapter);
		}
	}
	
	public void txtReadIn() {
		AssetManager assetMan = this.getAssets();
		try {
			String str = "";
			StringBuffer buffer = new StringBuffer();
			InputStream inputStream = assetMan.open("serieslist.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			if (inputStream != null) {
				while ((str = reader.readLine()) != null) {
					buffer.append(str + "\n");
				}
			}
			inputStream.close();
			stringArray = buffer.toString().split("[\\r\\n]"); // split into new
																// line

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connMan = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connMan.getActiveNetworkInfo() != null
				&& connMan.getActiveNetworkInfo().isConnected())
			return true;
		else
			return false;
	}

}