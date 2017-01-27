package it.beppi.tristatetogglebutton_library;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

import static it.beppi.tristatetogglebutton_library.TriStateToggleButton.ToggleStatus.mid;
import static it.beppi.tristatetogglebutton_library.TriStateToggleButton.ToggleStatus.off;
import static it.beppi.tristatetogglebutton_library.TriStateToggleButton.ToggleStatus.on;


/**
 * @author ThinkPad
 * @author modified by BeppiMenozzi
 */
public class TriStateToggleButton extends View{
	// Beppi: added a three state enumerator
	public enum ToggleStatus {
		on, mid, off
	}
	// Beppi: convert from the string values defined in the xml attributes to an usable value
	private ToggleStatus attrToStatus(String attr) {
		if (attr == null) return off;
		if (attr.equals("0")) return off;
		else if (attr.equals("1")) return mid;
		else return on;
	}

	// Beppi: static shortcuts for handling boolean values with 2-state. mid = false.
	public static boolean toggleStatusToBoolean(ToggleStatus toggleStatus) {
		if (toggleStatus == on) return true;
		else return false;
	}
	public static ToggleStatus booleanToToggleStatus(boolean toggleStatus) {
		if (toggleStatus) return on;
		else return off;
	}
	// Beppi: same with integers
	public static int toggleStatusToInt(ToggleStatus toggleStatus) {
		switch (toggleStatus) {
			case off: return 0;
			case mid: return 1;
			case on:
			default: return 2;
		}
	}
	public static ToggleStatus intToToggleStatus(int toggleIntValue) {
		if (toggleIntValue == 0) return off;
		else if (toggleIntValue == 1) return mid;
		else return on;
	}


	private SpringSystem springSystem;
	private Spring spring ;
	/** */
	private float radius;
	/** 开启颜色*/  // Turn on color
	// Beppi: Modified color to match material design
	// private int onColor = Color.parseColor("#4ebb7f");
	private int onColor = Color.parseColor("#42bd41");  // green 300
	/** 关闭颜色*/  // Turn off color
	// Beppi: Modified color to match material design
	// private int offBorderColor = Color.parseColor("#dadbda");
	private int offBorderColor = Color.parseColor("#bdbdbd");   // grey 400
	/** 灰色带颜色*/  // Gray color
	private int offColor = Color.parseColor("#ffffff");
	/** 手柄颜色*/   // Handle color
	// Beppi: Added third mid color
	private int midColor = Color.parseColor("#ffca28");  // amber 400
	private int spotColor = Color.parseColor("#ffffff");
	/** 边框颜色*/	// Border color
	private int borderColor = offBorderColor;
	/** 画笔*/		// brush
	private Paint paint ;
	/** 开关状态*/	// switch status
	// Beppi: changed the type of toggleOn from boolean to ToggleStatus
	// Beppi: refactored the variable name from toggleOn to toggleStatus
//	private boolean toggleOn = false;
	private ToggleStatus toggleStatus = off;
	// Beppi: added previousToggleStatus to manage transitions correctly
	private ToggleStatus previousToggleStatus = off;
	/** 边框大小*/	// Border size
	private int borderWidth = 2;
	/** 垂直中心*/	// Vertical center
	private float centerY;
	/** 按钮的开始和结束位置*/  // The start and end positions of the button
	// Beppi: added midX position
//	private float startX, endX;
	private float startX, midX, endX;
	/** 手柄X位置的最小和最大值*/  // The minimum and maximum values for the X position of the handle
	// Beppi: added spotMidX
//	private float spotMinX, spotMaxX;
	private float spotMinX, spotMidX, spotMaxX;
	/**手柄大小 */			// Handle size
	private int spotSize ;
	/** 手柄X位置*/			// Handle X position
	private float spotX;
	/** 关闭时内部灰色带高度*/  // Off Internal gray band height
	private float offLineWidth;
	/** */
	private RectF rect = new RectF();
	/** 默认使用动画*/		// Animation is used by default
	private boolean defaultAnimate = true;
	// Beppi: added midSelectable
	private boolean midSelectable = true;

	// Beppi: swipe management
	private int swipeSensitivityPixels = 200;
	private int swipeX = 0;

	/** 是否默认处于打开状态*/	// Whether it is on by default
	// Beppi: changed the type of isDefaultOn from boolean to ToggleStatus
	// Beppi: refactored the variable name from isDefaultOn to defaultStatus
//	private boolean isDefaultOn = false;
	private ToggleStatus defaultStatus = off;
	// Beppi: enabled && disabledColor
	private boolean enabled = true;
	private int disabledColor = Color.parseColor("#bdbdbd");   // grey 400

	private boolean swipeing = false;

	private OnToggleChanged listener;
	
	private TriStateToggleButton(Context context) {
		super(context);
	}
	public TriStateToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setup(attrs);
	}
	public TriStateToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(attrs);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		spring.removeListener(springListener);
	}
	
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		spring.addListener(springListener);
	}

	public void setup(AttributeSet attrs) {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Style.FILL);
		paint.setStrokeCap(Cap.ROUND);
		
		springSystem = SpringSystem.create();
		spring = springSystem.createSpring();
		spring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(50, 7));

		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toggle(defaultAnimate);
			}
		});

		// Beppi: swipe management
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				int x = (int) motionEvent.getX();
				int action = motionEvent.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					swipeX = x;
					swipeing = false;
				}
				else if (action == MotionEvent.ACTION_MOVE) {
					if (swipeSensitivityPixels == 0) return false;
					else if (x - swipeX > swipeSensitivityPixels) {
						swipeX = x;
						swipeing = true;
						increaseValue();
						return true;
					}
					else if (swipeX - x > swipeSensitivityPixels) {
						swipeX = x;
						swipeing = true;
						decreaseValue();
						return true;
					}
				}
				else if (action == MotionEvent.ACTION_UP) {
					if (!swipeing) toggle(defaultAnimate);    // here simple clicks are managed.
					return true;
				}
				return false;
			}
		});
		
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TriStateToggleButton);
		offBorderColor = typedArray.getColor(R.styleable.TriStateToggleButton_tbOffBorderColor, offBorderColor);
		onColor = typedArray.getColor(R.styleable.TriStateToggleButton_tbOnColor, onColor);
		spotColor = typedArray.getColor(R.styleable.TriStateToggleButton_tbSpotColor, spotColor);
		offColor = typedArray.getColor(R.styleable.TriStateToggleButton_tbOffColor, offColor);
		// Beppi: added midColor attribute
		midColor = typedArray.getColor(R.styleable.TriStateToggleButton_tbMidColor, midColor);
		borderWidth = typedArray.getDimensionPixelSize(R.styleable.TriStateToggleButton_tbBorderWidth, borderWidth);
		defaultAnimate = typedArray.getBoolean(R.styleable.TriStateToggleButton_tbAnimate, defaultAnimate);
		// Beppi: modified defaultStatus from boolean to DefaultStatus
//		defaultStatus = typedArray.getBoolean(R.styleable.ToggleButton_tbDefaultStatus, defaultStatus);
		defaultStatus = attrToStatus(typedArray.getString(R.styleable.TriStateToggleButton_tbDefaultStatus));
		// Beppi: added tbIsMidSelectable
		midSelectable = typedArray.getBoolean(R.styleable.TriStateToggleButton_tbIsMidSelectable, midSelectable);
		// Beppi: added enabled
		enabled = typedArray.getBoolean(R.styleable.TriStateToggleButton_enabled, enabled);

		// Beppi: swipe
		swipeSensitivityPixels = typedArray.getInt(R.styleable.TriStateToggleButton_tbSwipeSensitivityPixels, swipeSensitivityPixels);
		// 0 == off

		typedArray.recycle();

		borderColor = offBorderColor;

		// Beppi: changed the usage of defaultStatus to match ToggleStatus type
//		if (defaultStatus) { toggleOn(); }
		switch (defaultStatus) {
			case off: toggleOff(); break;  // actually not needed, added for clearness
			case mid: toggleMid(); break;
			case on: toggleOn(); break;
		}
	}

	public void toggle() {
		toggle(true);
	}

	// Beppi: modified to iterate on the 3 values instead of switching between two
	public void toggle(boolean animate) {
//		toggleStatus = !toggleStatus;
		if (midSelectable)
			switch (toggleStatus) {
				case off: putValueInToggleStatus(mid); break;
				case mid: putValueInToggleStatus(on); break;
				case on: putValueInToggleStatus(off); break;
			}
		else
			switch (toggleStatus) {
				case off:
				case mid: putValueInToggleStatus(on); break;
				case on: putValueInToggleStatus(off); break;
			}
		takeEffect(animate);
		
		if(listener != null){
			listener.onToggle(toggleStatus, toggleStatusToBoolean(toggleStatus), toggleStatusToInt(toggleStatus));
		}
	}

	public void toggleOn() {
		setToggleOn();
		if(listener != null){
			listener.onToggle(toggleStatus, toggleStatusToBoolean(toggleStatus), toggleStatusToInt(toggleStatus));
		}
	}
	
	public void toggleOff() {
		setToggleOff();
		if(listener != null){
			listener.onToggle(toggleStatus, toggleStatusToBoolean(toggleStatus), toggleStatusToInt(toggleStatus));
		}
	}

	// Beppi: added method to handle the mid value
	public void toggleMid() {
		setToggleMid();
		if(listener != null){
			listener.onToggle(toggleStatus, toggleStatusToBoolean(toggleStatus), toggleStatusToInt(toggleStatus));
		}
	}

	private void putValueInToggleStatus(ToggleStatus value) {
		if (!enabled) return;
		previousToggleStatus = toggleStatus;
		toggleStatus = value;
	}

	/**
	 * 设置显示成打开样式，不会触发toggle事件	// Setting the display to open style does not fire the toggle event
	 */
	public void setToggleOn() {
		setToggleOn(true);
	}
	
	/**
	 * @param animate    asd
	 */
	public void setToggleOn(boolean animate){
		// Beppi: changed toggleStatus value from true to on
//		toggleStatus = true;
		putValueInToggleStatus(on);
		takeEffect(animate);
	}
	
	/**
	 * 设置显示成关闭样式，不会触发toggle事件	// Settings are shown as off styles, and the toggle event is not fired
	 */
	public void setToggleOff() {
		setToggleOff(true);
	}
	
	public void setToggleOff(boolean animate) {
		// Beppi: changed toggleStatus value from false to off
//		toggleStatus = false;
		putValueInToggleStatus(off);
		takeEffect(animate);
	}

	// Beppi: added method for Mid value management
	public void setToggleMid(boolean animate) {
		putValueInToggleStatus(mid);
		takeEffect(animate);
	}
	public void setToggleMid() {
		setToggleMid(true);
	}

	//Beppi: added setToggleStatus() method, that imho was missing and needed
	public void setToggleStatus(ToggleStatus toggleStatus, boolean animate) {
		putValueInToggleStatus(toggleStatus);
		takeEffect(animate);
	}
	public void setToggleStatus(ToggleStatus toggleStatus) {
		setToggleStatus(toggleStatus, true);
	}
	public void setToggleStatus(boolean toggleStatus) {
		setToggleStatus(toggleStatus, true);
	}
	public void setToggleStatus(boolean toggleStatus, boolean animate) {
		if (toggleStatus) 	putValueInToggleStatus(on);
		else 				putValueInToggleStatus(off);
		takeEffect(animate);
	}
	public void setToggleStatus(int toggleIntValue) {
		setToggleStatus(toggleIntValue, true);
	}
	public void setToggleStatus(int toggleIntValue, boolean animate) {
		setToggleStatus(intToToggleStatus(toggleIntValue), animate);
	}

	public void increaseValue(boolean animate) {  // same as toggle, but after on does not rewind to off
		switch (toggleStatus) {
			case off: if (midSelectable) putValueInToggleStatus(mid); else putValueInToggleStatus(on); break;
			case mid: putValueInToggleStatus(on); break;
			case on: break;
			}
		takeEffect(animate);
		if(listener != null){
			listener.onToggle(toggleStatus, toggleStatusToBoolean(toggleStatus), toggleStatusToInt(toggleStatus));
		}
	}
	public void increaseValue() {
		increaseValue(true);
	}
	public void decreaseValue(boolean animate) {
		switch (toggleStatus) {
			case on: if (midSelectable) putValueInToggleStatus(mid); else putValueInToggleStatus(off); break;
			case mid: putValueInToggleStatus(off); break;
			case off: break;
		}
		takeEffect(animate);
		if(listener != null){
			listener.onToggle(toggleStatus, toggleStatusToBoolean(toggleStatus), toggleStatusToInt(toggleStatus));
		}
	}
	public void decreaseValue() {
		decreaseValue(true);
	}

	// Beppi: rewritten takeEffect() method to manage 3 states
/*
	private void takeEffect(boolean animate) {
		if(animate){
			spring.setEndValue(toggleStatus ? 1 : 0);
		}else{
			//这里没有调用spring，所以spring里的当前值没有变更，这里要设置一下，同步两边的当前值
			// There is no call spring, so the current value of the spring has not changed, here to set it, the current value on both sides of synchronization
			spring.setCurrentValue(toggleStatus ? 1 : 0);
			calculateEffect(toggleStatus ? 1 : 0);
		}
	}
*/
	private void takeEffect(boolean animate) {
		if(animate){
			spring.setEndValue(toggleStatus == on ? 1 : toggleStatus == off ? 0 : 0.5);
		}else{
			//这里没有调用spring，所以spring里的当前值没有变更，这里要设置一下，同步两边的当前值
			// There is no call spring, so the current value of the spring has not changed, here to set it, the current value on both sides of synchronization
			spring.setCurrentValue(toggleStatus == on ? 1 : toggleStatus == off ? 0 : 0.5);
			if (toggleStatus == on) 		calculateEffect(1);
			else if (toggleStatus == mid) 	calculateEffect(0.5);
			else 							calculateEffect(0);

//			calculateEffect(toggleStatus == on ? 1 : toggleStatus == off ? 0 : 0.5);
		}
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		Resources r = Resources.getSystem();
		if(widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST){
			widthSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics());
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
		}
		
		if(heightMode == MeasureSpec.UNSPECIFIED || heightSize == MeasureSpec.AT_MOST){
			heightSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
		}
		
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		final int width = getWidth();
		final int height = getHeight();
		
		radius = Math.min(width, height) * 0.5f;
		centerY = radius;
		startX = radius;
		endX = width - radius;
		spotMinX = startX + borderWidth;
		spotMaxX = endX - borderWidth;
		spotSize = height - 4 * borderWidth;
		// Beppi: changed management of the position according to 3 states
//		spotX = toggleStatus ? spotMaxX : spotMinX;
		spotX = toggleStatus == on ? spotMaxX : toggleStatus == off ? spotMinX : spotMidX;
		offLineWidth = 0;
	}
	
	
	SimpleSpringListener springListener = new SimpleSpringListener(){
		@Override
		public void onSpringUpdate(Spring spring) {
			final double value = spring.getCurrentValue();
			calculateEffect(value);
		}
	};

	private int clamp(int value, int low, int high) {
		return Math.min(Math.max(value, low), high);
	}

	
	@Override
	public void draw(Canvas canvas) {
		rect.set(0, 0, getWidth(), getHeight());
		paint.setColor(borderColor);
		canvas.drawRoundRect(rect, radius, radius, paint);
		
		if(offLineWidth > 0){
			final float cy = offLineWidth * 0.5f;
			rect.set(spotX - cy, centerY - cy, endX + cy, centerY + cy);
			paint.setColor(enabled ? (toggleStatus == mid ? midColor : offColor) : disabledColor);
			canvas.drawRoundRect(rect, cy, cy, paint);
		}
		
		rect.set(spotX - 1 - radius, centerY - radius, spotX + 1.1f + radius, centerY + radius);
		paint.setColor(enabled ? borderColor : disabledColor);
		canvas.drawRoundRect(rect, radius, radius, paint);
		
		final float spotR = spotSize * 0.5f;
		rect.set(spotX - spotR, centerY - spotR, spotX + spotR, centerY + spotR);
		paint.setColor(enabled ? spotColor : disabledColor);
		canvas.drawRoundRect(rect, spotR, spotR, paint);
		
	}
	
	/**
	 * @param value
	 */
	/*
	private void calculateEffect(final double value) {
		final float mapToggleX = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, spotMinX, spotMaxX);
		spotX = mapToggleX;
		
		float mapOffLineWidth = (float) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1, 10, spotSize);
		
		offLineWidth = mapOffLineWidth;
		
		final int fromB = Color.blue(onColor);
		final int fromR = Color.red(onColor);
		final int fromG = Color.green(onColor);
		
		final int toB = Color.blue(offBorderColor);
		final int toR = Color.red(offBorderColor);
		final int toG = Color.green(offBorderColor);
		
		int springB = (int) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1, fromB, toB);
		int springR = (int) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1, fromR, toR);
		int springG = (int) SpringUtil.mapValueFromRangeToRange(1 - value, 0, 1, fromG, toG);
		
		springB = clamp(springB, 0, 255);
		springR = clamp(springR, 0, 255);
		springG = clamp(springG, 0, 255);
		
		borderColor = Color.rgb(springR, springG, springB);
		
		postInvalidate();
	}
*/
	private void calculateEffect(final double value) {
		final float mapToggleX = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, spotMinX, spotMaxX);
		spotX = mapToggleX;
		double min = 0, max = 0;
		int fromColor, toColor;
		if (previousToggleStatus == off && toggleStatus == mid) {
				toColor = offBorderColor; fromColor = midColor;
		} else
		if (previousToggleStatus == off && toggleStatus == on) {
			toColor = offBorderColor; fromColor = onColor;
		} else
		if (previousToggleStatus == mid && toggleStatus == on) {
			toColor = midColor; fromColor = onColor;
		} else
		if (previousToggleStatus == on && toggleStatus == off) {
			toColor = offBorderColor; fromColor = onColor;
		} else
		if (previousToggleStatus == on && toggleStatus == mid) {
			toColor = midColor; fromColor = onColor;
		} else
		{
			toColor = offBorderColor; fromColor = onColor;
		}

		if (previousToggleStatus == off) min = 0;
		else if (previousToggleStatus == mid) min = 0.5;
		else min = 1;
		if (toggleStatus == off) max = 0;
		else if (toggleStatus == mid) max = 0.5;
		else max = 1;

		if (min == max) { min = 0; max = 1; }
		else if (min > max) { double temp = min; min = max; max = temp; }

		offLineWidth = (float) SpringUtil.mapValueFromRangeToRange(min + max - value, min, max, 0, spotSize);

		final int fromB = Color.blue(fromColor);
		final int fromR = Color.red(fromColor);
		final int fromG = Color.green(fromColor);
		final int toB = Color.blue(toColor);
		final int toR = Color.red(toColor);
		final int toG = Color.green(toColor);

		int springB = (int) SpringUtil.mapValueFromRangeToRange(min + max - value, min, max, fromB, toB);
		int springR = (int) SpringUtil.mapValueFromRangeToRange(min + max - value, min, max, fromR, toR);
		int springG = (int) SpringUtil.mapValueFromRangeToRange(min + max - value, min, max, fromG, toG);

		springB = clamp(springB, 0, 255);
		springR = clamp(springR, 0, 255);
		springG = clamp(springG, 0, 255);

		borderColor = Color.rgb(springR, springG, springB);

		postInvalidate();
	}

	/**
	 * @author ThinkPad
	 *
	 */
	public interface OnToggleChanged{
		/**
		 * @param toggleStatus     = =
		 */
		// Beppi: changed according to 3 states value
//		public void onToggle(boolean on);
		public void onToggle(ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue);
	}

	public void setOnToggleChanged(OnToggleChanged onToggleChanged) {
		listener = onToggleChanged;
	}
	
	public boolean isAnimate() {
		return defaultAnimate;
	}
	public void setAnimate(boolean animate) {
		this.defaultAnimate = animate;
	}
	
// Beppi: added all next methods, getters and setters


	public int getOnColor() {
		return onColor;
	}

	public void setOnColor(int onColor) {
		this.onColor = onColor;
		postInvalidate();
	}

	public int getOffBorderColor() {
		return offBorderColor;
	}

	public void setOffBorderColor(int offBorderColor) {
		this.offBorderColor = offBorderColor;
		postInvalidate();
	}

	public int getOffColor() {
		return offColor;
	}

	public void setOffColor(int offColor) {
		this.offColor = offColor;
		postInvalidate();
	}

	public int getMidColor() {
		return midColor;
	}

	public void setMidColor(int midColor) {
		this.midColor = midColor;
		postInvalidate();
	}

	public int getSpotColor() {
		return spotColor;
	}

	public void setSpotColor(int spotColor) {
		this.spotColor = spotColor;
		postInvalidate();
	}

	public int getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
		postInvalidate();
	}

	public ToggleStatus getToggleStatus() {
		return toggleStatus;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
		postInvalidate();
	}

	public int getSpotSize() {
		return spotSize;
	}

	public void setSpotSize(int spotSize) {
		this.spotSize = spotSize;
		postInvalidate();
	}

	public boolean isMidSelectable() {
		return midSelectable;
	}

	public void setMidSelectable(boolean midSelectable) {
		this.midSelectable = midSelectable;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		postInvalidate();
		super.setEnabled(enabled);
	}
}
