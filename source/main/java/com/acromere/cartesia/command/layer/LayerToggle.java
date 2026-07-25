package com.acromere.cartesia.command.layer;

import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.tool.DesignTool;
import com.acromere.zerra.javafx.Fx;
import lombok.CustomLog;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

@CustomLog
public class LayerToggle extends LayerCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		DesignTool tool = task.getTool();
		DesignLayer layer = tool.getSelectedLayer();
		boolean visible = !tool.isLayerVisible( layer );
		Fx.run( () -> tool.setLayerVisible( layer, visible ) );
		return SUCCESS;
	}

}
