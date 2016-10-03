package org.kohaerenzstiftung.globjectstest3;

import java.util.LinkedList;

import org.kohaerenzstiftung.game.Factorisable;
import org.kohaerenzstiftung.game.gl.objects.util.VisibleTexObject;

public class ScoreText implements Factorisable {
	
	private Font mFont = null;
	private SharedContext mSharedContext;
	private StringBuffer mStringBuffer = new StringBuffer();
	private LinkedList<VisibleTexObject> mObjectsList =
			new LinkedList<VisibleTexObject>();
	private float mGlyphHeight = 1;
	private float mGlyphWidth = 1;

	public ScoreText(Font font) {
		this.mFont = font;
	}

	@Override
	public Factorisable createInstance() {
		return new ScoreText(mFont);
	}

	public void setValues(SharedContext sharedContext) {
		this.mSharedContext = sharedContext;
		refresh(sharedContext.mScore);
	}

	private void refresh(int score) {
		mStringBuffer.delete(0, mStringBuffer.length());
		mStringBuffer.insert(0, score);
		try {
			mFont.recycleVisibleTexObjects(mObjectsList);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			mFont.getObjects(mStringBuffer, mGlyphWidth  ,mGlyphHeight ,
					Font.ALIGN_LEFT, 0.2f,
					Font.ALIGN_TOP, 0.2f, 2, mObjectsList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update() {
		refresh(mSharedContext.mScore);
	}

}
