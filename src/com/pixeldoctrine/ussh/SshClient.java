package com.pixeldoctrine.ussh;

import java.io.IOException;

import android.os.AsyncTask;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class SshClient {

	private ConsoleInput inStream;
	private ConsoleOutput outStream;

	public SshClient(ConsoleInput inStream, ConsoleOutput outStream) {
		this.inStream = inStream;
		this.outStream = outStream;
	}

	public void backgroundConnect(String hostname, int port, String username) {
		new BackgroundConnectTask().execute(new SshConnectParams(hostname, port, username));
	}

	public void connect(String hostname, int port, String username) throws Exception {
		JSch jsch = new JSch();
		Session session = jsch.getSession(username, hostname, port);
		session.setUserInfo(new MyUserInfo());
		session.connect(15000);	// Connect with timeout.
		Channel channel = session.openChannel("shell");
		channel.setInputStream(inStream);
		channel.setOutputStream(outStream);
		channel.connect(3*1000);
	}

	private class MyUserInfo implements UserInfo, UIKeyboardInteractive{
		public String getPassword() {
			outStream.print("Trying to fetch password...\n");
			return null;
		}
		public boolean promptYesNo(String str) {
			outStream.print(str + "\n");
			String answer = inStream.waitString();
			boolean yes = (answer != null && answer.trim().toLowerCase().startsWith("y"));
			return yes;
		}
		public String getPassphrase() {
			outStream.print("Trying to fetch passphrase...\n");
			return null;
		}
		public boolean promptPassphrase(String message) {
			outStream.print("Passphrase prompt:\n");
			outStream.print(message + "\n");
			return false;
		}

		public boolean promptPassword(String message) {
			outStream.print("Password prompt:\n");
			outStream.print(message + "\n");
			return false;
		}

		public void showMessage(String message) {
			outStream.print(message + "\n");
		}
		public String[] promptKeyboardInteractive(String destination, String name, String instruction,
				String[] prompt, boolean[] echo) {
			outStream.print(destination + "\n" + name + "\n" + instruction + "\n");
			for (String s : prompt) {
				outStream.print(s + "\n");
			}
			return null;
		}
	}

	private class BackgroundConnectTask extends AsyncTask<SshConnectParams, Integer, Long> {
		private Exception error;
		protected Long doInBackground(SshConnectParams... params) {
			for (SshConnectParams param : params) {
				try {
					connect(param.hostname, param.port, param.username);
				} catch (Exception e) {
					error = e;
					return -1L;
				}
	         }
			return 0L;
	     }
	     protected void onProgressUpdate(Integer... progress) {
	     }
	     protected void onPostExecute(Long result) {
	    	 try {
				outStream.write(error.getMessage().getBytes());
			} catch (IOException e) {
			}
	     }
	 }
}
