package com.acromere.cartesia;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DesignValueTest extends BaseCartesiaUnitTest {

	@Test
	void to() {
		DesignValue value = new DesignValue( 1, DesignUnit.M );
		DesignValue result = value.to( DesignUnit.MM );
		assertThat( result.value() ).isEqualTo( 1000 );
		assertThat( result.unit() ).isEqualTo( DesignUnit.MM );
	}

}
