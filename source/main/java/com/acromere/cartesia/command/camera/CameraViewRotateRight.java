package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class CameraViewRotateRight extends CameraCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		double angle = task.getTool().getViewRotate() - 5;
		if( angle < 180 ) angle += 360;
		task.getTool().setViewRotate( angle );

		return SUCCESS;
	}

}
