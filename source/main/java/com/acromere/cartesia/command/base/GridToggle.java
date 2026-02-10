package com.acromere.cartesia.command.base;

import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.command.ToggleCommand;

import static com.acromere.cartesia.command.Command.Result.*;

public class GridToggle extends ToggleCommand {

	@Override
	public Object execute( CommandTask task) throws Exception {
		task.getTool().setGridVisible( !task.getTool().isGridVisible() );
		return SUCCESS;
	}

}
