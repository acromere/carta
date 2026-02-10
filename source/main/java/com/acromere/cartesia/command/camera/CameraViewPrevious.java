package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class CameraViewPrevious extends CameraCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		task.getTool().setView( task.getTool().getPriorPortal() );
		return SUCCESS;
	}

}
