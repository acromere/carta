package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.BaseCommandTest;
import com.acromere.cartesia.command.Command;
import com.acromere.cartesia.command.CommandTask;
import com.acromere.cartesia.tool.DesignPortal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CameraViewPreviousTest extends BaseCommandTest {

	private final Command command = new CameraViewPrevious();

	@Test
	void execute() throws Exception {
		// given
		CommandTask task = new CommandTask( commandContext, tool, null, null, command );

		// when
		Object result = task.runTaskStep();

		// then
		verify( tool, times( 1 ) ).setView( (DesignPortal)any() );
		assertThat( result ).isEqualTo( Command.Result.SUCCESS );
	}

}
