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
                  
                  "type": "default"
                },
                {
                  "text": result.eventinfo.eventLocation,
                  
                  "type": "default"
                },
                 {
                  "text": ${"$"}fromMillis(${"$"}toMillis(result.eventinfo.eventFromDate), "[h]:[m01] [P]") & " " & ${"$"}lookup(${"$"}localization, "label.date.to") &" " & ${"$"}fromMillis(${"$"}toMillis(result.eventinfo.eventToDate), "[h]:[m01] [P]"),
                  
                  "type": "default"
                },
                 {
                  "text":  ${"$"}lookup(${"$"}localization, "label.targetAudiance") & " " & result.eventinfo.targetAudience,
                  "thumbnail": {
                    "dark": "https://google.com//ADLocker/ADPF/Dark/location-target.png",
                    "light": "https://google.com//ADLocker/ADPF/Light/location-target.png"
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
        val initResult = jsEngine.initialize()
        val jsonataResult = jsEngine.loadLibraryFromAssets("jsonata.min.js")
        if (!jsonataResult.success) {
            print("Failed to load JSONata library: ${jsonataResult.error}")
        }

        val libraryResult = jsEngine.loadLibraryFromAssets("sample-library.js")
        if (!libraryResult.success) {
            println("Failed to load library: ${libraryResult.error}")
        }

        // Use functions from the loaded library
        val script = """
             const datda1 = { name: "Alice", age: 30 };
            (async () => {
                const expression = jsonata('name');
                const result = await expression.evaluate(data);  // returns 24
                console.log(JSON.stringify(result));
                JSON.stringify(result)
            })()
        """.trimIndent()

        val result = jsEngine.executeScript(script)
        val r = jsEngine.convertResultToString(result.result)
        println("Library example result: ${result.result}")
        return initResult
    }

    /**
     * Execute the raw JSONata transformation using JavaScript
     * Since we don't have JSONata library, we'll simulate it with JavaScript
     */
    suspend fun executeRawJSONataTransformation(): JSExecutionResult {
        // First, we need to create JavaScript functions that mimic JSONata behavior
        val jsonataSimulationScript = """
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
                                    "dark": "https://google.com//ADLocker/ADPF/Dark/calendar-alt.png",
                                    "light": "https://google.com//ADLocker/ADPF/Light/calendar-alt.png"
                                },
                                "type": "default"
                            },
                            {
                                "text": data.result.eventinfo.eventLocation,
                                "thumbnail": {
                                    "dark": "https://google.com//ADLocker/ADPF/Dark/pin-marker.png",
                                    "light": "https://google.com//ADLocker/ADPF/Light/pin-marker.png"
                                },
                                "type": "default"
                            },
                            {
                                "text": ${'$'}fromMillis(${'$'}toMillis(data.result.eventinfo.eventFromDate), "[h]:[m01] [P]") + " " + 
                                       ${'$'}lookup(localization, "label.date.to") + " " + 
                                       ${'$'}fromMillis(${'$'}toMillis(data.result.eventinfo.eventToDate), "[h]:[m01] [P]"),
                                "thumbnail": {
                                    "dark": "https://google.com//ADLocker/ADPF/Dark/time-clock.png",
                                    "light": "https://google.com//ADLocker/ADPF/Light/time-clock.png"
                                },
                                "type": "default"
                            },
                            {
                                "text": ${'$'}lookup(localization, "label.targetAudiance") + " " + data.result.eventinfo.targetAudience,
                                "thumbnail": {
                                    "dark": "https://google.com//ADLocker/ADPF/Dark/location-target.png",
                                    "light": "https://google.com//ADLocker/ADPF/Light/location-target.png"
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
                        name: "This is name",
                        category: "This is category",
                        eventLogo: "https://example.com/workshop-logo.png",
                        description: "This is description",
                        eventFromDate: "2024-09-25T14:00:00Z",
                        eventToDate: "2024-09-25T18:00:00Z", 
                        eventLocation: "This is location",
                        targetAudience: "This is audience",
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
    suspend fun destroy() {
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
//        val result = rawExample.executeRawJSONataTransformation()
//        if (result.success) {
//            println("JSONata transformation result:")
//            println(result.result)
//        } else {
//            println("Transformation failed: ${result.error}")
//        }
//
//        // Get the raw JSONata string
//        println("Raw JSONata transformation string:")
//        println(rawExample.getRawJSONataString())

    } finally {
        rawExample.destroy()
    }
}