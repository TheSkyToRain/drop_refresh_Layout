package com.ebaryice.drop_refresh_layout.layout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by Administrator on 2017/10/6/006.
 */

public class TouchPullView extends View{
    // 圆的画笔
    private Paint mCirclePaint;
    // 圆的半径
    private float mCircleRadius = 40;

    private float mCirclePointX,mCirclePointY;
    //可拖动的高度
    private int mDragHeight = 120;
    private float mProgress;
    // 整体效果宽度(因“机”而异)
    private int mTargetWidth = 520;
    //bezier曲线的路径.
    private Path mPath = new Path();
    private Paint mPathPaint;
    //控制点的Y坐标
    private int mTargetGravityHeight = 4;
    //切线角度
    private int mTangentAngle = 105;
    //释放时的恢复原形动画
    private ValueAnimator valueAnimator;

    private Interpolator mProgressInterpolator = new DecelerateInterpolator();
    private Interpolator mTangentInterpolator;


    public TouchPullView(Context context) {
        super(context);
        init();
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * 初始化方法
     */
    private void init(){
        // 圆笔
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        //设置抗锯齿
        p.setAntiAlias(true);
        //设置防抖动
        p.setDither(true);
        //设置填充方式
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.parseColor("#0099ff"));
        mCirclePaint = p;

        // bezier路径笔
        mPathPaint = p;
        // 切角路径插值器
        mTangentInterpolator = PathInterpolatorCompat.create((mCircleRadius*2.0f)/mDragHeight,90.0f/mTangentAngle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 进行基础坐标系改变
        int count = canvas.save();
        float tranX = (getWidth() -
                getValueBySlither(getWidth(),mTargetWidth,mProgress))/2;
        canvas.translate(tranX,0);

        // 画bezier
        canvas.drawPath(mPath,mPathPaint);

        // 画圆
        canvas.drawCircle(
                mCirclePointX,
                mCirclePointY,
                mCircleRadius,
                mCirclePaint);

        canvas.restoreToCount(count);
    }

    /**
     * 当进行测量时触发
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Mode --> 类型

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // 最小值
        int iHeight = (int) ((mDragHeight *mProgress + 0.5f)
                +getPaddingTop()+getPaddingBottom());
        int iWidth = (int) (2*mCircleRadius+getPaddingLeft()+getPaddingRight());

        // 测量值
        int measureWidth,measureHeight;

        if (widthMode == MeasureSpec.EXACTLY){
            // 有确切值
            measureWidth = width;
        }else if (widthMode == MeasureSpec.AT_MOST){
            // 有最大值
            measureWidth = Math.min(iWidth,width);
        }else{
            measureWidth = iWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY){
            // 有确切值
            measureHeight = height;
        }else if (widthMode == MeasureSpec.AT_MOST){
            // 有最大值
            measureHeight = Math.min(iHeight,height);
        }else{
            measureHeight = iHeight;
        }
        //设置测量的宽高
        setMeasuredDimension(measureWidth,measureHeight);
    }


    /**
     * 当大小改变时触发
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 当高度变化时,进行更新
        updatePathLayout();

    }


    /**
     * 设置进度
     *
     * @param progress 进度
     */
    public void setProgress(float progress){
        this.mProgress = progress;
        // 请求重新绘制
        requestLayout();
    }

    /**
     * 更新路径等相关操作
     */
    private void updatePathLayout(){
        // 得到进度
        final float progress = mProgressInterpolator.getInterpolation(mProgress);
        //可绘制区域的宽高
        final float w = getValueBySlither(getWidth(),mTargetWidth,mProgress);
        final float h = getValueBySlither(0,mDragHeight,mProgress);
        // 圆的中心点X坐标
        final float cPointX = w/2.0f;
        // 圆的半径
        final float cRadius = mCircleRadius;
        // 圆的中心点Y坐标
        final float cPointY = h - cRadius;
        // 控制点结束Y的值
        final float endControlY = mTargetGravityHeight;

        // 更新圆的坐标
        mCirclePointX = cPointX;
        mCirclePointY = cPointY;

        // ?路径复位
        final Path path = mPath;
        path.reset();
        path.moveTo(0,0);

        //左边的结束点和控制点
        float lEndPointX,lEndPointY;
        float lControlPointX,lControlPointY;

        float angle = mTangentAngle*mTangentInterpolator.getInterpolation(progress);
        double radian = Math.toRadians(angle);

        float x = (float) (Math.sin(radian)*cRadius);
        float y = (float) (Math.cos(radian)*cRadius);

        lEndPointX = cPointX - x;
        lEndPointY = cPointY + y;
        // 控制点Y坐标变化
        lControlPointY = getValueBySlither(0,endControlY,progress);
        // 控制点与结束点之间的高度差
        float tHeight  = lEndPointY - lControlPointY;
        // 控制点x距离
        float tWidth = (float) (tHeight/Math.tan(radian));

        lControlPointX = lEndPointX - tWidth;

        //bezier
        path.quadTo(lControlPointX,lControlPointY,lEndPointX,lEndPointY);
        // 再到right
        path.lineTo(cPointX+(cPointX-lEndPointX),lEndPointY);
        path.quadTo(cPointX+cPointX-lControlPointX,lControlPointY,w,0);

        //GG ,貌似忘记了什么。。
    }

    /**
     * 获取当前值
     * @param start
     * @param end
     * @param progress
     * @return
     */
    private float getValueBySlither(float start,float end,float progress){
        return start+(end-start)*progress;
    }

    /**
     * 释放动画
     */
    public void release(){
        if (valueAnimator == null){
            ValueAnimator animator = ValueAnimator.ofFloat(mProgress,0f);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(400);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Object val = valueAnimator.getAnimatedValue();
                    if (val instanceof Float){
                        setProgress((Float) val);
                    }
                }
            });
            valueAnimator = animator;
        }else {
            valueAnimator.cancel();
            valueAnimator.setFloatValues(mProgress,0f);
        }
        valueAnimator.start();
    }
}
