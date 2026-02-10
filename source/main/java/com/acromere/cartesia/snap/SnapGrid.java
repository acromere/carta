package com.acromere.cartesia.snap;

import com.acromere.cartesia.math.CadPoints;
import com.acromere.cartesia.tool.DesignTool;
import javafx.geometry.Point3D;

public class SnapGrid implements Snap {

	@Override
	public String getPromptKey() {
		return "snap-to-grid";
	}

	@Override
	public Point3D snap( DesignTool tool, Point3D point ) {
		if( point == null || point == CadPoints.NONE ) return CadPoints.NONE;
		return tool.getGridSystem().getNearest( tool.getWorkplane(), point );
	}

}
