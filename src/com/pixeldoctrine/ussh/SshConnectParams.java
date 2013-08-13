package com.pixeldoctrine.ussh;

public class SshConnectParams {
	public String hostname;
	public int port;
	public String username;
	public SshConnectParams(String hostname, int port, String username) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
	}
}