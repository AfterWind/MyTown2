package mytown.commands;

import com.google.common.collect.ImmutableList;
import mytown.api.interfaces.IHasFlags;
import mytown.core.Localization;
import mytown.core.utils.command.Command;
import mytown.core.utils.command.CommandCompletion;
import mytown.core.utils.command.CommandManager;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Utils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 8/29/2014.
 * Base class for all classes that hold command methods... Mostly for some utils
 */
public abstract class Commands {
    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }

    public static MyTownUniverse getUniverse() {
        return MyTownUniverse.getInstance();
    }

    public static Localization getLocal() {
        return LocalizationProxy.getLocalization();
    }

    public static boolean callSubFunctions(ICommandSender sender, List<String> args, List<String> subCommands, String callersPermNode) {
        if (args.size() > 0) {
            for (String s : subCommands) {
                String name = CommandManager.commandNames.get(s);
                // Checking if name corresponds and if parent's
                if (name.equals(args.get(0)) && CommandManager.getParentPermNode(s).equals(callersPermNode)) {
                    CommandManager.commandCall(s, sender, args.subList(1, args.size()));
                    return true;
                }
            }
        }

        sendHelpMessage(sender, callersPermNode);

        return false;
    }

    public static void sendHelpMessage(ICommandSender sender, String permissionBase) {
        Resident res = getDatasource().getOrMakeResident(sender);

        List<String> scList = CommandManager.getSubCommandsList(permissionBase);
        String command = null;
        for(String s = permissionBase; s != null; s = CommandManager.getParentPermNode(s)) {
            if(command == null)
                command = CommandManager.commandNames.get(s);
            else
                command = new StringBuilder(command).insert(0, CommandManager.commandNames.get(s) + " ").toString();
        }
        res.sendMessage("/" + command + ": ");
        for(String s : scList) {
            res.sendMessage("   " + CommandManager.commandNames.get(s) + ": " + getLocal().getLocalization(s + ".help"));
        }
    }


    public static boolean firstPermissionBreach(String permission, ICommandSender sender) {
        // Since everybody should have permission to /t
        if (permission.equals("mytown.cmd"))
            return true;

        Resident res = getDatasource().getOrMakeResident(sender);
        // Get its rank with the permissions
        Rank rank = res.getTownRank(res.getSelectedTown());

        if (rank == null) {
            return Rank.outsiderPermCheck(permission);
        }
        return rank.hasPermissionOrSuperPermission(permission);
    }

    public static void populateCompletionMap() {
        List<String> populator = new ArrayList<String>();
        populator.addAll(MyTownUniverse.getInstance().getTownsMap().keySet());
        populator.add("@a");
        CommandCompletion.completionMap.put("townCompletionAndAll", populator);

        populator.clear();
        populator.addAll(MyTownUniverse.getInstance().getTownsMap().keySet());
        CommandCompletion.completionMap.put("townCompletion", populator);

        populator.clear();
        for (Resident res : MyTownUniverse.getInstance().getResidentsMap().values()) {
            populator.add(res.getPlayerName());
        }
        CommandCompletion.completionMap.put("residentCompletion", populator);

        populator = new ArrayList<String>();
        for(FlagType flag : FlagType.values()) {
            populator.add(flag.toString());
        }
        CommandCompletion.completionMap.put("flagCompletion", populator);

        populator = new ArrayList<String>();
        for(FlagType flag : FlagType.values()) {
            if(flag.isWhitelistable())
                populator.add(flag.toString());
        }
        CommandCompletion.completionMap.put("flagCompletionWhitelist", populator);

        populator.clear();
        populator.addAll(Rank.defaultRanks.keySet());
        CommandCompletion.completionMap.put("rankCompletion", populator);
    }

    /* ---- HELPERS ---- */

    public static Town getTownFromResident(Resident res) {
        Town town = res.getSelectedTown();
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        return town;
    }

    public static Town getTownFromName(String name) {
        Town town = MyTownUniverse.getInstance().getTownsMap().get(name);
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.town.notexist"), name);
        return town;
    }

    public static Resident getResidentFromName(String playerName) {
        Resident res = getDatasource().getOrMakeResident(playerName);
        if (res == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.resident.notexist", playerName));
        return res;
    }

    public static Plot getPlotAtResident(Resident res) {
        Town town = getTownFromResident(res);
        Plot plot = town.getPlotAtResident(res);
        if (plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.notInPlot"));
        return plot;
    }

    public static ImmutableList<Town> getInvitesFromResident(Resident res) {
        ImmutableList<Town> list = res.getInvites();
        if (list == null || list.isEmpty())
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.invite.noinvitations"));
        return list;
    }

    public static Flag getFlagFromType(IHasFlags hasFlags, FlagType flagType) {
        Flag flag = hasFlags.getFlag(flagType);
        if(flag == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.flagNotExists", flagType.toString()));
        return flag;
    }

    public static Flag getFlagFromName(IHasFlags hasFlags, String name) {
        Flag flag = hasFlags.getFlag(FlagType.valueOf(name));
        if(flag == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.flagNotExists", name));
        return flag;
    }

    public static TownBlock getBlockAtResident(Resident res) {
        TownBlock block = getDatasource().getBlock(res.getPlayer().dimension, res.getPlayer().chunkCoordX, res.getPlayer().chunkCoordZ);
        if(block == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.claim.notexist"));
        return block;
    }

    public static Rank getRankFromTown(Town town, String rankName) {
        Rank rank = town.getRank(rankName);
        if (rank == null) {
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.rank.notexist", rankName, town.getName()));
        }
        return rank;
    }

    public static Rank getRankFromResident(Resident res) {
        Rank rank = res.getTownRank();
        if (rank == null) {
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        }
        return rank;
    }

    public static Plot getPlotAtPosition(int dim, int x, int y, int z) {
        Town town = Utils.getTownAtPosition(dim, x >> 4, z >> 4);
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.blockNotInPlot"));
        Plot plot = town.getPlotAtCoords(dim, x, y, z);
        if (plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.blockNotInPlot"));
        return plot;
    }

    public static FlagType getFlagTypeFromName(String name) {
        try {
            return FlagType.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.flagNotExists", name));
        }
    }
}
