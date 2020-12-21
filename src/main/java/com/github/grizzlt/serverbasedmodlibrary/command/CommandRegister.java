package com.github.grizzlt.serverbasedmodlibrary.command;

import com.github.grizzlt.serverbasedmodlibrary.ServerBasedRegisterUtil;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandRegister
{
    private final Map<String, ICommand> commandSet = new HashMap<>();

    private Field commandSetField;

    public CommandRegister()
    {
        try {
            commandSetField = Lists.newArrayList(CommandHandler.class.getDeclaredFields()).stream().filter(field -> field.getType().equals(Set.class)).findFirst().orElseThrow(() -> new IllegalArgumentException("CommandSet was not found in CommandManager!"));
            commandSetField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        if (commandSet.isEmpty()) return;

        try {
            ((Set<ICommand>)commandSetField.get(ClientCommandHandler.instance)).removeAll(commandSet.values());
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        commandSet.forEach((name, command) -> {
            ClientCommandHandler.instance.getCommands().remove(command.getCommandName());
            command.getCommandAliases().forEach(alias -> ClientCommandHandler.instance.getCommands().remove(alias));
        });
    }

    public void addCommands()
    {
        if (ServerBasedRegisterUtil.connectedToServer)
        {
            commandSet.values().forEach(ClientCommandHandler.instance::registerCommand);
        }
    }
}
