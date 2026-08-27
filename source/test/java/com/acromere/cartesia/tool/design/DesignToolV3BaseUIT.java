package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.BaseCartesiaUiTest;
import com.acromere.cartesia.DesignUnit;
import com.acromere.cartesia.data.DesignLayer;
import com.acromere.cartesia.data.DesignModel;
import com.acromere.cartesia.data.DesignShape;
import com.acromere.xenon.ProgramTool;
import com.acromere.xenon.ProgramToolEvent;
import com.acromere.xenon.resource.Resource;
import com.acromere.zerra.event.FxEventWatcher;
import com.acromere.zerra.javafx.Fx;
import javafx.geometry.Point3D;
import javafx.scene.shape.Shape;
import lombok.CustomLog;
import lombok.Getter;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.acromere.cartesia.TestTimeouts.FX_STABILITY_TIMEOUT;
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

		// Load the design resource into a tool
		URI uri = Objects.requireNonNull( getClass().getResource( "/design-tool-test.cartesia2d" ) ).toURI();
		Future<ProgramTool> future = getProgram().getResourceManager().openResource( uri, DesignToolV3.class );
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
			tool.setView( Point3D.ZERO, 2, 0 );
		} );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

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

		double size = designModel.calcDesignUnit().from( 2, DesignUnit.MM ) / getTool().getViewZoom();
		double selectTolerance = getTool().worldToScreen( size, size ).getX() - originX;
		assertThat( selectTolerance ).isCloseTo( 12.598425196850371, Offset.offset( 1e-15 ) );

		//Fx.waitForStability2( 1000 );
	}

	/**
	 * Get the select tolerance in design model units instead of select aperture units.
	 *
	 * @return the select tolerance in design model units
	 */
	protected double getWorldSelectTolerance() {
		return getTool().getSelectTolerance().to( getDesignModel().calcDesignUnit() ).value() / getTool().getViewZoom();
	}

	protected void useBoxLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e6" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );
	}

	protected void useLineLayer() throws TimeoutException, InterruptedException {
		Optional<DesignLayer> optional = getDesignModel().findLayerById( LINE_LAYER_ID );
		if( optional.isEmpty() ) return;

		DesignLayer layer = optional.get();
		Fx.run( () -> getTool().setLayerVisible( layer, true ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		List<DesignShape> shapes = layer.getShapes();
		Shape fxShape0 = getTool().getRenderer().getFxGeometry( shapes.get( 0 ) );
		assertThat( fxShape0.getStrokeWidth() ).isCloseTo( 3.15, Offset.offset( 0.01 ) );
		Shape fxShape1 = getTool().getRenderer().getFxGeometry( shapes.get( 1 ) );
		assertThat( fxShape1.getStrokeWidth() ).isCloseTo( 3.15, Offset.offset( 0.01 ) );
	}

	protected DesignLayer getLineLayer() {
		return getDesignModel().findLayerById( LINE_LAYER_ID ).orElseThrow();
	}

	protected void useEllipseLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e9" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );
	}

	protected void useArcLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0e8" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );
	}

	protected void useQuadLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ea" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );
	}

	protected void useCubicLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0eb" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );
	}

	protected void usePathLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ec" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );
	}

	protected void useMarkerLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ed" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );
	}

	protected void useTextLayer() throws TimeoutException, InterruptedException {
		getDesignModel().findLayerById( "a56cede9-ee12-40d0-a86c-b3701146c0ee" ).ifPresent( l -> Fx.run( () -> getTool().setLayerVisible( l, true ) ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );
	}

}
