package com.acromere.cartesia.data;

/**
 * A design shape representing no shape at all.
 */
public class DesignNil extends DesignBox {

	public DesignNil() {
		super( 0, 0, 0, 0 );
		removeModifyingKeys( ORIGIN, SIZE, ROTATE );
	}

}
