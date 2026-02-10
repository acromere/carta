package com.acromere.cartesia.command.layer;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class LayerHide extends LayerCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		task.getTool().setLayerVisible( task.getTool().getSelectedLayer(), false );
		return SUCCESS;
	}

}
