package com.acromere.cartesia.command.camera;

import com.acromere.cartesia.BaseCommandTest;
import com.acromere.cartesia.command.Command;
import com.acromere.cartesia.data.DesignLine;
import com.acromere.cartesia.command.CommandTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CameraZoomAllTest extends BaseCommandTest {

	private final Command command = new CameraZoomAll();

	@Test
	void execute() throws Exception {
		// given
		CommandTask task = new CommandTask( commandContext, tool, null, null, command );
		when( tool.getVisibleShapes() ).thenReturn( List.of( new DesignLine( -1, -1, 1, 1 ) ) );

		// when
		Object result = task.runTaskStep();

		// then
		verify( tool, times( 1 ) ).setWorldViewport( any() );
		assertThat( result ).isEqualTo( Command.Result.SUCCESS );
	}

}
