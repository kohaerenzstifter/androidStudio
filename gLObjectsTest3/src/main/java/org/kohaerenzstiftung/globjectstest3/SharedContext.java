package org.kohaerenzstiftung.globjectstest3;

import java.util.LinkedList;

import org.kohaerenzstiftung.game.ListFactory;

public class SharedContext {


	static final float GRAVITY = -12;
    static final float WORLD_WIDTH = 10;
    static final float WORLD_HEIGHT = 15 * 20;
	private float mVerticalCentre = 0;
    static final float BOB_MIN_Y = -Game.FRUSTRUM_HEIGHT / 2;
	private float mCurBobMinY = BOB_MIN_Y;
	int mScore = 0;
	
	public LinkedList<Coin> mCoinsRemove = new LinkedList<Coin>();
	public LinkedList<Platform> mStaticPlatforms = new LinkedList<Platform>();
	public LinkedList<Platform> mDynamicPlatforms = new LinkedList<Platform>();
	public LinkedList<Platform> mDynamicPlatformsRemove = new LinkedList<Platform>();
	public LinkedList<Platform> mStaticPlatformsRemove = new LinkedList<Platform>();
	public ListFactory<Coin> mCoinFactory =
			new ListFactory<Coin>(new Coin());
	public ListFactory<Platform> PlatformFactory =
			new ListFactory<Platform>(new Platform());
	
	public float getmCurBobMinY() {
		return mCurBobMinY;
	}

	public float getmVerticalCentre() {
		return mVerticalCentre;
	}

	public void setmVerticalCentre(float verticalCentre) {
		float balance = verticalCentre - mVerticalCentre;
		this.mVerticalCentre = verticalCentre;
		this.mCurBobMinY += balance;
	}

	void setValues() {
		mVerticalCentre = 0;
		mCurBobMinY = BOB_MIN_Y;
	}
}
