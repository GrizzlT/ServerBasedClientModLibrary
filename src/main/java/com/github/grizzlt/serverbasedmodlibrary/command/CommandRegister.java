package com.github.grizzlt.serverbasedmodlibrary.command;

import com.github.grizzlt.serverbasedmodlibrary.ServerBasedRegisterUtil;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;

import java.util.HashMap;
import java.util.Map;

public class CommandRegister
{
    private final Map<String, ICommand> commandSet = new HashMap<>();

    public void register(ICommand command)
    {
        if (commandSet.containsKey(command.getCommandName())) return;

        //the wrapper automatically makes the command available to everyone (client usually doesn't have command perm)
        CommandWrapper wrapper = new CommandWrapper(command);
        commandSet.put(command.getCommandName(), wrapper);
    }

    public void reload()
    {
        clean();
        addCommands();
    }

    public void clean()
    {
        for (ICommand icommand : this.commandSet.values())
        {
            ClientCommandHandler.instance.getCommands().remove(icommand.getCommandName());

            for (String s : icommand.getCommandAliases())
            {
                if (!icommand.getCommandName().equals(s))
                {
                    ClientCommandHandler.instance.getCommands().remove(s);
                }
            }
        }
    }

    public void addCommands()
    {
        if (ServerBasedRegisterUtil.connectedToServer)
        {
            commandSet.values().forEach(ClientCommandHandler.instance::registerCommand);
        }
    }
}
