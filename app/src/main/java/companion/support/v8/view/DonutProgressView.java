package companion.support.v8.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v8.R;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;


/**
 * A Donut Progress.
 *
 * @author Vitor Ribeiro
 */
@SuppressWarnings("unused")
public class DonutProgressView extends View {

    private static final String INSTANCE_STATE = "saved_instance";

    private static final String INSTANCE_STARTING_DEGREE = "starting_degree";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_INNER_STRING = "progress";
    private static final String INSTANCE_MAX = "max";

    private static final String INSTANCE_FINISHED_STROKE_WIDTH = "finished_stroke_width";
    private static final String INSTANCE_UNFINISHED_STROKE_WIDTH = "unfinished_stroke_width";
    private static final String INSTANCE_FINISHED_STROKE_COLOR = "finished_stroke_color";
    private static final String INSTANCE_UNFINISHED_STROKE_COLOR = "unfinished_stroke_color";
    private static final String INSTANCE_BACKGROUND_COLOR = "inner_background_color";

    private static final String INSTANCE_TEXT_SIZE = "text_size";
    private static final String INSTANCE_TEXT_COLOR = "text_color";
    private static final String INSTANCE_TEXT = "text";

    private int startingDegree;
    private float progress;
    private int max;

    private float finishedStrokeWidth;
    private float unfinishedStrokeWidth;
    private int finishedStrokeColor;
    private int unfinishedStrokeColor;
    private int innerBackgroundColor;

    private boolean showProgress;
    private boolean showText;

    private float textSize;
    private int textColor;
    private String text;

    private final int minSize;
    private RectF finishedOuterRect;
    private RectF unfinishedOuterRect;

    private Paint finishedPaint;
    private Paint unfinishedPaint;
    private Paint innerCirclePaint;
    private Paint textPaint;

    public DonutProgressView(Context context) {
        this(context, null);
    }

    public DonutProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DonutProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources resources = getResources();
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DonutProgressView, defStyleAttr, 0);

        startingDegree = attributes.getInt(R.styleable.DonutProgressView_donut_circle_starting_degree, -90);
        progress = attributes.getFloat(R.styleable.DonutProgressView_donut_progress, 0);
        max = attributes.getInt(R.styleable.DonutProgressView_donut_max, 100);

        finishedStrokeWidth = attributes.getDimension(R.styleable.DonutProgressView_donut_finished_stroke_width, dp2px(resources, 3));
        unfinishedStrokeWidth = attributes.getDimension(R.styleable.DonutProgressView_donut_unfinished_stroke_width, dp2px(resources, 3));
        finishedStrokeColor = attributes.getColor(R.styleable.DonutProgressView_donut_finished_color, Color.CYAN);
        unfinishedStrokeColor = attributes.getColor(R.styleable.DonutProgressView_donut_unfinished_color, Color.GRAY);
        innerBackgroundColor = attributes.getColor(R.styleable.DonutProgressView_donut_background_color, Color.TRANSPARENT);

        showProgress = attributes.getBoolean(R.styleable.DonutProgressView_donut_show_progress, true);
        showText = attributes.getBoolean(R.styleable.DonutProgressView_donut_show_text, true);

        textSize = attributes.getDimension(R.styleable.DonutProgressView_donut_text_size, sp2px(resources, 28));
        textColor = attributes.getColor(R.styleable.DonutProgressView_donut_text_color, Color.CYAN);
        text = attributes.getString(R.styleable.DonutProgressView_donut_text);

        minSize = (int) dp2px(resources, 100);
        finishedOuterRect = new RectF();
        unfinishedOuterRect = new RectF();

        attributes.recycle();
        initPainters();
    }

    private float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    private float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    private void initPainters() {
        finishedPaint = new Paint();
        finishedPaint.setColor(finishedStrokeColor);
        finishedPaint.setStyle(Paint.Style.STROKE);
        finishedPaint.setAntiAlias(true);
        finishedPaint.setStrokeWidth(finishedStrokeWidth);

        unfinishedPaint = new Paint();
        unfinishedPaint.setColor(unfinishedStrokeColor);
        unfinishedPaint.setStyle(Paint.Style.STROKE);
        unfinishedPaint.setAntiAlias(true);
        unfinishedPaint.setStrokeWidth(unfinishedStrokeWidth);

        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(innerBackgroundColor);
        innerCirclePaint.setAntiAlias(true);

        if (showText || showProgress) {
            textPaint = new TextPaint();
            textPaint.setColor(textColor);
            textPaint.setTextSize(textSize);
            textPaint.setAntiAlias(true);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());

        bundle.putInt(INSTANCE_STARTING_DEGREE, getStartingDegree());
        bundle.putFloat(INSTANCE_PROGRESS, getProgress());
        bundle.putInt(INSTANCE_MAX, getMax());

        bundle.putFloat(INSTANCE_FINISHED_STROKE_WIDTH, getFinishedStrokeWidth());
        bundle.putFloat(INSTANCE_UNFINISHED_STROKE_WIDTH, getUnfinishedStrokeWidth());
        bundle.putInt(INSTANCE_FINISHED_STROKE_COLOR, getFinishedStrokeColor());
        bundle.putInt(INSTANCE_UNFINISHED_STROKE_COLOR, getUnfinishedStrokeColor());
        bundle.putInt(INSTANCE_BACKGROUND_COLOR, getInnerBackgroundColor());

        bundle.putFloat(INSTANCE_TEXT_SIZE, getTextSize());
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
        bundle.putString(INSTANCE_TEXT, getText());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof Bundle)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final Bundle bundle = (Bundle) state;

        startingDegree = bundle.getInt(INSTANCE_STARTING_DEGREE);
        progress = bundle.getFloat(INSTANCE_PROGRESS);
        max = bundle.getInt(INSTANCE_MAX);

        finishedStrokeWidth = bundle.getFloat(INSTANCE_FINISHED_STROKE_WIDTH);
        unfinishedStrokeWidth = bundle.getFloat(INSTANCE_UNFINISHED_STROKE_WIDTH);
        finishedStrokeColor = bundle.getInt(INSTANCE_FINISHED_STROKE_COLOR);
        unfinishedStrokeColor = bundle.getInt(INSTANCE_UNFINISHED_STROKE_COLOR);
        innerBackgroundColor = bundle.getInt(INSTANCE_BACKGROUND_COLOR);

        textSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
        textColor = bundle.getInt(INSTANCE_TEXT_COLOR);
        text = bundle.getString(INSTANCE_TEXT);

        invalidate();
        super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
    }

    @Override
    public void invalidate() {
        initPainters();
        super.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            return size;
        }

        int result = minSize;
        if (mode == MeasureSpec.AT_MOST) {
            result = Math.min(result, size);
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float delta = Math.max(finishedStrokeWidth, unfinishedStrokeWidth);
        finishedOuterRect.set(delta, delta,
                getWidth() - delta,
                getHeight() - delta);
        unfinishedOuterRect.set(delta, delta,
                getWidth() - delta,
                getHeight() - delta);

        float innerCircleRadius = (getWidth() - Math.min(finishedStrokeWidth, unfinishedStrokeWidth) + Math.abs(finishedStrokeWidth - unfinishedStrokeWidth)) / 2f;
        canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, innerCircleRadius, innerCirclePaint);
        canvas.drawArc(finishedOuterRect, getStartingDegree(), getProgressAngle(), false, finishedPaint);
        canvas.drawArc(unfinishedOuterRect, getStartingDegree() + getProgressAngle(), 360 - getProgressAngle(), false, unfinishedPaint);

        String innerText = "";
        float margin = 0.0f;

        if (showText && !TextUtils.isEmpty(text)) {
            innerText += text;
        }

        if (showProgress) {
            innerText += Math.round(progress);
            margin = 6.0f;
        }

        if (TextUtils.isEmpty(innerText)) {
            return;
        }

        textPaint.setTextSize(textSize);
        float textHeight = textPaint.descent() + textPaint.ascent();
        float textX = (getWidth() - textPaint.measureText(innerText)) / 2.0f - margin;
        float textY = (getHeight() - textHeight) / 2.0f;
        canvas.drawText(innerText, textX, textY, textPaint);

        if (!showProgress) {
            return;
        }
        textPaint.setTextSize(textSize / 2.0f);
        float suffixHeight = textPaint.descent() + textPaint.ascent();
        float suffixX = getWidth() / 2.0f + textPaint.measureText(innerText);
        float suffixY = textY + textHeight - suffixHeight;
        canvas.drawText("%", suffixX, suffixY, textPaint);


    }

    private float getProgressAngle() {
        return getProgress() / (float) max * 360f;
    }

    public int getStartingDegree() {
        return startingDegree;
    }

    public void setStartingDegree(int startingDegree) {
        this.startingDegree = startingDegree;
        this.invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        if (this.progress > getMax()) {
            this.progress %= getMax();
        }
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max > 0) {
            this.max = max;
            invalidate();
        }
    }

    public float getFinishedStrokeWidth() {
        return finishedStrokeWidth;
    }

    public void setFinishedStrokeWidth(float finishedStrokeWidth) {
        this.finishedStrokeWidth = finishedStrokeWidth;
        this.invalidate();
    }

    public float getUnfinishedStrokeWidth() {
        return unfinishedStrokeWidth;
    }

    public void setUnfinishedStrokeWidth(float unfinishedStrokeWidth) {
        this.unfinishedStrokeWidth = unfinishedStrokeWidth;
        this.invalidate();
    }

    public int getFinishedStrokeColor() {
        return finishedStrokeColor;
    }

    public void setFinishedStrokeColor(int finishedStrokeColor) {
        this.finishedStrokeColor = finishedStrokeColor;
        this.invalidate();
    }

    public int getUnfinishedStrokeColor() {
        return unfinishedStrokeColor;
    }

    public void setUnfinishedStrokeColor(int unfinishedStrokeColor) {
        this.unfinishedStrokeColor = unfinishedStrokeColor;
        this.invalidate();
    }

    public int getInnerBackgroundColor() {
        return innerBackgroundColor;
    }

    public void setInnerBackgroundColor(int innerBackgroundColor) {
        this.innerBackgroundColor = innerBackgroundColor;
        this.invalidate();
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        this.invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        this.invalidate();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.invalidate();
    }
}