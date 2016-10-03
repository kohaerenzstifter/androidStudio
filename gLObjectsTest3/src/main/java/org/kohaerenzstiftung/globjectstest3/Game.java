package org.kohaerenzstiftung.globjectstest3;



import java.util.LinkedList;
import java.util.Random;

import org.kohaerenzstiftung.game.ListFactory;
import org.kohaerenzstiftung.game.Resolution;
import org.kohaerenzstiftung.game.Sound;
import org.kohaerenzstiftung.game.gl.objects.util.InvisibleTexObject;


public class Game extends org.kohaerenzstiftung.game.gl.objects.Game {

	static final int FRUSTRUM_WIDTH = 10;
	static final int FRUSTRUM_HEIGHT = 15;
	
	private Bob mBob;
	private ScoreText mScoreText;
	@SuppressWarnings("unused")
	private int mDensityDpi;
	private int mHeight;
	@SuppressWarnings("unused")
	private int mHeightPixels;
	@SuppressWarnings("unused")
	private int mWidthPixels;
	private InvisibleTexObject mItemsInvisibleTexObject;
	private int mWidth;
	@SuppressWarnings("unused")
	private Spring mSpring;
	@SuppressWarnings("unused")
	private Castle mCastle;

	private LinkedList<Spring> mSprings = new LinkedList<Spring>();
	private LinkedList<Squirrel> mSquirrels = new LinkedList<Squirrel>();
	private LinkedList<Coin> mCoins = new LinkedList<Coin>();
	private InvisibleTexObject mBackgroundInvisibleTexObject;
	@SuppressWarnings("unused")
	private Background mBackground;
	private SharedContext mSharedContext = new SharedContext();
	private Random mRandom;

	private ListFactory<Squirrel> mSquirrelFactory =
			new ListFactory<Squirrel>(new Squirrel());
	private ListFactory<Spring> mSpringFactory =
			new ListFactory<Spring>(new Spring());
	private ListFactory<Bob> mBobFactory =
			new ListFactory<Bob>(new Bob());
	private ListFactory<Background> mBackgroundFactory =
			new ListFactory<Background>(new Background());
	private Font mFont = new Font(this);
	ListFactory<ScoreText> mScoreTextFactory =
			new ListFactory<ScoreText>(new ScoreText(mFont));
	private Sound mCoinSound;
	@SuppressWarnings("unused")
	private Sound mClickSound;
	private Sound mHighJumpSound;
	private Sound mHitSound;
	private Sound mJumpSound;

	@Override
	protected AccelerationChangedListener getAccelerationChangedListener() {
		return new AccelerationChangedListener() {
			@Override
			public void handleAccelerationChanged(float x, float y, float z) {
				if (Game.this.mBob != null) {
					if (Game.this.mBob.mState != Bob.STATE_HIT) {
						Game.this.mBob.mVelocity.mX = -x / 10 * Bob.MOVE_VELOCITY;	
					}
				}
			}
		};
	}


	@Override
	protected HandleTouchesListener getHandleTouchesListener() {
		return null;
	}

	@Override
	protected Resolution getResolution(int widthPixels, int heightPixels,
			int densityDpi) {
		this.mWidth = Game.FRUSTRUM_WIDTH;
		this.mHeight = Game.FRUSTRUM_HEIGHT;

		this.mWidthPixels = widthPixels;
		this.mHeightPixels = heightPixels;
		this.mDensityDpi = densityDpi;

		Resolution result = new Resolution(this.mWidth, this.mHeight);

		return result;
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	protected void generateLevel() {
		this.mItemsInvisibleTexObject =
				new InvisibleTexObject(this, "items.png", true);
		this.mBackgroundInvisibleTexObject =
				new InvisibleTexObject(this, "background.png", true);

		float y = Platform.HEIGHT / 2;
	    float maxJumpHeight = Bob.JUMP_VELOCITY * Bob.JUMP_VELOCITY
	            / (2 * -SharedContext.GRAVITY);
	    while (y < SharedContext.WORLD_HEIGHT - SharedContext.WORLD_WIDTH / 2) {
	        int type = getRandfloat() > 0.8f ? Platform.TYPE_MOVING
	                : Platform.TYPE_STATIC;
	        float x = getRandfloat()
	                * (SharedContext.WORLD_WIDTH - Platform.WIDTH)
	                + Platform.WIDTH / 2;
	
	        Platform platform = getPlatform(type, x, y);
	        if (type == Platform.TYPE_MOVING) {
		        mSharedContext.mDynamicPlatforms.add(platform);
	        } else {
	        	mSharedContext.mStaticPlatforms.add(platform);
	        }
	
	        if (getRandfloat() > 0.9f
	                && type != Platform.TYPE_MOVING) {
	            Spring spring = getSpring(x,
	            		y + Platform.HEIGHT / 2
	                            + Spring.HEIGHT / 2);
	            this.mSprings.add(spring);
	        }
	
	        if (y > SharedContext.WORLD_HEIGHT / 3 && getRandfloat() > 0.8f) {
	            Squirrel squirrel = getSquirrel(x
	                    + getRandfloat(), y
	                    + Squirrel.HEIGHT + getRandfloat() * 2);
	            this.mSquirrels.add(squirrel);
	        }
	
	        if (getRandfloat() > 0.6f) {
	            Coin coin = getCoin(x + getRandfloat(),
	            		y + Coin.HEIGHT
	                            + getRandfloat() * 3);
	            this.mCoins.add(coin);
	        }
	
	        y += (maxJumpHeight - 0.5f);
	        y -= getRandfloat() * (maxJumpHeight / 3);
	    }
	    this.mBob = getBob(5, 1);
	    this.mBackground = getBackground();
	    this.mScoreText = getScoreText();
	}

	private Background getBackground() {
		
		Background result = this.mBackgroundFactory.getFree();
		result.setValues(0, mSharedContext,
				this, this.mBackgroundInvisibleTexObject);
		return result;
	}


	private Bob getBob(int x, int y) {
		Bob result = this.mBobFactory.getFree();
		x -= FRUSTRUM_WIDTH / 2;
		y -= FRUSTRUM_HEIGHT / 2;
		result.setValues(x, y, this.mJumpSound, this.mHighJumpSound, this.mHitSound, this, this.mItemsInvisibleTexObject, this.mSharedContext);
		return result;
	}

	private ScoreText getScoreText() {
		ScoreText result = this.mScoreTextFactory.getFree();
		result.setValues(mSharedContext);
		return result;
	}

	private Coin getCoin(float x, float y) {
		Coin result = mSharedContext.mCoinFactory.getFree();
		x -= FRUSTRUM_WIDTH / 2;
		y -= FRUSTRUM_HEIGHT / 2;
		result.setValues(x, y, this, this.mCoinSound, this.mItemsInvisibleTexObject, this.mSharedContext);
		return result;
	}

	private Squirrel getSquirrel(float x, float y) {
		Squirrel result = this.mSquirrelFactory.getFree();
		x -= FRUSTRUM_WIDTH / 2;
		y -= FRUSTRUM_HEIGHT / 2;
		result.setValues(x, y, this, this.mItemsInvisibleTexObject);
		return result;
	}


	private Spring getSpring(float x, float y) {
		Spring result = this.mSpringFactory.getFree();
		x -= FRUSTRUM_WIDTH / 2;
		y -= FRUSTRUM_HEIGHT / 2;
		result.setValues(x, y, this, this.mItemsInvisibleTexObject);
		return result;
	}


	private Platform getPlatform(int type, float x, float y) {
		Platform result = mSharedContext.PlatformFactory.getFree();
		x -= FRUSTRUM_WIDTH / 2;
		y -= FRUSTRUM_HEIGHT / 2;
		result.setValues(x, y, this, type, this.mItemsInvisibleTexObject, mSharedContext);
		return result;
	}


	private float getRandfloat() {
		if (this.mRandom == null) {
			this.mRandom = new Random();
		}
		return this.mRandom.nextFloat();
	}


	@Override
	protected void setup() {
		try {
			mCoinSound = getSound("coin.ogg");
			mClickSound = getSound("click.ogg");
			mHighJumpSound = getSound("highjump.ogg");
			mHitSound = getSound("hit.ogg");
			mJumpSound = getSound("jump.ogg");
		} catch (Exception e) {
			e.printStackTrace();
		}

		generateLevel();
	}

	@Override
	protected void update(long deltaMillis) {
		if (this.mBob != null) {
			this.mBob.update(deltaMillis);	
		}
		for (Platform platform : mSharedContext.mDynamicPlatforms) {
			platform.update(deltaMillis);
		}
		for (Platform platform : mSharedContext.mDynamicPlatformsRemove) {
			mSharedContext.mDynamicPlatforms.remove(platform);
		}
		mSharedContext.mDynamicPlatformsRemove.clear();
		for (Platform platform : mSharedContext.mStaticPlatformsRemove) {
			mSharedContext.mStaticPlatforms.remove(platform);
		}
		mSharedContext.mDynamicPlatformsRemove.clear();
		for (Squirrel squirrel : this.mSquirrels) {
			squirrel.update(deltaMillis);
		}
		for (Coin coin : this.mCoins) {
			coin.update(deltaMillis);
		}
		for (Coin coin : mSharedContext.mCoinsRemove) {
			mCoins.remove(coin);
		}
		mSharedContext.mCoinsRemove.clear();
		if (this.mBob != null) {
			if (this.mBob.mState != Bob.STATE_HIT) {
				if (this.mBob.mY <= this.mSharedContext.getmCurBobMinY()) {
					if (this.mSharedContext.getmCurBobMinY() == SharedContext.BOB_MIN_Y) {
						this.mBob.hitPlatform();
					} else {
						gameOver();
					}
				} else {
					float balance = this.mBob.mY - this.mSharedContext.getmVerticalCentre();
					if (balance > 0) {
						org.kohaerenzstiftung.game.gl.Graphics.move(0, balance, 0);
						mSharedContext.setmVerticalCentre(this.mBob.mY);
					}
				}
				this.mBob.mState = this.mBob.mVelocity.mY >= 0 ? Bob.STATE_JUMP : Bob.STATE_FALL;
				checkCollisions();
			}
		}
		if (this.mScoreText != null) {
			this.mScoreText.update();
		}
	}


	private void gameOver() {
		// TODO Auto-generated method stub
		
	}


	private void checkCollisions() {
	    checkPlatformCollisions();
	    checkSquirrelCollisions();
	    checkItemCollisions();
	    checkCastleCollisions();
	}


	private void checkCastleCollisions() {
		// TODO Auto-generated method stub
		
	}


	private void checkItemCollisions() {
		for (Coin coin : mCoins) {
        	if (this.mBob.overlapCoin(coin)) {
        		coin.collect();
                break;
        	}
		}

		if (this.mBob.mState == Bob.STATE_FALL) {
			for (Spring spring : mSprings) {
		        if (this.mBob.mY > spring.mY) {
		        	if (this.mBob.overlapSpring(spring)) {
		        		this.mBob.hitSpring();
		                break;		
		        	}
		        }
			}
		}
	}

	private void checkSquirrelCollisions() {
		for (Squirrel squirrel : mSquirrels) {
        	if (this.mBob.overlapSquirrel(squirrel)) {
        		this.mBob.hitSquirrel();
                break;		
        	}
		}
	}


	private void checkPlatformCollisions() {
		if (this.mBob.mState != Bob.STATE_FALL) {
			return;
		}
	    if (!checkPlatformCollisions(mSharedContext.mDynamicPlatforms)) {
		    checkPlatformCollisions(mSharedContext.mStaticPlatforms);	
	    }
	}


	private boolean checkPlatformCollisions(
			LinkedList<org.kohaerenzstiftung.globjectstest3.Platform> platforms) {
		boolean result = false;
		for (Platform platform : platforms) {
	        if (this.mBob.mY > platform.mY) {
	        	if (this.mBob.overlapPlatform(platform)) {
	        		this.mBob.hitPlatform();
	                if (getRandfloat() > 0.5f) {
	                	platform.pulverize();
	                }
	                result = true;
	                break;		
	        	}
	        }
		}
		return result;
	}


	@Override
	protected int getMaxStreams() {
		return 20;
	}
}
