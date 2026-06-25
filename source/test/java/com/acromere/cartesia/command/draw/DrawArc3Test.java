package com.acromere.cartesia.command.draw;

import com.acromere.cartesia.BaseCommandTest;
import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.command.InvalidInputException;
import com.acromere.cartesia.command.base.Prompt;
import com.acromere.cartesia.data.DesignArc;
import com.acromere.cartesia.data.DesignLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Objects;
import java.util.stream.Stream;

import static com.acromere.cartesia.command.Command.Result.INCOMPLETE;
import static com.acromere.cartesia.command.Command.Result.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class DrawArc3Test extends BaseCommandTest {

	private final DrawArc3 command = new DrawArc3();

	/**
	 * Draw arc with no parameters or event. Should prompt the
	 * user to select an origin point. The result should be incomplete.
	 *
	 * @throws Exception If an error occurs during the test
	 */
	@Test
	void testRunTaskStepNoParameters() throws Exception {
		// given
		CommandTask task = new CommandTask( commandContext, tool, null, null, command );

		// when
		Object result = task.runTaskStep();

		// then
		verify( commandContext, times( 1 ) ).submit( eq( tool ), any( Prompt.class ) );
		verify( tool, timeout( FX_TIMEOUT ).times( 1 ) ).setCursor( RETICLE );
		assertThat( Objects.requireNonNull( command.getReference().stream().findFirst().orElse( null ) ) ).isInstanceOf( DesignLine.class );
		assertThat( command.getReference() ).hasSize( 1 );
		assertThat( result ).isEqualTo( INCOMPLETE );
	}

	@Test
	void testRunTaskStepWithThreeParameters() throws Exception {
		// given
		CommandTask task = new CommandTask( commandContext, tool, null, null, command, "8,3", "1,0", "1,1" );

		// when
		Object result = task.runTaskStep();

		// then
		verify( commandContext, times( 0 ) ).submit( eq( tool ), any( Prompt.class ) );
		verify( currentLayer, times( 1 ) ).addShape( any( DesignArc.class ) );
		assertThat( command.getReference() ).hasSize( 0 );
		assertThat( command.getPreview() ).hasSize( 0 );
		assertThat( result ).isEqualTo( SUCCESS );
	}

	// Stepped tests -------------------------------------------------------------

	@Test
	void testRunTaskStepWithOneStep() throws Exception {
		// given
		CommandTask task = new CommandTask( commandContext, tool, null, null, command );
		task.runTaskStep();
		task.addParameter( "8,3" );

		// when
		Object result = task.runTaskStep();

		// then
		verify( commandContext, times( 2 ) ).submit( eq( tool ), any( Prompt.class ) );
		verify( tool, timeout( FX_TIMEOUT ).times( 2 ) ).setCursor( RETICLE );

		// There is not enough information to provide a preview arc, so a reference line is used
		assertThat( Objects.requireNonNull( command.getReference().stream().findFirst().orElse( null ) ) ).isInstanceOf( DesignLine.class );
		assertThat( command.getReference() ).hasSize( 1 );
		assertThat( command.getPreview() ).hasSize( 0 );
		assertThat( result ).isEqualTo( INCOMPLETE );
	}

	@Test
	void testRunTaskStepWithTwoSteps() throws Exception {
		// given
		CommandTask task = new CommandTask( commandContext, tool, null, null, command );
		task.runTaskStep();
		task.addParameter( "8,3" );
		task.runTaskStep();
		task.addParameter( "1,0" );

		// when
		Object result = task.runTaskStep();

		// then
		verify( commandContext, times( 3 ) ).submit( eq( tool ), any( Prompt.class ) );
		verify( tool, timeout( FX_TIMEOUT ).times( 3 ) ).setCursor( RETICLE );

		// The reference line is replaced with a preview arc
		assertThat( command.getReference() ).hasSize( 0 );
		assertThat( Objects.requireNonNull( command.getPreview().stream().findFirst().orElse( null ) ) ).isInstanceOf( DesignArc.class );
		assertThat( command.getPreview() ).hasSize( 1 );
		assertThat( result ).isEqualTo( INCOMPLETE );
	}

	@Test
	void testRunTaskStepWithThreeSteps() throws Exception {
		// given
		CommandTask task1 = new CommandTask( commandContext, tool, null, null, command );
		task1.runTaskStep();
		CommandTask task2 = new CommandTask( commandContext, tool, null, null, command, "8,3" );
		task2.runTaskStep();
		CommandTask task3 = new CommandTask( commandContext, tool, null, null, command, "8,3", "1,0" );
		task3.runTaskStep();

		CommandTask task = new CommandTask( commandContext, tool, null, null, command, "8,3", "1,0", "1,1", "0" );

		// when
		Object result = task.runTaskStep();

		// then
		verify( commandContext, times( 3 ) ).submit( eq( tool ), any( Prompt.class ) );
		verify( currentLayer, times( 1 ) ).addShape( any( DesignArc.class ) );
		assertThat( command.getReference() ).hasSize( 0 );
		assertThat( command.getPreview() ).hasSize( 0 );
		assertThat( result ).isEqualTo( SUCCESS );
	}

	// Bad Parameter Tests -------------------------------------------------------

	@ParameterizedTest
	@MethodSource( "provideParametersForTestWithParameters" )
	void testRunTaskStepWithBadParameters( Object[] parameters, String rbKey ) throws Exception {
		// given
		CommandTask task = new CommandTask( commandContext, tool, null, null, command, parameters );

		// when
		InvalidInputException exception = catchThrowableOfType( InvalidInputException.class, task::runTaskStep );

		// then
		verify( commandContext, times( 0 ) ).submit( eq( tool ), any( Prompt.class ) );
		verify( currentLayer, times( 0 ) ).addShape( any() );
		assertThat( exception.getInputRbKey() ).isEqualTo( rbKey );
		assertThat( command.getReference() ).hasSize( 0 );
		assertThat( command.getPreview() ).hasSize( 0 );
	}

	private static Stream<Arguments> provideParametersForTestWithParameters() {
		return Stream.of(
			Arguments.of( new String[]{ "bad parameter" }, "start-point" ),
			Arguments.of( new String[]{ "8,3", "bad parameter" }, "mid-point" ),
			Arguments.of( new String[]{ "8,3", "1,0", "bad parameter" }, "end-point" )
		);
	}

	@Test
	void testExecuteWithBadParameterFourIsIgnored() throws Exception {
		// given
		CommandTask task = new CommandTask( commandContext, tool, null, null, command, "8,3", "1,0", "1,1", "bad parameter" );

		// when
		Object result = task.runTaskStep();

		// then
		verify( currentLayer, times( 1 ) ).addShape( any( DesignArc.class ) );
		assertThat( command.getReference() ).hasSize( 0 );
		assertThat( command.getPreview() ).hasSize( 0 );
		assertThat( result ).isEqualTo( SUCCESS );
	}

}
