package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.command.CommandTask;

public class CameraZoomAll extends CameraCommand {

	@Override
	public Object execute( CommandTask task) throws Exception {
		return zoomShapes( task.getContext(), task.getTool().getVisibleShapes() );
	}

}
