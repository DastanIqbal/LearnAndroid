package com.dastanapps.javascriptengine

import android.content.Context
import com.dastanapps.javascriptengine.jsengine.JSEngineManager
import com.dastanapps.javascriptengine.jsengine.JSExecutionResult

/**
 * Example showing how to transform real API response data
 * This demonstrates working with the actual JSON structure you provided
 */
class RealAPIDataExample(private val context: Context) {

    private val jsEngine = JSEngineManager(context)

    // Your actual API response as a string
    private val apiResponseJson = """
        {
          "status": "success",
          "data": {
            "error": false,
            "status": true,
            "responseAt": "2025-09-26T13:13:58.540Z",
            "result": {
              "eventinfo": [
                {
                  "id": 102,
                   name: "This is name",
                   category: "This is category",
                   eventLogo: "https://example.com/workshop-logo.png",
                   description: "This is description",
                   eventFromDate: "2024-09-25T14:00:00Z",
                   eventToDate: "2024-09-25T18:00:00Z", 
                   eventLocation: "This is location",
                   targetAudience: "This is audience",
                  "targetAudience": "This is target audience",
                  "category": "This is category",
                  "Classification": "This is Classification",
                  "registrationEnable": "N",
                  "isRegistered": "N"
                }
              ]
            }
          }
        }
    """.trimIndent()

    /**
     * Initialize the engine and load transformation libraries
     */
    suspend fun initialize(): Boolean {
        val initResult = jsEngine.initialize()
        if (!initResult) return false

        // Load the JSONata transformation library
        val libraryResult = jsEngine.loadLibraryFromAssets("jsonata.min.js")
        return libraryResult.success
    }

    /**
     * Transform single event from the API response
     */
    suspend fun transformSingleEvent(): JSExecutionResult {
        val transformScript = """
            // Parse the API response
            const apiResponse = $apiResponseJson;
            
            // Extract the first event
            const eventData = apiResponse.data.result.eventinfo[0];
            
            // Create transformation input structure
            const transformInput = {
                config: {
                    lang: "en",
                    localization: {
                        "label.date.from": "From",
                        "label.date.to": "to",
                        "label.targetAudiance": "Target Audience:",
                        "label.contact": "Contact:",
                        "label.registration": "Registration:",
                        "label.classification": "Type:",
                        "label.time": "Time:"
                    }
                },
                data: {
                    result: {
                        eventinfo: {
                            name: eventData.name,
                            category: eventData.category,
                            eventLogo: "https://google.com//events/food-week-logo.png",
                            description: eventData.description,
                            eventFromDate: eventData.eventFromDate,
                            eventToDate: eventData.eventToDate,
                            eventLocation: eventData.eventLocation,
                            targetAudience: eventData.targetAudience
                        }
                    },
                    filters: {
                        category: eventData.category.toLowerCase().replace(/\s+/g, "_"),
                        hasRegistration: eventData.registrationEnable === "Y",
                        hasSeats: eventData.availableSeats === "Y",
                        id: eventData.id
                    }
                },
                localizationStrings: {}
            };
            
            // Apply basic transformation
            const transformed = transformEventData(transformInput);
            
            // Add custom captions for additional fields
            const customCaptions = [
                {
                    "text": transformInput.config.localization["label.time"] + " " + 
                           eventData.eventTimeFrom + " - " + eventData.eventTimeTo,
                   
                    "type": "time"
                },
                {
                    "text": transformInput.config.localization["label.contact"] + " " + 
                           eventData.email + " | " + eventData.mobile,
                    
                    "type": "contact"
                },
                {
                    "text": transformInput.config.localization["label.classification"] + " " + eventData.Classification,
                    
                    "type": "classification"
                },
                {
                    "text": transformInput.config.localization["label.registration"] + " " + 
                           (eventData.registrationEnable === "Y" ? "Available" : "Not Available"),
                   
                    "type": eventData.registrationEnable === "Y" ? "success" : "info"
                }
            ];
            
            // Add custom captions to the result
            transformed.data.list[0].props.captions.push(...customCaptions);
            
            // Add metadata about the original API response
            transformed.data.list[0].props.metadata = {
                originalId: eventData.id,
                apiResponseTime: apiResponse.data.responseAt,
                registrationEnabled: eventData.registrationEnable === "Y",
                availableSeats: eventData.availableSeats === "Y",
                isUserRegistered: eventData.isRegistered === "Y",
                eventTimes: {
                    from: eventData.eventTimeFrom,
                    to: eventData.eventTimeTo
                }
            };
            
            JSON.stringify(transformed, null, 2);
        """.trimIndent()

        return jsEngine.executeScript(transformScript, "real-api-transform.js")
    }

    /**
     * Transform all events from the API response
     */
    suspend fun transformAllEvents(): JSExecutionResult {
        val transformAllScript = """
            // Parse the API response
            const apiResponse = $apiResponseJson;
            
            // Transform all events
            const allTransformed = apiResponse.data.result.eventinfo.map(eventData => {
                const transformInput = {
                    config: {
                        lang: "en",
                        localization: {
                            "label.date.from": "From",
                            "label.date.to": "to",
                            "label.targetAudiance": "Target Audience:",
                            "label.contact": "Contact:",
                            "label.time": "Time:"
                        }
                    },
                    data: {
                        result: {
                            eventinfo: {
                                name: eventData.name,
                                category: eventData.category,
                                eventLogo: `https://google.com//events/event-${"$"}{eventData.id}-logo.png`,
                                description: eventData.description,
                                eventFromDate: eventData.eventFromDate,
                                eventToDate: eventData.eventToDate,
                                eventLocation: eventData.eventLocation,
                                targetAudience: eventData.targetAudience
                            }
                        },
                        filters: {
                            category: eventData.category.toLowerCase().replace(/\s+/g, "_"),
                            id: eventData.id
                        }
                    },
                    localizationStrings: {}
                };
                
                const transformed = transformEventData(transformInput);
                
                // Add event-specific metadata
                transformed.data.list[0].props.metadata = {
                    eventId: eventData.id,
                    hasRegistration: eventData.registrationEnable === "Y",
                    contactInfo: {
                        email: eventData.email,
                        mobile: eventData.mobile
                    },
                    classification: eventData.Classification,
                    eventTimes: {
                        from: eventData.eventTimeFrom,
                        to: eventData.eventTimeTo
                    }
                };
                
                return transformed.data.list[0];
            });
            
            // Return consolidated result
            JSON.stringify({
                status: "success",
                data: {
                    list: allTransformed,
                    filters: apiResponse.data.result.eventinfo.map(event => ({
                        id: event.id,
                        category: event.category,
                        hasRegistration: event.registrationEnable === "Y",
                        hasSeats: event.availableSeats === "Y"
                    })),
                    metadata: {
                        totalEvents: allTransformed.length,
                        apiResponseTime: apiResponse.data.responseAt,
                        transformedAt: new Date().toISOString()
                    }
                }
            }, null, 2);
        """.trimIndent()

        return jsEngine.executeScript(transformAllScript, "all-events-transform.js")
    }

    /**
     * Extract and format event data for specific use cases
     */
    suspend fun extractEventSummary(): JSExecutionResult {
        val summaryScript = """
            // Parse the API response
            const apiResponse = $apiResponseJson;
            
            // Create event summary
            const eventSummaries = apiResponse.data.result.eventinfo.map(event => ({
                id: event.id,
                title: event.name,
                category: event.category,
                dates: {
                    from: event.eventFromDate,
                    to: event.eventToDate,
                    fromTime: event.eventTimeFrom,
                    toTime: event.eventTimeTo
                },
                location: event.eventLocation,
                contact: {
                    email: event.email,
                    mobile: event.mobile
                },
                registration: {
                    enabled: event.registrationEnable === "Y",
                    userRegistered: event.isRegistered === "Y",
                    seatsAvailable: event.availableSeats === "Y"
                },
                classification: event.Classification,
                description: event.description.substring(0, 100) + "...", // Truncated description
                targetAudience: event.targetAudience.split(",").map(s => s.trim()) // Convert to array
            }));
            
            JSON.stringify({
                summary: {
                    totalEvents: eventSummaries.length,
                    responseTime: apiResponse.data.responseAt,
                    events: eventSummaries
                }
            }, null, 2);
        """.trimIndent()

        return jsEngine.executeScript(summaryScript, "event-summary.js")
    }

    /**
     * Bind custom Kotlin functions for additional processing
     */
    suspend fun transformWithKotlinIntegration(): JSExecutionResult {
        // Bind Kotlin functions
        jsEngine.bindFunction("formatEventDate") { _, args ->
            val dateString = if (args.length() > 0) args.getString(0) else ""
            try {
                val date = java.time.Instant.parse(dateString)
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
                    .withZone(java.time.ZoneId.systemDefault())
                    .format(date)
            } catch (e: Exception) {
                dateString
            }
        }

        jsEngine.bindFunction("validateEmail") { _, args ->
            val email = if (args.length() > 0) args.getString(0) else ""
            email.contains("@") && email.contains(".")
        }

        jsEngine.bindVoidFunction("logEventProcessing") { _, args ->
            val eventName = if (args.length() > 0) args.getString(0) else "Unknown"
            val eventId = if (args.length() > 1) args.getInteger(1).toString() else "Unknown"
            println("Processing event: $eventName (ID: $eventId)")
        }

        val integrationScript = """
            // Parse the API response
            const apiResponse = $apiResponseJson;
            
            // Process events with Kotlin integration
            const processedEvents = apiResponse.data.result.eventinfo.map(event => {
                // Log processing
                logEventProcessing(event.name, event.id);
                
                const transformInput = {
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
                            eventinfo: {
                                name: event.name,
                                category: event.category,
                                eventLogo: `https://example.com/event-${"$"}{event.id}.png`,
                                description: event.description,
                                eventFromDate: event.eventFromDate,
                                eventToDate: event.eventToDate,
                                eventLocation: event.eventLocation,
                                targetAudience: event.targetAudience
                            }
                        },
                        filters: { id: event.id }
                    },
                    localizationStrings: {}
                };
                
                const transformed = transformEventData(transformInput);
                
                // Enhance with Kotlin processing
                transformed.data.list[0].props.enhancedData = {
                    formattedFromDate: formatEventDate(event.eventFromDate),
                    formattedToDate: formatEventDate(event.eventToDate),
                    validEmail: validateEmail(event.email),
                    processedAt: new Date().toISOString(),
                    originalId: event.id
                };
                
                return transformed.data.list[0];
            });
            
            JSON.stringify({
                status: "success",
                data: {
                    list: processedEvents,
                    metadata: {
                        processedBy: "Kotlin Integration",
                        totalProcessed: processedEvents.length
                    }
                }
            }, null, 2);
        """.trimIndent()

        return jsEngine.executeScript(integrationScript, "kotlin-integration.js")
    }

    /**
     * Get the raw API response JSON
     */
    fun getRawAPIResponse(): String = apiResponseJson

    /**
     * Clean up resources
     */
    suspend fun destroy() {
        jsEngine.destroy()
    }
}

/**
 * Usage example
 */
suspend fun useRealAPIDataExample(context: Context) {
    val apiExample = RealAPIDataExample(context)

    try {
        // Initialize
        val initialized = apiExample.initialize()
        if (!initialized) {
            println("Failed to initialize API data transformation")
            return
        }

        // Transform single event
        val singleResult = apiExample.transformSingleEvent()
        if (singleResult.success) {
            println("Single event transformation:")
            println(singleResult.result)
        }

        // Transform all events
        val allResult = apiExample.transformAllEvents()
        if (allResult.success) {
            println("All events transformation:")
            println(allResult.result)
        }

        // Extract summary
        val summaryResult = apiExample.extractEventSummary()
        if (summaryResult.success) {
            println("Event summary:")
            println(summaryResult.result)
        }

        // Transform with Kotlin integration
        val integrationResult = apiExample.transformWithKotlinIntegration()
        if (integrationResult.success) {
            println("Kotlin integration result:")
            println(integrationResult.result)
        }

    } finally {
        apiExample.destroy()
    }
}