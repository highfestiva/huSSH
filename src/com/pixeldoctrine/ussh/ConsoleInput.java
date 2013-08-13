package com.pixeldoctrine.ussh;

import java.io.IOException;
import java.io.InputStream;

import android.text.Editable;
import android.text.InputType;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class ConsoleInput extends InputStream {

	private byte[] data = new byte[0];
	private int offset = 0;

	public ConsoleInput(EditText con) {
		con.setKeyListener(new ConKeyListener());
	}

	public synchronized void appendText(String text) {
		byte[] a = data;
		byte[] b = text.getBytes();
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		data = c;
		notify();
	}

	@Override
	public synchronized int read() throws IOException {
		if (offset < data.length) {
			int b = data[offset++];
			if (offset == data.length) {
				data = new byte[0];
				offset = 0;
			}
			return b;
		}
		return -1;
	}

	public synchronized String waitString() {
		try {
			byte[] buffer = new byte[1024];
			int byteCount;
			do {
				wait();
				byteCount = read(buffer);
			} while (byteCount <= 0);
			return new String(buffer, 0, byteCount);
		} catch (InterruptedException e) {
		} catch (IOException e) {
		}
		return null;
	}

	private class ConKeyListener implements KeyListener {
		@Override
		public void clearMetaKeyState(View view, Editable ed, int n) {
		}
		@Override
		public int getInputType() {
			return InputType.TYPE_CLASS_TEXT;
		}
		@Override
		public boolean onKeyDown(View view, Editable ed, int n, KeyEvent e) {
			appendText(e.getCharacters());
			return true;
		}
		@Override
		public boolean onKeyOther(View view, Editable ed, KeyEvent e) {
			return false;
		}
		@Override
		public boolean onKeyUp(View view, Editable ed, int n, KeyEvent e) {
			return false;
		}
	}
}