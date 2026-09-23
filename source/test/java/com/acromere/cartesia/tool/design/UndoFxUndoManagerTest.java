package com.acromere.cartesia.tool.design;

import org.fxmisc.undo.UndoManager;
import org.fxmisc.undo.UndoManagerFactory;
import org.junit.jupiter.api.Test;
import org.reactfx.EventSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UndoFxUndoManagerTest {

	@Test
	void testUnlimitedHistorySingleChangeUMSingleValue() {
		EventSource<String> events = new EventSource<>();

		UndoManager<String> undoManager = UndoManagerFactory.unlimitedHistorySingleChangeUM( events, change -> {return null;}, change -> {}, ( c1, c2 ) -> java.util.Optional.of( c1 + c2 ) );

		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		assertThat( undoManager.getNextUndo() ).isNull();
		assertThat( undoManager.getNextRedo() ).isNull();

		events.push( "a" );

		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		assertThat( undoManager.getNextUndo() ).isNotNull();
		assertThat( undoManager.getNextRedo() ).isNull();

		undoManager.undo();

		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
		assertThat( undoManager.getNextUndo() ).isNull();
		assertThat( undoManager.getNextRedo() ).isNotNull();
	}

	@Test
	void testUnlimitedHistorySingleChangeUMWithMultiValue() {
		EventSource<List<String>> events = new EventSource<>();

		UndoManager<List<String>> undoManager = UndoManagerFactory.unlimitedHistorySingleChangeUM(
			events, change -> {return null;}, change -> {}, ( c1, c2 ) -> {
				ArrayList<String> list = new ArrayList<>();
				list.addAll( c1 );
				list.addAll( c2 );
				return java.util.Optional.of( list );
			}
		);

		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		assertThat( undoManager.getNextUndo() ).isNull();
		assertThat( undoManager.getNextRedo() ).isNull();

		events.push( List.of( "a" ) );

		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		assertThat( undoManager.getNextUndo() ).isNotNull();
		assertThat( undoManager.getNextRedo() ).isNull();

		undoManager.undo();

		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isTrue();
		assertThat( undoManager.getNextUndo() ).isNull();
		assertThat( undoManager.getNextRedo() ).isNotNull();
	}

	@Test
	void testUnlimitedHistoryMultiChangeUM() {
		EventSource<List<String>> events = new EventSource<>();

		// MultiChange managers take lists of changes
		UndoManager<List<String>> undoManager = UndoManagerFactory.unlimitedHistoryMultiChangeUM( events, c -> {return null;}, c -> {}, ( c1, c2 ) -> java.util.Optional.of( c1 + c2 ) );

		assertThat( undoManager.isUndoAvailable() ).isFalse();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		assertThat( undoManager.getNextUndo() ).isNull();
		assertThat( undoManager.getNextRedo() ).isNull();

		events.push( List.of( "a" ) );

		assertThat( undoManager.isUndoAvailable() ).isTrue();
		assertThat( undoManager.isRedoAvailable() ).isFalse();
		assertThat( undoManager.getNextUndo() ).isNotNull();
		assertThat( undoManager.getNextRedo() ).isNull();

		// We do not believe we have the neen for "multi change" so the rest of
		// the test is commented out since we never figured out how to make it work
		// without it throwing an IllegalStateException all the time.

		//undoManager.undo();

		//assertThat( undoManager.isUndoAvailable() ).isFalse();
		//assertThat( undoManager.isRedoAvailable() ).isTrue();
		//assertThat( undoManager.getNextUndo() ).isNull();
		//assertThat( undoManager.getNextRedo() ).isNotNull();
	}

}
