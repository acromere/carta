package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.tool.BaseDesignTool;
import com.acromere.xenon.XenonProgramProduct;
import com.acromere.xenon.resource.OpenResourceRequest;
import com.acromere.xenon.resource.Resource;
import com.acromere.xenon.workpane.ToolException;
import lombok.CustomLog;

@CustomLog
public class DesignToolV3 extends BaseDesignTool {

	@SuppressWarnings( "unused" )
	public DesignToolV3( XenonProgramProduct product, Resource resource ) {
		this( product, resource, new DesignToolV3Renderer() );
	}

	DesignToolV3( XenonProgramProduct product, Resource resource, BaseDesignRenderer renderer ) {
		super( product, resource, renderer );
	}

	/**
	 * Called when both the tool and the resource are ready to be used.
	 *
	 * @param request The request to open the resource
	 * @throws ToolException If there is a problem preparing the tool for use
	 */
	@Override
	protected void ready( OpenResourceRequest request ) throws ToolException {
		super.ready( request );
	}

	@Override
	public Class<? extends BaseDesignRenderer> getPrintDesignRendererClass() {
		return DesignToolV3Renderer.class;
	}

}
