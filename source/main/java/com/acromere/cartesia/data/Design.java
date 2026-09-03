package com.acromere.cartesia.data;

import com.acromere.cartesia.tool.CommandContext;
import com.acromere.cartesia.tool.DesignContext;
import com.acromere.data.DataNode;
import lombok.Getter;
import lombok.Setter;

/**
 * The Design class contains the context and data for a design. This is the main
 * bridge between the data model and the UI.
 */
@Getter
public class Design<T extends DesignModel> extends DataNode {

	public static final String MODEL = "model";

	private final DesignContext designContext;

	@Setter
	private CommandContext commandContext;

	public Design( T model ) {
		this.designContext = new DesignContext();
		addModifyingKeys( MODEL );
		setDataModel(model);
	}

	@SuppressWarnings( "unchecked" )
	public T getDataModel() {
		return (T)getValue( MODEL );
	}

	public Design<T> setDataModel( T model ) {
		setValue( MODEL, model );
		return this;
	}

}
