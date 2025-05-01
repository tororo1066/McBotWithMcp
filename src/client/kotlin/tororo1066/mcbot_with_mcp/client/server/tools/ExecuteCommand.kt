package tororo1066.mcbot_with_mcp.client.server.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import net.minecraft.client.MinecraftClient
import java.time.LocalDateTime

class ExecuteCommand: AbstractTool() {
    override fun definition(): Tool {
        return Tool(
            name = "mc_client_execute_command",
            description = "Execute a command",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("command") {
                        put("type", "string")
                        put("description", "The command to execute. Do not include the leading slash.")
                    }
                },
                required = listOf("command"),
            )
        )
    }

    override fun process(request: CallToolRequest): CallToolResult {

        val command = request.arguments["command"]!!.jsonPrimitive.content

        MinecraftClient.getInstance().execute {
            MinecraftClient.getInstance().player?.networkHandler?.sendCommand(command)
        }

        return CallToolResult(
            content = listOf(TextContent("Executed command at ${LocalDateTime.now()}")),
        )
    }

}