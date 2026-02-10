package com.acromere.cartesia;

import com.acromere.xenon.XenonProgramProduct;
import com.acromere.xenon.resource.ResourceType;
import com.acromere.xenon.resource.Codec;
import com.acromere.xenon.resource.PlaceholderCodec;

public class ShapePropertiesResourceType extends ResourceType {

	private static final String uriPattern = CartesiaScheme.ID + ":/shape-properties";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ShapePropertiesResourceType( XenonProgramProduct product ) {
		super( product, "shape-properties" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, uriPattern );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return uriPattern;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

}
