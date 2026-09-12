package com.acromere.cartesia.tool.design.binding;

import com.acromere.data.DataNode;
import javafx.beans.property.DoublePropertyBase;

import java.util.function.Function;

public class DesignDoubleBinding extends DoublePropertyBase {

	public <T extends DataNode, R extends Double> DesignDoubleBinding( T node, String designPropertyName, Function<T, R> consumer ) {
		set( consumer.apply( node ) );
		node.register( this, designPropertyName, _ -> {
			// Forces the old value to be valid again before changing
			get();

			// Set the new value
			set( consumer.apply( node ) );
		} );
	}

	@Override
	public Object getBean() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}
}
