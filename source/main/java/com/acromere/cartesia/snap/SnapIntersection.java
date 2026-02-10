package com.acromere.cartesia.snap;

import com.acromere.cartesia.data.DesignShape;
import com.acromere.cartesia.math.CadIntersection;
import com.acromere.cartesia.math.CadPoints;
import com.acromere.cartesia.tool.DesignTool;
import javafx.geometry.Point3D;
import lombok.CustomLog;

import java.util.List;

@CustomLog
public class SnapIntersection implements Snap {

	@Override
	public String getPromptKey() {
		return "snap-to-intersection";
	}

	@Override
	public Point3D snap( DesignTool tool, Point3D point ) {
		if( point == null || point == CadPoints.NONE ) return CadPoints.NONE;

		Point3D mouse = tool.worldToScreen( point );
		List<DesignShape> shapes = tool.screenPointSyncFindAll( mouse );
		if( shapes.size() < 2 ) return CadPoints.NONE;

		DesignShape shape1 = shapes.get( 0 );
		DesignShape shape2 = shapes.get( 1 );
		List<Point3D> points = CadIntersection.getIntersections( shape1, shape2 );

		// Find the closest intersection point
		Point3D nearest = CadPoints.getNearest( point, points );

		return nearest == null ? CadPoints.NONE : nearest;
	}

}
