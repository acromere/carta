package com.acromere.cartesia.tool.design;

import com.acromere.annotation.Note;
import com.acromere.cartesia.test.Point3DAssert;
import com.acromere.cartesia.tool.Grid;
import com.acromere.marea.fx.FxRenderer2d;
import com.acromere.xenon.resource.Resource;
import com.acromere.zerra.javafx.Fx;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Note( "All V2 tests have been implemented in V3 as of 2026-07-11" )
@ExtendWith( MockitoExtension.class )
public class DesignToolV2Test extends BaseDesignToolTest {

	private static final double DPC = FxRenderer2d.DEFAULT_DPI / 2.54;

	private DesignToolV2 tool;

	@Mock
	protected Resource resource;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		Fx.run( () -> tool = new DesignToolV2( module, resource ) );
		Fx.waitForDangerously( 2, TimeUnit.SECONDS );
		setTool( tool );
	}

	@Test
	void testScreenToWorkplaneWithCoordinates() {
		// given
		tool.setDpi( FxRenderer2d.DEFAULT_DPI );
		assertThat( tool.isGridSnapEnabled() ).isTrue();
		assertThat( tool.getWorkplane().calcSnapGridX() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().calcSnapGridY() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().getGridSystem() ).isEqualTo( Grid.ORTHO );

		// then
		Point3DAssert.assertThat( tool.screenToWorkplane( 2 * DPC, -2 * DPC, 0 ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.screenToWorkplane( 2.001 * DPC, -2.002 * DPC, 0 ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.screenToWorkplane( 2.101 * DPC + 0.1, -2.101 * DPC, 0 ) ).isEqualTo( new Point3D( 2.1, 2.1, 0 ) );
	}

	@Test
	void testScreenToWorkplaneWithPoints() {
		// given
		tool.setDpi( FxRenderer2d.DEFAULT_DPI );
		assertThat( tool.isGridSnapEnabled() ).isTrue();
		assertThat( tool.getWorkplane().calcSnapGridX() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().calcSnapGridY() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().getGridSystem() ).isEqualTo( Grid.ORTHO );

		// then
		Point3DAssert.assertThat( tool.screenToWorkplane( new Point3D( 2 * DPC, -2 * DPC, 0 ) ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.screenToWorkplane( new Point3D( 2.001 * DPC, -2.001 * DPC, 0 ) ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.screenToWorkplane( new Point3D( 2.101 * DPC + 0.1, -2.101 * DPC, 0 ) ) ).isEqualTo( new Point3D( 2.1, 2.1, 0 ) );
	}

	@Test
	void testSnapToGridWithCoordinates() {
		// given
		assertThat( tool.isGridSnapEnabled() ).isTrue();
		assertThat( tool.getWorkplane().calcSnapGridX() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().calcSnapGridY() ).isEqualTo( 0.1 );

		// then
		Point3DAssert.assertThat( tool.snapToGrid( 2, 2, 0 ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.snapToGrid( 2.01, 2.01, 0 ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.snapToGrid( 2.101, 2.101, 0 ) ).isEqualTo( new Point3D( 2.1, 2.1, 0 ) );
	}

	@Test
	void testSnapToGridWithPoints() {
		// given
		assertThat( tool.isGridSnapEnabled() ).isTrue();
		assertThat( tool.getWorkplane().calcSnapGridX() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().calcSnapGridY() ).isEqualTo( 0.1 );

		// then
		Point3DAssert.assertThat( tool.snapToGrid( new Point3D( 2, 2, 0 ) ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.snapToGrid( new Point3D( 2.01, 2.01, 0 ) ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.snapToGrid( new Point3D( 2.101, 2.101, 0 ) ) ).isEqualTo( new Point3D( 2.1, 2.1, 0 ) );
	}

}
