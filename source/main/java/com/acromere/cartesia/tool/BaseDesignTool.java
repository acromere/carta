package com.acromere.cartesia.tool;

import com.acromere.annotation.Note;
import com.acromere.cartesia.*;
import com.acromere.cartesia.RbKey;
import com.acromere.cartesia.cursor.Reticule;
import com.acromere.cartesia.cursor.ReticuleCursor;
import com.acromere.cartesia.data.*;
import com.acromere.cartesia.data.map.DesignUnitMapper;
import com.acromere.cartesia.data.util.DesignPropertiesMap;
import com.acromere.cartesia.snap.Snap;
import com.acromere.cartesia.snap.SnapGrid;
import com.acromere.cartesia.tool.design.BaseDesignRenderer;
import com.acromere.cartesia.tool.design.DesignToolEvent;
import com.acromere.cartesia.tool.design.LayerGuide;
import com.acromere.data.IdNode;
import com.acromere.data.MultiNodeSettings;
import com.acromere.data.NodeSettings;
import com.acromere.product.Rb;
import com.acromere.settings.Settings;
import com.acromere.skill.WritableIdentity;
import com.acromere.util.DelayedAction;
import com.acromere.util.IdGenerator;
import com.acromere.util.TypeReference;
import com.acromere.xenon.*;
import com.acromere.xenon.resource.OpenAssetRequest;
import com.acromere.xenon.resource.Resource;
import com.acromere.xenon.resource.ResourceSwitchedEvent;
import com.acromere.xenon.resource.type.ProgramPropertiesType;
import com.acromere.xenon.task.Task;
import com.acromere.xenon.tool.guide.GuideNode;
import com.acromere.xenon.tool.guide.GuidedTool;
import com.acromere.xenon.tool.settings.SettingsPage;
import com.acromere.xenon.workpane.ToolException;
import com.acromere.xenon.workpane.Workpane;
import com.acromere.xenon.workspace.StatusBar;
import com.acromere.xenon.workspace.Workspace;
import com.acromere.zerra.color.Paints;
import com.acromere.zerra.event.FxEventHub;
import com.acromere.zerra.javafx.Fx;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.GestureEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
import lombok.CustomLog;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The design tool is the base class for all design tools.
 */
@CustomLog
public abstract class BaseDesignTool extends GuidedTool implements DesignTool, EventTarget, WritableIdentity {

	/**
	 * All tools share the same grid snap object
	 */
	protected static final Snap gridSnap = new SnapGrid();

	// GUIDES

	/**
	 * The layer guide for this tool.
	 */
	@Getter
	private final LayerGuide layerGuide;

	/**
	 * The view guide for this tool.
	 */
	//	private final ViewGuide viewGuide;

	/**
	 * The print guide for this tool.
	 */
	//	private final PrintGuide printGuide;

	/**
	 * The toast prompt for the tool. Shows initialization messages to the user.
	 * Initially this shows the resource is "loading" but then may show other
	 * messages if there was an error or other problem. If the resource is
	 * successfully loaded, the toast prompt is hidden.
	 */
	@Getter
	private final Node toast;

	@Getter
	private final BaseDesignRenderer renderer;

	/**
	 * The stack of portals the user has used to view the design in this tool.
	 */
	private final Stack<DesignPortal> portalStack;

	// FX properties (what others should be here?)

	// Current:
	// selectAperture

	/**
	 * The current layer for adding geometry to the design.
	 */
	private final ObjectProperty<DesignLayer> currentLayer;

	/**
	 * The selected layer according to the tool guide.
	 */
	private final ObjectProperty<DesignLayer> selectedLayer;

	/**
	 * @deprecated In favor of portalStack
	 */
	@Deprecated
	private final ObjectProperty<DesignView> currentView;

	// Proposed:
	// gridVisible - renderer property
	// viewpoint - renderer property
	// rotate - renderer property
	// zoom - renderer property

	// TOOL PROPERTIES

	private BooleanProperty gridSnapEnabled;

	private BooleanProperty showHotspotEnabled;

	/**
	 * The reticule is the more specialized equivalent of the crosshair cursor.
	 * Whenever the program uses the crosshair cursor, it should use the reticule
	 * cursor.
	 */
	private final ObjectProperty<Reticule> reticule;

	// The renderer might also have some properties that should be exposed

	private final ObjectProperty<DesignValue> selectTolerance;

	// portal (viewport)

	// LAYERS
	// Reference points
	// Preview
	// Design layers
	// Grid

	private final Workplane workplane;

	private final Map<String, ProgramAction> commandActions;

	protected final DesignPropertiesMap designPropertiesMap;

	@Getter
	private final PrintAction printAction;

	@Getter
	private final PropertiesAction propertiesAction;

	@Getter
	private final DeleteAction deleteAction;

	@Getter
	private final UndoAction undoAction;

	@Getter
	private final RedoAction redoAction;

	@Getter
	private final DelayedAction storePreviousViewAction;

	private com.acromere.event.EventHandler<ResourceSwitchedEvent> assetSwitchListener;

	protected BaseDesignTool( XenonProgramProduct product, Resource resource, BaseDesignRenderer renderer ) {
		super( product, resource );
		setUid( IdGenerator.getId() );
		addStylesheet( CartesiaMod.STYLESHEET );
		getStyleClass().add( "design-tool" );

		// Fields
		portalStack = new Stack<>();
		commandActions = new ConcurrentHashMap<>();
		designPropertiesMap = new DesignPropertiesMap( product );

		// Actions
		printAction = new PrintAction( product.getProgram() );
		propertiesAction = new PropertiesAction( product.getProgram() );
		deleteAction = new DeleteAction( product.getProgram() );
		undoAction = new UndoAction( product.getProgram() );
		redoAction = new RedoAction( product.getProgram() );

		storePreviousViewAction = new DelayedAction( getProgram().getTaskManager().getExecutor(), this::capturePreviousPortal );
		storePreviousViewAction.setMinTriggerLimit( 1000 );
		storePreviousViewAction.setMaxTriggerLimit( 5000 );

		// Create the tool toast
		this.toast = new Label( Rb.text( RbKey.LABEL, "loading-asset", resource.getName() ) + " ..." );
		this.toast.getStyleClass().add( "tool-toast" );
		StackPane.setAlignment( this.toast, Pos.CENTER );

		layerGuide = new LayerGuide( product, this );
		//		viewsGuide = new ViewsGuide( product, this );
		//		printsGuide = new PrintsGuide( product, this );

		// Configure the tool renderer
		// The renderer is configured to render to the primary screen by default,
		// but it can be configured to render to different media just as easily.

		// Example DPI values:
		// Sapphire: 162 @ 1x
		// Graphene: 153 @ 1x
		// Headless: 100 @ 1x
		renderer.setDpiX( Screen.getPrimary().getDpi() );
		renderer.setDpiY( Screen.getPrimary().getDpi() );

		// To change this on Gnome-based systems, use the following command:
		// `gsettings set org.gnome.desktop.interface scaling-factor 2`
		renderer.setOutputScaleX( Screen.getPrimary().getOutputScaleX() );
		renderer.setOutputScaleY( Screen.getPrimary().getOutputScaleY() );

		this.renderer = renderer;

		// Initially the toast is shown and the renderer is hidden
		this.toast.setVisible( true );
		this.renderer.setVisible( false );

		// Initialize the reticule
		reticule = new SimpleObjectProperty<>( DEFAULT_RETICULE );
		selectTolerance = new SimpleObjectProperty<>( DEFAULT_SELECT_TOLERANCE );

		// Initialize the cursor to the default cursor
		// There is debate whether this should be the reticule
		setCursor( Cursor.DEFAULT );

		selectedLayer = new SimpleObjectProperty<>();
		currentLayer = new SimpleObjectProperty<>();
		currentView = new SimpleObjectProperty<>();

		this.workplane = new Workplane();
		renderer.setWorkplane( this.workplane );

		// Keep the workplane up to date when the renderer changes
		renderer.widthProperty().addListener( ( _, _, _ ) -> updateWorkplaneBoundaries() );
		renderer.heightProperty().addListener( ( _, _, _ ) -> updateWorkplaneBoundaries() );
		renderer.viewCenterXProperty().addListener( ( _, _, _ ) -> updateWorkplaneBoundaries() );
		renderer.viewCenterYProperty().addListener( ( _, _, _ ) -> updateWorkplaneBoundaries() );
		renderer.viewCenterZProperty().addListener( ( _, _, _ ) -> updateWorkplaneBoundaries() );
		renderer.viewRotateProperty().addListener( ( _, _, _ ) -> updateWorkplaneBoundaries() );
		renderer.viewZoomXProperty().addListener( ( _, _, _ ) -> updateWorkplaneBoundaries() );
		renderer.viewZoomYProperty().addListener( ( _, _, _ ) -> updateWorkplaneBoundaries() );

		// Update the zoom in the coordinate status when the zoom property changes
		renderer.viewZoomXProperty().addListener( ( _, _, n ) -> getCommandContext().setZoom( n.doubleValue() ) );

		// Register the listener to update the cursor when the reticule changes, and the cursor is also a reticule cursor
		reticule.addListener( ( _, _, n ) -> {
			if( getCursor() instanceof ReticuleCursor ) setCursor( n.getCursor( getProgram() ) );
		} );

		// Add the components to the parent
		getChildren().addAll( this.renderer, this.toast );
	}

	protected void ready( OpenAssetRequest request ) throws ToolException {
		super.ready( request );

		setTitle( getResource().getName() );
		setGraphic( getProgram().getIconLibrary().getIcon( getProduct().getCard().getArtifact() ) );

		getResource().register( Resource.NAME, e -> setTitle( e.getNewValue() ) );
		getResource().register( Resource.ICON, e -> setIcon( e.getNewValue() ) );

		getResource().getUndoManager().undoAvailableProperty().addListener( ( p, o, n ) -> undoAction.updateEnabled() );
		getResource().getUndoManager().redoAvailableProperty().addListener( ( p, o, n ) -> redoAction.updateEnabled() );

		// Set the design model
		Design<? extends DesignModel> design = request.getResource().getModel();
		getRenderer().setDesign( design );
		DesignModel model = design.getDataModel();

		// TEMPORARY - Create layer
		String constructionLayerName = Rb.textOr( com.acromere.xenon.RbKey.LABEL, "layer-construction", "construction" ).toLowerCase();
		DesignLayer layer = new DesignLayer().setName( constructionLayerName );
		design.getDataModel().getLayers().addLayer( layer );

		// Set the current layer
		setCurrentLayer( getDesign().getDataModel().getAllLayers().getFirst() );

		// Fire the design-ready event (should be done after renderer.setDesign)
		fireEvent( new DesignToolEvent( this, DesignToolEvent.DESIGN_READY ) );

		// Configure the undo manager
		getResource().getUndoManager().undoAvailableProperty().addListener( ( _, _, _ ) -> getUndoAction().updateEnabled() );
		getResource().getUndoManager().redoAvailableProperty().addListener( ( _, _, _ ) -> getRedoAction().updateEnabled() );

		layerGuide.ready( request );
		//		viewsGuide.ready( request );
		//		printsGuide.ready( request );
		//getGuideContext().getGuides().addAll( layersGuide, viewsGuide, printsGuide );
		getGuideContext().getGuides().addAll( layerGuide );
		getGuideContext().setCurrentGuide( layerGuide );

		// Default values
		String defaultSelectSize = String.valueOf( DEFAULT_SELECT_TOLERANCE.value() );
		String defaultSelectUnit = DEFAULT_SELECT_TOLERANCE.unit().toString().toLowerCase();
		String defaultReferencePointType = DesignMarker.Type.CIRCLE.name().toLowerCase();
		String defaultReferencePointSize = "10";
		String defaultReferencePointPaint = "#808080";
		String defaultReticule = DEFAULT_RETICULE.name().toLowerCase();

		setSelectTolerance( DEFAULT_SELECT_TOLERANCE );

		// Get the settings collections
		Settings productSettings = getProduct().getSettings();
		Settings settings = getSettings();
		Settings assetSettings = getAssetSettings();

		// Workplane settings and listeners
		configureWorkplane( getWorkplane(), assetSettings );

		// Tool settings
		double selectApertureSize = Double.parseDouble( productSettings.get( SELECT_APERTURE_SIZE, defaultSelectSize ) );
		DesignUnit selectApertureUnit = DesignUnit.valueOf( DesignUnitMapper.mapNameToAbbreviation( productSettings.get( SELECT_APERTURE_UNIT, defaultSelectUnit ) ).toUpperCase() );
		DesignMarker.Type referencePointType = DesignMarker.Type.valueOf( productSettings.get( REFERENCE_POINT_TYPE, defaultReferencePointType ).toUpperCase() );
		double referencePointSize = Double.parseDouble( productSettings.get( REFERENCE_POINT_SIZE, defaultReferencePointSize ) );
		Paint referencePointPaint = Paints.parse( productSettings.get( REFERENCE_POINT_PAINT, defaultReferencePointPaint ) );

		Point3D viewPoint = ParseUtil.parsePoint3D( settings.get( SETTINGS_VIEW_POINT, "0,0,0" ) );
		double viewZoom = Double.parseDouble( settings.get( SETTINGS_VIEW_ZOOM, "1.0" ) );
		double viewRotate = Double.parseDouble( settings.get( SETTINGS_VIEW_ROTATE, "0.0" ) );
		setView( viewPoint, viewZoom, viewRotate );
		//		designPane.setReferencePointType( referencePointType );
		//		designPane.setReferencePointSize( referencePointSize );
		//		designPane.setReferencePointPaint( referencePointPaint );

		getDesignModel().findLayers( DesignLayer.ID, settings.get( CURRENT_LAYER, "" ) ).stream().findFirst().ifPresent( this::setCurrentLayer );
		getDesignModel().findViews( DesignView.ID, settings.get( CURRENT_VIEW, "" ) ).stream().findFirst().ifPresent( this::setCurrentView );

		// Restore the list of enabled layers
		Set<String> enabledLayerIds = settings.get( ENABLED_LAYERS, new TypeReference<>() {}, Set.of() );
		getDesignModel().getAllLayers().forEach( l -> setLayerEnabled( l, enabledLayerIds.contains( l.getId() ) ) );

		// Restore the list of visible layers
		Set<String> visibleLayerIds = settings.get( VISIBLE_LAYERS, new TypeReference<>() {}, Set.of() );
		getDesignModel().getAllLayers().forEach( l -> setLayerVisible( l, visibleLayerIds.contains( l.getId() ) ) );
		// TEMPORARY Show the first layer
		if( !model.getLayers().getLayers().isEmpty() ) {
			getRenderer().setLayerVisible( model.getLayers().getLayers().getFirst(), true );
		}

		// Restore the grid-visible flag
		setGridVisible( Boolean.parseBoolean( settings.get( GRID_VISIBLE, DEFAULT_GRID_VISIBLE ) ) );

		// Restore the grid-snap enabled flag
		setGridSnapEnabled( Boolean.parseBoolean( settings.get( GRID_SNAP_ENABLED, DEFAULT_GRID_SNAP_ENABLED ) ) );

		//		// Restore the reference view visibility
		//		setReferenceLayerVisible( Boolean.parseBoolean( settings.get( REFERENCE_LAYER_VISIBLE, Boolean.TRUE.toString() ) ) );

		// Settings listeners
		productSettings.bind( RETICULE, DEFAULT_RETICULE, e -> this.setReticule( Reticule.valueOf( String.valueOf( e.getNewValue() ).toUpperCase() ) ) );
		productSettings.bind( SELECT_APERTURE_SIZE, DEFAULT_SELECT_TOLERANCE, e -> setSelectTolerance( new DesignValue( Double.parseDouble( (String)e.getNewValue() ), getSelectTolerance().unit() ) ) );
		productSettings.bind(
			SELECT_APERTURE_UNIT,
			DEFAULT_SELECT_TOLERANCE,
			e -> setSelectTolerance( new DesignValue( getSelectTolerance().value(), DesignUnitMapper.map( ((String)e.getNewValue()) ) ) )
		);

		// TODO Set the reference point settings on the renderer
		//productSettings.register( REFERENCE_POINT_TYPE, e -> renderer.setReferencePointType( DesignMarker.Type.valueOf( String.valueOf( e.getNewValue() ).toUpperCase() ) ) );
		//productSettings.register( REFERENCE_POINT_SIZE, e -> renderer.setReferencePointSize( Double.parseDouble( (String)e.getNewValue() ) ) );
		//productSettings.register( REFERENCE_POINT_PAINT, e -> renderer.setReferencePointPaint( Paints.parse( String.valueOf( e.getNewValue() ).toUpperCase() ) ) );

		// Renderer property listeners ---------------------------------------------

		// Add view point property listener
		renderer.viewCenterXProperty().addListener( ( p, o, n ) -> {
			getStorePreviousViewAction().request();
			Point3D vp = renderer.getViewCenter();
			settings.set( SETTINGS_VIEW_POINT, vp.getX() + "," + vp.getY() + ",0" );
		} );
		renderer.viewCenterYProperty().addListener( ( p, o, n ) -> {
			getStorePreviousViewAction().request();
			Point3D vp = renderer.getViewCenter();
			settings.set( SETTINGS_VIEW_POINT, vp.getX() + "," + vp.getY() + ",0" );
		} );

		// Add view rotate property listener
		renderer.viewRotateProperty().addListener( ( p, o, n ) -> {
			getStorePreviousViewAction().request();
			settings.set( SETTINGS_VIEW_ROTATE, n.doubleValue() );
		} );

		// Add view zoom property listener
		renderer.viewZoomXProperty().addListener( ( p, o, n ) -> {
			getStorePreviousViewAction().request();
			getCoordinateStatus().updateZoom( n.doubleValue() );
			settings.set( SETTINGS_VIEW_ZOOM, n.doubleValue() );
		} );
		renderer.viewZoomYProperty().addListener( ( p, o, n ) -> {
			getStorePreviousViewAction().request();
			getCoordinateStatus().updateZoom( n.doubleValue() );
			settings.set( SETTINGS_VIEW_ZOOM, n.doubleValue() );
		} );

		// Add enabled layers listener
		enabledLayers().addListener( this::doStoreEnabledLayers );

		// Add visible layers listener
		visibleLayers().addListener( this::doStoreVisibleLayers );

		// Add current layer property listener
		currentLayerProperty().addListener( ( p, o, n ) -> settings.set( CURRENT_LAYER, n.getId() ) );

		// Add the selected layer property listener to show its properties page
		selectedLayerProperty().addListener( ( p, o, n ) -> showPropertiesPage( n ) );

		// Add the selected layer property listener to store the selected layer in the settings
		selectedLayerProperty().addListener( ( p, o, n ) -> settings.set( SELECTED_LAYER, n.getId() ) );

		// Add current view property listener
		currentViewProperty().addListener( ( p, o, n ) -> settings.set( CURRENT_VIEW, n.getId() ) );

		// Add grid visible property listener
		gridVisible().addListener( ( p, o, n ) -> settings.set( GRID_VISIBLE, String.valueOf( n ) ) );

		// Add grid visible property listener
		gridSnapEnabled().addListener( ( p, o, n ) -> settings.set( GRID_SNAP_ENABLED, String.valueOf( n ) ) );

		//		// Add reference points visible property listener
		//		designPane.referenceLayerVisible().addListener( ( p, o, n ) -> settings.set( REFERENCE_LAYER_VISIBLE, String.valueOf( n ) ) );

		getDesignContext().selectedShapes().addListener( this::onSelectedShapesChanged );

		// Update the select aperture when the mouse moves
		addEventFilter(
			MouseEvent.MOUSE_MOVED, e -> {
				if( getCommandContext().isEmptyMode() ) {
					setSelectAperture( new Point3D( e.getX(), e.getY(), e.getZ() ), new Point3D( e.getX(), e.getY(), e.getZ() ) );
				}
			}
		);

		// Link the command context to the user interfaces
		//addEventFilter( KeyEvent.ANY, e -> getCommandContext().handle( e ) );
		addEventFilter( MouseEvent.ANY, e -> getCommandContext().handle( e ) );
		addEventFilter( GestureEvent.ANY, e -> getCommandContext().handle( e ) );
		addEventFilter( TouchEvent.ANY, e -> getCommandContext().handle( e ) );

		// Update the design context when the mouse moves
		addEventFilter( MouseEvent.MOUSE_MOVED, e -> getCommandContext().setMouse( e ) );

		// TODO Should selected layer be stored in the tool settings or the asset settings?
		if( getSelectedLayer() == null ) setSelectedLayer( getCurrentLayer() );

		// Swap the toast for the renderer
		getToast().setVisible( false );
		getRenderer().setVisible( true );
		getCoordinateStatus().updateZoom( getViewZoom() );
	}

	@Override
	protected void allocate() throws ToolException {
		super.allocate();

		getCommandContext().setLastUserTool( this );

		// Add asset switch listener to remove command prompt
		getProgram().register(
			ResourceSwitchedEvent.SWITCHED, assetSwitchListener = e -> {
				if( isDisplayed() && e.getOldAsset() == getResource() && e.getNewAsset() != getResource() ) {
					unregisterStatusBarItems();
				}
			}
		);
	}

	@Override
	protected void activate() throws ToolException {
		super.activate();
		if( !getResource().isLoaded() ) return;

		getCommandContext().setLastUserTool( this );

		registerStatusBarItems();
		registerCommandCapture();
		registerActions();

		requestFocus();
	}

	@Override
	protected void conceal() throws ToolException {
		unregisterActions();
		unregisterCommandCapture();

		hidePropertiesPage();

		super.conceal();
	}

	@Override
	protected void deallocate() throws ToolException {
		// Remove asset switch listener to unregister status bar items
		getProgram().unregister( ResourceSwitchedEvent.SWITCHED, assetSwitchListener );

		if( renderer != null ) renderer.setDesign( null );

		super.deallocate();
	}

	@Override
	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
		if( getCurrentGuide() == getLayerGuide() ) {
			newNodes.stream().findFirst().ifPresent( n -> doSetSelectedLayerById( n.getId() ) );
			//		} else if( getCurrentGuide() == viewsGuide ) {
			//			newNodes.stream().findFirst().ifPresent( n -> doSetCurrentViewById( n.getId() ) );
			//		} else if( getCurrentGuide() == printsGuide ) {
			//			newNodes.stream().findFirst().ifPresent( n -> doSetCurrentPrintById( n.getId() ) );
		}
	}

	@Override
	protected void guideFocusChanged( boolean focused, Set<GuideNode> nodes ) {}

	@Override
	public final CartesiaMod getMod() {
		return (CartesiaMod)getProduct();
	}

	@Override
	public final Design<? extends DesignModel> getDesign() {
		return getAssetModel();
	}

	@Override
	public final DesignModel getDesignModel() {
		return getDesign().getDataModel();
	}

	@Override
	@NonNull
	public final DesignContext getDesignContext() {
		Design<? extends DesignModel> design = getAssetModel();
		return design == null ? DesignContext.EMPTY : design.getDesignContext();
	}

	@Override
	@NonNull
	public final CommandContext getCommandContext() {
		Design<? extends DesignModel> design = getDesign();
		if( design == null ) return CommandContext.EMPTY;

		CommandContext context = design.getCommandContext();
		if( context == null ) {
			context = new CommandContext();
			context.setCoordinateStatus( new CoordinateStatus() );
			design.setCommandContext( context );
		}

		context.setTool( this );
		return context;
	}

	@Override
	public final @NonNull Workplane getWorkplane() {
		return workplane;
	}

	@Override
	public final @NonNull Grid getGridSystem() {
		return getWorkplane().getGridSystem();
	}

	@Override
	public final void setGridSystem( Grid system ) {
		getWorkplane().setGridSystem( system );
	}

	@Override
	public Point3D getViewCenter() {
		return getRenderer().getViewCenter();
	}

	@Override
	public void setViewCenter( Point3D viewCenter ) {
		getRenderer().setViewCenter( viewCenter );
	}

	@Override
	public double getViewRotate() {
		return getRenderer().getViewRotate();
	}

	@Override
	public void setViewRotate( double rotate ) {
		getRenderer().setViewRotate( rotate );
	}

	@Override
	public double getViewZoom() {
		return getRenderer().getViewZoomX();
	}

	@Override
	public void setViewZoom( double viewZoom ) {
		getRenderer().setViewZoom( viewZoom, viewZoom );
	}

	@Override
	public void setView( DesignPortal portal ) {
		setView( portal.center(), portal.zoom(), portal.rotate() );
	}

	@Override
	public void setView( Point3D viewpoint, double zoom ) {
		setView( viewpoint, zoom, getViewRotate() );
	}

	@Override
	public void setView( Point3D viewpoint, double zoom, double rotate ) {
		setViewCenter( viewpoint );
		setViewZoom( zoom );
		setViewRotate( rotate );
	}

	@Override
	public void setView( DesignView view ) {
		setViewCenter( view.getOrigin() );
		setViewZoom( view.getZoom() );
		setViewRotate( view.getRotate() );
		setVisibleLayers( view.getLayers() );
	}

	@Override
	public DesignView createView() {
		DesignView view = new DesignView();
		view.setOrigin( getViewCenter() );
		view.setRotate( getViewRotate() );
		view.setZoom( getViewZoom() );
		view.setLayers( new ArrayList<>( getVisibleLayers() ) );
		return view;
	}

	@Override
	public double getDpi() {
		return getRenderer().getDpiX();
	}

	@Override
	public void setDpi( double dpi ) {
		getRenderer().setDpi( dpi, dpi );
	}

	public DoubleProperty viewDpiProperty() {
		return getRenderer().dpiXProperty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DesignView getCurrentView() {
		return currentView.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentView( DesignView view ) {
		currentView.set( Objects.requireNonNull( view ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectProperty<DesignView> currentViewProperty() {
		return currentView;
	}

	@Override
	@Note( Note.FX_THREAD )
	public final ReticuleCursor getReticuleCursor() {
		return this.getReticule().getCursor( getProgram() );
	}

	public Reticule getReticule() {
		return reticule.get();
	}

	public void setReticule( Reticule reticule ) {
		this.reticule.set( reticule );
	}

	public ObjectProperty<Reticule> reticule() {
		return reticule;
	}

	@Override
	public DesignValue getSelectTolerance() {
		return selectTolerance.get();
	}

	@Override
	public void setSelectTolerance( DesignValue aperture ) {
		selectTolerance().set( aperture );
	}

	@Override
	public ObjectProperty<DesignValue> selectTolerance() {
		return selectTolerance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setScreenViewport( Bounds viewport ) {
		setWorldViewport( screenToWorld( viewport ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWorldViewport( Bounds viewport ) {
		Bounds toolBounds = getBoundsInLocal();
		if( toolBounds.getWidth() == 0 || toolBounds.getHeight() == 0 ) return;

		Bounds worldBounds = screenToWorld( toolBounds );
		double xZoom = Math.abs( worldBounds.getWidth() / viewport.getWidth() );
		double yZoom = Math.abs( worldBounds.getHeight() / viewport.getHeight() );
		double zoom = Math.min( xZoom, yZoom ) * getViewZoom();

		Point3D worldCenter = new Point3D( viewport.getCenterX(), viewport.getCenterY(), viewport.getCenterZ() );
		setView( worldCenter, zoom );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Transform getWorldToScreenTransform() {
		return getRenderer().getWorldToScreenTransform();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D worldToScreen( double x, double y ) {
		return getRenderer().worldToScreen( x, y );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D worldToScreen( Point2D point ) {
		return getRenderer().worldToScreen( point );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D worldToScreen( double x, double y, double z ) {
		return getRenderer().worldToScreen( x, y, z );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D worldToScreen( Point3D point ) {
		return getRenderer().worldToScreen( point );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bounds worldToScreen( Bounds bounds ) {
		return getRenderer().worldToScreen( bounds );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Transform getScreenToWorldTransform() {
		return getRenderer().getScreenToWorldTransform();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D screenToWorld( double x, double y ) {
		return getRenderer().screenToWorld( x, y );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D screenToWorld( Point2D point ) {
		return getRenderer().screenToWorld( point );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D screenToWorld( double x, double y, double z ) {
		return getRenderer().screenToWorld( x, y, z );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D screenToWorld( Point3D point ) {
		return getRenderer().screenToWorld( point );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bounds screenToWorld( Bounds bounds ) {
		return getRenderer().screenToWorld( bounds );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D screenToWorkplane( Point3D point ) {
		return screenToWorkplane( point.getX(), point.getY(), point.getZ() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D screenToWorkplane( double x, double y, double z ) {
		Point3D worldPoint = screenToWorld( x, y, z );
		return isGridSnapEnabled() ? gridSnap.snap( this, worldPoint ) : worldPoint;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D snapToGrid( Point3D point ) {
		return isGridSnapEnabled() ? gridSnap.snap( this, point ) : point;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point3D snapToGrid( double x, double y, double z ) {
		return snapToGrid( new Point3D( x, y, z ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGridVisible() {
		return renderer.isGridVisible();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGridVisible( boolean visible ) {
		renderer.setGridVisible( visible );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BooleanProperty gridVisible() {
		return renderer.gridVisible();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isGridSnapEnabled() {
		return gridSnapEnabled == null ? DEFAULT_GRID_SNAP_ENABLED : gridSnapEnabled().get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGridSnapEnabled( boolean enabled ) {
		gridSnapEnabled().set( enabled );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BooleanProperty gridSnapEnabled() {
		if( gridSnapEnabled == null ) gridSnapEnabled = new SimpleBooleanProperty( DEFAULT_GRID_SNAP_ENABLED );
		return gridSnapEnabled;
	}

	public boolean isShowHotspotEnabled() {
		return showHotspotEnabled == null ? DEFAULT_SHOW_HOTSPOT_ENABLED : showHotspotEnabled().get();
	}

	public void setShowHotspotEnabled( boolean enabled ) {
		showHotspotEnabled.set( enabled );
	}

	public BooleanProperty showHotspotEnabled() {
		if( showHotspotEnabled == null ) showHotspotEnabled = new SimpleBooleanProperty( DEFAULT_SHOW_HOTSPOT_ENABLED );
		return showHotspotEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLayerEnabled( DesignLayer layer ) {
		return getRenderer().isLayerEnabled( layer );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLayerEnabled( DesignLayer layer, boolean visible ) {
		getRenderer().setLayerEnabled( layer, visible );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DesignLayer> getEnabledLayers() {
		return getRenderer().getEnabledLayers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabledLayers( Collection<DesignLayer> layers ) {
		getRenderer().setEnabledLayers( layers );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservableList<DesignLayer> enabledLayers() {
		return getRenderer().enabledLayers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLayerVisible( DesignLayer layer ) {
		return getRenderer().isLayerVisible( layer );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLayerVisible( DesignLayer layer, boolean visible ) {
		getRenderer().setLayerVisible( layer, visible );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DesignLayer> getVisibleLayers() {
		return getRenderer().getVisibleLayers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVisibleLayers( Collection<DesignLayer> layers ) {
		getRenderer().setVisibleLayers( layers );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservableList<DesignLayer> visibleLayers() {
		return renderer.visibleLayers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCurrentLayer( DesignLayer layer ) {
		return getCurrentLayer() == layer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DesignLayer getCurrentLayer() {
		return currentLayer.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentLayer( DesignLayer layer ) {
		currentLayer.set( Objects.requireNonNull( layer ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectProperty<DesignLayer> currentLayerProperty() {
		return currentLayer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DesignLayer getSelectedLayer() {
		return selectedLayer.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSelectedLayer( DesignLayer layer ) {
		selectedLayer.set( Objects.requireNonNull( layer ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectProperty<DesignLayer> selectedLayerProperty() {
		return selectedLayer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void zoom( Point3D anchor, double factor ) {
		Fx.onFxOrCurrent( () -> getRenderer().zoom( anchor, factor ) );
	}

	// TODO Insert common design tool implementations here
	// Many implementations found in DesignToolV2 can be moved here

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void showCommandPrompt() {
		Fx.run( this::registerStatusBarItems );
		Fx.run( this::requestFocus );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseDesignRenderer getScreenDesignRenderer() {
		return renderer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DesignPortal getPriorPortal() {
		// Remove the current portal
		if( !portalStack.isEmpty() ) portalStack.pop();

		// Return the prior portal
		return portalStack.isEmpty() ? DesignPortal.DEFAULT : portalStack.pop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DesignShape> getSelectedShapes() {
		return List.copyOf( getDesignContext().getSelectedShapes() );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSelectedShapes( List<DesignShape> shapes, boolean selected ) {
		shapes.forEach( shape -> shape.setSelected( selected ) );
		getDesignContext().setSelectedShapes( shapes, selected );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObservableList<DesignShape> selectedShapes() {
		return getDesignContext().selectedShapes();
	}

	// NEXT Work on selecting shapes
	protected void selectShapes( List<DesignShape> shapes, boolean toggle ) {
		final ObservableList<DesignShape> selectedShapes = selectedShapes();
		if( toggle ) {
			shapes.forEach( shape -> {
				if( shape.isSelected() ) {
					selectedShapes.remove( shape );
				} else {
					selectedShapes.add( shape );
				}
			} );
		} else {
			selectedShapes.setAll( shapes );
		}
	}

	private CommandPrompt getCommandPrompt() {
		return getCommandContext().getCommandPrompt();
	}

	protected CoordinateStatus getCoordinateStatus() {
		return getCommandContext().getCoordinateStatus();
	}

	private void registerStatusBarItems() {
		Fx.run( () -> {
			Workspace workspace = getWorkspace();
			if( workspace == null ) return;

			StatusBar bar = workspace.getStatusBar();
			bar.setLeftToolItems( getCommandPrompt() );
			bar.setRightToolItems( getCoordinateStatus() );
		} );
	}

	private void unregisterStatusBarItems() {
		Fx.run( () -> {
			Workspace workspace = getWorkspace();
			if( workspace == null ) return;

			StatusBar bar = workspace.getStatusBar();
			bar.removeLeftToolItems( getCommandPrompt() );
			bar.removeRightToolItems( getCoordinateStatus() );
		} );
	}

	private void registerCommandCapture() {
		// If there is already a command capture handler, then remove it
		// (because it may belong to a different design)
		unregisterCommandCapture();

		// Add the design command capture handler. This captures all key events that
		// make it to the tool and forwards them to the command context, which
		// will help determine what to do.
		addEventHandler( KeyEvent.ANY, getCommandContext() );
	}

	@SuppressWarnings( "unchecked" )
	private void unregisterCommandCapture() {
		Workpane workpane = getWorkpane();
		EventHandler<KeyEvent> handler = (EventHandler<KeyEvent>)workpane.getProperties().get( "design-tool-command-capture" );
		if( handler != null ) workpane.removeEventHandler( KeyEvent.ANY, handler );
	}

	private void registerActions() {
		pushAction( "print", printAction );
		pushAction( "properties", propertiesAction );
		pushAction( "delete", deleteAction );
		pushAction( "undo", undoAction );
		pushAction( "redo", redoAction );

		pushCommandAction( "draw-arc-2" );
		pushCommandAction( "draw-arc-3" );
		pushCommandAction( "draw-circle-2" );
		pushCommandAction( "draw-circle-3" );
		pushCommandAction( "draw-circle-diameter-2" );
		//pushCommandAction( "draw-curve-3" );
		pushCommandAction( "draw-curve-4" );
		pushCommandAction( "draw-ellipse-3" );
		pushCommandAction( "draw-ellipse-arc-5" );
		pushCommandAction( "draw-line-2" );
		pushCommandAction( "draw-line-perpendicular" );
		pushCommandAction( "draw-marker" );
		pushCommandAction( "draw-path" );

		pushCommandAction( "measure-angle" );
		pushCommandAction( "measure-distance" );
		pushCommandAction( "measure-length" );
		pushCommandAction( "measure-point" );
		pushCommandAction( "shape-information" );

		//ProgramAction gridVisibleToggleAction = pushCommandAction( "grid-toggle", isGridVisible() ? "enabled" : "disabled" );
		//gridVisible().addListener( gridVisibleToggleHandler = ( p, o, n ) -> gridVisibleToggleAction.setState( n ? "enabled" : "disabled" ) );
		//ProgramAction snapGridToggleAction = pushCommandAction( "snap-grid-toggle", isGridSnapEnabled() ? "enabled" : "disabled" );
		//gridSnapEnabled().addListener( snapGridToggleHandler = ( p, o, n ) -> snapGridToggleAction.setState( n ? "enabled" : "disabled" ) );

		String viewActions = "grid-toggle snap-grid-toggle";
		String layerActions = "layer[layer-create layer-sublayer | layer-delete]";
		String drawMarkerActions = "marker[draw-marker]";
		String drawLineActions = "line[draw-line-2 draw-line-perpendicular]";
		String drawCircleActions = "circle[draw-circle-2 draw-circle-diameter-2 draw-circle-3 | draw-arc-2 draw-arc-3]";
		String drawEllipseActions = "ellipse[draw-ellipse-3 draw-ellipse-arc-5]";
		String drawCurveActions = "curve[draw-curve-4 draw-path]";

		String measurementActions = "measure[shape-information measure-angle measure-distance measure-point measure-length]";

		@SuppressWarnings( "StringBufferReplaceableByString" ) StringBuilder menus = new StringBuilder( viewActions );
		menus.append( " " ).append( layerActions );
		menus.append( "|" ).append( drawMarkerActions );
		menus.append( " " ).append( drawLineActions );
		menus.append( " " ).append( drawCircleActions );
		menus.append( " " ).append( drawEllipseActions );
		menus.append( " " ).append( drawCurveActions );
		menus.append( "|" ).append( measurementActions );

		@SuppressWarnings( "StringBufferReplaceableByString" ) StringBuilder tools = new StringBuilder( viewActions );
		tools.append( " " ).append( drawMarkerActions );
		tools.append( " " ).append( drawLineActions );
		tools.append( " " ).append( drawCircleActions );
		tools.append( " " ).append( drawEllipseActions );
		tools.append( " " ).append( drawCurveActions );

		pushMenus( menus.toString() );
		pushTools( tools.toString() );
	}

	private void unregisterActions() {
		pullMenus();
		pullTools();

		//if( gridVisibleToggleHandler != null ) gridVisible().removeListener( gridVisibleToggleHandler );
		//pullCommandAction( "grid-toggle" );
		//if( snapGridToggleHandler != null ) gridSnapEnabled().removeListener( snapGridToggleHandler );
		//pullCommandAction( "snap-grid-toggle" );

		pullCommandAction( "draw-path" );
		pullCommandAction( "draw-marker" );
		pullCommandAction( "draw-line-perpendicular" );
		pullCommandAction( "draw-line-2" );
		pullCommandAction( "draw-ellipse-arc-5" );
		pullCommandAction( "draw-ellipse-3" );
		pullCommandAction( "draw-curve-4" );
		//pullCommandAction( "draw-curve-3" );
		pullCommandAction( "draw-circle-diameter-2" );
		pullCommandAction( "draw-circle-3" );
		pullCommandAction( "draw-circle-2" );
		pullCommandAction( "draw-arc-3" );
		pullCommandAction( "draw-arc-2" );

		pullCommandAction( "shape-information" );
		pullCommandAction( "measure-point" );
		pullCommandAction( "measure-length" );
		pullCommandAction( "measure-distance" );
		pullCommandAction( "measure-angle" );

		pullAction( "print", printAction );
		pullAction( "properties", propertiesAction );
		pullAction( "delete", deleteAction );
		pullAction( "undo", undoAction );
		pullAction( "redo", redoAction );
	}

	protected void configureWorkplane( Workplane workplane, Settings settings ) {
		workplane.setGridSystem( Grid.valueOf( settings.get( Workplane.GRID_SYSTEM, Workplane.DEFAULT_GRID_SYSTEM.name() ).toUpperCase() ) );
		workplane.setOrigin( settings.get( Workplane.WORKPANE_ORIGIN, Workplane.DEFAULT_ORIGIN ) );

		workplane.setGridAxisVisible( settings.get( Workplane.GRID_AXIS_VISIBLE, Boolean.class, Workplane.DEFAULT_GRID_AXIS_VISIBLE ) );
		workplane.setGridAxisPaint( settings.get( Workplane.GRID_AXIS_PAINT, Workplane.DEFAULT_GRID_AXIS_PAINT ) );
		workplane.setGridAxisWidth( settings.get( Workplane.GRID_AXIS_WIDTH, Workplane.DEFAULT_GRID_AXIS_WIDTH ) );

		workplane.setMajorGridVisible( settings.get( Workplane.GRID_MAJOR_VISIBLE, Boolean.class, Workplane.DEFAULT_GRID_MAJOR_VISIBLE ) );
		workplane.setMajorGridX( settings.get( Workplane.GRID_MAJOR_X, Workplane.DEFAULT_GRID_MAJOR_SIZE ) );
		workplane.setMajorGridY( settings.get( Workplane.GRID_MAJOR_Y, Workplane.DEFAULT_GRID_MAJOR_SIZE ) );
		workplane.setMajorGridPaint( settings.get( Workplane.GRID_MAJOR_PAINT, Workplane.DEFAULT_GRID_MAJOR_PAINT ) );
		workplane.setMajorGridWidth( settings.get( Workplane.GRID_MAJOR_WIDTH, Workplane.DEFAULT_GRID_MAJOR_WIDTH ) );

		workplane.setMinorGridVisible( settings.get( Workplane.GRID_MINOR_VISIBLE, Boolean.class, Workplane.DEFAULT_GRID_MINOR_VISIBLE ) );
		workplane.setMinorGridX( settings.get( Workplane.GRID_MINOR_X, Workplane.DEFAULT_GRID_MINOR_SIZE ) );
		workplane.setMinorGridY( settings.get( Workplane.GRID_MINOR_Y, Workplane.DEFAULT_GRID_MINOR_SIZE ) );
		workplane.setMinorGridPaint( settings.get( Workplane.GRID_MINOR_PAINT, Workplane.DEFAULT_GRID_MINOR_PAINT ) );
		workplane.setMinorGridWidth( settings.get( Workplane.GRID_MINOR_WIDTH, Workplane.DEFAULT_GRID_MINOR_WIDTH ) );

		workplane.setSnapGridX( settings.get( Workplane.GRID_SNAP_X, Workplane.DEFAULT_GRID_SNAP_SIZE ) );
		workplane.setSnapGridY( settings.get( Workplane.GRID_SNAP_Y, Workplane.DEFAULT_GRID_SNAP_SIZE ) );

		settings.register( Workplane.GRID_SYSTEM, e -> workplane.setGridSystem( Grid.valueOf( String.valueOf( e.getNewValue() ).toUpperCase() ) ) );
		settings.register( Workplane.GRID_ORIGIN, e -> workplane.setOrigin( String.valueOf( e.getNewValue() ) ) );

		settings.register( Workplane.GRID_AXIS_VISIBLE, e -> workplane.setGridAxisVisible( Boolean.parseBoolean( String.valueOf( e.getNewValue() ) ) ) );
		settings.register( Workplane.GRID_AXIS_PAINT, e -> workplane.setGridAxisPaint( String.valueOf( e.getNewValue() ) ) );
		settings.register( Workplane.GRID_AXIS_WIDTH, e -> workplane.setGridAxisWidth( String.valueOf( e.getNewValue() ) ) );

		settings.register( Workplane.GRID_MAJOR_VISIBLE, e -> workplane.setMajorGridVisible( Boolean.parseBoolean( String.valueOf( e.getNewValue() ) ) ) );
		settings.register( Workplane.GRID_MAJOR_X, e -> workplane.setMajorGridX( String.valueOf( e.getNewValue() ) ) );
		settings.register( Workplane.GRID_MAJOR_Y, e -> workplane.setMajorGridY( String.valueOf( e.getNewValue() ) ) );
		//settings.register( DesignWorkplane.GRID_MAJOR_Z, e -> workplane.setMajorGridZ( String.valueOf( e.getNewValue() ) ) );
		settings.register( Workplane.GRID_MAJOR_PAINT, e -> workplane.setMajorGridPaint( String.valueOf( e.getNewValue() ) ) );
		settings.register( Workplane.GRID_MAJOR_WIDTH, e -> workplane.setMajorGridWidth( String.valueOf( e.getNewValue() ) ) );

		settings.register( Workplane.GRID_MINOR_VISIBLE, e -> workplane.setMinorGridVisible( Boolean.parseBoolean( String.valueOf( e.getNewValue() ) ) ) );
		settings.register( Workplane.GRID_MINOR_X, e -> workplane.setMinorGridX( String.valueOf( e.getNewValue() ) ) );
		settings.register( Workplane.GRID_MINOR_Y, e -> workplane.setMinorGridY( String.valueOf( e.getNewValue() ) ) );
		//settings.register( DesignWorkplane.GRID_MINOR_Z, e -> workplane.setMinorGridZ( String.valueOf( e.getNewValue() ) ) );
		settings.register( Workplane.GRID_MINOR_PAINT, e -> workplane.setMinorGridPaint( String.valueOf( e.getNewValue() ) ) );
		settings.register( Workplane.GRID_MINOR_WIDTH, e -> workplane.setMinorGridWidth( String.valueOf( e.getNewValue() ) ) );

		settings.register( Workplane.GRID_SNAP_X, e -> workplane.setSnapGridX( String.valueOf( e.getNewValue() ) ) );
		settings.register( Workplane.GRID_SNAP_Y, e -> workplane.setSnapGridY( String.valueOf( e.getNewValue() ) ) );
		//settings.register( DesignWorkplane.GRID_SNAP_Z, e -> workplane.setSnapGridZ( String.valueOf( e.getNewValue() ) ) );

		// Rebuild the grid if any workplane values change
		//workplane.register( NodeEvent.VALUE_CHANGED, e -> rebuildGridAction.request() );
	}

	private void capturePreviousPortal() {
		portalStack.push( new DesignPortal( getViewCenter(), getViewZoom(), getViewRotate() ) );
	}

	private ProgramAction pushCommandAction( String key ) {
		return pushCommandAction( key, null );
	}

	private ProgramAction pushCommandAction( String key, String initialActionState ) {
		ActionProxy proxy = getProgram().getActionLibrary().getAction( key );
		ProgramAction action = commandActions.computeIfAbsent( key, k -> new CommandAction( this, getProgram(), proxy.getCommand() ) );
		if( initialActionState != null ) action.setState( initialActionState );
		pushAction( key, action );
		return action;
	}

	private void pullCommandAction( String key ) {
		pullAction( key, commandActions.get( key ) );
	}

	private void updateWorkplaneBoundaries() {
		// Determine the viewport boundary from the renderer layout bounds
		Bounds viewport = getRenderer().getLayoutBounds();

		// The workplane bounds can be determined from the viewport
		workplane.setBounds( getRenderer().screenToWorld( viewport ) );
	}

	private List<DesignLayer> getFilteredLayers( Predicate<? super DesignLayer> filter ) {
		return getDesignModel().getAllLayers().stream().filter( filter ).collect( Collectors.toList() );
	}

	private void doSetSelectedLayerById( String id ) {
		getDesignModel().findLayerById( id ).ifPresent( this::setSelectedLayer );
		log.atConfig().log( "Selected layer: %s", id );
	}

	private void doSetCurrentLayerById( String id ) {
		getDesignModel().findLayerById( id ).ifPresent( y -> {
			currentLayerProperty().set( y );
			showPropertiesPage( y );
		} );
	}

	private void doSetCurrentViewById( String id ) {
		getDesignModel().findViewById( id ).ifPresent( v -> {
			currentViewProperty().set( v );
			//renderer.setView( v.getLayers(), v.getOrigin(), v.getZoom(), v.getRotate() );
			//showPropertiesPage( v );
		} );
	}

	private void doSetCurrentPrintById( String id ) {
		// TODO Implement DesignTool.doSetCurrentPrintById()
	}

	protected void showPropertiesPage( DesignDrawable drawable ) {
		if( drawable != null ) {
			// Wrap the drawable in a data node settings object
			NodeSettings wrapper = new NodeSettings( drawable );

			// Show the properties page for the drawable
			showPropertiesPage( wrapper, drawable.getClass() );
		}
	}

	private void showPropertiesPage( Settings settings, Class<? extends DesignDrawable> type ) {
		SettingsPage page = designPropertiesMap.getSettingsPage( type );
		if( page != null ) {
			// Switch to a task thread to get the tool
			getProgram().getTaskManager().submit( Task.of( () -> {
				try {
					// Open the tool but don't make it the active tool
					getProgram().getResourceManager().openAsset( ShapePropertiesResourceType.URI, getWorkpane(), true, false ).get();

					// Fire the event on the FX thread
					Fx.run( () -> getWorkspace().getEventBus().dispatch( new ShapePropertiesToolEvent( this, ShapePropertiesToolEvent.SHOW, page, settings ) ) );
				} catch( Exception exception ) {
					log.atWarn( exception ).log();
				}
			} ) );
		} else {
			log.atError().log( "Unable to find properties page for %s", type.getName() );
		}
	}

	private void hidePropertiesPage() {
		getWorkspace().getEventBus().dispatch( new ShapePropertiesToolEvent( this, ShapePropertiesToolEvent.HIDE ) );
	}

	protected void doStoreEnabledLayers( ListChangeListener.Change<? extends DesignLayer> c ) {
		c.next();
		getSettings().set( ENABLED_LAYERS, c.getList().stream().map( IdNode::getId ).collect( Collectors.toSet() ) );
	}

	protected void doStoreVisibleLayers( ListChangeListener.Change<? extends DesignLayer> c ) {
		c.next();
		getSettings().set( VISIBLE_LAYERS, c.getList().stream().map( IdNode::getId ).collect( Collectors.toSet() ) );
	}

	protected void onSelectedShapesChanged( ListChangeListener.Change<? extends DesignShape> c ) {
		while( c.next() ) {
			c.getRemoved().forEach( s -> s.setSelected( false ) );
			c.getAddedSubList().forEach( s -> s.setSelected( true ) );

			int size = c.getList().size();

			if( size == 0 ) {
				showPropertiesPage( getSelectedLayer() );
			} else if( size == 1 ) {
				c.getList().stream().findFirst().ifPresent( this::showPropertiesPage );
			} else {
				// If all selected shapes are of the same type then show the properties page for that type
				Class<? extends DesignDrawable> type = c.getList().getFirst().getClass();

				// Otherwise show the general DesignShape properties page
				for( DesignShape shape : c.getList() ) {
					if( shape.getClass() != type ) {
						type = DesignShape.class;
						break;
					}
				}

				showPropertiesPage( new MultiNodeSettings( c.getList() ), type );
			}
		}

		// Request a render
		renderer.render();

		getDeleteAction().updateEnabled();
	}

	protected class PrintAction extends ProgramAction {

		protected PrintAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public void handle( ActionEvent event ) {
			getProgram().getTaskManager().submit( new DesignPrintTask( getProgram(), BaseDesignTool.this, getResource(), (DesignPrint)null ) );
			//getProgram().getTaskManager().submit( new DesignAwtPrintTask( getProgram(), FxRenderDesignTool.this, getAsset(), (DesignPrint)null ) );
		}

	}

	// FIXME Is this a duplicate of com.acromere.xenon.action.PropertiesAction?
	protected class PropertiesAction extends ProgramAction {

		protected PropertiesAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public void handle( ActionEvent event ) {
			// Get the settings pages for the asset type
			Resource resource = getResource();
			SettingsPage assetSettingsPage = resource.getType().getSettingsPages().get( "grid" );
			SettingsPage designSettingsPage = resource.getType().getSettingsPages().get( "asset" );

			Settings assetSettings = getAssetSettings();
			Settings designSettings = new NodeSettings( getResource().getModel() );

			// Set the settings for the pages
			assetSettingsPage.setSettings( assetSettings );
			designSettingsPage.setSettings( designSettings );

			// Switch to a task thread to get the tool
			getProgram().getTaskManager().submit( Task.of( () -> {
				try {
					// Show the properties tool
					getProgram().getResourceManager().openAsset( ProgramPropertiesType.URI, getWorkpane() ).get();

					// Fire the show request on the workspace event bus
					PropertiesToolEvent toolEvent = new PropertiesToolEvent( PropertiesAction.this, PropertiesToolEvent.SHOW, designSettingsPage, assetSettingsPage );
					Workspace workspace = getProgram().getWorkspaceManager().getActiveWorkspace();
					FxEventHub workspaceEventBus = workspace.getEventBus();
					Fx.run( () -> workspaceEventBus.dispatch( toolEvent ) );
				} catch( Exception exception ) {
					log.atError( exception ).log();
				}
			} ) );
		}

	}

	protected class DeleteAction extends ProgramAction {

		protected DeleteAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return !getSelectedShapes().isEmpty();
		}

		@Override
		public void handle( ActionEvent event ) {
			getCommandContext().command( getMod().getCommandMap().getCommandByAction( "delete" ).getCommand() );
		}

	}

	protected class UndoAction extends ProgramAction {

		protected UndoAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return getResource().getUndoManager().isUndoAvailable();
		}

		@Override
		public void handle( ActionEvent event ) {
			getResource().getUndoManager().undo();
		}

	}

	protected class RedoAction extends ProgramAction {

		protected RedoAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return getResource().getUndoManager().isRedoAvailable();
		}

		@Override
		public void handle( ActionEvent event ) {
			getResource().getUndoManager().redo();
		}

	}

}
