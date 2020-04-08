package com.example.userratingtask.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.example.userratingtask.R;

import java.math.BigDecimal;

public class RangeSeekBar<T extends Number> extends ImageView {

    public static final int ACTIVE_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);

    public static final int INVALID_POINTER_ID = 255;

    public static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

    public static final Integer DEFAULT_MINIMUM = 0;
    public static final Integer DEFAULT_MAXIMUM = 100;
    public static final Integer DEFAULT_STEP = 1;
    public static final int HEIGHT_IN_DP = 30;
    public static final int TEXT_LATERAL_PADDING_IN_DP = 3;

    private static final int INITIAL_PADDING_IN_DP = 8;
    private static final int DEFAULT_TEXT_SIZE_IN_DP = 14;
    private static final int DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP = 8;
    private static final int DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP = 8;

    private static final int LINE_HEIGHT_IN_DP = 1;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint();

    private Bitmap thumbImage;
    private Bitmap thumbPressedImage;
    private Bitmap thumbDisabledImage;

    private float thumbHalfWidth;
    private float thumbHalfHeight;

    private float padding;
    protected T absoluteMinValue, absoluteMaxValue, absoluteStepValue;
    protected NumberType numberType;
    protected double absoluteMinValuePrim, absoluteMaxValuePrim, absoluteStepValuePrim;
    protected double normalizedMinValue = 0d;
    protected double normalizedMaxValue = 1d;
    protected double minDeltaForDefault = 0;
    private Thumb pressedThumb = null;
    private boolean notifyWhileDragging = false;
    private OnRangeSeekBarChangeListener<T> listener;

    private float downMotionX;

    private int activePointerId = INVALID_POINTER_ID;

    private int scaledTouchSlop;

    private boolean isDragging;

    private int textOffset;
    private int textSize;
    private int distanceToTop;
    private RectF rect;

    private boolean singleThumb;
    private boolean alwaysActive;
    private boolean showLabels;
    private boolean showTextAboveThumbs;
    private float internalPad;
    private int activeColor;
    private int defaultColor;
    private int textAboveThumbsColor;

    private boolean thumbShadow;
    private int thumbShadowXOffset;
    private int thumbShadowYOffset;
    private int thumbShadowBlur;
    private Path thumbShadowPath;
    private Path translatedThumbShadowPath = new Path();
    private Matrix thumbShadowMatrix = new Matrix();

    private boolean activateOnDefaultValues;


    public RangeSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RangeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @SuppressWarnings("unchecked")
    private T extractNumericValueFromAttributes(TypedArray a, int attribute, int defaultValue) {
        TypedValue tv = a.peekValue(attribute);
        if (tv == null) {
            return (T) Integer.valueOf(defaultValue);
        }

        int type = tv.type;
        if (type == TypedValue.TYPE_FLOAT) {
            return (T) Float.valueOf(a.getFloat(attribute, defaultValue));
        } else {
            return (T) Integer.valueOf(a.getInteger(attribute, defaultValue));
        }
    }

    private void init(Context context, AttributeSet attrs) {
        float barHeight;
        int thumbNormal = R.drawable.thumb_left;
        int thumbPressed = R.drawable.thumb_left;
        int thumbDisabled = R.drawable.thumb_left;
        int thumbShadowColor;
        int defaultShadowColor = Color.argb(75, 0, 0, 0);
        int defaultShadowYOffset = PixelUtil.dpToPx(context, 2);
        int defaultShadowXOffset = PixelUtil.dpToPx(context, 0);
        int defaultShadowBlur = PixelUtil.dpToPx(context, 2);

        if (attrs == null) {
            setRangeToDefaultValues();
            internalPad = PixelUtil.dpToPx(context, INITIAL_PADDING_IN_DP);
            barHeight = PixelUtil.dpToPx(context, LINE_HEIGHT_IN_DP);
            activeColor = ACTIVE_COLOR;
            defaultColor = Color.GRAY;
            alwaysActive = false;
            showTextAboveThumbs = true;
            textAboveThumbsColor = Color.WHITE;
            thumbShadowColor = defaultShadowColor;
            thumbShadowXOffset = defaultShadowXOffset;
            thumbShadowYOffset = defaultShadowYOffset;
            thumbShadowBlur = defaultShadowBlur;
            activateOnDefaultValues = false;
        } else {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar, 0, 0);
            try {
                setRangeValues(
                        extractNumericValueFromAttributes(a, R.styleable.RangeSeekBar_absoluteMinValue, DEFAULT_MINIMUM),
                        extractNumericValueFromAttributes(a, R.styleable.RangeSeekBar_absoluteMaxValue, DEFAULT_MAXIMUM),
                        extractNumericValueFromAttributes(a, R.styleable.RangeSeekBar_step, DEFAULT_STEP)
                );
                showTextAboveThumbs = a.getBoolean(R.styleable.RangeSeekBar_valuesAboveThumbs, true);
                textAboveThumbsColor = a.getColor(R.styleable.RangeSeekBar_textAboveThumbsColor, Color.WHITE);
                singleThumb = a.getBoolean(R.styleable.RangeSeekBar_singleThumb, false);
                showLabels = a.getBoolean(R.styleable.RangeSeekBar_showLabels, true);
                internalPad = a.getDimensionPixelSize(R.styleable.RangeSeekBar_internalPadding, INITIAL_PADDING_IN_DP);
                barHeight = a.getDimensionPixelSize(R.styleable.RangeSeekBar_barHeight, LINE_HEIGHT_IN_DP);
                activeColor = a.getColor(R.styleable.RangeSeekBar_activeColor, ACTIVE_COLOR);
                defaultColor = a.getColor(R.styleable.RangeSeekBar_defaultColor, Color.GRAY);
                alwaysActive = a.getBoolean(R.styleable.RangeSeekBar_alwaysActive, false);

                Drawable normalDrawable = a.getDrawable(R.styleable.RangeSeekBar_thumbNormal);
                if (normalDrawable != null) {
                    thumbImage = BitmapUtil.drawableToBitmap(normalDrawable);
                }
                Drawable disabledDrawable = a.getDrawable(R.styleable.RangeSeekBar_thumbDisabled);
                if (disabledDrawable != null) {
                    thumbDisabledImage = BitmapUtil.drawableToBitmap(disabledDrawable);
                }
                Drawable pressedDrawable = a.getDrawable(R.styleable.RangeSeekBar_thumbPressed);
                if (pressedDrawable != null) {
                    thumbPressedImage = BitmapUtil.drawableToBitmap(pressedDrawable);
                }
                thumbShadow = a.getBoolean(R.styleable.RangeSeekBar_thumbShadow, false);
                thumbShadowColor = a.getColor(R.styleable.RangeSeekBar_thumbShadowColor, defaultShadowColor);
                thumbShadowXOffset = a.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbShadowXOffset, defaultShadowXOffset);
                thumbShadowYOffset = a.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbShadowYOffset, defaultShadowYOffset);
                thumbShadowBlur = a.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbShadowBlur, defaultShadowBlur);

                activateOnDefaultValues = a.getBoolean(R.styleable.RangeSeekBar_activateOnDefaultValues, false);
            } finally {
                a.recycle();
            }
        }

        if (thumbImage == null) {
            thumbImage = BitmapFactory.decodeResource(getResources(), thumbNormal);
        }
        if (thumbPressedImage == null) {
            thumbPressedImage = BitmapFactory.decodeResource(getResources(), thumbPressed);
        }
        if (thumbDisabledImage == null) {
            thumbDisabledImage = BitmapFactory.decodeResource(getResources(), thumbDisabled);
        }

        thumbHalfWidth = 0.5f * thumbImage.getWidth();
        thumbHalfHeight = 0.5f * thumbImage.getHeight();

        setValuePrimAndNumberType();

        textSize = PixelUtil.dpToPx(context, DEFAULT_TEXT_SIZE_IN_DP);
        distanceToTop = PixelUtil.dpToPx(context, DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP);
        textOffset = !showTextAboveThumbs ? 0 : this.textSize + PixelUtil.dpToPx(context,
                DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP) + this.distanceToTop;

        rect = new RectF(padding,
                textOffset + thumbHalfHeight - barHeight / 2,
                getWidth() - padding,
                textOffset + thumbHalfHeight + barHeight / 2);
        setFocusable(true);
        setFocusableInTouchMode(true);
        scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        if (thumbShadow) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            shadowPaint.setColor(thumbShadowColor);
            shadowPaint.setMaskFilter(new BlurMaskFilter(thumbShadowBlur, BlurMaskFilter.Blur.NORMAL));
            thumbShadowPath = new Path();
            thumbShadowPath.addCircle(0,
                    0,
                    thumbHalfHeight,
                    Path.Direction.CW);
        }
    }

    public void setRangeValues(T minValue, T maxValue) {
        this.absoluteMinValue = minValue;
        this.absoluteMaxValue = maxValue;
        setValuePrimAndNumberType();
    }

    public void setRangeValues(T minValue, T maxValue, T step) {
        this.absoluteStepValue = step;
        setRangeValues(minValue, maxValue);
    }

    public void setTextAboveThumbsColor(int textAboveThumbsColor) {
        this.textAboveThumbsColor = textAboveThumbsColor;
        invalidate();
    }

    public void setTextAboveThumbsColorResource(@ColorRes int resId) {
        setTextAboveThumbsColor(getResources().getColor(resId));
    }

    private void setRangeToDefaultValues() {
        this.absoluteMinValue = (T) DEFAULT_MINIMUM;
        this.absoluteMaxValue = (T) DEFAULT_MAXIMUM;
        this.absoluteStepValue = (T) DEFAULT_STEP;
        setValuePrimAndNumberType();
    }

    private void setValuePrimAndNumberType() {
        absoluteMinValuePrim = absoluteMinValue.doubleValue();
        absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
        absoluteStepValuePrim = absoluteStepValue.doubleValue();
        numberType = NumberType.fromNumber(absoluteMinValue);
    }

    public void resetSelectedValues() {
        setSelectedMinValue(absoluteMinValue);
        setSelectedMaxValue(absoluteMaxValue);
    }

    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }

    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    public T getAbsoluteMinValue() {
        return absoluteMinValue;
    }

    public T getAbsoluteMaxValue() {
        return absoluteMaxValue;
    }

    private T roundOffValueToStep(T value) {
        double d = Math.round(value.doubleValue() / absoluteStepValuePrim) * absoluteStepValuePrim;
        return (T) numberType.toNumber(Math.max(absoluteMinValuePrim, Math.min(absoluteMaxValuePrim, d)));
    }

    public T getSelectedMinValue() {
        return roundOffValueToStep(normalizedToValue(normalizedMinValue));
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void setSelectedMinValue(T value) {
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMinValue(0d);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
    }

    public T getSelectedMaxValue() {
        return roundOffValueToStep(normalizedToValue(normalizedMaxValue));
    }

    public void setSelectedMaxValue(T value) {
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMaxValue(1d);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
    }

    public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener<T> listener) {
        this.listener = listener;
    }

    public void setThumbShadowPath(Path thumbShadowPath) {
        this.thumbShadowPath = thumbShadowPath;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (!isEnabled()) {
            return false;
        }

        int pointerIndex;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(activePointerId);
                downMotionX = event.getX(pointerIndex);

                pressedThumb = evalPressedThumb(downMotionX);

                if (pressedThumb == null) {
                    return super.onTouchEvent(event);
                }

                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null) {

                    if (isDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(activePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - downMotionX) > scaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }

                    if (notifyWhileDragging && listener != null) {
                        listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                pressedThumb = null;
                invalidate();
                if (listener != null) {
                    listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getPointerCount() - 1;
                downMotionX = event.getX(index);
                activePointerId = event.getPointerId(index);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate();
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == activePointerId) {
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            downMotionX = ev.getX(newPointerIndex);
            activePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(activePointerId);
        final float x = event.getX(pointerIndex);
        if (Thumb.MIN.equals(pressedThumb) && !singleThumb) {
            setNormalizedMinValue(screenToNormalized(x));
        } else if (Thumb.MAX.equals(pressedThumb)) {
            setNormalizedMaxValue(screenToNormalized(x));
        }
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    void onStartTrackingTouch() {
        isDragging = true;
    }

    void onStopTrackingTouch() {
        isDragging = false;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = thumbImage.getHeight()
                + (!showTextAboveThumbs ? 0 : PixelUtil.dpToPx(getContext(), HEIGHT_IN_DP))
                + (thumbShadow ? thumbShadowYOffset + thumbShadowBlur : 0);
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected synchronized void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        paint.setTextSize(textSize);
        paint.setStyle(Style.FILL);
        paint.setColor(defaultColor);
        paint.setAntiAlias(true);
        float minMaxLabelSize = 0;
        if (showLabels) {
            String minLabel = getContext().getString(R.string.demo_min_label);
            String maxLabel = getContext().getString(R.string.demo_max_label);
            minMaxLabelSize = Math.max(paint.measureText(minLabel), paint.measureText(maxLabel));
            float minMaxHeight = textOffset + thumbHalfHeight + textSize / 3;
            canvas.drawText(minLabel, 0, minMaxHeight, paint);
            canvas.drawText(maxLabel, getWidth() - minMaxLabelSize, minMaxHeight, paint);
        }
        padding = internalPad + minMaxLabelSize + thumbHalfWidth;

        rect.left = padding;
        rect.right = getWidth() - padding;
        canvas.drawRect(rect, paint);

        boolean selectedValuesAreDefault = (normalizedMinValue <= minDeltaForDefault && normalizedMaxValue >= 1 - minDeltaForDefault);

        int colorToUseForButtonsAndHighlightedLine = !alwaysActive && !activateOnDefaultValues && selectedValuesAreDefault ?
                defaultColor : activeColor;
        rect.left = normalizedToScreen(normalizedMinValue);
        rect.right = normalizedToScreen(normalizedMaxValue);

        paint.setColor(colorToUseForButtonsAndHighlightedLine);
        canvas.drawRect(rect, paint);

        if (!singleThumb) {
            if (thumbShadow) {
                drawThumbShadow(normalizedToScreen(normalizedMinValue), canvas);
            }
            drawThumb(normalizedToScreen(normalizedMinValue), Thumb.MIN.equals(pressedThumb), canvas,
                    selectedValuesAreDefault);
        }

        if (thumbShadow) {
            drawThumbShadow(normalizedToScreen(normalizedMaxValue), canvas);
        }
        drawThumb(normalizedToScreen(normalizedMaxValue), Thumb.MAX.equals(pressedThumb), canvas,
                selectedValuesAreDefault);

        if (showTextAboveThumbs && (activateOnDefaultValues || !selectedValuesAreDefault)) {
            paint.setTextSize(textSize);
            paint.setColor(textAboveThumbsColor);

            String minText = valueToString(getSelectedMinValue());
            String maxText = valueToString(getSelectedMaxValue());
            float minTextWidth = paint.measureText(minText);
            float maxTextWidth = paint.measureText(maxText);
            float minPosition = Math.max(0f, normalizedToScreen(normalizedMinValue) - minTextWidth * 0.5f);
            float maxPosition = Math.min(getWidth() - maxTextWidth, normalizedToScreen(normalizedMaxValue) - maxTextWidth * 0.5f);
            if (!singleThumb) {
                int spacing = PixelUtil.dpToPx(getContext(), TEXT_LATERAL_PADDING_IN_DP);
                float overlap = minPosition + minTextWidth - maxPosition + spacing;
                if (overlap > 0f) {
                    minPosition -= overlap * normalizedMinValue / (normalizedMinValue + 1 - normalizedMaxValue);
                    maxPosition += overlap * (1 - normalizedMaxValue) / (normalizedMinValue + 1 - normalizedMaxValue);
                }
                canvas.drawText(minText,
                        minPosition,
                        distanceToTop + textSize,
                        paint);
            }
            canvas.drawText(maxText, maxPosition, distanceToTop + textSize, paint);
        }

    }

    protected String valueToString(T value) {
        return String.valueOf(value);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putDouble("MIN", normalizedMinValue);
        bundle.putDouble("MAX", normalizedMaxValue);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        final Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        normalizedMinValue = bundle.getDouble("MIN");
        normalizedMaxValue = bundle.getDouble("MAX");
    }

    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas, boolean areSelectedValuesDefault) {
        Bitmap buttonToDraw;
        if (!activateOnDefaultValues && areSelectedValuesDefault) {
            buttonToDraw = thumbDisabledImage;
        } else {
            buttonToDraw = pressed ? thumbPressedImage : thumbImage;
        }
        canvas.drawBitmap(buttonToDraw, screenCoord - thumbHalfWidth, textOffset, paint);
    }

    private void drawThumbShadow(float screenCoord, Canvas canvas) {
        thumbShadowMatrix.setTranslate(screenCoord + thumbShadowXOffset, textOffset + thumbHalfHeight + thumbShadowYOffset);
        translatedThumbShadowPath.set(thumbShadowPath);
        translatedThumbShadowPath.transform(thumbShadowMatrix);
        canvas.drawPath(translatedThumbShadowPath, shadowPaint);
    }

    private Thumb evalPressedThumb(float touchX) {
        Thumb result = null;
        boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue);
        boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue);
        if (minThumbPressed && maxThumbPressed) {
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }
        return result;
    }

    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth;
    }

    private void setNormalizedMinValue(double value) {
        normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
        invalidate();
    }

    private void setNormalizedMaxValue(double value) {
        normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
        invalidate();
    }

    protected T normalizedToValue(double normalized) {
        double v = absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim);
        // TODO parameterize this rounding to allow variable decimal points
        return (T) numberType.toNumber(Math.round(v * 100) / 100d);
    }

    protected double valueToNormalized(T value) {
        if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            return 0d;
        }
        return (value.doubleValue() - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim);
    }

    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }

    private double screenToNormalized(float screenCoord) {
        int width = getWidth();
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            double result = (screenCoord - padding) / (width - 2 * padding);
            return Math.min(1d, Math.max(0d, result));
        }
    }

    private enum Thumb {
        MIN, MAX
    }

    protected enum NumberType {
        LONG, DOUBLE, INTEGER, FLOAT, SHORT, BYTE, BIG_DECIMAL;

        public static <E extends Number> NumberType fromNumber(E value) throws IllegalArgumentException {
            if (value instanceof Long) {
                return LONG;
            }
            if (value instanceof Double) {
                return DOUBLE;
            }
            if (value instanceof Integer) {
                return INTEGER;
            }
            if (value instanceof Float) {
                return FLOAT;
            }
            if (value instanceof Short) {
                return SHORT;
            }
            if (value instanceof Byte) {
                return BYTE;
            }
            if (value instanceof BigDecimal) {
                return BIG_DECIMAL;
            }
            throw new IllegalArgumentException("Number class '" + value.getClass().getName() + "' is not supported");
        }

        public Number toNumber(double value) {
            switch (this) {
                case LONG:
                    return (long) value;
                case DOUBLE:
                    return value;
                case INTEGER:
                    return (int) value;
                case FLOAT:
                    return (float) value;
                case SHORT:
                    return (short) value;
                case BYTE:
                    return (byte) value;
                case BIG_DECIMAL:
                    return BigDecimal.valueOf(value);
            }
            throw new InstantiationError("can't convert " + this + " to a Number object");
        }
    }

    public interface OnRangeSeekBarChangeListener<T extends Number> {
        void onRangeSeekBarValuesChanged(RangeSeekBar<T> bar, T minValue, T maxValue);
    }

}