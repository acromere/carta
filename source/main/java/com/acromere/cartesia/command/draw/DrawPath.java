package com.acromere.cartesia.command.draw;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class DrawPath extends DrawCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		// This one will be fun because it can be very powerful
		// Once started it can take SVG-like parameters to draw a path
		// Of particular interest is that it can be open or closed

		return SUCCESS;
	}

}
