package com.wqd.widget;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 环形指示器
 * Author: wangqi 
 * Date: 2021/6/90 星期三 09:46
 */
public class RingIndicator extends View {
    private static final boolean LOG_ENABLE = BuildConfig.DEBUG;

    /**
     * 滑动阈值
     */
    private final int TOUCH_SLOP;
    /**
     * 惯性滑动最小、最大速度
     */
    private final int MIN_FLING_VELOCITY;
    private final int MAX_FLING_VELOCITY;

    /**
     * 背景色
     */
    private int bgColor;
    /**
     * 刻度颜色
     */
    private int keduColor;
    /**
     * 短刻度线宽度
     */
    private float shortLineWidth;
    /**
     * 长刻度线宽度
     * 默认 = 2 * shortLineWidth
     */
    private float longLineWidth ;
    /**
     * 短刻度长度
     */
    private float shortKeduLen;
    /**
     * 长刻度长度
     * 默认为短刻度的2倍
     */
    private float longKeduLen;
    /**
     * 刻度字体颜色
     */
    private int textColor;
    /**
     * 长刻度字体大小
     */
    private float longKedutextSize;
	/**
     * 短刻度字体大小
     */
    private float shortKedutextSize;
	/**
     * 短刻度字体总偏移量
     */
    private float shortKedutextOffsetY;
	/**
     * 长刻度字体总偏移量
     */
    private float longKedutextOffsetY;
    /**
     * 中间指针线颜色
     */
    private int indicatorLineColor;
    /**
     * 中间指针线宽度
     */
    private float indicatorLineWidth;
    /**
     * 中间指针线长度
     */
    private float indicatorLineLen;
	/**
     * 放大倍数
     */
    private float scaleValue;
	private float scaleValue01;// 倒数 1/scaleValue
    /**
     * 最小值
     */
    private float minValue;
    /**
     * 最大值
     */
    private float maxValue;
    /**
     * 当前值
     */
    private float currentValue;
	/**
     * 最小刻度单位
     */
    private float smallestKeduUnit;
    /**
     * 刻度单位
     */
    private float keduUnit;
    /**
     * 需要绘制的数值
     */
    private int numberPerCount;
    /**
     * 刻度间距离
     */
    private float kedu_space;
	private float kedu_space01; // 倒数 1/kedu_space
    /**
     * 刻度与文字的间距
     */
    private float keduNumberSpace;

    /**
     * 最小数值，放大10倍：minValue * 10
     */
    private int mMinNumber;
    /**
     * 最大数值，放大10倍：maxValue * 10
     */
    private int mMaxNumber;
    /**
     * 当前数值
     */
    private int mCurrentNumber;
	private int mCurrentNumber123;
    /**
     * 最大数值与最小数值间的距离：(mMaxNumber - mMinNumber) / mNumberUnit * gradationGap
     */
    private float mMinMaxDistance;
    /**
     * 刻度数值最小单位
     */
    private int mNumberUnit;
	private float mNumberUnit01; // 倒数 1/mNumberUnit
    /**
     * 当前数值与最小值的距离：(mCurrentNumber - minValue) / mNumberUnit * gradationGap
     */
    private float mCurrentDistanceToMin;
	private float mCurrentDistanceToMin123;
	/**
     * 控件宽度所分的段数的二分之一：mWidth / keduSpace
     */
    private int mSegmentsOfHalfWidth;
    /**
     * 控件宽度所占有的数值范围：mWidth / gradationGap * mNumberUnit
     */
    private int mWidthNumberRange;

    /**
     * 普通画笔
     */
    private Paint mPaint;
    /**
     * 文字画笔
     */
    private TextPaint mTextPaint;
    /**
     * 滑动器
     */
    private Scroller mScroller;
    /**
     * 速度跟踪器
     */
    private VelocityTracker mVelocityTracker;
    /**
     * 尺寸
     */
    private int mWidth, mHalfWidth, mHeight;

    private int mDownX;
    private int mLastX, mLastY;
    private boolean isMoved;

    private OnValueChangedListener mValueChangedListener;

    /**
     * 当前值变化监听器
     */
    public interface OnValueChangedListener{
        void onValueChanged(float value);
    }


    public RingIndicator(Context context) {
        this(context, null);
    }

    public RingIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
		// 放大倍数 1倍 10倍 100倍 1000倍 ...
		scaleValue = 1;
		scaleValue01 = 1/scaleValue;
        // 初始化final常量，必须在构造中赋初值
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        TOUCH_SLOP = viewConfiguration.getScaledTouchSlop();
        MIN_FLING_VELOCITY = viewConfiguration.getScaledMinimumFlingVelocity();
        MAX_FLING_VELOCITY = viewConfiguration.getScaledMaximumFlingVelocity();

        convertValue2Number();
        init(context);
		initString(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
        bgColor = ta.getColor(R.styleable.RulerView_bgColor, Color.parseColor("#00ffffff"));
        keduColor = ta.getColor(R.styleable.RulerView_gradationColor, Color.LTGRAY);
        shortLineWidth = ta.getDimension(R.styleable.RulerView_gv_shortLineWidth, dp2px(1));
        shortKeduLen = ta.getDimension(R.styleable.RulerView_gv_shortGradationLen, dp2px(7));
        longKeduLen = ta.getDimension(R.styleable.RulerView_gv_longGradationLen, shortKeduLen * 2);
        longLineWidth = ta.getDimension(R.styleable.RulerView_gv_longLineWidth, shortLineWidth * 2);
        textColor = ta.getColor(R.styleable.RulerView_textColor, Color.WHITE);
        longKedutextSize = ta.getDimension(R.styleable.RulerView_wq_longGradation_textSize, sp2px(11));
		shortKedutextSize = ta.getDimension(R.styleable.RulerView_wq_shortGradation_textSize, sp2px(8));
       	indicatorLineColor = ta.getColor(R.styleable.RulerView_indicatorLineColor, Color.RED);
        indicatorLineWidth = ta.getDimension(R.styleable.RulerView_indicatorLineWidth, dp2px(2f));
        indicatorLineLen = ta.getDimension(R.styleable.RulerView_gv_indicatorLineLen, dp2px(14f));
        minValue = ta.getFloat(R.styleable.RulerView_gv_minValue, 0f);
        maxValue = ta.getFloat(R.styleable.RulerView_gv_maxValue, 360f);
        currentValue = ta.getFloat(R.styleable.RulerView_gv_currentValue, 0f);
		smallestKeduUnit = ta.getFloat(R.styleable.RulerView_smallestKeDuUnit, 1.0f);// 最小刻度单位
        keduUnit = ta.getFloat(R.styleable.RulerView_gv_gradationUnit, 15.0f);// 绘制刻度单位
        numberPerCount = ta.getInt(R.styleable.RulerView_gv_numberPerCount, 3); // 每绘制刻度单位显示的段数
        kedu_space = ta.getDimension(R.styleable.RulerView_gv_gradationGap, dp2px(30));// 刻度间隔
		kedu_space01 = (float)1/kedu_space;
        keduNumberSpace = ta.getDimension(R.styleable.RulerView_gv_gradationNumberGap, dp2px(2.5f));
        ta.recycle();
		// 短刻度和长刻度文字在Y方向的偏移
		shortKedutextOffsetY = shortKeduLen + keduNumberSpace + shortKedutextSize;
		longKedutextOffsetY = longKeduLen + keduNumberSpace + longKedutextSize;
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(shortLineWidth);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(longKedutextSize);
        mTextPaint.setColor(textColor);

        mScroller = new Scroller(context);
    }
	
    /**
     * 把真实数值转换成绘制数值
     * 为了防止float的精度丢失，把minValue、maxValue、currentValue、gradationUnit都放大10倍
     */
    private void convertValue2Number() {
        mMinNumber = (int) (minValue * scaleValue);
        mMaxNumber = (int) (maxValue * scaleValue);
        mCurrentNumber = (int) (currentValue * scaleValue);
		mCurrentNumber123 = mCurrentNumber;
        mNumberUnit = (int) (keduUnit * scaleValue);
		mNumberUnit01 = (float)1/mNumberUnit;
        mCurrentDistanceToMin = (mCurrentNumber - mMinNumber) * mNumberUnit01 * kedu_space;
		mCurrentDistanceToMin123 = mCurrentDistanceToMin;
        mMinMaxDistance = (mMaxNumber - mMinNumber) * mNumberUnit01 * kedu_space;
        if (mWidth != 0) {
			mSegmentsOfHalfWidth = (int)(mWidth * kedu_space01)/2 + 1;
            // 初始化时，在onMeasure()里计算
            mWidthNumberRange = (int) (mWidth * kedu_space01 * mNumberUnit);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = calculateSize(true, widthMeasureSpec);
        mHeight = calculateSize(false, heightMeasureSpec);
        mHalfWidth = mWidth >> 1;
        if (mWidthNumberRange == 0) {
            mWidthNumberRange = (int) (mWidth * kedu_space01 * mNumberUnit);
        }
		mSegmentsOfHalfWidth = (int)(mHalfWidth * kedu_space01) + 1;
        setMeasuredDimension(mWidth, mHeight);
    }

    /**
     * 计算宽度或高度的真实大小
     *
     * 宽或高为wrap_content时，父控件的测量模式无论是EXACTLY还是AT_MOST，默认给的测量模式都是AT_MOST，测量大小为父控件的size
     * 所以，我们宽度不管，只处理高度，默认80dp
     * @see ViewGroup#getChildMeasureSpec(int, int, int)
     *
     * @param isWidth 是不是宽度
     * @param spec    测量规则
     * @return 真实的大小
     */
    private int calculateSize(boolean isWidth, int spec) {
        final int mode = MeasureSpec.getMode(spec);
        final int size = MeasureSpec.getSize(spec);

        int realSize = size;
        switch (mode) {
				// 精确模式：已经确定具体数值：layout_width为具体值，或match_parent
            case MeasureSpec.EXACTLY:
                break;
				// 最大模式：最大不能超过父控件给的widthSize：layout_width为wrap_content
            case MeasureSpec.AT_MOST:
                if (!isWidth) {
                    int defaultContentSize = dp2px(32);
                    realSize = Math.min(realSize, defaultContentSize);
                }
                break;
				// 未指定尺寸模式：一般父控件是AdapterView
            case MeasureSpec.UNSPECIFIED:
            default:

        }
        //logD("isWidth=%b, mode=%d, size=%d, realSize=%d", isWidth, mode, size, realSize);
        return realSize;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        //logD("onTouchEvent: action=%d", action);
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mDownX = x;
                isMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                final int dx = x - mLastX;

                // 判断是否已经滑动
                if (!isMoved) {
                    final int dy = y - mLastY;
                    // 滑动的触发条件：水平滑动大于垂直滑动；滑动距离大于阈值
                    if (Math.abs(dx) < Math.abs(dy) || Math.abs(x - mDownX) < TOUCH_SLOP) {
                        break;
                    }
                    isMoved = true;
                }

                mCurrentDistanceToMin += -dx;
                calculateValue();
                break;
            case MotionEvent.ACTION_UP:
                // 计算速度：使用1000ms为单位
                mVelocityTracker.computeCurrentVelocity(1000, MAX_FLING_VELOCITY);
                // 获取速度。速度有方向性，水平方向：左滑为负，右滑为正
                int xVelocity = (int) mVelocityTracker.getXVelocity();
                // 达到速度则惯性滑动，否则缓慢滑动到刻度
                if (Math.abs(xVelocity) >= MIN_FLING_VELOCITY) {
                    // 速度具有方向性，需要取反
                    mScroller.fling((int)mCurrentDistanceToMin, 0, -xVelocity, 0,
									0, (int)mMinMaxDistance, 0, 0);
                    invalidate();
                } else {
                    scrollToGradation();
                }
                break;
            default:
                break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    /**
     * 根据distance距离，计算数值
     */
    private void calculateValue() {
		mCurrentDistanceToMin123 = mCurrentDistanceToMin;
		mCurrentNumber123 = mMinNumber + (int)(mCurrentDistanceToMin123 * kedu_space01 * mNumberUnit);
		/// round四舍五入
		/// mCurrentNumber123 = mMinNumber + (int)Math.round(mCurrentDistanceToMin123 * kedu_space01 * mNumberUnit);
		if(mCurrentNumber123 > mMaxNumber){
			mCurrentNumber123 -= mMaxNumber;
			// 重新计算距离(与最小值的距离)
			mCurrentDistanceToMin123 -= mMinMaxDistance;
		}else if(mCurrentNumber123 < mMinNumber){
			mCurrentNumber123 += mMaxNumber;
			// 重新计算距离(与最小值的距离)
			mCurrentDistanceToMin123 += mMinMaxDistance;
		}
        // 限定范围：在最小值与最大值之间
        /// mCurrentDistanceToMin = Math.min(Math.max(mCurrentDistanceToMin, 0), mMinMaxDistance);
		if(mCurrentDistanceToMin > mMinMaxDistance){
			mCurrentDistanceToMin -= mMinMaxDistance;
		}else if(mCurrentDistanceToMin < 0){
			mCurrentDistanceToMin += mMinMaxDistance;
		}
        mCurrentNumber = mMinNumber + (int)(mCurrentDistanceToMin * kedu_space01) * mNumberUnit;
        currentValue = mCurrentNumber123 * scaleValue01;
        // 事件监听
        if (mValueChangedListener != null) {
            mValueChangedListener.onValueChanged(currentValue);
        }
        invalidate();
    }

    /**
     * 四舍五入得到邻近的数字
	 * Thanks wangqi
     */
    private void scrollToGradation() {
		mCurrentDistanceToMin123 = mCurrentDistanceToMin;
		mCurrentNumber123 = mMinNumber + (int)(mCurrentDistanceToMin123 * kedu_space01 * mNumberUnit);
		/// round四舍五入
       	/// mCurrentNumber123 = mMinNumber + (int)Math.round(mCurrentDistanceToMin123 * kedu_space01 * mNumberUnit);
		if(mCurrentNumber123 > mMaxNumber){
			mCurrentNumber123 -= mMaxNumber;
			// 重新计算距离(与最小值的距离)
			mCurrentDistanceToMin123 -= mMinMaxDistance;
		}else if(mCurrentNumber123 < mMinNumber){
			mCurrentNumber123 += mMaxNumber;
			// 重新计算距离(与最小值的距离)
			mCurrentDistanceToMin123 += mMinMaxDistance;
		}
		
		mCurrentNumber = mMinNumber + Math.round(mCurrentDistanceToMin * kedu_space01) * mNumberUnit;
        mCurrentNumber = Math.min(Math.max(mCurrentNumber, mMinNumber), mMaxNumber);
        mCurrentDistanceToMin = (mCurrentNumber - mMinNumber) * mNumberUnit01 * kedu_space;
       	// 当前值
        currentValue = mCurrentNumber123 * scaleValue01;
        // 事件监听
        if (mValueChangedListener != null) {
            mValueChangedListener.onValueChanged(currentValue);
        }
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() != mScroller.getFinalX()) {
                mCurrentDistanceToMin = mScroller.getCurrX();
                calculateValue();
            } else {
                scrollToGradation();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 1 绘制背景色
        canvas.drawColor(bgColor);
        // 2 绘制刻度、数字
        drawGradation(canvas);
        // 3 绘制指针
        drawIndicator(canvas);
    }

    /**
     * 绘制刻度
     */
    private void drawGradation(Canvas canvas) {
        // 1 顶部基准线
        mPaint.setColor(keduColor);
        mPaint.setStrokeWidth(shortLineWidth);
        canvas.drawLine(0, shortLineWidth * .5f, mWidth, 0, mPaint);

		// 偏移距离
		float offsetDist = mCurrentDistanceToMin + (-mCurrentDistanceToMin123);
       	/*
         2 左侧刻度
         2.1 计算左侧开始绘制的刻度
		 */
		int startNum = (int)(((int)mCurrentDistanceToMin - mHalfWidth) * kedu_space01) * mNumberUnit + mMinNumber;
		/// int startNum = ((int)mCurrentDistanceToMin - mHalfWidth) / (int)kedu_space * mNumberUnit + mMinNumber;
        // 扩展2个单位
        final int expendUnit = mNumberUnit << 1;
        // 左侧扩展
        startNum -= expendUnit;
		int tmpNum = startNum; // 0左侧halfWidth空白区域
        if (startNum < mMinNumber) {
			startNum = mMinNumber;
			tmpNum = mMinNumber;
        }
		// 右侧扩展
        /*int rightMaxNum = (startNum + expendUnit) + mWidthNumberRange + expendUnit;
        if (rightMaxNum > mMaxNumber) {
            rightMaxNum = mMaxNumber;
        }*/
		
		int total = (mSegmentsOfHalfWidth<<1)+4;

        // 当前绘制刻度对应控件左侧的位置
        float distance = offsetDist + mHalfWidth - (mCurrentDistanceToMin - (startNum - mMinNumber) * mNumberUnit01 * kedu_space);
		
		float newdistance = distance;
		/*String str = "mHalfWidth = "+mHalfWidth
			+ "\nmCurrentDistanceToMin123 = "+mCurrentDistanceToMin123
			+ "\nmCurrentDistanceToMin = "+mCurrentDistanceToMin
			+ "\nstartNum = "+startNum
			+ "\nmCurrentNumber123 = "+mCurrentNumber123
			+ "\nmCurrentNumber = "+mCurrentNumber
			+ "\noffsetDist = "+offsetDist;*/

		
        // final int perUnitCount = mNumberUnit * numberPerCount;
        int count = 0;
        while (count < total/*startNum <= rightMaxNum*/) {
         
			// 首尾连接
			if(startNum >= mMaxNumber){
				startNum -= mMaxNumber;
			}
			
			// 画刻度线以及文字
			drawLine(canvas,(int)(startNum * scaleValue01),distance);
			// 取模运算的方法绘制刻度线和文字不可取（UI不停地取模运算消耗大）
			/*float fNum = startNum * scaleValue01;
			String text = numToAzimuth((int)fNum);
           	if (startNum % perUnitCount == 0) {
            	// 长刻度
                drawLongLine(canvas,distance,startNum);
            } else {
                // 短刻度
                drawShortLine(canvas,distance,startNum);
            }*/
			
            startNum += mNumberUnit;
            distance += kedu_space;
			
			count++;
        }
		
		if(newdistance > 12){
			while(newdistance > 12){
				newdistance -= kedu_space;
				tmpNum -= mNumberUnit;
				// 首尾连接
				if(tmpNum < mMinNumber){
					tmpNum += mMaxNumber;
				}

				// 补充0坐边mHalfWidth宽空白区域的刻度线以及文字
				drawLine(canvas,(int)(tmpNum * scaleValue01),newdistance);
				// 取模运算绘制刻度线和文字不可取（UI在不停地取模运算消耗大）
				/*float fNum = tmpNum * scaleValue01;
				String text = numToAzimuth((int)fNum);
				if (tmpNum % perUnitCount == 0) {
					// 长刻度
					drawLongLine(canvas,newdistance,text);
				} else {
					// 短刻度
					drawShortLine(canvas,newdistance,text);
				}*/
			}
		}
    }

    /**
     * 绘制指针
     */
    private void drawIndicator(Canvas canvas) {
        mPaint.setColor(indicatorLineColor);
        mPaint.setStrokeWidth(indicatorLineWidth);
        // 圆头画笔
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        // 指示线
        canvas.drawLine(mHalfWidth, 0, mHalfWidth, indicatorLineLen, mPaint);
        // 默认形状画笔
        mPaint.setStrokeCap(Paint.Cap.BUTT);
		mTextPaint.setTextSize(longKedutextSize);
		String text = String.valueOf(mCurrentNumber123);
		final float textWidth = mTextPaint.measureText(text);
		
		float left = mHalfWidth - textWidth * .5f;
		//mPaint.setColor(0xff000000);
		//canvas.drawRect(left,longKedutextOffsetY,left + textWidth,longKedutextOffsetY + textWidth,mPaint);
		
		canvas.drawText(text, left, longKedutextOffsetY, mTextPaint);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    /**
     * 设置新值
     */
    public void setCurrentValue(float currentValue) {
        if (currentValue < minValue || currentValue > maxValue) {
            throw new IllegalArgumentException(String.format("The currentValue of %f is out of range: [%f, %f]",
															 currentValue, minValue, maxValue));
        }
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        this.currentValue = currentValue;
        mCurrentNumber = (int) (this.currentValue * scaleValue);
        final float newDistance = (mCurrentNumber - mMinNumber) * mNumberUnit01 * kedu_space;
        final int dx = (int) (newDistance - mCurrentDistanceToMin);
        // 最大1000ms
        final int duration = dx * 1000 / (int)mMinMaxDistance;
        // 滑动到目标值
        mScroller.startScroll((int) mCurrentDistanceToMin, 0, dx, duration);
        postInvalidate();
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    /**
     * 获取当前值
     */
    public float getCurrentValue() {
        return this.currentValue;
    }

    /**
     * 重新配置参数
     *
     * @param minValue  最小值
     * @param maxValue  最大值
     * @param curValue  当前值
     * @param unit      最小单位所代表的值
     * @param perCount  相邻两条长刻度线之间被分成的隔数量
     */
    public void setValue(float minValue, float maxValue, float curValue, float unit, int perCount) {
        if (minValue > maxValue || curValue < minValue || curValue > maxValue) {
            throw new IllegalArgumentException(String.format("The given values are invalid, check firstly: " +
															 "minValue=%f, maxValue=%f, curValue=%s", minValue, maxValue, curValue));
        }
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = curValue;
        this.keduUnit = unit;
        this.numberPerCount = perCount;
        convertValue2Number();
        if (mValueChangedListener != null) {
            mValueChangedListener.onValueChanged(currentValue);
        }
        postInvalidate();
    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.mValueChangedListener = listener;
    }

	/*
	 * 绘制大刻度线
	 */
	private void drawLongLine(Canvas canvas,float distance,String text){
		mTextPaint.setTextSize(longKedutextSize);
		// 长刻度：刻度宽度为短刻度的2倍
		mPaint.setStrokeWidth(longLineWidth);
		canvas.drawLine(distance, 0, distance, shortKeduLen, mPaint);
		// 文字
		final float textWidth = mTextPaint.measureText(text);
		canvas.drawText(text, distance - textWidth * .5f, shortKedutextOffsetY, mTextPaint);
	}
	
	/*
	 * 绘制小刻度线
	 */
	private void drawShortLine(Canvas canvas,float distance,String text){
		mTextPaint.setTextSize(shortKedutextSize);
		// 短刻度
		mPaint.setStrokeWidth(shortLineWidth);
		canvas.drawLine(distance, 0, distance, shortKeduLen, mPaint);
		// 文字
		final float textWidth = mTextPaint.measureText(text);
		canvas.drawText(text, distance - textWidth * .5f, shortKedutextOffsetY, mTextPaint);
	}
	
	String east = "",northeast="",southeast="";
	String west = "",northwest="",southwest="";
	String south = "",north="";
	
	private void initString(Context context){
		Resources r = context.getResources();
		east = r.getString(R.string.east);
	    northeast = r.getString(R.string.north_east);
		north = r.getString(R.string.north);
		northwest = r.getString(R.string.north_west);
		west = r.getString(R.string.west);
		southwest = r.getString(R.string.south_west);
		south = r.getString(R.string.south);
		southeast = r.getString(R.string.south_east);
	}
	
	/*
	 * 绘制刻度线
	 */
	private void drawLine(Canvas canvas,int n,float distance){
		switch(n){
			case   0: drawLongLine(canvas,distance,east); return;
			case  45: drawLongLine(canvas,distance,northeast); return;
			case  90: drawLongLine(canvas,distance,north); return;
			case 135: drawLongLine(canvas,distance,northwest); return;
			case 180: drawLongLine(canvas,distance,west); return;
			case 225: drawLongLine(canvas,distance,southwest); return;
			case 270: drawLongLine(canvas,distance,south); return;
			case 315: drawLongLine(canvas,distance,southeast); return;
			default: drawShortLine(canvas,distance,String.valueOf(n)); return;
		}
	}
}
