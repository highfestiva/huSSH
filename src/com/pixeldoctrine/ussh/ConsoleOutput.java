package com.pixeldoctrine.ussh;

import java.io.IOException;
import java.io.OutputStream;

import android.widget.EditText;

public class ConsoleOutput extends OutputStream {

	private EditText console;
	private byte[] tempData = new byte[1024];
	private int offset = 0;
	private boolean doPost = true;

	public ConsoleOutput(EditText console) {
		this.console = console;
	}

	public void print(String s) {
		byte[] data = s.getBytes();
		try {
			write(data, 0, data.length);
		} catch (IOException e) {
		}
	}

	@Override
	public synchronized void write(byte[] buffer, int offset, int count) throws IOException {
		doPost = false;
		super.write(buffer, offset, count);
		doPost = true;
		post();
	}

	@Override
	public synchronized void write(int oneByte) throws IOException {
		tempData[offset++] = (byte) oneByte;
		if (doPost) {
			post();
		}
	}

	private void post() {
		console.post(new Runnable() {
			public void run(){
				synchronized (ConsoleOutput.this) {
					String text = new String(tempData);
					console.append(text);
				}
			}
		});
	}
}