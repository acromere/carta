package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.zerra.javafx.Fx;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import lombok.CustomLog;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Getter
@CustomLog
public class DesignToolV3ScreenPointSelectLineUIT extends DesignToolV3BaseUIT {

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		useLineLayer();

		//		DesignLine line1 = (DesignLine)getLineLayer().getShapes().getFirst();
		//		Line fxLine1 = getTool().getRenderer().getFxGeometry( line1 );
		//		assertThat( fxLine1.getStartX() ).isEqualTo( -dpu );
		//		assertThat( fxLine1.getStartY() ).isEqualTo( dpu );
		//		assertThat( fxLine1.getEndX() ).isEqualTo( dpu );
		//		assertThat( fxLine1.getEndY() ).isEqualTo( -dpu );
	}

	@Test
	void screenPointSelectLine() throws Exception {
		// given
		assertThat( getTool().getSelectedShapes() ).hasSize( 0 );
		Point3D mouse = getTool().worldToScreen( new Point3D( 0, 0, 0 ) );

		// when - select once
		getTool().screenPointSelect( mouse, false );
		Fx.waitForStability( 1000 );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignLine.class );
	}

	@Test
	void screenPointSelectLineWithMouseCloseEnough() throws Exception {
		assertThat( tool ).isSameAs( getTool() );

		// given
		assertThat( getTool().getSelectedShapes() ).hasSize( 0 );

		double worldSelectTolerance = getWorldSelectTolerance();
		assertThat( getTool().getViewZoom() ).isEqualTo( 2 );
		assertThat( worldSelectTolerance ).isEqualTo( 0.1 );

		double apertureRadius = worldSelectTolerance;
		double radius = getTool().worldToScreen( new Point2D( apertureRadius, 0 ) ).add( -0.5 * width, 0 ).getX();

		// Guess I'll have to verify the other values.

		// Need to get the selector inside the stroke width of the line
		// 0.02 is just under half the line stroke width
		System.out.println( "World zoom=" + getTool().getViewZoom() );
		System.out.println( "World select tolerance=" + apertureRadius );
		System.out.println( "Select radius=" + radius );

		// Aperture radius ended up at 0.63 (very small), but should be 6.378.
		// Actual offset between 1.03 and 1.04 results in a match (way too small).
		// I would have expected that an offset of 1.06 would have worked because
		// that is inside the selector radius, but that didn't work either.

		Point3D offset = new Point3D( 0.02 + apertureRadius, 0, 0 );
		Point3D point = new Point3D( 1, 1, 0 ).add( offset );
		Point3D mouse = getTool().worldToScreen( point );
		//mouse = new Point3D( Math.round( mouse.getX() ), Math.round( mouse.getY() ), 0 );
		System.out.println( "screenMouse=" + mouse );
		System.out.println( "Select mouse=" + mouse.add( -0.5 * width, -0.5 * height, 0 ) );

		// NEXT The selector radius ends up being very small for the operation
		// This should be somewhere around 25.1968503937008, but isn't
		// it is 0.63 screen units, which is smaller than a pixel

		// The height and width match at 762x514 (flaky)
		// CenterX and centerY for the select seems off as well

		// when
		Fx.run( () -> getTool().screenPointSelect( mouse, false ) );
		Fx.waitForStability( 1000 );

		// then
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignLine.class );
	}

	@Test
	void screenPointSelectLineWithMouseTooFarAway() throws Exception {
		// given
		assertThat( getTool().getSelectedShapes() ).hasSize( 0 );

		// Need to get the selector outside the stroke width of the line
		// 0.03 is just over half the line stroke width
		Point3D offset = new Point3D( 0.03 + getWorldSelectTolerance(), 0, 0 );
		Point3D point = new Point3D( 1, 1, 0 ).add( offset );
		Point3D mouse = getTool().worldToScreen( point );

		// when
		getTool().screenPointSelect( mouse, false );
		Fx.waitForStability( 1000 );

		// then
		assertThat( getTool().getSelectedShapes() ).hasSize( 0 );
	}

}
