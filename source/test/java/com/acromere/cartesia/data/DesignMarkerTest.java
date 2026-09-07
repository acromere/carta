package com.acromere.cartesia.data;

import com.acromere.cartesia.test.Point3DAssert;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.acromere.cartesia.TestConstants.TOLERANCE;
import static org.assertj.core.api.Assertions.assertThat;

public class DesignMarkerTest extends DesignShapeTest {

	DesignMarkerTest() {
		super( new DesignMarker( new Point3D( 0, 0, 0 ), "1", DesignMarker.Type.DIAMOND ) );
	}

	@Test
	void testGetSteps() {
		// given
		DesignMarker point = new DesignMarker( new Point3D( 0, 0, 0 ), "1", DesignMarker.Type.DIAMOND );

		// when
		List<DesignPath.Step> steps = point.getSteps();

		// then
		assertThat( steps ).isNotEmpty();
		assertThat( steps.get( 0 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 0 ).data() ).isEqualTo( new double[]{ -0.5, 0 } );
		assertThat( steps.get( 1 ).command() ).isEqualTo( DesignPath.Command.L );
		assertThat( steps.get( 1 ).data() ).isEqualTo( new double[]{ 0.0, 0.5 } );
		assertThat( steps.get( 2 ).command() ).isEqualTo( DesignPath.Command.L );
		assertThat( steps.get( 2 ).data() ).isEqualTo( new double[]{ 0.5, 0 } );
		assertThat( steps.get( 3 ).command() ).isEqualTo( DesignPath.Command.L );
		assertThat( steps.get( 3 ).data() ).isEqualTo( new double[]{ 0.0, -0.5 } );
		assertThat( steps.get( 4 ).command() ).isEqualTo( DesignPath.Command.Z );
		assertThat( point.getSteps().size() ).isEqualTo( 5 );
	}

	@Test
	void testGetStepsWithOriginAndSize() {
		// given
		DesignMarker point = new DesignMarker( new Point3D( 1, 1, 0 ), "2", DesignMarker.Type.DIAMOND );

		// when
		List<DesignPath.Step> steps = point.getSteps();

		// then
		assertThat( steps ).isNotEmpty();
		assertThat( steps.get( 0 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 0 ).data() ).isEqualTo( new double[]{ 0.0, 1.0 } );
		assertThat( steps.get( 1 ).command() ).isEqualTo( DesignPath.Command.L );
		assertThat( steps.get( 1 ).data() ).isEqualTo( new double[]{ 1.0, 2.0 } );
		assertThat( steps.get( 2 ).command() ).isEqualTo( DesignPath.Command.L );
		assertThat( steps.get( 2 ).data() ).isEqualTo( new double[]{ 2.0, 1.0 } );
		assertThat( steps.get( 3 ).command() ).isEqualTo( DesignPath.Command.L );
		assertThat( steps.get( 3 ).data() ).isEqualTo( new double[]{ 1.0, 0.0 } );
		assertThat( steps.get( 4 ).command() ).isEqualTo( DesignPath.Command.Z );
		assertThat( point.getSteps().size() ).isEqualTo( 5 );
	}

	@Test
	void testModify() {
		DesignMarker point = new DesignMarker( new Point3D( 0, 0, 0 ) );
		assertThat( point.isModified() ).isTrue();
		point.setModified( false );
		assertThat( point.isModified() ).isFalse();

		point.setOrigin( new Point3D( 1, 1, 0 ) );
		assertThat( point.isModified() ).isTrue();
		point.setOrigin( new Point3D( 0, 0, 0 ) );
		assertThat( point.isModified() ).isFalse();

		point.setOrigin( new Point3D( 1, 1, 0 ) );
		assertThat( point.isModified() ).isTrue();
		point.setModified( false );
		assertThat( point.isModified() ).isFalse();
	}

	@Test
	void testOrigin() {
		DesignMarker point = new DesignMarker( new Point3D( 0, 0, 0 ) );
		assertThat( point.getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );

		point.setOrigin( new Point3D( 1, 2, 3 ) );
		assertThat( point.getOrigin() ).isEqualTo( new Point3D( 1, 2, 3 ) );
	}

	@Test
	void testDistanceTo() {
		assertThat( new DesignMarker( new Point3D( -2, 1, 0 ) ).distanceTo( new Point3D( 2, -2, 0 ) ) ).isCloseTo( 5.0, TOLERANCE );
	}

	@Test
	void testPathLength() {
		assertThat( new DesignMarker( new Point3D( -2, 1, 0 ) ).pathLength() ).isCloseTo( 0.0, TOLERANCE );
	}

	@Test
	void testGetVisualBounds() {
		// This is the geometrically correct bounds
		//assertThat( new DesignMarker( new Point3D( -2, 1, 0 ) ).getVisualBounds() ).isEqualTo( new BoundingBox( -2.5, 0.5, 1, 1 ) );

		// But this is what is computed by JavaFX
		assertThat( new DesignMarker( new Point3D( -2, 1, 0 ) ).getSelectBounds() ).isEqualTo( new BoundingBox( -3.0, 0.0, 2, 2 ) );
	}

	@Test
	void testCirclePath() {
		DesignMarker marker = new DesignMarker( new Point3D( 0, 0, 0 ) );
		marker.setType( DesignMarker.Type.CIRCLE.name() );

		assertThat( marker.getMarkerType() ).isEqualTo( DesignMarker.Type.CIRCLE.name().toLowerCase() );

		List<DesignPath.Step> steps = marker.getSteps();
		DesignPath.Step e0 = steps.getFirst();
		assertThat( e0.command() ).isEqualTo( DesignPath.Command.M );
		assertThat( e0.data() ).isEqualTo( new double[]{ 0.0, -0.5 } );
		DesignPath.Step e1 = steps.get( 1 );
		assertThat( e1.command() ).isEqualTo( DesignPath.Command.A );
		assertThat( e1.data() ).isEqualTo( new double[]{ 0.0, 0.5, 0.5, 0.5, 0, 0, 0 } );
		DesignPath.Step e2 = steps.get( 2 );
		assertThat( e2.command() ).isEqualTo( DesignPath.Command.A );
		assertThat( e2.data() ).isEqualTo( new double[]{ 0.0, -0.5, 0.5, 0.5, 0, 0, 0 } );
		DesignPath.Step e3 = steps.get( 3 );
		assertThat( e3.command() ).isEqualTo( DesignPath.Command.Z );
		assertThat( e3.data() ).isEqualTo( new double[]{} );
		assertThat( steps.size() ).isEqualTo( 4 );
	}

	@Test
	void testCgPath() {
		DesignMarker marker = new DesignMarker( new Point3D( 0, 0, 0 ), DesignMarker.Type.CG );
		List<DesignPath.Step> steps = marker.getSteps();
		assertThat( steps ).hasSize( 12 );

		// Circle
		assertThat( steps.get( 0 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 0 ).data() ).isEqualTo( new double[]{ 0.0, -0.5 } );
		assertThat( steps.get( 1 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 1 ).data() ).isEqualTo( new double[]{ 0.0, 0.5, 0.5, 0.5, 0, 0, 0 } );
		assertThat( steps.get( 2 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 2 ).data() ).isEqualTo( new double[]{ 0.0, -0.5, 0.5, 0.5, 0, 0, 0 } );
		assertThat( steps.get( 3 ).command() ).isEqualTo( DesignPath.Command.Z );

		// Sector 1
		assertThat( steps.get( 4 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 4 ).data() ).isEqualTo( new double[]{ 0.0, 0.0 } );
		assertThat( steps.get( 5 ).command() ).isEqualTo( DesignPath.Command.L );
		assertThat( steps.get( 5 ).data() ).isEqualTo( new double[]{ 0.0, -0.45 } );
		assertThat( steps.get( 6 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 6 ).data() ).isEqualTo( new double[]{ 0.45, 0.0, 0.45, 0.45, 0, 0, 1 } );
		assertThat( steps.get( 7 ).command() ).isEqualTo( DesignPath.Command.Z );

		// Sector 2
		assertThat( steps.get( 8 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 8 ).data() ).isEqualTo( new double[]{ 0.0, 0.0 } );
		assertThat( steps.get( 9 ).command() ).isEqualTo( DesignPath.Command.L );
		assertThat( steps.get( 9 ).data() ).isEqualTo( new double[]{ 0.0, 0.45 } );
		assertThat( steps.get( 10 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 10 ).data() ).isEqualTo( new double[]{ -0.45, 0.0, 0.45, 0.45, 0, 0, 1 } );
		assertThat( steps.get( 11 ).command() ).isEqualTo( DesignPath.Command.Z );
	}

	@Test
	void testRingPath() {
		DesignMarker marker = new DesignMarker( new Point3D( 0, 0, 0 ), DesignMarker.Type.RING );
		List<DesignPath.Step> steps = marker.getSteps();
		assertThat( steps ).hasSize( 8 );

		// Outer circle
		assertThat( steps.get( 0 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 0 ).data() ).isEqualTo( new double[]{ 0.0, -0.5 } );
		assertThat( steps.get( 1 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 1 ).data() ).isEqualTo( new double[]{ 0.0, 0.5, 0.5, 0.5, 0, 0, 0 } );
		assertThat( steps.get( 2 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 2 ).data() ).isEqualTo( new double[]{ 0.0, -0.5, 0.5, 0.5, 0, 0, 0 } );
		assertThat( steps.get( 3 ).command() ).isEqualTo( DesignPath.Command.Z );

		// Inner circle
		assertThat( steps.get( 4 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 4 ).data() ).isEqualTo( new double[]{ 0.0, -0.4 } );
		assertThat( steps.get( 5 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 5 ).data() ).isEqualTo( new double[]{ 0.0, 0.4, 0.4, 0.4, 0, 0, 1 } );
		assertThat( steps.get( 6 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 6 ).data() ).isEqualTo( new double[]{ 0.0, -0.4, 0.4, 0.4, 0, 0, 1 } );
		assertThat( steps.get( 7 ).command() ).isEqualTo( DesignPath.Command.Z );
	}

	@Test
	void testReticlePath() {
		DesignMarker marker = new DesignMarker( new Point3D( 0, 0, 0 ), DesignMarker.Type.RETICLE );
		List<DesignPath.Step> steps = marker.getSteps();
		assertThat( steps ).hasSize( 21 );

		double s = 0.1 * 0.2;
		double r = 0.5;
		double r1 = 0.5 * r + s;
		double r2 = 0.5 * r - s;

		// Cross
		assertThat( steps.get( 0 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 12 ).command() ).isEqualTo( DesignPath.Command.Z );

		// Outer Circle (r1 = 0.27)
		assertThat( steps.get( 13 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 13 ).data() ).isEqualTo( new double[]{ 0.0, -r1 } );
		assertThat( steps.get( 14 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 14 ).data() ).isEqualTo( new double[]{ 0.0, r1, r1, r1, 0, 0, 0 } );
		assertThat( steps.get( 15 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 15 ).data() ).isEqualTo( new double[]{ 0.0, -r1, r1, r1, 0, 0, 0 } );
		assertThat( steps.get( 16 ).command() ).isEqualTo( DesignPath.Command.Z );

		// Inner Circle (r2 = 0.23, sweep = 1)
		assertThat( steps.get( 17 ).command() ).isEqualTo( DesignPath.Command.M );
		assertThat( steps.get( 17 ).data() ).isEqualTo( new double[]{ 0.0, -r2 } );
		assertThat( steps.get( 18 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 18 ).data() ).isEqualTo( new double[]{ 0.0, r2, r2, r2, 0, 0, 1 } );
		assertThat( steps.get( 19 ).command() ).isEqualTo( DesignPath.Command.A );
		assertThat( steps.get( 19 ).data() ).isEqualTo( new double[]{ 0.0, -r2, r2, r2, 0, 0, 1 } );
		assertThat( steps.get( 20 ).command() ).isEqualTo( DesignPath.Command.Z );
	}

	@Test
	void testAllMarkerTypesProduceValidDesignPath() {
		for( DesignMarker.Type type : DesignMarker.Type.values() ) {
			DesignPath designPath = type.getDesignPath();
			assertThat( designPath ).isNotNull();
			assertThat( designPath.getSteps() ).isNotEmpty();
			assertThat( designPath.getSteps().getFirst().command() ).isEqualTo( DesignPath.Command.M );
		}
	}

	@Test
	void getReferencePoints() {
		// given
		DesignMarker marker = new DesignMarker( new Point3D( 5, -2, 0 ), DesignMarker.Type.CIRCLE );

		// when
		List<Point3D> points = marker.getReferencePoints();

		// then
		Point3DAssert.assertThat( points.getFirst() ).isCloseTo( new Point3D( 5, -2, 0 ) );
	}

}
