package com.acromere.cartesia.command.measure;

import com.acromere.cartesia.RbKey;
import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.product.Rb;
import com.acromere.xenon.notice.Notice;
import com.acromere.zerra.javafx.Fx;
import javafx.geometry.Point3D;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import lombok.CustomLog;

import static com.acromere.cartesia.command.Command.Result.*;

@CustomLog
public class MeasureLength extends MeasureCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		setCaptureUndoChanges( task, false );

		if( task.getParameterCount() == 0 ) {
			promptForShape( task, "select-shape" );
			return INCOMPLETE;
		}

		if( task.hasParameter( 0 ) ) {
			Point3D point = asPoint( task, "select-shape", 0 );
			DesignShape shape = findNearestShapeAtPoint( task, point );
			if( shape == null ) return SUCCESS;

			double length = shape.pathLength();

			if( task.getContext().isInteractive() ) {
				String title = Rb.text( RbKey.NOTICE, "measurement" );
				String message = shape == DesignShape.NONE ? Rb.text( RbKey.NOTICE, "shape-not-selected" ) : Rb.text( RbKey.NOTICE, "length", length );
				Notice notice = new Notice( title, message );
				notice.setAction( () -> Fx.run( () -> {
					Clipboard clipboard = Clipboard.getSystemClipboard();
					ClipboardContent content = new ClipboardContent();
					content.putString( message );
					clipboard.setContent( content );
				} ) );
				task.getContext().getTool().getProgram().getNoticeManager().addNotice( notice );
			}

			return length;
		}

		return FAILURE;
	}

}
