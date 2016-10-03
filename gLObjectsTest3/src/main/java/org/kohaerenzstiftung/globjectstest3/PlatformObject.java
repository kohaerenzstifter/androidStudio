package org.kohaerenzstiftung.globjectstest3;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.gl.objects.util.TexCoordValues;
import org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject;


public class PlatformObject extends VisibleTexObject {

	private static final float HEIGHT = 0.5f;
	private static final TexCoordValues[][] mTexTexCoordValues = {
		{
			new TexCoordValues((float) 64 / (float) 512, (float) 160 / (float) 512, (float) 127 / (float) 512, (float) 175 / (float) 512)
		},
		{
			new TexCoordValues((float) 64 / (float) 512, (float) 160 / (float) 512, (float) 127 / (float) 512, (float) 175 / (float) 512),
			new TexCoordValues((float) 64 / (float) 512, (float) 176 / (float) 512, (float) 127 / (float) 512, (float) 191 / (float) 512),
			new TexCoordValues((float) 64 / (float) 512, (float) 192 / (float) 512, (float) 127 / (float) 512, (float) 207 / (float) 512),
			new TexCoordValues((float) 64 / (float) 512, (float) 208 / (float) 512, (float) 127 / (float) 512, (float) 223 / (float) 512)	
		}
	};
	private static final float WIDTH = 2;
	private int mLastPlatformState;
	private int mLastState;
	private Platform mPlatform;
	private int mState = 0;
	long mStateTime = 0;
	SharedContext mSharedContext;

	public void initialise() {
		mState = 0;
		mStateTime = 0;
		setTexCoords();
	}

	public PlatformObject(Platform platform) {
		super(1);

		this.mPlatform = platform;
	}

	@Override
	protected float getHeight() {
		return PlatformObject.HEIGHT;
	}

	@Override
	protected TexCoordValues getTexCoordValues() {
		this.mLastState = this.mState;
		this.mLastPlatformState = this.mPlatform.mState;
		return PlatformObject.mTexTexCoordValues[this.mLastPlatformState][this.mLastState];
	}

	@Override
	protected float getWidth() {
		return PlatformObject.WIDTH;
	}

	@Override
	protected float getX() {
		return this.mPlatform.mX;
	}

	@Override
	protected float getY() {
		return this.mPlatform.mY;
	}

	@Override
	protected void preSetGLStates(GL10 gl) {
		while (this.mStateTime > 200) {
			this.mStateTime -= 200;
			this.mState++;
			this.mState %= mTexTexCoordValues[this.mLastPlatformState].length;
			if ((this.mLastPlatformState == Platform.STATE_PULVERISING) && (this.mState == 0)) {
				try {
					this.removeFromContainer();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (this.mPlatform.mType == Platform.TYPE_MOVING) {
					mSharedContext.mDynamicPlatformsRemove.add(this.mPlatform);
				} else {
					mSharedContext.mStaticPlatformsRemove.add(this.mPlatform);
				}
				mSharedContext.PlatformFactory.recycle(this.mPlatform);
			}
		}

		if ((this.mLastState != this.mState)||(this.mLastPlatformState != this.mPlatform.mState)) {
			setTexCoords();
		}
	}

	public void update(long deltaMillis) {
		this.mStateTime += deltaMillis;
	}

}