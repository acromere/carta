package com.acromere.cartesia.tool.design;

import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
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
		Rectangle box = new Rectangle( 0, 50, 50,100 );
		Line line = new Line( 50, 0, 50, 100 );

		line.setStrokeWidth( 10 );

		Shape shape = Shape.intersect( box, line );
		Path path = (Path)shape;
		assertThat( path.getElements() ).isNotEmpty();
	}

	@Test
	void intersectBoxAndLineWithoutStrokeWidth() {
		Rectangle box = new Rectangle( 0, 50, 50,100 );
		Line line = new Line( 50, 0, 50, 100 );

		line.setStrokeWidth( 0 );

		Shape shape = Shape.intersect( box, line );
		Path path = (Path)shape;
		assertThat( path.getElements() ).isEmpty();
	}

}
