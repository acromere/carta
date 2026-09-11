package com.acromere.cartesia.tool.design.binding;

import com.acromere.data.DataNode;
import javafx.beans.property.ObjectPropertyBase;
import lombok.CustomLog;

import java.util.function.Function;

@CustomLog
public class DesignBinding<T> extends ObjectPropertyBase<T> {

	public <S extends DataNode, R extends T> DesignBinding( S node, String designPropertyName, Function<S, R> consumer ) {
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
