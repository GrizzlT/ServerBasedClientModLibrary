package com.github.grizzlt.serverbasedmodlibrary;

import com.github.grizzlt.serverbasedmodlibrary.command.CommandRegister;
import com.github.grizzlt.serverbasedmodlibrary.event.EventSubscriberRegister;
import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.net.InetSocketAddress;
import java.util.function.Function;

public class ServerBasedRegisterUtil
{
    public static boolean connectedToServer = false;
    public static InetSocketAddress serverAddress = null;

    private final Function<InetSocketAddress, Boolean> serverQualifier;

    private final EventSubscriberRegister subscriberRegister;
    private final CommandRegister commandRegister;

    public ServerBasedRegisterUtil(Function<InetSocketAddress, Boolean> serverQualifier)
    {
        this.serverQualifier = serverQualifier;
        this.subscriberRegister = new EventSubscriberRegister();
        this.commandRegister = new CommandRegister();
    }

    public void Init()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void registerToEventBus(Object subscriber)
    {
        subscriberRegister.register(subscriber);
    }

    public void registerCommand(ICommand command)
    {
        commandRegister.register(command);
    }

    @SubscribeEvent
    public void onConnectToServer(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        if (!(event.manager.channel().remoteAddress() instanceof InetSocketAddress)) return;

        serverAddress = ((InetSocketAddress)event.manager.channel().remoteAddress());
        String hostname = serverAddress.getHostName();
        System.out.println("Connected to " + hostname);

        connectedToServer = serverQualifier.apply(serverAddress);
        commandRegister.reload();
    }

    @SubscribeEvent
    public void onDisconnectFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        connectedToServer = false;
        serverAddress = null;
    }
}
