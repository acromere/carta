package com.acromere.cartesia.data.util;

import com.acromere.cartesia.data.Design;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignModel;
import com.acromere.data.IdDataNode;
import com.acromere.util.TextUtil;
import com.acromere.xenon.XenonProgramProduct;
import com.acromere.xenon.resource.Resource;
import com.acromere.xenon.tool.settings.SettingOptionProvider;
import lombok.CustomLog;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CustomLog
public class DesignLayerOptionProvider implements SettingOptionProvider {

	private final XenonProgramProduct product;

	private final boolean showRoot;

	public DesignLayerOptionProvider( XenonProgramProduct product, boolean showRoot ) {
		this.product = product;
		this.showRoot = showRoot;
	}

	@Override
	public List<String> getKeys() {
		Optional<DesignModel> optional = getDesign();
		if( optional.isEmpty() ) return List.of();

		DesignModel model = optional.get();

		List<String> rootKey = List.of();
		if( showRoot ) rootKey = List.of( model.getLayers().getId() );

		return Stream.concat( rootKey.stream(), model.getAllLayers().stream().map( IdDataNode::getId ) ).collect( Collectors.toList() );
	}

	@Override
	public String getName( String key ) {
		Optional<DesignModel> optional = getDesign();
		if( optional.isEmpty() ) return TextUtil.EMPTY;

		DesignModel model = optional.get();
		DesignLayer notfound = new DesignLayer();
		DesignLayer layer = model.getAllLayersAndRoot().stream().filter( l -> l.getId().equals( key ) ).findAny().orElse( notfound );

		return layer == notfound ? key : layer.getFullName();
	}

	private Optional<DesignModel> getDesign() {
		Resource currentResource = product.getProgram().getResourceManager().getCurrentResource();
		if( currentResource == null ) {
			log.atWarn().log( "No current design for layer lookup" );
			return Optional.empty();
		}

		Object object = currentResource.getModel();
		if( !(object instanceof Design<?> design) ) {
			log.atWarn().log( "Resource model not a design model" );
			return Optional.empty();
		}

		return Optional.of( design.getDataModel() );
	}

}
