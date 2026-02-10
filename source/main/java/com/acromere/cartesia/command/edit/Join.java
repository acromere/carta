package com.acromere.cartesia.command.edit;

import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.cartesia.math.Meet;
import javafx.geometry.Point3D;
import lombok.CustomLog;

import static com.acromere.cartesia.command.Command.Result.*;

@CustomLog
public class Join extends EditCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		if( task.getParameterCount() == 0 ) {
			promptForShape( task, "select-meet-shape" );
			return INCOMPLETE;
		}

		if( task.getParameterCount() == 1 ) {
			Point3D trimPoint = asPoint( task, "select-meet-shape", 0 );
			DesignShape trim = selectNearestShapeAtPoint( task, trimPoint );
			if( trim == null ) return SUCCESS;
			promptForShape( task, "select-meet-shape" );
			return INCOMPLETE;
		}

		if( task.hasParameter( 1 ) ) {
			Point3D trimPoint = asPoint( task, "select-meet-shape", 0 );
			Point3D edgePoint = asPoint( task, "select-meet-shape", 1 );
			DesignShape trim = selectNearestShapeAtPoint( task, trimPoint );
			DesignShape edge = findNearestShapeAtPoint( task, edgePoint );
			Point3D trimMouse = task.getTool().worldToScreen( trimPoint );
			Point3D edgeMouse = task.getTool().worldToScreen( edgePoint );

			Meet.meet( task.getTool(), trim, edge, trimMouse, edgeMouse );

			return SUCCESS;
		}

		return FAILURE;
	}

}
