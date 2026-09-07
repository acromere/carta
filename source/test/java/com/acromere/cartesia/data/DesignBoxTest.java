package com.acromere.cartesia.data;

import com.acromere.cartesia.math.CadMath;
import com.acromere.cartesia.math.CadTransform;
import com.acromere.cartesia.test.Point3DAssert;
import com.acromere.zerra.color.Paints;
import com.acromere.zerra.javafx.FxUtil;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.acromere.cartesia.TestConstants.EXTRA_LOOSE_TOLERANCE;
import static com.acromere.cartesia.TestConstants.TOLERANCE;
import static org.assertj.core.api.Assertions.assertThat;

public class DesignBoxTest extends DesignShapeTest {

	DesignBoxTest() {
		super( new DesignBox( new Point3D( 0, 0, 0 ), new Point3D( 1, 1, 0 ) ) );
	}

	@Test
	void testDesignBox() {
		// when
		DesignBox box = new DesignBox();

		// then
		assertThat( box.getOrigin() ).isNull();
		assertThat( box.getSize() ).isNull();
		assertThat( box.getRotate() ).isNull();
	}

	@Test
	void testDesignBoxWithBounds() {
		// when
		DesignBox box = new DesignBox( FxUtil.bounds( new Point3D( 0, 0, 0 ), new Point3D( 3, 4, 0 ) ) );

		// then
		Point3DAssert.assertThat( box.getOrigin() ).isEqualTo( new Point3D( 1.5, 2, 0 ) );
		Point3DAssert.assertThat( box.getSize() ).isEqualTo( new Point3D( 3, 4, 0 ) );
		assertThat( box.getRotate() ).isNull();
	}

	@Test
	void testDesignBoxWithBoundsAndRotate() {
		// when
		DesignBox box = new DesignBox( FxUtil.bounds( new Point3D( 0, 0, 0 ), new Point3D( 3, 4, 0 ) ), 45 );

		// then
		Point3DAssert.assertThat( box.getOrigin() ).isEqualTo( new Point3D( 1.5, 2, 0 ) );
		Point3DAssert.assertThat( box.getSize() ).isEqualTo( new Point3D( 3, 4, 0 ) );
		assertThat( box.getRotate() ).isEqualTo( "45.0" );
		assertThat( box.calcRotate() ).isEqualTo( 45.0 );
	}

	@Test
	void testDesignBoxWithXYWH() {
		// when
		DesignBox box = new DesignBox( 0, 0, 2, 3 );

		// then
		Point3DAssert.assertThat( box.getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		Point3DAssert.assertThat( box.getSize() ).isEqualTo( new Point3D( 2, 3, 0 ) );
		assertThat( box.getRotate() ).isNull();
	}

	@Test
	void testDesignBoxWithXYWHR() {
		// when
		DesignBox box = new DesignBox( 0, 0, 2, 3, 30 );

		// then
		Point3DAssert.assertThat( box.getOrigin() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		Point3DAssert.assertThat( box.getSize() ).isEqualTo( new Point3D( 2, 3, 0 ) );
		assertThat( box.getRotate() ).isEqualTo( "30.0" );
		assertThat( box.calcRotate() ).isEqualTo( 30.0 );
	}

	@Test
	void testDesignBoxWithOriginSize() {
		// when
		DesignBox box = new DesignBox( new Point3D( 3, 1, 0 ), new Point3D( 4, 2, 0 ) );

		// then
		Point3DAssert.assertThat( box.getOrigin() ).isEqualTo( new Point3D( 3, 1, 0 ) );
		Point3DAssert.assertThat( box.getSize() ).isEqualTo( new Point3D( 4, 2, 0 ) );
		assertThat( box.getRotate() ).isNull();
	}

	@Test
	void testDesignBoxWithOriginSizeRotate() {
		// when
		DesignBox box = new DesignBox( new Point3D( 3, 1, 0 ), new Point3D( 4, 2, 0 ), -135.0 );

		// then
		Point3DAssert.assertThat( box.getOrigin() ).isEqualTo( new Point3D( 3, 1, 0 ) );
		Point3DAssert.assertThat( box.getSize() ).isEqualTo( new Point3D( 4, 2, 0 ) );
		assertThat( box.getRotate() ).isEqualTo( "-135.0" );
		assertThat( box.calcRotate() ).isEqualTo( -135.0 );
	}

	@Test
	void getBounds() {
		// given
		DesignBox box = new DesignBox( new Point3D( 4, 2, 0 ), new Point3D( 4, 2, 0 ) );
		box.setDrawPaint( null );
		box.setFillPaint( "#ffffffff" );

		// The default draw width is 0.05

		// when
		Bounds bounds = box.getBounds();

		assertThat( bounds.getMinX() ).isEqualTo( 2 );
		assertThat( bounds.getMinY() ).isEqualTo( 1 );
		assertThat( bounds.getMaxX() ).isEqualTo( 6 );
		assertThat( bounds.getMaxY() ).isEqualTo( 3 );
		assertThat( bounds.getWidth() ).isEqualTo( 4 );
		assertThat( bounds.getHeight() ).isEqualTo( 2 );
	}

	@Test
	void getBoundsWithStroke() {
		// given
		DesignBox box = new DesignBox( new Point3D( 4, 2, 0 ), new Point3D( 4, 2, 0 ) );
		box.setDrawPaint( null );
		box.setDrawWidth( "1" );
		box.setFillPaint( "#ffffffff" );

		// when
		Bounds bounds = box.getBounds();

		assertThat( bounds.getMinX() ).isEqualTo( 2 );
		assertThat( bounds.getMinY() ).isEqualTo( 1 );
		assertThat( bounds.getMaxX() ).isEqualTo( 6 );
		assertThat( bounds.getMaxY() ).isEqualTo( 3 );
		assertThat( bounds.getWidth() ).isEqualTo( 4 );
		assertThat( bounds.getHeight() ).isEqualTo( 2 );
	}

	@Test
	void getBoundsWithRotate() {
		// given
		DesignBox box = new DesignBox( 2, 1 + CadMath.SQRT2_OVER_2, 1, 1 );
		box.setDrawPaint( null );
		box.setFillPaint( "#ffffffff" );
		box.setRotate( 45 );

		// when
		Bounds bounds = box.getBounds();

		assertThat( bounds.getMinX() ).isEqualTo( 2 - CadMath.SQRT2_OVER_2, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMaxX() ).isEqualTo( 2 + CadMath.SQRT2_OVER_2, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMinY() ).isEqualTo( 1, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMaxY() ).isEqualTo( 1 + CadMath.SQRT2, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getWidth() ).isEqualTo( CadMath.SQRT2, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getHeight() ).isEqualTo( CadMath.SQRT2, EXTRA_LOOSE_TOLERANCE );
	}

	@Test
	void getVisualBounds() {
		// given
		DesignBox box = new DesignBox( new Point3D( 4, 2, 0 ), new Point3D( 4, 2, 0 ) );

		// when
		Bounds bounds = box.getSelectBounds();

		assertThat( bounds.getMinX() ).isEqualTo( 1.975, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMinY() ).isEqualTo( 0.975, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMaxX() ).isEqualTo( 6.025, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMaxY() ).isEqualTo( 3.025, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getWidth() ).isEqualTo( 4.05, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getHeight() ).isEqualTo( 2.05, EXTRA_LOOSE_TOLERANCE );
	}

	@Test
	void getVisualBoundsWithStroke() {
		// given
		DesignBox box = new DesignBox( new Point3D( 4, 2, 0 ), new Point3D( 4, 2, 0 ) );
		box.setDrawPaint( Paints.toString( Color.WHITE ) );
		box.setDrawWidth( "1" );

		// when
		Bounds bounds = box.getSelectBounds();

		assertThat( bounds.getMinX() ).isEqualTo( 1.5, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMinY() ).isEqualTo( 0.5, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMaxX() ).isEqualTo( 6.5, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMaxY() ).isEqualTo( 3.5, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getWidth() ).isEqualTo( 5, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getHeight() ).isEqualTo( 3, EXTRA_LOOSE_TOLERANCE );
	}

	@Test
	void getVisualBoundsWithRotate() {
		// given
		DesignBox box = new DesignBox( new Point3D( 2, 1, 0 ), new Point3D( 4, 2, 0 ) );
		box.setRotate( 45 );

		double a = 0.025 * CadMath.SQRT2;
		double b = 0.05 * CadMath.SQRT2;

		// when
		Bounds bounds = box.getSelectBounds();

		assertThat( bounds.getMinX() ).isEqualTo( 2 - (3 * CadMath.SQRT2_OVER_2) - a, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMaxX() ).isEqualTo( 2 + (3 * CadMath.SQRT2_OVER_2) + a, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMinY() ).isEqualTo( 1 - (3 * CadMath.SQRT2_OVER_2) - a, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getMaxY() ).isEqualTo( 1 + (3 * CadMath.SQRT2_OVER_2) + a, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getWidth() ).isEqualTo( 2 * CadMath.SQRT2_OVER_2 + 4 * CadMath.SQRT2_OVER_2 + b, EXTRA_LOOSE_TOLERANCE );
		assertThat( bounds.getHeight() ).isEqualTo( 2 * CadMath.SQRT2_OVER_2 + 4 * CadMath.SQRT2_OVER_2 + b, EXTRA_LOOSE_TOLERANCE );
	}

	@Test
	void getReferencePoints() {
		// given
		double a = 0.5 * CadMath.SQRT2_OVER_2;
		double b = CadMath.SQRT2_OVER_2;
		DesignBox box = new DesignBox( new Point3D( 1, 1, 0 ), new Point3D( 2, 1, 0 ) );
		box.setRotate( 45 );

		// when
		List<Point3D> points = box.getReferencePoints();

		// then
		assertThat( box.getSize() ).isEqualTo( new Point3D( 2, 1, 0 ) );
		Point3DAssert.assertThat( points.get( 0 ) ).isCloseTo( new Point3D( 1 - a, 1 - a - b, 0 ) );
		Point3DAssert.assertThat( points.get( 1 ) ).isCloseTo( new Point3D( 1 + a + b, 1 + a, 0 ) );
		Point3DAssert.assertThat( points.get( 2 ) ).isCloseTo( new Point3D( 1 + a, 1 + a + b, 0 ) );
		Point3DAssert.assertThat( points.get( 3 ) ).isCloseTo( new Point3D( 1 - a - b, 1 - a, 0 ) );
		assertThat( points ).hasSize( 4 );
	}

	@Test
	void apply() {
		// given
		DesignBox box = new DesignBox( new Point3D( 1, 2, 0 ), new Point3D( 3, 4, 0 ) );
		CadTransform transform = CadTransform.translation( 2, 3, 0 );

		// when
		box.apply( transform );

		// then
		Point3DAssert.assertThat( box.getOrigin() ).isCloseTo( new Point3D( 3, 5, 0 ) );
		Point3DAssert.assertThat( box.getSize() ).isCloseTo( new Point3D( 3, 4, 0 ) );

		// given
		CadTransform scale = CadTransform.scale( 2, 3, 1 );

		// when
		box.apply( scale );

		// then
		Point3DAssert.assertThat( box.getOrigin() ).isCloseTo( new Point3D( 6, 15, 0 ) );
		Point3DAssert.assertThat( box.getSize() ).isCloseTo( new Point3D( 6, 12, 0 ) );
	}

	@Test
	void testApplyWithRotate() {
		// given
		DesignBox box = new DesignBox( new Point3D( 1, 0, 0 ), new Point3D( 4, 2, 0 ), 30.0 );
		CadTransform rotation = CadTransform.rotation( new Point3D( 0, 0, 0 ), new Point3D( 0, 0, 1 ), 60.0 );

		// when
		box.apply( rotation );

		// then
		Point3DAssert.assertThat( box.getOrigin() ).isCloseTo( new Point3D( 0.5, Math.sqrt( 3 ) / 2.0, 0 ) );
		Point3DAssert.assertThat( box.getSize() ).isCloseTo( new Point3D( 4, 2, 0 ) );
		assertThat( box.calcRotate() ).isCloseTo( 90.0, TOLERANCE );
	}

	@Test
	void testDistanceTo() {
		// Box centered at (0, 0, 0) with width 4, height 2
		// Edges: x in [-2, 2], y = -1; x in [-2, 2], y = 1; x = -2, y in [-1, 1]; x = 2, y in [-1, 1]
		DesignBox box = new DesignBox( new Point3D( 0, 0, 0 ), new Point3D( 4, 2, 0 ) );

		// Point at center
		assertThat( box.distanceTo( new Point3D( 0, 0, 0 ) ) ).isCloseTo( 1.0, TOLERANCE );

		// Point on right edge
		assertThat( box.distanceTo( new Point3D( 2, 0, 0 ) ) ).isCloseTo( 0.0, TOLERANCE );

		// Point outside right
		assertThat( box.distanceTo( new Point3D( 5, 0, 0 ) ) ).isCloseTo( 3.0, TOLERANCE );

		// Point outside top
		assertThat( box.distanceTo( new Point3D( 0, 4, 0 ) ) ).isCloseTo( 3.0, TOLERANCE );

		// Point corner diagonal outside (3, 2, 0) -> distance to (2, 1, 0) is sqrt((3-2)^2 + (2-1)^2) = sqrt(2)
		assertThat( box.distanceTo( new Point3D( 3, 2, 0 ) ) ).isCloseTo( CadMath.SQRT2, TOLERANCE );
	}

	@Test
	void testDistanceToWithRotation() {
		// 1x1 box centered at (0, 0, 0) rotated 45 degrees
		DesignBox box = new DesignBox( new Point3D( 0, 0, 0 ), new Point3D( 2, 2, 0 ), 45.0 );

		// Point at center: distance to any edge is 1.0
		assertThat( box.distanceTo( new Point3D( 0, 0, 0 ) ) ).isCloseTo( 1.0, TOLERANCE );

		// Corners are at distance sqrt(2) along axes: (sqrt(2), 0), (0, sqrt(2)), (-sqrt(2), 0), (0, -sqrt(2))
		assertThat( box.distanceTo( new Point3D( CadMath.SQRT2, 0, 0 ) ) ).isCloseTo( 0.0, TOLERANCE );
	}

	@Test
	void testPathLength() {
		DesignBox box = new DesignBox( new Point3D( 0, 0, 0 ), new Point3D( 4, 2, 0 ) );
		assertThat( box.pathLength() ).isEqualTo( 12.0 );
	}

	@Test
	void testGetInformation() {
		DesignBox box = new DesignBox( new Point3D( 1, 2, 0 ), new Point3D( 4, 2, 0 ) );
		Map<String, Object> info = box.getInformation();

		assertThat( info.get( DesignBox.ORIGIN ) ).isEqualTo( new Point3D( 1, 2, 0 ) );
		assertThat( info.get( DesignBox.SIZE ) ).isEqualTo( new Point3D( 4, 2, 0 ) );
		assertThat( info.get( DesignBox.PERIMETER ) ).isEqualTo( 12.0 );
		assertThat( info.containsKey( DesignBox.ROTATE ) ).isFalse();
	}

	@Test
	void testGetInformationWithRotate() {
		DesignBox box = new DesignBox( new Point3D( 1, 2, 0 ), new Point3D( 4, 2, 0 ), 45.0 );
		Map<String, Object> info = box.getInformation();

		assertThat( info.get( DesignBox.ORIGIN ) ).isEqualTo( new Point3D( 1, 2, 0 ) );
		assertThat( info.get( DesignBox.SIZE ) ).isEqualTo( new Point3D( 4, 2, 0 ) );
		assertThat( info.get( DesignBox.ROTATE ) ).isEqualTo( "45.0" );
		assertThat( info.get( DesignBox.PERIMETER ) ).isEqualTo( 12.0 );
	}

	@Test
	void testAsMap() {
		DesignBox box = new DesignBox( new Point3D( 1, 2, 0 ), new Point3D( 4, 2, 0 ) );
		Map<String, Object> map = box.asMap();

		assertThat( map.get( DesignBox.SHAPE ) ).isEqualTo( DesignBox.BOX );
		assertThat( map.get( DesignBox.ORIGIN ) ).isEqualTo( new Point3D( 1, 2, 0 ) );
		assertThat( map.get( DesignBox.SIZE ) ).isEqualTo( new Point3D( 4, 2, 0 ) );
		assertThat( map.get( DesignBox.ROTATE ) ).isNull();
	}

	@Test
	void testAsMapWithRotate() {
		DesignBox box = new DesignBox( new Point3D( 1, 2, 0 ), new Point3D( 4, 2, 0 ), 45.0 );
		Map<String, Object> map = box.asMap();

		assertThat( map.get( DesignBox.SHAPE ) ).isEqualTo( DesignBox.BOX );
		assertThat( map.get( DesignBox.ORIGIN ) ).isEqualTo( new Point3D( 1, 2, 0 ) );
		assertThat( map.get( DesignBox.SIZE ) ).isEqualTo( new Point3D( 4, 2, 0 ) );
		assertThat( map.get( DesignBox.ROTATE ) ).isEqualTo( "45.0" );
	}

	@Test
	void testUpdateFromMap() {
		Map<String, Object> map = new HashMap<>();
		map.put( DesignBox.ORIGIN, "1,2,0" );
		map.put( DesignBox.SIZE, "4,3,0" );
		map.put( DesignBox.ROTATE, "30.0" );

		DesignBox box = new DesignBox();
		box.updateFrom( map );

		assertThat( box.getOrigin() ).isEqualTo( new Point3D( 1, 2, 0 ) );
		assertThat( box.getSize() ).isEqualTo( new Point3D( 4, 3, 0 ) );
		assertThat( box.getRotate() ).isEqualTo( "30.0" );
		assertThat( box.calcRotate() ).isEqualTo( 30.0 );
	}

	@Test
	void testUpdateFromShape() {
		DesignBox source = new DesignBox( new Point3D( 1, 2, 0 ), new Point3D( 4, 3, 0 ), 30.0 );
		DesignBox target = new DesignBox();

		target.updateFrom( source );

		assertThat( target.getOrigin() ).isEqualTo( new Point3D( 1, 2, 0 ) );
		assertThat( target.getSize() ).isEqualTo( new Point3D( 4, 3, 0 ) );
		assertThat( target.getRotate() ).isEqualTo( "30.0" );
		assertThat( target.calcRotate() ).isEqualTo( 30.0 );
	}

	@Test
	void testToString() {
		DesignBox box = new DesignBox( new Point3D( 1, 2, 0 ), new Point3D( 4, 3, 0 ), 30.0 );
		assertThat( box.toString() ).isEqualTo( "DesignBox{origin=Point3D [x = 1.0, y = 2.0, z = 0.0],size=Point3D [x = 4.0, y = 3.0, z = 0.0],rotate=30.0}" );
	}

}
