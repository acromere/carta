package com.acromere.cartesia.command.view;

import com.acromere.cartesia.data.DesignView;
import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class ViewUpdate extends ViewCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		DesignView view = task.getTool().getCurrentView();
		if( view == null ) return SUCCESS;

		view.setOrigin( task.getTool().getViewCenter() );
		view.setZoom( task.getTool().getViewZoom() );
		view.setRotate( task.getTool().getViewRotate() );
		view.setLayers( task.getTool().getVisibleLayers() );

		return SUCCESS;
	}

}
