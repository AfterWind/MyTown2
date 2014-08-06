package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Sub command to create a new town
 * 
 * @author Joe Goett
 */
@Permission("mytown.cmd.outsider.new")
public class CmdNewTown extends CommandBase {

	public CmdNewTown(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		EntityPlayer player = (EntityPlayer) sender;
        if (args.length < 1)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.newtown"));
        if (getDatasource().hasTown(args[0])) // Is the town name already in use?
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.newtown.nameinuse", args[0]));
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ)) // Is the Block already claimed?   TODO Bit-shift the coords?
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.newtown.positionError"));

        Town town = getDatasource().newTown(args[0]); // Attempt to create the Town
        if (town == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.newtown.failed"));

        Resident res = getDatasource().getOrMakeResident(player.getPersistentID(), true); // Attempt to get or make the Resident
        if (res == null)
            throw new CommandException("Failed to get or save resident"); // TODO Localize!

        town.addResident(res);
        if (!getDatasource().saveTown(town))
            throw new CommandException("Failed to save Town"); // TODO Localize!
        // TODO Town Flags
        // TODO Set Town spawn
        // TODO Link Resident to Town

        res.sendMessage(MyTown.getLocal().getLocalization("mytown.notification.town.created", town.getName()));
	}

	/**
	 * Helper method to return the current MyTownDatasource instance
	 * 
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return DatasourceProxy.getDatasource();
	}
}