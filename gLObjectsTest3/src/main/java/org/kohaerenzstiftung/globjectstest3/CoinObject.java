package org.kohaerenzstiftung.globjectstest3;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.gl.objects.util.TexCoordValues;
import org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject;


public class CoinObject extends VisibleTexObject {

	private static final float HEIGHT = 1;

	private static final TexCoordValues mTexTexCoordValues[] = {
		new TexCoordValues((float) 128 / (float) 512, (float) 32 / (float) 512, (float) 159 / (float) 512, (float) 63 / (float) 512),
		new TexCoordValues((float) 160 / (float) 512, (float) 32 / (float) 512, (float) 191 / (float) 512, (float) 63 / (float) 512),
		new TexCoordValues((float) 192 / (float) 512, (float) 32 / (float) 512, (float) 223 / (float) 512, (float) 63 / (float) 512),
		new TexCoordValues((float) 160 / (float) 512, (float) 32 / (float) 512, (float) 191 / (float) 512, (float) 63 / (float) 512)
	};

	private static final float WIDTH = 1;

	private Coin mCoin;
	private int mLastState;

	private int mState = 0;
	private long mStateTime = 0;
	
	public void initialise() {
		this.mState = 0;
		this.mStateTime = 0;
		setTexCoords();
	}

	public CoinObject(Coin coin) {
		super(1);
		
		this.mCoin = coin;
	}
	
	@Override
	protected void doRender(GL10 gl) {
		super.doRender(gl);
	}

	@Override
	protected float getHeight() {
		return CoinObject.HEIGHT;
	}

	@Override
	protected TexCoordValues getTexCoordValues() {
		this.mLastState = this.mState;
		return CoinObject.mTexTexCoordValues[this.mLastState];
	}

	@Override
	protected float getWidth() {
		return CoinObject.WIDTH;
	}

	@Override
	protected float getX() {
		return this.mCoin.mX;
	}

	@Override
	protected float getY() {
		return this.mCoin.mY;
	}

	@Override
	protected void preSetGLStates(GL10 gl) {
		while (this.mStateTime > 100) {
			this.mStateTime -= 100;
			this.mState++;
			this.mState %= mTexTexCoordValues.length;
		}

		if (this.mLastState != this.mState) {
			setTexCoords();
		}
	}

	@Override
	protected void unsetGLStates(GL10 gl) {
	}

	public void update(long deltaMillis) {
		this.mStateTime  += deltaMillis;
	}

}