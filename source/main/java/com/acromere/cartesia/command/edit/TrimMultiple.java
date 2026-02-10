package com.acromere.cartesia.command.edit;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

/**
 * Select a shape to be a trim edge, then select multiple shapes to trim.
 * Press ESC to stop.
 */
public class TrimMultiple extends EditCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		return SUCCESS;
	}

}
