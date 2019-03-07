package com.simprints.libmatcher.sourceafis.general;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Point implements Serializable {
	public int X;
	public int Y;

	public Point(int x, int y) {
		X = x;
		Y = y;
	}

	public int hashCode() {
		//return X + Y;
		return (new Integer(X)).hashCode() + (new Integer(Y)).hashCode();

	}

	public boolean equals(Object other) {
		return other instanceof Point && this == other;
	}

	public static boolean isEqual(Point left, Point right) {
		return left.X == right.X && left.Y == right.Y;
	}

	public static boolean isNotEqual(Point left, Point right) {
		return left.X != right.X || left.Y != right.Y;
	}

	public static Point add(Point left, Size right) {
		return new Point(left.X + right.Width, left.Y + right.Height);
	}

	public static Point minus(Point left, Size right) {
		return new Point(left.X - right.Width, left.Y - right.Height);
	}

	public static android.graphics.Point toPoint(Point point) {
		return new android.graphics.Point(point.X, point.Y);
	}
}
