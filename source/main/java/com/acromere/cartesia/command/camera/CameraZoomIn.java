package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.tool.BaseDesignTool;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

/**
 * Zoom in to the design. This has the effect of making the design larger or
 * moving toward the design.
 *
 * <p>This command does not require any parameters. If no parameters are
 * provided it assumes the zoom will be centered around the current view point
 * and will use {@link BaseDesignTool#ZOOM_IN_FACTOR} as the zoom factor.</p>
 */
public class CameraZoomIn extends CameraZoom {

	@Override
	public Object execute( CommandTask task) {
		zoomByFactor( task.getTool(), task.getTool().getViewCenter(), BaseDesignTool.ZOOM_IN_FACTOR );
		return SUCCESS;
	}

}
