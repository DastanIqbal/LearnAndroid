// JSONata-like transformation implemented in JavaScript
// This replaces the JSONata dependency with pure JavaScript

// Helper function to lookup values in localization object
function lookup(obj, key) {
    return obj && obj[key] ? obj[key] : key;
}

// Helper function to format date from milliseconds
function fromMillis(millis, format) {
    if (!millis) return "";
    
    const date = new Date(millis);
    
    // Simple format parsing - you can extend this for more complex formats
    if (format === "[MNn] [D1o], [Y0001]") {
        const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
                       "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        const day = date.getDate();
        const ordinal = getOrdinal(day);
        return `${months[date.getMonth()]} ${day}${ordinal}, ${date.getFullYear()}`;
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

// Helper to get ordinal suffix (1st, 2nd, 3rd, etc.)
function getOrdinal(day) {
    if (day > 3 && day < 21) return 'th';
    switch (day % 10) {
        case 1: return 'st';
        case 2: return 'nd';
        case 3: return 'rd';
        default: return 'th';
    }
}

// Helper function to convert date string to milliseconds
function toMillis(dateString) {
    if (!dateString) return null;
    return new Date(dateString).getTime();
}

// Main transformation function that mimics the JSONata logic
function transformEventData(inputData) {
    const { config, data, localizationStrings } = inputData;
    
    // Extract language and localization
    const language = config.lang;
    const localization = config.localization || lookup(localizationStrings, language) || {};
    
    // Transform the data
    const transformedData = {
        props: {
            title: data.result.eventinfo.name,
            subtitle: data.result.eventinfo.category,
            image: data.result.eventinfo.eventLogo,
            description: data.result.eventinfo.description,
            uiType: "media",
            captions: [
                {
                    text: `${lookup(localization, "label.date.from")} ${fromMillis(toMillis(data.result.eventinfo.eventFromDate), "[MNn] [D1o], [Y0001]")} ${lookup(localization, "label.date.to")} ${fromMillis(toMillis(data.result.eventinfo.eventToDate), "[MNn] [D1o], [Y0001]")}`,

                    type: "default"
                },
                {
                    text: data.result.eventinfo.eventLocation,

                    type: "default"
                },
                {
                    text: `${fromMillis(toMillis(data.result.eventinfo.eventFromDate), "[h]:[m01] [P]")} ${lookup(localization, "label.date.to")} ${fromMillis(toMillis(data.result.eventinfo.eventToDate), "[h]:[m01] [P]")}`,

                    type: "default"
                },
                {
                    text: `${lookup(localization, "label.targetAudiance")} ${data.result.eventinfo.targetAudience}`,

                    type: "default"
                }
            ]
        },
        metadata: {}
    };
    
    // Return the final structure
    return {
        status: "success",
        data: {
            list: Array.isArray(transformedData) ? transformedData : [transformedData],
            filters: data.filters || {}
        }
    };
}

// Export the transformation function
console.log("JSONata transformation library loaded successfully!");
"JSONata transformation functions available: transformEventData, lookup, fromMillis, toMillis";