package com.acromere.cartesia.snap;

import com.acromere.cartesia.data.DesignEllipse;
import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.cartesia.math.CadGeometry;
import com.acromere.cartesia.math.CadPoints;
import com.acromere.cartesia.tool.DesignTool;
import javafx.geometry.Point3D;

import java.util.List;

public class SnapCenter implements Snap {

	@Override
	public String getPromptKey() {
		return "snap-to-center";
	}

	@Override
	public Point3D snap( DesignTool tool, Point3D point ) {
		if( point == null || point == CadPoints.NONE ) return CadPoints.NONE;

		Point3D mouse = tool.worldToScreen( point );
		List<DesignShape> shapes = tool.screenPointSyncFindOne( mouse );
		if( shapes.isEmpty() ) return CadPoints.NONE;

		DesignShape shape = shapes.getFirst();
		if( shape instanceof DesignLine line ) {
			return CadGeometry.midpoint( line.getOrigin(), line.getPoint() );
		} else if( shape instanceof DesignEllipse ellipse ) {
			return ellipse.getOrigin();
		}

		return CadPoints.NONE;
	}

}
