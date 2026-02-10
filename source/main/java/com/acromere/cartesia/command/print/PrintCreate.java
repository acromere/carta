package com.acromere.cartesia.command.print;

import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.data.DesignPrint;

import static com.acromere.cartesia.command.Command.Result.FAILURE;
import static com.acromere.cartesia.command.Command.Result.INCOMPLETE;

public class PrintCreate extends PrintCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		if( task.getParameterCount() == 0 ) {
			promptForText( task, "print-name" );
			return INCOMPLETE;
		}

		if( task.hasParameter( 0 ) ) {
			DesignPrint print = new DesignPrint().setName( asText( task, "print-name", 0 ) );
			print.setOrigin( task.getTool().getViewCenter() );
			print.setZoom( task.getTool().getViewZoom() );
			print.setRotate( task.getTool().getViewRotate() );
			print.setLayers( task.getTool().getVisibleLayers() );

			task.getTool().getDesign().addPrint( print );
			//task.getTool().setCurrentPrint( print );

			return print;
		}

		return FAILURE;
	}

}
