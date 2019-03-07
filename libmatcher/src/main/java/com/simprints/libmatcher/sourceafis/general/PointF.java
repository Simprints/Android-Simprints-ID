package com.simprints.libmatcher.sourceafis.general;

public class PointF {

	public float X;
	public float Y;

	public PointF(float x, float y) {
		X = x;
		Y = y;
	}

	public static PointF toPointF(Point point) {
		return new PointF(point.X, point.Y);
	}

	public static PointF add(PointF left, SizeF right) {
		return new PointF(left.X + right.Width, left.Y + right.Height);
	}

	public static android.graphics.PointF toPoint(PointF point) {
		return new android.graphics.PointF(point.X, point.Y);
	}

}
