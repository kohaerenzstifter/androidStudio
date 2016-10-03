package org.kohaerenzstiftung.globjectstest3;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.gl.objects.util.InvisibleTexObject;


public class Squirrel implements Factorisable {

	public static final int FACING_LEFT = 1;
	public static final int FACING_RIGHT = 0;
	static final float WIDTH = 1;
	static final float HEIGHT = 0.6f;
    static final float VELOCITY = 3f;

	int mFacing;
	private InvisibleTexObject mInvisibleTexObject;
	private SquirrelObject mObject;
	@SuppressWarnings("unused")
	private org.kohaerenzstiftung.game.gl.objects.Game mObjectsGame;

	public float mX;

	public float mY;
	private float mMaxX;
	private float mMinX;
	private float mVelocity;

	public void setValues(float x, float y,
			org.kohaerenzstiftung.game.gl.objects.Game objectsGame,
			InvisibleTexObject invisibleTexObject) {
		this.mX = x;
		this.mY = y;
		
		this.mMinX = -org.kohaerenzstiftung.globjectstest3.Game.FRUSTRUM_WIDTH / 2 +
			WIDTH / 2;
		this.mMaxX = org.kohaerenzstiftung.globjectstest3.Game.FRUSTRUM_WIDTH / 2 -
				WIDTH / 2;
		
		this.mVelocity = VELOCITY;

		this.mFacing = this.mVelocity > 0 ? FACING_RIGHT : FACING_LEFT;
		this.mObjectsGame = objectsGame;
		mObject.setmGame(objectsGame);
		this.mInvisibleTexObject = invisibleTexObject;

		this.mInvisibleTexObject.addToObjects(this.mObject);

		this.mObject.initialise();
	}

	private void toggleDirection(int factor) {
		this.mFacing = factor > 0 ? FACING_RIGHT : FACING_LEFT;
		this.mVelocity = factor * VELOCITY;
	}

	public void update(long deltaMillis) {
		if (deltaMillis >= 0) {
			if (this.mX < this.mMinX) {
				toggleDirection(1);
			} else if (this.mX > this.mMaxX) {
				toggleDirection(-1);				
			}

			float fraction = (float) deltaMillis / (float) 1000;
			float advanceX = (fraction * this.mVelocity);
			this.mX = (float) (this.mX + advanceX);
			this.mObject.update(deltaMillis);
		}
	}

	@Override
	public Factorisable createInstance() {
		return new Squirrel();
	}
	
	public Squirrel() {
		this.mObject = new SquirrelObject(this);
	}
}