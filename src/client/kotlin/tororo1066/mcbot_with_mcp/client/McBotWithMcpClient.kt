package tororo1066.mcbot_with_mcp.client

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.ktor.util.collections.ConcurrentMap
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredResource
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tororo1066.mcbot_with_mcp.client.server.resources.AbstractResource
import tororo1066.mcbot_with_mcp.client.server.tools.AbstractTool
import tororo1066.mcbot_with_mcp.client.server.tools.ExecuteCommand
import java.io.File
import java.util.jar.JarFile
import kotlin.collections.set

class McBotWithMcpClient : ClientModInitializer {

    companion object {
        const val MOD_ID = "mcbot_with_mcp"
        const val MOD_NAME = "McBotWithMcp"

        val LOGGER: Logger = LoggerFactory.getLogger(MOD_NAME)
    }

//    private val classes by lazy {
//        val classes = mutableListOf<Class<*>>()
//        val file = File(this.javaClass.protectionDomain.codeSource.location.toURI())
//        val jar = JarFile(file)
//        jar.stream().filter { entry ->
//            entry.name.endsWith(".class") && entry.name.startsWith("tororo1066/mcbot_with_mcp/")
//        }.forEach { entry ->
//            val className = entry.name.replace("/", ".").removeSuffix(".class")
//            runCatching {
//                classes.add(Class.forName(className))
//            }
//        }
//        classes.toList()
//    }
    lateinit var sseServer: EmbeddedServer<*,*>

    override fun onInitializeClient() {
        //log
        LOGGER.info("Initializing $MOD_NAME")
        val servers = ConcurrentMap<String, Server>()
        sseServer = embeddedServer(CIO, host = "0.0.0.0", port = 3002) {
            install(SSE)
            routing {
                sse("/sse") {
                    val transport = SseServerTransport("/message", this)
                    val server = createServer()

                    servers[transport.sessionId] = server

                    server.onClose {
                        servers.remove(transport.sessionId)
                    }

                    server.connect(transport)
                }

                post("/message") {
                    val sessionId: String = call.request.queryParameters["sessionId"]!!
                    val transport = servers[sessionId]?.transport as? SseServerTransport
                    if (transport == null) {
                        call.respond(HttpStatusCode.NotFound, "Session not found")
                        return@post
                    }

                    transport.handlePostMessage(call)
                }
            }
        }.start(wait = false)

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            sseServer.stop(0, 0)
        }
    }

    private fun createServer(): Server {
        val server = Server(
            Implementation(
                name = "Minecraft Client",
                version = "1.0.0"
            ),
            ServerOptions(
                capabilities = ServerCapabilities(
                    prompts = ServerCapabilities.Prompts(listChanged = true),
                    resources = ServerCapabilities.Resources(subscribe = true, listChanged = true),
                    tools = ServerCapabilities.Tools(listChanged = true),
                )
            )
        )
        server.addResources(getResources())
        server.addTools(getTools())
        return server
    }

    private fun getResources(): List<RegisteredResource> {
        return listOf()
//        return classes.filter { it.superclass == AbstractResource::class.java }
//            .mapNotNull { it.getDeclaredConstructor().newInstance() as? AbstractResource }
//            .map { resource ->
//                resource.getRegisteredResource()
//            }
    }

    private fun getTools(): List<RegisteredTool> {
        return listOf(ExecuteCommand().getRegisteredTool())
//        return classes.filter { it.superclass == AbstractTool::class.java }
//            .mapNotNull { it.getDeclaredConstructor().newInstance() as? AbstractTool }
//            .map { tool ->
//                tool.getRegisteredTool()
//            }
    }
}
