package com.acromere.cartesia.data;

import com.acromere.cartesia.BaseCartesiaUnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class DesignShapeTest extends BaseCartesiaUnitTest {

	private final DesignShape shape;

	protected DesignShapeTest( DesignShape shape ) {
		this.shape = shape;
	}

	@Test
	public void isVisible() {
		// given
		assertThat( shape.isVisible() ).isFalse();

		// when
		shape.setVisible( true );

		// then
		assertThat( shape.isVisible() ).isTrue();
	}

	@Test
	public void getVisible() {
		// given
		assertThat( shape.isVisible() ).isFalse();

		// when
		shape.setVisible( "TRUE" );

		// then
		assertThat( shape.getVisible() ).isEqualTo( "TRUE" );
	}

	@Test
	public void isVisibleWithString() {
		// given
		assertThat( shape.isVisible() ).isFalse();

		// when
		shape.setVisible( "TRUE" );

		// then
		assertThat( shape.isVisible() ).isTrue();
	}

	@Test
	public void isSelected() {
		// given
		assertThat( shape.isSelected() ).isFalse();

		// when
		shape.setSelected( true );

		// then
		assertThat( shape.isSelected() ).isTrue();
	}

	@Test
	public void getSelected() {
		// given
		assertThat( shape.isSelected() ).isFalse();

		// when
		shape.setSelected( "TRUE" );

		// then
		assertThat( shape.getSelected() ).isEqualTo( "TRUE" );
	}

	@Test
	public void isSelectedWithString() {
		// given
		assertThat( shape.isSelected() ).isFalse();

		// when
		shape.setSelected( "TRUE" );

		// then
		assertThat( shape.isSelected() ).isTrue();
	}

	@Test
	public void testShapeRotate() {
		// when
		shape.setRotate( 45.0 );

		// then
		assertThat( shape.getRotate() ).isEqualTo( "45.0" );
		assertThat( shape.calcRotate() ).isEqualTo( 45.0 );

		// when
		shape.setRotate( "90" );

		// then
		assertThat( shape.getRotate() ).isEqualTo( "90" );
		assertThat( shape.calcRotate() ).isEqualTo( 90.0 );
	}

	@Test
	public void testShapeUpdateFromShape() {
		// given
		DesignShape source = new DesignBox( new javafx.geometry.Point3D( 1, 2, 3 ), new javafx.geometry.Point3D( 4, 5, 6 ), 30.0 );
		DesignShape target = new DesignBox();

		// when
		target.updateFrom( source );

		// then
		assertThat( target.getOrigin() ).isEqualTo( new javafx.geometry.Point3D( 1, 2, 3 ) );
	}

	@Test
	public void testShapeAsMapWithRotate() {
		// given
		DesignShape testShape = new DesignBox( new javafx.geometry.Point3D( 1, 2, 0 ), new javafx.geometry.Point3D( 4, 2, 0 ), 45.0 );

		// when
		java.util.Map<String, Object> map = testShape.asMap();

		// then
		assertThat( map.get( DesignShape.ORIGIN ) ).isEqualTo( new javafx.geometry.Point3D( 1, 2, 0 ) );
		assertThat( map.get( DesignShape.ROTATE ) ).isEqualTo( "45.0" );
	}

	@Test
	public void testInvalidateCache() {
		// given
		DesignBox box = new DesignBox( new javafx.geometry.Point3D( 0, 0, 0 ), new javafx.geometry.Point3D( 2, 2, 0 ) );
		javafx.geometry.Bounds bounds1 = box.getBounds();
		assertThat( bounds1 ).isNotNull();

		// when
		box.setOrigin( new javafx.geometry.Point3D( 10, 10, 0 ) );
		javafx.geometry.Bounds bounds2 = box.getBounds();

		// then
		assertThat( bounds2 ).isNotEqualTo( bounds1 );
	}

	@Test
	public void testNaturalComparator() {
		DesignBox s1 = new DesignBox();
		s1.setOrder( 1 );
		DesignBox s2 = new DesignBox();
		s2.setOrder( 2 );

		java.util.Comparator<com.acromere.data.DataNode> comparator = s1.getNaturalComparator();
		assertThat( comparator.compare( s1, s2 ) ).isGreaterThan( 0 );
		assertThat( comparator.compare( s2, s1 ) ).isLessThan( 0 );
		assertThat( comparator.compare( s1, s1 ) ).isEqualTo( 0 );
		assertThat( comparator.compare( s1, null ) ).isLessThan( 0 );
		assertThat( comparator.compare( null, s1 ) ).isGreaterThan( 0 );
	}

}
