package org.kohaerenzstiftung.globjectstest2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.Finger;
import org.kohaerenzstiftung.game.Resolution;
import org.kohaerenzstiftung.game.TouchEvent;
import org.kohaerenzstiftung.game.gl.Graphics;
import org.kohaerenzstiftung.game.gl.objects.Object;
import org.kohaerenzstiftung.game.Game.HandleTouchesListener;


import android.graphics.Color;

public class GLObjectsTest2Activity extends org.kohaerenzstiftung.game.gl.objects.Game implements HandleTouchesListener {
	public class BasicTriangle extends Object {

		public BasicTriangle() {
			super(-1);
			this.mGame = GLObjectsTest2Activity.this;
		}

		@Override
		public void setGLStates(GL10 gl) {
			
	        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
	        gl.glEnable(GL10.GL_BLEND);
	        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_DST_ALPHA);
	        
	        mVertices.position(0);
	        gl.glVertexPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, mVertices);
	        mVertices.position(2);            
	        gl.glColorPointer(4, GL10.GL_FLOAT, VERTEX_SIZE, mVertices);

			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		}

		@Override
		public void unsetGLStates(GL10 gl) {
			gl.glDisable(GL10.GL_BLEND);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);	
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		}

		@Override
		public void doRender(GL10 gl) {
		}

		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onPause() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class Triangle extends Object {

		private float mX;
		private float mY;

		public Triangle(float x, float y) {
			super(0);
			mX = x;
			mY = y;
			this.mGame = GLObjectsTest2Activity.this;
		}

		@Override
		protected void setGLStates(GL10 gl) {
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
            gl.glTranslatef(mX, mY, 0);
            gl.glScalef(4, 4f, 0);
		}

		@Override
		protected void unsetGLStates(GL10 gl) {
		}

		@Override
		protected void doRender(GL10 gl) {
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		}

		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onPause() {
			// TODO Auto-generated method stub
			
		}

	}

	private BasicTriangle mBasicTriangle = null;
	private static final int DEFAULTDPI = 240;
	private static final int DEFAULTWIDTH = 30;//60;//120;//480;
	private static final int DEFAULTHEIGHT = 50;//100;//200;//800;
	private static final int VECTOR_COORDINATES = 2;
	private static final int VECTOR_COLOURCOMPONENTS = 4;
	private static final int VERTEX_SIZE =
			(VECTOR_COORDINATES + VECTOR_COLOURCOMPONENTS) * 4;
    private FloatBuffer mVertices =
    		ByteBuffer.allocateDirect(3 *
    				VERTEX_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private LinkedList<Finger> mFingersTouching = new LinkedList<Finger>();
	private boolean mHaveVertices = false;

	@Override
	protected void update(long deltaMillis) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setup() {
		Graphics.setClearColour(Color.BLACK);
		if (!mHaveVertices ) {
	        mVertices.put( new float[] {   -1.0f,   1.0f, 1, 0, 0, 1f,
	        		1.0f,   1.0f, 0, 1, 0, 1f,
	                0, -1.0f, 0, 0, 1, 1f});	
	        mHaveVertices = true;
		}
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

	private void processTouch(TouchEvent touchEvent) {

		if (touchEvent.mType == TouchEvent.UP) {
			mFingersTouching.remove(touchEvent.mFinger);
			addTriangle((float) touchEvent.mX, (float) touchEvent.mY);
		}
		if (touchEvent.mType == TouchEvent.DOWN) {
			if (mFingersTouching.size() < 2) {
				mFingersTouching.add(touchEvent.mFinger);
			}
		} else {
			if (mFingersTouching.size() < 2) {
				Finger finger = touchEvent.mFinger;
				float moveX = -(touchEvent.mX - finger.mX);
				float moveY = -(touchEvent.mY - finger.mY);
				Graphics.move(moveX,
						moveY, (float) 0);
			} else {
				Finger theOther = null;
				for (Finger finger : mFingersTouching) {
					if (finger != touchEvent.mFinger) {
						theOther = finger;
					}
				}
				float distanceBefore = calcDistance((float) theOther.mX, (float) theOther.mY,
						(float) touchEvent.mFinger.mX, (float) touchEvent.mFinger.mY);
				float distanceAfter = calcDistance((float) theOther.mX, (float) theOther.mY,
						(float) touchEvent.mX, (float) touchEvent.mY);
				float diffXBetweenTwo = Math.abs(touchEvent.mX - theOther.mX) / 2;
				float x = touchEvent.mX > theOther.mX ? touchEvent.mX - diffXBetweenTwo :
					theOther.mX - diffXBetweenTwo;
				float diffYBetweenTwo = Math.abs(touchEvent.mY - theOther.mY) / 2;
				float y = touchEvent.mY > theOther.mY ? touchEvent.mY - diffYBetweenTwo :
					theOther.mY - diffYBetweenTwo;
				Graphics.zoom(x, y, 0,
						(distanceAfter / distanceBefore));
			}
		}
	}

	private float calcDistance(float x1, float y1, float x2, float y2) {
		float distSquared = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
		return (float) Math.sqrt((double)distSquared);
	}

	private void addTriangle(float x, float y) {
		if (mBasicTriangle == null) {
			mBasicTriangle = new BasicTriangle();
			addToObjects(mBasicTriangle);
		}
		Triangle triangle = new Triangle(x, y);
		mBasicTriangle.addToObjects(triangle);
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
	protected int getMaxStreams() {
		// TODO Auto-generated method stub
		return 0;
	}
}
