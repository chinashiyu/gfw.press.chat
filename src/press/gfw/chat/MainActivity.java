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
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {

	private Button sendButton;

	private ListView messageListView;

	private SimpleCursorAdapter adapter;

	/**
	 * 列出最近一百天内最新的一千条消息
	 * 
	 * @param type
	 */
	private void list(String type) {

		long last = System.currentTimeMillis() - 100 * 24 * 3600 * 1000L; // 100天前

		// inbox / sent / draft
		Uri uri = Uri.parse("content://sms/" + type);

		String[] cols = new String[] { "_id", "address", "body", "date" };

		ContentResolver cr = getContentResolver();

		Cursor c = cr.query(uri, cols, "date>" + last, null, "date DESC LIMIT 1000");

		adapter = new SimpleCursorAdapter(this, R.layout.messgae_row, c, new String[] { "address", "body", "date" }, new int[] { R.id.addressTextView, R.id.bodyTextView, R.id.dateTextView });

		adapter.setViewBinder(new ViewBinder() {

			public boolean setViewValue(View view, Cursor cur, int col) {

				if (col == 3) {

					((TextView) view).setText(new Timestamp(cur.getLong(col)).toString().substring(2, 16));

					return true;

				}

				return false;

			}

		});

		messageListView.setAdapter(adapter);

	}

	/**
	 * 发送信息
	 */
	@Override
	public void onClick(View v) {

		if (v == sendButton) {

			Intent sendIntent = new Intent(this, SendActivity.class);

			this.startActivity(sendIntent);

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		sendButton = (Button) findViewById(R.id.sendButton);

		sendButton.setOnClickListener(this);

		messageListView = (ListView) findViewById(R.id.messageListView);

		messageListView.setOnItemClickListener(this);

		list("inbox");

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		Cursor cursor = (Cursor) parent.getItemAtPosition(position);

		String from = cursor.getString(cursor.getColumnIndex("address"));

		String cipher = cursor.getString(cursor.getColumnIndex("body"));

		long date = cursor.getLong(cursor.getColumnIndex("date"));

		Intent viewIntent = new Intent(this, ViewActivity.class);

		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		viewIntent.putExtra("DATE", date);

		viewIntent.putExtra("FROM", from);

		viewIntent.putExtra("CIPHER", cipher);

		this.startActivity(viewIntent);

	}

}
