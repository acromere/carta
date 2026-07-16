package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.BaseToolTest;
import com.acromere.cartesia.CartesiaTestTag;
import com.acromere.cartesia.Design2dResourceType;
import com.acromere.cartesia.data.Design;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignModel;
import com.acromere.cartesia.test.Point3DAssert;
import com.acromere.cartesia.tool.DesignPortal;
import com.acromere.cartesia.tool.Grid;
import com.acromere.marea.fx.FxRenderer2d;
import com.acromere.xenon.resource.OpenAssetRequest;
import com.acromere.xenon.resource.Resource;
import com.acromere.zerra.javafx.Fx;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.stage.Screen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DesignToolV3Test extends BaseToolTest {

	private static final double DPC = FxRenderer2d.DEFAULT_DPI / 2.54;

	private DesignToolV3Renderer renderer;

	private DesignToolV3 tool;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		DesignModel model = ExampleDesigns.redBlueX();
		Design<DesignModel> design = new Design<>( model );

		Resource resource = new Resource( new Design2dResourceType( getProgram() ), URI.create( "new://test" ) ).setModel( design );
		resource.setModel( design );

		renderer = Mockito.spy( new DesignToolV3Renderer() );
		tool = Mockito.spy( new DesignToolV3( module, resource, renderer ) );

		// Before calling tool.ready(), reset the module and tool settings
		getMod().getSettings().delete();
		tool.getSettings().delete();

		OpenAssetRequest request = new OpenAssetRequest();
		request.setResource( resource );
		tool.ready( request );

		Design<DesignModel> resourceModel = tool.getResource().getModel();
		assertThat( resourceModel ).isEqualTo( design );
		assertThat( resourceModel.getDataModel() ).isEqualTo( model );
		assertThat( tool.getDesignModel() ).isEqualTo( model );

		// Post-setup checks
		verify( renderer, times( 5 ) ).visibleLayers();
		verify( renderer, times( 3 ) ).enabledLayers();
		verify( renderer, times( 1 ) ).gridVisible();
		verify( tool, times( 1 ) ).currentLayerProperty();
		verify( tool, times( 2 ) ).selectedLayerProperty();
		verify( tool, times( 2 ) ).gridSnapEnabled();

		// Reset the invocation counts
		Mockito.clearInvocations( renderer );
	}

	@Test
	void constructor() {
		assertThat( tool ).isNotNull();
	}

	@Test
	void defaultViewCenter() {
		// when
		Point3D result = tool.getViewCenter();

		// then
		assertThat( result ).isEqualTo( DesignToolV3.DEFAULT_CENTER );
	}

	@Test
	void setViewCenter() {
		// given
		Point3D center = new Point3D( 3, 2, 1 );

		// when
		tool.setViewCenter( center );

		// then
		assertThat( tool.getViewCenter() ).isEqualTo( center );
	}

	@Test
	void defaultRotate() {
		// when
		double result = tool.getViewRotate();

		// then
		assertThat( result ).isEqualTo( DesignToolV3.DEFAULT_ROTATE );
	}

	@Test
	void setViewRotate() {
		// given
		double rotate = 123.45;

		// when
		tool.setViewRotate( rotate );

		// then
		assertThat( tool.getViewRotate() ).isEqualTo( rotate );
	}

	@Test
	void defaultViewZoom() {
		// when
		double result = tool.getViewZoom();

		// then
		assertThat( result ).isEqualTo( DesignToolV3.DEFAULT_ZOOM );
	}

	@Test
	void setViewZoom() {
		// given
		double zoom = 123.45;

		// when
		tool.setViewZoom( zoom );

		// then
		assertThat( tool.getViewZoom() ).isEqualTo( zoom );
	}

	@Test
	void setViewWithDesignPortal() {
		// given
		DesignPortal portal = new DesignPortal( new Point3D( 1, 2, 3 ), 123.45, 123.45 );

		// when
		tool.setView( portal );

		// then
		assertThat( tool.getViewCenter() ).isEqualTo( portal.center() );
		assertThat( tool.getViewZoom() ).isEqualTo( portal.zoom() );
		assertThat( tool.getViewRotate() ).isEqualTo( portal.rotate() );
	}

	@Test
	void setViewWithViewpointZoom() {
		// given
		Point3D center = new Point3D( 1, 2, 3 );
		double zoom = 123.46;

		// when
		tool.setView( center, zoom );

		// then
		assertThat( tool.getViewCenter() ).isEqualTo( center );
		assertThat( tool.getViewZoom() ).isEqualTo( zoom );
		assertThat( tool.getViewRotate() ).isEqualTo( DesignToolV3.DEFAULT_ROTATE );
	}

	@Test
	void setViewWithViewpointRotateZoom() {
		// given
		Point3D center = new Point3D( 1, 2, 3 );
		double rotate = 123.45;
		double zoom = 678.90;

		// when
		tool.setView( center, zoom, rotate );

		// then
		assertThat( tool.getViewCenter() ).isEqualTo( center );
		assertThat( tool.getViewRotate() ).isEqualTo( rotate );
		assertThat( tool.getViewZoom() ).isEqualTo( zoom );
	}

	@Test
	void initialDpi() {
		// when
		double result = tool.getDpi();

		// then
		assertThat( result ).isEqualTo( Screen.getPrimary().getDpi() );
	}

	@Test
	void setDpi() {
		// given
		double dpi = 123.45;

		// when
		tool.setDpi( dpi );

		// then
		assertThat( tool.getDpi() ).isEqualTo( dpi );
	}

	/**
	 * This test ensures that FX geometry is resized when the DPI is changed
	 * in the tool. This test relies on the somewhat complicated implementation of
	 * the renderer, even though the API is exposed here at the tool level.
	 */
	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void updateDpi() {
		tool.setDpi( 2 * DesignToolV3.DEFAULT_DPI );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).setDpi( 2 * DesignToolV3.DEFAULT_DPI, 2 * DesignToolV3.DEFAULT_DPI );
	}

	@Test
	void defaultCursor() {
		// when
		Cursor result = tool.getCursor();

		// then
		assertThat( result ).isEqualTo( Cursor.DEFAULT );
		assertThat( result ).isInstanceOf( Cursor.class );
	}

	@Test
	void setCursor() throws Exception {
		// given
		Cursor currentCursor = Cursor.DEFAULT;
		Cursor differentCursor = Cursor.CROSSHAIR;
		CompletableFuture<Cursor> future = new CompletableFuture<>();
		Fx.run( () -> future.complete( tool.getCursor() ) );
		Cursor cursor = future.get();
		assertThat( cursor ).isEqualTo( currentCursor );

		// when
		tool.setCursor( differentCursor );
		Cursor result = tool.getCursor();

		// then
		assertThat( result.toString() ).isEqualTo( differentCursor.toString() );
		assertThat( result ).isInstanceOf( Cursor.class );
	}

	@Test
	void setWorldViewportWithToolWidthAndHeightAtZero() {
		// given
		assertThat( tool.getWidth() ).isEqualTo( 0 );
		assertThat( tool.getHeight() ).isEqualTo( 0 );
		assertThat( tool.getViewCenter() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( tool.getViewZoom() ).isEqualTo( 1 );

		// when
		tool.setWorldViewport( new BoundingBox( 0, 0, 1, 1 ) );

		// then
		assertThat( tool.getWidth() ).isEqualTo( 0 );
		assertThat( tool.getHeight() ).isEqualTo( 0 );
		assertThat( tool.getViewCenter() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( tool.getViewZoom() ).isEqualTo( 1 );
	}

	@Test
	void setWorldViewport() {
		// given
		tool.resize( 1000, 1000 );
		assertThat( tool.getWidth() ).isEqualTo( 1000 );
		assertThat( tool.getHeight() ).isEqualTo( 1000 );
		assertThat( tool.getViewCenter() ).isEqualTo( new Point3D( 0, 0, 0 ) );
		assertThat( tool.getViewZoom() ).isEqualTo( 1 );

		// when
		tool.setWorldViewport( new BoundingBox( 400, 500, 100, 100 ) );

		// then
		assertThat( tool.getViewCenter() ).isEqualTo( new Point3D( 450, 550, 0 ) );
		assertThat( tool.getViewZoom() ).isEqualTo( 0.254 );
		assertThat( tool.getWidth() ).isEqualTo( 1000 );
		assertThat( tool.getHeight() ).isEqualTo( 1000 );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void isLayerEnabled() {
		DesignLayer layer = new DesignLayer().setName( "layer-0" );
		tool.isLayerEnabled( layer );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).isLayerEnabled( eq( layer ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void setLayerEnabled() {
		DesignLayer layer = new DesignLayer().setName( "layer-0" );
		tool.setLayerEnabled( layer, true );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).setLayerEnabled( eq( layer ), eq( true ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void getEnabledLayers() {
		tool.getEnabledLayers();

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).getEnabledLayers();
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void setEnabledLayers() {
		DesignLayer layer = new DesignLayer().setName( "layer-0" );
		tool.setEnabledLayers( Set.of( layer ) );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).setEnabledLayers( eq( Set.of( layer ) ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void enabledLayers() {
		// given
		verify( renderer, times( 0 ) ).enabledLayers();

		// when
		tool.enabledLayers();

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).enabledLayers();
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void isLayerVisible() {
		DesignLayer layer = new DesignLayer().setName( "layer-0" );
		tool.isLayerVisible( layer );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).isLayerVisible( eq( layer ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void setLayerVisible() {
		DesignLayer layer = new DesignLayer().setName( "layer-0" );
		tool.setLayerVisible( layer, true );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).setLayerVisible( eq( layer ), eq( true ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void getVisibleLayers() {
		tool.getVisibleLayers();

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).getVisibleLayers();
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void setVisibleLayers() {
		DesignLayer layer = new DesignLayer().setName( "layer-0" );
		tool.setVisibleLayers( Set.of( layer ) );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).setVisibleLayers( Set.of( layer ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void visibleLayers() {
		// given
		verify( renderer, times( 0 ) ).visibleLayers();

		// when
		tool.visibleLayers();

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).visibleLayers();
	}

	@Test
	void testScreenToWorkplaneWithCoordinatesAndSnapEnabled() {
		// given
		tool.setDpi( FxRenderer2d.DEFAULT_DPI );
		assertThat( tool.isGridSnapEnabled() ).isTrue();
		assertThat( tool.getWorkplane().calcSnapGridX() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().calcSnapGridY() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().getGridSystem() ).isEqualTo( Grid.ORTHO );

		// then
		Point3DAssert.assertThat( tool.screenToWorkplane( 2 * DPC, -2 * DPC, 0 ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.screenToWorkplane( 2.001 * DPC, -2.002 * DPC, 0 ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.screenToWorkplane( 2.101 * DPC + 0.1, -2.101 * DPC, 0 ) ).isEqualTo( new Point3D( 2.1, 2.1, 0 ) );
	}

	@Test
	void testScreenToWorkplaneWithPoints() {
		// given
		tool.setDpi( FxRenderer2d.DEFAULT_DPI );
		assertThat( tool.isGridSnapEnabled() ).isTrue();
		assertThat( tool.getWorkplane().calcSnapGridX() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().calcSnapGridY() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().getGridSystem() ).isEqualTo( Grid.ORTHO );

		// then
		Point3DAssert.assertThat( tool.screenToWorkplane( new Point3D( 2 * DPC, -2 * DPC, 0 ) ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.screenToWorkplane( new Point3D( 2.001 * DPC, -2.001 * DPC, 0 ) ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.screenToWorkplane( new Point3D( 2.101 * DPC + 0.1, -2.101 * DPC, 0 ) ) ).isEqualTo( new Point3D( 2.1, 2.1, 0 ) );
	}

	@Test
	void testSnapToGridWithCoordinatesAndSnapEnabled() {
		// given
		assertThat( tool.isGridSnapEnabled() ).isTrue();
		assertThat( tool.getWorkplane().calcSnapGridX() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().calcSnapGridY() ).isEqualTo( 0.1 );

		// then
		Point3DAssert.assertThat( tool.snapToGrid( 2, 2, 0 ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.snapToGrid( 2.01, 2.01, 0 ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.snapToGrid( 2.101, 2.101, 0 ) ).isEqualTo( new Point3D( 2.1, 2.1, 0 ) );
	}

	@Test
	void testSnapToGridWithPoints() {
		// given
		assertThat( tool.isGridSnapEnabled() ).isTrue();
		assertThat( tool.getWorkplane().calcSnapGridX() ).isEqualTo( 0.1 );
		assertThat( tool.getWorkplane().calcSnapGridY() ).isEqualTo( 0.1 );

		// then
		Point3DAssert.assertThat( tool.snapToGrid( new Point3D( 2, 2, 0 ) ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.snapToGrid( new Point3D( 2.01, 2.01, 0 ) ) ).isEqualTo( new Point3D( 2.0, 2.0, 0 ) );
		Point3DAssert.assertThat( tool.snapToGrid( new Point3D( 2.101, 2.101, 0 ) ) ).isEqualTo( new Point3D( 2.1, 2.1, 0 ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void screenToWorldWithPoint2D() {
		tool.screenToWorld( new Point2D( 1, 2 ) );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).screenToWorld( new Point2D( 1, 2 ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void screenToWorldWithDoubleDouble() {
		tool.screenToWorld( 3, 4 );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).screenToWorld( 3, 4 );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void screenToWorldWithPoint3D() {
		tool.screenToWorld( new Point3D( 1, 2, 3 ) );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).screenToWorld( new Point3D( 1, 2, 3 ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void screenToWorldWithDoubleDoubleDouble() {
		tool.screenToWorld( 3, 4, 5 );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).screenToWorld( 3, 4, 5 );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void screenToWorldWithBounds() {
		tool.screenToWorld( new BoundingBox( 1, 2, 3, 4 ) );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).screenToWorld( new BoundingBox( 1, 2, 3, 4 ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void worldToScreenWithPoint2D() {
		tool.worldToScreen( new Point2D( 1, 2 ) );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).worldToScreen( new Point2D( 1, 2 ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void worldToScreenWithDoubleDouble() {
		tool.worldToScreen( 3, 4 );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).worldToScreen( 3, 4 );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void worldToScreenWithPoint3D() {
		tool.worldToScreen( new Point3D( 1, 2, 3 ) );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).worldToScreen( new Point3D( 1, 2, 3 ) );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void worldToScreenWithDoubleDoubleDouble() {
		tool.worldToScreen( 3, 4, 5 );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).worldToScreen( 3, 4, 5 );
	}

	@Test
	@Tag( CartesiaTestTag.WHITE_BOX )
	void worldToScreenWithBounds() {
		tool.worldToScreen( new BoundingBox( 4, 3, 2, 1 ) );

		// Check that it delegates to the renderer
		verify( renderer, times( 1 ) ).worldToScreen( new BoundingBox( 4, 3, 2, 1 ) );
	}

}
