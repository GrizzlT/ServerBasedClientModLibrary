package com.github.grizzlt.serverbasedmodlibrary.event;

import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.*;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Uses the same bus id as {@see net.minecraftforge.fml.common.eventhandler.EventBus} = 0
 */
public class EventSubscriberRegister
{
    private ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = new ConcurrentHashMap<Object, ArrayList<IEventListener>>();
    private Map<Object, ModContainer> listenerOwners = new MapMaker().weakKeys().weakValues().makeMap();

    public void register(Object target)
    {
        if (listeners.containsKey(target))
        {
            return;
        }

        ModContainer activeModContainer = Loader.instance().activeModContainer();
        if (activeModContainer == null)
        {
            FMLLog.log(Level.ERROR, new Throwable(), "Unable to determine registrant mod for %s. This is a critical error and should be impossible", target);
            activeModContainer = Loader.instance().getMinecraftModContainer();
        }
        listenerOwners.put(target, activeModContainer);
        Set<? extends Class<?>> supers = TypeToken.of(target.getClass()).getTypes().rawTypes();
        for (Method method : target.getClass().getMethods())
        {
            for (Class<?> cls : supers)
            {
                try
                {
                    Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                    if (real.isAnnotationPresent(SubscribeEvent.class))
                    {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != 1)
                        {
                            throw new IllegalArgumentException(
                                    "Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
                                            " arguments.  Event handler methods must require a single argument."
                            );
                        }

                        Class<?> eventType = parameterTypes[0];

                        if (!Event.class.isAssignableFrom(eventType))
                        {
                            throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
                        }

                        register(eventType, target, real, activeModContainer);
                        break;
                    }
                }
                catch (NoSuchMethodException e)
                {
                    ;
                }
            }
        }
    }

    private void register(Class<?> eventType, Object target, Method method, ModContainer owner)
    {
        try
        {
            Constructor<?> ctr = eventType.getConstructor();
            ctr.setAccessible(true);
            Event event = (Event)ctr.newInstance();
            ServerASMEventHandler listener = new ServerASMEventHandler(target, method, owner);
            //bus id = 0
            event.getListenerList().register(0, listener.getPriority(), listener);

            listeners.computeIfAbsent(target, k -> new ArrayList<>()).add(listener);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void unregister(Object object)
    {
        ArrayList<IEventListener> list = listeners.remove(object);
        if(list == null)
            return;
        for (IEventListener listener : list)
        {
            //bus id = 0
            ListenerList.unregisterAll(0, listener);
        }
    }
}
