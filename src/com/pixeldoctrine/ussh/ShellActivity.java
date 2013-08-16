package com.pixeldoctrine.ussh;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.pixeldoctrine.ussh.shell.ConsoleInput;
import com.pixeldoctrine.ussh.shell.ConsoleOutput;
import com.pixeldoctrine.ussh.shell.SshClient;

public class ShellActivity extends Activity {

	private SshClient sshClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the message from the intent
	    Intent intent = getIntent();
	    String hname = intent.getStringExtra(MainActivity.HOSTNAME);
	    int port  = intent.getIntExtra(MainActivity.PORT, 22);
	    String uname = intent.getStringExtra(MainActivity.USERNAME);
	    String info = String.format(getResources().getString(R.string.connecting_info), hname, port, uname);

        setContentView(R.layout.activity_shell);
    	TextView conWidget = (TextView) findViewById(R.id.console);
    	conWidget.setText(info + "\n", TextView.BufferType.SPANNABLE);
    	EditText inputWidget = (EditText) findViewById(R.id.input);
		// Show the Up button in the action bar.
		setupActionBar();

		ConsoleInput in = new ConsoleInput(inputWidget);
		ConsoleOutput out = new ConsoleOutput(conWidget);
		sshClient = new SshClient(in, out);
		sshClient.backgroundConnect(hname, port, uname);
	}

	@Override
	public void onDestroy() {
		sshClient.disconnect();
		sshClient = null;
		super.onDestroy();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
