package com.acromere.cartesia.tool.design;

import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestShapeIntersect {

	@Test
	void intersectLinesWithStrokeWidth() {
		Line line1 = new Line( 0, 50, 100, 50 );
		Line line2 = new Line( 50, 0, 50, 100 );

		line1.setStrokeWidth( 10 );
		line2.setStrokeWidth( 10 );

		Shape shape = Shape.intersect( line1, line2 );
		Path path = (Path)shape;
		assertThat( path.getElements() ).isNotEmpty();
	}

	@Test
	void intersectLinesWithoutStrokeWidth() {
		Line line1 = new Line( 0, 50, 100, 50 );
		Line line2 = new Line( 50, 0, 50, 100 );

		line1.setStrokeWidth( 0 );
		line2.setStrokeWidth( 0 );

		Shape shape = Shape.intersect( line1, line2 );
		Path path = (Path)shape;
		assertThat( path.getElements() ).isEmpty();
	}

	@Test
	void intersectBoxAndLineWithStrokeWidth() {
		Rectangle box = new Rectangle( 0, 50, 50, 100 );
		Line line = new Line( 50, 0, 50, 100 );
		assertLineProperties( line );

		line.setStrokeWidth( 10 );

		Shape shape = Shape.intersect( box, line );
		Path path = (Path)shape;
		assertThat( path.getElements() ).isNotEmpty();
	}

	@Test
	void intersectBoxAndLineWithStrokeWidthSpecificUseCase() {
		Rectangle box = new Rectangle( -94.48818897637794, -31.49606299212598, 188.97637795275588, 125.98425196850393 );
		Line line = new Line( -62.99212598425196, -62.99212598425196, 62.99212598425196, 62.99212598425196 );
		assertLineProperties( line );

		box.setFill( Paint.valueOf( "0x000000ff" ) );
		line.setStroke( Paint.valueOf( "0x8080ffff" ) );
		line.setStrokeWidth( 62.99212598425196 );
		line.setStrokeLineCap( StrokeLineCap.ROUND );

		Shape shape = Shape.intersect( box, line );
		assertThat( ((Path)shape).getElements() ).isNotEmpty();
	}

	@Test
	void intersectBoxAndLineWithoutStrokeWidth() {
		Rectangle box = new Rectangle( 0, 50, 50, 100 );
		Line line = new Line( 50, 0, 50, 100 );
		assertLineProperties( line );

		line.setStrokeWidth( 0 );

		Shape shape = Shape.intersect( box, line );
		Path path = (Path)shape;
		assertThat( path.getElements() ).isEmpty();
	}

	private void assertLineProperties( Line line ) {
		assertThat( line.getFill() ).isNull();
	}

}
