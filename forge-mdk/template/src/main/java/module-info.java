import ga.ozli.novamodloader.api.ModuleLoaderMod;

// Your mod is a Java module. Its name is also your mod id, and the namespace of everything it registers.
module examplemod {
    requires minecraft;
    requires ga.ozli.novamodloader;
    requires net.minecraftforge.eventbus;
    requires org.slf4j;
    requires logging;

    // Entrypoints loaded on both the client and the dedicated server
    provides ModuleLoaderMod.Common with com.example.examplemod.ExampleMod;
    // Entrypoints only loaded on the client
    provides ModuleLoaderMod.Client with com.example.examplemod.ExampleMod.Client;
}
