package com.acromere.cartesia.tool;

import com.acromere.cartesia.RbKey;
import com.acromere.cartesia.data.DesignPrint;
import com.acromere.cartesia.tool.design.BaseDesignRenderer;
import com.acromere.product.Rb;
import com.acromere.xenon.Xenon;
import com.acromere.xenon.notice.Notice;
import com.acromere.xenon.resource.Resource;
import com.acromere.xenon.task.Task;
import com.acromere.zerra.color.Colors;
import com.acromere.zerra.color.Paints;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.CustomLog;
import lombok.Getter;

@Getter
@CustomLog
public class DesignPrintTask extends Task<Void> {

	private final Xenon program;

	private final DesignTool tool;

	private final Resource resource;

	private final DesignPrint print;

	public DesignPrintTask( final Xenon program, final DesignTool tool, final Resource resource, final DesignPrint print ) {
		this.program = program;
		this.tool = tool;
		this.resource = resource;
		this.print = print;
		setName( Rb.textOr( RbKey.LABEL, "print", "Print" ) + " " + resource.getName() );
	}

	private static Printer getPrinterByName( String name, Printer orElse ) {
		for( Printer p : Printer.getAllPrinters() ) {
			if( p.getName().equalsIgnoreCase( name ) ) return p;
		}
		return orElse;
	}

	@Override
	public Void call() throws Exception {
		log.atWarn().log( "Starting design print task..." );

		attempt2();

		log.atWarn().log( "Design print task complete." );
		return null;
	}

	private void attempt2() throws Exception {
		PrinterJob job = PrinterJob.createPrinterJob();
		if( job == null ) {
			Notice notice = new Notice();
			notice.setTitle( "No Printer Available" );
			notice.setMessage( "There are no printers available on this computer." );
			notice.setType( Notice.Type.WARN );
			getProgram().getNoticeManager().addNotice( notice );
			return;
		}

		// NOTE This can be used to give feedback to the user. It can be bound to a text field
		job.jobStatusProperty().asString();

		// NOTE This is a rather Swing looking dialog, maybe handle print properties separately
		//				boolean print = job.showPageSetupDialog( getScene().getWindow() );
		boolean print = job.showPrintDialog( getProgram().getWorkspaceManager().getActiveStage() );
		if( !print ) return;

		// Render the design -------------------------------------------------------

		boolean successful = printWithSingleRenderPane( job );
		//boolean successful = renderWithMultipleRenderPanes( job );

		// Inform the user ---------------------------------------------------------

		Notice notice = new Notice();
		notice.setTitle( successful ? "Print Job Success" : "Print Job Failure" );
		notice.setMessage( DesignPrintTask.this.getName() );
		notice.setType( successful ? Notice.Type.INFO : Notice.Type.WARN );
		getProgram().getNoticeManager().addNotice( notice );
	}

	private void priorAttempt() throws Exception {
		log.atWarn().log( "Starting design print task..." );

		Printer printer = getPrinterByName( "PDF", Printer.getDefaultPrinter() );

		PrintResolution resolution = printer.getPrinterAttributes().getDefaultPrintResolution();
		for( PrintResolution r : printer.getPrinterAttributes().getSupportedPrintResolutions() ) {
			if( r.getFeedResolution() > resolution.getFeedResolution() ) resolution = r;
		}

		// Print setup properties --------------------------------------------------

		// TODO This should come from the DesignPrint setup
		// There is no concept of custom paper sizes in FX.
		// According to FX the authoritative size for paper is from the printer itself
		// However, the Paper class has common sizes that can be used for convenience.
		Paper paper = Paper.NA_LETTER;

		// TODO This should come from the DesignPrint setup
		PageOrientation orientation = PageOrientation.PORTRAIT;

		// TODO These should come from the DesignPrint setup
		double leftMargin = 0.5 * 72;
		double rightMargin = 0.5 * 72;
		double topMargin = 0.5 * 72;
		double bottomMargin = 0.5 * 72;

		PageLayout layout = printer.createPageLayout( paper, orientation, leftMargin, rightMargin, topMargin, bottomMargin );

		// Configure the print job -------------------------------------------------

		PrinterJob job = PrinterJob.createPrinterJob();

		// Link the print job to the selected printer
		job.setPrinter( printer );

		JobSettings settings = job.getJobSettings();
		settings.setPageLayout( layout );
		settings.setPrintResolution( resolution );
		settings.setPrintQuality( PrintQuality.HIGH );
		settings.setPaperSource( PaperSource.AUTOMATIC );

		// NOTE This can be used to give feedback to the user. It can be bound to a text field
		job.jobStatusProperty().asString();

		// NOTE This is a rather Swing looking dialog, maybe handle print properties separately
		//				boolean print = job.showPageSetupDialog( getScene().getWindow() );
		//				boolean print = job.showPrintDialog( getScene().getWindow() );
		//				if( !print ) return;

		// Render the design -------------------------------------------------------

		boolean successful = printWithSingleRenderPane( job );
		//boolean successful = renderWithMultipleRenderPanes( job );

		// Inform the user ---------------------------------------------------------

		Notice notice = new Notice();
		notice.setTitle( successful ? "Print Job Success" : "Print Job Failure" );
		notice.setMessage( DesignPrintTask.this.getName() );
		notice.setType( successful ? Notice.Type.INFO : Notice.Type.WARN );
		getProgram().getNoticeManager().addNotice( notice );
	}

	private boolean renderWithMultipleRenderPanes( PrinterJob job ) {
		PageLayout layout = job.getJobSettings().getPageLayout();

		// NEXT Tactic is to try a bunch of small canvases contained in a GridPane

		// Each canvas will be focused on a small portion of the design to provide
		// a better print quality.

		// But wait! If FX limits the rendering canvas to 4K-ish, how will this even work?
		// We think this will get around this problem: https://bugs.openjdk.org/browse/JDK-8090822
		// The overall canvas can be larger than 4K, but each individual canvas will be smaller.

		// We're limited to a 4k-byte patch, so how big is that in pixels?
		double maxBytes = 4096; // 64x64 tiny
		// But we know we have printed larger than that from experimentation (see below).
		// Per experiment, we could print 7.5 x 10 at 72dpi and 144 dpi without trouble.
		// Not sure if that was because there wasn't much detail, or if that was because
		// we truly had the room.

		Node page = new Canvas();
		return job.printPage( layout, page ) && job.endJob();
	}

	private boolean printWithSingleRenderPane( PrinterJob job ) throws Exception {
		PageLayout layout = job.getJobSettings().getPageLayout();

		// TODO Can this factor be nicely linked to the DPI of the printer?
		double factor = 8;
		double inverse = 1 / factor;

		// The NEW way
		Class<? extends BaseDesignRenderer> rendererClass = tool.getPrintDesignRendererClass();
		final BaseDesignRenderer renderer = rendererClass.getDeclaredConstructor().newInstance();
		//renderer.setBackground( Background.fill( Color.LIGHTGRAY ) );
		renderer.setDesign( resource.getModel() );
		renderer.setVisibleLayers( tool.getVisibleLayers() );

		renderer.setDpi( factor * 72, factor * 72 );
		renderer.setPrefWidth( layout.getPrintableWidth() );
		renderer.setPrefHeight( layout.getPrintableHeight() );
		renderer.setViewCenter( tool.getViewCenter() );
		renderer.setViewRotate( tool.getViewRotate() );
		renderer.setViewZoom( inverse * tool.getViewZoom(), inverse * tool.getViewZoom() );

		//renderer.setReferenceLayerVisible( false );

		// FIXME This is changing the actual geometry colors...not just copying them to the print
		// Move this to the renderer and let it change the colors of the FX geometry
		// Invert the colors if using a dark theme
		// TODO This should eventually be a user preference
		//		if( getProgram().getWorkspaceManager().getThemeMetadata().isDark() ) {
		//			Fx.run( () -> renderer.getVisibleShapes().forEach( s -> {
		//				s.setDrawPaint( invertLuminance( s.calcDrawPaint() ) );
		//				s.setFillPaint( invertLuminance( s.calcFillPaint() ) );
		//			} ) );
		//		}

		// Do the actual rendering
		// It is NOT required to do this on the FX thread
		renderer.print( factor );

		return job.printPage( layout, renderer ) && job.endJob();
	}

	private String invertLuminance( Paint paint ) {
		if( paint instanceof Color color ) return Paints.toString( Colors.invertLuminance( color ) );
		return Paints.toString( paint );
	}

}
