package com.acromere.cartesia;

import com.acromere.product.Product;
import com.acromere.product.Rb;

public class CartesiaDesignCodec3d extends CartesiaDesignCodec {

	public static final String MEDIA_TYPE = "application/vnd.acromere.cartesia.design.3d";

	public CartesiaDesignCodec3d( Product product ) {
		super( product );
		setDefaultExtension( "cartesia3d" );
		addSupported( Pattern.MEDIATYPE, MEDIA_TYPE );
	}

	@Override
	public String getKey() {
		return MEDIA_TYPE;
	}

	@Override
	public String getName() {
		return Rb.text( "asset", "codec-cartesia3d-name" );
	}

}
