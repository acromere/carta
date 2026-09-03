package com.acromere.cartesia.data;

import com.acromere.data.IdDataNode;
import com.acromere.data.DataNode;

import java.util.Map;
import java.util.Optional;

/**
 * The base node for all design data objects.
 */
public abstract class DesignNode extends IdDataNode {

	public DesignNode() {}

	protected Map<String, Object> asMap() {
		return asMap( ID );
	}

	public DesignNode updateFrom( Map<String, Object> map ) {
		if( map.containsKey( ID ) ) setId( (String)map.get( ID ) );
		return this;
	}

	public Optional<DesignModel> getDesign() {
		DataNode node = this;
		while( node != null && !(node instanceof DesignModel) ) {
			node = node.getParent();
		}
		return Optional.ofNullable( (DesignModel)node );
	}

}
