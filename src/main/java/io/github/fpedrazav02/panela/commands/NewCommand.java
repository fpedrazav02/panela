package io.github.fpedrazav02.panela.commands;

import io.github.fpedrazav02.panela.Panela;
import io.github.fpedrazav02.panela.PanelaHome;
import picocli.CommandLine.Command;

/**
 * NewCommand
 */
@Command(name = "new", description = "Create a new Panela job or transformation")
public class NewCommand implements Runnable {

	@Override
	public void run() {
		PanelaHome panelaHome = PanelaHome.getInstance();
		System.out.println(panelaHome.getBaseDir());
	}
}
