package com.pixeldoctrine.ussh.shell;

import java.io.InputStream;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

public class ConsoleInput extends InputStream {

	private EditText con;
	private byte[] data = new byte[0];
	private int offset = 0;
	private boolean ignore = false;

	public ConsoleInput(EditText con) {
		this.con = con;
		this.con.addTextChangedListener(new TextChangeListener());
	}

	@Override
	public int read(byte[] data, int offset, int count) {
		int minCount = Math.max(available(), 1);
		int cnt = Math.min(count, minCount);
		int i;
		for (i = 0; i < cnt; ++i) { 
			data[i + offset] = (byte) read();
		}
		return i;
	}

	@Override
	public synchronized int read() {
		try {
			while (offset >= data.length) {
				wait();
			}
			int b = data[offset++];
			if (offset == data.length) {
				data = new byte[0];
				offset = 0;
			}
			return b;
		} catch (InterruptedException e) {
			return -1;
		}
	}

	@Override
	public synchronized int available() {
		return data.length - offset;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void close() {
		con.setEnabled(false);
	}

	public synchronized void appendText(String text) {
		if (data.length == 0) {
			data = text.getBytes();
		} else {
			byte[] a = data;
			byte[] b = text.getBytes();
			byte[] c = new byte[a.length + b.length];
			System.arraycopy(a, 0, c, 0, a.length);
			System.arraycopy(b, 0, c, a.length, b.length);
			data = c;
		}
		notify();
	}

	@SuppressLint("NewApi")
	public synchronized int removeText(int byteCount) {
		if (data.length >= byteCount) {
			data = Arrays.copyOf(data, byteCount);
		} else if (data.length > 0) {
			byteCount -= data.length;
			data = new byte[0];
		}
		notify();
		return byteCount;
	}

	public void setPasswordMode(final boolean enable) {
		con.post(new Runnable() {
			public void run() {
				int pwFlag = enable? InputType.TYPE_TEXT_VARIATION_PASSWORD : 0;
				con.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE  | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | pwFlag);
				con.setHint(enable? "Enter password" : "$");
			}
		});
	}

	public synchronized String waitChar() {
		byte[] buffer = new byte[32];
		buffer[0] = (byte) read();
		int cnt = Math.min(buffer.length-1, available());
		read(buffer, 1, cnt);
		return new String(buffer, 0, 1+cnt);
	}

	public String waitLine() {
		String s = "";
		for (;;) {
			String ch = waitChar();
			if (ch.equals("\n")) {
				break;
			}
			s += ch;
		}
		return s;
	}

	private class TextChangeListener implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (ignore) {
				return;
			}
			synchronized (ConsoleInput.this) {
				int off = count - before;
				if (off > 0) {
					appendText(s.subSequence(start+count-off, start+count).toString());
					if (s.charAt(s.length()-1) == '\n') {
						con.post(new Runnable() {
							public void run() {
								ignore = true;
								con.setText("");
								ignore = false;
							}
						});
					}
				} else if (off < 0) {
					off += removeText(-off);
					for (int c = 0; c < -off; ++c) {
						appendText("\b");	// Backspace.
					}
				}
			}
		}
	}
}