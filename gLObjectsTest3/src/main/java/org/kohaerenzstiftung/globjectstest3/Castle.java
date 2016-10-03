package org.kohaerenzstiftung.globjectstest3;

import org.kohaerenzstiftung.game.gl.objects.Game;
import org.kohaerenzstiftung.game.gl.objects.util.InvisibleTexObject;

public class Castle {

	float mX;
	float mY;
	@SuppressWarnings("unused")
	private Game mObjectsGame;
	private InvisibleTexObject mInvisibleTexObject;
	private CastleObject mObject;

	public Castle(float x, float y,
			org.kohaerenzstiftung.game.gl.objects.Game objectsGame,
			InvisibleTexObject invisibleTexObject) {
		this.mX = x;
		this.mY = y;

		this.mObjectsGame = objectsGame;
		mObject.setmGame(objectsGame);
		this.mInvisibleTexObject = invisibleTexObject;

		this.mInvisibleTexObject.addToObjects(this.mObject);

		this.mObject.initialise();
	}
	
	public Castle() {
		this.mObject = new CastleObject(this);
	}

}
