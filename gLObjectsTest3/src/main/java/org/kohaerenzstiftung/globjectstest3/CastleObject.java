package org.kohaerenzstiftung.globjectstest3;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.gl.objects.util.TexCoordValues;
import org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject;

public class CastleObject extends VisibleTexObject {

	private static final float HEIGHT = 2;

	private static final TexCoordValues mTexTexCoordValues =
			new TexCoordValues((float) 128 / (float) 512, (float) 64 / (float) 512, (float) 191 / (float) 512, (float) 127 / (float) 512);
	private static final float WIDTH = 2;

	private Castle mCastle;
	
	public void initialise() {
		setTexCoords();
	}

	public CastleObject(Castle castle) {
		super(1);
		this.mCastle = castle;
	}

	@Override
	protected float getHeight() {
		return CastleObject.HEIGHT;
	}

	@Override
	protected TexCoordValues getTexCoordValues() {
		return CastleObject.mTexTexCoordValues;
	}

	@Override
	protected float getWidth() {
		return CastleObject.WIDTH;
	}

	@Override
	protected float getX() {
		return this.mCastle.mX;
	}

	@Override
	protected float getY() {
		return this.mCastle.mY;
	}

	@Override
	protected void preSetGLStates(GL10 gl) {
	}

}
