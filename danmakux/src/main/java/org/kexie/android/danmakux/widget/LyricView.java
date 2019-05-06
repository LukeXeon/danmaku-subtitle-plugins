package org.kexie.android.danmakux.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import org.kexie.android.danmakux.R;
import org.kexie.android.danmakux.model.Lyric;

import java.util.Collections;
import java.util.List;


public class LyricView extends AppCompatTextView {
    public LyricView(Context context) {
        this(context, null);
    }

    public LyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(
                attrs, R.styleable.LyricView, 0, 0);
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        try {
            mLyricsSelected = attributes.getColor(R.styleable.LyricView_selectedColor,
                    Color.parseColor("#6200ea"));
            mLyricsNormal = attributes.getColor(R.styleable.LyricView_normalColor,
                    Color.parseColor("#7c4dff"));
            mBigTextSize = attributes.getDimension(R.styleable.LyricView_bigTextSize,
                    20);
            mSmallTextSize = attributes.getDimension(R.styleable.LyricView_bigTextSize,
                    15);
            mLineHeight = (int) (attributes.getDimension(R.styleable.LyricView_lineHeight,
                    30) * metrics.density + 0.5f);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            attributes.recycle();
        }
        mPaint = new Paint();
        mBounds = new Rect();
        mSingleBounds = new Rect();
        setMaxLines(2);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mLyricsSelected);
        mPaint.setTextSize(mBigTextSize);
        musicLyrList = Collections.emptyList();
    }

    private Paint mPaint;
    private int mViewW;
    private int mViewH;
    private String mCurrentLrc;
    private List<Lyric> musicLyrList;
    private int mCenterLine;
    private float mBigTextSize;
    private int mLyricsSelected;
    private float mSmallTextSize;
    private int mLyricsNormal;
    private int mLineHeight;
    private int mDuration;
    private int mCurrent;
    private Rect mBounds;
    private Rect mSingleBounds;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewW = w;
        mViewH = h;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (musicLyrList == null || musicLyrList.size() == 1 || musicLyrList.size() == 0) {
            drawSingLine(canvas);
        } else {
            drawMultiLine(canvas);
        }
    }

    /**
     * 绘制多行歌词。
     *
     * @param canvas c
     */
    private void drawMultiLine(Canvas canvas) {
        //        中间行y=中间行开始位置-移的距离
        int lineTime;
        //        最后一行居中
        if (mCenterLine == musicLyrList.size() - 1) {
            //     行可用时间 = 总进度 - 行开始时间
            lineTime = mDuration - musicLyrList.get(mCenterLine)
                    .getStartTime();
        } else {
//                           其它行居中，
//            行可用时间 = 下一行开始 时间 - 居中行开始 时间
            lineTime = musicLyrList.get(mCenterLine + 1)
                    .getStartTime() - musicLyrList.get(mCenterLine)
                    .getStartTime();
        }
        //          播放时间偏移 = 播放进度 - 居中开始时间
        int offsetTime = mCurrent - musicLyrList.get(mCenterLine)
                .getStartTime();
        //           播放时间比 = 播放时间偏移/行可用时间
        float offsetTimePercent = offsetTime / (float) lineTime;

        //          y方向移动的距离 = 行高*播放时间比
        int offsetY = (int) (mLineHeight * offsetTimePercent);
        //          中间行歌词
        String centerLrc = musicLyrList.get(mCenterLine)
                .getContent();
        // 歌词绘制的边界
        mPaint.getTextBounds(centerLrc, 0, centerLrc.length(), mBounds);
        //          中间行 y view 高度一半 + text高度一半
        //          中间行y = 中间行开始 位置 - 移动的距离
        int centerY = mViewH / 2 + mBounds.height() / 2 - offsetY;
        for (int i = 0; i < musicLyrList.size(); i++) {
            if (i == mCenterLine) {
                mPaint.setTextSize(mBigTextSize);
                mPaint.setColor(mLyricsSelected);
            } else {

                mPaint.setTextSize(mSmallTextSize);
                mPaint.setColor(mLyricsNormal);
            }
            mCurrentLrc = musicLyrList.get(i)
                    .getContent();


            float textW = mPaint.measureText(mCurrentLrc, 0, mCurrentLrc.length());

            float x = (mViewW >> 1) - textW / 2;
            float y = centerY + (i - mCenterLine) * mLineHeight;
            canvas.drawText(mCurrentLrc, 0, mCurrentLrc.length(), x, y, mPaint);

        }

    }

    /**
     * 根据播放时间滚动歌词，将已经播放的歌词滚动出屏幕。
     *
     * @param progress p
     * @param duration d
     */
    public void update(int progress, int duration) {
        if (musicLyrList == null || musicLyrList.size() == 0) {
            return;
        }
        this.mCurrent = progress;
        this.mDuration = duration;
        int startTime = musicLyrList.get(musicLyrList.size() - 1)
                .getStartTime();

        if (progress >= startTime) {
            mCenterLine = musicLyrList.size() - 1;
        } else {
            for (int i = 0; i < musicLyrList.size() - 1; i++) {
                boolean b = progress >= musicLyrList.get(i)
                        .getStartTime() && progress < musicLyrList.get(i + 1)
                        .getStartTime();
                if (b) {
                    mCenterLine = i;
                    break;
                }
            }


        }
//        触发重新绘制
        invalidate();
    }

    /**
     * 根据歌曲名和歌手名查找歌词，并将歌词解析到List里。
     *
     * @param lrcList s
     */
    public void setLrc(List<Lyric> lrcList) {
        musicLyrList = lrcList == null ? Collections.emptyList() : lrcList;
        //默认剧中行=0
        mCenterLine = 0;
        invalidate();
    }


    private void drawSingLine(Canvas canvas) {
        mCurrentLrc = "暂无歌词";
        mPaint.setColor(mLyricsNormal);
        mPaint.getTextBounds(mCurrentLrc, 0, mCurrentLrc.length(), mSingleBounds);
        float x = (mViewW >> 1) - (mSingleBounds.width() >> 1);
        float y = (mViewH >> 1) + (mSingleBounds.height() >> 1);
        canvas.drawText(mCurrentLrc, 0, mCurrentLrc.length(), x, y, mPaint);
    }
}