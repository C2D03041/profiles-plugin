package com.awooga.profiles.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HiddenStringUtil {

	// String constants. TODO Change them to something unique to avoid conflict with other plugins!
	private static final char ZWSP = '\u2B4D';
	private static final String TEST = "";//""\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u0009\u0010\u0011\u0012\u0013\u0014";
	private static final String SEQUENCE_HEADER = "\u00A7_\u00A7z\u00A7q";
	private static final String SEQUENCE_FOOTER = "\u00A7_\u00A7q\u00A7z" + TEST;
	private static final char COLOR_CHAR = '\u00A7';
	private static final int CHAR_OFFSET = 100;

	public static void addLore(ItemStack itemStack, Object data) {
		ItemMeta meta = itemStack.getItemMeta();
		List<String> lore = meta.getLore();
		if(lore == null) {
			lore = new ArrayList<>();
		}

		Gson gson = new Gson();
		String json = gson.toJson(data);
		String newLore = encodeString(json);
		//System.out.println("Putting new lore: "+newLore);
		if(lore.size() > 0) {
			lore.set(0, newLore + lore.get(0));
		} else {
			lore.add(newLore);
		}

		meta.setLore(lore);
		itemStack.setItemMeta(meta);
	}

	public static <T> T getLore(ItemStack itemStack, Class<T> className) {
		ItemMeta meta = itemStack.getItemMeta();
		List<String> lore = meta.getLore();

		if(lore == null || lore.size() == 0) { return null; }

		for(String relevantLore : lore) {
			//String relevantLore = lore.get(lore.size() - 1);
			Gson gson = new Gson();
			//System.out.println("Pulling string from: " + relevantLore);
			String hiddenString = extractHiddenString(relevantLore);
			//System.out.println("Got hidden string: " + hiddenString);
			if(hiddenString == null) {
				continue;
			}
			T data = gson.fromJson(hiddenString, className);
			return data;
		}
		return null;
	}

	public static String encodeString(String hiddenString) {
		return quote(stringToColors(hiddenString));
	}

	public static boolean hasHiddenString(String input) {
		if (input == null) return false;

		return input.contains(SEQUENCE_HEADER) && input.contains(SEQUENCE_FOOTER);
	}

	public static String extractHiddenString(String input) {
		return colorsToString(extract(input));
	}


	public static String replaceHiddenString(String input, String hiddenString) {
		if (input == null) return null;

		int start = input.indexOf(SEQUENCE_HEADER);
		int end = input.indexOf(SEQUENCE_FOOTER);

		if (start < 0 || end < 0) {
			return null;
		}

		return input.substring(0, start + SEQUENCE_HEADER.length()) + stringToColors(hiddenString) + input.substring(end, input.length());
	}

	/**
	 * Internal stuff.
	 */
	private static String quote(String input) {
		if (input == null) return null;
		return SEQUENCE_HEADER + input + SEQUENCE_FOOTER;
	}

	private static String extract(String input) {
		if (input == null) return null;

		int start = input.indexOf(SEQUENCE_HEADER);
		int end = input.indexOf(SEQUENCE_FOOTER);
		System.out.println("Got start and end: "+start+", "+end);

		if (start < 0 || end < 0) {
			return null;
		}

		String extracted = input.substring(start + SEQUENCE_HEADER.length(), end);
		//System.out.println("Extracted subsequence: "+extracted);
		return extracted;
	}

	private static String stringToColors(String normal) {
		if (normal == null) return null;
		//System.out.println("Putting string: "+normal);

		byte[] bytes = normal.getBytes(StandardCharsets.UTF_8);
		//System.out.println("Bytes length: "+bytes.length);
		//char[] chars = new char[bytes.length * 4];

		StringBuilder sb = new StringBuilder();
		sb.ensureCapacity(bytes.length * 4);

		//boolean debug = normal != "null";

		for (int i = 0; i < bytes.length; i++) {
			char[] hex = byteToHex(bytes[i]);
			/*
			if(debug) {
				System.out.println("got hex: "+ Arrays.toString(hex));
				System.out.println("got hex2: "+hex[0]);
				System.out.println("got hex2: "+hex[1]);
			}
			 */
			sb.append(COLOR_CHAR);
			sb.append(hex[0]);
			sb.append(COLOR_CHAR);
			sb.append(hex[1]);
			/*
			chars[i * 4] = COLOR_CHAR;
			chars[i * 4 + 1] = hex[0];
			chars[i * 4 + 2] = COLOR_CHAR;
			chars[i * 4 + 3] = hex[1];
			 */
		}

		/*
		System.out.println("Generated chars: "+sb.length());
		System.out.println("Generated chars2: "+sb.toString());
		System.out.println("Generated chars3: "+sb.toString().length());
		 */

		return sb.toString();
	}

	private static String colorsToString(String colors) {
		if (colors == null) return null;

		colors = colors.toLowerCase().replace("" + COLOR_CHAR, "");

		if (colors.length() % 2 != 0) {
			colors = colors.substring(0, (colors.length() / 2) * 2);
		}

		char[] chars = colors.toCharArray();
		byte[] bytes = new byte[chars.length / 2];

		for (int i = 0; i < chars.length; i += 2) {
			bytes[i / 2] = hexToByte(chars[i], chars[i + 1]);
		}

		return new String(bytes, StandardCharsets.UTF_8);
	}

	private static int hexToUnsignedInt(char c) {
		//if (c >= '0'+CHAR_OFFSET && c <= '9'+CHAR_OFFSET) {
			return c - (48 + CHAR_OFFSET);
		//} else {
		//	throw new IllegalArgumentException("Invalid hex char: out of range");
		//}
	}

	private static char unsignedIntToHex(int i) {
		if (i >= 0 && i <= 15) {
			return (char) (i + 48 + CHAR_OFFSET);
		} else {
			throw new IllegalArgumentException("Invalid hex int: out of range");
		}
		/*
		if (i >= 0 && i <= 9) {
			return (char) (i + 48);
		} else if (i >= 10 && i <= 15) {
			return (char) (i + 87);
		} else {
			throw new IllegalArgumentException("Invalid hex int: out of range");
		}
		 */
	}

	private static byte hexToByte(char hex1, char hex0) {
		return (byte) (((hexToUnsignedInt(hex1) << 4) | hexToUnsignedInt(hex0)) + Byte.MIN_VALUE);
	}

	private static char[] byteToHex(byte b) {
		int unsignedByte = (int) b - Byte.MIN_VALUE;
		return new char[]{unsignedIntToHex((unsignedByte >> 4) & 0xf), unsignedIntToHex(unsignedByte & 0xf)};
	}

}