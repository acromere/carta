package com.acromere.cartesia.command.base;

import com.acromere.cartesia.command.Command;
import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class ReferencePointsToggle extends Command {

	@Override
	public Object execute( CommandTask task) throws Exception {
		task.getTool().setReferenceLayerVisible( !task.getTool().isReferenceLayerVisible() );
		return SUCCESS;
	}

}
