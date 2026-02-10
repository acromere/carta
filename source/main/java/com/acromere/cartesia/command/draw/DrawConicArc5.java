package com.acromere.cartesia.command.draw;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class DrawConicArc5 extends DrawCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		// NOTE It turns out that five points define a conic, not just an ellipse
		// therefore, implementing this command is left for "later"
		return SUCCESS;
	}

}
