package org.kohaerenzstiftung.globjectstest3;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.gl.objects.util.InvisibleTexObject;

public class Platform implements Factorisable {

	static final float HEIGHT = 0.5f;
	static final float WIDTH = 2;

	static final int TYPE_MOVING = 0;
	static int TYPE_STATIC = 1;
	
    static final float VELOCITY = 2;
    
	static final int STATE_NORMAL = 0;
	static final int STATE_PULVERISING = 1;

	private InvisibleTexObject mItemsInvisibleTexObject;
	private PlatformObject mObject;
	@SuppressWarnings("unused")
	private org.kohaerenzstiftung.game.gl.objects.Game mObjectsGame;
	float mX;
	float mY;
	int mType;
	private float mMinX;
	private float mMaxX;
	private float mVelocity;
	int mState;
	@SuppressWarnings("unused")
	private SharedContext mSharedContext;

	public void setValues(float x, float y,
			org.kohaerenzstiftung.game.gl.objects.Game objectsGame,
			int type, InvisibleTexObject invisibleTexObject, SharedContext sharedContext) {
		this.mX = x;
		this.mY = y;
		
		this.mMinX = -org.kohaerenzstiftung.globjectstest3.Game.FRUSTRUM_WIDTH / 2 +
				WIDTH / 2;
		this.mMaxX = org.kohaerenzstiftung.globjectstest3.Game.FRUSTRUM_WIDTH / 2 -
				WIDTH / 2;

		this.mVelocity = VELOCITY;

		this.mObjectsGame = objectsGame;
		mObject.setmGame(objectsGame);
		this.mItemsInvisibleTexObject = invisibleTexObject;
		this.mType = type;
		this.mState = STATE_NORMAL;
		this.mSharedContext = sharedContext;
		this.mObject.mSharedContext = sharedContext;
		
		this.mItemsInvisibleTexObject.addToObjects(this.mObject);

		this.mObject.initialise();
	}

	public void update(long deltaMillis) {
		if (deltaMillis >= 0) {
			if (this.mState == STATE_PULVERISING) {
				this.mObject.update(deltaMillis);
				return;
			}
			if (this.mType == TYPE_STATIC) {
				return;
			}
			if (this.mX < this.mMinX) {
				toggleDirection(1);
			} else if (this.mX > this.mMaxX) {
				toggleDirection(-1);				
			}
			float fraction = (float) deltaMillis / (float) 1000;
			float advanceX = (fraction * this.mVelocity);
			this.mX = (float) (this.mX + advanceX);
		}
	}

	private void toggleDirection(int factor) {
		this.mVelocity = factor * VELOCITY;
	}

	@Override
	public Factorisable createInstance() {
		return new Platform();
	}

	public void pulverize() {
		this.mState = STATE_PULVERISING;
		this.mObject.mStateTime = 0;
	}
	
	public Platform() {
		this.mObject = new PlatformObject(this);
	}

}