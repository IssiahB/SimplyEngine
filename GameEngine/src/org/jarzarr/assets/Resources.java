package org.jarzarr.assets;

import java.io.InputStream;
import java.util.Objects;

/**
 * Helper for loading classpath resources (images, audio, fonts, etc.).
 *
 * <p>
 * This centralizes path normalization and provides a buffered
 * {@link InputStream} for reading resource data from the jar/classpath.
 * </p>
 *
 * <p>
 * Paths are normalized to be classloader-friendly (no leading "/").
 * </p>
 */
public final class Resources {

	/** Utility class; not instantiable. */
	private Resources() {
	}

	/**
	 * Opens a buffered input stream for the given classpath resource.
	 *
	 * @param path resource path (ex: "assets/player.png" or "/assets/player.png")
	 * @return buffered input stream for the resource
	 * @throws IllegalArgumentException if the resource does not exist
	 */
	public static InputStream stream(String path) {
		path = normalize(path);
		InputStream in = Resources.class.getClassLoader().getResourceAsStream(path);
		if (in == null)
			throw new IllegalArgumentException("Resource not found: " + path);
		return new java.io.BufferedInputStream(in);
	}

	/**
	 * Normalizes a resource path:
	 * <ul>
	 * <li>non-null</li>
	 * <li>trim whitespace</li>
	 * <li>strip leading "/" so ClassLoader lookups work</li>
	 * </ul>
	 *
	 * @param path input path
	 * @return normalized classpath resource path
	 * @throws NullPointerException if path is null
	 */
	public static String normalize(String path) {
		path = Objects.requireNonNull(path).trim();
		if (path.startsWith("/"))
			path = path.substring(1);
		return path;
	}
}
