package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.data.DesignLayer;
import com.acromere.zerra.javafx.Fx;
import lombok.CustomLog;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.acromere.cartesia.TestTimeouts.FX_STABILITY_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class is testing the logic in DesignToolV3Renderer.determineLayerIndex(DesignLayer).
 */
@CustomLog
public class DesignToolV3DetermineLayerIndexUIT extends DesignToolV3BaseUIT {

	@Test
	void layersAreInsertedAccordingToModelOrderRegardlessOfToggleSequence() throws Exception {
		// given: pick three distinct layers from the model in canonical order
		List<DesignLayer> all = getDesignModel().getAllLayers();
		assertThat( all.size() ).isGreaterThanOrEqualTo( 3 );

		DesignLayer first = all.get( 0 );
		DesignLayer second = all.get( 1 );
		DesignLayer third = all.get( 2 );

		// when: toggle visibility in a shuffled order (third, first, second)
		Fx.run( () -> getTool().setLayerVisible( third, true ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		Fx.run( () -> getTool().setLayerVisible( first, true ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		Fx.run( () -> getTool().setLayerVisible( second, true ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		// then: pane indices in the renderer should reflect the model order, not the add order
		DesignToolV3Renderer renderer = (DesignToolV3Renderer)getTool().getRenderer();

		int idxFirstPane = renderer.getPaneIndex( first );
		int idxSecondPane = renderer.getPaneIndex( second );
		int idxThirdPane = renderer.getPaneIndex( third );

		// Collect for optional debugging/assertions
		List<Integer> paneIndices = List.of( idxFirstPane, idxSecondPane, idxThirdPane );
		assertThat( paneIndices ).allMatch( i -> i >= 0 );

		// Assert ordering matches renderer's reversed stacking: first > second > third
		assertThat( idxFirstPane ).isGreaterThan( idxSecondPane );
		assertThat( idxSecondPane ).isGreaterThan( idxThirdPane );

		// And verify that the indices are contiguous in descending order
		List<Integer> sortedDesc = new ArrayList<>( paneIndices );
		sortedDesc.sort( Comparator.reverseOrder() );
		assertThat( sortedDesc.get( 1 ) ).isEqualTo( sortedDesc.get( 0 ) - 1 );
		assertThat( sortedDesc.get( 2 ) ).isEqualTo( sortedDesc.get( 1 ) - 1 );
	}

	@Test
	void midListInsertionPlacesLayerBetweenNeighborsAndMaintainsContiguity() throws Exception {
		// given
		List<DesignLayer> all = getDesignModel().getAllLayers();
		assertThat( all.size() ).isGreaterThanOrEqualTo( 3 );

		DesignLayer l0 = all.get( 0 );
		DesignLayer l1 = all.get( 1 );
		DesignLayer l2 = all.get( 2 );

		// when: show L0 and L2, leaving a gap for L1
		Fx.run( () -> getTool().setLayerVisible( l0, true ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		Fx.run( () -> getTool().setLayerVisible( l2, true ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		// sanity: ordering reflects reversed stacking (l0 above l2)
		DesignToolV3Renderer renderer = (DesignToolV3Renderer)getTool().getRenderer();
		int idx0 = renderer.getPaneIndex( l0 );
		int idx2 = renderer.getPaneIndex( l2 );
		assertThat( idx0 ).isGreaterThan( idx2 );

		// when: now make L1 visible (mid-list insertion)
		Fx.run( () -> getTool().setLayerVisible( l1, true ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		// recompute all indices after insertion (insertion shifts neighbors)
		int idx1 = renderer.getPaneIndex( l1 );
		idx0 = renderer.getPaneIndex( l0 );
		idx2 = renderer.getPaneIndex( l2 );

		// then: indices are in reversed model order and contiguous
		assertThat( idx0 ).isGreaterThan( idx1 );
		assertThat( idx1 ).isGreaterThan( idx2 );

		List<Integer> idx = new ArrayList<>( List.of( idx0, idx1, idx2 ) );
		idx.sort( Comparator.reverseOrder() );
		assertThat( idx.get( 1 ) ).isEqualTo( idx.get( 0 ) - 1 );
		assertThat( idx.get( 2 ) ).isEqualTo( idx.get( 1 ) - 1 );
	}

	@Test
	void hideAndReShowRestoresCorrectOrderingWithoutDuplicates() throws Exception {
		// given
		List<DesignLayer> all = getDesignModel().getAllLayers();
		assertThat( all.size() ).isGreaterThanOrEqualTo( 3 );

		DesignLayer l0 = all.get( 0 );
		DesignLayer l1 = all.get( 1 );
		DesignLayer l2 = all.get( 2 );

		// show three consecutive layers
		Fx.run( () -> {
			getTool().setLayerVisible( l0, true );
			getTool().setLayerVisible( l1, true );
			getTool().setLayerVisible( l2, true );
		} );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		DesignToolV3Renderer renderer = (DesignToolV3Renderer)getTool().getRenderer();

		int before0 = renderer.getPaneIndex( l0 );
		int before1 = renderer.getPaneIndex( l1 );
		int before2 = renderer.getPaneIndex( l2 );

		// initial assertions: reversed model order and contiguous
		assertThat( before0 ).isGreaterThan( before1 );
		assertThat( before1 ).isGreaterThan( before2 );
		List<Integer> before = new ArrayList<>( List.of( before0, before1, before2 ) );
		before.sort( Comparator.reverseOrder() );
		assertThat( before.get( 1 ) ).isEqualTo( before.get( 0 ) - 1 );
		assertThat( before.get( 2 ) ).isEqualTo( before.get( 1 ) - 1 );

		// when: hide the middle layer (l1)
		Fx.run( () -> getTool().setLayerVisible( l1, false ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		// then: l1 pane removed and l0 remains above l2
		assertThat( renderer.getPaneIndex( l1 ) ).isEqualTo( -1 );
		assertThat( renderer.getPaneIndex( l0 ) ).isGreaterThan( renderer.getPaneIndex( l2 ) );

		// when: re-show l1
		Fx.run( () -> getTool().setLayerVisible( l1, true ) );
		Fx.waitForStability( FX_STABILITY_TIMEOUT );

		int after0 = renderer.getPaneIndex( l0 );
		int after1 = renderer.getPaneIndex( l1 );
		int after2 = renderer.getPaneIndex( l2 );

		// then: correct reinsertion between l0 and l2 and contiguity
		assertThat( after0 ).isGreaterThan( after1 );
		assertThat( after1 ).isGreaterThan( after2 );
		List<Integer> after = new ArrayList<>( List.of( after0, after1, after2 ) );
		after.sort( Comparator.reverseOrder() );
		assertThat( after.get( 1 ) ).isEqualTo( after.get( 0 ) - 1 );
		assertThat( after.get( 2 ) ).isEqualTo( after.get( 1 ) - 1 );

		// and: no duplicates for any tested layer
		long dup0 = renderer.layersPane().getChildren().stream().filter( n -> n.getUserData() == l0 ).count();
		long dup1 = renderer.layersPane().getChildren().stream().filter( n -> n.getUserData() == l1 ).count();
		long dup2 = renderer.layersPane().getChildren().stream().filter( n -> n.getUserData() == l2 ).count();
		assertThat( dup0 ).isEqualTo( 1L );
		assertThat( dup1 ).isEqualTo( 1L );
		assertThat( dup2 ).isEqualTo( 1L );
	}
}
