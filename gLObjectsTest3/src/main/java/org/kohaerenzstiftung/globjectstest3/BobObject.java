package org.kohaerenzstiftung.globjectstest3;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.gl.objects.util.TexCoordValues;
import org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject;


public class BobObject extends VisibleTexObject {

	public static final int FACING_LEFT = 0;
	public static final int FACING_RIGHT = 1;
	private static final float HEIGHT = 1;
	private static final TexCoordValues[][][] mTexTexCoordValues = {
		{
			//BOB_FALL
			{
				//BOB_FACING_LEFT
				new TexCoordValues((float) 95 / (float) 512, (float) 128 / (float) 512, (float) 64 / (float) 512, (float) 159 / (float) 512),
				new TexCoordValues((float) 127 / (float) 512, (float) 128 / (float) 512, (float) 96 / (float) 512, (float) 159 / (float) 512)
			},
			{
				//BOB_FACING_RIGHT
				new TexCoordValues((float) 64 / (float) 512, (float) 128 / (float) 512, (float) 95 / (float) 512, (float) 159 / (float) 512),
				new TexCoordValues((float) 96 / (float) 512, (float) 128 / (float) 512, (float) 127 / (float) 512, (float) 159 / (float) 512)
			}
		},
		{
			//BOB_JUMP
			{
				//BOB_FACING_LEFT
				new TexCoordValues((float) 31 / (float) 512, (float) 128 / (float) 512, (float) 0 / (float) 512, (float) 159 / (float) 512),
				new TexCoordValues((float) 63 / (float) 512, (float) 128 / (float) 512, (float) 32 / (float) 512, (float) 159 / (float) 512)
				
			},
			{
				//BOB_FACING_RIGHT
				new TexCoordValues((float) 0 / (float) 512, (float) 128 / (float) 512, (float) 31 / (float) 512, (float) 159 / (float) 512),
				new TexCoordValues((float) 32 / (float) 512, (float) 128 / (float) 512, (float) 63 / (float) 512, (float) 159 / (float) 512)
			}
		},
		{
			//BOB_HIT
			{
				//BOB_FACING_LEFT
				new TexCoordValues((float) 159 / (float) 512, (float) 128 / (float) 512, (float) 128 / (float) 512, (float) 159 / (float) 512)
			},
			{
				//BOB_FACING_RIGHT
				new TexCoordValues((float) 128 / (float) 512, (float) 128 / (float) 512, (float) 159 / (float) 512, (float) 159 / (float) 512)
			}
		}
	};
	private static final float WIDTH = 1;
	private Bob mBob;
	private int mFacing;
	private int mLastFacing;
	private int mLastBobState;
	private int mLastState;
	private int mState = 0;
	private long mStateTime = 0;

	public void initialise() {
		this.mFacing = this.mBob.mVelocity.mX > 0 ?
				FACING_RIGHT : FACING_LEFT;
		this.mState = 0;
		this.mStateTime = 0;
		setTexCoords();
	}

	public BobObject(Bob bob) {
		super(1);
		
		this.mBob = bob;
	}

	@Override
	protected float getHeight() {
		return BobObject.HEIGHT;
	}

	@Override
	protected TexCoordValues getTexCoordValues() {
		this.mLastBobState = this.mBob.mState;
		this.mLastFacing = this.mFacing;
		this.mLastState = this.mState;
		return BobObject.mTexTexCoordValues[this.mLastBobState][this.mLastFacing][this.mLastState];
	}

	@Override
	protected float getWidth() {
		return BobObject.WIDTH;
	}

	@Override
	protected float getX() {
		return this.mBob.mX;
	}

	@Override
	protected float getY() {
		return this.mBob.mY;
	}

	@Override
	protected void preSetGLStates(GL10 gl) {
		while (this.mStateTime > 200) {
			this.mStateTime -= 200;
			this.mState++;
		}
		this.mState %= mTexTexCoordValues[this.mBob.mState][this.mFacing].length;

		this.mFacing = this.mBob.mVelocity.mX > 0 ? FACING_RIGHT :
			this.mBob.mVelocity.mX < 0 ? FACING_LEFT : this.mFacing;

		if ((this.mLastBobState != this.mBob.mState)||
			(this.mLastFacing != this.mFacing)||
			(this.mLastState != this.mState)) {
			setTexCoords();
		}
	}

	public void update(long deltaMillis) {
		this.mStateTime  += deltaMillis;
	}
}