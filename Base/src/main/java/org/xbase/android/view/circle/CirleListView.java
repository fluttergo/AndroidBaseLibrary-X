package org.xbase.android.view.circle;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.Scroller;

public class CirleListView extends ViewGroup {

	private TextPaint mTextPaint;
	private int contentWidth;
	private int contentHeight;
	final int hideChildViewAtEnd = 2;// 结尾不显示的view个数
	final int MaxVisableChildViewCount = 8;
	private double FixedOffsetAngle = 22.5;// 整体偏移角度

	Scroller mScroller;
	int scrollX = 0;// ScrollerX轴移动偏移量
	int scrollY = 0;// ScrollerY轴移动偏移量
	int lastScrollX = 0;
	int lastScrollY = 0;
	int downx = 0;// 按下的坐标
	int downy = 0;// 按下的坐标
	int movedx = 0;// 已经移动的X距离
	int movedy = 0;// 已经移动的Y的距离

	private int radius; // 圆形菜单半径
	private double IntervalAngle = 0;// child view之间间隔的角度

	private int MaxScrollY = 0;
	private int MinScrollY = 0;

	public CirleListView(Context context) {
		super(context);
		init(null, 0);
	}

	public CirleListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public CirleListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		mTextPaint = new TextPaint();
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);

		mScroller = new Scroller(getContext(),
				new AccelerateDecelerateInterpolator());
		
		Log.d(TagScroller, "init");
	}


	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/**
		 * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
		 */
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

		// 计算出所有的childView的宽和高
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		/**
		 * 记录如果是wrap_content是设置的宽和高
		 */
		int width = 0;
		int height = 0;
		// 用于计算左边两个childView的高度
		int lHeight = 0;
		// 用于计算右边两个childView的高度，最终高度取二者之间大值
		int rHeight = 0;

		// 用于计算上边两个childView的宽度
		int tWidth = 0;
		// 用于计算下面两个childiew的宽度，最终宽度取二者之间大值
		int bWidth = 0;
		width = Math.max(tWidth, bWidth);
		height = Math.max(lHeight, rHeight);

		// 如果是wrap_content设置为我们计算的值 否则：直接设置为父容器计算的值
		setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? sizeWidth
				: width, (heightMode == MeasureSpec.EXACTLY) ? sizeHeight
				: height);
	}

	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	public String TagScroller = "TagScroller";
	public String TagEvent = "TagEvent";
	private BaseAdapter mAdapter;

	public OnTouchListener eventDisptchTaget;

	@Override
	public void computeScroll() {
		super.computeScroll();
		// 先判断mScroller滚动是否完成
		if (mScroller.computeScrollOffset()) {

			// 这里调用View的scrollTo()完成实际的滚动
			// scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

			Log.d(TagScroller, "computeScroll:x" + mScroller.getCurrX() + " y "
					+ mScroller.getCurrY());
			// 根据角度预期值反推offset

			scrollX = lastScrollX + mScroller.getCurrX();
			scrollY = lastScrollY + mScroller.getCurrY();

			if (mScroller.getCurrY() == mScroller.getFinalY()) {
				Log.d(TagScroller, "has finsh" + mScroller.getCurrX() + " y "
						+ mScroller.getCurrY());
				// scrollX += mScroller.getStartX();
				// scrollY += mScroller.getStartY(); 
			}
			requestLayout();
			// 必须调用该方法，否则不一定能看到滚动效果
			invalidate();
		} else {

		}
	}

	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent e) {

		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:// 按下
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
				scrollX += mScroller.getStartX();
				scrollY += mScroller.getStartY();
			}
			downx = (int) e.getX();
			downy = (int) e.getY();
			lastScrollX = scrollX;
			lastScrollY = scrollY;
			movedx = 0;
			movedy = 0;

			// if ( eventDisptchTaget != null) {
			// eventDisptchTaget.onTouch(this, e);
			// }
			// mHasInterputTouch = true;
			// mIsFirstMoveEvent = true;

			break;
		case MotionEvent.ACTION_MOVE:// 移动
			// if (mIsFirstMoveEvent) {
			// double movedx2 = Math.abs(e.getX() - lastEventX);
			// double movedy2 = Math.abs(e.getY() - lastEventY);
			// if (movedy2 > movedx2 && movedy2 > 1) {
			// mHasInterputTouch = true;
			// }else{
			// mHasInterputTouch = false;
			// }
			// mIsFirstMoveEvent = false;
			// }
			// if (!mHasInterputTouch ) {
			// if (eventDisptchTaget!= null) {
			// eventDisptchTaget.onTouch(this,e);
			// }
			// break;
			// }

			movedx = (int) (e.getX() - downx);
			movedy = (int) (e.getY() - downy);
			scrollX = lastScrollX + movedx;
			scrollY = lastScrollY + movedy;

			Log.d(TagEvent, "tempoffsetx: " + scrollX + " tempoffsety :"
					+ scrollY + "  ");

			if (scrollY > 0) {
				scrollY = 0;
			} else if (scrollY < -MaxScrollY) {
				scrollY = -MaxScrollY;
			}

			requestLayout();
			break;
		case MotionEvent.ACTION_UP:

			// adjustOffset();
			break;
		default:
			break;
		}

		return true;
	}

	protected void adjustOffset() {
		// 计算此时的偏移角度
		final double distance = offsetYToOffsetAngle(scrollY);
		// 取模得到相对于单个区间内角度偏移
		final double moth = distance % (IntervalAngle);
		Log.d(TagEvent, "ACTION_UP->distance angle" + distance + "moth:" + moth);
		// 四舍五入校正偏移的角度
		double adjustAngle;
		if (moth > (IntervalAngle / 2)) {
			adjustAngle = IntervalAngle - moth;
		} else {// 上一格
			adjustAngle = -1 * moth;
		}
		// 换算成滑动偏移量
		double adjustScrollY = offsetAngleToOffsetY(adjustAngle);
		Log.d(TagEvent, "ACTION_UP->adjustAngle: " + adjustAngle
				+ " adjustScrollY:" + adjustScrollY);
		Log.d(TagEvent, "offsety:" + scrollY);
		mScroller.abortAnimation();
		Log.d(TagScroller, "start scroll ,adjustScrollY-> " + adjustScrollY);

		lastScrollX = scrollX;
		lastScrollY = scrollY;

		mScroller.startScroll(0, 0, 0, (int) adjustScrollY, 1000);
		invalidate();
	}

	private double offsetYToOffsetAngle(double offsetY) {
		// Y轴偏移量/原周长 * 360度 得到偏移的角度
		final double angle = (offsetY / (2 * radius * Math.PI)) * 360;
		// Log.d(TagScroller,"y to angle=>   "+offsetY+"  =>  " + angle +"");
		return angle;
	}

	private double offsetAngleToOffsetY(double angle) {
		final double offsetY = (angle / 360) * (2 * radius * Math.PI);
		// Log.d(TagScroller,"angle to y  =>   "+angle+"  =>  " + offsetY );
		return offsetY;
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
//		canvas.drawLine(10, getHeight() / 2, 10, getHeight() / 2 + scrollY,
//				mTextPaint);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (getChildCount() < 1) {
			return;
		}
		caluInitValue();

		final double distance = offsetYToOffsetAngle(scrollY);
		for (int i = 0; i < getChildCount(); i++) {
			final double offsetAngleThisView = IntervalAngle * i + distance
					+ FixedOffsetAngle;
			final double angleroffset = Math.toRadians(offsetAngleThisView);
			double x = (Math.sin(angleroffset) * radius);
			double y = -(Math.cos(angleroffset) * radius);
			View v0 = getChildAt(i);
			final int vw = v0.getMeasuredWidth();
			final int vh = v0.getMeasuredHeight();
			Log.d(TagScroller, "v "+i+" view Measured "+vw+","+vh);
			int vx = (int) (x + contentWidth / 2 - vw / 2);
			int vy = (int) (y + radius);
			Log.d(TagScroller, "v "+i+" view.x y "+vx+" "+vy);
			if (isViewVisiable(offsetAngleThisView, v0, vx, vy, vx + vw, vy
					+ vh)) {
				v0.layout(vx, vy, vx + vw, vy + vh);
			} else {
				v0.layout(9999, 9999, 9999 + vw, 9999 + vh);
			}

		}

	}

	private boolean isViewVisiable(double angleroffset, View v0, int t, int l,
			int r, int b) {
		if (angleroffset < 0) {
			return false;
		}
		if (angleroffset > 360 - 2 * IntervalAngle) {
			return false;
		}
		return true;
	}

	private void caluInitValue() {
		if (contentWidth == 0 || contentHeight == 0) {
			int paddingLeft = getPaddingLeft();
			int paddingTop = getPaddingTop();
			int paddingRight = getPaddingRight();
			int paddingBottom = getPaddingBottom();

			contentWidth = getWidth() - paddingLeft - paddingRight;
			contentHeight = getHeight() - paddingTop - paddingBottom;
			radius = (contentHeight - getChildAt(0).getMeasuredHeight())
					/ hideChildViewAtEnd;

			IntervalAngle = 360 / MaxVisableChildViewCount;

			MinScrollY = 0;
			double scrolleroffset = ((double) getChildCount() + hideChildViewAtEnd)
					/ MaxVisableChildViewCount;
			scrolleroffset = scrolleroffset - 1 > 0 ? scrolleroffset - 1 : 0;
			MaxScrollY = (int) (scrolleroffset * hideChildViewAtEnd * Math.PI * radius);
			Log.d(TagScroller, "MinScrollY:" + MinScrollY + " MaxScrollY:"
					+ MaxScrollY);
//			new Exception().printStackTrace();
		}
	}

	public BaseAdapter getAdapter() {
		return mAdapter;
	}

	public void setAdapter(BaseAdapter adapter) {
		clearState();
		Log.d(TagScroller, "setAdapter:"+adapter.getCount());
		mAdapter = adapter;
		for (int i = 0; i < mAdapter.getCount(); i++) {
			View v0 = mAdapter.getView(i, null, this);
			addView(v0);
			// attachViewToParent(v0, i,v0.getLayoutParams());
		}
		Log.d(TagScroller, "requestLayout:"+mAdapter.getCount());
		requestLayout();
	}

	private void clearState() {
		mScroller.abortAnimation();
		contentWidth = 0;
		contentHeight = 0;
		scrollX = 0;// ScrollerX轴移动偏移量
		scrollY = 0;// ScrollerY轴移动偏移量
		lastScrollX = 0;
		lastScrollY = 0;
		downx = 0;// 按下的坐标
		downy = 0;// 按下的坐标
		movedx = 0;// 已经移动的X距离
		movedy = 0;// 已经移动的Y的距离

		radius = 0; // 圆形菜单半径
		IntervalAngle = 0;// child view之间间隔的角度

		MaxScrollY = 0;
		MinScrollY = 0;
		removeAllViews();
	}

}
