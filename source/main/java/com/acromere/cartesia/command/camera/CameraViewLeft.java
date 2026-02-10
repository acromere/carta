package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.CommandTrigger;
import com.acromere.cartesia.tool.CommandContext;
import javafx.scene.input.InputEvent;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class CameraViewLeft extends CameraCommand {

	@Override
	public Object execute( CommandContext context, CommandTrigger trigger, InputEvent triggerEvent, Object... parameters ) throws Exception {
		context.getTool().setViewRotate( 90 );
		return SUCCESS;
	}

}
