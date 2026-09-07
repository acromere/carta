package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class CameraViewRight extends CameraCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		task.getTool().setViewRotate( -90 );
		return SUCCESS;
	}

}
