package com.simprints.fingerprintmatcher.sourceafis.templates;

import com.simprints.fingerprintmatcher.sourceafis.general.Point;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Minutia implements Serializable {
		public Point Position=new Point(0,0);
		public byte Direction;
		public MinutiaType Type;
}
