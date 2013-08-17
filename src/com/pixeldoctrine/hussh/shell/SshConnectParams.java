package com.pixeldoctrine.hussh.shell;

import java.io.Serializable;

public class SshConnectParams implements Serializable {

	private static final long serialVersionUID = 1L;
	public String hostname;
	public int port;
	public String username;

	public SshConnectParams(String hostname, int port, String username) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SshConnectParams)) {
			return false;
		}
		SshConnectParams other = (SshConnectParams) o;
		return other.hostname.equals(hostname) &&
				other.port == port &&
				other.username.equals(username);
	}

	@Override
	public String toString() {
		String s = hostname;
		if (username != null && username.length() > 0) {
			s = username + "@" + hostname;
		}
		if (port != 22) {
			s += ":" + port;
		}
		return s;
	}
}