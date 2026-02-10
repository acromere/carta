package com.acromere.cartesia.command.print;

import com.acromere.cartesia.command.CommandTask;

import static com.acromere.cartesia.command.Command.Result.SUCCESS;

public class PrintDelete extends PrintCommand {

	@Override
	public Object execute( CommandTask task ) throws Exception {
//		DesignPrint print = task.getTool().getSelectedPrint();
//		if( print == null ) return SUCCESS;
//
//		task.getTool().getDesign().removePrint( print );

		return SUCCESS;
	}

}
