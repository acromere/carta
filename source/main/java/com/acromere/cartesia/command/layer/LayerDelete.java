package com.acromere.cartesia.command.layer;

import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignModel;
import com.acromere.transaction.Txn;
import lombok.CustomLog;

import java.util.List;
import java.util.Optional;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

@CustomLog
public class LayerDelete extends LayerCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
		DesignLayer layer = task.getTool().getSelectedLayer();
		if( layer == null ) return SUCCESS;
		Optional<DesignModel> optionalModel = layer.getDesign();

		task.getTool().setCurrentLayer( getNextValidLayer( layer ) );

		try( Txn _ = Txn.create() ) {
			layer.getLayer().removeLayer( layer );
			if( optionalModel.isPresent() ) {
				DesignModel model = optionalModel.get();
				model.getViews().forEach( view -> view.removeLayer( layer ) );
				model.getPrints().forEach( print -> print.removeLayer( layer ) );
			}
		}

		return layer;
	}

	DesignLayer getNextValidLayer( DesignLayer layer ) {
		DesignLayer parent = layer.getLayer();
		List<DesignLayer> layers = parent.getLayers();
		int count = layers.size();
		int order = layers.indexOf( layer );

		DesignLayer next;
		if( count == 1 ) {
			next = parent;
		} else {
			next = layers.get( order == 0 ? order + 1 : order - 1 );
		}

		return next;
	}

}
