package tororo1066.mcbot_with_mcp.client

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

class Mcbot_with_mcpDataGenerator : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        val pack = fabricDataGenerator.createPack();
    }
}
