import com.acromere.cartesia.CartesiaMod;
import com.acromere.xenon.Module;

// This should match the group and artifact from the product card,
// or there will be a lot of confusion.
module com.acromere.carta {

	// Compile-time only
	requires static lombok;
	requires static org.jspecify;
	requires static org.mapstruct;

	// Both compile-time and run-time
	requires com.acromere.curve;
	requires com.acromere.marea;
	requires com.acromere.xenon;
	requires com.acromere.zerra;
	requires com.acromere.zevra;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires java.logging;
	requires javafx.base;
	requires javaluator;
	requires org.jsoup;

	// Public APIs
	exports com.acromere.cartesia;
	exports com.acromere.cartesia.command;
	exports com.acromere.cartesia.command.base;
	exports com.acromere.cartesia.command.camera;
	exports com.acromere.cartesia.command.draw;
	exports com.acromere.cartesia.command.layer;
	exports com.acromere.cartesia.command.measure;
	exports com.acromere.cartesia.command.view;
	exports com.acromere.cartesia.command.print;
	exports com.acromere.cartesia.command.snap;
	exports com.acromere.cartesia.command.edit;
	exports com.acromere.cartesia.data;
	exports com.acromere.cartesia.math;
	exports com.acromere.cartesia.snap;
	exports com.acromere.cartesia.trial;

	// Private APIs
	exports com.acromere.cartesia.cursor to com.acromere.zerra;
	exports com.acromere.cartesia.icon to com.acromere.zerra;
	exports com.acromere.cartesia.settings to com.acromere.xenon;
	exports com.acromere.cartesia.tool to com.acromere.xenon;
	exports com.acromere.cartesia.tool.design to com.acromere.xenon;
	exports com.acromere.cartesia.tool.design.binding to org.mapstruct;
	exports com.acromere.cartesia.rb to com.acromere.xenon;

	// Public resources
	opens com.acromere.cartesia.bundles;
	opens com.acromere.cartesia.design.props;
	opens com.acromere.cartesia.settings;

	provides Module with CartesiaMod;

}
