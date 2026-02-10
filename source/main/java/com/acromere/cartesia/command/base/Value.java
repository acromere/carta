package com.acromere.cartesia.command.base;

import com.acromere.cartesia.command.Command;
import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.command.InvalidInputException;
import lombok.CustomLog;

@CustomLog
public class Value extends Command {

	public Value() {}

	@Override
	public boolean clearSelectionWhenComplete() {
		return false;
	}

	@Override
	public boolean clearReferenceAndPreviewWhenComplete() {
		return false;
	}

	@Override
	public Object execute( CommandTask task ) throws Exception {
		if( task.getParameterCount() == 0 ) throw new InvalidInputException( this, "value", null );
		return task.getParameters();
	}

}
