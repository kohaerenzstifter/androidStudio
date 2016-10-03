package org.kohaerenzstiftung.globjectstest3;


import javax.microedition.khronos.opengles.GL10;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.ListFactory;
import org.kohaerenzstiftung.game.gl.objects.Game;
import org.kohaerenzstiftung.game.gl.objects.util.TexCoordValues;

public class Font extends org.kohaerenzstiftung.game.gl.objects.util.Font {

	private TexCoordValues[] mTexCoordValues = new TexCoordValues[128];

	public class VisibleTexObject
		extends org.kohaerenzstiftung.game.gl.objects.util.Font.VisibleTexObject {

		@Override
		public Factorisable createInstance() {
			return new VisibleTexObject();
		}

		@Override
		protected TexCoordValues getTexCoordValues(char character) {
			if (character < ' ') {
				character = ' ';
			}
			return Font.this.getTexCoordValues(character);
		}

		@Override
		protected void preSetGLStates(GL10 gl) {
		}
	}

	public Font(Game game) {
		super(game, "items.png");
	}

	public TexCoordValues getTexCoordValues(char character) {
		if (mTexCoordValues[character] == null) {
			int index = character - ' ';
			int row = index / 16;
			int column = index % 16;
			float lowX = 226 + (16 * column);
			float lowY = 0 + (20 * row);
			float highX = lowX +16;
			float highY = lowY + 20;
			
			lowX /= 512;
			lowY /= 512;
			highX /= 512;
			highY /= 512;

			mTexCoordValues[character] = new TexCoordValues(lowX, lowY, highX, highY);
		}
		return mTexCoordValues[character];
	}

	@Override
	protected ListFactory<org.kohaerenzstiftung.game.gl.objects.util.Font.VisibleTexObject> getVisibleTexObjectFactory() {
		return new ListFactory<org.kohaerenzstiftung.game.gl.objects.util.Font.VisibleTexObject>(new VisibleTexObject());
	}

}
