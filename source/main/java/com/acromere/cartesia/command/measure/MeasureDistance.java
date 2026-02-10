package com.acromere.cartesia.command.measure;

import com.acromere.cartesia.RbKey;
import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.tool.BaseDesignTool;
import com.acromere.product.Rb;
import com.acromere.xenon.notice.Notice;
import com.acromere.zerra.javafx.Fx;
import javafx.geometry.Point3D;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import lombok.CustomLog;

import static com.acromere.cartesia.command.Command.Result.FAILURE;
import static com.acromere.cartesia.command.Command.Result.INCOMPLETE;

@CustomLog
public class MeasureDistance extends MeasureCommand {

	private DesignLine referenceLine;

	@Override
	public Object execute( CommandTask task ) throws Exception {
		if( task.getParameterCount() == 0 ) {
			if( referenceLine == null ) referenceLine = createReferenceLine( task );
			promptForPoint( task, "start-point" );
			return INCOMPLETE;
		}

		if( task.getParameterCount() == 1 ) {
			Point3D origin = asPoint( task, "start-point", 0 );

			if( referenceLine == null ) referenceLine = createReferenceLine( task );
			referenceLine.setOrigin( origin );

			promptForPoint( task, "end-point" );
			return INCOMPLETE;
		}

		if( task.hasParameter( 1 ) ) {
			Point3D p1 = asPoint( task, "start-point", 0 );
			Point3D p2 = asPoint( task, "end-point", 1 );
			double distance = p1.distance( p2 );

			if( task.getContext().isInteractive() ) {
				String title = Rb.text( RbKey.NOTICE, "measurement" );
				String message = Rb.text( RbKey.NOTICE, "distance", distance );
				Notice notice = new Notice( title, message );
				notice.setAction( () -> Fx.run( () -> {
					Clipboard clipboard = Clipboard.getSystemClipboard();
					ClipboardContent content = new ClipboardContent();
					// TODO Run the distance value through the design value formatter
					content.putString( String.valueOf( distance ) );
					clipboard.setContent( content );
				} ) );
				task.getContext().getTool().getProgram().getNoticeManager().addNotice( notice );
			}

			log.atDebug().log( "Measured distance=%s", distance );
			return distance;
		}

		return FAILURE;
	}

	@Override
	public void handle( CommandTask task, MouseEvent event ) {
		if( event.getEventType() == MouseEvent.MOUSE_MOVED ) {
			if( referenceLine != null ) {
				BaseDesignTool tool = (BaseDesignTool)event.getSource();
				Point3D point = tool.screenToWorkplane( event.getX(), event.getY(), event.getZ() );
				switch( getStep() ) {
					case 1 -> {
						referenceLine.setOrigin( point );
						referenceLine.setPoint( point );
					}
					case 2 -> referenceLine.setPoint( point );
				}
			}
		}
	}

}
