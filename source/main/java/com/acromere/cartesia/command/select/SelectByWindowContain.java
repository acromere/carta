package com.acromere.cartesia.command.select;

import com.acromere.cartesia.command.CommandTask;

public class SelectByWindowContain extends SelectByWindow {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		return execute( task, false );
	}

}
