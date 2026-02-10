package com.acromere.cartesia.command.snap;

import com.acromere.cartesia.command.ToggleCommand;
import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.tool.DesignTool;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class SnapGridToggle extends ToggleCommand {

	@Override
	public Object execute( CommandTask task) throws Exception {
		DesignTool tool = task.getTool();
		tool.setGridSnapEnabled( !tool.isGridSnapEnabled() );
		return SUCCESS;
	}

}
