package org.kohaerenzstiftung.gltest2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.ArrayFactory;
import org.kohaerenzstiftung.game.Finger;
import org.kohaerenzstiftung.game.Resolution;
import org.kohaerenzstiftung.game.TouchEvent;
import org.kohaerenzstiftung.game.gl.Graphics;
import org.kohaerenzstiftung.game.Game.HandleTouchesListener;

import android.graphics.Color;

public class GLTest2Activity extends org.kohaerenzstiftung.game.gl.Game implements HandleTouchesListener {

	public class Touch implements Factorisable {
		Finger mFinger = null;
		@Override
		public boolean equals(Object o) {
			Touch other = (Touch) o;
			return other.mFinger == this.mFinger;
		}
		public float mX;
		public float mY;
		@Override
		public Factorisable createInstance() {
			return new Touch();
		}
	}

	private static final int DEFAULTWIDTH = 30;//60;//120;//480;
	private static final int DEFAULTHEIGHT = 50;//100;//200;//800;
	private static final int DEFAULTDPI = 240;
	private static final int VECTOR_COORDINATES = 2;
	private static final int VECTOR_COLOURCOMPONENTS = 4;
	private static final int VERTEX_SIZE =
			(VECTOR_COORDINATES + VECTOR_COLOURCOMPONENTS) * 4;
	private Touch mTouches[];
	private ArrayFactory<Touch> mTouchFactory =
			new ArrayFactory<Touch>((mTouches =
			new Touch[TouchEvent.MAX_TOUCHEVENTS]),
			new Touch());

	private List<Touch> mTouchList = Collections.synchronizedList(new LinkedList<Touch>());
    private FloatBuffer vertices =
    		ByteBuffer.allocateDirect(3 *
    				VERTEX_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

	@Override
	protected void setup() {
		
		Graphics.setClearColour(Color.BLACK);
		
        vertices.put( new float[] {   -1.0f,   1.0f, 1, 0, 0, 0,
        		1.0f,   1.0f, 0, 1, 0, 0,
                0, -1.0f, 0, 0, 1, 0});
		
		GL10 gl = Graphics.mGl;
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        
        vertices.position(0);
        gl.glVertexPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, vertices);
        vertices.position(2);            
        gl.glColorPointer(4, GL10.GL_FLOAT, VERTEX_SIZE, vertices);
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
	protected void doRender(long deltaMillis) {
		GL10 gl = Graphics.mGl;
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		for (Touch touch : mTouchList) {
            gl.glLoadIdentity();
            gl.glTranslatef(touch.mX, touch.mY, 0);
            gl.glScalef(8, 8f, 0);
            gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		}
	}

	private void processTouch(TouchEvent touchEvent) {
		if (touchEvent.mType == TouchEvent.UP) {
			Touch touch = getTouch(touchEvent.mFinger);
			mTouchList.remove(touch);
			recycleTouch(touch);
			return;
		}
		if (touchEvent.mType == TouchEvent.DOWN) {
			Touch touch = getFreeTouch();
			touch.mFinger = touchEvent.mFinger;
			touch.mX = (float) touchEvent.mX;
			touch.mY = (float) touchEvent.mY;
			mTouchList.add(touch);
		} else {
			Touch touch = getTouch(touchEvent.mFinger);
			touch.mX = (float) touchEvent.mX;
			touch.mY = (float) touchEvent.mY;
		}
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
	
	private void recycleTouch(Touch me) {
		me.mFinger = null;
		mTouchFactory.recycle(me);
	}
	
	private Touch getFreeTouch() {
		return mTouchFactory.getFree();
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
