package com.acromere.cartesia.tool.design.binding;

import com.acromere.data.DataNode;
import javafx.beans.property.BooleanPropertyBase;

import java.util.function.Function;

public class DesignBooleanBinding extends BooleanPropertyBase {

	public <T extends DataNode, R extends Boolean> DesignBooleanBinding( T node, String designPropertyName, Function<T, R> consumer ) {
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
