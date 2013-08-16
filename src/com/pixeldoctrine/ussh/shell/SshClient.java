package com.pixeldoctrine.ussh.shell;

import java.util.Locale;

import android.os.AsyncTask;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class SshClient {

	private ConsoleInput inStream;
	private ConsoleOutput outStream;
	private Channel channel;
	private String host;

	public SshClient(ConsoleInput inStream, ConsoleOutput outStream) {
		this.inStream = inStream;
		this.outStream = outStream;
	}

	public void backgroundConnect(String hostname, int port, String username) {
		new BackgroundConnectTask().execute(new SshConnectParams(hostname, port, username));
	}

	public void connect(String hostname, int port, String username) throws Exception {
		host = hostname;
		JSch jsch = new JSch();
		Session session = jsch.getSession(username, hostname, port);
		session.setUserInfo(new MyUserInfo());
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect(15000);	// Connect with timeout.
		channel = session.openChannel("shell");
		channel.setInputStream(inStream);
		channel.setOutputStream(outStream);
		channel.setExtOutputStream(outStream);
		channel.connect(3*1000);
	}

	public void disconnect() {
		channel.disconnect();
		try {
			channel.getSession().disconnect();
		} catch (Exception e) {
		}
	}

	private class MyUserInfo implements UserInfo, UIKeyboardInteractive{
		public boolean promptYesNo(String str) {
			outStream.print(str + " [y/n] ");
			String answer = inStream.waitChar();
			outStream.print("\n");
			boolean yes = (answer != null && answer.trim().toLowerCase(Locale.ENGLISH).startsWith("y"));
			return yes;
		}
		public String getPassword() {
			String pw = inStream.waitLine();
			inStream.setPasswordMode(false);
			return pw;
		}
		public String getPassphrase() {
			String pw = inStream.waitLine();
			inStream.setPasswordMode(false);
			return pw;
		}
		public boolean promptPassphrase(String message) {
			outStream.print(message + ":\n");
			inStream.setPasswordMode(true);
			return true;
		}
		public boolean promptPassword(String message) {
			outStream.print(message + ":\n");
			inStream.setPasswordMode(true);
			return true;
		}
		public void showMessage(String message) {
			outStream.print(message + "\n");
		}
		public String[] promptKeyboardInteractive(String destination, String name, String instruction,
				String[] prompt, boolean[] echo) {
			outStream.print("promptKeyboardInteractive\n");
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
					try {
						Thread.sleep(100);
						outStream.setPipeToNull(true);
						int w = outStream.getCharWidth();
						int h = outStream.getCharHeight();
						inStream.appendText("stty cols " + w + "; stty rows " + h + "\n");
						Thread.sleep(100);
					} catch (Exception e) {
					}
					outStream.setPipeToNull(false);
					while (channel.isConnected()) {
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					error = e;
				}
			}
			return 0L;
		}
		protected void onProgressUpdate(Integer... progress) {
		}
		protected void onPostExecute(Long result) {
			if (error != null) {
				outStream.print(error.getMessage() + "\n");
			} else {
				outStream.print("\nConnection to " + host + " closed.\n");
				inStream.close();
			}
		}
	}
}
