package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.cartesia.tool.BaseDesignTool;
import com.acromere.xenon.XenonProgramProduct;
import com.acromere.xenon.resource.OpenAssetRequest;
import com.acromere.xenon.resource.Resource;
import com.acromere.xenon.workpane.ToolException;
import javafx.geometry.Point3D;
import javafx.scene.paint.Paint;
import lombok.CustomLog;

import java.util.Collection;
import java.util.List;

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
	 * Called when both the tool and the asset are ready to be used.
	 *
	 * @param request The request to open the asset
	 * @throws ToolException If there is a problem preparing the tool for use
	 */
	@Override
	protected void ready( OpenAssetRequest request ) throws ToolException {
		// Most logic has been moved to the super.ready() method
		super.ready( request );

		// super.ready() already set the design

		// TODO What is special about DesignToolV3?

		// DEVELOPMENT
		//		if( Objects.equals( getProgram().getMode(), XenonMode.DEV ) ) {
		//			Design developmentDesign = ExampleDesigns.design1();
		//			getRenderer().setDesign( developmentDesign );
		//			if( !developmentDesign.getLayers().getLayers().isEmpty() ) {
		//				getRenderer().setLayerVisible( developmentDesign.getLayers().getLayers().getFirst(), true );
		//			}
		//		}
	}

	@Override
	public Point3D nearestReferencePoint( Collection<DesignShape> shapes, Point3D point ) {
		return null;
	}

	@Override
	public DesignLayer getPreviewLayer() {
		return getDesignContext().getPreviewLayer();
	}

	@Override
	public DesignLayer getReferenceLayer() {
		return getDesignContext().getReferenceLayer();
	}

	@Override
	public List<DesignShape> getVisibleShapes() {
		return List.of();
	}

	@Override
	public Paint getSelectedDrawPaint() {
		return null;
	}

	@Override
	public Paint getSelectedFillPaint() {
		return null;
	}

	@Override
	public boolean isReferenceLayerVisible() {
		return false;
	}

	@Override
	public void setReferenceLayerVisible( boolean visible ) {

	}

	@Override
	public Point3D scaleScreenToWorld( Point3D point ) {
		return null;
	}

	@Override
	public Point3D scaleWorldToScreen( Point3D point ) {
		return null;
	}

	@Override
	public void setSelectAperture( Point3D anchor, Point3D mouse ) {

	}

	@Override
	public List<DesignShape> screenPointSyncFindOne( Point3D mouse ) {
		return List.of();
	}

	@Override
	public List<DesignShape> worldPointSyncFindOne( Point3D mouse ) {
		return List.of();
	}

	@Override
	public List<DesignShape> screenPointSyncFindAll( Point3D mouse ) {
		return List.of();
	}

	@Override
	public List<DesignShape> worldPointSyncFindAll( Point3D mouse ) {
		return List.of();
	}

	@Override
	public List<DesignShape> screenPointSyncSelect( Point3D mouse ) {
		return List.of();
	}

	@Override
	public List<DesignShape> worldPointSyncSelect( Point3D mouse ) {
		return List.of();
	}

	@Override
	public void screenPointSelect( Point3D mouse ) {

	}

	@Override
	public void screenPointSelect( Point3D mouse, boolean toggle ) {

	}

	@Override
	public void screenWindowSelect( Point3D a, Point3D b, boolean intersect, boolean toggle ) {

	}

	@Override
	public void worldPointSelect( Point3D point ) {

	}

	@Override
	public void worldPointSelect( Point3D point, boolean toggle ) {

	}

	@Override
	public void worldWindowSelect( Point3D a, Point3D b, boolean intersect, boolean toggle ) {

	}

	@Override
	public Class<? extends BaseDesignRenderer> getPrintDesignRendererClass() {
		return DesignToolV3Renderer.class;
	}

}
