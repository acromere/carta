package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.data.DesignShape;
import javafx.geometry.Point3D;
import lombok.CustomLog;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Getter
@CustomLog
public class DesignToolV2ScreenPointSelectUIT extends DesignToolV2BaseUIT {

	@Test
	void screenPointSelect() throws Exception {
		// given
		useLineLayer();

		// when - select once
		Point3D mouse = getTool().worldToScreen( new Point3D( 0, 0, 0 ) );
		getTool().screenPointSelect( mouse, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignLine.class );
	}

	@Test
	void screenPointNotSelect() throws Exception {
		// given
		useLineLayer();
		usePathLayer();
		useMarkerLayer();

		// when - select once
		Point3D mouse = getTool().worldToScreen( new Point3D( 1, 0, 0 ) );
		getTool().screenPointSelect( mouse, false );

		// then - nothing should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 0 );
	}

	@Test
	void screenPointUnelect() throws Exception {
		// given
		useLineLayer();

		// when - select once
		Point3D mouse = getTool().worldToScreen( new Point3D( 0, 0, 0 ) );
		getTool().screenPointSelect( mouse, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignLine.class );

		// when - select once
		mouse = getTool().worldToScreen( new Point3D( 1, 0, 0 ) );
		getTool().screenPointSelect( mouse, false );

		// then - nothing should be selected
		selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 0 );
	}

	@Test
	// FIXME Flaky. Occasionally, comes back with no selection on second select
	void screenPointSelectWithMultipleSelectsMovingDownVisibleGeometry() throws Exception {
		// given
		useLineLayer();
		Point3D mouse = getTool().worldToScreen( new Point3D( 0, 0, 0 ) );

		// when - select once
		getTool().screenPointSelect( mouse, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignLine.class );
		assertThat( selected.getFirst().getOrigin() ).isEqualTo( new Point3D( -1, 1, 0 ) );

		// when - select again
		getTool().screenPointSelect( mouse, false );

		// then - the second line should be selected
		selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		// TODO Temporarily commented out until cascading point select is implemented
		//assertThat( selected.getFirst().getOrigin() ).isEqualTo( new Point3D( -1, -1, 0 ) );

		// when - select again
		getTool().screenPointSelect( mouse, false );

		// then - the first line should be selected again
		selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst().getOrigin() ).isEqualTo( new Point3D( -1, 1, 0 ) );
	}

}
