# Minecraft Server Based Mod Library (Forge)
This is a library to automatically enable and disable *client-side* mods on certain servers.

## Main idea
Don't you sometimes have that feeling of missing features on a server?
And wouldn't developers of such features want them to only be active on certain servers?

This library answers those questions in two ways:

- `@SubscribeEvent`-s (registered through this library) will only be called on the feature developer's server(s) of choice
- `ICommand`-s (registered through this library) will only be registered on the feature developer's server(s) of choice

## Getting Started (for developers of course)
This library can be added to your project using JitPack.
[![](https://jitpack.io/v/GrizzlT/ServerBasedClientModLibrary.svg)](https://jitpack.io/#GrizzlT/ServerBasedClientModLibrary)

Make sure to have a `ServerBasedRegisterUtil` instance somewhere. Also make sure to call that instance's `Init`-method after the `MinecraftForge.EVENT_BUS` has been loaded (nearly instantly).
Use `registerToEventBus()` and `registerCommand()` to register the necessary listeners.


That's all you need to know!
Happy coding!

> Written with [StackEdit](https://stackedit.io/).
