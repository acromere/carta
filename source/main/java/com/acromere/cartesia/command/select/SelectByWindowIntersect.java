package com.acromere.cartesia.command.select;

import com.acromere.cartesia.command.CommandTask;

@Deprecated
public class SelectByWindowIntersect extends SelectByWindow {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		return execute( task, true );
	}

}
