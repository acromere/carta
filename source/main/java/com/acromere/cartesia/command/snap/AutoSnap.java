package com.acromere.cartesia.command.snap;

import com.acromere.cartesia.command.InvalidInputException;
import com.acromere.cartesia.snap.Snap;
import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.tool.DesignTool;

import static com.acromere.cartesia.command.Command.Result.FAILURE;

/**
 * Auto snap commands use the first parameter to determine what kind of snap to
 * perform. The command will then use the event or a second parameter to
 * determine the reference point to snap.
 */
public class AutoSnap extends SnapCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		Snap snap = (Snap)task.getParameter( 0 );
		if( snap == null ) throw new InvalidInputException( this, "snap", null );

		if( task.getEvent() == null & !task.hasParameter( 1 ) ) throw new InvalidInputException( this, "snap", null );

		DesignTool tool = task.getTool();

		// Get the snap point from the parameter
		if( task.hasParameter( 1 ) ) return snap.snap( tool, asPoint( task, "snap", 1 ) );

		// Get the snap point from the event
		if( task.getEvent() != null ) return snap.snap( tool, asPoint( task, "snap", task.getEvent() ) );

		return FAILURE;
	}

}
