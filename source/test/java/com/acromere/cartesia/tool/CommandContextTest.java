package com.acromere.cartesia.tool;

import com.acromere.cartesia.BaseCommandTest;
import com.acromere.cartesia.command.Command;
import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.command.base.Prompt;
import com.acromere.cartesia.command.base.Value;
import com.acromere.cartesia.error.UnknownCommand;
import com.acromere.settings.MapSettings;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommandContextTest extends BaseCommandTest {

	private static boolean configured;

	@BeforeEach
	public void setup() throws Exception {
		super.setup();
		this.commandContext = spy( new CommandContext() );
		lenient().doReturn( tool ).when( commandContext ).getTool();
		lenient().doReturn( commandPrompt ).when( commandContext ).getCommandPrompt();
		if( !configured ) {
			getMod().getCommandMap().add( "test", MockCommand.class, "Test Command", "test", null );
			configured = true;
		}
	}

	@Test
	void handleWithMouseEventAndTrigger() {
		// given
		MouseEvent event = createMouseEvent( tool, null, MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY, false, false, false, false, 48, 17 );

		// when
		commandContext.handle( event );

		// then
		verify( commandContext, times( 1 ) ).submitCommand( any( CommandTask.class ) );
		verify( commandContext, times( 0 ) ).sendEventToCommandStack( any( MouseEvent.class ) );
	}

	@Test
	void handleWithMouseEventNoTrigger() {
		// given
		MouseEvent event = createMouseEvent( tool, null, MouseEvent.ANY, MouseButton.PRIMARY, false, false, false, false, 48, 17 );

		// when
		commandContext.handle( event );

		// then
		verify( commandContext, times( 0 ) ).submitCommand( any( CommandTask.class ) );
		verify( commandContext, times( 1 ) ).sendEventToCommandStack( any( MouseEvent.class ) );
	}

	@Test
	void testNumberInput() throws Exception {
		// given
		MockCommand command = new MockCommand( 1 );
		commandContext.submit( tool, command );
		commandContext.doProcessCommands();
		commandContext.setLastUserTool( tool );

		// when
		commandContext.setInputMode( CommandContext.Input.NUMBER );
		commandContext.processText( "4,3,2", true );
		commandContext.doProcessCommands();

		// then
		assertThat( command.getValues()[ 0 ] ).isEqualTo( "4,3,2" );
	}

	@Test
	void testPointInput() throws Exception {
		// given
		MockCommand command = new MockCommand( 1 );
		commandContext.submit( tool, command );
		commandContext.doProcessCommands();
		commandContext.setLastUserTool( tool );

		// when
		commandContext.setInputMode( CommandContext.Input.POINT );
		commandContext.processText( "4,3,2", true );
		commandContext.doProcessCommands();

		// then
		assertThat( command.getValues()[ 0 ] ).isEqualTo( "4,3,2" );
	}

	@Test
	void testRelativePointInput() throws Exception {
		// given
		MockCommand command = new MockCommand( 1 );
		commandContext.submit( tool, command );
		commandContext.doProcessCommands();
		commandContext.setLastUserTool( tool );

		// when
		commandContext.setInputMode( CommandContext.Input.POINT );
		commandContext.processText( "@4,3,2", true );
		commandContext.doProcessCommands();

		// then
		assertThat( command.getValues()[ 0 ] ).isEqualTo( "@4,3,2" );
	}

	@Test
	void testTextInput() throws Exception {
		// given
		MockCommand command = new MockCommand( 1 );
		commandContext.submit( tool, command );
		commandContext.doProcessCommands();
		commandContext.setLastUserTool( tool );

		// when
		commandContext.setInputMode( CommandContext.Input.TEXT );
		commandContext.processText( "test", true );
		commandContext.doProcessCommands();

		// then
		assertThat( command.getValues()[ 0 ] ).isEqualTo( "test" );
	}

	@Test
	void testUnknownInput() throws Exception {
		// given
		MockCommand command = new MockCommand( 1 );
		commandContext.submit( tool, command );
		commandContext.doProcessCommands();
		commandContext.setLastUserTool( tool );

		// when
		commandContext.setInputMode( CommandContext.Input.NONE );
		UnknownCommand exception = catchThrowableOfType( UnknownCommand.class, () -> commandContext.processText( "unknown", true ) );

		// then
		assertThat( exception.getMessage() ).isEqualTo( "unknown" );
	}

	@Test
	void testCommand() throws Exception {
		MockCommand command = new MockCommand();
		commandContext.submit( tool, command );
		commandContext.doProcessCommands();
		assertThat( command.getValues().length ).isEqualTo( 0 );
	}

	@Test
	void testCommandWithOneParameter() throws Exception {
		MockCommand command = new MockCommand();
		commandContext.submit( tool, command, "0" );
		commandContext.doProcessCommands();
		assertThat( command.getValues()[ 0 ] ).isEqualTo( "0" );
	}

	@Test
	void testCommandWithTwoParameters() throws Exception {
		MockCommand command = new MockCommand();
		commandContext.submit( tool, command, "0", "1" );
		commandContext.doProcessCommands();
		assertThat( command.getValues()[ 0 ] ).isEqualTo( "0" );
		assertThat( command.getValues()[ 1 ] ).isEqualTo( "1" );
	}

	@Test
	void testCommandThatNeedsOneValue() throws Exception {
		MockCommand command = new MockCommand( 1 );
		commandContext.submit( tool, command );
		commandContext.doProcessCommands();
		commandContext.submit( tool, new Value(), "hello" );
		commandContext.doProcessCommands();
		assertThat( command.getValues()[ 0 ] ).isEqualTo( "hello" );
	}

	@Test
	void testCommandThatNeedsTwoValues() throws Exception {
		MockCommand command = new MockCommand( 2 );
		commandContext.submit( tool, command );
		commandContext.doProcessCommands();
		commandContext.submit( tool, new Value(), "0" );
		commandContext.doProcessCommands();
		commandContext.submit( tool, new Value(), "1" );
		commandContext.doProcessCommands();
		assertThat( command.getValues()[ 0 ] ).isEqualTo( "0" );
		assertThat( command.getValues()[ 1 ] ).isEqualTo( "1" );
	}

	@Test
	void testAutoCommand() throws Exception {
		// given
		commandContext.setLastUserTool( tool );
		when( getMod().getSettings() ).thenReturn( new MapSettings() );

		Command command = commandContext.processText( "test", false );
		commandContext.doProcessCommands();
		assertThat( command ).isInstanceOf( MockCommand.class );
	}

	@Test
	void testNoAutoCommandWithTextInput() throws Exception {
		commandContext.setLastUserTool( tool );
		commandContext.setInputMode( CommandContext.Input.TEXT );
		Command command = commandContext.processText( "test", false );
		commandContext.doProcessCommands();
		assertThat( command ).isNull();
	}

	@Test
	void testInputMode() throws Exception {
		assertThat( commandContext.getInputMode() ).isEqualTo( CommandContext.Input.NONE );

		MockCommand command = new MockCommand( 0 );
		commandContext.submit( tool, command );
		commandContext.doProcessCommands();

		commandContext.submit( tool, new Prompt( "", CommandContext.Input.NONE ) );
		commandContext.doProcessCommands();
		assertThat( commandContext.getInputMode() ).isEqualTo( CommandContext.Input.NONE );
		commandContext.submit( tool, new Prompt( "", CommandContext.Input.NUMBER ) );
		commandContext.doProcessCommands();
		assertThat( commandContext.getInputMode() ).isEqualTo( CommandContext.Input.NUMBER );
		commandContext.submit( tool, new Prompt( "", CommandContext.Input.POINT ) );
		commandContext.doProcessCommands();
		assertThat( commandContext.getInputMode() ).isEqualTo( CommandContext.Input.POINT );
		commandContext.submit( tool, new Prompt( "", CommandContext.Input.TEXT ) );
		commandContext.doProcessCommands();
		assertThat( commandContext.getInputMode() ).isEqualTo( CommandContext.Input.TEXT );
		commandContext.submit( tool, new Prompt( "", CommandContext.Input.NONE ) );
		commandContext.doProcessCommands();
		assertThat( commandContext.getInputMode() ).isEqualTo( CommandContext.Input.NONE );
	}

	@Test
	void testFullCommand() throws Exception {
		// Executing this should give a line from 0,0 to 1,1
		String command = "ll 0,0 1,1";
		String[] parameters = command.split( " " );

		// FIXME This is a hack. The context should determine command from the string
		MockCommand mockCommand = new MockCommand( parameters.length - 1 );

		// when
		commandContext.submit( tool, mockCommand, (Object[])parameters );
		commandContext.doProcessCommands();

		// then
		// TODO Verify there is a line from 0,0 to 1,1
	}

}
