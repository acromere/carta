package com.acromere.cartesia.command.view;

import com.acromere.cartesia.data.DesignView;
import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class ViewDelete extends ViewCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		DesignView view = task.getTool().getCurrentView();
		if( view == null ) return SUCCESS;

		task.getTool().getDesign().removeView( view );

		return SUCCESS;
	}

}
