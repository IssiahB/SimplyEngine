package org.jarzarr.assets;

import java.io.InputStream;
import java.util.Objects;

public final class Resources {
	private Resources() {
	}

	public static InputStream stream(String path) {
		path = normalize(path);
		InputStream in = Resources.class.getClassLoader().getResourceAsStream(path);
		if (in == null)
			throw new IllegalArgumentException("Resource not found: " + path);
		return new java.io.BufferedInputStream(in);
	}

	public static String normalize(String path) {
		path = Objects.requireNonNull(path).trim();
		if (path.startsWith("/"))
			path = path.substring(1);
		return path;
	}
}
