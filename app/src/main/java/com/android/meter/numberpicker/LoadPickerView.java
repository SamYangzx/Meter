package com.android.meter.numberpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.widget.ScrollerCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.android.meter.util.Constant;
import com.android.meter.util.LogUtil;
import com.android.meter.util.StringUtil;

/**
 * Created by Carbs.Wang.
 * email : yeah0126@yeah.net
 * github : https://github.com/Carbs0126/NumberPickerView
 */
public class LoadPickerView extends View {
    private static final String TAG = LogUtil.COMMON_TAG + LoadPickerView.class.getSimpleName();
    private static final float MIN_SPEED = 0.02f;

    // default text color of not selected item
    private static final int DEFAULT_TEXT_COLOR_NORMAL = 0XFF888888;

    // default text color of selected item
    private static final int DEFAULT_TEXT_COLOR_SELECTED = 0XFFFFFFFF;

    // default text size of normal item
    private static final int DEFAULT_TEXT_SIZE_NORMAL_SP = 16;

    // default text size of selected item
    private static final int DEFAULT_TEXT_SIZE_SELECTED_SP = 16;

    // default text size of hint text, the middle item's right text
    private static final int DEFAULT_TEXT_SIZE_HINT_SP = 14;

    // distance between selected text and hint text
    private static final int DEFAULT_MARGIN_START_OF_HINT_DP = 2;

    // distance between hint text and right of this view, used in wrap_content mode
    private static final int DEFAULT_MARGIN_END_OF_HINT_DP = 5;

    // default divider's color
    private static final int DEFAULT_DIVIDER_COLOR = 0XFFAAAAAA;

    // default divider's height
    private static final int DEFAULT_DIVIDER_HEIGHT = 2;

    // default divider's margin to the left & right of this view
    private static final int DEFAULT_DIVIDER_MARGIN_HORIZONTAL = 0;

    // default shown items' count, now we display 5 items, the 2nd one is selected
    private static final int DEFAULT_SHOW_COUNT = 19;

    // default items' horizontal padding, left padding and right padding are both 5dp,
    // only used in wrap_content mode
    private static final int DEFAULT_ITEM_PADDING_DP_H = 5;

    // default items' vertical padding, top padding and bottom padding are both 2dp,
    // only used in wrap_content mode
    private static final int DEFAULT_ITEM_PADDING_DP_V = 2;

    // message's what argument to refresh current state, used by mHandler
    private static final int HANDLER_WHAT_REFRESH = 1;

    // message's what argument to respond value changed event, used by mHandler
    private static final int HANDLER_WHAT_LISTENER_VALUE_CHANGED = 2;

    // message's what argument to request layout, used by mHandlerInMainThread
    private static final int HANDLER_WHAT_REQUEST_LAYOUT = 3;
    private static final int HANDLER_SCROLL_CYCLE = 4;

    // interval time to scroll the distance of one item's height
    private static final int HANDLER_INTERVAL_REFRESH = 32;//millisecond

    // in millisecond unit, default duration of scrolling an item' distance
    private static final int DEFAULT_INTERVAL_REVISE_DURATION = 300;

    // max and min durations when scrolling from one value to another
    private static final int DEFAULT_MIN_SCROLL_BY_INDEX_DURATION = DEFAULT_INTERVAL_REVISE_DURATION * 1;
    private static final int DEFAULT_MAX_SCROLL_BY_INDEX_DURATION = DEFAULT_INTERVAL_REVISE_DURATION * 2;

    private static final String TEXT_ELLIPSIZE_START = "start";
    private static final String TEXT_ELLIPSIZE_MIDDLE = "middle";
    private static final String TEXT_ELLIPSIZE_END = "end";

    private static final boolean DEFAULT_SHOW_DIVIDER = true;
    private static final boolean DEFAULT_WRAP_SELECTOR_WHEEL = true;
    private static final boolean DEFAULT_CURRENT_ITEM_INDEX_EFFECT = false;
    private static final boolean DEFAULT_RESPOND_CHANGE_ON_DETACH = false;
    private static final boolean DEFAULT_RESPOND_CHANGE_IN_MAIN_THREAD = true;

    private static final float NORMAL_PAINT_WIDTH = 3;
    private static final float SELECT_PAINT_WIDTH = 5;

    private int mTextColorNormal = DEFAULT_TEXT_COLOR_NORMAL;
    private int mTextColorSelected = DEFAULT_TEXT_COLOR_SELECTED;
    private int mTextColorHint = DEFAULT_TEXT_COLOR_SELECTED;
    private int mTextSizeNormal = 0;
    private int mTextSizeSelected = 0;
    private int mTextSizeHint = 0;
    private int mWidthOfHintText = 0;
    private int mWidthOfAlterHint = 0;
    private int mMarginStartOfHint = 0;
    private int mMarginEndOfHint = 0;
    private int mItemPaddingVertical = 0;
    private int mItemPaddingHorizontal = 0;
    private int mDividerColor = DEFAULT_DIVIDER_COLOR;
    private int mDividerHeight = DEFAULT_DIVIDER_HEIGHT;
    private int mDividerMarginL = DEFAULT_DIVIDER_MARGIN_HORIZONTAL;
    private int mDividerMarginR = DEFAULT_DIVIDER_MARGIN_HORIZONTAL;
    private int mShowCount = DEFAULT_SHOW_COUNT;
    private int mDividerIndex0 = 0;
    private int mDividerIndex1 = 0;
    private int mMinShowIndex = -1;
    private int mMaxShowIndex = -1;
    //compat for android.widget.NumberPicker
    private int mMinValue = 0;
    //compat for android.widget.NumberPicker
    private int mMaxValue = 0;
    private int mMaxWidthOfDisplayedValues = 0;
    private int mMaxHeightOfDisplayedValues = 0;
    private int mMaxWidthOfAlterArrayWithMeasureHint = 0;
    private int mMaxWidthOfAlterArrayWithoutMeasureHint = 0;
    private int mPrevPickedIndex = 0;
    private int mMiniVelocityFling = 150;
    private int mScaledTouchSlop = 8;
    private String mHintText;
    private String mTextEllipsize;
    private String mEmptyItemHint;
    private String mAlterHint;
    //friction used by scroller when fling
    private float mFriction = 1f;
    private float mTextSizeNormalCenterYOffset = 0f;
    private float mTextSizeSelectedCenterYOffset = 0f;
    private float mTextSizeHintCenterYOffset = 0f;
    //true to show the two dividers
    private boolean mShowDivider = DEFAULT_SHOW_DIVIDER;
    //true to wrap the displayed values
    private boolean mWrapSelectorWheel = DEFAULT_WRAP_SELECTOR_WHEEL;
    //true to set to the current position, false set position to 0
    private boolean mCurrentItemIndexEffect = DEFAULT_CURRENT_ITEM_INDEX_EFFECT;
    //true if NumberPickerView has initialized
    private boolean mHasInit = false;
    // if displayed values' number is less than show count, then this value will be false.
    private boolean mWrapSelectorWheelCheck = true;
    // if you want you set to linear mode from wrap mode when scrolling, then this value will be true.
    private boolean mPendingWrapToLinear = false;

    // if this view is used in same resetDialog or PopupWindow more than once, and there are several
    // NumberPickerViews linked, such as Gregorian Calendar with MonthPicker and DayPicker linked,
    // set mRespondChangeWhenDetach true to respond onValueChanged callbacks if this view is scrolling
    // when detach from window, but this solution is unlovely and may cause NullPointerException
    // (even i haven't found this NullPointerException),
    // so I highly recommend that every time setting up a reusable resetDialog with a NumberPickerView in it,
    // please initialize NumberPickerView's data, and in this way, you can set mRespondChangeWhenDetach false.
    private boolean mRespondChangeOnDetach = DEFAULT_RESPOND_CHANGE_ON_DETACH;

    // this is to set which thread to respond onChange... listeners including
    // OnValueChangeListener, OnValueChangeListenerRelativeToRaw and OnScrollListener when view is
    // scrolling or starts to scroll or stops scrolling.
    private boolean mRespondChangeInMainThread = DEFAULT_RESPOND_CHANGE_IN_MAIN_THREAD;

    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;

    private Paint mPaintDivider = new Paint();
    private Paint mLinePaint = new Paint();
    private TextPaint mPaintText = new TextPaint();
    private Paint mPaintHint = new Paint();

    private String[] mDisplayedValues;
    private CharSequence[] mAlterTextArrayWithMeasureHint;
    private CharSequence[] mAlterTextArrayWithoutMeasureHint;

    private HandlerThread mHandlerThread;
    private Handler mHandlerInNewThread;
    private Handler mHandlerInMainThread;


    // compatible for NumberPicker
    public interface OnValueChangeListener {
        void onValueChange(LoadPickerView picker, int oldVal, int newVal);
    }

    public interface OnValueChangeListenerRelativeToRaw {
        void onValueChangeRelativeToRaw(LoadPickerView picker, int oldPickedIndex, int newPickedIndex,
                                        String[] displayedValues);
    }

    public interface OnValueChangeListenerInScrolling {
        void onValueChangeInScrolling(LoadPickerView picker, int oldVal, int newVal);
    }

    // compatible for NumberPicker
    public interface OnScrollListener {
        int SCROLL_STATE_IDLE = 0;
        int SCROLL_STATE_TOUCH_SCROLL = 1;
        int SCROLL_STATE_FLING = 2;
        int SCROLL_STATE_CYCLE = 3;

        void onScrollStateChange(LoadPickerView view, int scrollState);

        //This is a custom listener, the listener will be invokded when touch_scroll or fling.
        void onScrollFling(LoadPickerView view, float speedRatio);
    }

    private OnValueChangeListenerRelativeToRaw mOnValueChangeListenerRaw;
    private OnValueChangeListener mOnValueChangeListener; //compatible for NumberPicker
    private OnScrollListener mOnScrollListener;//compatible for NumberPicker
    private OnValueChangeListenerInScrolling mOnValueChangeListenerInScrolling;//response onValueChanged in scrolling

    // The current scroll state of the NumberPickerView.
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    public LoadPickerView(Context context) {
        super(context);
        init(context);
    }

    public LoadPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        init(context);
    }

    public LoadPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init(context);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_ShowCount) {
                mShowCount = a.getInt(attr, DEFAULT_SHOW_COUNT);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_DividerColor) {
                mDividerColor = a.getColor(attr, DEFAULT_DIVIDER_COLOR);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_DividerHeight) {
                mDividerHeight = a.getDimensionPixelSize(attr, DEFAULT_DIVIDER_HEIGHT);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_DividerMarginLeft) {
                mDividerMarginL = a.getDimensionPixelSize(attr, DEFAULT_DIVIDER_MARGIN_HORIZONTAL);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_DividerMarginRight) {
                mDividerMarginR = a.getDimensionPixelSize(attr, DEFAULT_DIVIDER_MARGIN_HORIZONTAL);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_TextArray) {
                mDisplayedValues = convertCharSequenceArrayToStringArray(a.getTextArray(attr));
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_TextColorNormal) {
                mTextColorNormal = a.getColor(attr, DEFAULT_TEXT_COLOR_NORMAL);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_TextColorSelected) {
                mTextColorSelected = a.getColor(attr, DEFAULT_TEXT_COLOR_SELECTED);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_TextColorHint) {
                mTextColorHint = a.getColor(attr, DEFAULT_TEXT_COLOR_SELECTED);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_TextSizeNormal) {
                mTextSizeNormal = a.getDimensionPixelSize(attr, sp2px(context, DEFAULT_TEXT_SIZE_NORMAL_SP));
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_TextSizeSelected) {
                mTextSizeSelected = a.getDimensionPixelSize(attr, sp2px(context, DEFAULT_TEXT_SIZE_SELECTED_SP));
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_TextSizeHint) {
                mTextSizeHint = a.getDimensionPixelSize(attr, sp2px(context, DEFAULT_TEXT_SIZE_HINT_SP));
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_MinValue) {
                mMinShowIndex = a.getInteger(attr, 0);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_MaxValue) {
                mMaxShowIndex = a.getInteger(attr, 0);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_WrapSelectorWheel) {
                mWrapSelectorWheel = a.getBoolean(attr, DEFAULT_WRAP_SELECTOR_WHEEL);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_ShowDivider) {
                mShowDivider = a.getBoolean(attr, DEFAULT_SHOW_DIVIDER);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_HintText) {
                mHintText = a.getString(attr);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_AlternativeHint) {
                mAlterHint = a.getString(attr);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_EmptyItemHint) {
                mEmptyItemHint = a.getString(attr);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_MarginStartOfHint) {
                mMarginStartOfHint = a.getDimensionPixelSize(attr, dp2px(context, DEFAULT_MARGIN_START_OF_HINT_DP));
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_MarginEndOfHint) {
                mMarginEndOfHint = a.getDimensionPixelSize(attr, dp2px(context, DEFAULT_MARGIN_END_OF_HINT_DP));
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_ItemPaddingVertical) {
                mItemPaddingVertical = a.getDimensionPixelSize(attr, dp2px(context, DEFAULT_ITEM_PADDING_DP_V));
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_ItemPaddingHorizontal) {
                mItemPaddingHorizontal = a.getDimensionPixelSize(attr, dp2px(context, DEFAULT_ITEM_PADDING_DP_H));
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_AlternativeTextArrayWithMeasureHint) {
                mAlterTextArrayWithMeasureHint = a.getTextArray(attr);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_AlternativeTextArrayWithoutMeasureHint) {
                mAlterTextArrayWithoutMeasureHint = a.getTextArray(attr);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_RespondChangeOnDetached) {
                mRespondChangeOnDetach = a.getBoolean(attr, DEFAULT_RESPOND_CHANGE_ON_DETACH);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_RespondChangeInMainThread) {
                mRespondChangeInMainThread = a.getBoolean(attr, DEFAULT_RESPOND_CHANGE_IN_MAIN_THREAD);
            } else if (attr == cn.carbswang.android.numberpickerview.library.R.styleable.NumberPickerView_npv_TextEllipsize) {
                mTextEllipsize = a.getString(attr);
            }
        }
        a.recycle();
    }

    private void init(Context context) {
        mScroller = ScrollerCompat.create(context);
        mMiniVelocityFling = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        if (mTextSizeNormal == 0) mTextSizeNormal = sp2px(context, DEFAULT_TEXT_SIZE_NORMAL_SP);
        if (mTextSizeSelected == 0)
            mTextSizeSelected = sp2px(context, DEFAULT_TEXT_SIZE_SELECTED_SP);
        if (mTextSizeHint == 0) mTextSizeHint = sp2px(context, DEFAULT_TEXT_SIZE_HINT_SP);
        if (mMarginStartOfHint == 0)
            mMarginStartOfHint = dp2px(context, DEFAULT_MARGIN_START_OF_HINT_DP);
        if (mMarginEndOfHint == 0) mMarginEndOfHint = dp2px(context, DEFAULT_MARGIN_END_OF_HINT_DP);

        mPaintDivider.setColor(mDividerColor);
        mLinePaint.setColor(DEFAULT_TEXT_COLOR_NORMAL);
        mPaintDivider.setAntiAlias(true);
        mPaintDivider.setStyle(Paint.Style.STROKE);
        mPaintDivider.setStrokeWidth(mDividerHeight);

        mPaintText.setColor(mTextColorNormal);
        mPaintText.setAntiAlias(true);
        mPaintText.setTextAlign(Align.CENTER);

        mPaintHint.setColor(mTextColorHint);
        mPaintHint.setAntiAlias(true);
        mPaintHint.setTextAlign(Align.CENTER);
        mPaintHint.setTextSize(mTextSizeHint);

        if (mShowCount % 2 == 0) {
            mShowCount++;
        }
        if (mMinShowIndex == -1 || mMaxShowIndex == -1) {
            updateValueForInit();
        }
        initHandler();
    }

    private void initHandler() {
        mHandlerThread = new HandlerThread("HandlerThread-For-Refreshing");
        mHandlerThread.start();

        mHandlerInNewThread = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                printD("msg: " + msg.what + ", mScroller.isFinished: " + mScroller.isFinished() + ", mScrollState: " + mScrollState + ", currentV: " + mScroller.getCurrVelocity());

                switch (msg.what) {
                    case HANDLER_WHAT_REFRESH:
                        if (!mScroller.isFinished()) {
                            if (mOnScrollListener != null) {
                                mOnScrollListener.onScrollFling(LoadPickerView.this, calculateSpeedRatio(OnScrollListener.SCROLL_STATE_FLING));
                            }
                            if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                                onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                            }
                            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH, 0, 0, msg.obj), HANDLER_INTERVAL_REFRESH);
                        } else {
                            if (mOnScrollListener != null) {
                                mOnScrollListener.onScrollFling(LoadPickerView.this, 0);
                            }
                            int duration = 0;
                            int willPickIndex;
                            //if scroller finished(not scrolling), then adjust the position

//                            if (mCurrDrawFirstItemY != 0) {//need to adjust
//                                if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
//                                    onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
//                                }
//                                if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
//                                    //adjust to scroll upward
//                                    duration = (int) ((float) DEFAULT_INTERVAL_REVISE_DURATION * (mItemHeight + mCurrDrawFirstItemY) / mItemHeight);
//                                    mScroller.startScroll(0, mCurrDrawGlobalY, 0, mItemHeight + mCurrDrawFirstItemY, duration * 3);
//                                    willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY);
//                                } else {
//                                    //adjust to scroll downward
//                                    duration = (int) ((float) DEFAULT_INTERVAL_REVISE_DURATION * (-mCurrDrawFirstItemY) / mItemHeight);
//                                    mScroller.startScroll(0, mCurrDrawGlobalY, 0, mCurrDrawFirstItemY, duration * 3);
//                                    willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mCurrDrawFirstItemY);
//                                }
//                                postInvalidate();
//                            } else {
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                            //get the index which will be selected
                            willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY);
//                            }

                            Message changeMsg = getMsg(HANDLER_WHAT_LISTENER_VALUE_CHANGED, mPrevPickedIndex, willPickIndex, msg.obj);
                            if (mRespondChangeInMainThread) {
                                mHandlerInMainThread.sendMessageDelayed(changeMsg, duration * 2);
                            } else {
                                mHandlerInNewThread.sendMessageDelayed(changeMsg, duration * 2);
                            }
                        }
                        break;
                    case HANDLER_WHAT_LISTENER_VALUE_CHANGED:
                        respondPickedValueChanged(msg.arg1, msg.arg2, msg.obj);
                        break;
                    case HANDLER_SCROLL_CYCLE:
                        mCycleCount++;
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_CYCLE);
                        calculateFirstItemParameterByGlobalY();
                        postInvalidate();
//                        mOnScrollListener.onScrollFling(LoadPickerView.this, calculateSpeedRatio(OnScrollListener.SCROLL_STATE_CYCLE));
                        this.sendEmptyMessageDelayed(HANDLER_SCROLL_CYCLE, 50);
                        break;
                }
            }
        };
        mHandlerInMainThread = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLER_WHAT_REQUEST_LAYOUT:
                        requestLayout();
                        break;
                    case HANDLER_WHAT_LISTENER_VALUE_CHANGED:
                        respondPickedValueChanged(msg.arg1, msg.arg2, msg.obj);
                        break;
                }
            }
        };
    }

    private int mInScrollingPickedOldValue;
    private int mInScrollingPickedNewValue;

    private void respondPickedValueChangedInScrolling(int oldVal, int newVal) {
        mOnValueChangeListenerInScrolling.onValueChangeInScrolling(this, oldVal, newVal);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        updateMaxWHOfDisplayedValues(false);
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        mItemHeight = mViewHeight / mShowCount;
//        mItemHeight = mItemHeight / 3; //高度变为越来1/3;
        mViewCenterX = ((float) (mViewWidth + getPaddingLeft() - getPaddingRight())) / 2;
        mQuarterX = mViewCenterX / 2;

        int defaultValue = 0;
        if (getOneRecycleSize() > 1) {
            if (mHasInit) {
                defaultValue = getValue() - mMinValue;
            } else if (mCurrentItemIndexEffect) {
                defaultValue = mCurrDrawFirstItemIndex + (mShowCount - 1) / 2;
            } else {
                defaultValue = 0;
            }
        }
        correctPositionByDefaultValue(defaultValue, mWrapSelectorWheel && mWrapSelectorWheelCheck);
        updateFontAttr();
        updateNotWrapYLimit();
        updateDividerAttr();
        mHasInit = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mHandlerThread == null || !mHandlerThread.isAlive()) {
            initHandler();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandlerThread.quit();
        //These codes are for resetDialog or PopupWindow which will be used for more than once.
        //Not an elegant solution, if you have any good idea, please let me know, thank you.
        if (mItemHeight == 0) return;
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            mCurrDrawGlobalY = mScroller.getCurrY();
            calculateFirstItemParameterByGlobalY();
            if (mCurrDrawFirstItemY != 0) {
                if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
                    mCurrDrawGlobalY = mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY;
                } else {
                    mCurrDrawGlobalY = mCurrDrawGlobalY + mCurrDrawFirstItemY;
                }
                calculateFirstItemParameterByGlobalY();
            }
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        }
        // see the comments on mRespondChangeOnDetach, if mRespondChangeOnDetach is false,
        // please initialize NumberPickerView's data every time setting up NumberPickerView,
        // set the demo of GregorianLunarCalendar
        int currPickedIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY);
        if (currPickedIndex != mPrevPickedIndex && mRespondChangeOnDetach) {
            try {
                if (mOnValueChangeListener != null) {
                    mOnValueChangeListener.onValueChange(LoadPickerView.this, mPrevPickedIndex + mMinValue, currPickedIndex + mMinValue);
                }
                if (mOnValueChangeListenerRaw != null) {
                    mOnValueChangeListenerRaw.onValueChangeRelativeToRaw(LoadPickerView.this, mPrevPickedIndex, currPickedIndex, mDisplayedValues);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mPrevPickedIndex = currPickedIndex;
    }

    public int getOneRecycleSize() {
        return mMaxShowIndex - mMinShowIndex + 1;
    }

    public int getRawContentSize() {
        if (mDisplayedValues != null)
            return mDisplayedValues.length;
        return 0;
    }

    public void setDisplayedValuesAndPickedIndex(String[] newDisplayedValues, int pickedIndex, boolean needRefresh) {
        stopScrolling();
        if (newDisplayedValues == null) {
            throw new IllegalArgumentException("newDisplayedValues should not be null.");
        }
        if (pickedIndex < 0) {
            throw new IllegalArgumentException("pickedIndex should not be negative, now pickedIndex is " + pickedIndex);
        }
        updateContent(newDisplayedValues);
        updateMaxWHOfDisplayedValues(true);
        updateNotWrapYLimit();
        updateValue();
        mPrevPickedIndex = pickedIndex + mMinShowIndex;
        correctPositionByDefaultValue(pickedIndex, mWrapSelectorWheel && mWrapSelectorWheelCheck);
        if (needRefresh) {
            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), 0);
            postInvalidate();
        }
    }

    public void setDisplayedValues(String[] newDisplayedValues, boolean needRefresh) {
        setDisplayedValuesAndPickedIndex(newDisplayedValues, 0, needRefresh);
    }

    public void setDisplayedValues(String[] newDisplayedValues) {
        stopRefreshing();
        stopScrolling();
        if (newDisplayedValues == null) {
            throw new IllegalArgumentException("newDisplayedValues should not be null.");
        }

        if (mMaxValue - mMinValue + 1 > newDisplayedValues.length) {
            throw new IllegalArgumentException("mMaxValue - mMinValue + 1 should not be greater than mDisplayedValues.length, now "
                    + "((mMaxValue - mMinValue + 1) is " + (mMaxValue - mMinValue + 1)
                    + " newDisplayedValues.length is " + newDisplayedValues.length
                    + ", you need to set MaxValue and MinValue before setDisplayedValues(String[])");
        }
        updateContent(newDisplayedValues);
        updateMaxWHOfDisplayedValues(true);
        mPrevPickedIndex = 0 + mMinShowIndex;
        correctPositionByDefaultValue(0, mWrapSelectorWheel && mWrapSelectorWheelCheck);
        postInvalidate();
        mHandlerInMainThread.sendEmptyMessage(HANDLER_WHAT_REQUEST_LAYOUT);
    }

    /**
     * Gets the values to be displayed instead of string values.
     *
     * @return The displayed values.
     */
    public String[] getDisplayedValues() {
        return mDisplayedValues;
    }

    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        if (mWrapSelectorWheel != wrapSelectorWheel) {
            if (!wrapSelectorWheel) {
                if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    internalSetWrapToLinear();
                } else {
                    mPendingWrapToLinear = true;
                }
            } else {
                mWrapSelectorWheel = wrapSelectorWheel;
                updateWrapStateByContent();
                postInvalidate();
            }
        }
    }

    /**
     * get the "fromValue" by using getValue(), if your picker's minValue is not 0,
     * make sure you can get the accurate value by getValue(), or you can use
     * smoothScrollToValue(int fromValue, int toValue, boolean needRespond)
     *
     * @param toValue the value you want picker to scroll to
     */
    public void smoothScrollToValue(int toValue) {
        smoothScrollToValue(getValue(), toValue, true);
    }

    /**
     * get the "fromValue" by using getValue(), if your picker's minValue is not 0,
     * make sure you can get the accurate value by getValue(), or you can use
     * smoothScrollToValue(int fromValue, int toValue, boolean needRespond)
     *
     * @param toValue     the value you want picker to scroll to
     * @param needRespond set if you want picker to respond onValueChange listener
     */
    public void smoothScrollToValue(int toValue, boolean needRespond) {
        smoothScrollToValue(getValue(), toValue, needRespond);
    }

    public void smoothScrollToValue(int fromValue, int toValue) {
        smoothScrollToValue(fromValue, toValue, true);
    }

    /**
     * @param fromValue   need to set the fromValue, can be greater than mMaxValue or less than mMinValue
     * @param toValue     the value you want picker to scroll to
     * @param needRespond need Respond to the ValueChange callback When Scrolling, default is false
     */
    public void smoothScrollToValue(int fromValue, int toValue, boolean needRespond) {
        int deltaIndex;
        fromValue = refineValueByLimit(fromValue, mMinValue, mMaxValue,
                mWrapSelectorWheel && mWrapSelectorWheelCheck);
        toValue = refineValueByLimit(toValue, mMinValue, mMaxValue,
                mWrapSelectorWheel && mWrapSelectorWheelCheck);
        if (mWrapSelectorWheel && mWrapSelectorWheelCheck) {
            deltaIndex = toValue - fromValue;
            int halfOneRecycleSize = getOneRecycleSize() / 2;
            if (deltaIndex < -halfOneRecycleSize || halfOneRecycleSize < deltaIndex) {
                deltaIndex = deltaIndex > 0 ? deltaIndex - getOneRecycleSize() : deltaIndex + getOneRecycleSize();
            }
        } else {
            deltaIndex = toValue - fromValue;
        }
        setValue(fromValue);
        if (fromValue == toValue) return;
        scrollByIndexSmoothly(deltaIndex, needRespond);
    }

    /**
     * simplify the "setDisplayedValue() + setMinValue() + setMaxValue()" process,
     * default minValue is 0, and make sure you do NOT change the minValue.
     *
     * @param display new values to be displayed
     */
    public void refreshByNewDisplayedValues(String[] display) {
        int minValue = getMinValue();

        int oldMaxValue = getMaxValue();
        int oldSpan = oldMaxValue - minValue + 1;

        int newMaxValue = display.length - 1;
        int newSpan = newMaxValue - minValue + 1;

        if (newSpan > oldSpan) {
            setDisplayedValues(display);
            setMaxValue(newMaxValue);
        } else {
            setMaxValue(newMaxValue);
            setDisplayedValues(display);
        }
    }

    /**
     * used by handlers to respond onchange callbacks
     *
     * @param oldVal        prevPicked value
     * @param newVal        currPicked value
     * @param respondChange if want to respond onchange callbacks
     */
    private void respondPickedValueChanged(int oldVal, int newVal, Object respondChange) {
        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        if (oldVal != newVal) {
            if (respondChange == null || !(respondChange instanceof Boolean) || (Boolean) respondChange) {
                if (mOnValueChangeListener != null) {
                    mOnValueChangeListener.onValueChange(LoadPickerView.this, oldVal + mMinValue, newVal + mMinValue);
                }
                if (mOnValueChangeListenerRaw != null) {
                    mOnValueChangeListenerRaw.onValueChangeRelativeToRaw(LoadPickerView.this, oldVal, newVal, mDisplayedValues);
                }
            }
        }
        mPrevPickedIndex = newVal;
        if (mPendingWrapToLinear) {
            mPendingWrapToLinear = false;
            internalSetWrapToLinear();
        }
    }

    private void scrollByIndexSmoothly(int deltaIndex) {
        scrollByIndexSmoothly(deltaIndex, true);
    }

    /**
     * @param deltaIndex  the delta index it will scroll by
     * @param needRespond need Respond to the ValueChange callback When Scrolling, default is false
     */
    private void scrollByIndexSmoothly(int deltaIndex, boolean needRespond) {
        if (!(mWrapSelectorWheel && mWrapSelectorWheelCheck)) {
            int willPickRawIndex = getPickedIndexRelativeToRaw();
            if (willPickRawIndex + deltaIndex > mMaxShowIndex) {
                deltaIndex = mMaxShowIndex - willPickRawIndex;
            } else if (willPickRawIndex + deltaIndex < mMinShowIndex) {
                deltaIndex = mMinShowIndex - willPickRawIndex;
            }
        }
        int duration;
        int dy;
        if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
            //scroll upwards for a distance of less than mItemHeight
            dy = mItemHeight + mCurrDrawFirstItemY;
            duration = (int) ((float) DEFAULT_INTERVAL_REVISE_DURATION * (mItemHeight + mCurrDrawFirstItemY) / mItemHeight);
            if (deltaIndex < 0) {
                duration = -duration - deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION;
            } else {
                duration = duration + deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION;
            }
        } else {
            //scroll downwards for a distance of less than mItemHeight
            dy = mCurrDrawFirstItemY;
            duration = (int) ((float) DEFAULT_INTERVAL_REVISE_DURATION * (-mCurrDrawFirstItemY) / mItemHeight);
            if (deltaIndex < 0) {
                duration = duration - deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION;
            } else {
                duration = duration + deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION;
            }
        }
        dy = dy + deltaIndex * mItemHeight;
        if (duration < DEFAULT_MIN_SCROLL_BY_INDEX_DURATION)
            duration = DEFAULT_MIN_SCROLL_BY_INDEX_DURATION;
        if (duration > DEFAULT_MAX_SCROLL_BY_INDEX_DURATION)
            duration = DEFAULT_MAX_SCROLL_BY_INDEX_DURATION;
        mScroller.startScroll(0, mCurrDrawGlobalY, 0, dy, duration);
        if (needRespond) {
            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), duration / 4);
        } else {
            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH, 0, 0, new Boolean(needRespond)), duration / 4);
        }
        postInvalidate();
    }

    public int getMinValue() {
        return mMinValue;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
        mMinShowIndex = 0;
        updateNotWrapYLimit();
    }

    //compatible for android.widget.NumberPicker
    public void setMaxValue(int maxValue) {
        if (mDisplayedValues == null) {
            throw new NullPointerException("mDisplayedValues should not be null");
        }
        if (maxValue - mMinValue + 1 > mDisplayedValues.length) {
            throw new IllegalArgumentException("(maxValue - mMinValue + 1) should not be greater than mDisplayedValues.length now " +
                    " (maxValue - mMinValue + 1) is " + (maxValue - mMinValue + 1) + " and mDisplayedValues.length is " + mDisplayedValues.length);
        }
        mMaxValue = maxValue;
        mMaxShowIndex = mMaxValue - mMinValue + mMinShowIndex;
        setMinAndMaxShowIndex(mMinShowIndex, mMaxShowIndex);
        updateNotWrapYLimit();
    }

    //compatible for android.widget.NumberPicker
    public void setValue(int value) {
        if (value < mMinValue) {
            throw new IllegalArgumentException("should not set a value less than mMinValue, value is " + value);
        }
        if (value > mMaxValue) {
            throw new IllegalArgumentException("should not set a value greater than mMaxValue, value is " + value);
        }
        setPickedIndexRelativeToRaw(value - mMinValue);
    }

    //compatible for android.widget.NumberPicker
    public int getValue() {
        return getPickedIndexRelativeToRaw() + mMinValue;
    }

    public String getContentByCurrValue() {
        return mDisplayedValues[getValue() - mMinValue];
    }

    public boolean getWrapSelectorWheel() {
        return mWrapSelectorWheel;
    }

    public boolean getWrapSelectorWheelAbsolutely() {
        return mWrapSelectorWheel && mWrapSelectorWheelCheck;
    }

    public void setHintText(String hintText) {
        if (isStringEqual(mHintText, hintText)) return;
        mHintText = hintText;
        mTextSizeHintCenterYOffset = getTextCenterYOffset(mPaintHint.getFontMetrics());
        mWidthOfHintText = getTextWidth(mHintText, mPaintHint);
        mHandlerInMainThread.sendEmptyMessage(HANDLER_WHAT_REQUEST_LAYOUT);
    }

    public void setPickedIndexRelativeToMin(int pickedIndexToMin) {
        if (0 <= pickedIndexToMin && pickedIndexToMin < getOneRecycleSize()) {
            mPrevPickedIndex = pickedIndexToMin + mMinShowIndex;
            correctPositionByDefaultValue(pickedIndexToMin, mWrapSelectorWheel && mWrapSelectorWheelCheck);
            postInvalidate();
        }
    }

    public void setNormalTextColor(int normalTextColor) {
        if (mTextColorNormal == normalTextColor) return;
        mTextColorNormal = normalTextColor;
        postInvalidate();
    }

    public void setSelectedTextColor(int selectedTextColor) {
        if (mTextColorSelected == selectedTextColor) return;
        mTextColorSelected = selectedTextColor;
        postInvalidate();
    }

    public void setHintTextColor(int hintTextColor) {
        if (mTextColorHint == hintTextColor) return;
        mTextColorHint = hintTextColor;
        mPaintHint.setColor(mTextColorHint);
        postInvalidate();
    }

    public void setDividerColor(int dividerColor) {
        if (mDividerColor == dividerColor) return;
        mDividerColor = dividerColor;
        mPaintDivider.setColor(mDividerColor);
        postInvalidate();
    }

    public void setPickedIndexRelativeToRaw(int pickedIndexToRaw) {
        if (mMinShowIndex > -1) {
            if (mMinShowIndex <= pickedIndexToRaw && pickedIndexToRaw <= mMaxShowIndex) {
                mPrevPickedIndex = pickedIndexToRaw;
                correctPositionByDefaultValue(pickedIndexToRaw - mMinShowIndex, mWrapSelectorWheel && mWrapSelectorWheelCheck);
                postInvalidate();
            }
        }
    }

    public int getPickedIndexRelativeToRaw() {
        int willPickIndex;
        if (mCurrDrawFirstItemY != 0) {
            if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
                willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY);
            } else {
                willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mCurrDrawFirstItemY);
            }
        } else {
            willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY);
        }
        return willPickIndex;
    }

    public void setMinAndMaxShowIndex(int minShowIndex, int maxShowIndex) {
        setMinAndMaxShowIndex(minShowIndex, maxShowIndex, true);
    }

    public void setMinAndMaxShowIndex(int minShowIndex, int maxShowIndex, boolean needRefresh) {
        if (minShowIndex > maxShowIndex) {
            throw new IllegalArgumentException("minShowIndex should be less than maxShowIndex, minShowIndex is "
                    + minShowIndex + ", maxShowIndex is " + maxShowIndex + ".");
        }
        if (mDisplayedValues == null) {
            throw new IllegalArgumentException("mDisplayedValues should not be null, you need to set mDisplayedValues first.");
        } else {
            if (minShowIndex < 0) {
                throw new IllegalArgumentException("minShowIndex should not be less than 0, now minShowIndex is " + minShowIndex);
            } else if (minShowIndex > mDisplayedValues.length - 1) {
                throw new IllegalArgumentException("minShowIndex should not be greater than (mDisplayedValues.length - 1), now " +
                        "(mDisplayedValues.length - 1) is " + (mDisplayedValues.length - 1) + " minShowIndex is " + minShowIndex);
            }

            if (maxShowIndex < 0) {
                throw new IllegalArgumentException("maxShowIndex should not be less than 0, now maxShowIndex is " + maxShowIndex);
            } else if (maxShowIndex > mDisplayedValues.length - 1) {
                throw new IllegalArgumentException("maxShowIndex should not be greater than (mDisplayedValues.length - 1), now " +
                        "(mDisplayedValues.length - 1) is " + (mDisplayedValues.length - 1) + " maxShowIndex is " + maxShowIndex);
            }
        }
        mMinShowIndex = minShowIndex;
        mMaxShowIndex = maxShowIndex;
        if (needRefresh) {
            mPrevPickedIndex = 0 + mMinShowIndex;
            correctPositionByDefaultValue(0, mWrapSelectorWheel && mWrapSelectorWheelCheck);
            postInvalidate();
        }
    }

    /**
     * set the friction of scroller, it will effect the scroller's acceleration when fling
     *
     * @param friction default is ViewConfiguration.get(mContext).getScrollFriction()
     *                 if setFriction(2 * ViewConfiguration.get(mContext).getScrollFriction()),
     *                 the friction will be twice as much as before
     */
    public void setFriction(float friction) {
        if (friction <= 0)
            throw new IllegalArgumentException("you should set a a positive float friction, now friction is " + friction);
        mFriction = ViewConfiguration.get(getContext()).getScrollFriction() / friction;
    }

    //compatible for NumberPicker
    private void onScrollStateChange(int scrollState) {
        printD("mScrollState: " + mScrollState + ", scrollState: " + scrollState);
        if (mScrollState == scrollState) {
            return;
        }
        mScrollState = scrollState;
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChange(this, scrollState);
        }
    }

    //compatible for NumberPicker
    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    //compatible for NumberPicker
    public void setOnValueChangedListener(OnValueChangeListener listener) {
        mOnValueChangeListener = listener;
    }

    public void setOnValueChangedListenerRelativeToRaw(OnValueChangeListenerRelativeToRaw listener) {
        mOnValueChangeListenerRaw = listener;
    }

    public void setOnValueChangeListenerInScrolling(OnValueChangeListenerInScrolling listener) {
        mOnValueChangeListenerInScrolling = listener;
    }

    public void setContentTextTypeface(Typeface typeface) {
        mPaintText.setTypeface(typeface);
    }

    public void setHintTextTypeface(Typeface typeface) {
        mPaintHint.setTypeface(typeface);
    }

    //return index relative to mDisplayedValues from 0.
    private int getWillPickIndexByGlobalY(int globalY) {
        if (mItemHeight == 0) return 0;
        int willPickIndex = globalY / mItemHeight + mShowCount / 2;
        int index = getIndexByRawIndex(willPickIndex, getOneRecycleSize(), mWrapSelectorWheel && mWrapSelectorWheelCheck);
        if (0 <= index && index < getOneRecycleSize()) {
            return index + mMinShowIndex;
        } else {
            throw new IllegalArgumentException("getWillPickIndexByGlobalY illegal index : " + index
                    + " getOneRecycleSize() : " + getOneRecycleSize() + " mWrapSelectorWheel : " + mWrapSelectorWheel);
        }
    }

    private int getIndexByRawIndex(int index, int size, boolean wrap) {
        if (size <= 0) return 0;
        if (wrap) {
            index = index % size;
            if (index < 0) {
                index = index + size;
            }
            return index;
        } else {
            return index;
        }
    }

    private void internalSetWrapToLinear() {
        int rawIndex = getPickedIndexRelativeToRaw();
        correctPositionByDefaultValue(rawIndex - mMinShowIndex, false);
        mWrapSelectorWheel = false;
        postInvalidate();
    }

    private void updateDividerAttr() {
        mDividerIndex0 = mShowCount / 2;
        mDividerIndex1 = mDividerIndex0 + 1;
        dividerY0 = mDividerIndex0 * mViewHeight / mShowCount;
        dividerY1 = mDividerIndex1 * mViewHeight / mShowCount;
        if (mDividerMarginL < 0) mDividerMarginL = 0;
        if (mDividerMarginR < 0) mDividerMarginR = 0;

        if (mDividerMarginL + mDividerMarginR == 0) return;
        if (getPaddingLeft() + mDividerMarginL >= mViewWidth - getPaddingRight() - mDividerMarginR) {
            int surplusMargin = getPaddingLeft() + mDividerMarginL + getPaddingRight() + mDividerMarginR - mViewWidth;
            mDividerMarginL = (int) (mDividerMarginL - (float) surplusMargin * mDividerMarginL / (mDividerMarginL + mDividerMarginR));
            mDividerMarginR = (int) (mDividerMarginR - (float) surplusMargin * mDividerMarginR / (mDividerMarginL + mDividerMarginR));
        }
    }

    private int mNotWrapLimitYTop;
    private int mNotWrapLimitYBottom;

    private void updateFontAttr() {
        if (mTextSizeNormal > mItemHeight) mTextSizeNormal = mItemHeight;
        if (mTextSizeSelected > mItemHeight) mTextSizeSelected = mItemHeight;

        if (mPaintHint == null) {
            throw new IllegalArgumentException("mPaintHint should not be null.");
        }
        mPaintHint.setTextSize(mTextSizeHint);
        mTextSizeHintCenterYOffset = getTextCenterYOffset(mPaintHint.getFontMetrics());
        mWidthOfHintText = getTextWidth(mHintText, mPaintHint);

        if (mPaintText == null) {
            throw new IllegalArgumentException("mPaintText should not be null.");
        }
        mPaintText.setTextSize(mTextSizeSelected);
        mTextSizeSelectedCenterYOffset = getTextCenterYOffset(mPaintText.getFontMetrics());
        mPaintText.setTextSize(mTextSizeNormal);
        mTextSizeNormalCenterYOffset = getTextCenterYOffset(mPaintText.getFontMetrics());
    }

    private void updateNotWrapYLimit() {
        mNotWrapLimitYTop = 0;
        mNotWrapLimitYBottom = -mShowCount * mItemHeight;
        if (mDisplayedValues != null) {
            mNotWrapLimitYTop = (getOneRecycleSize() - mShowCount / 2 - 1) * mItemHeight;
            mNotWrapLimitYBottom = -(mShowCount / 2) * mItemHeight;
        }
    }

    private float downYGlobal = 0;
    private float downY = 0;
    private float currY = 0;

    private int limitY(int currDrawGlobalYPreferred) {
        printD("currDrawGlobalYPreferred: " + currDrawGlobalYPreferred + ", mWrapSelectorWheel: " +
                mWrapSelectorWheel + " ,mWrapSelectorWheelCheck: " + mWrapSelectorWheelCheck + " ,mNotWrapLimitYBottom: " + mNotWrapLimitYBottom + " , mNotWrapLimitYTop: " + mNotWrapLimitYTop);

        if (mWrapSelectorWheel && mWrapSelectorWheelCheck) return currDrawGlobalYPreferred;
        if (currDrawGlobalYPreferred < mNotWrapLimitYBottom) {
            currDrawGlobalYPreferred = mNotWrapLimitYBottom;
        } else if (currDrawGlobalYPreferred > mNotWrapLimitYTop) {
            currDrawGlobalYPreferred = mNotWrapLimitYTop;
        }
        return currDrawGlobalYPreferred;
    }

    private static final int LIMIT_HEIGHT = 5;

    private float limitLinesY(float relativeY) {
        if (relativeY < -mViewHeight / 2) {
            relativeY = -mViewHeight / 2;
        } else if (relativeY > mViewHeight / 2) {
            relativeY = mViewHeight / 2;
        }

        return relativeY;
    }

    private boolean mFlagMayPress = false;
    private float mSpanY;
    private float mLastY;
    private int velocityY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mItemHeight == 0) return true;

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        currY = event.getY();
        LogUtil.d(TAG, "event.getAction: " + event.getAction());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                reset(false);

                mFlagMayPress = true;
                mHandlerInNewThread.removeMessages(HANDLER_WHAT_REFRESH);
                stopScrolling();
                downY = currY;
                mLastY = downY;
                downYGlobal = mCurrDrawGlobalY;
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                LogUtil.d(TAG, "onTouchEvent.ACTION_MOVE");
                mSpanY = downY - currY;
                if (mOnScrollListener != null) {
                    mOnScrollListener.onScrollFling(LoadPickerView.this, calculateSpeedRatio(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL));
                }
                printD("mSpanY: " + mSpanY + ", downYGlobal: " + downYGlobal + ", downY: " + downY + ", currY: " + currY);
                if (mFlagMayPress && (-mScaledTouchSlop < mSpanY && mSpanY < mScaledTouchSlop)) {

                } else {
                    LogUtil.d(TAG, "mFlagMayPress turn to false");
                    mFlagMayPress = false;
                    mCurrDrawGlobalY = limitY((int) (downYGlobal + mSpanY));
                    calculateFirstItemParameterByGlobalY();
                    invalidate();
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                }
                break;
            case MotionEvent.ACTION_UP:
                LogUtil.d(TAG, "onTouchEvent.ACTION_UP mFlagMayPress: " + mFlagMayPress);
                if (mFlagMayPress) {
//                    click(event);
                    reset();
                } else {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000);
                    velocityY = (int) (velocityTracker.getYVelocity() * mFriction);
                    printD("velocityY: " + velocityY);
//                    if (Math.abs(velocityY) > mMiniVelocityFling) {
//                        mScroller.fling(0, mCurrDrawGlobalY, 0, -velocityY,
//                                Integer.MIN_VALUE, Integer.MAX_VALUE, limitY(Integer.MIN_VALUE), limitY(Integer.MAX_VALUE));
//                        invalidate();
//                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
//                    }
//                    mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), 0);
                    releaseVelocityTracker();
                    stopScrolling();
                    mHandlerInNewThread.removeCallbacksAndMessages(null);
                    mHandlerInNewThread.sendEmptyMessageDelayed(HANDLER_SCROLL_CYCLE, 0);
                    mOnScrollListener.onScrollFling(LoadPickerView.this, calculateSpeedRatio(OnScrollListener.SCROLL_STATE_CYCLE));
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                LogUtil.d(TAG, "onTouchEvent.ACTION_CANCEL");

                downYGlobal = mCurrDrawGlobalY;
                stopScrolling();
                mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), 0);
                reset();
                break;
        }
        return true;
    }

    private void click(MotionEvent event) {
        float y = event.getY();
        for (int i = 0; i < mShowCount; i++) {
            if (mItemHeight * i <= y && y < mItemHeight * (i + 1)) {
                clickItem(i);
                break;
            }
        }
    }

    private void clickItem(int showCountIndex) {
        if (0 <= showCountIndex && showCountIndex < mShowCount) {
            //clicked the showCountIndex of the view
            scrollByIndexSmoothly(showCountIndex - mShowCount / 2);
        } else {
            //wrong
        }
    }

    private float getTextCenterYOffset(Paint.FontMetrics fontMetrics) {
        if (fontMetrics == null) return 0;
        return Math.abs(fontMetrics.top + fontMetrics.bottom) / 2;
    }

    private int mViewWidth;
    private int mViewHeight;
    private int mItemHeight;
    private float dividerY0;
    private float dividerY1;
    private float mViewCenterX;
    private float mQuarterX;

    //defaultPickedIndex relative to the shown part
    private void correctPositionByDefaultValue(int defaultPickedIndex, boolean wrap) {
        mCurrDrawFirstItemIndex = defaultPickedIndex - (mShowCount - 1) / 2;
        mCurrDrawFirstItemIndex = getIndexByRawIndex(mCurrDrawFirstItemIndex, getOneRecycleSize(), wrap);
        if (mItemHeight == 0) {
            mCurrentItemIndexEffect = true;
        } else {
            mCurrDrawGlobalY = mCurrDrawFirstItemIndex * mItemHeight;

            mInScrollingPickedOldValue = mCurrDrawFirstItemIndex + mShowCount / 2;
            mInScrollingPickedOldValue = mInScrollingPickedOldValue % getOneRecycleSize();
            if (mInScrollingPickedOldValue < 0) {
                mInScrollingPickedOldValue = mInScrollingPickedOldValue + getOneRecycleSize();
            }
            mInScrollingPickedNewValue = mInScrollingPickedOldValue;
            calculateFirstItemParameterByGlobalY();
        }

//        printD("mCurrDrawGlobalY: " + mCurrDrawGlobalY);
    }

    //first shown item's content index, corresponding to the Index of mDisplayedValued
    private int mCurrDrawFirstItemIndex = 0;
    //the first shown item's Y
    private int mCurrDrawFirstItemY = 0;
    //global Y corresponding to scroller
    private int mCurrDrawGlobalY = 0;

    @Override
    public void computeScroll() {
        if (mItemHeight == 0) return;
        if (mScroller.computeScrollOffset()) {
            mCurrDrawGlobalY = mScroller.getCurrY();
            printD("computeScroll.mCurrDrawGlobalY: " + mCurrDrawGlobalY);
            calculateFirstItemParameterByGlobalY();
            postInvalidate();
        }
    }

    private int mCycleCount = 0;

    /**
     * 计算第一个文本从哪里开始绘制。
     */
    private void calculateFirstItemParameterByGlobalY() {
        mCurrDrawFirstItemIndex = (int) Math.floor((float) mCurrDrawGlobalY / mItemHeight);
        mCurrDrawFirstItemY = -(mCurrDrawGlobalY - mCurrDrawFirstItemIndex * mItemHeight);
        printD("mCurrDrawGlobalY: " + mCurrDrawGlobalY + " , mItemHeight: " + mItemHeight + " ,mCurrDrawFirstItemIndex: " + mCurrDrawFirstItemIndex);
        printD("before.mCurrDrawFirstItemY: " + mCurrDrawFirstItemY);
        if (mOnValueChangeListenerInScrolling != null) {
            if (-mCurrDrawFirstItemY > mItemHeight / 2) {
                mInScrollingPickedNewValue = mCurrDrawFirstItemIndex + 1 + mShowCount / 2;
            } else {
                mInScrollingPickedNewValue = mCurrDrawFirstItemIndex + mShowCount / 2;
            }
            mInScrollingPickedNewValue = mInScrollingPickedNewValue % getOneRecycleSize();
            if (mInScrollingPickedNewValue < 0) {
                mInScrollingPickedNewValue = mInScrollingPickedNewValue + getOneRecycleSize();
            }
            if (mInScrollingPickedOldValue != mInScrollingPickedNewValue) {
                respondPickedValueChangedInScrolling(mInScrollingPickedNewValue, mInScrollingPickedOldValue);
            }
            mInScrollingPickedOldValue = mInScrollingPickedNewValue;
        }

        if (OnScrollListener.SCROLL_STATE_CYCLE == mScrollState) {
            mCurrDrawFirstItemY = mCycleFirstY * mCycleCount % mItemHeight;
            printD("after.mCurrDrawFirstItemY: " + mCurrDrawFirstItemY);
        }
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void updateMaxWHOfDisplayedValues(boolean needRequestLayout) {
        updateMaxWidthOfDisplayedValues();
        updateMaxHeightOfDisplayedValues();
        if (needRequestLayout &&
                (mSpecModeW == MeasureSpec.AT_MOST || mSpecModeH == MeasureSpec.AT_MOST)) {
            mHandlerInMainThread.sendEmptyMessage(HANDLER_WHAT_REQUEST_LAYOUT);
        }
    }

    private int mSpecModeW = MeasureSpec.UNSPECIFIED;
    private int mSpecModeH = MeasureSpec.UNSPECIFIED;

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = mSpecModeW = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            int marginOfHint = Math.max(mWidthOfHintText, mWidthOfAlterHint) == 0 ? 0 : mMarginEndOfHint;
            int gapOfHint = Math.max(mWidthOfHintText, mWidthOfAlterHint) == 0 ? 0 : mMarginStartOfHint;

            int maxWidth = Math.max(mMaxWidthOfAlterArrayWithMeasureHint,
                    Math.max(mMaxWidthOfDisplayedValues, mMaxWidthOfAlterArrayWithoutMeasureHint)
                            + 2 * (gapOfHint + Math.max(mWidthOfHintText, mWidthOfAlterHint) + marginOfHint + 2 * mItemPaddingHorizontal));
            result = this.getPaddingLeft() + this.getPaddingRight() + maxWidth;//MeasureSpec.UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = mSpecModeH = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            int maxHeight = mShowCount * (mMaxHeightOfDisplayedValues + 2 * mItemPaddingVertical);
            result = this.getPaddingTop() + this.getPaddingBottom() + maxHeight;//MeasureSpec.UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //It is used to clear canvas, invode two line on top and bottom.
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//        if (mIsDrawLine) {
//        drawLinesContent(canvas);
//        } else {
        drawContent(canvas);
//        }

//        drawLine(canvas); //No need divide lines and hints. yangzhuoxun add 20170808.
//        drawHint(canvas);
    }

    private boolean mIsDrawLine = false;

    public void setIsDrawLine(boolean isDrawLine) {
        mIsDrawLine = isDrawLine;
    }

    private void drawLinesContent(Canvas canvas) {
//        canvas.drawLine(mViewCenterX ,
//                dividerY0, mViewWidth - getPaddingRight() - mDividerMarginR, dividerY0, mPaintDivider);
//        printD("left: " + mViewCenterX + " , y: " + dividerY0 + ", right: " + (mViewWidth - getPaddingRight() - mDividerMarginR) + " ,mViewHeight: " + mViewHeight);
        float halfHeight = mViewHeight / 2;
        float diffHeight = (float) mViewHeight / (Constant.LINES_COUNT - 1);
//        printD("halfHeight: " + halfHeight + " ,diffHeight: " + diffHeight);
        for (int i = 1; i < Constant.LINES_COUNT; i++) {
            float y = diffHeight * i;
            float xLength = getHalfXLength(y - halfHeight);
//            printD("i: " + i + " ,y: " + y + ", xLength: " + xLength);
            mPaintText.setColor(mTextColorNormal);
            canvas.drawLine(mViewCenterX - xLength, y - limitLinesY(mSpanY), mViewCenterX + xLength, y - limitLinesY(mSpanY), mPaintText);
        }
    }

    private static final int Y_HALF_LENGTH = 400;
//    private static final int LINE_COUNT = 21;


    private static boolean mIsEllipse = false; //s

    private float getXLengthByIndex(int count) {
        if (count == 0 || Constant.LINES_COUNT == count + 1) {
            return 5; //set a default lenght for 0.
        }

        float diffHeight = (float) Y_HALF_LENGTH * 2 / (Constant.LINES_COUNT - 1);

        float y = diffHeight * count;
        /***
         *      -------------------------------->X
         *      |
         *      |
         *      |
         *      |
         *    Y V
         */
        float xLength = getHalfXLength(y - Y_HALF_LENGTH);
        return limitLinesY(xLength);

    }

    private float getHalfXLength(float y) {
        float x = 0f;
        if (mIsEllipse) {
            x = (float) (Math.sqrt(1 - y * y / (Y_HALF_LENGTH * Y_HALF_LENGTH)) * mViewCenterX);
            return x;
        } else {
            x = mViewCenterX * (1 - Math.abs(y / Y_HALF_LENGTH));
        }
        return x;
    }

    private void drawContent(Canvas canvas) {
        int index;
        int textColor;
        float textSize;
        float fraction = 0f;// fraction of the item in state between normal and selected, in[0, 1]
        float textSizeCenterYOffset;

        for (int i = 0; i < mShowCount + 1; i++) {
            float y = mCurrDrawFirstItemY + mItemHeight * i;
            index = getIndexByRawIndex(mCurrDrawFirstItemIndex + i, getOneRecycleSize(), mWrapSelectorWheel && mWrapSelectorWheelCheck);
//            printD("y: " + y + ", mCurrDrawFirstItemY: " + mCurrDrawFirstItemY + " , mItemHeight: " + mItemHeight);
//            printD("i: " + i + ", mShowCount: " + mShowCount + ", index: " + index);
            if (i == mShowCount / 2) {//this will be picked
                fraction = (float) (mItemHeight + mCurrDrawFirstItemY) / mItemHeight;
                textColor = getEvaluateColor(fraction, mTextColorNormal, mTextColorSelected);
                textSize = getEvaluateSize(fraction, mTextSizeNormal, mTextSizeSelected);
                textSizeCenterYOffset = getEvaluateSize(fraction, mTextSizeNormalCenterYOffset,
                        mTextSizeSelectedCenterYOffset);

            } else if (i == mShowCount / 2 + 1) {
                textColor = getEvaluateColor(1 - fraction, mTextColorNormal, mTextColorSelected);
                textSize = getEvaluateSize(1 - fraction, mTextSizeNormal, mTextSizeSelected);
                textSizeCenterYOffset = getEvaluateSize(1 - fraction, mTextSizeNormalCenterYOffset,
                        mTextSizeSelectedCenterYOffset);
            } else {
                textColor = mTextColorNormal;
                textSize = mTextSizeNormal;
                textSizeCenterYOffset = mTextSizeNormalCenterYOffset;
            }
            mPaintText.setColor(textColor);
            mPaintText.setTextSize(textSize);

            if (mIsDrawLine) {
//                if (y + mItemHeight / 2 > mViewHeight / 2 - mItemHeight / 2 && y + mItemHeight / 2 < mViewHeight / 2 + mItemHeight / 2) {
//                    mLinePaint.setColor(DEFAULT_TEXT_COLOR_SELECTED);
//                    mLinePaint.setStrokeWidth(SELECT_PAINT_WIDTH);
//                } else {
                mLinePaint.setColor(DEFAULT_TEXT_COLOR_SELECTED);
                mLinePaint.setStrokeWidth(NORMAL_PAINT_WIDTH);
//                }
            }

            if (0 <= index && index < getOneRecycleSize()) {
                if (mIsDrawLine) {
//                    canvas.drawLine(mViewCenterX - getXLengthByIndex(index), y + mItemHeight / 2, mViewCenterX + getXLengthByIndex(index), y + mItemHeight / 2, mLinePaint);
                    canvas.drawLine(mViewCenterX - mQuarterX, y + mItemHeight / 2, mViewCenterX + mQuarterX, y + mItemHeight / 2, mLinePaint);
                } else {
                    CharSequence str = mDisplayedValues[index + mMinShowIndex];
                    if (mTextEllipsize != null) {
                        str = TextUtils.ellipsize(str, mPaintText, getWidth() - 2 * mItemPaddingHorizontal, getEllipsizeType());
                    }
                    canvas.drawText(str.toString(), mViewCenterX,
                            y + mItemHeight / 2 + textSizeCenterYOffset, mPaintText);
                }
            } else if (!TextUtils.isEmpty(mEmptyItemHint)) {
                canvas.drawText(mEmptyItemHint, mViewCenterX,
                        y + mItemHeight / 2 + textSizeCenterYOffset, mPaintText);
            }
        }
    }

    private TextUtils.TruncateAt getEllipsizeType() {
        switch (mTextEllipsize) {
            case TEXT_ELLIPSIZE_START:
                return TextUtils.TruncateAt.START;
            case TEXT_ELLIPSIZE_MIDDLE:
                return TextUtils.TruncateAt.MIDDLE;
            case TEXT_ELLIPSIZE_END:
                return TextUtils.TruncateAt.END;
            default:
                throw new IllegalArgumentException("Illegal text ellipsize type.");
        }
    }

    private void drawLine(Canvas canvas) {
        if (mShowDivider) {
//            printD("getPaddingLeft() + mDividerMarginL: " + (getPaddingLeft() + mDividerMarginL) + ", dividerY0: " + dividerY0 + " ,right: " + (mViewWidth - getPaddingRight() - mDividerMarginR));
            canvas.drawLine(getPaddingLeft() + mDividerMarginL,
                    dividerY0, mViewWidth - getPaddingRight() - mDividerMarginR, dividerY0, mPaintDivider);
            canvas.drawLine(getPaddingLeft() + mDividerMarginL,
                    dividerY1, mViewWidth - getPaddingRight() - mDividerMarginR, dividerY1, mPaintDivider);
        }
    }

    private void drawHint(Canvas canvas) {
        if (TextUtils.isEmpty(mHintText)) return;
        canvas.drawText(mHintText,
                mViewCenterX + (mMaxWidthOfDisplayedValues + mWidthOfHintText) / 2 + mMarginStartOfHint,
                (dividerY0 + dividerY1) / 2 + mTextSizeHintCenterYOffset, mPaintHint);
    }

    private void updateMaxWidthOfDisplayedValues() {
        float savedTextSize = mPaintText.getTextSize();
        mPaintText.setTextSize(mTextSizeSelected);
        mMaxWidthOfDisplayedValues = getMaxWidthOfTextArray(mDisplayedValues, mPaintText);
        mMaxWidthOfAlterArrayWithMeasureHint = getMaxWidthOfTextArray(mAlterTextArrayWithMeasureHint, mPaintText);
        mMaxWidthOfAlterArrayWithoutMeasureHint = getMaxWidthOfTextArray(mAlterTextArrayWithoutMeasureHint, mPaintText);
        mPaintText.setTextSize(mTextSizeHint);
        mWidthOfAlterHint = getTextWidth(mAlterHint, mPaintText);
        mPaintText.setTextSize(savedTextSize);
    }

    private int getMaxWidthOfTextArray(CharSequence[] array, Paint paint) {
        if (array == null) {
            return 0;
        }
        int maxWidth = 0;
        for (CharSequence item : array) {
            if (item != null) {
                int itemWidth = getTextWidth(item, paint);
                maxWidth = Math.max(itemWidth, maxWidth);
            }
        }
        return maxWidth;
    }

    private int getTextWidth(CharSequence text, Paint paint) {
        if (!TextUtils.isEmpty(text)) {
            return (int) (paint.measureText(text.toString()) + 0.5f);
        }
        return 0;
    }

    private void updateMaxHeightOfDisplayedValues() {
        float savedTextSize = mPaintText.getTextSize();
        mPaintText.setTextSize(mTextSizeSelected);
        mMaxHeightOfDisplayedValues = (int) (mPaintText.getFontMetrics().bottom - mPaintText.getFontMetrics().top + 0.5);
        mPaintText.setTextSize(savedTextSize);
    }

    private void updateContentAndIndex(String[] newDisplayedValues) {
        mMinShowIndex = 0;
        mMaxShowIndex = newDisplayedValues.length - 1;
        mDisplayedValues = newDisplayedValues;
        updateWrapStateByContent();
    }

    private void updateContent(String[] newDisplayedValues) {
        mDisplayedValues = newDisplayedValues;
        updateWrapStateByContent();
    }

    //used in setDisplayedValues
    private void updateValue() {
        inflateDisplayedValuesIfNull();
        updateWrapStateByContent();
        mMinShowIndex = 0;
        mMaxShowIndex = mDisplayedValues.length - 1;
    }

    private void updateValueForInit() {
        inflateDisplayedValuesIfNull();
        updateWrapStateByContent();
        if (mMinShowIndex == -1) {
            mMinShowIndex = 0;
        }
        if (mMaxShowIndex == -1) {
            mMaxShowIndex = mDisplayedValues.length - 1;
        }
        setMinAndMaxShowIndex(mMinShowIndex, mMaxShowIndex, false);
    }

    private void inflateDisplayedValuesIfNull() {
        if (mDisplayedValues == null) {
            mDisplayedValues = new String[1];
            mDisplayedValues[0] = "0";
        }
    }

    private void updateWrapStateByContent() {
        mWrapSelectorWheelCheck = mDisplayedValues.length <= mShowCount ? false : true;
    }

    private int refineValueByLimit(int value, int minValue, int maxValue, boolean wrap) {
        if (wrap) {
            if (value > maxValue) {
                value = (value - maxValue) % getOneRecycleSize() + minValue - 1;
            } else if (value < minValue) {
                value = (value - minValue) % getOneRecycleSize() + maxValue + 1;
            }
            return value;
        } else {
            if (value > maxValue) {
                value = maxValue;
            } else if (value < minValue) {
                value = minValue;
            }
            return value;
        }
    }

    private void stopRefreshing() {
        if (mHandlerInNewThread != null) {
            mHandlerInNewThread.removeMessages(HANDLER_WHAT_REFRESH);
        }
    }

    public void stopScrolling() {
        if (mScroller != null) {
            if (!mScroller.isFinished()) {
                mScroller.startScroll(0, mScroller.getCurrY(), 0, 0, 1);
                mScroller.abortAnimation();
                postInvalidate();
            }
        }
    }

    public void stopScrollingAndCorrectPosition() {
        stopScrolling();
        if (mHandlerInNewThread != null) {
            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH), 0);
        }
    }

    private Message getMsg(int what) {
        return getMsg(what, 0, 0, null);
    }

    private Message getMsg(int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        return msg;
    }

    //===tool functions===//
    private boolean isStringEqual(String a, String b) {
        if (a == null) {
            if (b == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return a.equals(b);
        }
    }

    private int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private int dp2px(Context context, float dpValue) {
        final float densityScale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * densityScale + 0.5f);
    }

    private int getEvaluateColor(float fraction, int startColor, int endColor) {

        int a, r, g, b;

        int sA = (startColor & 0xff000000) >>> 24;
        int sR = (startColor & 0x00ff0000) >>> 16;
        int sG = (startColor & 0x0000ff00) >>> 8;
        int sB = (startColor & 0x000000ff) >>> 0;

        int eA = (endColor & 0xff000000) >>> 24;
        int eR = (endColor & 0x00ff0000) >>> 16;
        int eG = (endColor & 0x0000ff00) >>> 8;
        int eB = (endColor & 0x000000ff) >>> 0;

        a = (int) (sA + (eA - sA) * fraction);
        r = (int) (sR + (eR - sR) * fraction);
        g = (int) (sG + (eG - sG) * fraction);
        b = (int) (sB + (eB - sB) * fraction);

        return a << 24 | r << 16 | g << 8 | b;
    }

    private float getEvaluateSize(float fraction, float startSize, float endSize) {
        return startSize + (endSize - startSize) * fraction;
    }

    private String[] convertCharSequenceArrayToStringArray(CharSequence[] charSequences) {
        if (charSequences == null) return null;
        String[] ret = new String[charSequences.length];
        for (int i = 0; i < charSequences.length; i++) {
            ret[i] = charSequences[i].toString();
        }
        return ret;
    }

    private static final int MAX_Y_DIFF = 200;
    private static final int MAX_FLING_SPEED = 10000;

    /**
     * calculate speed by MAX_Y_DIFF if touch move; by MAX_FLING_SPEED if it is fling; else return 0.
     *
     * @return
     */
    public float calculateSpeedRatio(int mode) {
        LogUtil.d(TAG, "calculateSpeedRatio.mode: " + mode);
        if (OnScrollListener.SCROLL_STATE_IDLE == mode) {
            return StringUtil.big2(0f);
        } else if (OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == mode) {
            float diffY = currY - mLastY;
            if (diffY > MAX_Y_DIFF) {
                diffY = MAX_Y_DIFF;
            } else if (diffY < -MAX_Y_DIFF) {
                diffY = -MAX_Y_DIFF;
            }
            mLastY = currY;
            printD("calculateSpeedRatio.diffY: " + diffY);
            return StringUtil.big2(diffY / MAX_Y_DIFF);
        } else if (OnScrollListener.SCROLL_STATE_FLING == mode) {
            if (mScroller != null) {
                float currentV = mScroller.getCurrVelocity();
                printD("calculateSpeedRatio.currentV: " + currentV);
                if (currentV > MAX_FLING_SPEED) {
                    if (velocityY > 0) {
                        return StringUtil.big2(1f);
                    } else {
                        return StringUtil.big2(-1f);
                    }
                } else {
                    return velocityY > 0 ? StringUtil.big2(currentV / MAX_FLING_SPEED) : StringUtil.big2(-currentV / MAX_FLING_SPEED);
                }
            }
        } else if (OnScrollListener.SCROLL_STATE_CYCLE == mode) {
            float v = ((float) velocityY) / MAX_FLING_SPEED;

            mCycleFirstY = getUIScrollSpeed(v);
            LogUtil.d(TAG, "v: " + v + " ,mCycleFirstY: " + mCycleFirstY);
            if (v >= 1) {
                return StringUtil.big2(1f);
            } else if (v <= -1) {
                return StringUtil.big2(-1f);
            } else {
                if (Math.abs(v) < MIN_SPEED) { //速度小最小刻度值时，停止。
                    reset();
                    return StringUtil.big2(0);
                }
                return StringUtil.big2(v);
            }
        }
        return 0f;
    }

    private static final boolean D = true;

    private void printD(String msg) {
        if (D) {
            if (mIsDrawLine) {
                LogUtil.d(TAG, "---" + msg);
            } else {
                LogUtil.d(TAG, "++" + msg);
            }
        }
    }

    private void reset() {
        reset(true);
    }

    private void reset(boolean draw) {
        velocityY = 0;
        mCycleCount = 0;
        mHandlerInNewThread.removeCallbacksAndMessages(null);
        mUIScrollSpeed = 0;
        mOnScrollListener.onScrollFling(LoadPickerView.this, calculateSpeedRatio(OnScrollListener.SCROLL_STATE_IDLE));
        if (draw) {
            calculateFirstItemParameterByGlobalY();
            postInvalidate();
        }
    }

    private static final int TAP[] = new int[]{0, 1, 5, 10, 15};

    private int mUIScrollSpeed = 0;
    private int mCycleFirstY = 0;

    private int getUIScrollSpeed(float signedTapV) {
        int count = TAP.length;
        float didV = ((float) 1) / (count - 1);
        LogUtil.d(TAG, "getUIScrollSpeed.signedTapV: " + signedTapV + ", didV: " + didV);
        if (Math.abs(signedTapV) < MIN_SPEED) {
            return 0;
        }
        if (signedTapV >= 1) {
            return TAP[count - 1];
        } else if (signedTapV <= -1) {
            return -TAP[count - 1];
        }

        int i = 0;
        if (signedTapV > 0) {
            for (i = 1; i < count; i++) {
                if (didV * i > signedTapV) {
                    LogUtil.d(TAG, "i: " + i);
                    return TAP[i];
                }
            }
        } else {
            for (i = count - 2; i >= 0; i--) {
                if (-didV * i > signedTapV) {
                    LogUtil.d(TAG, "i: " + i);
                    return -TAP[i + 1];
                }
            }

        }
        return 0;

    }

}