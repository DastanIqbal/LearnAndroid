package com.dastanapps.javascriptengine

import android.content.Context
import com.dastanapps.javascriptengine.jsengine.JSEngineManager
import com.dastanapps.javascriptengine.jsengine.JSExecutionResult

/**
 * Example showing how to use raw JSONata transformation string
 * This demonstrates the original JSONata pattern you provided
 */
class RawJSONataExample(private val context: Context) {

    private val jsEngine = JSEngineManager(context)

    // Your original JSONata transformation as a string
    private val jsonataTransformation = """
        (
          ${"$"}language := config.lang;

          ${"$"}localization := config.localization ? config.localization : ${"$"}lookup(${"$"}localizationStrings, ${"$"}language);
          ${"$"}transformedData := data
            .{
              "props": {
                "title": result.eventinfo.name,
                "subtitle":result.eventinfo.category,
                "image":result.eventinfo.eventLogo,
                "description": result.eventinfo.description,
                "uiType":"media",
                "captions": [
                  {
                  "text": ${"$"}lookup(${"$"}localization, "label.date.from") & " " & 
            ${"$"}fromMillis(${"$"}toMillis(result.eventinfo.eventFromDate), "[MNn] [D1o], [Y0001]") & 
            " " & ${"$"}lookup(${"$"}localization, "label.date.to") &" " & 
            ${"$"}fromMillis(${"$"}toMillis(result.eventinfo.eventToDate), "[MNn] [D1o], [Y0001]"),
                  "thumbnail": {
                     "dark": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Dark/calendar-alt.png",
                    "light": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Light/calendar-alt.png"
                    },
                  "type": "default"
                },
                {
                  "text": result.eventinfo.eventLocation,
                  "thumbnail": {
                     "dark": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Dark/pin-marker.png",
                    "light": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Light/pin-marker.png"
                    },
                  "type": "default"
                },
                 {
                  "text": ${"$"}fromMillis(${"$"}toMillis(result.eventinfo.eventFromDate), "[h]:[m01] [P]") & " " & ${"$"}lookup(${"$"}localization, "label.date.to") &" " & ${"$"}fromMillis(${"$"}toMillis(result.eventinfo.eventToDate), "[h]:[m01] [P]"),
                  "thumbnail": {
                    "dark": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Dark/time-clock.png",
                    "light": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Light/time-clock.png"},
                  "type": "default"
                },
                 {
                  "text":  ${"$"}lookup(${"$"}localization, "label.targetAudiance") & " " & result.eventinfo.targetAudience,
                  "thumbnail": {
                    "dark": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Dark/location-target.png",
                    "light": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Light/location-target.png"
                  },
                  "type": "default"
                }
                ]
              },
              "metadata": {}
            };

          {
            "status": "success",
            "data": { "list": ${"$"}type(${"$"}transformedData) = "array" ? ${"$"}transformedData : [${"$"}transformedData], "filters": data.filters }
          }
        )
    """.trimIndent()

    /**
     * Initialize the engine
     */
    suspend fun initialize(): Boolean {
        return jsEngine.initialize()
    }

    /**
     * Execute the raw JSONata transformation using JavaScript
     * Since we don't have JSONata library, we'll simulate it with JavaScript
     */
    suspend fun executeRawJSONataTransformation(): JSExecutionResult {
        // First, we need to create JavaScript functions that mimic JSONata behavior
        val jsonataSimulationScript = """
            // Simulate JSONata ${'$'}lookup function
            function ${'$'}lookup(obj, key) {
                return obj && obj[key] ? obj[key] : key;
            }
            
            // Simulate JSONata ${'$'}fromMillis function
            function ${'$'}fromMillis(millis, format) {
                if (!millis) return "";
                const date = new Date(millis);
                
                if (format === "[MNn] [D1o], [Y0001]") {
                    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                   "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
                    const day = date.getDate();
                    const ordinal = day > 3 && day < 21 ? 'th' : 
                                   day % 10 === 1 ? 'st' : 
                                   day % 10 === 2 ? 'nd' : 
                                   day % 10 === 3 ? 'rd' : 'th';
                    return months[date.getMonth()] + " " + day + ordinal + ", " + date.getFullYear();
                }
                
                if (format === "[h]:[m01] [P]") {
                    return date.toLocaleTimeString('en-US', { 
                        hour: 'numeric', 
                        minute: '2-digit', 
                        hour12: true 
                    });
                }
                
                return date.toISOString();
            }
            
            // Simulate JSONata ${'$'}toMillis function
            function ${'$'}toMillis(dateString) {
                if (!dateString) return null;
                return new Date(dateString).getTime();
            }
            
            // Simulate JSONata ${'$'}type function
            function ${'$'}type(value) {
                if (Array.isArray(value)) return "array";
                return typeof value;
            }
            
            // Now execute the transformation logic
            function executeJSONataTransformation(config, data, localizationStrings) {
                const language = config.lang;
                const localization = config.localization ? config.localization : ${'$'}lookup(localizationStrings, language);
                
                const transformedData = {
                    "props": {
                        "title": data.result.eventinfo.name,
                        "subtitle": data.result.eventinfo.category,
                        "image": data.result.eventinfo.eventLogo,
                        "description": data.result.eventinfo.description,
                        "uiType": "media",
                        "captions": [
                            {
                                "text": ${'$'}lookup(localization, "label.date.from") + " " + 
                                       ${'$'}fromMillis(${'$'}toMillis(data.result.eventinfo.eventFromDate), "[MNn] [D1o], [Y0001]") + 
                                       " " + ${'$'}lookup(localization, "label.date.to") + " " + 
                                       ${'$'}fromMillis(${'$'}toMillis(data.result.eventinfo.eventToDate), "[MNn] [D1o], [Y0001]"),
                                "thumbnail": {
                                    "dark": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Dark/calendar-alt.png",
                                    "light": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Light/calendar-alt.png"
                                },
                                "type": "default"
                            },
                            {
                                "text": data.result.eventinfo.eventLocation,
                                "thumbnail": {
                                    "dark": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Dark/pin-marker.png",
                                    "light": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Light/pin-marker.png"
                                },
                                "type": "default"
                            },
                            {
                                "text": ${'$'}fromMillis(${'$'}toMillis(data.result.eventinfo.eventFromDate), "[h]:[m01] [P]") + " " + 
                                       ${'$'}lookup(localization, "label.date.to") + " " + 
                                       ${'$'}fromMillis(${'$'}toMillis(data.result.eventinfo.eventToDate), "[h]:[m01] [P]"),
                                "thumbnail": {
                                    "dark": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Dark/time-clock.png",
                                    "light": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Light/time-clock.png"
                                },
                                "type": "default"
                            },
                            {
                                "text": ${'$'}lookup(localization, "label.targetAudiance") + " " + data.result.eventinfo.targetAudience,
                                "thumbnail": {
                                    "dark": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Dark/location-target.png",
                                    "light": "https://static-stg.tamm.abudhabi/static-stage/Project/TAMM/ADLocker/ADPF/Light/location-target.png"
                                },
                                "type": "default"
                            }
                        ]
                    },
                    "metadata": {}
                };
                
                return {
                    "status": "success",
                    "data": { 
                        "list": ${'$'}type(transformedData) === "array" ? transformedData : [transformedData], 
                        "filters": data.filters 
                    }
                };
            }
            
            "JSONata simulation functions loaded successfully";
        """.trimIndent()

        // Load the simulation functions
        val simulationResult =
            jsEngine.executeScript(jsonataSimulationScript, "jsonata-simulation.js")
        if (!simulationResult.success) {
            return JSExecutionResult.error("Failed to load JSONata simulation: ${simulationResult.error}")
        }

        // Now execute the actual transformation
        val transformationScript = """
            // Sample data for transformation
            const config = {
                lang: "en",
                localization: {
                    "label.date.from": "From",
                    "label.date.to": "to",
                    "label.targetAudiance": "Target Audience:"
                }
            };
            
            const data = {
                result: {
                    eventinfo: {
                        name: "ADPF Cultural Festival 2024",
                        category: "Cultural Event",
                        eventLogo: "https://example.com/adpf-logo.png",
                        description: "Annual cultural festival celebrating UAE heritage and traditions",
                        eventFromDate: "2024-12-01T18:00:00Z",
                        eventToDate: "2024-12-03T22:00:00Z",
                        eventLocation: "Al Ain Cultural Center",
                        targetAudience: "Families, Culture Enthusiasts, Tourists"
                    }
                },
                filters: {
                    category: "cultural",
                    location: "al-ain",
                    family_friendly: true
                }
            };
            
            const localizationStrings = {
                "ar": {
                    "label.date.from": "من",
                    "label.date.to": "إلى",
                    "label.targetAudiance": "الجمهور المستهدف:"
                }
            };
            
            // Execute the transformation (simulating your original JSONata)
            const result = executeJSONataTransformation(config, data, localizationStrings);
            
            JSON.stringify(result, null, 2);
        """.trimIndent()

        return jsEngine.executeScript(transformationScript, "jsonata-transformation.js")
    }

    /**
     * Get the raw JSONata transformation string
     */
    fun getRawJSONataString(): String {
        return jsonataTransformation
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        jsEngine.destroy()
    }
}

/**
 * Usage example
 */
suspend fun useRawJSONataTransformation(context: Context) {
    val rawExample = RawJSONataExample(context)

    try {
        // Initialize
        val initialized = rawExample.initialize()
        if (!initialized) {
            println("Failed to initialize engine")
            return
        }

        // Execute the transformation
        val result = rawExample.executeRawJSONataTransformation()
        if (result.success) {
            println("JSONata transformation result:")
            println(result.result)
        } else {
            println("Transformation failed: ${result.error}")
        }

        // Get the raw JSONata string
        println("Raw JSONata transformation string:")
        println(rawExample.getRawJSONataString())

    } finally {
        rawExample.destroy()
    }
}