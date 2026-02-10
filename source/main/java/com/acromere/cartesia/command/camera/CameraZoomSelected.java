package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.command.CommandTask;

public class CameraZoomSelected extends CameraCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		return zoomShapes( task.getContext(), task.getTool().getSelectedShapes() );
	}

}
