package org.kohaerenzstiftung.globjectstest;

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
import org.kohaerenzstiftung.game.gl.objects.Object;
import org.kohaerenzstiftung.game.Game.HandleTouchesListener;

import android.graphics.Color;

public class GLObjectsTestActivity extends org.kohaerenzstiftung.game.gl.objects.Game implements HandleTouchesListener {
	public class Rotation implements Factorisable {

		public Finger mFinger = null;
		public double mX;
		public double mY;
		public Triangle[] mTriangles = new Triangle[TRIANGLES_PER_ROTATION];
		@Override
		public Factorisable createInstance() {
			return new Rotation();
		}
	}

	public class BasicTriangle extends Object {

		public BasicTriangle(int level) {
			super(level);
			setmGame(GLObjectsTestActivity.this);
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

	public class Triangle extends BasicTriangle implements Factorisable {
		public Triangle(int level) {
			super(level);
			setmGame(GLObjectsTestActivity.this);
		}
		public double mX;
		public double mY;
		public int mDegrees;
		@Override
		public Factorisable createInstance() {
			return new Triangle(0);
		}
		@Override
		public void setGLStates(GL10 gl) {
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
            gl.glTranslatef((float) mX, (float) mY, 0);
            gl.glScalef(4, 4f, 0);
		}
		@Override
		public void unsetGLStates(GL10 gl) {
		}
		@Override
		public void doRender(GL10 gl) {
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		}
		public void calcPosition(double centerX, double centerY) {
			double radians = 2 * Math.PI * ((double) mDegrees / (double) 360);
			double distanceX = Math.sin(radians);
			double distanceY = Math.cos(radians);
			distanceX *= MAX_DISTANCE;
			distanceY *= MAX_DISTANCE;
			this.mX = centerX + (double) distanceX;
			this.mY = centerY + (double) distanceY;
		}
	}

	private static final double MAX_DISTANCE = 10;
	private static final int DEFAULTWIDTH = 30;//60;//120;//480;
	private static final int DEFAULTHEIGHT = 50;//100;//200;//800;
	private static final int DEFAULTDPI = 240;
	private static final int VECTOR_COORDINATES = 2;
	private static final int VECTOR_COLOURCOMPONENTS = 4;
	private static final int VERTEX_SIZE =
			(VECTOR_COORDINATES + VECTOR_COLOURCOMPONENTS) * 4;
	private static final int TRIANGLES_PER_ROTATION = 10;
	private static final int DEGREE_STRIDE = 360 / TRIANGLES_PER_ROTATION;

	private Triangle mTriangles[];
	private Rotation mRotations[];
	private ArrayFactory<Triangle> mTriangleFactory =
			new ArrayFactory<Triangle>((mTriangles =
			new Triangle[TouchEvent.MAX_TOUCHEVENTS * TRIANGLES_PER_ROTATION]),
			new Triangle(0));
	private ArrayFactory<Rotation> mRotationFactory =
			new ArrayFactory<Rotation>((mRotations =
			new Rotation[TouchEvent.MAX_TOUCHEVENTS]),
			new Rotation());
    private FloatBuffer mVertices =
    		ByteBuffer.allocateDirect(3 *
    				VERTEX_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private BasicTriangle mBasicTriangle = null;
	private Rotation mLastRotation;
	private boolean mZoomIn = true;
	private int mRenderCount = 0;
	private List<Rotation> mActiveRotations =
			Collections.synchronizedList(new LinkedList<Rotation>());
	
	@Override
	protected void update(long deltaMillis) {
		for (Rotation rotation : mActiveRotations) {
			for (Triangle triangle : rotation.mTriangles) {
				triangle.calcPosition(rotation.mX, rotation.mY);
				triangle.mDegrees += 3;
				triangle.mDegrees %= 360;
			}
		}	
	}

	@Override
	protected void setup() {
		Graphics.setClearColour(Color.BLACK);
        mVertices.put( new float[] {   -1.0f,   1.0f, 1, 0, 0, 1f,
        		1.0f,   1.0f, 0, 1, 0, 1f,
                0, -1.0f, 0, 0, 1, 1f});
	}

	@Override
	protected Resolution getResolution(int widthPixels, int heightPixels,
			int densityDpi) {
		double x = DEFAULTWIDTH;
		double y = DEFAULTHEIGHT;

		double factor = ((double) densityDpi) / ((double) DEFAULTDPI);
		
		x *= factor;
		y *= factor;

		Resolution result = new Resolution((int) x, (int) y);

		return result;
	}

	private void processTouch(TouchEvent touchEvent) {
		Rotation rotation;
		if (touchEvent.mType == TouchEvent.UP) {
			rotation = getRotation(touchEvent.mFinger);
			if (rotation == null) {
				//strange, I've seen this
				return;
			}
			if (mLastRotation == rotation) {
				mLastRotation = null;
			}
			removeRotation(rotation);
			mActiveRotations.remove(rotation);
			return;
		}
		if (touchEvent.mType == TouchEvent.DOWN) {
			rotation = getFreeRotation();
			rotation.mFinger = touchEvent.mFinger;
			rotation.mX = touchEvent.mX;
			rotation.mY = touchEvent.mY;
			putTriangles(rotation);
			addTriangles(rotation);
			mActiveRotations.add(rotation);
		} else {
			rotation = getRotation(touchEvent.mFinger);
			if (rotation == null) {
				//strange, I've seen this
				return;
			}
			rotation.mX = touchEvent.mX;
			rotation.mY = touchEvent.mY;
		}
		mLastRotation = rotation;
	}

	private void removeRotation(Rotation rotation) {
		Triangle triangles[] = rotation.mTriangles;
		for (int i = 0; i < TRIANGLES_PER_ROTATION; i++) {
			removeTriangle(triangles[i]);
			recycleTriangle(triangles[i]);
			triangles[i] = null;
		}
		recycleRotation(rotation);
	}

	private void addTriangles(Rotation rotation) {
		for (Triangle triangle : rotation.mTriangles) {
			addTriangle(triangle);
		}
	}

	private void putTriangles(Rotation rotation) {
		int degrees = 0;
		Triangle triangles[] = rotation.mTriangles;
		for (int i = 0; i < TRIANGLES_PER_ROTATION; i++) {
			triangles[i] = getFreeTriangle();
			triangles[i].mDegrees = degrees;
			degrees += DEGREE_STRIDE;
		}
	}

	private Rotation getFreeRotation() {
		return mRotationFactory.getFree();
	}

	private void addTriangle(Triangle triangle) {
		if (mBasicTriangle == null) {
			mBasicTriangle = new BasicTriangle(-1);
			addToObjects(mBasicTriangle);
		}
		mBasicTriangle.addToObjects(triangle);
	}

	private void removeTriangle(Triangle triangle) {
		try {
			triangle.removeFromContainer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Rotation getRotation(Finger finger) {
		Rotation result = null;
		for (int i = 0; i < mTriangles.length; i++) {
			if (mRotations[i].mFinger == finger) {
				result = mRotations[i];
				break;
			}
		}
		return result;
	}
	
	private void recycleTriangle(Triangle me) {
		mTriangleFactory.recycle(me);
	}
	
	private void recycleRotation(Rotation me) {
		me.mFinger = null;
		mRotationFactory.recycle(me);
	}
	
	private Triangle getFreeTriangle() {
		return mTriangleFactory.getFree();
	}

	@Override
	protected void doRender(long deltaMillis) {
		super.doRender(deltaMillis);
		mRenderCount++;
		if ((mRenderCount > 4)&&(mLastRotation != null)) {
			Graphics.zoom((float) mLastRotation.mX, (float) mLastRotation.mX,
					(float) 0, mZoomIn ? (float) 2 : (float) .5);
			mZoomIn = !mZoomIn;
			mRenderCount = 0;
		}
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
