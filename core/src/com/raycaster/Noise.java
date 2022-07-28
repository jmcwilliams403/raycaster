package com.raycaster;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import static com.raycaster.Raycaster.TAUf;

public class Noise {
	protected static final long default_seed = 0x9E3779B97F4A7C15L;
	protected static final int default_bits = Byte.SIZE;
	protected static final int max_bits = Short.SIZE;
	
	private static int length;
	private static int mask;
	private static char[] permutation;
	private static char[] p;
	
	static {
		generatePermutationTable(default_bits, default_seed);
	}
	
	protected static void generatePermutationTable(int bits, Long seed) {
		final int shift = Math.min(bits, max_bits);
		length = shift > 0 ? 1 << shift : 1;
		mask = length - 1;
		
		permutation = new char[length];
		for (char i = 0; i < length; i++)
			permutation[i] = i;
		
		java.util.Random rand = (seed == null)? new java.util.Random() : new java.util.Random(seed);
        for (int i = length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            char temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
		
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
				float gamma = Math.max(Math.min(noise[x][y] + gain, 1f),0f);
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
				result[x][y] = ((noise[x][y] - minValue) / (maxValue - minValue));
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
		float dx = x * TAUf, dy = y * TAUf;
		return noise((float)Math.sin(dx),(float)Math.cos(dx),(float)Math.sin(dy),(float)Math.cos(dy));
	}
	
	private static float noise(float x, float y, float z, float w) {
	    int x0 = floor( x ); // Integer part of x
	    int y0 = floor( y ); // Integer part of y
	    int z0 = floor( z ); // Integer part of y
	    int w0 = floor( w ); // Integer part of w
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

	    return lerp( s,
    		lerp( t,
	    		lerp ( u,
		    		lerp( v,
		    		    grad(p[x0 + p[y0 + p[z0 + p[w0]]]], x0f, y0f, z0f, w0f),
		    		    grad(p[x0 + p[y0 + p[z0 + p[w1]]]], x0f, y0f, z0f, w1f)
		    		    ),
		    		lerp( v,
		    		    grad(p[x0 + p[y0 + p[z1 + p[w0]]]], x0f, y0f, z1f, w0f),
		    		    grad(p[x0 + p[y0 + p[z1 + p[w1]]]], x0f, y0f, z1f, w1f)
		    		)
			    ),
	    		lerp ( u,
			    	lerp( v,
			    		grad(p[x0 + p[y1 + p[z0 + p[w0]]]], x0f, y1f, z0f, w0f),
			    		grad(p[x0 + p[y1 + p[z0 + p[w1]]]], x0f, y1f, z0f, w1f)
			    	),
			    	lerp( v,
			    	    grad(p[x0 + p[y1 + p[z1 + p[w0]]]], x0f, y1f, z1f, w0f),
			    	    grad(p[x0 + p[y1 + p[z1 + p[w1]]]], x0f, y1f, z1f, w1f)
			    	)
			    )
		    ),
    		lerp( t,
	    		lerp ( u,
		    		lerp( v,
				    	grad(p[x1 + p[y0 + p[z0 + p[w0]]]], x1f, y0f, z0f, w0f),
				    	grad(p[x1 + p[y0 + p[z0 + p[w1]]]], x1f, y0f, z0f, w1f)
				    ),
		    		lerp( v,
				    	grad(p[x1 + p[y0 + p[z1 + p[w0]]]], x1f, y0f, z1f, w0f),
				    	grad(p[x1 + p[y0 + p[z1 + p[w1]]]], x1f, y0f, z1f, w1f)
				    )
			    ),
	    		lerp ( u,
		    		lerp( v,
				    	grad(p[x1 + p[y1 + p[z0 + p[w0]]]], x1f, y1f, z0f, w0f),
				    	grad(p[x1 + p[y1 + p[z0 + p[w1]]]], x1f, y1f, z0f, w1f)
				    ),
		    		lerp( v,
				    	grad(p[x1 + p[y1 + p[z1 + p[w0]]]], x1f, y1f, z1f, w0f),
				    	grad(p[x1 + p[y1 + p[z1 + p[w1]]]], x1f, y1f, z1f, w1f)
				    )
			    )
		    )
	    );
	}
	
	private static int floor(float f) {
		int i = (int)f;
		return f < i ? i - 1 : i;
	}
	
	private static float fade(float f) {
		return f * f * f * (f * (f * 6 - 15) + 10);
	}

	private static float lerp(float c, float a, float b) {
		return a + c * (b - a);
	}

	private static float grad(int hash, float x, float y, float z, float w) {
		// CONVERT LO 4 BITS OF HASH CODE INTO 12 GRADIENT DIRECTIONS
		int h = hash & 0b11111;
		float t = h < 0b11000 ? x : y;
		float u = h < 0b10000 ? y : z;
		float v = h < 0b01000 ? z : w;
		return ((h & 0b1) == 0 ? t : -t) + ((h & 0b10) == 0 ? u : -u) + ((h & 0b100) == 0 ? v : -v);
	}
}
