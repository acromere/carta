package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.BaseCartesiaUiTest;
import com.acromere.cartesia.DesignUnit;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignModel;
import com.acromere.xenon.ProgramTool;
import com.acromere.xenon.ProgramToolEvent;
import com.acromere.xenon.resource.Resource;
import com.acromere.zerra.event.FxEventWatcher;
import com.acromere.zerra.javafx.Fx;
import lombok.CustomLog;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Getter
@CustomLog
public abstract class DesignToolV3BaseUIT extends BaseCartesiaUiTest {

	public static final String LINE_LAYER_ID = "a56cede9-ee12-40d0-a86c-b3701146c0e7";

	protected DesignToolV3 tool;

	private Resource resource;

	private DesignModel designModel;

	protected double dpu;

	protected double width;

	protected double height;

	protected double originX;

	protected double originY;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();

		// Load the design asset into a tool
		URI uri = Objects.requireNonNull( getClass().getResource( "/design-tool-test.cartesia2d" ) ).toURI();
		Future<ProgramTool> future = getProgram().getResourceManager().openAsset( uri, DesignToolV3.class );
		tool = (DesignToolV3)future.get();
		assertNotNull( getTool() );

		// Wait for the tool to be ready
		FxEventWatcher<ProgramToolEvent> eventWatcher = new FxEventWatcher<>();
		tool.addEventHandler( ProgramToolEvent.READY, eventWatcher );
		eventWatcher.waitForEvent( ProgramToolEvent.READY );

		// Ensure the test resources are available
		this.resource = tool.getResource();
		this.designModel = tool.getDesignModel();
		assertNotNull( getResource() );
		assertNotNull( getDesignModel() );

		// Arguably, it shouldn't matter what the DPI is,
		// but it does affect some assertions later in the test
		Fx.run( () -> {
			tool.setDpi( 160 );
			tool.resize( 1920, 1080 );
			tool.setViewZoom( 2 );
		} );
		Fx.waitForStability( 1000 );

		// Check the design state
		assertThat( getDesignModel().calcDesignUnit() ).isEqualTo( DesignUnit.CM );
		assertThat( getDesignModel().getAllLayers().size() ).isEqualTo( 10 );

		// Check the tool state
		assertThat( getTool().getViewZoom() ).isEqualTo( 2 );
		assertThat( getTool().getVisibleLayers().size() ).isEqualTo( 0 );
		assertThat( getTool().getEnabledLayers().size() ).isEqualTo( 0 );
		assertThat( getTool().getVisibleShapes().size() ).isEqualTo( 0 );

		assertThat( getTool().getSelectTolerance().value() ).isEqualTo( 2 );
		assertThat( getTool().getSelectTolerance().unit() ).isEqualTo( DesignUnit.MM );

		dpu = DesignUnit.IN.from( getTool().getDpi(), getDesignModel().calcDesignUnit() );

		width = getTool().getWidth();
		height = getTool().getHeight();
		originX = 0.5 * width;
		originY = 0.5 * height;

		// FIXME Fx.waitforStability( 1000 ) did not fix flakiness
		Fx.waitForStability( 1000 );
		double size = designModel.calcDesignUnit().from( 2, DesignUnit.MM ) / getTool().getViewZoom();
		double selectTolerance = getTool().worldToScreen( size, size ).getX() - originX;
		// FIXME This assert is flaky
		// This value depends on the DPI and the select tolerance
		//assertThat( selectTolerance ).isCloseTo( 12.5984251968504, Offset.offset( 1e-10 ) );
		//assertThat( uncentered).isEqualTo( new Point2D( 0, 0));

		Fx.waitForStability( 1000 );
	}

	/**
	 * Get the select tolerance in design model units instead of select aperture units.
	 *
	 * @return the select tolerance in design model units
	 */
	protected double getWorldSelectTolerance() {
		return getTool().getSelectTolerance().to( getDesignModel().calcDesignUnit() ).value();
	}

	protected void useBoxLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e6" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( 1000 );
	}

	protected void useLineLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( LINE_LAYER_ID ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( 1000 );
	}

	protected DesignLayer getLineLayer() {
		return getDesignModel().findLayerById( LINE_LAYER_ID ).orElseThrow();
	}

	protected void useEllipseLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e9" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( 1000 );
	}

	protected void useArcLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e8" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( 1000 );
	}

	protected void useQuadLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ea" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( 1000 );
	}

	protected void useCubicLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0eb" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( 1000 );
	}

	protected void usePathLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ec" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( 1000 );
	}

	protected void useMarkerLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ed" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( 1000 );
	}

	protected void useTextLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ee" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( 1000 );
	}

}
