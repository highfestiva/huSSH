package com.pixeldoctrine.ussh;

import com.pixeldoctrine.ussh.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

	public final static String HOSTNAME = "com.pixeldoctrine.ussh.HOSTNAME";
	public final static String PORT		= "com.pixeldoctrine.ussh.PORT";
	public final static String USERNAME	= "com.pixeldoctrine.ussh.USERNAME";
	private String hostname = "pixeldoctrine.dyndns.org";
	private String port = "22";
	private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onConnect(View view) {
    	Intent intent = new Intent(this, ShellActivity.class);
    	EditText hostname = (EditText) findViewById(R.id.hostname);
    	intent.putExtra(HOSTNAME, hostname.getText().toString());
    	EditText port = (EditText) findViewById(R.id.port);
    	intent.putExtra(PORT, Integer.valueOf(port.getText().toString()));
    	EditText username = (EditText) findViewById(R.id.username);
    	intent.putExtra(USERNAME, username.getText().toString());
    	startActivity(intent);
    }

    /**
     * Action button response
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                //openSearch();
                return true;
            case R.id.action_settings:
                //openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onPause() {
		super.onPause();
    	EditText hostnameText = (EditText) findViewById(R.id.hostname);
    	EditText portText = (EditText) findViewById(R.id.port);
    	EditText usernameText = (EditText) findViewById(R.id.username);
    	hostname = hostnameText.getText().toString();
    	port = portText.getText().toString();
    	username = usernameText.getText().toString();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString("host", hostname);
		savedInstanceState.putString("port", port);
		savedInstanceState.putString("user", username);
	}

	@Override
	public void onStart() {
		super.onStart();
    	EditText hostnameText = (EditText) findViewById(R.id.hostname);
    	EditText portText = (EditText) findViewById(R.id.port);
    	EditText usernameText = (EditText) findViewById(R.id.username);
		hostnameText.setText(hostname);
		portText.setText(port);
		usernameText.setText(username);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		hostname = savedInstanceState.getString("host");
		port = savedInstanceState.getString("port");
		username = savedInstanceState.getString("user");
	}
}
