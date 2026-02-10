package com.acromere.cartesia.tool;

import com.acromere.cartesia.BaseCommandTest;
import com.acromere.cartesia.command.Command;
import com.acromere.cartesia.command.CommandTask;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandTaskTest extends BaseCommandTest {

	private Command command;

	private CommandTask task;

	@BeforeEach
	protected void setup() throws Exception {
		super.setup();
		command = new MockCommand();
		task = new CommandTask( commandContext, tool, null, null, command );
	}

	@Test
	void testAddNullParameter() {
		task.addParameter( null );
		assertThat( task.getParameterCount() ).isEqualTo( 0 );
	}

	@Test
	void testAddNormalParameter() {
		task.addParameter( new Point3D( 0, 0, 0 ) );
		task.addParameter( new Point3D( 1, 0, 0 ) );
		assertThat( task.getParameterCount() ).isEqualTo( 2 );
	}

	@Test
	void testAddArrayParameter() {
		task.addParameter( new Point3D( 0, 0, 0 ) );
		task.addParameter( new Point3D[]{ new Point3D( 1, 0, 0 ), new Point3D( 2, 0, 0 ) } );
		task.addParameter( new Point3D( 3, 0, 0 ) );
		assertThat( task.getParameterCount() ).isEqualTo( 4 );
	}

}
