package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.CommandTrigger;
import com.acromere.cartesia.tool.CommandContext;
import javafx.scene.input.InputEvent;
import static com.acromere.cartesia.command.Command.Result.*;

public class CameraViewTop extends CameraCommand {

	@Override
	public Object execute( CommandContext context, CommandTrigger trigger, InputEvent triggerEvent, Object... parameters ) throws Exception {
		context.getTool().setViewRotate( 0 );
		return SUCCESS;
	}

}

