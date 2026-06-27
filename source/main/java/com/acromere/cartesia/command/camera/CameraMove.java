package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.command.base.Value;
import com.acromere.cartesia.tool.BaseDesignTool;
import javafx.geometry.Point3D;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Transform;
import lombok.CustomLog;

import static com.acromere.cartesia.command.Command.Result.*;

@CustomLog
public class CameraMove extends CameraCommand {

	private Point3D originalAnchor;

	private Point3D originalViewPoint;

	private Transform originalTransform;

	@Override
	public Object execute( CommandTask task ) throws Exception {
		int paramCount = task.getParameters().length;
		InputEvent event = task.getEvent();
		boolean noEvent = event == null;
		boolean hasEvent = !noEvent;

		// If the command is already successful, return SUCCESS
		if( paramCount == 1 && task.getParameter( 0 ) == SUCCESS ) return SUCCESS;

		// Command triggered by input event
		if( paramCount == 0 & hasEvent && event instanceof MouseEvent mouseEvent && task.getTrigger().matches( event ) ) {
			event.consume();

			originalAnchor = task.getTool().screenToWorkplane( mouseEvent.getX(), mouseEvent.getY(), mouseEvent.getZ() );
			originalViewPoint = task.getTool().getViewCenter();
			originalTransform = task.getTool().getScreenToWorldTransform().clone();

			// Set the context anchor
			setContextAnchor( task, mouseEvent );

			return INCOMPLETE;
		}

		// Prompt the user for an anchor
		if( paramCount == 0 & noEvent ) {
			promptForPoint( task, "pan-anchor" );
			return INCOMPLETE;
		}

		// Prompt the user for a target
		if( paramCount == 1 & noEvent ) {
			Point3D worldAnchor = asPoint( task, "pan-anchor", 0 );
			if( worldAnchor != null ) {
				originalAnchor = worldAnchor;
				setContextAnchor( task, worldAnchor );
			}

			promptForPoint( task, "pan-target" );

			return INCOMPLETE;
		}

		// With the anchor and the target, move the view point
		if( paramCount == 2 & noEvent ) {
			Point3D worldAnchor = asPoint( task, "pan-anchor", 0 );
			Point3D worldTarget = asPoint( task, "pan-target", 1 );
			if( worldAnchor != null && worldTarget != null ) {
				Point3D worldOffset = worldAnchor.subtract( worldTarget );
				task.getTool().setViewCenter( originalViewPoint.add( worldOffset ) );
				return SUCCESS;
			}
		}

		return FAILURE;
	}

	@Override
	public void handle( CommandTask task, MouseEvent event ) {
		if( originalAnchor == null ) return;
		if( originalViewPoint == null ) return;
		if( originalTransform == null ) return;

		BaseDesignTool tool = (BaseDesignTool)event.getSource();
		Point3D anchor = originalAnchor;
		Point3D target = originalTransform.transform( event.getX(), event.getY(), event.getZ() );

		if( event.getEventType().equals( MouseEvent.MOUSE_DRAGGED ) ) {
			Point3D worldOffset = anchor.subtract( target );
			tool.setViewCenter( originalViewPoint.add( worldOffset ) );
			event.consume();
		} else if( event.getEventType().equals( MouseEvent.MOUSE_RELEASED ) ) {
			// Submit a SUCCESS Value to complete this command
			task.getContext().submit( tool, new Value(), SUCCESS );
			event.consume();
		}
	}

	/**
	 * For testing purposes only.
	 *
	 * @param originalViewPoint The original view point
	 */
	void setOriginalViewPoint( Point3D originalViewPoint ) {
		this.originalViewPoint = originalViewPoint;
	}

}
