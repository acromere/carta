package com.acromere.cartesia.data;

import com.acromere.cartesia.math.CadMath;
import com.acromere.cartesia.test.Point3DAssert;
import com.acromere.curve.math.Constants;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.acromere.cartesia.TestConstants.TOLERANCE;
import static org.assertj.core.api.Assertions.assertThat;

public class DesignEllipseTest extends DesignShapeTest {

	DesignEllipseTest() {
		super( new DesignEllipse( new Point3D( 0, 0, 0 ), 1.0, 2.0 ) );
	}

	@Test
	void testModify() {
		DesignEllipse line = new DesignEllipse( new Point3D( 0, 0, 0 ), 1.0 );
		assertThat( line.isModified() ).isTrue();
		line.setModified( false );
		assertThat( line.isModified() ).isFalse();

		line.setOrigin( new Point3D( 0, 0, 0 ) );
		line.setRadius( 1.0 );
		assertThat( line.isModified() ).isFalse();

		line.setOrigin( new Point3D( 1, 1, 0 ) );
		assertThat( line.isModified() ).isTrue();
		line.setModified( false );
		assertThat( line.isModified() ).isFalse();

		line.setRadius( 2.0 );
		assertThat( line.isModified() ).isTrue();
		line.setModified( false );
		assertThat( line.isModified() ).isFalse();
	}

	@Test
	void testOrigin() {
		DesignEllipse arc = new DesignEllipse( new Point3D( 0, 0, 0 ), 2.0 );
		assertThat( arc.getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );

		arc.setOrigin( new Point3D( 1, 2, 3 ) );
		assertThat( arc.getOrigin() ).isEqualTo( new Point3D( 1, 2, 3 ) );
	}

	@Test
	void testRadii() {
		Point3D radii = new Point3D( 7, 5, 0.0 );
		DesignEllipse arc = new DesignEllipse( new Point3D( 0, 0, 0 ), radii );
		assertThat( arc.getRadii() ).isEqualTo( radii );

		arc.setRadii( new Point3D( 13, 11, 0 ) );
		assertThat( arc.getRadii() ).isEqualTo( new Point3D( 13, 11, 0 ) );
	}

	@Test
	void testRadius() {
		DesignEllipse arc = new DesignEllipse( new Point3D( 0, 0, 0 ), 3.0 );
		assertThat( arc.getRadius() ).isEqualTo( 3.0 );

		arc.setRadius( 3.5 );
		assertThat( arc.getRadius() ).isEqualTo( 3.5 );
	}

	@Test
	void testToMapWithCircle() {
		DesignEllipse arc = new DesignEllipse( new Point3D( 1, 2, 3 ), 4.0 );
		Map<String, Object> map = arc.asMap();

		assertThat( map.get( DesignEllipse.SHAPE ) ).isEqualTo( DesignEllipse.CIRCLE );
		assertThat( map.get( DesignEllipse.ORIGIN ) ).isEqualTo( new Point3D( 1, 2, 3 ) );
		assertThat( map.get( DesignEllipse.RADII ) ).isEqualTo( new Point3D( 4, 4, 0 ) );
		assertThat( map.get( DesignEllipse.ROTATE ) ).isNull();
	}

	@Test
	void testToMapWithEllipse() {
		DesignEllipse arc = new DesignEllipse( new Point3D( 1, 2, 3 ), 4.0, 5.0 );
		Map<String, Object> map = arc.asMap();

		assertThat( map.get( DesignEllipse.SHAPE ) ).isEqualTo( DesignEllipse.ELLIPSE );
		assertThat( map.get( DesignEllipse.ORIGIN ) ).isEqualTo( new Point3D( 1, 2, 3 ) );
		assertThat( map.get( DesignEllipse.RADII ) ).isEqualTo( new Point3D( 4, 5, 0 ) );
		assertThat( map.get( DesignEllipse.ROTATE ) ).isNull();
	}

	@Test
	void testToMapWithRotatedEllipse() {
		DesignEllipse arc = new DesignEllipse( new Point3D( 1, 2, 3 ), 6.0, 7.0, 8.0 );
		Map<String, Object> map = arc.asMap();

		assertThat( map.get( DesignEllipse.SHAPE ) ).isEqualTo( DesignEllipse.ELLIPSE );
		assertThat( map.get( DesignEllipse.ORIGIN ) ).isEqualTo( new Point3D( 1, 2, 3 ) );
		assertThat( map.get( DesignEllipse.RADII ) ).isEqualTo( new Point3D( 6, 7, 0 ) );
		assertThat( map.get( DesignEllipse.ROTATE ) ).isEqualTo( "8.0" );
	}

	@Test
	void testUpdateFromCircle() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignEllipse.SHAPE, DesignEllipse.CIRCLE );
		map.put( DesignEllipse.ORIGIN, "0,0,0" );
		map.put( DesignEllipse.RADII, "4,4,0" );

		DesignEllipse arc = new DesignEllipse();
		arc.updateFrom( map );

		assertThat( arc.getOrigin() ).isEqualTo( Point3D.ZERO );
		assertThat( arc.getRadius() ).isEqualTo( 4.0 );
		assertThat( arc.getXRadius() ).isEqualTo( 4.0 );
		assertThat( arc.getYRadius() ).isEqualTo( 4.0 );
		assertThat( arc.calcRotate() ).isEqualTo( 0.0 );
		assertThat( arc.getRotate() ).isNull();
	}

	@Test
	void testUpdateFromCircleWithDeprecatedRadius() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignEllipse.SHAPE, DesignEllipse.CIRCLE );
		map.put( DesignEllipse.ORIGIN, "0,0,0" );
		map.put( DesignEllipse.RADII, "4,4,0" );

		DesignEllipse arc = new DesignEllipse();
		arc.updateFrom( map );

		assertThat( arc.getOrigin() ).isEqualTo( Point3D.ZERO );
		assertThat( arc.getRadius() ).isEqualTo( 4.0 );
		assertThat( arc.getXRadius() ).isEqualTo( 4.0 );
		assertThat( arc.getYRadius() ).isEqualTo( 4.0 );
		assertThat( arc.calcRotate() ).isEqualTo( 0.0 );
		assertThat( arc.getRotate() ).isNull();
	}

	@Test
	void testUpdateFromEllipse() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignEllipse.SHAPE, DesignEllipse.ELLIPSE );
		map.put( DesignEllipse.ORIGIN, "0,0,0" );
		map.put( DesignEllipse.RADII, "4,5,0" );

		DesignEllipse arc = new DesignEllipse();
		arc.updateFrom( map );

		assertThat( arc.getOrigin() ).isEqualTo( Point3D.ZERO );
		assertThat( arc.getRadius() ).isEqualTo( 4.0 );
		assertThat( arc.getXRadius() ).isEqualTo( 4.0 );
		assertThat( arc.getYRadius() ).isEqualTo( 5.0 );
		assertThat( arc.calcRotate() ).isEqualTo( 0.0 );
		assertThat( arc.getRotate() ).isNull();
	}

	@Test
	void testUpdateFromEllipseWithDeprecatedRadius() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignEllipse.SHAPE, DesignEllipse.ELLIPSE );
		map.put( DesignEllipse.ORIGIN, "0,0,0" );
		map.put( DesignArc.RADII, "4,5,0" );

		DesignEllipse ellipse = new DesignEllipse();
		ellipse.updateFrom( map );

		assertThat( ellipse.getOrigin() ).isEqualTo( Point3D.ZERO );
		assertThat( ellipse.getRadius() ).isEqualTo( 4.0 );
		assertThat( ellipse.getXRadius() ).isEqualTo( 4.0 );
		assertThat( ellipse.getYRadius() ).isEqualTo( 5.0 );
		assertThat( ellipse.calcRotate() ).isEqualTo( 0.0 );
		assertThat( ellipse.getRotate() ).isNull();
	}

	@Test
	void testUpdateFromRotatedEllipse() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignEllipse.SHAPE, DesignEllipse.ELLIPSE );
		map.put( DesignEllipse.ORIGIN, "0,0,0" );
		map.put( DesignEllipse.RADII, "6,7,0" );
		map.put( DesignEllipse.ROTATE, "8.0" );

		DesignEllipse arc = new DesignEllipse();
		arc.updateFrom( map );

		assertThat( arc.getOrigin() ).isEqualTo( Point3D.ZERO );
		assertThat( arc.getRadius() ).isEqualTo( 6.0 );
		assertThat( arc.getXRadius() ).isEqualTo( 6.0 );
		assertThat( arc.getYRadius() ).isEqualTo( 7.0 );
		assertThat( arc.calcRotate() ).isEqualTo( 8.0 );
		assertThat( arc.getRotate() ).isEqualTo( "8.0" );
	}

	@Test
	void testUpdateFromRotatedEllipseWithDeprecatedRadius() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignEllipse.SHAPE, DesignEllipse.ELLIPSE );
		map.put( DesignEllipse.ORIGIN, "0,0,0" );
		map.put( DesignEllipse.RADII, "6,7,0" );
		map.put( DesignEllipse.ROTATE, "8.0" );

		DesignEllipse arc = new DesignEllipse();
		arc.updateFrom( map );

		assertThat( arc.getOrigin() ).isEqualTo( Point3D.ZERO );
		assertThat( arc.getRadius() ).isEqualTo( 6.0 );
		assertThat( arc.getXRadius() ).isEqualTo( 6.0 );
		assertThat( arc.getYRadius() ).isEqualTo( 7.0 );
		assertThat( arc.calcRotate() ).isEqualTo( 8.0 );
		assertThat( arc.getRotate() ).isEqualTo( "8.0" );
	}

	@Test
	void testGetType() {
		DesignEllipse ellipse = new DesignEllipse();
		assertThat( ellipse.getType() ).isEqualTo( DesignShape.Type.ELLIPSE );
	}

	@Test
	void testDistanceTo() {
		// Test circles
		DesignEllipse circle = new DesignEllipse( new Point3D( 5, 0, 0 ), 1.0 );
		assertThat( circle.distanceTo( new Point3D( 0, 0, 0 ) ) ).isEqualTo( 4.0 );
		assertThat( circle.distanceTo( new Point3D( 5, 0, 0 ) ) ).isEqualTo( 1.0 );
		assertThat( circle.distanceTo( new Point3D( 5, 3, 0 ) ) ).isEqualTo( 2.0 );

		// Test non-circular ellipse
		DesignEllipse ellipse = new DesignEllipse( new Point3D( 0, 0, 0 ), 4.0, 2.0 );
		assertThat( ellipse.distanceTo( new Point3D( 4, 0, 0 ) ) ).isCloseTo( 0.0, Offset.offset( Constants.RESOLUTION_LENGTH ) );
		assertThat( ellipse.distanceTo( new Point3D( 0, 2, 0 ) ) ).isCloseTo( 0.0, Offset.offset( Constants.RESOLUTION_LENGTH ) );
		assertThat( ellipse.distanceTo( new Point3D( 0, 5, 0 ) ) ).isCloseTo( 3.0, Offset.offset( Constants.RESOLUTION_LENGTH ) );
		assertThat( ellipse.distanceTo( new Point3D( 6, 0, 0 ) ) ).isCloseTo( 2.0, Offset.offset( Constants.RESOLUTION_LENGTH ) );

		// Test rotated ellipse
		DesignEllipse rotated = new DesignEllipse( new Point3D( 0, 0, 0 ), 4.0, 2.0, 90.0 );
		assertThat( rotated.distanceTo( new Point3D( 0, 4, 0 ) ) ).isCloseTo( 0.0, Offset.offset( Constants.RESOLUTION_LENGTH ) );
		assertThat( rotated.distanceTo( new Point3D( 2, 0, 0 ) ) ).isCloseTo( 0.0, Offset.offset( Constants.RESOLUTION_LENGTH ) );

		// Null safety
		DesignEllipse empty = new DesignEllipse( null, (Point3D)null );
		assertThat( empty.distanceTo( new Point3D( 0, 0, 0 ) ) ).isNaN();
		assertThat( circle.distanceTo( null ) ).isNaN();
	}

	@Test
	void testIsCoincident() {
		DesignEllipse circle = new DesignEllipse( new Point3D( 0, 0, 0 ), 5.0 );
		assertThat( circle.isCoincident( new Point3D( 5, 0, 0 ) ) ).isTrue();
		assertThat( circle.isCoincident( new Point3D( 0, 5, 0 ) ) ).isTrue();
		assertThat( circle.isCoincident( new Point3D( -5, 0, 0 ) ) ).isTrue();
		assertThat( circle.isCoincident( new Point3D( 0, -5, 0 ) ) ).isTrue();
		assertThat( circle.isCoincident( new Point3D( 0, 0, 0 ) ) ).isFalse();
		assertThat( circle.isCoincident( new Point3D( 5, 5, 0 ) ) ).isFalse();
		assertThat( circle.isCoincident( null ) ).isFalse();

		DesignEllipse ellipse = new DesignEllipse( new Point3D( 1, 2, 0 ), 4.0, 2.0 );
		assertThat( ellipse.isCoincident( new Point3D( 5, 2, 0 ) ) ).isTrue();
		assertThat( ellipse.isCoincident( new Point3D( 1, 4, 0 ) ) ).isTrue();
		assertThat( ellipse.isCoincident( new Point3D( -3, 2, 0 ) ) ).isTrue();
		assertThat( ellipse.isCoincident( new Point3D( 1, 0, 0 ) ) ).isTrue();
		assertThat( ellipse.isCoincident( new Point3D( 1, 2, 0 ) ) ).isFalse();

		DesignEllipse rotated = new DesignEllipse( new Point3D( 0, 0, 0 ), 4.0, 2.0, 90.0 );
		assertThat( rotated.isCoincident( new Point3D( 0, 4, 0 ) ) ).isTrue();
		assertThat( rotated.isCoincident( new Point3D( -2, 0, 0 ) ) ).isTrue();

		DesignEllipse empty = new DesignEllipse( null, (Point3D)null );
		assertThat( empty.isCoincident( new Point3D( 0, 0, 0 ) ) ).isFalse();
	}

	@Test
	void testIsCircular() {
		assertThat( new DesignEllipse( new Point3D( 0, 0, 0 ), 5.0 ).isCircular() ).isTrue();
		assertThat( new DesignEllipse( new Point3D( 0, 0, 0 ), 5.0, 5.0 ).isCircular() ).isTrue();
		assertThat( new DesignEllipse( new Point3D( 0, 0, 0 ), 5.0, 3.0 ).isCircular() ).isFalse();
		assertThat( new DesignEllipse( null, (Point3D)null ).isCircular() ).isFalse();
	}

	@Test
	void testNullSafety() {
		DesignEllipse ellipse = new DesignEllipse( null, (Point3D)null );
		assertThat( ellipse.getRadius() ).isNull();
		assertThat( ellipse.getXRadius() ).isNull();
		assertThat( ellipse.getYRadius() ).isNull();
		assertThat( ellipse.isCircular() ).isFalse();
		assertThat( ellipse.getReferencePoints() ).isEmpty();
		assertThat( ellipse.pathLength() ).isNaN();
		assertThat( ellipse.distanceTo( new Point3D( 1, 1, 0 ) ) ).isNaN();
		assertThat( ellipse.isCoincident( new Point3D( 1, 1, 0 ) ) ).isFalse();

		ellipse.setRadius( null );
		assertThat( ellipse.getRadii() ).isNull();

		DesignEllipse ctorNulls = new DesignEllipse( null, (Double)null, (Double)null, (Double)null );
		assertThat( ctorNulls.getRadii() ).isNull();
	}

	@Test
	void testUpdateFromShape() {
		DesignEllipse source = new DesignEllipse( new Point3D( 1, 2, 3 ), 4.0, 5.0, 6.0 );
		DesignEllipse target = new DesignEllipse();
		target.updateFrom( source );

		assertThat( target.getOrigin() ).isEqualTo( new Point3D( 1, 2, 3 ) );
		assertThat( target.getRadii() ).isEqualTo( new Point3D( 4, 5, 0 ) );
		assertThat( target.calcRotate() ).isEqualTo( 6.0 );
	}

	@Test
	void testUpdateFromMapWithNumericAndStringValues() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignEllipse.ORIGIN, "1,2,3" );
		map.put( DesignEllipse.RADII, new Point3D( 4, 5, 0 ) );
		map.put( DesignEllipse.ROTATE, "45.0" );

		DesignEllipse ellipse = new DesignEllipse();
		ellipse.updateFrom( map );

		assertThat( ellipse.getOrigin() ).isEqualTo( new Point3D( 1, 2, 3 ) );
		assertThat( ellipse.getRadii() ).isEqualTo( new Point3D( 4, 5, 0 ) );
		assertThat( ellipse.calcRotate() ).isEqualTo( 45.0 );

		Map<String, Object> mapDeprecatedRadius = new HashMap<>();
		mapDeprecatedRadius.put( DesignEllipse.ORIGIN, "0,0,0" );
		mapDeprecatedRadius.put( DesignEllipse.RADIUS, 7.5 );

		ellipse = new DesignEllipse();
		ellipse.updateFrom( mapDeprecatedRadius );
		assertThat( ellipse.getRadius() ).isEqualTo( 7.5 );

		Map<String, Object> mapDeprecatedRadii = new HashMap<>();
		mapDeprecatedRadii.put( DesignEllipse.ORIGIN, "0,0,0" );
		mapDeprecatedRadii.put( "x-radius", 3.0 );
		mapDeprecatedRadii.put( "y-radius", 6.0 );

		ellipse = new DesignEllipse();
		ellipse.updateFrom( mapDeprecatedRadii );
		assertThat( ellipse.getXRadius() ).isEqualTo( 3.0 );
		assertThat( ellipse.getYRadius() ).isEqualTo( 6.0 );
	}

	@Test
	void testPathLength() {
		// Circle
		assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 1.0 ).pathLength() ).isCloseTo( Constants.FULL_CIRCLE, TOLERANCE );

		// Degenerate ellipses
		assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 2.0, 0.0 ).pathLength() ).isCloseTo( 8.0, Offset.offset( Constants.RESOLUTION_LENGTH ) );
		assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 0.0, 1.0 ).pathLength() ).isCloseTo( 4.0, Offset.offset( Constants.RESOLUTION_LENGTH ) );

		// Normal ellipse
		assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 10.0, 5.0 ).pathLength() ).isCloseTo( 48.44224110273837, Offset.offset( Constants.RESOLUTION_LENGTH ) );

		// Rotated ellipse
		assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 10.0, 5.0, 45.0 ).pathLength() ).isCloseTo( 48.44224110273837, Offset.offset( Constants.RESOLUTION_LENGTH ) );
	}

	@Test
	void testGetBounds() {
		// Circle
		assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 1.0 ).getBounds() ).isEqualTo( new BoundingBox( 4, -1, 2, 2 ) );

		// Degenerate ellipses
		assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 2.0, 0.0 ).getBounds() ).isEqualTo( new BoundingBox( 3, 0, 4, 0 ) );
		assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 0.0, 1.0 ).getBounds() ).isEqualTo( new BoundingBox( 5, -1, 0, 2 ) );

		// Normal ellipse
		assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 10.0, 5.0 ).getBounds() ).isEqualTo( new BoundingBox( -5, -5, 20, 10 ) );

		// Rotated ellipse
		//assertThat( new DesignEllipse( new Point3D( 5, 0, 0 ), 10.0, 5.0, 45.0 ).getBounds() ).isEqualTo( new BoundingBox( -5, -5, 20, 20 ) );
	}

	@Test
	void testBoundsWithRotatedEllipse() {
		// given
		DesignEllipse ellipse = new DesignEllipse( new Point3D( 5, 0, 0 ), 10.0, 5.0, 45.0 );

		// when
		Bounds bounds = ellipse.getBounds();

		// then
		//assertThat( bounds.getMinX() ).isCloseTo( -5, TOLERANCE );
	}

	@Test
	void testLocalTransform() {
		DesignEllipse ellipse = new DesignEllipse( new Point3D( 0, 0, 0 ), 2.0, 1.0 );
		Point3DAssert.assertThat( ellipse.getRotateTransform().apply( new Point3D( 1, 1, 0 ) ) ).isCloseTo( new Point3D( 1, 2, 0 ) );

		ellipse = new DesignEllipse( new Point3D( 0, 0, 0 ), 2.0, 4.0 );
		Point3DAssert.assertThat( ellipse.getRotateTransform().apply( new Point3D( 1, 1, 0 ) ) ).isCloseTo( new Point3D( 1, 0.5, 0 ) );
	}

	@Test
	void testLocalTransformWithRotationScaleAndTranslate() {
		double root2 = Math.sqrt( 2 );
		DesignEllipse ellipse = new DesignEllipse( new Point3D( -3.0, 3.0, 0 ), 2.0, 1.0, 45.0 );
		Point3DAssert.assertThat( ellipse.getRotateTransform().apply( ellipse.getOrigin() ) ).isCloseTo( new Point3D( 0, 0, 0 ) );
		Point3DAssert.assertThat( ellipse.getRotateTransform().apply( new Point3D( -1, -1, 0 ) ) ).isCloseTo( new Point3D( -1 * root2, -6 * root2, 0 ) );

		ellipse = new DesignEllipse( new Point3D( -3.0, -3.0, 0 ), 2.0, 4.0, 270.0 );
		Point3DAssert.assertThat( ellipse.getRotateTransform().apply( ellipse.getOrigin() ) ).isCloseTo( new Point3D( 0, 0, 0 ) );
		Point3DAssert.assertThat( ellipse.getRotateTransform().apply( new Point3D( -1, -1, 0 ) ) ).isCloseTo( new Point3D( -2, 1, 0 ) );
	}

	@Test
	void getReferencePointsWithCircle() {
		// given
		DesignEllipse ellipse = new DesignEllipse( new Point3D( 5, 0, 0 ), 1.0 );

		// when
		List<Point3D> points = ellipse.getReferencePoints();

		// then
		Point3DAssert.assertThat( points.getFirst() ).isCloseTo( new Point3D( 5, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 1 ) ).isCloseTo( new Point3D( 6, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 2 ) ).isCloseTo( new Point3D( 5, 1, 0 ) );
		Point3DAssert.assertThat( points.get( 3 ) ).isCloseTo( new Point3D( 4, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 4 ) ).isCloseTo( new Point3D( 5, -1, 0 ) );
		assertThat( points.size() ).isEqualTo( 5 );
	}

	@Test
	void getReferencePointsWithFlatEllipse() {
		// given
		DesignEllipse ellipse = new DesignEllipse( new Point3D( 5, 0, 0 ), 2.0, 0.0 );

		// when
		List<Point3D> points = ellipse.getReferencePoints();

		// then
		Point3DAssert.assertThat( points.getFirst() ).isCloseTo( new Point3D( 5, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 1 ) ).isCloseTo( new Point3D( 7, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 2 ) ).isCloseTo( new Point3D( 5, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 3 ) ).isCloseTo( new Point3D( 3, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 4 ) ).isCloseTo( new Point3D( 5, 0, 0 ) );
		assertThat( points.size() ).isEqualTo( 5 );
	}

	@Test
	void getReferencePointsWithThinEllipse() {
		// given
		DesignEllipse ellipse = new DesignEllipse( new Point3D( 5, 0, 0 ), 0.0, 1.0 );

		// when
		List<Point3D> points = ellipse.getReferencePoints();

		// then
		Point3DAssert.assertThat( points.getFirst() ).isCloseTo( new Point3D( 5, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 1 ) ).isCloseTo( new Point3D( 5, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 2 ) ).isCloseTo( new Point3D( 5, 1, 0 ) );
		Point3DAssert.assertThat( points.get( 3 ) ).isCloseTo( new Point3D( 5, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 4 ) ).isCloseTo( new Point3D( 5, -1, 0 ) );
		assertThat( points.size() ).isEqualTo( 5 );
	}

	@Test
	void getReferencePointsWithEllipse() {
		// given
		DesignEllipse ellipse = new DesignEllipse( new Point3D( 5, 0, 0 ), 10.0, 5.0 );

		// when
		List<Point3D> points = ellipse.getReferencePoints();

		// then
		Point3DAssert.assertThat( points.getFirst() ).isCloseTo( new Point3D( 5, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 1 ) ).isCloseTo( new Point3D( 15, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 2 ) ).isCloseTo( new Point3D( 5, 5, 0 ) );
		Point3DAssert.assertThat( points.get( 3 ) ).isCloseTo( new Point3D( -5, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 4 ) ).isCloseTo( new Point3D( 5, -5, 0 ) );
		assertThat( points.size() ).isEqualTo( 5 );
	}

	@Test
	void getReferencePointsWithRotatedEllipse() {
		// given
		DesignEllipse ellipse = new DesignEllipse( new Point3D( 5, 0, 0 ), 10.0, 5.0, 45.0 );
		double a = 10 * CadMath.SQRT2_OVER_2;
		double b = 5 * CadMath.SQRT2_OVER_2;

		// when
		List<Point3D> points = ellipse.getReferencePoints();

		// then
		Point3DAssert.assertThat( points.getFirst() ).isCloseTo( new Point3D( 5, 0, 0 ) );
		Point3DAssert.assertThat( points.get( 1 ) ).isCloseTo( new Point3D( 5 + a, a, 0 ) );
		Point3DAssert.assertThat( points.get( 2 ) ).isCloseTo( new Point3D( 5 - b, b, 0 ) );
		Point3DAssert.assertThat( points.get( 3 ) ).isCloseTo( new Point3D( 5 - a, -a, 0 ) );
		Point3DAssert.assertThat( points.get( 4 ) ).isCloseTo( new Point3D( 5 + b, -b, 0 ) );
		assertThat( points.size() ).isEqualTo( 5 );
	}

}
