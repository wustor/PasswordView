package com.fatchao.passwordview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class PasswordView extends View {

    private int mCircleColorChecked;//选中的圆的颜色
    private int mCircleColorUnchecked;//未选中的圆的颜色
    private int mNumberColorChecked;//选中的圆中的数字的颜色
    private int mNumberColorUnchecked;//未选中的圆中的数字的颜色
    private int mLineColor;//中间线的颜色
    private int mTextColor;//底部文字的颜色
    private int mCircleRadius;//圆的半径
    private int mTextSize;//文字的大小
    private int mTextPadding;//文字与顶部的间距,如果测量模式是Exactly的话不需要指定，其余的控件摆放完剩余的宽度就是padding，如果是AT_MOST的就必须指定,否则，无法计算View的宽度
    private int mEdgeLineWidth;//左右两边线的宽度
    private int mCenterLineWidth;//中间线的间隔宽度：如果测量模式是Exactly的话不需要指定，可以自动均分，如果是AT_MOST必须指定，否则，无法计算View的宽度
    private String[] mTopNames;//上面的数字数组
    private String[] mBottomNames;//下面的文字数组
    private int childNumbers;//需要绘制的圆的数量，由数组的长度来确定
    private int mCheckedNumber;//当前选中的位置
    private Paint mPaint;//画笔
    private int mNumberWidth;//数字宽度
    private int mNumberHeight;//数字高度
    private int mTextWidth;//文本宽度
    private int mTextHeight;//文本高度
    private Paint.FontMetrics mFontMetrics;

    public PasswordView(Context context) {
        this(context, null);
    }

    public PasswordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public PasswordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PasswordView);
        mCircleColorChecked = typedArray.getColor(R.styleable.PasswordView_circle_color_checked, Color.RED);
        mCircleColorUnchecked = typedArray.getColor(R.styleable.PasswordView_circle_color_unchecked, Color.RED);
        mNumberColorChecked = typedArray.getColor(R.styleable.PasswordView_number_color_checked, Color.RED);
        mNumberColorUnchecked = typedArray.getColor(R.styleable.PasswordView_number_color_unchecked, Color.RED);
        mTextPadding = toPx(typedArray.getDimension(R.styleable.PasswordView_text_padding, toPx(12.0f)));
        mLineColor = typedArray.getColor(R.styleable.PasswordView_line_color, Color.RED);
        mTextColor = typedArray.getColor(R.styleable.PasswordView_text_color, Color.RED);
        mCircleRadius = toPx(typedArray.getDimension(R.styleable.PasswordView_circle_radius, 0));
        mTextSize = toPx(typedArray.getDimension(R.styleable.PasswordView_text_size, 0));
        mEdgeLineWidth = toPx(typedArray.getDimension(R.styleable.PasswordView_edge_line_width, 0));
        mCenterLineWidth = toPx(typedArray.getDimension(R.styleable.PasswordView_center_line_width, 0));
        mCheckedNumber = typedArray.getInteger(R.styleable.PasswordView_checkedNumber, 0);
        int topNamesId = typedArray.getResourceId(R.styleable.PasswordView_topName, 0);
        if (topNamesId != 0)
            mTopNames = getResources().getStringArray(topNamesId);
        int bottomNamesId = typedArray.getResourceId(R.styleable.PasswordView_bottomName, 0);
        if (bottomNamesId != 0)
            mBottomNames = getResources().getStringArray(bottomNamesId);
        childNumbers = mBottomNames.length;
        typedArray.recycle();
        initPaint();
    }

    //初始化画笔,因为要测量宽度跟高度，需要知道Text的文本宽高
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//是否抗锯齿
        mPaint.setTextAlign(Paint.Align.LEFT);//确定矩形的绘制位置
        mPaint.setTextSize(mTextSize);
        mFontMetrics = mPaint.getFontMetrics();
        mNumberWidth = (int) mPaint.measureText(mTopNames[0]);//数字宽度
        mNumberHeight = (int) (mFontMetrics.bottom - mFontMetrics.top);//数字高度
        mTextWidth = (int) mPaint.measureText(mBottomNames[0]);//文字宽度
        mTextHeight = mNumberHeight;//高度是一样的
    }

    private int toPx(float dimen) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dimen, getResources().getDisplayMetrics());
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredWidth(widthMeasureSpec), measuredHeight(heightMeasureSpec));
    }


    //测量宽度
    private int measuredWidth(int widthMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY://width为match或者具体的长度
                result = width;
                //通过均分来计算中间分发现的宽度
                mCenterLineWidth = (result - getPaddingLeft() - getPaddingRight() - 2 * mEdgeLineWidth - 2 * mCircleRadius * childNumbers) / (childNumbers - 1);
                break;
            case MeasureSpec.AT_MOST://width为wrap
                //通过自定义属性来计算测量的宽度
                int realWidth = getPaddingLeft() + getPaddingLeft() + 2 * mEdgeLineWidth + 2 * mCircleRadius * childNumbers + mCenterLineWidth * (childNumbers - 1);
                result = Math.min(realWidth, width);
                break;
        }
        return result;
    }

    //测量高度
    private int measuredHeight(int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY://height为match或者具体的长度
                result = height;
                break;
            case MeasureSpec.AT_MOST://height为wrap_content
                int realHeight = getPaddingTop() + getPaddingBottom() + 2 * mCircleRadius + mTextPadding + mTextHeight;
                result = Math.min(height, realHeight);
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startX = getPaddingLeft();
        float endX = getMeasuredWidth() - getPaddingRight();
        float startY = getPaddingTop() + mCircleRadius;
        canvas.drawLine(startX,startY,endX,startY,mPaint);
        //画圆及圆中的数字
        for (int i = 0; i < mTopNames.length; i++) {
            float cx = getPaddingLeft() + mEdgeLineWidth + mCircleRadius + i * (mCenterLineWidth + 2 * mCircleRadius);//圆心横坐标
            float cy = getPaddingTop() + mCircleRadius;//圆心纵坐标
            float baseNumberX = cx - mNumberWidth / 2;//数字文本框的左上定点
            float baseNumberY = cy + mNumberHeight / 2 - mFontMetrics.bottom;//文字文本框的基线
            float baseTextX = cx - mTextWidth / 2;//文字文本框的左上定点
            float baseTextY = getHeight() - getPaddingBottom() - mFontMetrics.bottom;//文字文本框的基线
            if (i == mCheckedNumber) {
                //画实心圆
                mPaint.setColor(mCircleColorChecked);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
                //描边
                mPaint.setColor(mCircleColorChecked);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
                //画数字
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mNumberColorChecked);
                canvas.drawText(mTopNames[i], baseNumberX, baseNumberY, mPaint);
            } else {
                //画空心圆
                mPaint.setColor(mCircleColorUnchecked);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
                //描边
                mPaint.setColor(mNumberColorUnchecked);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
                //画数字
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mNumberColorUnchecked);
                canvas.drawText(mTopNames[i], baseNumberX, baseNumberY, mPaint);

            }
            //画文字
            mPaint.setColor(mTextColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(mBottomNames[i], baseTextX, baseTextY, mPaint);
            mPaint.setColor(mLineColor);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(3);

        }
    }

}