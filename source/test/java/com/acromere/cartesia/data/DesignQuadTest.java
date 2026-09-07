package com.acromere.cartesia.data;

import com.acromere.cartesia.math.CadConstants;
import com.acromere.cartesia.math.CadTransform;
import com.acromere.cartesia.test.Point3DAssert;
import javafx.geometry.Point3D;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.acromere.cartesia.TestConstants.TOLERANCE;
import static org.assertj.core.api.Assertions.assertThat;

public class DesignQuadTest extends DesignShapeTest {

	DesignQuadTest() {
		super( new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0, 1, 0 ), new Point3D( 1, 0, 0 ) ) );
	}

	@Test
	void testGetType() {
		DesignQuad quad = new DesignQuad();
		assertThat( quad.getType() ).isEqualTo( DesignShape.Type.QUAD );
	}

	@Test
	void testDistanceTo() {
		DesignQuad quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0.5, 1.0, 0 ), new Point3D( 1, 0, 0 ) );
		assertThat( quad.distanceTo( new Point3D( 0.5, 1.0, 0 ) ) ).isEqualTo( 0.5 );
		assertThat( quad.distanceTo( new Point3D( 0.5, 0.5, 0 ) ) ).isEqualTo( 0.0 );
		assertThat( quad.distanceTo( new Point3D( 0, 0, 0 ) ) ).isEqualTo( 0.0 );
		assertThat( quad.distanceTo( new Point3D( 1, 0, 0 ) ) ).isEqualTo( 0.0 );
	}

	@Test
	void testModify() {
		DesignQuad quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0, 0, 0 ), new Point3D( 0, 0, 0 ) );
		assertThat( quad.isModified() ).isTrue();
		quad.setModified( false );
		assertThat( quad.isModified() ).isFalse();

		quad.setOrigin( new Point3D( 0, 0, 0 ) );
		quad.setPoint( new Point3D( 0, 0, 0 ) );
		assertThat( quad.isModified() ).isFalse();

		quad.setOrigin( new Point3D( 1, 1, 0 ) );
		assertThat( quad.isModified() ).isTrue();
		quad.setModified( false );
		assertThat( quad.isModified() ).isFalse();

		quad.setPoint( new Point3D( 2, 2, 0 ) );
		assertThat( quad.isModified() ).isTrue();
		quad.setModified( false );
		assertThat( quad.isModified() ).isFalse();
	}

	@Test
	void testOrigin() {
		DesignQuad quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0, 0, 0 ), new Point3D( 0, 0, 0 ) );
		assertThat( quad.getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );

		quad.setOrigin( new Point3D( 1, 2, 3 ) );
		assertThat( quad.getOrigin() ).isEqualTo( new Point3D( 1, 2, 3 ) );
	}

	@Test
	void testControlPoint() {
		DesignQuad quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0, 0, 0 ), new Point3D( 0, 0, 0 ) );
		assertThat( quad.getControl() ).isEqualTo( new Point3D( 0, 0, 0 ) );

		quad.setControl( new Point3D( 1, 2, 3 ) );
		assertThat( quad.getControl() ).isEqualTo( new Point3D( 1, 2, 3 ) );
	}

	@Test
	void testPoint() {
		DesignQuad quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0, 0, 0 ), new Point3D( 0, 0, 0 ) );
		assertThat( quad.getPoint() ).isEqualTo( new Point3D( 0, 0, 0 ) );

		quad.setPoint( new Point3D( 1, 2, 3 ) );
		assertThat( quad.getPoint() ).isEqualTo( new Point3D( 1, 2, 3 ) );
	}

	@Test
	void testPathLength() {
		DesignQuad quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0.5, 0.5, 0 ), new Point3D( 1, 0, 0 ) );
		assertThat( quad.pathLength() ).isEqualTo( 1.274307417012654, Offset.offset( CadConstants.RESOLUTION_LENGTH ) );

		quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0, 1, 0 ), new Point3D( 1, 1, 0 ) );
		assertThat( quad.pathLength() ).isEqualTo( 1.802142831771924, Offset.offset( CadConstants.RESOLUTION_LENGTH ) );
	}

	@Test
	void testToMap() {
		DesignQuad quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0.5, 0.5, 0 ), new Point3D( 1, 0, 0 ) );
		Map<String, Object> map = quad.asMap();

		assertThat( map.get( DesignQuad.SHAPE ) ).isEqualTo( DesignQuad.QUAD );
		assertThat( map.get( DesignQuad.ORIGIN ) ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( map.get( DesignQuad.CONTROL ) ).isEqualTo( new Point3D( 0.5, 0.5, 0 ) );
		assertThat( map.get( DesignQuad.POINT ) ).isEqualTo( new Point3D( 1, 0, 0 ) );
	}

	@Test
	void testUpdateFrom() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignQuad.SHAPE, DesignQuad.QUAD );
		map.put( DesignQuad.ORIGIN, "0,0,0" );
		map.put( DesignQuad.CONTROL, "0.5,0.5,0" );
		map.put( DesignQuad.POINT, "1,0,0" );

		DesignQuad quad = new DesignQuad();
		quad.updateFrom( map );

		assertThat( quad.getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( quad.getControl() ).isEqualTo( new Point3D( 0.5, 0.5, 0 ) );
		assertThat( quad.getPoint() ).isEqualTo( new Point3D( 1, 0, 0 ) );
	}

	@Test
	void testUpdateFromWithPoint3D() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignQuad.SHAPE, DesignQuad.QUAD );
		map.put( DesignQuad.ORIGIN, "0,0,0" );
		map.put( DesignQuad.CONTROL, new Point3D( 0.5, 0.5, 0 ) );
		map.put( DesignQuad.POINT, new Point3D( 1, 0, 0 ) );

		DesignQuad quad = new DesignQuad();
		quad.updateFrom( map );

		assertThat( quad.getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( quad.getControl() ).isEqualTo( new Point3D( 0.5, 0.5, 0 ) );
		assertThat( quad.getPoint() ).isEqualTo( new Point3D( 1, 0, 0 ) );
	}

	@Test
	void getReferencePoints() {
		// given
		DesignQuad quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0.5, 0.5, 0 ), new Point3D( 1, 0, 0 ) );

		// when
		List<Point3D> points = quad.getReferencePoints();

		// then
		Point3DAssert.assertThat( points.getFirst() ).isCloseTo( new Point3D( 0, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 1 ) ).isCloseTo( new Point3D( 0.5, 0.5, 0 ) );
		Point3DAssert.assertThat( points.get( 2 ) ).isCloseTo( new Point3D( 1, 0, 0 ) );
	}

	@Test
	void testApply() {
		// given
		DesignQuad quad = new DesignQuad( new Point3D( 1, 2, 0 ), new Point3D( 2, 4, 0 ), new Point3D( 4, 6, 0 ) );
		CadTransform transform = CadTransform.translation( 2, 3, 0 );

		// when
		quad.apply( transform );

		// then
		assertThat( quad.getOrigin() ).isEqualTo( new Point3D( 3, 5, 0 ) );
		assertThat( quad.getControl() ).isEqualTo( new Point3D( 4, 7, 0 ) );
		assertThat( quad.getPoint() ).isEqualTo( new Point3D( 6, 9, 0 ) );

		// Apply null quad
		DesignQuad empty = new DesignQuad();
		empty.apply( transform );
		assertThat( empty.getOrigin() ).isNull();
		assertThat( empty.getControl() ).isNull();
		assertThat( empty.getPoint() ).isNull();
	}

	@Test
	void testGetInformation() {
		DesignQuad quad = new DesignQuad( new Point3D( 0, 0, 0 ), new Point3D( 0.5, 0.5, 0 ), new Point3D( 1, 0, 0 ) );
		Map<String, Object> info = quad.getInformation();
		assertThat( info.get( DesignQuad.ORIGIN ) ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( info.get( DesignQuad.CONTROL ) ).isEqualTo( new Point3D( 0.5, 0.5, 0 ) );
		assertThat( info.get( DesignQuad.POINT ) ).isEqualTo( new Point3D( 1, 0, 0 ) );
		assertThat( (Double)info.get( "length" ) ).isCloseTo( 1.274307417012654, TOLERANCE );

		// Empty quad
		DesignQuad empty = new DesignQuad();
		Map<String, Object> emptyInfo = empty.getInformation();
		assertThat( emptyInfo.get( DesignQuad.ORIGIN ) ).isNull();
		assertThat( emptyInfo.get( DesignQuad.CONTROL ) ).isNull();
		assertThat( emptyInfo.get( DesignQuad.POINT ) ).isNull();
		assertThat( (Double)emptyInfo.get( "length" ) ).isNaN();
	}

	@Test
	void testUpdateFromShape() {
		DesignQuad source = new DesignQuad( new Point3D( 1, 2, 3 ), new Point3D( 2, 3, 4 ), new Point3D( 4, 5, 6 ) );
		DesignQuad target = new DesignQuad();
		target.updateFrom( source );

		assertThat( target.getOrigin() ).isEqualTo( new Point3D( 1, 2, 3 ) );
		assertThat( target.getControl() ).isEqualTo( new Point3D( 2, 3, 4 ) );
		assertThat( target.getPoint() ).isEqualTo( new Point3D( 4, 5, 6 ) );

		// Update from non-quad shape
		DesignBox box = new DesignBox( new Point3D( 7, 8, 9 ), new Point3D( 1, 1, 0 ) );
		target.updateFrom( box );
		assertThat( target.getOrigin() ).isEqualTo( new Point3D( 7, 8, 9 ) );
		assertThat( target.getControl() ).isEqualTo( new Point3D( 2, 3, 4 ) );
		assertThat( target.getPoint() ).isEqualTo( new Point3D( 4, 5, 6 ) );
	}

	@Test
	void testNullSafety() {
		DesignQuad empty = new DesignQuad();
		assertThat( empty.getReferencePoints() ).isEmpty();
		assertThat( empty.pathLength() ).isNaN();
		assertThat( empty.getBounds() ).isNull();
		assertThat( empty.distanceTo( new Point3D( 0, 0, 0 ) ) ).isNaN();
	}

}
