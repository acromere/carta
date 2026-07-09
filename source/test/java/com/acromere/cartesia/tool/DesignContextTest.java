package com.acromere.cartesia.tool;

import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.data.DesignShape;
import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DesignContextTest {

	private DesignContext context;

	@BeforeEach
	void setup() {
		context = new DesignContext();
	}

	@Test
	void setAndGetSelectedShapes() {
		// given
		DesignLine line = new DesignLine();
		assertThat( context.getSelectedShapes() ).isEmpty();

		// when
		context.setSelectedShapes( List.of( line ), true );

		// then
		assertThat( context.getSelectedShapes() ).containsExactlyInAnyOrder( line );
	}

	@Test
	void selectedShapes() {
		// given
		DesignLine line = new DesignLine();
		assertThat( context.getSelectedShapes() ).isEmpty();
		List<ListChangeListener.Change<? extends DesignShape>> changes = new ArrayList<>();
		context.selectedShapes().addListener( (ListChangeListener<DesignShape>)changes::add );

		// when
		context.setSelectedShapes( List.of( line ), true );

		// then
		assertThat( changes).hasSize( 1 );
	}

}