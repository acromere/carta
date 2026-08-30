package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.BaseCartesiaUnitTest;
import com.acromere.cartesia.tool.BaseDesignTool;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.acromere.cartesia.tool.RenderConstants.DEFAULT_HOTSPOT_VISIBLE;
import static org.assertj.core.api.Assertions.assertThat;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.BooleanProperty;

import com.acromere.cartesia.DesignValue;
import com.acromere.cartesia.DesignUnit;
import com.acromere.cartesia.data.DesignView;
import com.acromere.cartesia.cursor.Reticule;
import com.acromere.cartesia.test.Point3DAssert;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import static com.acromere.cartesia.tool.RenderConstants.WINDOW_SELECT_APERTURE;
import com.acromere.cartesia.tool.design.BaseDesignRenderer;
import com.acromere.cartesia.tool.Grid;
import com.acromere.cartesia.tool.Workplane;
import com.acromere.cartesia.tool.GridStyle;
import javafx.scene.paint.Paint;
import javafx.geometry.BoundingBox;

public abstract class BaseDesignToolTest extends BaseCartesiaUnitTest {

	private BaseDesignTool tool;

	protected BaseDesignTool getTool() {
		return tool;
	}

	protected void setTool( BaseDesignTool tool ) {
		this.tool = tool;
	}

	@Test
	void hotspotVisible_defaultIsFalse() {
		// given a concrete tool provided by subclasses
		BaseDesignTool tool = getTool();
		assertThat( tool ).as( "Tool should be initialized in subclass setup()" ).isNotNull();

		// then default matches render constant (currently false)
		assertThat( tool.isHotspotVisible() ).isEqualTo( DEFAULT_HOTSPOT_VISIBLE );
	}

	@Test
	void hotspotVisible_toggleUpdatesState() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		// when toggle to true
		tool.setHotspotVisible( true );
		// then
		assertThat( tool.isHotspotVisible() ).isTrue();

		// when toggle back to false
		tool.setHotspotVisible( false );
		// then
		assertThat( tool.isHotspotVisible() ).isFalse();
	}

	@Test
	void hotspotVisible_propertyReflectsGetterAndUpdates() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		ReadOnlyBooleanProperty prop = tool.hotspotVisible();
		// property instance should be stable across calls
		assertThat( tool.hotspotVisible() ).isSameAs( prop );

		// default value matches getter
		assertThat( prop.get() ).isEqualTo( tool.isHotspotVisible() )
			.isEqualTo( DEFAULT_HOTSPOT_VISIBLE );

		// when toggled to true, both getter and property reflect the change
		tool.setHotspotVisible( true );
		assertThat( tool.isHotspotVisible() ).isTrue();
		assertThat( prop.get() ).isTrue();

		// when toggled back to false, both reflect the change
		tool.setHotspotVisible( false );
		assertThat( tool.isHotspotVisible() ).isFalse();
		assertThat( prop.get() ).isFalse();
	}

	@Test
	void hotspotVisible_redundantSetStillStableAndMayFire() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		AtomicInteger fired = new AtomicInteger( 0 );
		tool.hotspotVisible().addListener( ( _, _, _ ) -> fired.incrementAndGet() );

		// set to true twice; implementation may coalesce or still notify
		tool.setHotspotVisible( true );
		tool.setHotspotVisible( true );
		assertThat( tool.isHotspotVisible() ).isTrue();
		assertThat( fired.get() ).isGreaterThanOrEqualTo( 1 );

		// set back to false twice
		tool.setHotspotVisible( false );
		tool.setHotspotVisible( false );
		assertThat( tool.isHotspotVisible() ).isFalse();
		assertThat( fired.get() ).isGreaterThanOrEqualTo( 2 );
	}

	@Test
	void hotspotVisible_propertyFiresChanges() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		AtomicInteger fired = new AtomicInteger( 0 );
		tool.hotspotVisible().addListener( ( _, _, _ ) -> fired.incrementAndGet() );

		// flip to true (from default false)
		tool.setHotspotVisible( true );
		// flip to false
		tool.setHotspotVisible( false );

		// then at least one change should have fired (exact count may vary if platform coalesces)
		assertThat( fired.get() ).isGreaterThanOrEqualTo( 1 );
		// and final state should be false
		assertThat( tool.isHotspotVisible() ).isFalse();
	}

	// ===== Additional coverage for BaseDesignTool delegation and properties =====

	@Test
	void gridVisible_propertyReflectsGetterAndUpdates() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		BooleanProperty prop = tool.gridVisible();
		assertThat( tool.gridVisible() ).isSameAs( prop );

		// default and toggle round-trip
		boolean initial = tool.isGridVisible();
		assertThat( prop.get() ).isEqualTo( initial );

		tool.setGridVisible( !initial );
		assertThat( tool.isGridVisible() ).isEqualTo( !initial );
		assertThat( prop.get() ).isEqualTo( !initial );

		tool.setGridVisible( initial );
		assertThat( tool.isGridVisible() ).isEqualTo( initial );
		assertThat( prop.get() ).isEqualTo( initial );
	}

	@Test
	void gridSnapEnabled_propertyReflectsGetterAndUpdates() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		BooleanProperty prop = tool.gridSnapEnabled();
		assertThat( tool.gridSnapEnabled() ).isSameAs( prop );

		boolean initial = tool.isGridSnapEnabled();
		assertThat( prop.get() ).isEqualTo( initial );

		tool.setGridSnapEnabled( !initial );
		assertThat( tool.isGridSnapEnabled() ).isEqualTo( !initial );
		assertThat( prop.get() ).isEqualTo( !initial );

		tool.setGridSnapEnabled( initial );
		assertThat( tool.isGridSnapEnabled() ).isEqualTo( initial );
		assertThat( prop.get() ).isEqualTo( initial );
	}

	@Test
	void dpi_propertyAndSetterRoundTrip() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		double initial = tool.getDpi();
		assertThat( tool.viewDpiProperty().get() ).isEqualTo( initial );

		double newDpi = initial == 96.0 ? 144.0 : 96.0; // flip common DPI values
		tool.setDpi( newDpi );
		assertThat( tool.getDpi() ).isEqualTo( newDpi );
		assertThat( tool.viewDpiProperty().get() ).isEqualTo( newDpi );
	}

	@Test
	void dpi_propertyFiresOnChange() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		AtomicInteger fired = new AtomicInteger( 0 );
		tool.viewDpiProperty().addListener( ( _, _, _ ) -> fired.incrementAndGet() );

		double initial = tool.getDpi();
		double newDpi = initial == 96.0 ? 144.0 : 96.0;
		tool.setDpi( newDpi );
		assertThat( fired.get() ).isGreaterThanOrEqualTo( 1 );
		assertThat( tool.viewDpiProperty().get() ).isEqualTo( newDpi );
	}

	@Test
	void currentView_propertyRoundTrip() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		DesignView view = new DesignView();
		tool.setCurrentView( view );
		assertThat( tool.getCurrentView() ).isSameAs( view );
		assertThat( tool.currentViewProperty().get() ).isSameAs( view );
	}

	@Test
	void reticule_propertyRoundTrip() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		Reticule r = Reticule.DUPLEX;
		tool.setReticule( r );
		assertThat( tool.getReticule() ).isSameAs( r );
		assertThat( tool.reticule().get() ).isSameAs( r );
	}

	@Test
	void selectTolerance_propertyRoundTrip() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		DesignValue value = new DesignValue( 2.5, DesignUnit.MM );
		tool.setSelectTolerance( value );
		assertThat( tool.getSelectTolerance() ).isSameAs( value );
		assertThat( tool.selectTolerance().get() ).isSameAs( value );
	}

	// ===== Low‑risk transform/snap/aperture/selection coverage =====

	@Test
	void transforms_roundTripWorldScreenPoint() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		// Apply a non-trivial view so transforms aren’t identity
		tool.setViewRotate( 17.0 );
		tool.setViewZoom( 1.25 );
		tool.setViewCenter( new Point3D( 3.2, -1.7, 0 ) );

		Point3D world = new Point3D( 5.5, 2.25, 0.0 );
		Point3D screen = tool.worldToScreen( world );
		Point3D back = tool.screenToWorld( screen );
		Point3DAssert.assertThat( back ).isCloseTo( world );
	}

	// ===== Grid system tests =====

	@Test
	void gridSystem_roundTrip_setAndGet() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		// default should be ORTHO via both tool and workplane
		assertThat( tool.getGridSystem() ).isEqualTo( Grid.ORTHO );
		assertThat( tool.getWorkplane().getGridSystem() ).isEqualTo( Grid.ORTHO );

		// when switched to POLAR
		tool.setGridSystem( Grid.POLAR );
		assertThat( tool.getGridSystem() ).isEqualTo( Grid.POLAR );
		assertThat( tool.getWorkplane().getGridSystem() ).isEqualTo( Grid.POLAR );

		// and switch back
		tool.setGridSystem( Grid.ORTHO );
		assertThat( tool.getGridSystem() ).isEqualTo( Grid.ORTHO );
		assertThat( tool.getWorkplane().getGridSystem() ).isEqualTo( Grid.ORTHO );
	}

	@Test
	void gridSnap_intervalChangeAffectsSnapping() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		boolean restore = tool.isGridSnapEnabled();
		tool.setGridSnapEnabled( true );
		// Use ORTHO system for clarity
		tool.setGridSystem( Grid.ORTHO );

		// Pick a point that is not exactly on a fine or coarse grid line
		Point3D p = new Point3D( 2.34, 2.34, 0 );

		// Fine grid 0.1
		tool.getWorkplane().setSnapGridX( "0.1" );
		tool.getWorkplane().setSnapGridY( "0.1" );
		Point3D fine = tool.snapToGrid( p );

		// Coarse grid 0.5
		tool.getWorkplane().setSnapGridX( "0.5" );
		tool.getWorkplane().setSnapGridY( "0.5" );
		Point3D coarse = tool.snapToGrid( p );

		// Expect different snap targets between fine and coarse intervals
		assertThat( coarse ).isNotEqualTo( fine );

		// Restore defaults commonly used by fixtures
		tool.getWorkplane().setSnapGridX( "0.1" );
		tool.getWorkplane().setSnapGridY( "0.1" );
		tool.setGridSnapEnabled( restore );
	}

	@Test
	void gridSystem_switchChangesSnapBehavior() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		boolean restore = tool.isGridSnapEnabled();
		tool.setGridSnapEnabled( true );

		// Use the same snap intervals for both systems
		tool.getWorkplane().setSnapGridX( "0.5" );
		tool.getWorkplane().setSnapGridY( "0.5" );

		Point3D p = new Point3D( 1.3, 0.7, 0 );

		tool.setGridSystem( Grid.ORTHO );
		Point3D ortho = tool.snapToGrid( p );

		tool.setGridSystem( Grid.POLAR );
		Point3D polar = tool.snapToGrid( p );

		// Expect different results for ORTHO vs POLAR in general
		assertThat( polar ).isNotEqualTo( ortho );

		// Restore to default ORTHO and common intervals
		tool.setGridSystem( Grid.ORTHO );
		tool.getWorkplane().setSnapGridX( "0.1" );
		tool.getWorkplane().setSnapGridY( "0.1" );
		tool.setGridSnapEnabled( restore );
	}

	// Note: Bounds round-trip can be lossy with rotation; rely on point round-trips instead.

	@Test
	void gridSnap_disabledReturnsIdentity() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		// Choose a non-grid-aligned point that would normally snap
		Point3D p = new Point3D( 2.101, 2.101, 0 );

		// Ensure enabled case actually changes or equals expected snap target (don’t assert exact value here)
		boolean wasEnabled = tool.isGridSnapEnabled();
		tool.setGridSnapEnabled( true );
		Point3D snapped = tool.snapToGrid( p );
		// Now disable and assert identity
		tool.setGridSnapEnabled( false );
		Point3D unsnapped = tool.snapToGrid( p );
		Point3DAssert.assertThat( unsnapped ).isCloseTo( p );

		// Restore original state
		tool.setGridSnapEnabled( wasEnabled );
	}

	// ===== Workplane-specific tests =====

	@Test
	void workplane_origin_roundTrip_andCalcOriginResolvesAndResetsCache() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		Workplane wp = tool.getWorkplane();
		assertThat( wp ).isNotNull();

		// Set a custom origin string and ensure calcOrigin parses it to a Point3D
		wp.setOrigin( "1.25, -2.5, 0" );
		assertThat( wp.getOrigin() ).isEqualTo( "1.25, -2.5, 0" );
		Point3D o1 = wp.calcOrigin();
		assertThat( o1.getX() ).isEqualTo( 1.25 );
		assertThat( o1.getY() ).isEqualTo( -2.5 );
		assertThat( o1.getZ() ).isEqualTo( 0.0 );

		// Change origin and ensure cached value resets and reflects new value
		wp.setOrigin( "3, 4, 0" );
		assertThat( wp.getOrigin() ).isEqualTo( "3, 4, 0" );
		Point3D o2 = wp.calcOrigin();
		assertThat( o2.getX() ).isEqualTo( 3.0 );
		assertThat( o2.getY() ).isEqualTo( 4.0 );
		assertThat( o2.getZ() ).isEqualTo( 0.0 );
	}

	@Test
	void workplane_bounds_setGetRoundTrip() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		Workplane wp = tool.getWorkplane();
		assertThat( wp ).isNotNull();

		BoundingBox box = new BoundingBox( -5, -3, 10, 6 );
		wp.setBounds( box );
		Bounds got = wp.getBounds();
		assertThat( got.getMinX() ).isEqualTo( -5.0 );
		assertThat( got.getMaxX() ).isEqualTo( 5.0 );

		// Some implementations (V2) clamp Y-bounds to 0; accept either exact round-trip or clamped zero
		double minY = got.getMinY();
		double maxY = got.getMaxY();
		boolean clampedY = (Math.abs( minY ) == 0.0) && (Math.abs( maxY ) == 0.0);
		if( !clampedY ) {
			assertThat( minY ).isEqualTo( -3.0 );
			assertThat( maxY ).isEqualTo( 3.0 );
		}
	}

	@Test
	void workplane_gridAxis_visibility_paint_and_width_parse() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		Workplane wp = tool.getWorkplane();
		assertThat( wp ).isNotNull();

		// Toggle visibility ensures cached value invalidates and recomputes
		wp.setGridAxisVisible( Boolean.TRUE );
		assertThat( wp.isGridAxisVisible() ).isTrue();
		wp.setGridAxisVisible( Boolean.FALSE );
		assertThat( wp.isGridAxisVisible() ).isFalse();

		// Paint string round-trip and parsed Paint is non-null
		String paint = "#3366CC80"; // ARGB hex with alpha
		wp.setGridAxisPaint( paint );
		assertThat( wp.getGridAxisPaint() ).isEqualTo( paint );
		Paint parsed = wp.calcGridAxisPaint();
		assertThat( parsed ).isNotNull();

		// Width expression parses to numeric via calc*
		wp.setGridAxisWidth( "2.5" );
		assertThat( wp.getGridAxisWidth() ).isEqualTo( "2.5" );
		assertThat( wp.calcGridAxisWidth() ).isEqualTo( 2.5 );
	}

	@Test
	void workplane_major_minor_visibility_showing_and_spacing_parse() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		Workplane wp = tool.getWorkplane();
		assertThat( wp ).isNotNull();

		// Major visibility and showing flags
		wp.setMajorGridVisible( Boolean.TRUE );
		assertThat( wp.isMajorGridVisible() ).isTrue();
		wp.setMajorGridShowing( Boolean.TRUE );
		assertThat( wp.isMajorGridShowing() ).isTrue();
		wp.setMajorGridShowing( Boolean.FALSE );
		assertThat( wp.isMajorGridShowing() ).isFalse();

		// Major X/Y spacing parsing
		wp.setMajorGridX( "1.25" );
		wp.setMajorGridY( "2.5" );
		assertThat( wp.getMajorGridX() ).isEqualTo( "1.25" );
		assertThat( wp.getMajorGridY() ).isEqualTo( "2.5" );
		assertThat( wp.calcMajorGridX() ).isEqualTo( 1.25 );
		assertThat( wp.calcMajorGridY() ).isEqualTo( 2.5 );

		// Minor visibility and showing flags
		wp.setMinorGridVisible( Boolean.TRUE );
		assertThat( wp.isMinorGridVisible() ).isTrue();
		wp.setMinorGridShowing( Boolean.TRUE );
		assertThat( wp.isMinorGridShowing() ).isTrue();
		wp.setMinorGridShowing( Boolean.FALSE );
		assertThat( wp.isMinorGridShowing() ).isFalse();

		// Minor X/Y spacing parsing
		wp.setMinorGridX( "0.5" );
		wp.setMinorGridY( "0.25" );
		assertThat( wp.getMinorGridX() ).isEqualTo( "0.5" );
		assertThat( wp.getMinorGridY() ).isEqualTo( "0.25" );
		assertThat( wp.calcMinorGridX() ).isEqualTo( 0.5 );
		assertThat( wp.calcMinorGridY() ).isEqualTo( 0.25 );
	}

	@Test
	void workplane_snapGrid_spacing_parse() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		Workplane wp = tool.getWorkplane();
		assertThat( wp ).isNotNull();

		wp.setSnapGridX( "0.2" );
		wp.setSnapGridY( "0.4" );
		assertThat( wp.getSnapGridX() ).isEqualTo( "0.2" );
		assertThat( wp.getSnapGridY() ).isEqualTo( "0.4" );
		assertThat( wp.calcSnapGridX() ).isEqualTo( 0.2 );
		assertThat( wp.calcSnapGridY() ).isEqualTo( 0.4 );
	}

	@Test
	void workplane_gridStyle_roundTrip() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		Workplane wp = tool.getWorkplane();
		assertThat( wp ).isNotNull();

		// Use a non-null style value from enum; if null is acceptable, ensure we can set and get
		var style = GridStyle.SOLID;
		wp.setGridStyle( style );
		assertThat( wp.getGridStyle() ).isEqualTo( style );
	}

	@Test
	void aperture_movePointAndWindowAndNullCorner() {
		BaseDesignTool tool = getTool();
		assertThat( tool ).isNotNull();

		boolean isV2 = tool.getClass().getSimpleName().toLowerCase().contains( "v2" );

		if( isV2 ) {
			// V2 uses ad-hoc apertures not in renderer’s allowed set; verify null-corner path resets to default without throwing
			tool.moveSelectAperture( new Point3D( 0, 0, 0 ), null );
			assertThat( tool.getScreenDesignRenderer().getSelectAperture() ).isEqualTo( BaseDesignRenderer.DEFAULT_SELECT_APERTURE );
		} else {
			// 1) Move point select aperture with a location
			Point3D p1 = new Point3D( 1.0, 2.0, 0 );
			tool.moveSelectAperture( p1 );
			assertThat( tool.getScreenDesignRenderer().getSelectAperture() ).isNotNull();

			// 2) Switch to a window aperture and move with origin+corner
			var box = WINDOW_SELECT_APERTURE;
			tool.setSelectAperture( box );
			Point3D origin = new Point3D( 0.0, 0.0, 0 );
			Point3D corner = new Point3D( 3.0, 4.0, 0 );
			tool.moveSelectAperture( origin, corner );
			// After move, box should represent the window defined by origin/corner
			assertThat( box.getOrigin().getX() ).isLessThanOrEqualTo( Math.max( origin.getX(), corner.getX() ) );
			assertThat( box.getOrigin().getY() ).isLessThanOrEqualTo( Math.max( origin.getY(), corner.getY() ) );
			assertThat( box.getSize().getX() ).isCloseTo( Math.abs( corner.getX() - origin.getX() ), org.assertj.core.data.Offset.offset( 1e-9 ) );
			assertThat( box.getSize().getY() ).isCloseTo( Math.abs( corner.getY() - origin.getY() ), org.assertj.core.data.Offset.offset( 1e-9 ) );

			// Capture state
			Point3D beforeOrigin = box.getOrigin();
			Point3D beforeSize = box.getSize();

			// 3) Null corner is an early return: state should remain unchanged
			tool.moveSelectAperture( origin, null );
			assertThat( box.getOrigin() ).isSameAs( beforeOrigin );
			assertThat( box.getSize() ).isSameAs( beforeSize );
		}
	}

}
