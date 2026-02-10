package com.acromere.cartesia;

import com.acromere.xenon.Xenon;
import com.acromere.xenon.resource.Resource;
import com.acromere.xenon.resource.exception.ResourceException;
import com.acromere.xenon.scheme.ProductScheme;

public class CartesiaScheme extends ProductScheme {

	public static final String ID = "cartesia";

	public CartesiaScheme( Xenon program ) {
		super( program, ID );
	}

	@Override
	public boolean canLoad( Resource resource ) throws ResourceException {
		return true;
	}

}
