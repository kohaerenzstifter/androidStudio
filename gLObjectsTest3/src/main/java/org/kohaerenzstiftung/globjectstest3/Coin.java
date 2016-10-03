package org.kohaerenzstiftung.globjectstest3;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.Sound;
import org.kohaerenzstiftung.game.gl.objects.util.InvisibleTexObject;

public class Coin implements Factorisable {

	static final float HEIGHT = 0.8f;
	static final float WIDTH = 0.5f;
	private InvisibleTexObject mInvisibleTexObject;
	private CoinObject mObject;
	@SuppressWarnings("unused")
	private org.kohaerenzstiftung.game.gl.objects.Game mObjectsGame;
	float mX;
	float mY;
	private SharedContext mSharedContext;
	private Sound mCoinSound;
    private static final int SCORE = 10;

	public void setValues(float x, float y,
			org.kohaerenzstiftung.game.gl.objects.Game objectsGame,
			Sound coinSound,
			InvisibleTexObject invisibleTexObject,
			SharedContext sharedContext) {
		this.mX = x;
		this.mY = y;
		this.mCoinSound = coinSound;

		this.mObjectsGame = objectsGame;
		mObject.setmGame(objectsGame);
		this.mInvisibleTexObject = invisibleTexObject;

		this.mInvisibleTexObject.addToObjects(this.mObject);
		this.mSharedContext = sharedContext;

		this.mObject.initialise();
	}

	public void update(long deltaMillis) {
		if (deltaMillis >= 0) {
			this.mObject.update(deltaMillis);
		}
	}

	@Override
	public Factorisable createInstance() {
		return new Coin();
	}
	
	public Coin() {
		this.mObject = new CoinObject(this);
	}

	public void collect() {
		mCoinSound.play();
		try {
			this.mObject.removeFromContainer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mSharedContext.mScore += SCORE;
		mSharedContext.mCoinsRemove.add(this);
		mSharedContext.mCoinFactory.recycle(this);
	}

}