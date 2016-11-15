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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec_.binary.Base64;
import org.apache.commons.codec_.digest.DigestUtils;

import android.annotation.SuppressLint;

public class Encrypt {

	public static final String GB = "GB18030";

	public static final String ISO = "ISO-8859-1";

	public static final String UTF = "UTF-8";

	public static final int IV_SIZE = 16; // IV字节长度，16

	public static void main(String[] args) throws Exception {

		Encrypt aes = new Encrypt();

		// 测试
		// aes.testSecureRandom();

		aes.testEncrypt();

	}

	private Cipher cipher = null;

	private KeyGenerator keyGenerator = null;

	private SecureRandom secureRandom = null;

	@SuppressLint("TrulyRandom")
	public Encrypt() {

		super();

		secureRandom = new SecureRandom();

		try {

			cipher = Cipher.getInstance("AES/CFB/NoPadding"); // Advanced
																// Encryption
																// Standard -
																// Cipher
																// Feedback Mode
																// - No Padding

			keyGenerator = KeyGenerator.getInstance("AES");

		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {

			throw new RuntimeException(ex);

		}

	}

	/**
	 * 解密
	 *
	 * @param key
	 *            SecretKey
	 * @param encrypt_bytes
	 *            头部包含16字节IV的加密数据
	 *
	 * @return 解密数据
	 *
	 */
	public byte[] decrypt(SecretKey key, byte[] encrypt_bytes) {

		if (key == null || encrypt_bytes == null || encrypt_bytes.length < IV_SIZE) {

			return null;

		}

		byte[] IV = new byte[IV_SIZE];

		byte[] part2 = new byte[encrypt_bytes.length - IV_SIZE];

		System.arraycopy(encrypt_bytes, 0, IV, 0, IV.length);

		System.arraycopy(encrypt_bytes, IV.length, part2, 0, part2.length);

		return decrypt(key, part2, IV);

	}

	/**
	 * 解密
	 *
	 * @param key
	 *            SecretKey
	 * @param cipher_data
	 *            加密数据
	 * @param IV
	 *            IV
	 *
	 * @return 解密数据
	 *
	 */
	public byte[] decrypt(SecretKey key, byte[] cipher_data, byte[] IV) {

		if (key == null || cipher_data == null || cipher_data.length == 0 || IV == null || IV.length == 0) {

			return null;

		}

		IvParameterSpec IVSpec = new IvParameterSpec(IV);

		try {

			cipher.init(Cipher.DECRYPT_MODE, key, IVSpec);

		} catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {

			log("初始化Cipher出错：");

			ex.printStackTrace();

			return null;

		}

		try {

			return cipher.doFinal(cipher_data);

		} catch (IllegalBlockSizeException | BadPaddingException ex) {

			log("加密数据出错：");

			ex.printStackTrace();

			return null;

		}

	}

	/**
	 * 解密信息
	 * 
	 * @param key
	 * @param cipher
	 * @return
	 */
	public String decryptMessage(String md5, String cipher) {

		if (cipher == null || (cipher = cipher.trim()).length() == 0) {

			return null;

		}

		SecretKey key = getSecretKey(md5);

		if (key == null) {

			return null;

		}

		try {

			byte[] bytes = decrypt(key, Base64.decodeBase64(cipher.getBytes(ISO)));

			if (bytes != null) {

				return new String(bytes, GB);

			}

		} catch (UnsupportedEncodingException ex) {

			ex.printStackTrace();

		}

		return null;

	}

	/**
	 * 加密
	 *
	 * @param key
	 *            SecretKey
	 * @param data
	 *            数据
	 *
	 * @return 加密数据
	 *
	 */
	public byte[] encrypt(SecretKey key, byte[] data) {

		if (key == null || data == null) {

			return null;

		}

		byte[] IV = getSecureRandom(IV_SIZE);

		IvParameterSpec IVSpec = new IvParameterSpec(IV);

		try {

			cipher.init(Cipher.ENCRYPT_MODE, key, IVSpec);

		} catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {

			log("初始化Cipher出错：");

			ex.printStackTrace();

			return null;

		}

		byte[] cipher_bytes = null;

		try {

			cipher_bytes = cipher.doFinal(data);

		} catch (IllegalBlockSizeException | BadPaddingException ex) {

			log("加密数据出错：");

			ex.printStackTrace();

			return null;

		}

		byte[] iv_cipher_bytes = new byte[cipher_bytes.length + IV_SIZE];

		System.arraycopy(IV, 0, iv_cipher_bytes, 0, IV.length);

		System.arraycopy(cipher_bytes, 0, iv_cipher_bytes, IV.length, cipher_bytes.length);

		return iv_cipher_bytes;

	}

	/**
	 * 加密信息
	 * 
	 * @param md5
	 * @param message
	 * @return
	 */
	public String encryptMessage(String md5, String message) {

		if (message == null || (message = message.trim()).length() == 0) {

			return null;

		}

		SecretKey key = getSecretKey(md5);

		if (key == null) {

			return null;

		}

		try {

			byte[] bytes = encrypt(key, message.getBytes(GB));

			if (bytes != null) {

				return new String(Base64.encodeBase64(bytes), ISO).replaceAll("=*$", "");

			}

		} catch (UnsupportedEncodingException ex) {

			ex.printStackTrace();

		}

		return null;

	}

	/**
	 * 生成256位SecretKey
	 *
	 * @return 256位SecretKey
	 *
	 */
	public SecretKey getKey() {

		return getKey(256);

	}

	/**
	 * 生成指定加密位数的AES SecretKey
	 *
	 * @param bits
	 *            加密位数
	 *
	 * @return SecretKey
	 *
	 */
	public SecretKey getKey(int bits) {

		if (bits < 128) {

			return null;

		}

		try {

			keyGenerator.init(bits);

			return keyGenerator.generateKey();

		} catch (InvalidParameterException ex) {

			log("生成AES SecretKey出错：");

			ex.printStackTrace();

			return null;

		}

	}

	/**
	 * 获取密码的MD5字符串
	 * 
	 * @param password
	 * @return
	 */
	public String getMD5(String password) {

		if (password == null || (password = password.trim()).length() == 0) {

			return null;

		}

		try {

			return DigestUtils.md5Hex((password + " GFW.Press Chat").getBytes(UTF));

		} catch (UnsupportedEncodingException ex) {

			ex.printStackTrace();

		}

		return null;

	}

	/**
	 * 使用密码生成SecretKey
	 *
	 * @param password
	 *            密码，必须符合isPassword()要求的标准
	 *
	 * @return SecretKey
	 *
	 */
	public SecretKey getPasswordKey(String password) {

		if (!isPassword(password)) {

			return null;

		}

		try {

			return getSecretKey(DigestUtils.md5Hex(password.getBytes(UTF)));

		} catch (UnsupportedEncodingException ex) {

			log("使用密码生成SecretKey出错：");

			ex.printStackTrace();

			return null;

		}

	}

	/**
	 * 使用SecretKey字符串还原SecretKey
	 *
	 * @param stringKey
	 *            SecretKey字符串
	 *
	 * @return SecretKey
	 *
	 */
	public SecretKey getSecretKey(String stringKey) {

		if (stringKey == null || (stringKey = stringKey.trim()).length() < 24) {

			return null;

		}

		byte[] bytes = Base64.decodeBase64(stringKey);

		return new SecretKeySpec(bytes, 0, bytes.length, "AES");

	}

	/**
	 * 生成指定长度的SecureRandom
	 *
	 * @param size
	 *            指定长度
	 * @return
	 */
	public byte[] getSecureRandom(int size) {

		byte[] bytes = new byte[size];

		secureRandom.nextBytes(bytes);

		return bytes;

	}

	/**
	 * 获取SecretKey的字符串
	 *
	 * @param secretKey
	 *            SecretKey
	 *
	 * @return SecretKey的字符串
	 *
	 */
	public String getStringKey(SecretKey secretKey) {

		if (secretKey == null) {

			return null;

		}

		return Base64.encodeBase64String(secretKey.getEncoded());

	}

	/**
	 * 检查密码是否合格
	 *
	 * 要求：至少4个字母或数字
	 *
	 * @param password
	 * @return
	 */
	public boolean isPassword(String password) {

		return !(password == null || !password.matches("[0-9a-zA-Z]{4,}$"));

		// 1、长度至少为八个字符 2、至少包含一个数字 3、至少包含一个大写字母 4、至少包含一个小写字母 5、不得包含空格
		// password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

	}

	/**
	 * 打印信息
	 *
	 * @param o
	 */
	private void log(Object o) {

		String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

		System.out.println("[" + time + "] " + o.toString());

	}

	public void testEncrypt() throws Exception {

		String password = "Abc12345";

		log("加密测试");

		String message = "你好！你好！你好！你好！你好！你好！你好！他好！你好！你好！你好！你好！你好！测A";

		log("加密前：" + message);

		log("加密前长度：" + message.length());

		log("加密前字节长度：" + message.getBytes(GB).length);

		long start = System.currentTimeMillis();

		String cipher = encryptMessage(password, message);

		log("加密后：" + cipher);

		log("加密后长度：" + cipher.length());

		long end = System.currentTimeMillis();

		log("时间：" + (end - start));

		log("\n解密测试");

		start = System.currentTimeMillis();

		String _message = decryptMessage(password, cipher);

		end = System.currentTimeMillis();

		log("解密后：" + _message);

		log("时间：" + (end - start));

	}

	public void testIsPassword() {

		String password = "xxXxab12";

		log(isPassword(password));

	}

	public void testSecureRandom() {

		// secureRandom.nextBytes(bytes);

		// secureRandom.nextBytes(bytes);

		long start = System.currentTimeMillis();

		byte[] bytes = this.getSecureRandom(1024);

		long end = System.currentTimeMillis();

		log("时间：" + (end - start));

		try {

			log(new String(bytes, UTF));

		} catch (UnsupportedEncodingException ex) {

			ex.printStackTrace();

		}

	}

}
