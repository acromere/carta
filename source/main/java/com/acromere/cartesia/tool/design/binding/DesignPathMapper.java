package com.acromere.cartesia.tool.design.binding;

import com.acromere.cartesia.data.DesignMarker;
import com.acromere.cartesia.data.DesignPath;
import javafx.scene.shape.*;
import lombok.CustomLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface DesignPathMapper {

	@Mapping( target = "" )
	default Path map( DesignPath designPath ) {
		Path path = new Path();
		path.getElements().setAll( designPath.getSteps().stream().map( this::map ).toList() );
		return path;
	}

	@Mapping( target = "" )
	default Path map( DesignPath designPath, double shapeScaleX, double shapeScaleY ) {
		Path path = new Path();
		path.getElements().setAll( designPath.getSteps().stream().map( step -> map( step, shapeScaleX, shapeScaleY ) ).toList() );
		return path;
	}

	@Mapping( target = "" )
	default Path update( DesignPath designPath, Path path, double shapeScaleX, double shapeScaleY ) {
		path.getElements().setAll( designPath.getSteps().stream().map( step -> map( step, shapeScaleX, shapeScaleY ) ).toList() );
		return path;
	}

	@Mapping( target = "" )
	default Path update( DesignMarker designMarker, Path path, double shapeScaleX, double shapeScaleY ) {
		path.getElements().setAll( designMarker.getSteps().stream().map( step -> map( step, shapeScaleX, shapeScaleY ) ).toList() );
		return path;
	}

	@Mapping( target = "" )
	default PathElement map( DesignPath.Step step ) {
		return map( step, 1, 1 );
	}

	@Mapping( target = "" )
	default PathElement map( DesignPath.Step step, double shapeScaleX, double shapeScaleY ) {
		return switch( step.command() ) {
			// step data is: x, y
			case M -> new MoveTo( step.data()[ 0 ] * shapeScaleX, step.data()[ 1 ] * shapeScaleY );
			// step data is: x, y, rx, ry, rotate, largeArc, sweep
			case A -> new ArcTo(
				step.data()[ 2 ] * shapeScaleX,
				step.data()[ 3 ] * shapeScaleY,
				step.data()[ 4 ],
				step.data()[ 0 ] * shapeScaleX,
				step.data()[ 1 ] * shapeScaleY,
				step.data()[ 5 ] > 0,
				step.data()[ 6 ] > 0
			);
			// step data is: bx, by, cx, cy, dx, dy
			case B -> new CubicCurveTo(
				step.data()[ 0 ] * shapeScaleX,
				step.data()[ 1 ] * shapeScaleY,
				step.data()[ 2 ] * shapeScaleX,
				step.data()[ 3 ] * shapeScaleY,
				step.data()[ 4 ] * shapeScaleX,
				step.data()[ 5 ] * shapeScaleY
			);
			// step data is: x, y
			case L -> new LineTo( step.data()[ 0 ] * shapeScaleX, step.data()[ 1 ] * shapeScaleY );
			// step data is: bx, by, cx, cy
			case Q -> new QuadCurveTo( step.data()[ 0 ] * shapeScaleX, step.data()[ 1 ] * shapeScaleY, step.data()[ 2 ] * shapeScaleX, step.data()[ 3 ] * shapeScaleY );
			// no step data
			case Z -> new ClosePath();
		};
	}

	@CustomLog
	class Log {}

}
