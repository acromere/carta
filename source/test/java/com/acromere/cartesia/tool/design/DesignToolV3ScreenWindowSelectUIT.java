package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignBox;
import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.data.DesignPath;
import com.acromere.cartesia.data.DesignShape;
import javafx.geometry.Point3D;
import lombok.CustomLog;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Getter
@CustomLog
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
	void screenWindowNotSelect() throws Exception {
		// given
		useBoxLayer();
		useLineLayer();
		usePathLayer();
		useMarkerLayer();

		// when - select none
		Point3D origin = getTool().worldToScreen( new Point3D( 6, 6, 0 ) );
		Point3D mouse = getTool().worldToScreen( new Point3D( 7, 7, 0 ) );
		getTool().screenWindowSelect( origin, mouse, false, false );

		// then - nothing should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected ).isEmpty();
	}

	@Test
	void screenWindowUnelect() throws Exception {
		// given
		useBoxLayer();
		useLineLayer();
		usePathLayer();
		useMarkerLayer();

		// when - select once
		Point3D origin = getTool().worldToScreen( new Point3D( -4.5, 4.5, 0 ) );
		Point3D mouse = getTool().worldToScreen( new Point3D( -1.5, 1.5, 0 ) );
		getTool().screenWindowSelect( origin, mouse, false, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 1 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignPath.class );

		// when - select none
		origin = getTool().worldToScreen( new Point3D( 6, 6, 0 ) );
		mouse = getTool().worldToScreen( new Point3D( 7, 7, 0 ) );
		getTool().screenWindowSelect( origin, mouse, false, false );

		// then - nothing should be selected
		selected = getTool().getSelectedShapes();
		assertThat( selected ).isEmpty();
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
		useBoxLayer();
		useLineLayer();
		usePathLayer();
		useMarkerLayer();
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
		useLineLayer();
		usePathLayer();
		useMarkerLayer();
		Point3D origin = getTool().worldToScreen( new Point3D( -4.5, 4.5, 0 ) );
		Point3D mouse = getTool().worldToScreen( new Point3D( -1.5, 1.5, 0 ) );

		// when - select once
		getTool().screenWindowSelect( origin, mouse, true, false );

		// then - the first line should be selected
		List<DesignShape> selected = getTool().getSelectedShapes();
		assertThat( selected.size() ).isEqualTo( 2 );
		assertThat( selected.getFirst() ).isInstanceOf( DesignBox.class );
		assertThat( selected.get( 1 ) ).isInstanceOf( DesignPath.class );
	}

}
