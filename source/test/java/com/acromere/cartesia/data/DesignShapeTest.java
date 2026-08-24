package com.acromere.cartesia.data;

import com.acromere.cartesia.BaseCartesiaUnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class DesignShapeTest extends BaseCartesiaUnitTest {

	private final DesignShape shape;

	protected DesignShapeTest(DesignShape shape) {
		this.shape = shape;
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
		assertThat( shape.getSelected() ).isEqualTo( "TRUE");
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

}
