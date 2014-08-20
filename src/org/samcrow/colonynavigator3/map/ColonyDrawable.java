package org.samcrow.colonynavigator3.map;

import org.samcrow.colonynavigator3.data.Colony;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class ColonyDrawable extends Drawable {

	/**
	 * Background shape alpha (transparency), 0-255
	 */
	private static final int BG_ALPHA = 100;
	/**
	 * Background color for non-focus, non-visited colonies
	 */
	private static final int BG_NORMAL_COLOR = Color.argb(BG_ALPHA / 2, 100, 100, 100); // gray
	/**
	 * Background color for focus colonies
	 */
	private static final int BG_FOCUS_COLOR = Color.argb(BG_ALPHA, 115, 140, 255); // blue
	/**
	 * Background color for visited colonies, both focus and non-focus
	 */
	private static final int BG_VISITED_COLOR = Color.argb(BG_ALPHA, 77, 240, 101); // green
	
	/**
	 * Background circle radius
	 */
	private static final int BG_RADIUS = 20;
	
	/**
	 * Colony location point color
	 */
	private static final int POINT_COLOR = Color.BLACK;
	/**
	 * Colony number label color
	 */
	private static final int LABEL_COLOR = Color.BLACK;
	/**
	 * Colony location circle radius
	 */
	private static final int POINT_RADIUS = 3;
	/**
	 * Line color for the circle drawn around the selected colony
	 */
	private static final int SELECTED_CIRCLE_COLOR = Color.GREEN;
	/**
	 * Line width for the circle drawn around the selected colony
	 */
	private static final int SELECTED_CIRCLE_LINE_WIDTH = 5;
	
	private static final float SELECTED_CIRCLE_RADIUS = BG_RADIUS - (SELECTED_CIRCLE_LINE_WIDTH / 2f);
	
	/**
	 * The horizontal distance from the center that the colony number text is offset
	 */
	private static final int TEXT_X_OFFSET = 5;
	
	private final Paint paint = new Paint();
	
	private FontMetrics metrics = new FontMetrics();
	
	private final Colony colony;
	
	private final String colonyIdString;
	private final int idStringWidth;
	
	public ColonyDrawable(Colony colony) {
		this.colony = colony;
		colonyIdString = String.valueOf(colony.getId());
		idStringWidth = (int) Math.ceil(paint.measureText(colonyIdString));
		
		paint.setAntiAlias(true);
		metrics = paint.getFontMetrics();
		
		//Create a bounding box
		int left = -BG_RADIUS;
		int right = BG_RADIUS + idStringWidth;
		int top = -BG_RADIUS;
		int bottom = BG_RADIUS;
		setBounds(left, top, right, bottom);
		
	}
	
	/**
	 * 
	 * @return The distance that this drawable
	 * should be moved right along the X axis
	 * to make the center of its point match up
	 * with the center of the dimensions of this drawable
	 */
	public int getXOffset() {
		if(idStringWidth + TEXT_X_OFFSET <= BG_RADIUS) {
			return 0;
		}
		else {
			return (idStringWidth + TEXT_X_OFFSET) - BG_RADIUS;
		}
	}
	

	@Override
	public int getIntrinsicHeight() {
		return 2 * BG_RADIUS;
	}

	@Override
	public int getIntrinsicWidth() {
		return BG_RADIUS + idStringWidth + TEXT_X_OFFSET;
	}

	@Override
	public void draw(Canvas canvas) {
		
		//Paint within bounds
		//Calculate the X/Y canvas coordinate position of the actual colony position
		Rect bounds = getBounds();
		float centerX = bounds.left + BG_RADIUS;
		float centerY = bounds.top + BG_RADIUS;
		
		//Big background circle
		int backgroundColor;
		if(colony.isVisited()) {
			backgroundColor = BG_VISITED_COLOR;
		}
		else {
			if(colony.isFocusColony()) {
				backgroundColor = BG_FOCUS_COLOR;
			}
			else {
				backgroundColor = BG_NORMAL_COLOR;
			}
		}
		//Set paint to fill only, with the required color
		paint.setColor(backgroundColor);
		paint.setStyle(Style.FILL);
		
		//Draw the background at the center position
		canvas.drawCircle(centerX, centerY, BG_RADIUS, paint);
		
		// Draw the circle around the colony if it is selected
		if(colony.isSelected()) {
			paint.setStyle(Style.STROKE);
			paint.setColor(SELECTED_CIRCLE_COLOR);
			paint.setStrokeWidth(SELECTED_CIRCLE_LINE_WIDTH);
			
			canvas.drawCircle(centerX, centerY, SELECTED_CIRCLE_RADIUS, paint);
			
		}
		
		//Draw colony location point
		paint.setStyle(Style.FILL);
		paint.setColor(POINT_COLOR);
		canvas.drawCircle(centerX, centerY, POINT_RADIUS, paint);
		
		//Draw colony number
		paint.setColor(LABEL_COLOR);
		canvas.drawText(String.valueOf(colony.getId()), centerX + TEXT_X_OFFSET, centerY + metrics.descent, paint);
	}
	
	

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		//ignore

	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		//ignoring

	}

}
