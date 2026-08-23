package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.zerra.javafx.Fx;
import javafx.geometry.Point3D;
import lombok.CustomLog;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.acromere.cartesia.TestTimeouts.FX_STABILITY_TIMEOUT;
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
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

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
		assertThat( worldSelectTolerance ).isEqualTo( 0.1 );

		Point3D offset = new Point3D( 0.02 + worldSelectTolerance, 0, 0 );
		Point3D point = new Point3D( 1, 1, 0 ).add( offset );
		Point3D mouse = getTool().worldToScreen( point );

		// when
		Fx.run( () -> getTool().screenPointSelect( mouse, false ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

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
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		// then
		assertThat( getTool().getSelectedShapes() ).hasSize( 0 );
	}

}
