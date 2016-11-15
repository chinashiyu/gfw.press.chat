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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.content.Context;

public class AppId {

	private static String uuid = null;

	private static final String APPID = "APPID";

	public synchronized static String id(Context context) {

		if (uuid != null) {

			return uuid;

		}

		File file = new File(context.getFilesDir(), APPID);

		try {

			if (!file.exists()) {

				write(file);

			}

			uuid = read(file);

		} catch (Exception e) {

			throw new RuntimeException(e);

		}

		return uuid;

	}

	private static String read(File file) throws IOException {

		RandomAccessFile f = new RandomAccessFile(file, "r");

		byte[] bytes = new byte[(int) f.length()];

		f.readFully(bytes);

		f.close();

		return new String(bytes);

	}

	private static void write(File file) throws IOException {

		FileOutputStream out = new FileOutputStream(file);

		String id = UUID.randomUUID().toString();

		out.write(id.getBytes());

		out.close();

	}

}
