package com.raycaster;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import static com.raycaster.Raycaster.TAU;

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
	
	public static Pixmap fractalNoise(int width, int height, int depth, float clip) {
		return fractalNoise(width, height, depth, Color.WHITE, clip);
	}
	
	public static Pixmap fractalNoise(int width, int height, int depth) {
		return fractalNoise(width, height, depth, Color.WHITE, 1f);
	}
	public static Pixmap fractalNoise(int width, int height, int depth, Color tint) {
		return fractalNoise(width, height, depth, tint, 1f);
	}
	public static Pixmap fractalNoise(int width, int height, int depth, Color tint, float clip) {
		float[][] result = new float[width][height];
		
		final int exponent = depth > 0 ? 1 << depth : 1;
		
		for (int i = 1; i <= exponent; i *= 2) {
			result = blend(result, noise(width, height, i), i);
		}
		return getPixmap(normalize(result), tint, clip);
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
	
	protected static Pixmap getPixmap(float[][] noise, Color tint, float clip) {
		final int width = noise.length, height = noise[0].length;
		
		Pixmap result = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		result.setFilter(Pixmap.Filter.NearestNeighbour);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				float gamma = noise[x][y];
				if (gamma <= clip)
				{
					Color color = new Color(tint);
					color.mul(gamma, gamma, gamma, clip);
					result.drawPixel(x, y, Color.rgba8888(color));
				}
			}
		}
		
		return result;
	}
	
	public static Pixmap perlinNoise(int width, int height, int exponent) {
		return perlinNoise(width, height, exponent, Color.WHITE, 1f);
	}
	
	public static Pixmap perlinNoise(int width, int height, int exponent, float clip) {
		return perlinNoise(width, height, exponent, Color.WHITE, clip);
	}
	
	public static Pixmap perlinNoise(int width, int height, int exponent, Color tint) {
		return perlinNoise(width, height, exponent, tint, 1f);
	}
	
	public static Pixmap perlinNoise(int width, int height, int exponent, Color tint, float clip) {		
		return getPixmap(noise(width, height, exponent), tint, clip);
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
		double dx = x * TAU, dy = y * TAU;
		return noise((float)Math.sin(dx),(float)Math.cos(dx),(float)Math.sin(dy),(float)Math.cos(dy));
	}
	
	private static float noise(float x, float y, float z, float w) {
		int ix0, iy0, iz0, iw0, ix1, iy1, iz1, iw1;
	    float fx0, fy0, fz0, fw0, fx1, fy1, fz1, fw1;
	    float s, t, r, q;
	    float nxyz0, nxyz1, nxy0, nxy1, nx0, nx1, n0, n1;

	    ix0 = floor( x ); // Integer part of x
	    iy0 = floor( y ); // Integer part of y
	    iz0 = floor( z ); // Integer part of y
	    iw0 = floor( w ); // Integer part of w
	    ix1 = ix0 + 1;
	    iy1 = iy0 + 1;
	    iz1 = iz0 + 1;
	    iw1 = iw0 + 1;
	    
	    fx0 = x - ix0;        // Fractional part of x
	    fy0 = y - iy0;        // Fractional part of y
	    fz0 = z - iz0;        // Fractional part of z
	    fw0 = w - iw0;        // Fractional part of w
	    fx1 = fx0 - 1f;
	    fy1 = fy0 - 1f;
	    fz1 = fz0 - 1f;
	    fw1 = fw0 - 1f;
	    
	    ix0 = ix0 & mask; // Wrap to 0..255
	    iy0 = iy0 & mask;
	    iz0 = iz0 & mask;
	    iw0 = iw0 & mask;
	    ix1 = ix1 & mask;
	    iy1 = iy1 & mask;
	    iz1 = iz1 & mask;
	    iw1 = iw1 & mask;
	    

	    q = fade( fw0 );
	    r = fade( fz0 );
	    t = fade( fy0 );
	    s = fade( fx0 );

	    nxyz0 = grad(p[ix0 + p[iy0 + p[iz0 + p[iw0]]]], fx0, fy0, fz0, fw0);
	    nxyz1 = grad(p[ix0 + p[iy0 + p[iz0 + p[iw1]]]], fx0, fy0, fz0, fw1);
	    nxy0 = lerp( q, nxyz0, nxyz1 );
	        
	    nxyz0 = grad(p[ix0 + p[iy0 + p[iz1 + p[iw0]]]], fx0, fy0, fz1, fw0);
	    nxyz1 = grad(p[ix0 + p[iy0 + p[iz1 + p[iw1]]]], fx0, fy0, fz1, fw1);
	    nxy1 = lerp( q, nxyz0, nxyz1 );
	        
	    nx0 = lerp ( r, nxy0, nxy1 );

	    nxyz0 = grad(p[ix0 + p[iy1 + p[iz0 + p[iw0]]]], fx0, fy1, fz0, fw0);
	    nxyz1 = grad(p[ix0 + p[iy1 + p[iz0 + p[iw1]]]], fx0, fy1, fz0, fw1);
	    nxy0 = lerp( q, nxyz0, nxyz1 );
	        
	    nxyz0 = grad(p[ix0 + p[iy1 + p[iz1 + p[iw0]]]], fx0, fy1, fz1, fw0);
	    nxyz1 = grad(p[ix0 + p[iy1 + p[iz1 + p[iw1]]]], fx0, fy1, fz1, fw1);
	    nxy1 = lerp( q, nxyz0, nxyz1 );

	    nx1 = lerp ( r, nxy0, nxy1 );

	    n0 = lerp( t, nx0, nx1 );

	    nxyz0 = grad(p[ix1 + p[iy0 + p[iz0 + p[iw0]]]], fx1, fy0, fz0, fw0);
	    nxyz1 = grad(p[ix1 + p[iy0 + p[iz0 + p[iw1]]]], fx1, fy0, fz0, fw1);
	    nxy0 = lerp( q, nxyz0, nxyz1 );
	        
	    nxyz0 = grad(p[ix1 + p[iy0 + p[iz1 + p[iw0]]]], fx1, fy0, fz1, fw0);
	    nxyz1 = grad(p[ix1 + p[iy0 + p[iz1 + p[iw1]]]], fx1, fy0, fz1, fw1);
	    nxy1 = lerp( q, nxyz0, nxyz1 );

	    nx0 = lerp ( r, nxy0, nxy1 );

	    nxyz0 = grad(p[ix1 + p[iy1 + p[iz0 + p[iw0]]]], fx1, fy1, fz0, fw0);
	    nxyz1 = grad(p[ix1 + p[iy1 + p[iz0 + p[iw1]]]], fx1, fy1, fz0, fw1);
	    nxy0 = lerp( q, nxyz0, nxyz1 );
	        
	    nxyz0 = grad(p[ix1 + p[iy1 + p[iz1 + p[iw0]]]], fx1, fy1, fz1, fw0);
	    nxyz1 = grad(p[ix1 + p[iy1 + p[iz1 + p[iw1]]]], fx1, fy1, fz1, fw1);
	    nxy1 = lerp( q, nxyz0, nxyz1 );

	    nx1 = lerp ( r, nxy0, nxy1 );

	    n1 = lerp( t, nx0, nx1 );

	    return lerp( s, n0, n1 );
	}
	
	private static int floor(float x) {
		int ix = (int)x;
		return x < ix ? ix - 1 : ix;
	}
	
	private static float fade(float t) {
		return t * t * t * (t * (t * 6 - 15) + 10);
	}

	private static float lerp(float t, float a, float b) {
		return a + t * (b - a);
	}

	private static float grad(int hash, float x, float y, float z, float t) {
		// CONVERT LO 4 BITS OF HASH CODE INTO 12 GRADIENT DIRECTIONS
		int h = hash & 0b11111;
		float u = h < 0b11000 ? x : y;
		float v = h < 0b10000 ? y : z;
		float w = h < 0b01000 ? z : t;
		return ((h & 0b1) == 0 ? u : -u) + ((h & 0b10) == 0 ? v : -v) + ((h & 0b100) == 0 ? w : -w);
	}
}
