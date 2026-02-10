package com.acromere.cartesia.tool;

import com.acromere.cartesia.RbKey;
import com.acromere.cartesia.data.DesignModel;
import com.acromere.cartesia.data.DesignNode;
import com.acromere.cartesia.data.DesignPrint;
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
public class DesignToolPrintsGuide extends Guide {

	private final XenonProgramProduct product;

	private final BaseDesignTool tool;

	private final Map<DesignNode, GuideNode> printNodes;

	private final Map<GuideNode, DesignNode> nodePrints;

	public DesignToolPrintsGuide( XenonProgramProduct product, BaseDesignTool tool ) {
		this.product = product;
		this.tool = tool;
		this.printNodes = new ConcurrentHashMap<>();
		this.nodePrints = new ConcurrentHashMap<>();
		setTitle( Rb.textOr( RbKey.LABEL, "prints", "Prints" ) );
		setIcon( "prints" );
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
		design.getPrints().forEach( this::addPrint );

		// Add listeners for changes
		design.register( NodeEvent.CHILD_ADDED, e -> {
			if( DesignModel.PRINTS.equals( e.getSetKey() ) ) Fx.run( () -> addPrint( e.getNewValue() ) );
		} );
		design.register( NodeEvent.CHILD_REMOVED, e -> {
			if( DesignModel.PRINTS.equals( e.getSetKey() ) ) Fx.run( () -> removePrint( e.getOldValue() ) );
		} );
	}

	private void addPrint( DesignPrint print ) {
		GuideNode printGuideNode = new GuideNode( getProgram(), print.getId(), print.getName(), "print", print.getOrder() );
		addNode( getRoot().getValue(), printGuideNode );
		printNodes.put( print, printGuideNode );
		nodePrints.put( printGuideNode, print );
	}

	private void removePrint( DesignPrint print ) {
		removeNode( printNodes.get( print ) );
		nodePrints.remove( printNodes.get( print ) );
		printNodes.remove( print );
	}

}
