package org.kohaerenzstiftung.globjectstest3;

public class Vector {

	float mX;
	float mY;
	float mZ;
	public Vector(float x, float y, float z) {
		this.mX = x;
		this.mY = y;
		this.mZ = z;
	}
	public void add(float x, float y, float z) {
        this.mX += x;
        this.mY += y;
		this.mZ += z;
	}
}