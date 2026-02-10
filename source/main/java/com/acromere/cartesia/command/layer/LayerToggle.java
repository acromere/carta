package com.acromere.cartesia.command.layer;

import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.tool.DesignTool;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class LayerToggle extends LayerCommand {

	@Override
	public Object execute( CommandTask task) throws Exception {
		DesignTool tool = task.getTool();
		DesignLayer layer = tool.getSelectedLayer();
		tool.setLayerVisible( layer, !tool.isLayerVisible( layer ) );
		return SUCCESS;
	}

}
