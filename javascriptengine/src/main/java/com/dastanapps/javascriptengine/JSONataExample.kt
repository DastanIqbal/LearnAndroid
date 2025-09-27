package com.dastanapps.javascriptengine

import android.content.Context
import com.dastanapps.javascriptengine.jsengine.JSEngineManager
import com.dastanapps.javascriptengine.jsengine.JSExecutionResult

/**
 * Example showing how to use JSONata-like transformations with JSEngineManager
 * This demonstrates data transformation using the JavaScript implementation
 */
class JSONataExample(private val context: Context) {

    private val jsEngine = JSEngineManager(context)

    /**
     * Initialize and load the JSONata transformation library
     */
    suspend fun initialize(): Boolean {
        val initResult = jsEngine.initialize()
        if (!initResult) return false

        // Load the transformation library
        val libraryResult = jsEngine.loadLibraryFromAssets("jsonata.min.js")
        return libraryResult.success
    }

    /**
     * Transform event data using the JSONata-like transformation
     */
    suspend fun transformEventData(): JSExecutionResult {
        // Sample input data (this would typically come from your API or database)
        val inputDataScript = """
            const inputData = {
                config: {
                    lang: "en",
                    localization: {
                        "label.date.from": "From",
                        "label.date.to": "to", 
                        "label.targetAudiance": "Target Audience:"
                    }
                },
                data: {
                    result: {
                       "id": 102,
                        "name": "Marvel Comics",
                        "description": "Avenger Assemble",
                        "eventFromDate": "2025-10-06T00:00:00Z",
                        "eventToDate": "2025-10-10T00:00:00Z",
                        "eventTimeFrom": "12:30",
                        "eventTimeTo": "16:30"
                    },
                    filters: {
                        category: "technology",
                        year: 2024
                    }
                },
                localizationStrings: {
                    "ar": {
                        "label.date.from": "من",
                        "label.date.to": "إلى"
                    }
                }
            };
            
            // Apply the transformation
            const transformed = transformEventData(inputData);
            JSON.stringify(transformed, null, 2);
        """.trimIndent()

        return jsEngine.executeScript(inputDataScript, "event-transformation.js")
    }

    /**
     * Transform multiple events at once
     */
    suspend fun transformMultipleEvents(): JSExecutionResult {
        val multipleEventsScript = """
            const events = [
                {
                    config: { lang: "en", localization: null },
                    data: {
                        "id": 102,
                        "name": "Marvel Comics",
                        "description": "Avenger Assemble",
                        "eventFromDate": "2025-10-06T00:00:00Z",
                        "eventToDate": "2025-10-10T00:00:00Z",
                        "eventTimeFrom": "12:30",
                        "eventTimeTo": "16:30"
                    },
                    localizationStrings: {}
                }
            ];
            
            // Transform all events
            const transformedEvents = events.map(eventData => transformEventData(eventData));
            
            JSON.stringify({
                status: "success",
                count: transformedEvents.length,
                events: transformedEvents
            }, null, 2);
        """.trimIndent()

        return jsEngine.executeScript(multipleEventsScript, "multiple-events.js")
    }

    /**
     * Custom transformation with Kotlin data binding
     */
    suspend fun transformWithKotlinData(): JSExecutionResult {
        // Bind Kotlin functions for additional processing
        jsEngine.bindFunction("getCurrentTimestamp") { _, _ ->
            System.currentTimeMillis()
        }

        jsEngine.bindFunction("formatCurrency") { _, args ->
            val amount = if (args.length() > 0) args.getDouble(0) else 0.0
            val currency = if (args.length() > 1) args.getString(1) else "USD"
            "$${String.format("%.2f", amount)} $currency"
        }

        // Bind configuration from Android
        val androidConfig = mapOf(
            "appVersion" to "1.0.0",
            "platform" to "Android",
            "locale" to "en-US",
            "timezone" to "Asia/Dubai"
        )
        jsEngine.bindObject("AndroidConfig", androidConfig)

        val enhancedScript = """
            const eventData = {
                config: {
                    lang: "en",
                    localization: {
                        "label.date.from": "From",
                        "label.date.to": "to",
                        "label.targetAudiance": "Target Audience:",
                        "label.price": "Price:"
                    }
                },
                data: {
                    result: {
                        eventinfo: {
                            name: "This is name",
                            category: "This is category",
                            eventLogo: "https://example.com/workshop-logo.png",
                            description: "This is description",
                            eventFromDate: "2024-09-25T14:00:00Z",
                            eventToDate: "2024-09-25T18:00:00Z", 
                            eventLocation: "This is location",
                            targetAudience: "This is audience",
                            price: 2500.00,
                            currency: "AED"
                        }
                    },
                    filters: { premium: true, price_range: "high" }
                },
                localizationStrings: {}
            };
            
            // Transform with additional Kotlin data
            const baseTransformed = transformEventData(eventData);
            
            // Enhance with Android-specific data
            baseTransformed.data.list[0].props.metadata = {
                processedAt: getCurrentTimestamp(),
                processedBy: AndroidConfig.platform,
                appVersion: AndroidConfig.appVersion,
                locale: AndroidConfig.locale,
                formattedPrice: formatCurrency(eventData.data.result.eventinfo.price, eventData.data.result.eventinfo.currency)
            };
            
            // Add price caption
            baseTransformed.data.list[0].props.captions.push({
                text: lookup(eventData.config.localization, "label.price") + " " + 
                      formatCurrency(eventData.data.result.eventinfo.price, eventData.data.result.eventinfo.currency)
            });
            
            JSON.stringify(baseTransformed, null, 2);
        """.trimIndent()

        return jsEngine.executeScript(enhancedScript, "enhanced-transformation.js")
    }

    /**
     * Clean up resources
     */
    suspend fun destroy() {
        jsEngine.destroy()
    }
}

/**
 * Usage example in Activity
 */
suspend fun useJSONataTransformation(context: Context) {
    val jsonataExample = JSONataExample(context)

    try {
        // Initialize
        val initialized = jsonataExample.initialize()
        if (!initialized) {
            println("Failed to initialize JSONata transformation")
            return
        }

        // Basic transformation
        val basicResult = jsonataExample.transformEventData()
        if (basicResult.success) {
            println("Basic transformation result:")
            println(basicResult.result)
        } else {
            println("Basic transformation failed: ${basicResult.error}")
        }

        // Multiple events transformation
        val multipleResult = jsonataExample.transformMultipleEvents()
        if (multipleResult.success) {
            println("Multiple events transformation result:")
            println(multipleResult.result)
        }

        // Enhanced transformation with Kotlin bindings
        val enhancedResult = jsonataExample.transformWithKotlinData()
        if (enhancedResult.success) {
            println("Enhanced transformation result:")
            println(enhancedResult.result)
        }

    } finally {
        jsonataExample.destroy()
    }
}