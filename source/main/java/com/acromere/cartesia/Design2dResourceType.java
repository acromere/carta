package com.acromere.cartesia;

import com.acromere.cartesia.data.Design;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignModel2D;
import com.acromere.product.Rb;
import com.acromere.settings.Settings;
import com.acromere.xenon.RbKey;
import com.acromere.xenon.Xenon;
import com.acromere.xenon.XenonProgramProduct;
import com.acromere.xenon.resource.Resource;
import com.acromere.xenon.resource.ResourceType;

public class Design2dResourceType extends ResourceType {

	public static final String KEY = "design2d";

	public Design2dResourceType( XenonProgramProduct product ) {
		super( product, KEY );
		setDefaultCodec( new CartesiaDesignCodec2d( product ) );
	}

	@Override
	public boolean assetNew( Xenon program, Resource resource ) {
		Design<DesignModel2D> design = initModel( resource );

		// There might already be a model from assetNew()
		DesignModel2D dataModel = design.getDataModel();

		// Create the default layer
		String constructionLayerName = Rb.textOr( RbKey.LABEL, "layer-construction", "construction" ).toLowerCase();
		DesignLayer layer = new DesignLayer().setName( constructionLayerName );
		dataModel.getLayers().addLayer( layer );

		// Initialize the design settings
		Settings settings = program.getSettingsManager().getAssetSettings( resource );
		settings.set( "grid-major-x", "1.0" );
		settings.set( "grid-major-y", "1.0" );
		settings.set( "grid-minor-x", "0.5" );
		settings.set( "grid-minor-y", "0.5" );
		settings.set( "grid-snap-x", "0.1" );
		settings.set( "grid-snap-y", "0.1" );

		return true;
	}

	@Override
	public boolean assetOpen( Xenon program, Resource resource ) {
		initModel( resource );
		resource.setCaptureUndoChanges( true );
		return true;
	}

	private Design<DesignModel2D> initModel( Resource resource ) {
		// There might already be a model
		Design<DesignModel2D> design = resource.getModel();

		// If there is not already a model, create one
		if( design == null ) {
			design = new Design<DesignModel2D>().setDataModel( new DesignModel2D() );
			resource.setModel( design );
		}

		return design;
	}

}
