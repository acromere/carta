package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignShape;
import com.acromere.cartesia.data.DesignText;
import javafx.geometry.Point3D;
import lombok.CustomLog;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Getter
@CustomLog
public class DesignToolV3ScreenPointSelectTextUIT extends DesignToolV3BaseUIT {

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		useTextLayer();
	}

	@Test
	void screenPointSelectText1WithMouseCloseEnough() {
		// given

		// Need to get the selector inside the fill of the text
		// No offset is just barely touching

		Point3D offset = new Point3D( 0, 0, 0 );
		Point3D point = new Point3D( -6, 5, 0 ).add( offset );
		Point3D mouse = getTool().worldToScreen( point );

		// when
		getTool().screenPointSelect( mouse, false );

		// then
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignText.class );
	}

	@Test
	void screenPointSelectText1WithMouseTooFarAway() {
		// given

		// Need to get the selector outside the fill of the text
		// -0.02 is enough to get it out of reach

		Point3D offset = new Point3D( 0, -0.02, 0 );
		Point3D point = new Point3D( -6, 5, 0 ).add( offset );
		Point3D mouse = getTool().worldToScreen( point );

		// when
		getTool().screenPointSelect( mouse, false );

		// then
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 0 );
	}

	@Test
	void screenPointSelectText2WithMouseCloseEnough() {
		// given

		// Need to get the selector inside the fill of the text
		// No offset is just barely touching

		Point3D offset = new Point3D( 0, 0, 0 );
		Point3D point = new Point3D( -6, -5, 0 ).add( offset );
		Point3D mouse = getTool().worldToScreen( point );

		// when
		getTool().screenPointSelect( mouse, false );

		// then
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignText.class );
	}

	@Test
	void screenPointSelectText2WithMouseTooFarAway() {
		// given

		// Need to get the selector outside the fill of the text
		// -0.05 is enough to get it out of reach

		Point3D offset = new Point3D( 0, -0.05, 0 );
		Point3D point = new Point3D( -6, -5, 0 ).add( offset );
		Point3D mouse = getTool().worldToScreen( point );

		// when
		getTool().screenPointSelect( mouse, false );

		// then
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 0 );
	}

}
