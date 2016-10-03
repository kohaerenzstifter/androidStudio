package org.kohaerenzstiftung.globjectstest3;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.gl.objects.util.TexCoordValues;
import org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject;

public class SpringObject extends VisibleTexObject {

	private static final float HEIGHT = 1;
	private static final TexCoordValues mTexTexCoordValues =
			new TexCoordValues((float) 128 / (float) 512, (float) 0 / (float) 512, (float) 159 / (float) 512, (float) 31 / (float) 512);
	private static final float WIDTH = 1;
	private Spring mSpring;

	public void initialise() {
		setTexCoords();
	}

	public SpringObject(Spring spring) {
		super(1);
		this.mSpring = spring;
	}

	@Override
	protected float getHeight() {
		return SpringObject.HEIGHT;
	}

	@Override
	protected TexCoordValues getTexCoordValues() {
		return SpringObject.mTexTexCoordValues;
	}

	@Override
	protected float getWidth() {
		return SpringObject.WIDTH;
	}

	@Override
	protected float getX() {
		return this.mSpring.mX;
	}

	@Override
	protected float getY() {
		return this.mSpring.mY;
	}

	@Override
	protected void preSetGLStates(GL10 gl) {
	}

}
