package com.weizongwei.fireworks.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.weizongwei.fireworks.utils.ViewPickColorsUtil;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by weizongwei on 15-10-27.
 *
 * 爆炸view
 */
public class BangView extends View {

    public static final int RADIUS = (int) (4 * Resources.getSystem().getDisplayMetrics().density + 0.5f);

    ArrayList<Integer> radomPickColors;
    private Point[] pointList;

    private int upHeight = (int) (80 * Resources.getSystem().getDisplayMetrics().density + 0.5f);

    private Paint mPaint;
    private View paintView;

    //如下几个值用于 计算颗粒的 起始，顶部，落点位置
    private int top, height, left, width, xcenter, ycenter, right, bottom;

    public BangView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BangView(Activity activity) {
        super(activity);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (pointList != null) {
            drawCircleList(canvas);
        }
    }


    public void setPaintView(View v) {
        paintView = v;
        top = v.getTop();
        left = v.getLeft();

        height = v.getBottom() - top;
        width = v.getRight() - left;

        xcenter = left + width / 2;
        ycenter = top + height / 2;

        bottom = v.getBottom();
        right = v.getRight();

        int len=width/RADIUS/2*2;// 先/2，是根据颗粒的直径得出颗粒总数 ； 总数×2，是为了增加颗粒数。
        Log.d("XXXxxx","width:"+width+"  radius:"+RADIUS+" left:"+left);
        radomPickColors=ViewPickColorsUtil.radomPickColors(paintView,len+1);
    }


    private void drawCircleList(Canvas canvas) {
        if(pointList!=null) {
            for (Point point : pointList) {
                mPaint.setColor(point.getColor());
                canvas.drawCircle(point.getX(), point.getY(), RADIUS, mPaint);
            }
        }
    }

    private void shake() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(350);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            Random random = new Random();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                paintView.setTranslationX((random.nextFloat() - 0.5f) * paintView.getWidth() * 0.1f);
                paintView.setTranslationY((random.nextFloat() - 0.5f) * paintView.getHeight() * 0.1f);

            }
        });
        animator.start();


        paintView.animate()
                .setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                creatPointList();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        })
                .setDuration(350)
                .setStartDelay(10)
                .scaleX(0f).scaleY(0f)
                .alpha(0f).start();
    }

    public void startAnimation() {
        shake();
    }

    private void creatPointList()
    {
        int len=width/RADIUS/2*2;
        //len的结果  先/2，是根据颗粒的直径得出颗粒总数； 总数×2，是为了增加颗粒数。
        pointList=new Point[len];
        int xbegin=left+width/4;//一堆颗粒的最左边 和最右边都应该预留1/4的区域
        for (int i_index = 0; i_index <len; i_index ++) {
            int color=Color.RED;
            if(radomPickColors!=null&&radomPickColors.size()>i_index)
            {
                color=radomPickColors.get(i_index);
            }
            createPoint(xbegin+RADIUS*i_index/2, ycenter, color,i_index);
        }
    }

    private void createPoint(int x, int y, int col,final int index) {
        //Point startPoint = new Point(left + RADIUS, ycenter);
        //Point endPoint = new Point(left + RADIUS, top - RADIUS - upHeight);

        Point beginPoint = new Point(x, y, col);
        pointList[index]=beginPoint;
        Log.d("XXXX start", " x:" + x + " y:" + y);
        ValueAnimator anim = ValueAnimator.ofObject(
                new PointEvaluator(),
                beginPoint,
                getTopPoint(beginPoint),
                getEndPoint(beginPoint)
        );

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                pointList[index] = (Point) animation.getAnimatedValue();
                mPaint.setColor(pointList[index].getColor());
                invalidate();
            }
        });
        anim.setInterpolator(new DecelerateAccelerateInterpolator());
        anim.setDuration(600 + ViewPickColorsUtil.getRadomInt(200));//时间后面要追加随机数
        anim.start();

    }


    private Point getTopPoint(Point p) {
        float x = p.getX() + (p.getX() - xcenter);
        float y = p.getY() - upHeight;//跑到顶部的位置的高度 要有个随机数

        x+= ViewPickColorsUtil.getRadomInt(50);
        y+= ViewPickColorsUtil.getRadomInt(60);

        Log.d("XXXX top", " x:" + x + " y:" + y);
        return new Point(x, y, p.getColor());
    }

    private Point getEndPoint(Point p) {
        float x = p.getX() + 2 * (p.getX() - xcenter);
        float y = bottom;


        x+= ViewPickColorsUtil.getRadomInt(50);
        y+= ViewPickColorsUtil.getRadomInt(60);


        Log.d("XXXX end", " x:" + x + " y:" + y);
        return new Point(x, y, p.getColor());
    }


    //添加进页面中
    public static BangView add2RootView(Activity activity) {

        ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        BangView surfview = new BangView(activity);

        rootView.addView(
                surfview,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );


        return surfview;
    }
}