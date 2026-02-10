package com.acromere.cartesia.snap;

import com.acromere.cartesia.data.DesignArc;
import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.cartesia.math.CadGeometry;
import com.acromere.cartesia.math.CadPoints;
import com.acromere.cartesia.tool.DesignTool;
import javafx.geometry.Point3D;

import java.util.List;

public class SnapMidpoint implements Snap {

	@Override
	public String getPromptKey() {
		return "snap-to-midpoint";
	}

	@Override
	public Point3D snap( DesignTool tool, Point3D point ) {
		if( point == null || point == CadPoints.NONE ) return CadPoints.NONE;

		List<DesignShape> shapes = tool.worldPointSyncFindOne( point );
		if( shapes.isEmpty() ) return CadPoints.NONE;

		DesignShape shape = shapes.getFirst();
		if( shape instanceof DesignLine line ) {
			return CadGeometry.midpoint( line.getOrigin(), line.getPoint() );
		} else if( shape instanceof DesignArc arc ) {
			return CadGeometry.midpoint( arc.getOrigin(), arc.getXRadius(), arc.getYRadius(), arc.calcRotate(), arc.getStart(), arc.getExtent() );
		}

		return CadPoints.NONE;
	}

}
