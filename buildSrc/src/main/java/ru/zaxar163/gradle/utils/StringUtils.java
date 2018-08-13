package ru.zaxar163.gradle.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

public final class StringUtils {
	public static String fromUTF8Stream(InputStream stream) throws IOException {
		return new String(ByteStreams.toByteArray(stream), Charsets.UTF_8);
	}

	public static ImmutableList<String> lines(final String text) {
		try {
			return ImmutableList.copyOf(CharStreams.readLines(new StringReader(text)));
		} catch (IOException e) {
			return ImmutableList.of();
		}
	}

	public static String lower(String string) {
		return string.toLowerCase(Locale.ENGLISH);
	}

	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return StringUtils.tokenizeToStringArray(str, delimiters, true, true);
	}


	public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
			boolean ignoreEmptyTokens) {

		if (str == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || (token.length() > 0)) {
				tokens.add(token);
			}
		}
		return StringUtils.toStringArray(tokens);
	}

	public static String[] toStringArray(Collection<String> collection) {
		if (collection == null) {
			return null;
		}
		return collection.toArray(new String[collection.size()]);
	}

	private StringUtils() {
	}
}
