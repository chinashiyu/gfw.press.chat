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

import java.sql.Timestamp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ViewActivity extends Activity implements OnClickListener {

	private Button decryptButton, passwordButton, saveButton, cancelButton, replyButton;

	private EditText passwordEditText, repasswordEditText;

	private TextView fromTextView, decryptTextView, dateTextView, cipherTextView;

	private String from, cipher;

	private long date;

	private Encrypt aes;

	private SharedPreferences passwordPref;

	private SharedPreferences.Editor passwordPrefEditor;

	@Override
	public void onClick(View v) {

		if (v == replyButton) { // 回复信息

			Intent intent = new Intent(this, SendActivity.class);

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			intent.putExtra("TO", from);

			this.startActivity(intent);

		} else if (v == passwordButton) { // 设置密码

			passwordEditText.setText("");

			repasswordEditText.setText("");

			passwordButton.setVisibility(View.GONE);

			replyButton.setVisibility(View.GONE);

			decryptButton.setVisibility(View.GONE);

			passwordEditText.setVisibility(View.VISIBLE);

			repasswordEditText.setVisibility(View.VISIBLE);

			saveButton.setVisibility(View.VISIBLE);

			cancelButton.setVisibility(View.VISIBLE);

			passwordEditText.requestFocus();

		} else if (v == saveButton) { // 保存密码

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

			String _from = aes.getMD5(from + AppId.id(this));

			if (password.length() == 0) {

				passwordPrefEditor.remove(_from);

				passwordPrefEditor.commit();

				toast("密码已被清除");

			} else {

				if ((password = aes.getMD5(password)) == null) {

					toast("密码包含无法识别的字符");

					passwordEditText.requestFocus();

					return;

				}

				String oldPassword = passwordPref.getString(_from, "").trim();

				if (!password.equals(oldPassword)) {

					passwordPrefEditor.putString(_from, password);

					passwordPrefEditor.commit();

				}

				toast("密码保存成功");

			}

			passwordEditText.setVisibility(View.GONE);

			repasswordEditText.setVisibility(View.GONE);

			saveButton.setVisibility(View.GONE);

			cancelButton.setVisibility(View.GONE);

			passwordButton.setVisibility(View.VISIBLE);

			decryptButton.setVisibility(View.VISIBLE);

			replyButton.setVisibility(View.VISIBLE);

		} else if (v == cancelButton) { // 取消修改

			passwordEditText.setText("");

			repasswordEditText.setText("");

			passwordEditText.setVisibility(View.GONE);

			repasswordEditText.setVisibility(View.GONE);

			saveButton.setVisibility(View.GONE);

			cancelButton.setVisibility(View.GONE);

			passwordButton.setVisibility(View.VISIBLE);

			decryptButton.setVisibility(View.VISIBLE);

			replyButton.setVisibility(View.VISIBLE);

		} else if (v == decryptButton) { // 解密显示

			String _from = aes.getMD5(from + AppId.id(this));

			String password = passwordPref.getString(_from, "");

			if (password.length() != 32) {

				toast("请设置密码");

				passwordButton.requestFocus();

				return;

			}

			String message = aes.decryptMessage(password, cipher);

			if (message == null) {

				decryptTextView.setText("");

				toast("解密失败");

				return;

			}

			decryptTextView.setText(message);

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.view_activity);

		cipherTextView = (TextView) findViewById(R.id.cipherTextView);

		dateTextView = (TextView) findViewById(R.id.dateTextView);

		fromTextView = (TextView) findViewById(R.id.fromTextView);

		decryptTextView = (TextView) findViewById(R.id.decryptTextView);

		passwordEditText = (EditText) findViewById(R.id.passwordEditText);

		repasswordEditText = (EditText) findViewById(R.id.repasswordEditText);

		replyButton = (Button) findViewById(R.id.replyButton);

		passwordButton = (Button) findViewById(R.id.passwordButton);

		saveButton = (Button) findViewById(R.id.saveButton);

		cancelButton = (Button) findViewById(R.id.cancelButton);

		decryptButton = (Button) findViewById(R.id.decryptButton);

		passwordPref = getSharedPreferences(SendActivity.PASSWORD, Context.MODE_PRIVATE);

		passwordPrefEditor = passwordPref.edit();

		aes = new Encrypt();

		replyButton.setOnClickListener(this);

		passwordButton.setOnClickListener(this);

		saveButton.setOnClickListener(this);

		cancelButton.setOnClickListener(this);

		decryptButton.setOnClickListener(this);

		Intent intent = getIntent();

		from = (String) intent.getSerializableExtra("FROM");

		cipher = (String) intent.getSerializableExtra("CIPHER");

		date = (Long) intent.getSerializableExtra("DATE");

		cipherTextView.setText(cipher);

		fromTextView.setText(from);

		dateTextView.setText(new Timestamp(date).toString().substring(2, 16));

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
