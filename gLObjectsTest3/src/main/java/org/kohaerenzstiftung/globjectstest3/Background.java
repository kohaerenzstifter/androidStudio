package org.kohaerenzstiftung.globjectstest3;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.gl.objects.Game;
import org.kohaerenzstiftung.game.gl.objects.util.InvisibleTexObject;

public class Background implements Factorisable {

	float mX;
	@SuppressWarnings("unused")
	private Game mObjectsGame;
	private InvisibleTexObject mInvisibleTexObject;
	private BackgroundObject mObject;
	SharedContext mSharedContext;

	public void setValues(float x, SharedContext sharedContext,
			org.kohaerenzstiftung.game.gl.objects.Game objectsGame,
			InvisibleTexObject invisibleTexObject) {
		this.mX = x;
		this.mSharedContext = sharedContext;
		
		this.mObjectsGame = objectsGame;
		mObject.setmGame(objectsGame);
		this.mInvisibleTexObject = invisibleTexObject;
		
		this.mObject.initialise();
		this.mInvisibleTexObject.addToObjects(this.mObject);
	}

	@Override
	public Factorisable createInstance() {
		return new Background();
	}
	
	public Background() {
		this.mObject = new BackgroundObject(this);
	}

}
