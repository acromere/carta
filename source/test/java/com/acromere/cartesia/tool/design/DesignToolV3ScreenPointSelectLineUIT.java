package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.data.DesignShape;
import javafx.geometry.Point3D;
import javafx.scene.shape.Line;
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

		DesignLine line1 = (DesignLine)getLineLayer().getShapes().getFirst();
		Line fxLine1 = getTool().getRenderer().getFxGeometry( line1 );
		assertThat( fxLine1.getStartX() ).isEqualTo( -dpu );
		assertThat( fxLine1.getStartY() ).isEqualTo( dpu );
		assertThat( fxLine1.getEndX() ).isEqualTo( dpu );
		assertThat( fxLine1.getEndY() ).isEqualTo( -dpu );
	}

	@Test
	void screenPointSelectLine() throws Exception {
		// given
		assertThat( getTool().getSelectedShapes() ).hasSize( 0 );
		Point3D mouse = getTool().worldToScreen( new Point3D( 0, 0, 0 ) );

		// when - select once
		getTool().screenPointSelect( mouse, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignLine.class );
	}

	@Test
	void screenPointSelectLineWithMouseCloseEnough() throws Exception {
		// given
		assertThat( getTool().getSelectedShapes() ).hasSize( 0 );

		// Need to get the selector inside the stroke width of the line
		// 0.02 is just under half the line stroke width
		Point3D offset = new Point3D( 0.02 + getWorldSelectTolerance(), 0, 0 );
		Point3D point = new Point3D( 1, 1, 0 ).add( offset );
		Point3D mouse = getTool().worldToScreen( point );
		mouse = new Point3D( Math.round( mouse.getX() ), Math.round( mouse.getY() ), 0 );
		System.out.println( "screenMouse=" + mouse );

		// NEXT The selector radius ends up being very small for the operation
		// This should be somewhere around 25.1968503937008, but isn't
		// it is 0.63 screen units, which is smaller than a pixel

		// The height and width match at 762x514 (flaky)
		// CenterX and centerY for the select seems off as well

		// when
		getTool().screenPointSelect( mouse, false );

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
		Point3D offset = new Point3D( 0.03 + getWorldSelectTolerance() * 2, 0, 0 );
		Point3D point = new Point3D( 1, 1, 0 ).add( offset );
		Point3D mouse = getTool().worldToScreen( point );

		// when
		getTool().screenPointSelect( mouse, false );

		// then
		assertThat( getTool().getSelectedShapes() ).hasSize( 0 );
	}

}
