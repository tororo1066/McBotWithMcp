package tororo1066.mcbot_with_mcp.client.server.resources

import io.modelcontextprotocol.kotlin.sdk.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.Resource
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredResource

abstract class AbstractResource {

    abstract fun definition(): Resource

    abstract fun process(request: ReadResourceRequest): ReadResourceResult

    fun getRegisteredResource(): RegisteredResource {
        return RegisteredResource(
            resource = definition(),
            readHandler = { request ->
                process(request)
            }
        )
    }
}