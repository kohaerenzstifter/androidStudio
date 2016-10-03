package org.kohaerenzstiftung.globjectstest3;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.Sound;
import org.kohaerenzstiftung.game.gl.objects.util.InvisibleTexObject;



public class Bob implements Factorisable {

	public static final int STATE_FALL = 0;
	public static final int STATE_HIT = 2;
	public static final int STATE_JUMP = 1;
    private static final float WIDTH = 0.8f;
    static final float HEIGHT = 0.8f;
	static final float JUMP_VELOCITY = 11;

	static final float MOVE_VELOCITY = 20;
	private InvisibleTexObject mInvisibleTexObject;
	int mState;
	private BobObject mObject;
	@SuppressWarnings("unused")
	private org.kohaerenzstiftung.game.gl.objects.Game mObjectsGame;
	float mX;
	float mY;
	Vector mVelocity = new Vector(0, JUMP_VELOCITY, 0);

	@SuppressWarnings("unused")
	private SharedContext mSharedContext;
	private Sound mJumpSound;
	private Sound mHighJumpSound;
	private Sound mHitSound;
	
	public void setValues(float x, float y,
			Sound jumpSound, Sound highJumpSound, Sound hitSound,
			org.kohaerenzstiftung.game.gl.objects.Game objectsGame,
			InvisibleTexObject invisibleTexObject, SharedContext sharedContext) {
		this.mX = x;
		this.mY = y;

		this.mObjectsGame = objectsGame;
		mObject.setmGame(objectsGame);
		this.mInvisibleTexObject = invisibleTexObject;

		this.mInvisibleTexObject.addToObjects(this.mObject);
		this.mSharedContext = sharedContext;
		this.mJumpSound = jumpSound;
		this.mHighJumpSound = highJumpSound;
		this.mHitSound = hitSound;
		
		this.mVelocity.mX = 0;
		this.mVelocity.mY = JUMP_VELOCITY;
		this.mVelocity.mZ = 0;

		this.mState = this.mVelocity.mY >= 0 ? STATE_JUMP : STATE_FALL;

		this.mObject.initialise();
	}
	public void update(long deltaMillis) {
		if (deltaMillis > 0) {

			float deltaSeconds = (float) deltaMillis / (float) 1000;
			this.mVelocity.add(0, SharedContext.GRAVITY * deltaSeconds, 0);

			this.mX += this.mVelocity.mX * deltaSeconds;
			this.mY += this.mVelocity.mY * deltaSeconds;
			this.mObject.update(deltaMillis);
			
			if (this.mX > Game.FRUSTRUM_WIDTH / 2) {
				float add = Game.FRUSTRUM_WIDTH / 2 - this.mX;
				this.mX = -Game.FRUSTRUM_WIDTH / 2 + add;
			} else if (this.mX < -Game.FRUSTRUM_WIDTH / 2) {
				float subtract = -Game.FRUSTRUM_WIDTH / 2 - this.mX;
				this.mX = Game.FRUSTRUM_WIDTH / 2 - subtract;
			}
		}
	}
	@Override
	public Factorisable createInstance() {
		return new Bob();
	}
	
	public boolean overlapSpring(Spring spring) {

		float otherHalfWidth = Spring.WIDTH / 2;
		float otherHalfHeight = Spring.HEIGHT / 2;

		return overlapRectangles(
				spring.mX - otherHalfWidth,
				spring.mY - otherHalfHeight,
				Spring.WIDTH, Spring.HEIGHT);
	}

	public boolean overlapPlatform(Platform platform) {

		float otherHalfWidth = Platform.WIDTH / 2;
		float otherHalfHeight = Platform.HEIGHT / 2;

		return overlapRectangles(
				platform.mX - otherHalfWidth,
				platform.mY - otherHalfHeight,
				Platform.WIDTH, Platform.HEIGHT);
	}
	
	public boolean overlapCoin(Coin coin) {

		float otherHalfWidth = Coin.WIDTH / 2;
		float otherHalfHeight = Coin.HEIGHT / 2;

		return overlapRectangles(
				coin.mX - otherHalfWidth,
				coin.mY - otherHalfHeight,
				Coin.WIDTH, Coin.HEIGHT);
	}

	public boolean overlapSquirrel(Squirrel squirrel) {

		float otherHalfWidth = Squirrel.WIDTH / 2;
		float otherHalfHeight = Squirrel.HEIGHT / 2;

		return overlapRectangles(
				squirrel.mX - otherHalfWidth,
				squirrel.mY - otherHalfHeight,
				Squirrel.WIDTH, Squirrel.HEIGHT);
	}

    public boolean overlapRectangles(
    		float r2LowerLeftX, float r2LowerLeftY,
    		float r2Width, float r2Height) {

		float myHalfWidth = Bob.WIDTH / 2;
		float myHalfHeight = Bob.HEIGHT / 2;
    	float r1LowerLeftX = this.mX - myHalfWidth;
    	float r1Width = Bob.WIDTH;
    	float r1Height = Bob.HEIGHT;
    	float r1LowerLeftY = this.mY - myHalfHeight;

        if (r1LowerLeftX < r2LowerLeftX + r2Width &&
        		r1LowerLeftX + r1Width > r2LowerLeftX &&
        		r1LowerLeftY < r2LowerLeftY + r2Height &&
        		r1LowerLeftY + r1Height > r2LowerLeftY)
            return true;
        else
            return false;
    }
    
	public Bob() {
		this.mObject = new BobObject(this);
	}

	public void hitSpring() {
		mHighJumpSound.play();
		this.mVelocity.mY = JUMP_VELOCITY * 1.5f;
		this.mState = STATE_JUMP;
		//TODO
        //listener.jump();
	}
	
	public void hitPlatform() {
		mJumpSound.play();
		this.mVelocity.mY = JUMP_VELOCITY;
		this.mState = STATE_JUMP;
		//TODO
        //listener.jump();
	}

	public void hitSquirrel() {
		mHitSound.play();
		this.mState = STATE_HIT;
		this.mVelocity.mX = 0;
		this.mVelocity.mY = 0;
	}
}