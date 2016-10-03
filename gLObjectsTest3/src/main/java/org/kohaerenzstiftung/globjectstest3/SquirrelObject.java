package org.kohaerenzstiftung.globjectstest3;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.gl.objects.util.TexCoordValues;
import org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject;


public class SquirrelObject extends VisibleTexObject {

	private static final float HEIGHT = 1;
	private static TexCoordValues mTexTexCoordValues[][] = {
		{
			//SQUIRREL_FACING_RIGHT
			new TexCoordValues((float) 0 / (float) 512, (float) 160 / (float) 512, (float) 31 / (float) 512, (float) 191 / (float) 512),
			new TexCoordValues((float) 32 / (float) 512, (float) 160 / (float) 512, (float) 63 / (float) 512, (float) 191 / (float) 512)
		},
		{
			//SQUIRREL_FACING_LEFT
			new TexCoordValues((float) 31 / (float) 512, (float) 160 / (float) 512, (float) 0 / (float) 512, (float) 191 / (float) 512),
			new TexCoordValues((float) 63 / (float) 512, (float) 160 / (float) 512, (float) 32 / (float) 512, (float) 191 / (float) 512)
		}
	};
	public static final int SQUIRREL_WING_DOWN = 1;
	public static final int SQUIRREL_WING_UP = 0;

	private static final float WIDTH = 1;
	private int mLastFacing;

	private int mLastState;
	private Squirrel mSquirrel;
	private int mState = 0;
	private long mStateTime = 0;
	
	public void initialise() {
		mState = 0;
		mStateTime = 0;
		setTexCoords();
	}

	public SquirrelObject(Squirrel squirrel) {
		super(1);

		this.mSquirrel = squirrel;
	}

	@Override
	protected float getHeight() {
		return SquirrelObject.HEIGHT;
	}

	@Override
	protected TexCoordValues getTexCoordValues() {
		this.mLastFacing = this.mSquirrel.mFacing;
		this.mLastState = this.mState;
		return SquirrelObject.mTexTexCoordValues[this.mLastFacing][this.mLastState];
	}

	@Override
	protected float getWidth() {
		return SquirrelObject.WIDTH;
	}

	@Override
	protected float getX() {
		return this.mSquirrel.mX;
	}

	@Override
	protected float getY() {
		return this.mSquirrel.mY;
	}

	@Override
	protected void preSetGLStates(GL10 gl) {
		while (this.mStateTime > 100) {
			this.mStateTime -= 100;
			this.mState ++;
			this.mState %= mTexTexCoordValues[this.mSquirrel.mFacing].length;
		}

		if ((this.mLastFacing != this.mSquirrel.mFacing)||(this.mLastState != this.mState)) {
			setTexCoords();
		}
	}

	@Override
	protected void unsetGLStates(GL10 gl) {
	}

	public void update(long deltaMillis) {
		this.mStateTime += deltaMillis;
	}
}