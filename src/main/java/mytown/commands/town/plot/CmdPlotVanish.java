package mytown.commands.town.plot;

import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.town.Town;
import mytown.x_handlers.VisualsTickHandler;
import mytown.interfaces.ITownPlot;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.plot.vanish")
public class CmdPlotVanish extends CommandBase {
	public CmdPlotVanish(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return parent.canCommandSenderUseCommand(sender);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
		for (ITownPlot plot : town.getPlots()) {
			VisualsTickHandler.instance.unmarkPlotBorders(plot);
		}
		ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.plot.vanished");
	}

	/**
	 * Helper method to return the current MyTownDatasource instance
	 * 
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return X_DatasourceProxy.getDatasource();
	}
}