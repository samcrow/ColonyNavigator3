package org.samcrow.colonynavigator3.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.applantation.android.svg.SVG;
import com.applantation.android.svg.SVGParseException;
import com.applantation.android.svg.SVGParser;

/**
 * Provides static access to various cached Drawables defined in SVG files
 * 
 * @author samcrow
 *
 */
public class SVGDrawables {

	private SVGDrawables() {}
	
	private static SparseArray<Drawable> cache;
	
	public static Drawable get(Context context, int resId) {
		initCache();
		
		Drawable drawable = cache.get(resId);
		if(drawable != null) {
			// Found in the caceh
			return drawable;
		}
		else {
			try {
				// Create a Drawable from the SVG
				SVG svg = SVGParser.getSVGFromResource(context.getResources(), resId);
				
				drawable = svg.createPictureDrawable();
				cache.append(resId, drawable);
				return drawable;
				
			} catch (SVGParseException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Clears the drawable with the given ID from the cache
	 * @param resId
	 */
	public static void removeFromCache(int resId) {
		cache.delete(resId);
	}
	
	/**
	 * Clears all drawables from the cache
	 */
	public static void clearCache() {
		cache.clear();
	}
	
	private static void initCache() {
		if(cache == null) {
			cache = new SparseArray<Drawable>();
		}
	}
	
}
