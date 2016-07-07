package org.xbase.android.touch;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

/**
	 * 多点触控拖动缩放<br>
	 * 会追踪一个目标的缩放和移动状态
	 * @author Ge Liang
	 */
	public abstract class MultipleTouchGestrueListener implements
			View.OnTouchListener {
		public float MaxScale = 3f;
		public float MinScale = 0.5f;
		/**
		 * 计算两触点之间的距离
		 * 
		 * @param event
		 * @return
		 */
		@SuppressLint("FloatMath")
		public static float distance(MotionEvent event) {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			return (float)Math.sqrt(dx * dx + dy * dy);
		}

		/**
		 * 计算两触点之间的中间点
		 * 
		 * @param event
		 * @return
		 */
		public static PointF mid(MotionEvent event) {
			float midX = (event.getX(1) + event.getX(0)) / 2;
			float midY = (event.getY(1) + event.getY(0)) / 2;
			return new PointF(midX, midY);
		}
		
		public abstract void onTouch(MotionEvent event);
		/**
		 * 
		 * @param downPointf 拖曳事件发生点
		 * @param currentX
		 * @param currentY
		 * @param dragPointf 目标被拖曳到该点
		 */
		public abstract void onDrag(View v,PointF downPointf, float currentX, float currentY, PointF dragPointf);
		/**
		 * 
		 * @param downPointf 缩放事件发生点
		 * @param scale 目标被缩放到该比例 大于1为放大,小于1为缩小
		 */
		public abstract void onScale(PointF downPointf,MotionEvent event,float scale);
		public abstract void onUP(PointF dragPointf,PointF down,MotionEvent up,float scale);

		private PointF downPoint = new PointF();
		/**
		 * 记录目标最后的被拖曳的点
		 */
		private PointF historyDragPoint ;
		/**
		 * 目标当前被拖曳到的点
		 */
		private PointF dragPoint = new PointF();
		/**
		 * 目标当前缩放比
		 */
		protected float scale = 1f;
		/**
		 * 记录目标最后的缩放比
		 */
		private float historyScale = 1f;
		public float getHistoryScale() {
			return historyScale;
		}
		private float downDis;// 开始距离
		private int mode = 0;
		public boolean isStartAtCenter = false;
		private static final int DRAG = 1;
		private static final int ZOOM = 2;

		public boolean onTouch(View v, MotionEvent event) {
//			Log.i("MultTouch", event.toString());
			onTouch(event);
			//ACTION_MASK&ACTION_POINTER_1/2/3..._DOWN/UP=ACTION_POINTER_DOWN/UP；
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if (historyDragPoint==null) {
					historyDragPoint = new PointF();
					if(isStartAtCenter ){
						historyDragPoint.set(v.getWidth()/2, v.getHeight()/2);
					}
				}
				mode = DRAG;
				downPoint.set(event.getX(), event.getY());
//				historyDragPoint.set(dragPoint);
				break;
			case MotionEvent.ACTION_POINTER_DOWN:// Multiple Point Touch Down
				downDis = distance(event);
				if (downDis > 10f) {
					mode = ZOOM;
				}
				break;

			case MotionEvent.ACTION_MOVE:// Point Move
				if (mode == DRAG) {
					float dx = event.getX() - downPoint.x;// 得到在x轴的移动距离
					float dy = event.getY() - downPoint.y;// 得到在y轴的移动距离
					
					dragPoint.x = historyDragPoint.x + dx;
					dragPoint.y = historyDragPoint.y + dy;
					onDrag(v,downPoint, event.getX(), event.getY(),dragPoint);
				} else if (mode == ZOOM) {// 缩放
					float endDis = distance(event);// 结束距离
					if (endDis > 10f) {
						scale = (endDis / downDis)*historyScale;// 得到缩放倍数

						if (scale > MaxScale) {
							scale = MaxScale;
						} else {
							if (scale < MinScale) {
								scale = MinScale;
							}
						}
						onScale(downPoint,event, scale);
					}
				}
				break;

			case MotionEvent.ACTION_UP:// Point Move Up
			case MotionEvent.ACTION_POINTER_UP:// 有手指离开屏幕,但屏幕还有触点（手指）
				mode = 0;
				
				historyDragPoint.x =dragPoint.x;
				historyDragPoint.y = dragPoint.y;
				
				historyScale=scale;
				onUP(dragPoint, downPoint, event, scale);
				break;

			}
			return true;
		}

	}