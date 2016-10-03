package org.kohaerenzstiftung.gltest;

import java.util.LinkedList;
import java.util.Random;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.ArrayFactory;
import org.kohaerenzstiftung.game.Finger;
import org.kohaerenzstiftung.game.Resolution;
import org.kohaerenzstiftung.game.TouchEvent;
import org.kohaerenzstiftung.game.gl.Graphics;
import org.kohaerenzstiftung.game.Game.HandleTouchesListener;

import android.graphics.Color;


public class GLTestActivity extends org.kohaerenzstiftung.game.gl.Game implements HandleTouchesListener {

	public class Touch implements Factorisable {
		Finger mFinger = null;
		int mColour;
		int mMoves;
		@Override
		public Factorisable createInstance() {
			return new Touch();
		}
	}
	
	public class Pixel implements Factorisable {
		
		public Pixel(float x, float y) {
			this.mX = x;
			this.mY = y;
		}

		public float mY = -1;
		public float mX = -1;
		@Override
		public Factorisable createInstance() {
			return new Pixel(-1, -1);
		}
	}
	
	public class ColouredPixel extends Pixel {
		public int mColour;
		public ColouredPixel(float x, float y, int colour) {
			super(x, y);
			this.mColour = colour;
		}
	}
	
	private static final int DEFAULTWIDTH = 30;//60;//120;//480;
	private static final int DEFAULTHEIGHT = 50;//100;//200;//800;
	private static final int DEFAULTDPI = 240;
	private Random mRandom = new Random();
	private LinkedList<ColouredPixel> mColouredPixels = new LinkedList<GLTestActivity.ColouredPixel>();
	private Touch mTouches[];
	private ArrayFactory<Touch> mTouchFactory =
			new ArrayFactory<Touch>((mTouches =
			new Touch[TouchEvent.MAX_TOUCHEVENTS]),
			new Touch());

	private void recycleTouch(Touch me) {
		me.mFinger = null;
		mTouchFactory.recycle(me);
	}
	
	private synchronized void processTouch(TouchEvent touchEvent) {
		if (touchEvent.mType == TouchEvent.UP) {
			Touch touch = getTouch(touchEvent.mFinger);
			recycleTouch(touch);
			return;
		}
		LinkedList<ColouredPixel> colouredPixels = this.mColouredPixels;
		if (touchEvent.mType == TouchEvent.DOWN) {
			Touch touch = getFreeTouch();
			touch.mColour = getRandomColour();
			touch.mMoves = 0;
			touch.mFinger = touchEvent.mFinger;
			colouredPixels.add(new ColouredPixel((float) touchEvent.mX, (float) touchEvent.mY, touch.mColour));
		} else {
			Finger finger = touchEvent.mFinger;
			Touch touch = getTouch(touchEvent.mFinger);
			handleMove((float) finger.mX, (float) finger.mY, (float) touchEvent.mX,
					(float) touchEvent.mY, touch);
			colouredPixels.add(new ColouredPixel((float) touchEvent.mX, (float) touchEvent.mY, touch.mColour));
		}
	}

	private Touch getFreeTouch() {
		return mTouchFactory.getFree();
	}

	private Touch getTouch(Finger finger) {
		Touch result = null;
		for (int i = 0; i < mTouches.length; i++) {
			if (mTouches[i].mFinger == finger) {
				result  = mTouches[i];
				break;
			}
		}
		return result;
	}
	
	private int getRandomColour() {
		return mRandom.nextInt();
	}
	
	private void handleMove(float startX, float startY, float endX, float endY, Touch touch) {
		
		float deltaX = endX - startX;
		float deltaY = endY - startY;
		float absDeltaX = deltaX >= 0 ? deltaX : -deltaX;
		float absDeltaY = deltaY >= 0 ? deltaY : -deltaY;

		if (absDeltaX > absDeltaY) {
			handleMoveBy(startX, startY, deltaX, deltaY, touch, true);
		} else {
			handleMoveBy(startX, startY, deltaX, deltaY, touch, false);
		}
	}
	
	private void handleMoveBy(float startX, float startY, float deltaX, float deltaY,
			Touch touch, boolean byX) {
		float delta;
		float delta1;
		float delta2;
		float start1;
		float start2;
		if (byX) {
			delta = deltaX;
			delta1 = deltaX;
			delta2 = deltaY;
			start1 = startX;
			start2 = startY;
		} else {
			delta = deltaY;
			delta1 = deltaY;
			delta2 = deltaX;
			start1 = startY;
			start2 = startX;
		}
		float absDelta = delta >= 0 ? delta : -delta;
		boolean subtract = delta != absDelta;
		float increase = ((float) delta2) / ((float) delta1);
		float last1 = start1;
		float last2 = start2;
		for (int i = 1; i <= absDelta; i++) {
			touch.mMoves++;
			touch.mColour += (touch.mMoves * 10) + touch.mColour;
			float cur1 = subtract ? start1 - i : start1 + i;
			int cur2 = (int) ((increase * (float) i) + start2);
			float x;
			float y;
			for (float val2 = last2 + 1; val2 < cur2; val2++) {
				if (byX) {
					x = last1;
					y = val2;
				} else {
					x = val2;
					y = last1;
				}
				mColouredPixels.add(new ColouredPixel(x, y, touch.mColour));
				touch.mMoves++;
				touch.mColour += (touch.mMoves * 10) + touch.mColour;
			}
			if (byX) {
				x = cur1;
				y = cur2;
			} else {
				x = cur2;
				y = cur1;
			}
			mColouredPixels.add(new ColouredPixel(x, y, touch.mColour));
			last1 = cur1;
			last2 = cur2;
		}
	}
	
	@Override
	protected synchronized void doRender(long deltaMillis) {
		Graphics.clear();
		LinkedList<ColouredPixel> colouredPixels = mColouredPixels;
		int size = colouredPixels.size();
		for (int i = 0; i < size; i++) {
			ColouredPixel pixel = colouredPixels.get(i);
			Graphics.drawPoint((int) pixel.mX, (int) pixel.mY, pixel.mColour);
		}
		//TODO
	}

	@Override
	protected Resolution getResolution(int widthPixels, int heightPixels,
			int densityDpi) {
		float x = DEFAULTWIDTH;
		float y = DEFAULTHEIGHT;

		float factor = ((float) densityDpi) / ((float) DEFAULTDPI);
		
		x *= factor;
		y *= factor;

		Resolution result = new Resolution((int) x, (int) y);

		return result;
	}

	@Override
	protected void setup() {
		Graphics.setClearColour(Color.BLACK);
	}

	@Override
	protected AccelerationChangedListener getAccelerationChangedListener() {
		return null;
	}

	@Override
	protected HandleTouchesListener getHandleTouchesListener() {
		return this;
	}

	@Override
	public void handleTouches(LinkedList<TouchEvent> pendingEvents) {
		int length = pendingEvents.size();
		for (int i = 0; i < length; i++) {
			processTouch(pendingEvents.get(i));
		}
	}

	@Override
	protected void handleHandleOnResume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleHandleOnPause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getMaxStreams() {
		// TODO Auto-generated method stub
		return 0;
	}
}
