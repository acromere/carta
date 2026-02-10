package com.acromere.cartesia.tool;

import com.acromere.cartesia.RbKey;
import com.acromere.cartesia.data.DesignModel;
import com.acromere.cartesia.data.DesignNode;
import com.acromere.cartesia.data.DesignView;
import com.acromere.data.NodeEvent;
import com.acromere.product.Rb;
import com.acromere.xenon.Xenon;
import com.acromere.xenon.XenonProgramProduct;
import com.acromere.xenon.tool.guide.Guide;
import com.acromere.xenon.tool.guide.GuideNode;
import com.acromere.zerra.javafx.Fx;
import lombok.CustomLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
@CustomLog
public class DesignToolViewsGuide extends Guide {

	private final XenonProgramProduct product;

	private final BaseDesignTool tool;

	private final Map<DesignNode, GuideNode> viewNodes;

	private final Map<GuideNode, DesignNode> nodeViews;

	public DesignToolViewsGuide( XenonProgramProduct product, BaseDesignTool tool ) {
		this.product = product;
		this.tool = tool;
		this.viewNodes = new ConcurrentHashMap<>();
		this.nodeViews = new ConcurrentHashMap<>();
		setTitle( Rb.textOr( RbKey.LABEL, "views", "Views" ) );
		setIcon( "views" );
	}

	XenonProgramProduct getProduct() {
		return product;
	}

	Xenon getProgram() {
		return product.getProgram();
	}

	public void link() {
		DesignModel design = tool.getDesign();

		// Populate the guide
		design.getViews().forEach( this::addView );

		// Add listeners for changes
		design.register( NodeEvent.CHILD_ADDED, e -> {
			if( DesignModel.VIEWS.equals( e.getSetKey() ) ) Fx.run( () -> addView( e.getNewValue() ) );
		} );
		design.register( NodeEvent.CHILD_REMOVED, e -> {
			if( DesignModel.VIEWS.equals( e.getSetKey() ) ) Fx.run( () -> removeView( e.getOldValue() ) );
		} );
	}

	private void addView( DesignView view ) {
		GuideNode viewGuideNode = new GuideNode( getProgram(), view.getId(), view.getName(), "view", view.getOrder() );
		addNode( getRoot().getValue(), viewGuideNode );
		viewNodes.put( view, viewGuideNode );
		nodeViews.put( viewGuideNode, view );
	}

	private void removeView( DesignView view ) {
		removeNode( viewNodes.get( view ) );
		nodeViews.remove( viewNodes.get( view ) );
		viewNodes.remove( view );
	}

}
