package com.acromere.cartesia.command.select;

import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.command.base.Value;
import com.acromere.cartesia.tool.BaseDesignTool;
import javafx.geometry.Point3D;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import lombok.CustomLog;

import static com.acromere.cartesia.command.Command.Result.*;
import static com.acromere.cartesia.tool.RenderConstants.POINT_SELECT_APERTURE;
import static com.acromere.cartesia.tool.RenderConstants.WINDOW_SELECT_APERTURE;

@CustomLog
public abstract class SelectByWindow extends SelectCommand {

	protected Object execute( CommandTask task, boolean intersect ) throws Exception {
		int paramCount = task.getParameters().length;
		InputEvent event = task.getEvent();
		boolean noEvent = event == null;
		boolean hasEvent = !noEvent;

		// Nothing to do but prompt for the anchor point
		if( paramCount == 0 & noEvent ) {
			// Select window anchor
			task.getTool().setSelectAperture( WINDOW_SELECT_APERTURE );
			promptForWindow( task, "select-window-anchor" );
			return INCOMPLETE;
		}

		// If there is an event, but no parameters, use the world anchor as the first parameter
		if( paramCount == 0 & hasEvent && event instanceof MouseEvent mouseEvent && task.getTrigger().matches( mouseEvent ) ) {
			// Submit a Value command to pass the anchor back to this command
			task.getTool().setSelectAperture( WINDOW_SELECT_APERTURE );
			Point3D anchor = task.getContext().getWorldAnchor();
			task.getContext().setLocalAnchor( anchor );
			task.getContext().submit( task.getTool(), new Value(), anchor );
			return INCOMPLETE;
		}

		// Get the world anchor point from the first parameter
		if( paramCount == 1 & noEvent ) {
			Point3D worldPoint = asPoint( task, "select-window-anchor", 0 );
			if( worldPoint != null ) {
				promptForWindow( task, "select-window-corner" );
				return INCOMPLETE;
			}
		}

		// The situation of one parameter and an event should not occur

		// Get the world point from the event or the second parameter
		if( paramCount == 2 ) {
			Point3D worldAnchor = asPoint( task, "select-window-anchor", 0 );
			Point3D worldCorner = asPoint( task, "select-window-corner", 1 );
			if( worldAnchor != null && worldCorner != null ) {
				if( task.getContext().isSelectMode() ) {
					task.getTool().worldWindowSelect( worldAnchor, worldCorner, intersect, false );
					task.getTool().setSelectAperture( POINT_SELECT_APERTURE );
					return SUCCESS;
				} else {
					return new Point3D[]{ worldAnchor, worldCorner };
				}
			}
		}

		return FAILURE;
	}

	@Override
	public void handle( CommandTask task, MouseEvent event ) {
		BaseDesignTool tool = (BaseDesignTool)event.getSource();
		Point3D localAnchor = task.getContext().getLocalAnchor();
		Point3D worldAnchor = tool.screenToWorld( new Point3D( event.getX(), event.getY(), event.getZ() ) );

		event.consume();
		if( event.getEventType().equals( MouseEvent.MOUSE_DRAGGED ) ) {
			tool.moveSelectAperture( localAnchor, worldAnchor );
		} else if( getStep() == 2 && event.getEventType().equals( MouseEvent.MOUSE_MOVED ) ) {
			tool.moveSelectAperture( localAnchor, worldAnchor );
		} else if( event.getEventType().equals( MouseEvent.MOUSE_RELEASED ) ) {
			// Submit a Value command to pass the point back to this command
			task.getContext().submit( tool, new Value(), worldAnchor );
		}
	}

}
