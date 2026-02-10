package com.acromere.cartesia.command.edit;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class Redo extends EditCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		task.getContext().getTool().getResource().getUndoManager().redo();
		return SUCCESS;
	}

}
