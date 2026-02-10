package com.acromere.cartesia.command.layer;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class LayerShow extends LayerCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		task.getTool().setLayerVisible( task.getTool().getSelectedLayer(), true );
		return SUCCESS;
	}

}
