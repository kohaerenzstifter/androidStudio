package org.kohaerenzstiftung.globjectstest3;

import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.gl.objects.util.TexCoordValues;
import org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject;

public class BackgroundObject extends VisibleTexObject {

	private static TexCoordValues mTexCoordValues =
			new TexCoordValues(0, 0, (float) 319 / (float) 512, (float) 479 / (float) 512);
	private Background mBackground;

	public BackgroundObject(Background background) {
		super(0);
		this.mBackground = background;

		setTexCoords();
	}

	@Override
	protected float getHeight() {
		return Game.FRUSTRUM_HEIGHT;
	}

	@Override
	protected TexCoordValues getTexCoordValues() {
		return BackgroundObject.mTexCoordValues;
	}

	@Override
	protected float getWidth() {
		return Game.FRUSTRUM_WIDTH;
	}

	@Override
	protected float getX() {
		float result = this.mBackground.mX;
		return result;
	}

	@Override
	protected float getY() {
		float result = this.mBackground.mSharedContext.getmVerticalCentre();
		return result;
	}

	@Override
	protected void preSetGLStates(GL10 gl) {
	}

	public void initialise() {
		// TODO Auto-generated method stub
	}

}
