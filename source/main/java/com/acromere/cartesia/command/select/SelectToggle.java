package com.acromere.cartesia.command.select;

import com.acromere.cartesia.command.CommandTask;
import lombok.CustomLog;

@CustomLog
public class SelectToggle extends SelectByPoint {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		return execute( task, true );
	}

}
