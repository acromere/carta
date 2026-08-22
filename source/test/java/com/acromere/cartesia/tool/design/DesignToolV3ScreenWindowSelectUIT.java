package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignBox;
import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.data.DesignPath;
import com.acromere.cartesia.data.DesignShape;
import javafx.geometry.Point3D;
import lombok.CustomLog;
import lombok.Getter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Getter
@CustomLog
@Disabled
public class DesignToolV3ScreenWindowSelectUIT extends DesignToolV3BaseUIT {

	@Test
	void screenWindowSelect() throws Exception {
		// given
		useBoxLayer();
		useLineLayer();
		usePathLayer();
		useMarkerLayer();
		Point3D origin = getTool().worldToScreen( new Point3D( -4.5, 4.5, 0 ) );
		Point3D mouse = getTool().worldToScreen( new Point3D( -1.5, 1.5, 0 ) );

		// when - select once
		getTool().screenWindowSelect( origin, mouse, false, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignPath.class );
	}

	@Test
	void screenWindowSelectContainedLines() throws Exception {
		// given
		useBoxLayer();
		useLineLayer();
		usePathLayer();
		useMarkerLayer();
		Point3D origin = getTool().worldToScreen( new Point3D( -1.5, -1.5, 0 ) );
		Point3D mouse = getTool().worldToScreen( new Point3D( 1.5, 1.5, 0 ) );

		// when - select once
		getTool().screenWindowSelect( origin, mouse, false, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 2 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignLine.class );
		assertThat( selected.get( 1 ) ).isInstanceOf( DesignLine.class );
	}

	@Test
	void screenWindowSelectIntersectedLines() throws Exception {
		// given
		//useBoxLayer();
		useLineLayer();
		//usePathLayer();
		//useMarkerLayer();
		Point3D origin = getTool().worldToScreen( new Point3D( -1.5, -0.5, 0 ) );
		Point3D mouse = getTool().worldToScreen( new Point3D( 1.5, 1.5, 0 ) );

		// when - select once
		getTool().screenWindowSelect( origin, mouse, true, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 2 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignLine.class );
		assertThat( selected.get( 1 ) ).isInstanceOf( DesignLine.class );
	}

	@Test
	void screenWindowDoNotSelectUncontainedLines() throws Exception {
		// given
		useBoxLayer();
		useLineLayer();
		usePathLayer();
		useMarkerLayer();
		Point3D origin = getTool().worldToScreen( new Point3D( -1.5, -0.5, 0 ) );
		Point3D mouse = getTool().worldToScreen( new Point3D( 1.5, 1.5, 0 ) );

		// when - select once
		getTool().screenWindowSelect( origin, mouse, false, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected ).isEmpty();
	}

	@Test
	void screenWindowSelectByIntersect() throws Exception {
		// given
		useBoxLayer();
		//useLineLayer();
		usePathLayer();
		//useMarkerLayer();
		Point3D origin = getTool().worldToScreen( new Point3D( -4.5, 4.5, 0 ) );
		Point3D mouse = getTool().worldToScreen( new Point3D( -1.5, 1.5, 0 ) );

		// when - select once
		getTool().screenWindowSelect( origin, mouse, true, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		//System.out.println( "selected=" + selected );
		assertThat( selected.size() ).isEqualTo( 2 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignBox.class );
		assertThat( selected.get( 1 ) ).isInstanceOf( DesignPath.class );
	}

	@Test
	void screenWindowSelectNone() throws Exception {
		// FIXME How is this different than the first test?
		// Should there be more to this test? Like selecting some geometry and then
		// selecting an empty window and asserting the selection clears?

		// given
		useBoxLayer();
		useLineLayer();
		usePathLayer();
		useMarkerLayer();
		Point3D origin = getTool().worldToScreen( new Point3D( -4.5, 4.5, 0 ) );
		Point3D mouse = getTool().worldToScreen( new Point3D( -1.5, 1.5, 0 ) );

		// when - select once
		getTool().screenWindowSelect( origin, mouse, false, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignPath.class );
	}

}
