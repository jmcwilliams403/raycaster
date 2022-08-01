package com.raycaster;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.digital.ArrayTools;

public class Noise {
	protected static final long default_seed = MathTools.GOLDEN_LONGS[0];
	protected static final int default_bits = Byte.SIZE;
	protected static final int max_bits = Short.SIZE;

	private static char mask;
	private static char[] p;

	static {
		generatePermutationTable(default_bits, default_seed);
	}

	protected static void generatePermutationTable(int bits, Long seed) {
		final int shift = Math.min(bits, max_bits);
		final int length = shift > 0 ? 1 << shift : 1;
		mask = (char)(length - 1);

		final char[] permutation = ArrayTools.shuffle(
			ArrayTools.charSpan('\0', mask),
			seed == null ? null : new java.util.Random(seed)
		);

		p = new char[length*2];
		for (int i = 0; i < length; i++)
			p[i] = p[i + length] = permutation[i];
	}

	public static Pixmap fractalNoise(int width, int height, int depth, float gain, float clip) {
		return fractalNoise(width, height, depth, gain, clip, Color.WHITE);
	}

	public static Pixmap fractalNoise(int width, int height, int depth) {
		return fractalNoise(width, height, depth, 0f, 1f, Color.WHITE);
	}
	public static Pixmap fractalNoise(int width, int height, int depth, Color tint) {
		return fractalNoise(width, height, depth, 0f, 1f, tint);
	}
	public static Pixmap fractalNoise(int width, int height, int depth, float gain, float clip, Color tint) {
		float[][] result = new float[width][height];

		final int exponent = depth > 0 ? 1 << depth : 1;

		for (int i = 1; i <= exponent; i *= 2) {
			result = blend(result, noise(width, height, i), i);
		}
		return getPixmap(normalize(result), gain, clip, tint);
	}

	protected static float[][] blend(float[][] noise1, float[][] noise2, float persistence)
	{
		final int width = Math.min(noise1.length, noise2.length);
		final int height = Math.min(noise1[0].length, noise2[0].length);

		float[][] result = new float[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				result[x][y] = noise1[x][y] + (noise2[x][y] / persistence);
			}
		}

		return result;
	}

	protected static Pixmap getPixmap(float[][] noise, float gain, float clip, Color tint) {
		final int width = noise.length, height = noise[0].length;

		Pixmap result = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		result.setFilter(Pixmap.Filter.NearestNeighbour);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				float gamma = MathTools.clamp(noise[x][y] + gain, 0f, 1f);
				if (gamma <= clip)
				{
					Color color = new Color(tint);
					color.mul(gamma, gamma, gamma, 1f);
					result.drawPixel(x, y, Color.rgba8888(color));
				}
			}
		}

		return result;
	}

	public static Pixmap perlinNoise(int width, int height, int exponent) {
		return perlinNoise(width, height, exponent, 0f, 1f, Color.WHITE);
	}

	public static Pixmap perlinNoise(int width, int height, int exponent, float gain, float clip) {
		return perlinNoise(width, height, exponent, gain, clip, Color.WHITE);
	}

	public static Pixmap perlinNoise(int width, int height, int exponent, Color tint) {
		return perlinNoise(width, height, exponent, 0f, 1f, tint);
	}

	public static Pixmap perlinNoise(int width, int height, int exponent, float gain, float clip, Color tint) {		
		return getPixmap(noise(width, height, exponent), gain, clip, tint);
	}

	private static float[][] normalize(float[][] noise){
		int width = noise.length, height = noise[0].length;
		float minValue = Float.POSITIVE_INFINITY;
		float maxValue = Float.NEGATIVE_INFINITY;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (noise[x][y] < minValue)
					minValue = noise[x][y];
				else if (noise[x][y] > maxValue)
					maxValue = noise[x][y];
			}
		}

		float[][] result = new float[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				result[x][y] = MathTools.norm(minValue, maxValue, noise[x][y]);
			}
		}

		return result;
	}

	private static float[][] noise(int width, int height, int exponent){
		float[][] result = new float[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// FIND RELATIVE X,Y OF POINT
				float dx = (float) x * (float) exponent / (float) width;
				float dy = (float) y * (float) exponent / (float) height;

				result[x][y] = noise(dx, dy);
			}
		}
		return normalize(result);
	}

	private static float noise(float x, float y) {
		float dx = x * TrigTools.TAU, dy = y * TrigTools.TAU;
		return noise(TrigTools.sin(dx),TrigTools.cos(dx),TrigTools.sin(dy),TrigTools.cos(dy));
	}

	private static float noise(float x, float y, float z, float w) {
		int x0 = MathTools.floor( x ); // Integer part of x
		int y0 = MathTools.floor( y ); // Integer part of y
		int z0 = MathTools.floor( z ); // Integer part of y
		int w0 = MathTools.floor( w ); // Integer part of w
		int x1 = x0 + 1;
		int y1 = y0 + 1;
		int z1 = z0 + 1;
		int w1 = w0 + 1;

		float x0f = x - x0;        // Fractional part of x
		float y0f = y - y0;        // Fractional part of y
		float z0f = z - z0;        // Fractional part of z
		float w0f = w - w0;        // Fractional part of w
		float x1f = x0f - 1f;
		float y1f = y0f - 1f;
		float z1f = z0f - 1f;
		float w1f = w0f - 1f;

		x0 &= mask; // Wrap to 0..255
		y0 &= mask;
		z0 &= mask;
		w0 &= mask;
		x1 &= mask;
		y1 &= mask;
		z1 &= mask;
		w1 &= mask;

		float s = fade( x0f );
		float t = fade( y0f );
		float u = fade( z0f );
		float v = fade( w0f );

		return MathTools.lerp(
			MathTools.lerp(
				MathTools.lerp (
					MathTools.lerp(
						grad(p[x0 + p[y0 + p[z0 + p[w0]]]], x0f, y0f, z0f, w0f),
						grad(p[x0 + p[y0 + p[z0 + p[w1]]]], x0f, y0f, z0f, w1f),
						v
					),
					MathTools.lerp(
						grad(p[x0 + p[y0 + p[z1 + p[w0]]]], x0f, y0f, z1f, w0f),
						grad(p[x0 + p[y0 + p[z1 + p[w1]]]], x0f, y0f, z1f, w1f),
						v
					),
					u
				),
				MathTools.lerp(
					MathTools.lerp(
						grad(p[x0 + p[y1 + p[z0 + p[w0]]]], x0f, y1f, z0f, w0f),
						grad(p[x0 + p[y1 + p[z0 + p[w1]]]], x0f, y1f, z0f, w1f),
						v
					),
					MathTools.lerp(
						grad(p[x0 + p[y1 + p[z1 + p[w0]]]], x0f, y1f, z1f, w0f),
						grad(p[x0 + p[y1 + p[z1 + p[w1]]]], x0f, y1f, z1f, w1f),
						v
					),
					u
				),
				t
			),
			MathTools.lerp(
				MathTools.lerp (
					MathTools.lerp(
						grad(p[x1 + p[y0 + p[z0 + p[w0]]]], x1f, y0f, z0f, w0f),
						grad(p[x1 + p[y0 + p[z0 + p[w1]]]], x1f, y0f, z0f, w1f),
						v
					),
					MathTools.lerp(
						grad(p[x1 + p[y0 + p[z1 + p[w0]]]], x1f, y0f, z1f, w0f),
						grad(p[x1 + p[y0 + p[z1 + p[w1]]]], x1f, y0f, z1f, w1f),
						v
					),
					u
				),
				MathTools.lerp(
					MathTools.lerp(
						grad(p[x1 + p[y1 + p[z0 + p[w0]]]], x1f, y1f, z0f, w0f),
						grad(p[x1 + p[y1 + p[z0 + p[w1]]]], x1f, y1f, z0f, w1f),
						v
					),
					MathTools.lerp(
						grad(p[x1 + p[y1 + p[z1 + p[w0]]]], x1f, y1f, z1f, w0f),
						grad(p[x1 + p[y1 + p[z1 + p[w1]]]], x1f, y1f, z1f, w1f),
						v
					),
					u
				),
				t
			),
			s
		);
	}

	private static float fade(float f) {
		return f * f * f * (f * (f * 6 - 15) + 10);
	}

	private static float grad(int hash, float x, float y, float z, float w) {
		// CONVERT LO 4 BITS OF HASH CODE INTO 12 GRADIENT DIRECTIONS
		int h = hash & 0b1_1111;
		float t = h < 0b1_1000 ? x : y;
		float u = h < 0b1_0000 ? y : z;
		float v = h < 0b0_1000 ? z : w;
		return ((h & 0b0_0001) == 0 ? t : -t) + ((h & 0b0_0010) == 0 ? u : -u) + ((h & 0b0_0100) == 0 ? v : -v);
	}
}
