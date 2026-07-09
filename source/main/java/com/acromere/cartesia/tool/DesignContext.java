package com.acromere.cartesia.tool;

import com.acromere.cartesia.data.Design;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignShape;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.List;

/**
 * The DesignContext is for sharing design-related information between design
 * tools. It should only be accessed through the design tool and its associated
 * classes.
 * <pre>
 * {@link DesignTool} -> {@link Design} -> {@link DesignContext}
 * </pre>
 */
@Getter
public class DesignContext {

	// Should these layers be here or in the design?
	private final DesignLayer previewLayer;

	private final DesignLayer referenceLayer;

	private final ObservableList<DesignShape> selectedShapes;

	public DesignContext() {
		this.previewLayer = new DesignLayer();
		this.previewLayer.setName( "preview-layer" );
		this.referenceLayer = new DesignLayer();
		this.referenceLayer.setName( "reference-layer" );
		this.selectedShapes = FXCollections.synchronizedObservableList( FXCollections.observableArrayList() );
	}

	public List<DesignShape> getSelectedShapes() {
		return List.copyOf( selectedShapes );
	}

	public void setSelectedShapes( List<DesignShape> shapes, boolean selected ) {
		if( selected ) {
			selectedShapes.addAll( shapes );
		} else {
			selectedShapes.removeAll( shapes );
		}
	}

	public ObservableList<DesignShape> selectedShapes() {
		return selectedShapes;
	}

}
