package org.kohaerenzstiftung.globjectstest3;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.gl.objects.Game;
import org.kohaerenzstiftung.game.gl.objects.util.InvisibleTexObject;

public class Spring implements Factorisable {

	static final float HEIGHT = 0.3f;
	static final float WIDTH = 0.3f;
	float mX;
	float mY;
	@SuppressWarnings("unused")
	private Game mObjectsGame;
	private InvisibleTexObject mInvisibleTexObject;
	private SpringObject mObject;

	public void setValues(float x, float y,
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

	@Override
	public Factorisable createInstance() {
		return new Spring();
	}
	
	public Spring() {
		this.mObject = new SpringObject(this);
	}

}
