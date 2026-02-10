package com.acromere.cartesia;

import com.acromere.product.Product;
import com.acromere.product.Rb;

public class CartesiaDesignCodec2d extends CartesiaDesignCodec {

	public static final String MEDIA_TYPE = "application/vnd.acromere.cartesia.design.2d";

	public CartesiaDesignCodec2d( Product product ) {
		super( product );
		setDefaultExtension( "cartesia2d" );
		addSupported( Pattern.MEDIATYPE, MEDIA_TYPE );
	}

	@Override
	public String getKey() {
		return MEDIA_TYPE;
	}

	@Override
	public String getName() {
		return Rb.text( "asset", "codec-cartesia2d-name" );
	}

}
