package com.acromere.cartesia.data;

import com.acromere.cartesia.tool.CommandContext;
import com.acromere.cartesia.tool.DesignContext;
import lombok.Getter;
import lombok.Setter;

/**
 * The Design class contains the context and data for a design. This is the main
 * bridge between the data model and the UI.
 */
@Getter
public class Design<T extends DesignModel> {

	private final T dataModel;

	private final DesignContext designContext;

	@Setter
	private CommandContext commandContext;

	public Design( T model ) {
		this.dataModel = model;
		this.designContext = new DesignContext();
	}

}
