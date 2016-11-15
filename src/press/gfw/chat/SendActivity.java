/**
 * GFW.Press Chat
 * Copyright (C) 2016  chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package press.gfw.chat;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendActivity extends Activity implements OnClickListener {

	public static final String PASSWORD = "press.gfw.chat.password";

	private EditText messageEditText, toEditText, passwordEditText, repasswordEditText;

	private Button sendButton, passwordButton, saveButton, cancelButton;

	private Encrypt aes;

	private SmsManager smsManager;

	private SharedPreferences passwordPref;

	private SharedPreferences.Editor passwordPrefEditor;

	private String to = "";

	@Override
	public void onClick(View v) {

		if (v == passwordButton) { // 设置密码

			String to = toEditText.getText().toString().trim();

			if (to.length() == 0) {

				toast("请输入接收号码");

				toEditText.requestFocus();

				return;

			}

			passwordEditText.setText("");

			repasswordEditText.setText("");

			passwordEditText.requestFocus();

			messageEditText.setVisibility(View.GONE);

			passwordButton.setVisibility(View.GONE);

			sendButton.setVisibility(View.GONE);

			passwordEditText.setVisibility(View.VISIBLE);

			repasswordEditText.setVisibility(View.VISIBLE);

			saveButton.setVisibility(View.VISIBLE);

			cancelButton.setVisibility(View.VISIBLE);

		} else if (v == saveButton) { // 保存密码

			String to = toEditText.getText().toString().trim();

			if (to.length() == 0) {

				toast("请输入接收号码");

				toEditText.requestFocus();

				return;

			}

			String password = passwordEditText.getText().toString().trim();

			String repassword = repasswordEditText.getText().toString().trim();

			if (!password.equals(repassword)) {

				toast("两次输入的密码必须相同");

				repasswordEditText.requestFocus();

				return;

			}

			if (password.length() > 0 && !aes.isPassword(password)) {

				toast("密码至少需要4个英文数字");

				passwordEditText.requestFocus();

				return;

			}

			String _to = aes.getMD5(to + AppId.id(this));

			if (password.length() == 0) {

				passwordPrefEditor.remove(_to);

				passwordPrefEditor.commit();

				toast("密码已被清除");

			} else {

				if ((password = aes.getMD5(password)) == null) {

					toast("密码包含无法识别的字符");

					passwordEditText.requestFocus();

					return;

				}

				String oldPassword = passwordPref.getString(_to, "").trim();

				if (!password.equals(oldPassword)) {

					passwordPrefEditor.putString(_to, password);

					passwordPrefEditor.commit();

				}

				toast("密码保存成功");

			}

			passwordEditText.setVisibility(View.GONE);

			repasswordEditText.setVisibility(View.GONE);

			saveButton.setVisibility(View.GONE);

			cancelButton.setVisibility(View.GONE);

			messageEditText.setVisibility(View.VISIBLE);

			passwordButton.setVisibility(View.VISIBLE);

			sendButton.setVisibility(View.VISIBLE);

		} else if (v == cancelButton) { // 取消修改

			passwordEditText.setText("");

			repasswordEditText.setText("");

			passwordEditText.setVisibility(View.GONE);

			repasswordEditText.setVisibility(View.GONE);

			saveButton.setVisibility(View.GONE);

			cancelButton.setVisibility(View.GONE);

			messageEditText.setVisibility(View.VISIBLE);

			passwordButton.setVisibility(View.VISIBLE);

			sendButton.setVisibility(View.VISIBLE);

			messageEditText.requestFocus();

		} else if (v == sendButton) { // 加密发送

			String to = toEditText.getText().toString().trim();

			if (to.length() == 0) {

				toast("请输入接收号码");

				toEditText.requestFocus();

				return;

			}

			String message = messageEditText.getText().toString().trim();

			if (message.length() < 1) {

				toast("请输入发送内容");

				messageEditText.requestFocus();

				return;

			}

			try {

				int messageLength = message.getBytes(Encrypt.UTF).length;

				if (messageLength > 120) {

					toast("内容太长，已超出" + ((messageLength - 120) / 3 + ((messageLength - 120) % 3) == 0 ? 0 : 1) + "个汉字或" + (messageLength - 120) + "个字符");

					return;

				}

			} catch (UnsupportedEncodingException ex) {

				toast("信息包含无法处理的字符");

				messageEditText.requestFocus();

				return;

			}

			String _to = aes.getMD5(to + AppId.id(this));

			String password = passwordPref.getString(_to, "").trim();

			if (password.length() != 32) {

				toast("请设置密码");

				passwordButton.requestFocus();

				return;

			}

			String cipher = aes.encryptMessage(password, message);

			if (cipher == null) {

				toast("加密失败");

				return;

			}

			if (cipher.length() > 140) {

				toast("内容太长，最多40个汉字或120个英文数字");

				messageEditText.requestFocus();

				return;

			}

			sendButton.setEnabled(false);

			try {

				smsManager.sendTextMessage(to, null, cipher, null, null);

				toast("发送成功");

			} catch (Exception ex) {

				toast("发送失败：" + ex.getMessage());

			}

			sendButton.setEnabled(true);

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.send_activity);

		messageEditText = (EditText) findViewById(R.id.messageEditText);

		toEditText = (EditText) findViewById(R.id.toEditText);

		passwordEditText = (EditText) findViewById(R.id.passwordEditText);

		repasswordEditText = (EditText) findViewById(R.id.repasswordEditText);

		passwordButton = (Button) findViewById(R.id.passwordButton);

		saveButton = (Button) findViewById(R.id.saveButton);

		cancelButton = (Button) findViewById(R.id.cancelButton);

		sendButton = (Button) findViewById(R.id.sendButton);

		aes = new Encrypt();

		smsManager = SmsManager.getDefault();

		passwordPref = getSharedPreferences(PASSWORD, Context.MODE_PRIVATE);

		passwordPrefEditor = passwordPref.edit();

		Intent intent = getIntent();

		if (intent != null) {

			to = (String) intent.getSerializableExtra("TO");

			if (to != null && (to = to.trim()).length() > 0) {

				toEditText.setText(to);

				messageEditText.requestFocus();

			}

		}

		passwordButton.setOnClickListener(this);

		saveButton.setOnClickListener(this);

		cancelButton.setOnClickListener(this);

		sendButton.setOnClickListener(this);

	}

	/**
	 * 显示提示信息
	 * 
	 * @param tips
	 */
	private void toast(String tips) {

		Toast.makeText(this, tips, Toast.LENGTH_SHORT).show();

	}

}
